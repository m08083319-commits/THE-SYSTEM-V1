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
    val gold: Int = 1000, // Start with some gold to buy items
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
    val globalSync: Boolean = true,
    val abyssShards: Int = 300, // Start with some Abyss Shards for tasting the Void
    val hasEmergencyShield: Boolean = false,
    val hasDoubleXP: Boolean = false,
    val doubleXPExpiry: Long = 0L,
    val hasDoubleGold: Boolean = false,
    val doubleGoldExpiry: Long = 0L,
    val activeTitle: String = "وعاء بشري هش",
    val activeTitleId: String = "title_fragile",
    val unlockedTitles: String = "title_fragile",
    val customTheme: String = "DEFAULT",
    val physicalMissions: Int = 0,
    val mentalMissions: Int = 0,
    val defeatedGates: Int = 0,
    // Fate effects
    val lastFateDrawnDate: String = "",
    val currentFateCardId: String? = null,
    val xpMultiplier: Float = 1.0f,
    val apMultiplier: Float = 1.0f,
    val goldMultiplier: Float = 1.0f,
    val statMultiplier: Float = 1.0f,
    val missionDifficultyMultiplier: Float = 1.0f,
    val extraMissions: Int = 0,
    val hasFreePunishmentSkip: Boolean = false,
    val forcedGateBefore: Long? = null,
    val deadlineReductionHours: Int = 0,
    // Dawn Mission fields
    val lastDawnMissionDate: String = "",
    val totalDawnMissionsCompleted: Int = 0,
    // Lives, Streak, and Debt Systems
    val maxLives: Int = 3,
    val bestStreak: Int = 0,
    val consecutiveCompleteDays: Int = 0,
    val originalDebt: Int = 0,
    val remainingDebt: Int = 0,
    val debtDateStr: String = "",
    val lastPaymentDateStr: String = "",
    val hasStreakBrokenToday: Boolean = false,
    val isSystemOverridePending: Boolean = false
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
    val category: String, // "AP", "GOLD", "MARKET", "ABYSS"
    val cost: Int,
    val usageType: String, // "RECOVERY", "SHIELD", "SWORD", "MAP", "FAKE", "PRESSURE", "COOLDOWN", etc.
    val description: String,
    val arabicDescription: String,
    val count: Int = 0,
    val isSynced: Boolean = false,
    val durationHours: Int = 0,
    val requiredRank: String? = null, // Minimum required rank (E, D, C, B, A, S, SSS)
    val cooldownHours: Int = 0, // In hours before buy again
    val stock: Int = -1, // -1 = Unlimited
    val lastPurchaseTimestamp: Long = 0L // To track cooldowns per item
) {
    val costType: String get() = when (category) {
        "GOLD" -> "GOLD"
        "ABYSS" -> "SHARDS"
        else -> "AP"
    }
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
    val level: Int = 0, // 0 = Locked, 1 = Unlocked/Normal, 2 = Elite, 3 = Knight/Commander...
    val passiveStat: String, // "str", "agi", "endurance", "intl"
    val passiveAmount: Int, // base stat added
    val description: String,
    val arabicDescription: String,
    val loyalty: Int = 20, // 0-100
    val isActive: Boolean = false,
    val isSacrificed: Boolean = false,
    val sacrificeReturnTime: Long = 0L,
    val isMerged: Boolean = false,
    val originalMergedIds: String? = null
)

@Entity(tableName = "gates")
data class Gate(
    @PrimaryKey val id: String,
    val name: String, // اسم البوابة بالعربية
    val rank: String, // رتبة البوابة (E, D, C, B, A, S)
    val guardianName: String, // اسم الحارس بالعربية
    val guardianDialogue: String, // حوار الحارس قبل القتال
    val challengeDescription: String, // وصف التحدي
    val challengeReps: Int, // عدد التكرارات
    val challengeUnit: String, // وحدة القياس (عدة، كم، دقيقة، صفحة)
    val exerciseType: String, // نوع التمرين (pushups, running, reading, meditation...)
    val baseAPReward: Int,
    val baseGoldReward: Int,
    val baseXPReward: Int,
    val weaknessName: String?, // اسم نقطة الضعف (اختياري)
    val weaknessExercise: String?, // تمرين نقطة الضعف
    val weaknessReps: Int?, // تكرار نقطة الضعف
    val timeLimitMinutes: Int, // الوقت المسموح (بالدقائق)
    val appearanceTime: Long, // وقت الظهور (بالملي ثانية)
    val expiryTime: Long, // وقت الاختفاء
    val isCompleted: Boolean = false,
    val isEntered: Boolean = false
)

@Entity(tableName = "side_quests")
data class SideQuest(
    @PrimaryKey val id: String,
    val name: String, // بالعربية
    val description: String,
    val targetReps: Int,
    val unit: String,
    val exerciseType: String,
    val apReward: Int,
    val goldReward: Int,
    val xpReward: Int,
    val isCompleted: Boolean = false
)

@Entity(tableName = "daily_journal")
data class DailyJournalEntry(
    @PrimaryKey val id: String, // yyyy-MM-dd
    val dateMillis: Long,
    val missionsCompleted: Int = 0,
    val totalMissions: Int = 0,
    val gatesEntered: Int = 0,
    val gatesCompleted: Int = 0,
    val guardsDefeated: String = "", 
    val sideQuestsCompleted: Int = 0,
    val fateCardDrawn: String = "",
    val fateCardType: String = "",
    val xpEarned: Int = 0,
    val apEarned: Int = 0,
    val goldEarned: Int = 0,
    val dawnMissionCompleted: Boolean = false,
    val wasPunished: Boolean = false,
    val punishmentDurationMinutes: Int = 0,
    val mood: String = "orange"
) {
    fun calculateMood(): String {
        return when {
            wasPunished -> "red"
            missionsCompleted < (totalMissions * 0.5).toInt() -> "orange"
            gatesCompleted == 0 && missionsCompleted == totalMissions && totalMissions > 0 -> "yellow"
            missionsCompleted >= totalMissions && gatesCompleted >= 1 -> "green"
            missionsCompleted >= (totalMissions * 0.5).toInt() -> "yellow"
            else -> "orange"
        }
    }
}

