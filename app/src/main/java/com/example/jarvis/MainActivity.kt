package com.example.jarvis

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.jarvis.ai.LocalLlmEngine
import com.example.jarvis.ai.ModelManager
import com.example.jarvis.ui.chat.ChatScreen
import com.example.jarvis.voice.VoiceManager
import com.example.jarvis.voice.VoiceState
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

private data class ClassSlot(val start:String,val end:String,val subject:String)
private val schedule=mapOf(
"Monday" to listOf(ClassSlot("10:00 AM","10:50 AM","Introduction to AI"),ClassSlot("10:50 AM","11:40 AM","Computer Fundamentals"),ClassSlot("11:40 AM","12:30 PM","Basic Electronics"),ClassSlot("12:30 PM","1:20 PM","Essence of Indian Constitution"),ClassSlot("2:20 PM","4:00 PM","Physics Lab")),
"Tuesday" to listOf(ClassSlot("11:40 AM","12:30 PM","Physics"),ClassSlot("12:30 PM","1:20 PM","Basic Electronics"),ClassSlot("2:20 PM","3:10 PM","Essence of Indian Constitution")),
"Wednesday" to listOf(ClassSlot("10:00 AM","10:50 AM","Physics"),ClassSlot("10:50 AM","11:40 AM","Mathematics"),ClassSlot("11:40 AM","12:30 PM","Basic Electronics"),ClassSlot("12:30 PM","1:20 PM","Introduction to AI"),ClassSlot("2:20 PM","3:10 PM","Essence of Indian Constitution"),ClassSlot("4:00 PM","5:00 PM","Universal Human Values")),
"Thursday" to listOf(ClassSlot("3:10 PM","4:00 PM","Mathematics"),ClassSlot("4:00 PM","5:00 PM","Universal Human Values")),
"Friday" to listOf(ClassSlot("10:00 AM","11:40 AM","PPS Lab"),ClassSlot("11:40 AM","1:20 PM","Basic Electronics Lab"),ClassSlot("2:20 PM","3:10 PM","Physics"),ClassSlot("3:10 PM","4:00 PM","Mathematics")),
"Saturday" to listOf(ClassSlot("10:00 AM","10:50 AM","Computer Fundamentals"),ClassSlot("10:50 AM","11:40 AM","Computer Fundamentals"),ClassSlot("11:40 AM","12:30 PM","Introduction to AI")),"Sunday" to emptyList())
private fun todayName()=SimpleDateFormat("EEEE",Locale.ENGLISH).format(Date())
private fun greeting():String{val h=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);return when(h){in 5..11->"सुप्रभात";in 12..16->"नमस्कार";in 17..20->"शुभ संध्या";else->"शुभ रात्रि"}}
private fun timetableText(day:String=todayName())=schedule[day].orEmpty().joinToString("; "){"${it.start} ${it.subject}"}

class MainActivity:ComponentActivity(){
 private var voiceTrigger by mutableStateOf(false)
 private val mic=registerForActivityResult(ActivityResultContracts.RequestPermission()){if(it)voiceTrigger=true}
 override fun onCreate(b:Bundle?){super.onCreate(b);setContent{MaterialTheme{Surface(Modifier.fillMaxSize(),color=Color(0xFF05070D)){JarvisHome(voiceTrigger,{voiceTrigger=false}){if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED)voiceTrigger=true else mic.launch(Manifest.permission.RECORD_AUDIO)}}}}}
}

