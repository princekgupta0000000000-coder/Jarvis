package com.example.jarvis

import com.example.jarvis.ai.IntentRouter
import com.example.jarvis.data.CollegeData
import java.time.LocalDate
import android.Manifest
import android.content.pm.PackageManager
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

private fun todayName()=SimpleDateFormat("EEEE",Locale.ENGLISH).format(Date())
private fun todaySchedule()=weeklySchedule[todayName()].orEmpty()
private fun scheduleText()=todaySchedule().joinToString("; "){"${it.start} ${it.subject}"}
private fun greeting():String{val h=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);return when(h){in 5..11->"सुप्रभात";in 12..16->"नमस्कार";in 17..20->"शुभ संध्या";else->"शुभ रात्रि"}}

class MainActivity:ComponentActivity(){
 private var trigger by mutableStateOf(false)
 private val mic=registerForActivityResult(ActivityResultContracts.RequestPermission()){if(it)trigger=true}
 override fun onCreate(b:Bundle?){super.onCreate(b);setContent{MaterialTheme{Surface(Modifier.fillMaxSize(),color=Color(0xFF05070D)){JarvisHome(trigger,{trigger=false}){if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED)trigger=true else mic.launch(Manifest.permission.RECORD_AUDIO)}}}}}
}

@Composable
fun JarvisHome(trigger:Boolean,consume:()->Unit,requestMic:()->Unit){
 val context=LocalContext.current
 var chat by remember{mutableStateOf(false)}
 var schedule by remember{mutableStateOf(false)}
 var state by remember{mutableStateOf(VoiceState.IDLE)}
 var modelInstalled by remember{mutableStateOf(ModelManager.isInstalled(context))}
 var installing by remember{mutableStateOf(false)}
 var installError by remember{mutableStateOf(false)}
 val modelPicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null){installing=true;installError=false;Thread{val ok=ModelManager.installFromUri(context,uri);Handler(Looper.getMainLooper()).post{installing=false;modelInstalled=ok;installError=!ok}}.start()}}
 lateinit var voice:VoiceManager
 voice=remember{VoiceManager(context,onState={state=it},onText={text->
  val appResult = IntentRouter.execute(context, text)

if (appResult != null) {
    state = VoiceState.SPEAKING
    voice.speak(appResult)
    return@VoiceManager
}
  val lower=text.lowercase(Locale.getDefault())
  val today = LocalDate.now()

val holiday = CollegeData.holidays.find {
    !today.isBefore(it.from) && !today.isAfter(it.to)
}

if (holiday != null) {
    voice.speak("Aaj college band hai. ${holiday.name} ki chhutti chal rahi hai.")
    return@VoiceManager
}

if (
    lower.contains("class") ||
    lower.contains("schedule") ||
    text.contains("क्लास") ||
    text.contains("शेड्यूल")
) {

    val day = today.dayOfWeek.name

    val list = CollegeData.weeklySchedule[day] ?: emptyList()

    if (list.isEmpty()) {
        voice.speak("Aaj Sunday hai. College off hai.")
    } else {

        val msg = buildString {
            append("Aaj ${day.lowercase()} ko ${list.size} classes hain. ")
            list.forEach {
                append("${it.start} ${it.subject}. ")
            }
        }

        voice.speak(msg)
    }

    return@VoiceManager
}
  val isClassQuery=lower.contains("class")||lower.contains("classes")||lower.contains("schedule")||lower.contains("timetable")||lower.contains("next class")||lower.contains("class kab")||text.contains("क्लास")||text.contains("कक्ष")||text.contains("शेड्यूल")||text.contains("टाइमटेबल")||text.contains("आज की पढ़ाई")
  val direct=when{
   lower.contains("hello")||lower.contains("hi")||lower.contains("namaste")||text.contains("नमस्ते")->"${greeting()}! मैं JARVIS हूँ। मैं आपकी मदद के लिए तैयार हूँ।"
   isClassQuery&&(lower.contains("today")||lower.contains("aaj")||text.contains("आज")||text.contains("क्लास")||text.contains("आज की"))->if(todaySchedule().isEmpty())"आज ${todayName()} है और आज कोई class नहीं है।" else "आज ${todayName()} की classes हैं: ${scheduleText()}"
   lower.contains("time")||text.contains("समय")||text.contains("बजे")->"अभी समय ${SimpleDateFormat("h:mm a",Locale.ENGLISH).format(Date())} है।"
   lower.contains("date")||text.contains("तारीख")->"आज की तारीख ${SimpleDateFormat("d MMMM yyyy",Locale.ENGLISH).format(Date())} है।"
   else->null
  }
  if(direct!=null){state=VoiceState.SPEAKING;voice.speak(direct)}else if(!modelInstalled){state=VoiceState.SPEAKING;voice.speak("मेरा local brain अभी install नहीं है। पहले Gemma 3 1B model select कीजिए।")}else{state=VoiceState.THINKING;val engine=LocalLlmEngine(context);engine.initialize{ready->if(ready){val prompt="You are a helpful personal Android assistant named JARVIS. Understand Hindi, Hinglish and English. Always reply in the same language as the user. If the user asks about today's classes or schedule, use this timetable and do not invent classes. Today is ${todayName()}. Today's timetable: ${scheduleText()}. User: $text Assistant:";engine.generate(prompt){answer->val clean=answer.replace("<start_of_turn>assistant","",true).replace("<end_of_turn>","",true).trim();state=VoiceState.SPEAKING;voice.speak(if(clean.isBlank())"माफ कीजिए, मैं जवाब तैयार नहीं कर पाया।" else clean);engine.close()}}else{state=VoiceState.SPEAKING;voice.speak("Local brain load नहीं हो पाया। Gemma model को फिर से select कीजिए।");engine.close()}}}
 })}
 DisposableEffect(Unit){onDispose{voice.release()}}
 LaunchedEffect(trigger){if(trigger){consume();if(modelInstalled)voice.startListening()else voice.speak("पहले Gemma 3 1B model install कीजिए।")}}
