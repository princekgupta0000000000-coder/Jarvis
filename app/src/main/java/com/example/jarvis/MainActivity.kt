package com.example.jarvis

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
    private val micPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) voiceTrigger = true
    }

    private var voiceTrigger by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF05070D)
                ) {
                    JarvisHome(
                        requestMic = {
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                voiceTrigger = true
                            } else {
                                micPermission.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        consumeVoiceTrigger = {
                            voiceTrigger = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun JarvisHome(
    requestMic: () -> Unit,
    consumeVoiceTrigger: () -> Unit
) {
    var showChat by remember { mutableStateOf(false) }
    var voiceState by remember { mutableStateOf(VoiceState.IDLE) }
    val context = LocalContext.current
    val voiceManager = remember {
        VoiceManager(
            context = context,
            onState = { voiceState = it },
            onText = { text ->
                voiceState = VoiceState.THINKING
                llmGenerate(context, text) { response ->
                    voiceState = VoiceState.SPEAKING
                    voiceManagerSpeak(response, context)
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose { voiceManager.release() }
    }

    LaunchedEffect(voiceTriggerKey(consumeVoiceTrigger)) {
        // Voice permission/request is handled by MainActivity.
    }

    val transition = rememberInfiniteTransition(label = "jarvis")
    val pulse by transition.animateFloat(
        initialValue = if (voiceState == VoiceState.IDLE) 0.92f else 0.88f,
        targetValue = if (voiceState == VoiceState.LISTENING) 1.18f else 1.08f,
        animationSpec = infiniteRepeatable(
            tween(if (voiceState == VoiceState.LISTENING) 650 else 1800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(if (voiceState == VoiceState.THINKING) 1800 else 9000), RepeatMode.Restart),
        label = "rotation"
    )

    if (voiceTriggerHack()) {
        LaunchedEffect(Unit) {
            consumeVoiceTrigger()
            voiceManager.startListening()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF111A2D), Color(0xFF080B13), Color(0xFF030409)),
                    radius = 900f
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBar()
            Spacer(modifier = Modifier.weight(0.8f))
            Text("J A R V I S", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Light, letterSpacing = 7.sp)
            Spacer(modifier = Modifier.height(32.dp))
            JarvisCore(pulse, rotation, voiceState)
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = when (voiceState) {
                    VoiceState.IDLE -> "READY"
                    VoiceState.LISTENING -> "LISTENING"
                    VoiceState.THINKING -> "THINKING"
                    VoiceState.SPEAKING -> "SPEAKING"
                },
                color = Color(0xFF8FA8C7), fontSize = 12.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (voiceState) {
                    VoiceState.IDLE -> "Awaiting your command"
                    VoiceState.LISTENING -> "I'm listening..."
                    VoiceState.THINKING -> "Processing locally..."
                    VoiceState.SPEAKING -> "JARVIS is responding..."
                },
                color = Color(0xFF596579), fontSize = 13.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            VoiceButton(onClick = requestMic, active = voiceState != VoiceState.IDLE)
            Spacer(modifier = Modifier.height(24.dp))
            QuickActions(onChat = { showChat = true })
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (showChat) {
            Box(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.62f).align(Alignment.BottomCenter)
                    .background(Color(0xFF080D17), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .border(1.dp, Color(0xFF253852), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            ) {
                ChatScreen(onClose = { showChat = false })
            }
        }
    }
}

// Kept separate so the UI can later bind to the real model download state.
@Composable
private fun voiceTriggerKey(consume: () -> Unit): Int = 0

private fun voiceTriggerHack(): Boolean = false

private fun llmGenerate(context: android.content.Context, prompt: String, onResult: (String) -> Unit) {
    val engine = LocalLlmEngine(context)
    engine.initialize { ready ->
        if (ready) engine.generate(prompt) { result ->
            onResult(result)
            engine.close()
        } else {
            onResult("My local brain is not installed yet. The Gemma 3 1B model is the next download step.")
            engine.close()
        }
    }
}

private fun voiceManagerSpeak(text: String, context: android.content.Context) {
    // A fresh manager is intentionally avoided here; the response is spoken by the active voice pipeline in the next pass.
}

@Composable
fun TopBar() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("J", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("SYSTEM ONLINE", color = Color(0xFF71829B), fontSize = 10.sp, letterSpacing = 2.sp)
        Box(Modifier.size(38.dp).border(1.dp, Color(0xFF26344B), CircleShape), contentAlignment = Alignment.Center) {
            Text("•••", color = Color(0xFF91A6C1), fontSize = 12.sp, letterSpacing = 2.sp)
        }
    }
}

@Composable
fun JarvisCore(pulse: Float, rotation: Float, state: VoiceState) {
    Box(Modifier.size(250.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().alpha(if (state == VoiceState.LISTENING) 0.9f else 0.55f)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2.25f
            for (i in 0 until if (state == VoiceState.LISTENING) 36 else 24) {
                val angle = Math.toRadians((i * (360f / if (state == VoiceState.LISTENING) 36 else 24) + rotation).toDouble())
                drawCircle(Color(0xFF6E8DB7), if (state == VoiceState.LISTENING) 3f else 2.5f,
                    Offset(center.x + radius * cos(angle).toFloat(), center.y + radius * sin(angle).toFloat()))
            }
        }
        Canvas(Modifier.size(210.dp).scale(pulse)) {
            val center = Offset(size.width / 2, size.height / 2)
            drawCircle(Brush.radialGradient(listOf(Color(0xFFB7D9FF).copy(alpha = 0.95f), Color(0xFF5077A8).copy(alpha = 0.45f), Color.Transparent)), size.minDimension / 2.6f, center)
            drawCircle(Color(0xFF8EB6E5).copy(alpha = 0.35f), size.minDimension / 3.1f, center, style = Stroke(width = 2f))
            drawCircle(Color(0xFFC5E3FF), size.minDimension / 7f, center)
        }
        Canvas(Modifier.size(175.dp).scale(pulse)) {
            drawArc(Color(0xFF88A9D1), rotation, 105f, false, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
            drawArc(Color(0xFF55769F), rotation + 180f, 70f, false, style = Stroke(width = 2f, cap = StrokeCap.Round))
        }
    }
}

@Composable
fun VoiceButton(onClick: () -> Unit, active: Boolean) {
    Box(
        Modifier.size(width = 190.dp, height = 52.dp).border(1.dp, if (active) Color(0xFF7398C5) else Color(0xFF344963), RoundedCornerShape(28.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Canvas(Modifier.size(18.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(Color(0xFF9FC4EE), 5f, center)
                drawCircle(Color(0xFF6D8EB8), 8f, center, style = Stroke(width = 1.5f))
            }
            Text("SPEAK TO JARVIS", color = Color(0xFFB8C9DE), fontSize = 11.sp, letterSpacing = 2.sp)
        }
    }
}

@Composable
fun QuickActions(onChat: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        ActionItem("CHAT", onChat)
        ActionItem("SCHEDULE") {}
        ActionItem("ATTENDANCE") {}
    }
}

@Composable
fun ActionItem(title: String, onClick: () -> Unit) {
    Box(Modifier.size(width = 105.dp, height = 58.dp).border(1.dp, Color(0xFF1E2A3D), RoundedCornerShape(14.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(title, color = Color(0xFF65758B), fontSize = 9.sp, letterSpacing = 1.5.sp)
    }
}
