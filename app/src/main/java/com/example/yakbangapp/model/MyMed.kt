package com.example.yakbangapp.model

data class MyMed(
    val id: Long = System.currentTimeMillis(),
    val itemSeq: String? = null,
    val name: String,
    val doseText: String,       // "500mg · 1정"
    val scheduleText: String,   // "아침/저녁 · 식후 30분"
    val takenToday: Boolean = false
)