if(chat){
    ChatScreen(
        modifier = Modifier.fillMaxSize(),
        onClose = { chat = false }
    )
    return
}
 if(schedule){ScheduleScreen(onClose={schedule=false});return}
 val transition=rememberInfiniteTransition(label="jarvis")
 val listening=state==VoiceState.LISTENING;val thinking=state==VoiceState.THINKING
 val pulse by transition.animateFloat(if(listening).9f else .95f,if(listening)1.18f else 1.07f,infiniteRepeatable(tween(if(listening)550 else 1800),RepeatMode.Reverse),label="pulse")
 val rotation by transition.animateFloat(0f,360f,infiniteRepeatable(tween(if(thinking)1500 else 9000),RepeatMode.Restart),label="rotation")
 Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF111A2D),Color(0xFF080B13),Color(0xFF030409)),radius=900f))){Column(Modifier.fillMaxSize().padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally){TopBar();Spacer(Modifier.weight(.8f));Text("J A R V I S",color=Color.White,fontSize=27.sp,fontWeight=FontWeight.Light,letterSpacing=7.sp);Spacer(Modifier.height(32.dp));JarvisOrb(pulse,rotation,state);Spacer(Modifier.height(28.dp));Text(state.name,color=Color(0xFF8FA8C7),fontSize=12.sp,letterSpacing=4.sp);Text(when(state){VoiceState.IDLE->"Awaiting your command";VoiceState.LISTENING->"I'm listening...";VoiceState.THINKING->"Processing locally...";VoiceState.SPEAKING->"JARVIS is responding..."},color=Color(0xFF596579),fontSize=13.sp);Spacer(Modifier.weight(1f));VoiceButton(requestMic,state!=VoiceState.IDLE);Spacer(Modifier.height(24.dp));QuickActions({chat=true},{schedule=true});Spacer(Modifier.height(12.dp))}}
 if(!modelInstalled)ModelSetupCard(installing,installError){modelPicker.launch(arrayOf("application/octet-stream","application/x-tflite","*/*"))}
}

