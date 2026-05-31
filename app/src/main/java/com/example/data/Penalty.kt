package com.example.data

import java.io.Serializable

data class Penalty(
    val id: String = "lockdown",
    val missedMissions: Int = 1,
    val baseBurpees: Int = 10,
    val baseRunningKm: Double = 1.0,
    val basePushups: Int = 10,
    val basePullups: Int = 5,
    val baseSquats: Int = 15,
    val startTime: Long = System.currentTimeMillis(),
    var isCompleted: Boolean = false,
    var cheatAttempts: Int = 0,
    
    // Exercise progress
    var burpeesProgress: Int = 0,
    var pushupsProgress: Int = 0,
    var squatsProgress: Int = 0,
    var pullupsProgress: Int = 0,
    var runningProgressKm: Double = 0.0,
    
    // Simulated time shift for easy testing & demoing of the escalation logic
    var simulatedTimeOffsetMs: Long = 0L
) : Serializable {

    // Calculate elapsed time taking simulation offset into account
    fun getElapsedTimeMs(): Long {
        return (System.currentTimeMillis() - startTime + simulatedTimeOffsetMs).coerceAtLeast(0)
    }

    // Double timeMultiplier calculations based on elapsed hours:
    // 0-30 min: x1.0
    // 30-60 min: x1.5
    // 1-3 hours: x2.0
    // 3-6 hours: x3.0
    // 6-12 hours: x5.0
    // 12-24 hours: x10.0
    // >24 hours: demote rank + x10.0
    fun getTimeMultiplier(): Double {
        val diffMs = getElapsedTimeMs()
        val diffMin = diffMs / 1000.0 / 60.0
        val diffHours = diffMin / 60.0

        return when {
            diffHours >= 24.0 -> 10.0
            diffHours >= 12.0 -> 10.0
            diffHours >= 6.0 -> 5.0
            diffHours >= 3.0 -> 3.0
            diffHours >= 1.0 -> 2.0
            diffMin >= 30.0 -> 1.5
            else -> 1.0
        }
    }

    // E=1.0, D=1.2, C=1.5, B=2.0, A=3.0, S=4.0, SS=6.0, SSS=10.0
    fun getRankMultiplier(rank: String): Double {
        return when (rank.uppercase().trim()) {
            "SSS" -> 10.0
            "SS" -> 6.0
            "S" -> 4.0
            "A" -> 3.0
            "B" -> 2.0
            "C" -> 1.5
            "D" -> 1.2
            else -> 1.0 // E rank
        }
    }

    // Dynamic targets based on time and rank multipliers
    fun getTotalBurpees(rank: String, streakDouble: Boolean = false): Int {
        val mult = getTimeMultiplier() * getRankMultiplier(rank) * (if (streakDouble) 2.0 else 1.0)
        return (baseBurpees * mult).roundToInt()
    }

    fun getTotalPushups(rank: String, streakDouble: Boolean = false): Int {
        val mult = getTimeMultiplier() * getRankMultiplier(rank) * (if (streakDouble) 2.0 else 1.0)
        return (basePushups * mult).roundToInt()
    }

    fun getTotalSquats(rank: String, streakDouble: Boolean = false): Int {
        val mult = getTimeMultiplier() * getRankMultiplier(rank) * (if (streakDouble) 2.0 else 1.0)
        return (baseSquats * mult).roundToInt()
    }

    fun getTotalPullups(rank: String, streakDouble: Boolean = false): Int {
        val mult = getTimeMultiplier() * getRankMultiplier(rank) * (if (streakDouble) 2.0 else 1.0)
        return (basePullups * mult).roundToInt()
    }

    fun getTotalRunningKm(rank: String, streakDouble: Boolean = false): Double {
        val mult = getTimeMultiplier() * getRankMultiplier(rank) * (if (streakDouble) 2.0 else 1.0)
        return baseRunningKm * mult
    }

    private fun Double.roundToInt(): Int = kotlin.math.round(this).toInt()
}
