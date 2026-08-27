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
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    private var voiceTrigger by mutableStateOf(false)
    private val micPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) voiceTrigger = true }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme { Surface(Modifier.fillMaxSize(), color = Color(0xFF05070D)) {
                JarvisHome(voiceTrigger, { voiceTrigger = false }) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) voiceTrigger = true else micPermission.launch(Manifest.permission.RECORD_AUDIO)
                }
            } }
        }
    }
}

@Composable
fun JarvisHome(voiceTrigger: Boolean, onConsumeVoiceTrigger: () -> Unit, requestMic: () -> Unit) {
    var showChat by remember { mutableStateOf(false) }
    var voiceState by remember { mutableStateOf(VoiceState.IDLE) }
    val context = LocalContext.current
    lateinit var activeVoice: VoiceManager
    activeVoice = remember {
        VoiceManager(context, onState = { voiceState = it }, onText = { text ->
            voiceState = VoiceState.THINKING
            val engine = LocalLlmEngine(context)
            engine.initialize { ready ->
                if (ready) engine.generate(text) { response -> voiceState = VoiceState.SPEAKING; activeVoice.speak(response); engine.close() }
                else { voiceState = VoiceState.SPEAKING; activeVoice.speak("My local brain is not installed yet. Install the local model to activate intelligence."); engine.close() }
            }
        })
    }
    DisposableEffect(Unit) { onDispose { activeVoice.release() } }
    LaunchedEffect(voiceTrigger) { if (voiceTrigger) { onConsumeVoiceTrigger(); activeVoice.startListening() } }

    val transition = rememberInfiniteTransition(label = "jarvis")
    val listening = voiceState == VoiceState.LISTENING
    val thinking = voiceState == VoiceState.THINKING
    val pulse by transition.animateFloat(if (listening) .88f else .94f, if (listening) 1.20f else 1.08f, infiniteRepeatable(tween(if (listening) 550 else 1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")
    val rotation by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(if (thinking) 1500 else 9000), RepeatMode.Restart), label = "rotation")

    Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF111A2D), Color(0xFF080B13), Color(0xFF030409)), radius = 900f))) {
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(); Spacer(Modifier.weight(.8f))
            Text("J A R V I S", Color.White, 27.sp, FontWeight.Light, letterSpacing = 7.sp)
            Spacer(Modifier.height(32.dp)); JarvisCore(pulse, rotation, voiceState); Spacer(Modifier.height(30.dp))
            Text(when (voiceState) { VoiceState.IDLE -> "READY"; VoiceState.LISTENING -> "LISTENING"; VoiceState.THINKING -> "THINKING"; VoiceState.SPEAKING -> "SPEAKING" }, Color(0xFF8FA8C7), 12.sp, letterSpacing = 4.sp)
            Spacer(Modifier.height(8.dp)); Text(when (voiceState) { VoiceState.IDLE -> "Awaiting your command"; VoiceState.LISTENING -> "I'm listening..."; VoiceState.THINKING -> "Processing locally..."; VoiceState.SPEAKING -> "JARVIS is responding..." }, Color(0xFF596579), 13.sp)
            Spacer(Modifier.weight(1f)); VoiceButton(requestMic, voiceState != VoiceState.IDLE); Spacer(Modifier.height(24.dp)); QuickActions { showChat = true }; Spacer(Modifier.height(12.dp))
        }
        if (showChat) Box(Modifier.fillMaxWidth().fillMaxHeight(.62f).align(Alignment.BottomCenter).background(Color(0xFF080D17), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).border(1.dp, Color(0xFF253852), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))) { ChatScreen(onClose = { showChat = false }) }
    }
}

@Composable fun TopBar() { Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Text("J", Color.White, 22.sp, FontWeight.Bold); Text("SYSTEM ONLINE", Color(0xFF71829B), 10.sp, letterSpacing = 2.sp); Box(Modifier.size(38.dp).border(1.dp, Color(0xFF26344B), CircleShape), Alignment.Center) { Text("•••", Color(0xFF91A6C1), 12.sp, letterSpacing = 2.sp) } } }

@Composable fun JarvisCore(pulse: Float, rotation: Float, state: VoiceState) { Box(Modifier.size(250.dp), Alignment.Center) {
    Canvas(Modifier.fillMaxSize().alpha(if (state == VoiceState.LISTENING) .95f else .55f)) { val center = Offset(size.width / 2, size.height / 2); val count = if (state == VoiceState.LISTENING) 36 else 24; val radius = size.minDimension / 2.25f; for (i in 0 until count) { val angle = Math.toRadians((i * 360f / count + rotation).toDouble()); drawCircle(Color(0xFF6E8DB7), if (state == VoiceState.LISTENING) 3f else 2.5f, Offset(center.x + radius * cos(angle).toFloat(), center.y + radius * sin(angle).toFloat())) } }
    Canvas(Modifier.size(210.dp).scale(pulse)) { val center = Offset(size.width / 2, size.height / 2); drawCircle(Brush.radialGradient(listOf(Color(0xFFB7D9FF).copy(.95f), Color(0xFF5077A8).copy(.45f), Color.Transparent)), size.minDimension / 2.6f, center); drawCircle(Color(0xFF8EB6E5).copy(.35f), size.minDimension / 3.1f, center, style = Stroke(2f)); drawCircle(Color(0xFFC5E3FF), size.minDimension / 7f, center) }
    Canvas(Modifier.size(175.dp).scale(pulse)) { drawArc(Color(0xFF88A9D1), rotation, 105f, false, style = Stroke(2.5f, cap = StrokeCap.Round)); drawArc(Color(0xFF55769F), rotation + 180f, 70f, false, style = Stroke(2f, cap = StrokeCap.Round)) }
} }

@Composable fun VoiceButton(onClick: () -> Unit, active: Boolean) { Box(Modifier.size(190.dp, 52.dp).border(1.dp, if (active) Color(0xFF7398C5) else Color(0xFF344963), RoundedCornerShape(28.dp)).clickable(onClick = onClick), Alignment.Center) { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { Canvas(Modifier.size(18.dp)) { val c = Offset(size.width / 2, size.height / 2); drawCircle(Color(0xFF9FC4EE), 5f, c); drawCircle(Color(0xFF6D8EB8), 8f, c, style = Stroke(1.5f)) }; Text("SPEAK TO JARVIS", Color(0xFFB8C9DE), 11.sp, letterSpacing = 2.sp) } } }
@Composable fun QuickActions(onChat: () -> Unit) { Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) { ActionItem("CHAT", onChat); ActionItem("SCHEDULE") {}; ActionItem("ATTENDANCE") {} } }
@Composable fun ActionItem(title: String, onClick: () -> Unit) { Box(Modifier.size(105.dp, 58.dp).border(1.dp, Color(0xFF1E2A3D), RoundedCornerShape(14.dp)).clickable(onClick = onClick), Alignment.Center) { Text(title, Color(0xFF65758B), 9.sp, letterSpacing = 1.5.sp) } }