@Composable fun JarvisHome(trigger:Boolean,consume:()->Unit,requestMic:()->Unit){
 var chat by remember{mutableStateOf(false)};var showSchedule by remember{mutableStateOf(false)};var state by remember{mutableStateOf(VoiceState.IDLE)};val ctx=LocalContext.current
 var modelInstalled by remember{mutableStateOf(ModelManager.isInstalled(ctx))};var installing by remember{mutableStateOf(false)};var installError by remember{mutableStateOf(false)}
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri:Uri?->if(uri!=null){installing=true;Thread{val ok=ModelManager.installFromUri(ctx,uri);Handler(Looper.getMainLooper()).post{installing=false;modelInstalled=ok;installError=!ok}}.start()}}
 lateinit var voice:VoiceManager
 voice=remember{VoiceManager(ctx,onState={state=it},onText={text->
   if(!modelInstalled){voice.speak("मेरा local brain अभी install नहीं है। पहले Gemma model select कीजिए।");return@VoiceManager}
   val lower=text.lowercase(Locale.getDefault());val day=todayName()
   val instant=when{lower.contains("greeting")||lower.contains("hello")||lower.contains("hi")||lower.contains("namaste")||lower.contains("नमस्ते")->"${greeting()}! मैं JARVIS हूँ। मैं आपकी मदद के लिए तैयार हूँ।";lower.contains("aaj")&&lower.contains("class")||lower.contains("today")&&lower.contains("class")->"आज $day है। आपकी classes हैं: ${timetableText()}";lower.contains("schedule")||lower.contains("timetable")||lower.contains("class kab")||lower.contains("next class")->"आज $day है। Schedule: ${timetableText()}";lower.contains("time")||text.contains("समय")||text.contains("बजे")->SimpleDateFormat("h:mm a",Locale.ENGLISH).format(Date());lower.contains("date")||text.contains("तारीख")->SimpleDateFormat("d MMMM yyyy",Locale.ENGLISH).format(Date());else->null}
   if(instant!=null){state=VoiceState.SPEAKING;voice.speak(instant);return@VoiceManager}
   state=VoiceState.THINKING;val engine=LocalLlmEngine(ctx);engine.initialize{ready->if(ready){val prompt="You are JARVIS, a helpful personal Android assistant. Understand Hindi, Hinglish and English. Reply in the same language as the user. Today is $day. Today's classes: ${timetableText()}. User: $text Assistant:";engine.generate(prompt){r->val a=r.replace("<start_of_turn>assistant","",true).replace("<end_of_turn>","",true).trim();state=VoiceState.SPEAKING;voice.speak(if(a.isBlank())"माफ कीजिए, मैं जवाब तैयार नहीं कर पाया।" else a);engine.close()}}else{state=VoiceState.SPEAKING;voice.speak("Local brain load नहीं हो पाया। Model को फिर से select कीजिए।");engine.close()}}}
 })}
 DisposableEffect(Unit){onDispose{voice.release()}}
 LaunchedEffect(trigger,modelInstalled){if(trigger){consume();if(modelInstalled)voice.startListening()}}
 val tr=rememberInfiniteTransition(label="orb");val listening=state==VoiceState.LISTENING;val thinking=state==VoiceState.THINKING
 val pulse by tr.animateFloat(if(listening).9f else .95f,if(listening)1.18f else 1.07f,infiniteRepeatable(tween(if(listening)550 else 1800),RepeatMode.Reverse),label="pulse");val rot by tr.animateFloat(0f,360f,infiniteRepeatable(tween(if(thinking)1500 else 9000),RepeatMode.Restart),label="rot")
 Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF111A2D),Color(0xFF080B13),Color(0xFF030409)),radius=900f))){Column(Modifier.fillMaxSize().padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally){TopBar();Spacer(Modifier.weight(.8f));Text("J A R V I S",color=Color.White,fontSize=27.sp,fontWeight=FontWeight.Light,letterSpacing=7.sp);Spacer(Modifier.height(32.dp));Orb(pulse,rot,state);Spacer(Modifier.height(28.dp));Text(state.name,color=Color(0xFF8FA8C7),fontSize=12.sp,letterSpacing=4.sp);Text(when(state){VoiceState.IDLE->"Awaiting your command";VoiceState.LISTENING->"I'm listening...";VoiceState.THINKING->"Processing locally...";VoiceState.SPEAKING->"JARVIS is responding..."},color=Color(0xFF596579),fontSize=13.sp);Spacer(Modifier.weight(1f));VoiceButton(requestMic,state!=VoiceState.IDLE);Spacer(Modifier.height(24.dp));QuickActions({chat=true},{showSchedule=true});Spacer(Modifier.height(12.dp))}}
 if(chat)Panel{ChatScreen(onClose={chat=false})};if(showSchedule)SchedulePanel{showSchedule=false};if(!modelInstalled)BrainSetupOverlay(installing,installError){picker.launch(arrayOf("application/octet-stream","application/x-tflite","*/*"))}
 }
}
@Composable fun BrainSetupOverlay(installing:Boolean,error:Boolean,select:()->Unit){Box(Modifier.fillMaxSize().background(Color(0xD905070D)),Alignment.Center){Column(Modifier.fillMaxWidth(.88f).background(Color(0xFF0B111D),RoundedCornerShape(26.dp)).border(1.dp,Color(0xFF304662),RoundedCornerShape(26.dp)).padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("JARVIS BRAIN",color=Color.White,fontSize=22.sp,letterSpacing=3.sp);Spacer(Modifier.height(10.dp));Text(if(installing)"Installing Gemma 3 1B…" else "Local brain is not installed",color=Color(0xFF9EB2CA),fontSize=14.sp);Spacer(Modifier.height(8.dp));Text(if(error)"File install नहीं हुआ। Gemma .task file select करें." else "Downloaded Gemma 3 1B INT4 .task model select करें.",color=Color(0xFF64758C),fontSize=12.sp);Spacer(Modifier.height(20.dp));if(installing)LinearProgressIndicator(Modifier.fillMaxWidth())else OutlinedButton(onClick=select,modifier=Modifier.fillMaxWidth()){Text("SELECT GEMMA MODEL")}}}}
@Composable fun TopBar(){Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween,Alignment.CenterVertically){Text("J",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Bold);Text("SYSTEM ONLINE",color=Color(0xFF71829B),fontSize=10.sp,letterSpacing=2.sp);Text("•••",color=Color(0xFF91A6C1),fontSize=16.sp)}}
@Composable fun Orb(p:Float,r:Float,s:VoiceState){Box(Modifier.size(250.dp),Alignment.Center){Canvas(Modifier.fillMaxSize().alpha(if(s==VoiceState.LISTENING).95f else .55f)){val c=Offset(size.width/2,size.height/2);val n=30;val rr=size.minDimension/2.25f;repeat(n){val a=Math.toRadians((it*360f/n+r).toDouble());drawCircle(Color(0xFF6E8DB7),2.7f,Offset(c.x+rr*cos(a).toFloat(),c.y+rr*sin(a).toFloat()))}};Canvas(Modifier.size(210.dp).scale(p)){val c=Offset(size.width/2,size.height/2);drawCircle(Brush.radialGradient(listOf(Color(0xFFB7D9FF),Color(0xFF5077A8).copy(.45f),Color.Transparent)),size.minDimension/2.6f,c);drawCircle(Color(0xFF8EB6E5).copy(.35f),size.minDimension/3.1f,c,style=Stroke(2f));drawCircle(Color(0xFFC5E3FF),size.minDimension/7f,c)};Canvas(Modifier.size(175.dp).scale(p)){drawArc(Color(0xFF88A9D1),r,105f,false,style=Stroke(2.5f,cap=StrokeCap.Round));drawArc(Color(0xFF55769F),r+180f,70f,false,style=Stroke(2f,cap=StrokeCap.Round))}}}
@Composable fun VoiceButton(click:()->Unit,active:Boolean){Box(Modifier.size(190.dp,52.dp).border(1.dp,if(active)Color(0xFF7398C5)else Color(0xFF344963),RoundedCornerShape(28.dp)).clickable{click()},Alignment.Center){Text("SPEAK TO JARVIS",color=Color(0xFFB8C9DE),fontSize=11.sp,letterSpacing=2.sp)}}
@Composable fun QuickActions(chat:()->Unit,schedule:()->Unit){Row(Modifier.fillMaxWidth(),Arrangement.SpaceEvenly){Action("CHAT",chat);Action("SCHEDULE",schedule);Action("ATTENDANCE") {}}}
@Composable fun Action(t:String,c:()->Unit){Box(Modifier.size(105.dp,58.dp).border(1.dp,Color(0xFF1E2A3D),RoundedCornerShape(14.dp)).clickable{c()},Alignment.Center){Text(t,color=Color(0xFF65758B),fontSize=9.sp,letterSpacing=1.5.sp)}}
@Composable fun Panel(content:@Composable()->Unit){Box(Modifier.fillMaxSize().background(Color(0xFF080D17))){content()}}
@Composable fun SchedulePanel(close:()->Unit){Panel{Column(Modifier.fillMaxSize().padding(20.dp)){Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween,Alignment.CenterVertically){Text("WEEKLY SCHEDULE",color=Color.White,fontSize=18.sp);Text("CLOSE",color=Color(0xFF8FA8C7),fontSize=11.sp,modifier=Modifier.clickable{close()})};Spacer(Modifier.height(14.dp));Column{schedule.forEach{(day,slots)->Text(day,color=Color(0xFF9FC4EE),fontSize=13.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=8.dp,bottom=3.dp));if(slots.isEmpty())Text("OFF",color=Color(0xFF596579),fontSize=12.sp)else slots.forEach{s->Text("${s.start} – ${s.end}   ${s.subject}",color=Color(0xFFB8C9DE),fontSize=12.sp,modifier=Modifier.padding(vertical=2.dp))}}}}}}
