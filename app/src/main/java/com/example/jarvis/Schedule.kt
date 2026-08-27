package com.example.jarvis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class ClassSlot(val start: String, val end: String, val subject: String)
internal val weeklySchedule = linkedMapOf(
    "Monday" to listOf(ClassSlot("10:00 AM","10:50 AM","Introduction to AI"),ClassSlot("10:50 AM","11:40 AM","Computer Fundamentals"),ClassSlot("11:40 AM","12:30 PM","Basic Electronics"),ClassSlot("12:30 PM","1:20 PM","Essence of Indian Constitution"),ClassSlot("2:20 PM","4:00 PM","Physics Lab")),
    "Tuesday" to listOf(ClassSlot("11:40 AM","12:30 PM","Physics"),ClassSlot("12:30 PM","1:20 PM","Basic Electronics"),ClassSlot("2:20 PM","3:10 PM","Essence of Indian Constitution")),
    "Wednesday" to listOf(ClassSlot("10:00 AM","10:50 AM","Physics"),ClassSlot("10:50 AM","11:40 AM","Mathematics"),ClassSlot("11:40 AM","12:30 PM","Basic Electronics"),ClassSlot("12:30 PM","1:20 PM","Introduction to AI"),ClassSlot("2:20 PM","3:10 PM","Essence of Indian Constitution"),ClassSlot("4:00 PM","5:00 PM","Universal Human Values")),
    "Thursday" to listOf(ClassSlot("3:10 PM","4:00 PM","Mathematics"),ClassSlot("4:00 PM","5:00 PM","Universal Human Values")),
    "Friday" to listOf(ClassSlot("10:00 AM","11:40 AM","PPS Lab"),ClassSlot("11:40 AM","1:20 PM","Basic Electronics Lab"),ClassSlot("2:20 PM","3:10 PM","Physics"),ClassSlot("3:10 PM","4:00 PM","Mathematics")),
    "Saturday" to listOf(ClassSlot("10:00 AM","10:50 AM","Computer Fundamentals"),ClassSlot("10:50 AM","11:40 AM","Computer Fundamentals"),ClassSlot("11:40 AM","12:30 PM","Introduction to AI")),
    "Sunday" to emptyList()
)

@Composable
fun ScheduleScreen(onClose: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color(0xFF080D17)).padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("WEEKLY SCHEDULE", color=Color.White, fontSize=19.sp)
            Text("CLOSE", color=Color(0xFF9FC4EE), fontSize=11.sp, modifier=Modifier.clickable{onClose()})
        }
        Spacer(Modifier.height(14.dp))
        Column(Modifier.fillMaxWidth()) {
            weeklySchedule.forEach { (day, slots) ->
                Text(day, color=Color(0xFF9FC4EE), fontSize=13.sp, modifier=Modifier.padding(top=8.dp,bottom=5.dp))
                if (slots.isEmpty()) Text("OFF", color=Color(0xFF596579), fontSize=12.sp)
                slots.forEach { slot ->
                    Box(Modifier.fillMaxWidth().padding(vertical=2.dp).background(Color(0xFF0E1725), RoundedCornerShape(10.dp)).border(1.dp,Color(0xFF1E2A3D),RoundedCornerShape(10.dp)).padding(9.dp)) {
                        Text("${slot.start} – ${slot.end}\n${slot.subject}", color=Color(0xFFB8C9DE), fontSize=12.sp)
                    }
                }
            }
        }
    }
}
