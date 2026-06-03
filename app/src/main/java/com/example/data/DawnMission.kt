package com.example.data

data class DawnMission(
    val description: String,
    val exerciseType: String,
    val targetReps: Int,
    val timeLimitMinutes: Int = 120, // Generally 120 (4 to 6), dynamically calculated for view
    val baseXPReward: Int = 200,
    val baseAPReward: Int = 100,
    val xpMultiplier: Float,
    val apMultiplier: Float,
    var isCompleted: Boolean = false,
    var isMeditationDone: Boolean = false
)
