package com.example.jarvis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF05070D)
                ) {
                    ChatScreen()
                }
            }
        }
    }
}

@Composable
fun JarvisHome() {
    val transition = rememberInfiniteTransition(label = "jarvis")

    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(9000),
            RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF111A2D),
                        Color(0xFF080B13),
                        Color(0xFF030409)
                    ),
                    radius = 900f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBar()
            Spacer(modifier = Modifier.weight(0.8f))

            Text(
                text = "J A R V I S",
                color = Color.White,
                fontSize = 27.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 7.sp
            )

            Spacer(modifier = Modifier.height(32.dp))
            JarvisCore(pulse, rotation)
            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "READY",
                color = Color(0xFF8FA8C7),
                fontSize = 12.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Awaiting your command",
                color = Color(0xFF596579),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.weight(1f))
            VoiceButton {}
            Spacer(modifier = Modifier.height(24.dp))
            QuickActions()
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("J", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "SYSTEM ONLINE",
            color = Color(0xFF71829B),
            fontSize = 10.sp,
            letterSpacing = 2.sp
        )
        Box(
            modifier = Modifier
                .size(38.dp)
                .border(1.dp, Color(0xFF26344B), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("•••", color = Color(0xFF91A6C1), fontSize = 12.sp, letterSpacing = 2.sp)
        }
    }
}

@Composable
fun JarvisCore(pulse: Float, rotation: Float) {
    Box(
        modifier = Modifier.size(250.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.55f)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2.25f

            for (i in 0 until 24) {
                val angle = Math.toRadians((i * 15f + rotation).toDouble())
                val x = center.x + radius * cos(angle).toFloat()
                val y = center.y + radius * sin(angle).toFloat()
                drawCircle(
                    color = Color(0xFF6E8DB7),
                    radius = 2.5f,
                    center = Offset(x, y)
                )
            }
        }

        Canvas(
            modifier = Modifier.size(210.dp).scale(pulse)
        ) {
            val center = Offset(size.width / 2, size.height / 2)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFB7D9FF).copy(alpha = 0.95f),
                        Color(0xFF5077A8).copy(alpha = 0.45f),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension / 2.6f,
                center = center
            )

            drawCircle(
                color = Color(0xFF8EB6E5).copy(alpha = 0.35f),
                radius = size.minDimension / 3.1f,
                center = center,
                style = Stroke(width = 2f)
            )

            drawCircle(
                color = Color(0xFFC5E3FF),
                radius = size.minDimension / 7f,
                center = center
            )
        }

        Canvas(modifier = Modifier.size(175.dp).scale(pulse)) {
            drawArc(
                color = Color(0xFF88A9D1),
                startAngle = rotation,
                sweepAngle = 105f,
                useCenter = false,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFF55769F),
                startAngle = rotation + 180f,
                sweepAngle = 70f,
                useCenter = false,
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun VoiceButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 190.dp, height = 52.dp)
            .border(1.dp, Color(0xFF344963), RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Canvas(modifier = Modifier.size(18.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(Color(0xFF9FC4EE), 5f, center)
                drawCircle(Color(0xFF6D8EB8), 8f, center, style = Stroke(width = 1.5f))
            }
            Text(
                "SPEAK TO JARVIS",
                color = Color(0xFFB8C9DE),
                fontSize = 11.sp,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun QuickActions() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionItem("SCHEDULE")
        ActionItem("ATTENDANCE")
        ActionItem("VISION")
    }
}

@Composable
fun ActionItem(title: String) {
    Box(
        modifier = Modifier
            .size(width = 105.dp, height = 58.dp)
            .border(1.dp, Color(0xFF1E2A3D), RoundedCornerShape(14.dp))
            .clickable {},
        contentAlignment = Alignment.Center
    ) {
        Text(
            title,
            color = Color(0xFF65758B),
            fontSize = 9.sp,
            letterSpacing = 1.5.sp
        )
    }
}
