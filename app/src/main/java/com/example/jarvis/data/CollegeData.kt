
package com.example.jarvis.data

import java.time.LocalDate

data class ClassSlot(
    val start: String,
    val end: String,
    val subject: String
)

data class Holiday(
    val from: LocalDate,
    val to: LocalDate,
    val name: String
)

object CollegeData {

    val weeklySchedule = mapOf(

        "MONDAY" to listOf(
            ClassSlot("10:00","10:50","Introduction to AI"),
            ClassSlot("10:50","11:40","Computer Fundamentals"),
            ClassSlot("11:40","12:30","Basic Electronics"),
            ClassSlot("12:30","01:20","Indian Constitution"),
            ClassSlot("02:20","04:00","Physics Lab")
        ),

        "TUESDAY" to listOf(
            ClassSlot("11:40","12:30","Physics"),
            ClassSlot("12:30","01:20","Basic Electronics"),
            ClassSlot("02:20","03:10","Indian Constitution")
        ),

        "WEDNESDAY" to listOf(
            ClassSlot("10:00","10:50","Physics"),
            ClassSlot("10:50","11:40","Mathematics"),
            ClassSlot("11:40","12:30","Basic Electronics"),
            ClassSlot("12:30","01:20","Introduction to AI"),
            ClassSlot("02:20","03:10","Indian Constitution"),
            ClassSlot("04:00","05:00","Universal Human Values")
        ),

        "THURSDAY" to listOf(
            ClassSlot("03:10","04:00","Mathematics"),
            ClassSlot("04:00","05:00","Universal Human Values")
        ),

        "FRIDAY" to listOf(
            ClassSlot("10:00","11:40","PPS Lab"),
            ClassSlot("11:40","01:20","Basic Electronics Lab"),
            ClassSlot("02:20","03:10","Physics"),
            ClassSlot("03:10","04:00","Mathematics")
        ),

        "SATURDAY" to listOf(
            ClassSlot("10:00","10:50","Computer Fundamentals"),
            ClassSlot("10:50","11:40","Computer Fundamentals"),
            ClassSlot("11:40","12:30","Introduction to AI")
        ),

        "SUNDAY" to emptyList()

    )

    val holidays = listOf(

        Holiday(LocalDate.of(2026,1,1),LocalDate.of(2026,1,1),"New Year"),
        Holiday(LocalDate.of(2026,1,14),LocalDate.of(2026,1,14),"Makar Sankranti"),
        Holiday(LocalDate.of(2026,1,23),LocalDate.of(2026,1,23),"Saraswati Puja"),

        Holiday(LocalDate.of(2026,3,2),LocalDate.of(2026,3,4),"Holi"),
        Holiday(LocalDate.of(2026,3,21),LocalDate.of(2026,3,21),"Eid"),
        Holiday(LocalDate.of(2026,3,22),LocalDate.of(2026,3,22),"Bihar Diwas"),
        Holiday(LocalDate.of(2026,3,26),LocalDate.of(2026,3,26),"Ashok Jayanti"),
        Holiday(LocalDate.of(2026,3,27),LocalDate.of(2026,3,27),"Ram Navami"),
        Holiday(LocalDate.of(2026,3,31),LocalDate.of(2026,3,31),"Mahavir Jayanti"),

        Holiday(LocalDate.of(2026,4,3),LocalDate.of(2026,4,3),"Good Friday"),
        Holiday(LocalDate.of(2026,4,14),LocalDate.of(2026,4,14),"Ambedkar Jayanti"),
        Holiday(LocalDate.of(2026,4,23),LocalDate.of(2026,4,23),"Veer Kunwar Singh Jayanti"),
        Holiday(LocalDate.of(2026,4,25),LocalDate.of(2026,4,25),"Janki Navami"),

        Holiday(LocalDate.of(2026,5,1),LocalDate.of(2026,5,1),"Labour Day"),
        Holiday(LocalDate.of(2026,5,28),LocalDate.of(2026,5,28),"Bakrid"),

        Holiday(LocalDate.of(2026,6,1),LocalDate.of(2026,6,30),"Summer Vacation"),

        Holiday(LocalDate.of(2026,8,4),LocalDate.of(2026,8,4),"Chehlum"),
        Holiday(LocalDate.of(2026,8,26),LocalDate.of(2026,8,26),"Hazrat Mohammad Birthday"),
        Holiday(LocalDate.of(2026,8,28),LocalDate.of(2026,8,28),"Raksha Bandhan"),

        Holiday(LocalDate.of(2026,9,4),LocalDate.of(2026,9,4),"Janmashtami"),

        Holiday(LocalDate.of(2026,10,2),LocalDate.of(2026,10,2),"Gandhi Jayanti"),
        Holiday(LocalDate.of(2026,10,17),LocalDate.of(2026,10,20),"Durga Puja"),

        Holiday(LocalDate.of(2026,11,8),LocalDate.of(2026,11,16),"Diwali & Chhath"),
        Holiday(LocalDate.of(2026,11,24),LocalDate.of(2026,11,24),"Guru Nanak Jayanti"),

        Holiday(LocalDate.of(2026,12,25),LocalDate.of(2026,12,31),"Winter Vacation")

    )
}
