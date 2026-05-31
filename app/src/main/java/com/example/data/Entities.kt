package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val username: String = "ALPJA",
    val level: Int = 1,
    val xp: Int = 1269,
    val maxXp: Int = 1000,
    val gold: Int = 1200, // Start with some gold to buy items
    val apPoints: Int = 100, // AP points used in the shop as currency/Attributes
    val energy: Int = 100,
    val syncPercent: Float = 1.0f,
    val streak: Int = 2,
    val lives: Int = 3,
    val agi: Int = 0,
    val str: Int = 0,
    val endurance: Int = 0,
    val intl: Int = 0,
    val statPoints: Int = 5, // Unused stat points earned from leveling up
    val vesselStatus: String = "PRIME",
    val manaStored: Int = 0,
    val manaGoal: Int = 500,
    val globalSync: Boolean = true
) {
    val rank: String get() {
        return when {
            level >= 50 -> "SSS"
            level >= 40 -> "SS"
            level >= 30 -> "S"
            level >= 20 -> "A"
            level >= 15 -> "B"
            level >= 10 -> "C"
            level >= 5  -> "D"
            else        -> "E"
        }
    }

    val rankArabicTitle: String get() {
        return when (rank) {
            "SSS" -> "ملك الظلال المطلق"
            "SS" -> "الحاكم الأعلى"
            "S" -> "صياد القمة"
            "A" -> "مقتحم النخبة"
            "B" -> "مقاتل خضرم"
            "C" -> "صياد محترف"
            "D" -> "مستكشف مبتدئ"
            else -> "وعاء بشري هش"
        }
    }

    val rankEnglishTitle: String get() {
        return when (rank) {
            "SSS" -> "Shadow Monarch"
            "SS" -> "Supreme Overlord"
            "S" -> "Apex Hunter"
            "A" -> "Elite Raider"
            "B" -> "Veteran"
            "C" -> "Pro Hunter"
            "D" -> "Novice"
            else -> "Fragile Vessel"
        }
    }

    val rankColor: Long get() {
        return when (rank) {
            "SSS" -> 0xFFEF4444 // Glowing Dark Crimson
            "SS" -> 0xFF8B5CF6 // Void Deep Purple
            "S" -> 0xFF3B82F6 // Electric Cosmic Ice Blue
            "A" -> 0xFFF59E0B // Fiery Amber Orange
            "B" -> 0xFF10B981 // Jade Emerald Green
            "C" -> 0xFF06B6D4 // Ocean Cyan
            "D" -> 0xFF6B7280 // Durable Iron Gray
            else -> 0xFFB45309 // Fragile Wood Bronze
        }
    }
}

@Entity(tableName = "exercise_tasks")
data class ExerciseTask(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // "REPS", "KM", "SEC", "PAGES", "MIN"
    val target: Int,
    val progress: Int, // current completion e.g. 10
    val xpReward: Int = 200,
    val goldReward: Int = 150,
    val apReward: Int = 25,
    val isCompleted: Boolean = false,
    val iconType: String // e.g., "pushups", "lunges", "burpees", "pullups", "walking", "running", "shower", "reading", "meditation"
)

@Entity(tableName = "items")
data class Item(
    @PrimaryKey val id: String,
    val name: String,
    val arabicName: String,
    val category: String, // "AP", "GOLD", "MARKET", "FATE"
    val cost: Int,
    val usageType: String, // "RECOVERY", "SHIELD", "SWORD", "MAP", "FAKE", "PRESSURE"
    val description: String,
    val arabicDescription: String,
    val count: Int = 0,
    val isSynced: Boolean = false,
    val durationHours: Int = 0
) {
    val costType: String get() = if (category == "GOLD") "GOLD" else "AP"
}

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "shadow_soldiers")
data class ShadowSoldier(
    @PrimaryKey val id: String,
    val name: String,
    val arabicName: String,
    val rank: String,
    val rankArabic: String,
    val manaCost: Int, // Cost of Shadow Mana to extract/wake up
    val goldCost: Int, // Cost of Gold to upgrade/evolve
    val level: Int = 0, // 0 = Locked, 1 = Unlocked/Normal, 2 = Elite, 3 = Knight, 4 = Commander...
    val passiveStat: String, // "str", "agi", "endurance", "intl"
    val passiveAmount: Int, // base stat added
    val description: String,
    val arabicDescription: String
)