@Composable fun ModelSetupCard(installing:Boolean,error:Boolean,onSelect:()->Unit){Box(Modifier.fillMaxSize().background(Color(0xD905070D)),Alignment.Center){Column(Modifier.fillMaxWidth(.88f).background(Color(0xFF0B111D),RoundedCornerShape(26.dp)).border(1.dp,Color(0xFF304662),RoundedCornerShape(26.dp)).padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("JARVIS LOCAL BRAIN",color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Medium,letterSpacing=2.sp);Spacer(Modifier.height(10.dp));Text(if(installing)"Installing Gemma 3 1B…" else "Gemma 3 1B model required",color=Color(0xFF9EB2CA),fontSize=14.sp);Spacer(Modifier.height(8.dp));Text(if(error)"Install failed. Please select the .task file again." else "Select the downloaded Gemma 3 1B INT4 .task file.",color=Color(0xFF64758C),fontSize=12.sp);Spacer(Modifier.height(18.dp));if(installing)LinearProgressIndicator(Modifier.fillMaxWidth())else Button(onClick=onSelect,modifier=Modifier.fillMaxWidth()){Text("SELECT GEMMA MODEL")}}}}
@Composable fun TopBar(){Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween,Alignment.CenterVertically){Text("J",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Bold);Text("SYSTEM ONLINE",color=Color(0xFF71829B),fontSize=10.sp,letterSpacing=2.sp);Text("•••",color=Color(0xFF91A6C1),fontSize=16.sp)}}
@Composable fun JarvisOrb(pulse:Float,rotation:Float,state:VoiceState){Box(Modifier.size(250.dp),Alignment.Center){Canvas(Modifier.fillMaxSize().alpha(if(state==VoiceState.LISTENING).95f else .6f)){val c=Offset(size.width/2,size.height/2);val radius=size.minDimension/2.25f;repeat(32){val a=Math.toRadians((it*360f/32+rotation).toDouble());drawCircle(Color(0xFF6E8DB7),2.7f,Offset(c.x+radius*cos(a).toFloat(),c.y+radius*sin(a).toFloat()))}};Canvas(Modifier.size(210.dp).scale(pulse)){val c=Offset(size.width/2,size.height/2);drawCircle(Brush.radialGradient(listOf(Color(0xFFB7D9FF),Color(0xFF5077A8).copy(.45f),Color.Transparent)),size.minDimension/2.6f,c);drawCircle(Color(0xFF8EB6E5).copy(.35f),size.minDimension/3.1f,c,style=Stroke(2f));drawCircle(Color(0xFFC5E3FF),size.minDimension/7f,c)};Canvas(Modifier.size(175.dp).scale(pulse)){drawArc(Color(0xFF88A9D1),rotation,105f,false,style=Stroke(2.5f,cap=StrokeCap.Round));drawArc(Color(0xFF55769F),rotation+180f,70f,false,style=Stroke(2f,cap=StrokeCap.Round))}}}
@Composable fun VoiceButton(onClick:()->Unit,active:Boolean){Box(Modifier.size(190.dp,52.dp).border(1.dp,if(active)Color(0xFF7398C5)else Color(0xFF344963),RoundedCornerShape(28.dp)).clickable{onClick()},Alignment.Center){Text("SPEAK TO JARVIS",color=Color(0xFFB8C9DE),fontSize=11.sp,letterSpacing=2.sp)}}
@Composable fun QuickActions(chat:()->Unit,schedule:()->Unit){Row(Modifier.fillMaxWidth(),Arrangement.SpaceEvenly){ActionItem("CHAT",chat);ActionItem("SCHEDULE",schedule);ActionItem("ATTENDANCE") {}}}
@Composable fun ActionItem(title:String,onClick:()->Unit){Box(Modifier.size(105.dp,58.dp).border(1.dp,Color(0xFF1E2A3D),RoundedCornerShape(14.dp)).clickable{onClick()},Alignment.Center){Text(title,color=Color(0xFF65758B),fontSize=9.sp,letterSpacing=1.5.sp)}}
