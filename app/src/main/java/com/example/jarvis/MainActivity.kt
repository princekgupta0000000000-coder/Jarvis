package com.example.jarvis

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.jarvis.ui.chat.ChatScreen
import com.example.jarvis.voice.VoiceManager
import com.example.jarvis.voice.VoiceState
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

private fun todayName() = SimpleDateFormat("EEEE", Locale.ENGLISH).format(Date())
private fun todaySchedule() = weeklySchedule[todayName()].orEmpty()
private fun scheduleForPrompt() = todaySchedule().joinToString("; ") { "${it.start} ${it.subject}" }
private fun greeting(): String { val h=Calendar.getInstance().get(Calendar.HOUR_OF_DAY); return when(h){in 5..11->"सुप्रभात";in 12..16->"नमस्कार";in 17..20->"शुभ संध्या";else->"शुभ रात्रि"} }

class MainActivity : ComponentActivity() {
    private var voiceTrigger by mutableStateOf(false)
    private val micPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) voiceTrigger=true }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface(Modifier.fillMaxSize(), color=Color(0xFF05070D)) { JarvisHome(voiceTrigger, {voiceTrigger=false}) { if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED) voiceTrigger=true else micPermission.launch(Manifest.permission.RECORD_AUDIO) } } } }
    }
}

@Composable
fun JarvisHome(trigger:Boolean, consume:()->Unit, requestMic:()->Unit) {
    var showChat by remember { mutableStateOf(false) }
    var showSchedule by remember { mutableStateOf(false) }
    var state by remember { mutableStateOf(VoiceState.IDLE) }
    val context=LocalContext.current
    lateinit var voice: VoiceManager
    voice=remember { VoiceManager(context,onState={state=it},onText={text->
        val lower=text.lowercase(Locale.getDefault())
        val direct=when {
            lower.contains("hello")||lower.contains("hi")||lower.contains("namaste")||text.contains("नमस्ते") -> "${greeting()}! मैं JARVIS हूँ। मैं आपकी मदद के लिए तैयार हूँ।"
            (lower.contains("today")&&lower.contains("class"))||(lower.contains("aaj")&&lower.contains("class")) -> "आज ${todayName()} है। आपकी classes हैं: ${scheduleForPrompt()}"
            lower.contains("schedule")||lower.contains("timetable")||lower.contains("next class")||lower.contains("class kab") -> "आज ${todayName()} है। ${if(todaySchedule().isEmpty()) "आज आपकी कोई class नहीं है।" else "आज का schedule: ${scheduleForPrompt()}"}"
            lower.contains("time")||text.contains("समय")||text.contains("बजे") -> SimpleDateFormat("h:mm a",Locale.ENGLISH).format(Date())
            lower.contains("date")||text.contains("तारीख") -> SimpleDateFormat("d MMMM yyyy",Locale.ENGLISH).format(Date())
            else -> null
        }
        if(direct!=null){state=VoiceState.SPEAKING;voice.speak(direct)}
        else {
            state=VoiceState.THINKING
            val engine=LocalLlmEngine(context)
            engine.initialize { ready ->
                if(ready){
                    val prompt="You are JARVIS, a helpful personal Android assistant. Understand Hindi, Hinglish and English. Reply in the same language as the user. Today is ${todayName()}. Today's timetable: ${scheduleForPrompt()}. User: $text Assistant:"
                    engine.generate(prompt){answer->
                        val clean=answer.replace("<start_of_turn>assistant","",true).replace("<end_of_turn>","",true).trim()
                        state=VoiceState.SPEAKING
                        voice.speak(if(clean.isBlank()) "माफ कीजिए, मैं जवाब तैयार नहीं कर पाया।" else clean)
                        engine.close()
                    }
                } else { state=VoiceState.SPEAKING;voice.speak("मेरा local brain अभी तैयार नहीं है। पहले Gemma model install कीजिए।");engine.close() }
            }
        }
    }) }
    DisposableEffect(Unit){onDispose{voice.release()}}
    LaunchedEffect(trigger){if(trigger){consume();voice.startListening()}}
    val transition=rememberInfiniteTransition(label="jarvis")
    val listening=state==VoiceState.LISTENING; val thinking=state==VoiceState.THINKING
    val pulse by transition.animateFloat(if(listening).9f else .95f,if(listening)1.18f else 1.07f,infiniteRepeatable(tween(if(listening)550 else 1800),RepeatMode.Reverse),label="pulse")
    val rotation by transition.animateFloat(0f,360f,infiniteRepeatable(tween(if(thinking)1500 else 9000),RepeatMode.Restart),label="rotation")
    Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF111A2D),Color(0xFF080B13),Color(0xFF030409)),radius=900f))){
        Column(Modifier.fillMaxSize().padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally){
            TopBar();Spacer(Modifier.weight(.8f));Text("J A R V I S",color=Color.White,fontSize=27.sp,fontWeight=FontWeight.Light,letterSpacing=7.sp);Spacer(Modifier.height(32.dp));JarvisOrb(pulse,rotation,state);Spacer(Modifier.height(28.dp));Text(state.name,color=Color(0xFF8FA8C7),fontSize=12.sp,letterSpacing=4.sp);Text(when(state){VoiceState.IDLE->"Awaiting your command";VoiceState.LISTENING->"I'm listening...";VoiceState.THINKING->"Processing locally...";VoiceState.SPEAKING->"JARVIS is responding..."},color=Color(0xFF596579),fontSize=13.sp);Spacer(Modifier.weight(1f));VoiceButton(requestMic,state!=VoiceState.IDLE);Spacer(Modifier.height(24.dp));QuickActions({showChat=true},{showSchedule=true});Spacer(Modifier.height(12.dp))
        }
        if(showChat) FullPanel { ChatScreen(onClose={showChat=false}) }
        if(showSchedule) FullPanel { ScheduleScreen(onClose={showSchedule=false}) }
    }
}

@Composable fun TopBar(){Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween,Alignment.CenterVertically){Text("J",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Bold);Text("SYSTEM ONLINE",color=Color(0xFF71829B),fontSize=10.sp,letterSpacing=2.sp);Text("•••",color=Color(0xFF91A6C1),fontSize=16.sp)}}
@Composable fun JarvisOrb(pulse:Float,rotation:Float,state:VoiceState){Box(Modifier.size(250.dp),Alignment.Center){Canvas(Modifier.fillMaxSize().alpha(if(state==VoiceState.LISTENING).95f else .6f)){val c=Offset(size.width/2,size.height/2);val radius=size.minDimension/2.25f;repeat(32){val a=Math.toRadians((it*360f/32+rotation).toDouble());drawCircle(Color(0xFF6E8DB7),2.7f,Offset(c.x+radius*cos(a).toFloat(),c.y+radius*sin(a).toFloat()))}};Canvas(Modifier.size(210.dp).scale(pulse)){val c=Offset(size.width/2,size.height/2);drawCircle(Brush.radialGradient(listOf(Color(0xFFB7D9FF),Color(0xFF5077A8).copy(.45f),Color.Transparent)),size.minDimension/2.6f,c);drawCircle(Color(0xFF8EB6E5).copy(.35f),size.minDimension/3.1f,c,style=Stroke(2f));drawCircle(Color(0xFFC5E3FF),size.minDimension/7f,c)};Canvas(Modifier.size(175.dp).scale(pulse)){drawArc(Color(0xFF88A9D1),rotation,105f,false,style=Stroke(2.5f,cap=StrokeCap.Round));drawArc(Color(0xFF55769F),rotation+180f,70f,false,style=Stroke(2f,cap=StrokeCap.Round))}}}
@Composable fun VoiceButton(onClick:()->Unit,active:Boolean){Box(Modifier.size(190.dp,52.dp).border(1.dp,if(active)Color(0xFF7398C5)else Color(0xFF344963),RoundedCornerShape(28.dp)).clickable{onClick()},Alignment.Center){Text("SPEAK TO JARVIS",color=Color(0xFFB8C9DE),fontSize=11.sp,letterSpacing=2.sp)}}
@Composable fun QuickActions(chat:()->Unit,schedule:()->Unit){Row(Modifier.fillMaxWidth(),Arrangement.SpaceEvenly){ActionItem("CHAT",chat);ActionItem("SCHEDULE",schedule);ActionItem("ATTENDANCE") {}}}
@Composable fun ActionItem(title:String,onClick:()->Unit){Box(Modifier.size(105.dp,58.dp).border(1.dp,Color(0xFF1E2A3D),RoundedCornerShape(14.dp)).clickable{onClick()},Alignment.Center){Text(title,color=Color(0xFF65758B),fontSize=9.sp,letterSpacing=1.5.sp)}}
@Composable fun FullPanel(content:@Composable()->Unit){Box(Modifier.fillMaxSize().background(Color(0xFF080D17))){content()}}
