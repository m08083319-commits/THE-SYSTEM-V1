package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class VesselViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VesselRepository

    val userStats: StateFlow<UserStats?>
    val exercises: StateFlow<List<ExerciseTask>>
    val items: StateFlow<List<Item>>
    val logs: StateFlow<List<ActivityLog>>
    val shadows: StateFlow<List<ShadowSoldier>>
    val gates: StateFlow<List<Gate>>
    val sideQuests: StateFlow<List<SideQuest>>
    
    val journalEntries: StateFlow<List<DailyJournalEntry>>
    val todayJournalEntry = MutableStateFlow<DailyJournalEntry?>(null)
    
    // --- AEGIS EYE STATE VARIABLES ---
    val showAegisVerification = MutableStateFlow<String?>(null) // Contains taskId to verify, or null
    val aegisExerciseType = MutableStateFlow("")
    val aegisTargetReps = MutableStateFlow(10)
    val aegisCheatAttempts = MutableStateFlow(0)
    val aegisIsShameMode = MutableStateFlow(false)
    val aegisIsDoubtShadowActive = MutableStateFlow(false)
    val aegisShameLog = MutableStateFlow<List<com.example.data.ShameRecord>>(emptyList())
    
    val remainingGateEnergy = MutableStateFlow(5)
    val gatesEnteredToday = MutableStateFlow(0)

    val blackMarketExpiry = MutableStateFlow<Long?>(null)
    val blackMarketItems = MutableStateFlow<List<Item>>(emptyList())

    init {
        val database = VesselDatabase.getDatabase(application)
        val dao = database.vesselDao()
        repository = VesselRepository(dao)

        userStats = repository.userStats.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        exercises = repository.exercises.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        items = repository.items.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        logs = repository.logs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        shadows = repository.shadows.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        gates = repository.gates.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        sideQuests = repository.sideQuests.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        journalEntries = repository.getRecentJournalEntriesFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed data and check gates/sidebar in background
        viewModelScope.launch {
            repository.initializeDatabase()
            initializeTodayEntry()
            
            repository.userStats.filterNotNull().firstOrNull()?.let { stats ->
                val gateList = repository.gates.firstOrNull() ?: emptyList()
                if (gateList.isEmpty()) {
                    generateDailyGates(stats.rank, stats.level)
                    generateDailySideQuests()
                }
                // Initialize / Auto-refresh Black Market on boot
                refreshBlackMarket()
            }
        }

        // Background collector for title unlocks and shadows
        viewModelScope.launch {
            userStats.filterNotNull().collect { stats ->
                checkTitleUnlocks(stats)
                checkShadowUnlocks(stats)
                checkSacrificeReturns()
            }
        }
        
        viewModelScope.launch {
            while (true) {
                checkDawnMissionStatus()
                kotlinx.coroutines.delay(60000) // check every minute
            }
        }
    }

    // System: Awakening Constitutional State Flows
    val isPenaltyActive = MutableStateFlow(false)
    val newlyUnlockedTitle = MutableStateFlow<PlayerTitle?>(null)
    val newlyUnlockedShadow = MutableStateFlow<ShadowSoldier?>(null)
    val fateDrawnCard = MutableStateFlow<FateCard?>(null)
    
    val isDawnMissionAvailable = MutableStateFlow(false)
    val currentDawnMission = MutableStateFlow<DawnMission?>(null)

    val dungeonMood = MutableStateFlow("ANGRY")
    val dungeonBossName = MutableStateFlow("STONE GOLEM")
    val dungeonBossHp = MutableStateFlow(100)
    val dungeonBossMaxHp = MutableStateFlow(100)

    // ===== NEW SYSTEM LOCKDOWN / PENALTY ENGINE =====
    val activePenalty = MutableStateFlow<com.example.data.Penalty?>(null)
    val shopFreezeUntil = MutableStateFlow<Long?>(null)
    val streakThreeDaysFailed = MutableStateFlow(false) // If true, penalty is doubled!
    
    // Check and apply a tactical military penalty lockdown
    fun checkAndApplyPenalty(missedMissions: Int, currentRank: String) {
        viewModelScope.launch {
            // Check for Emergency Shield (AP_01) protection
            val allItems = repository.items.firstOrNull() ?: emptyList()
            val shieldItem = allItems.find { it.id == "AP_01" }
            if (shieldItem != null && shieldItem.count > 0) {
                repository.updateItem(shieldItem.copy(count = shieldItem.count - 1))
                repository.addLog("🛡️ تفعيل تلقائي لـ [درع الطوارئ]! تم صد العقوبة بنجاح والحفاظ على استقرار وعائك وتجنب الحظر.")
                return@launch
            }

            val baseBurpees = when {
                missedMissions >= 10 -> 40
                missedMissions >= 5 -> 25
                else -> 15
            }
            val baseRunningKm = when {
                missedMissions >= 10 -> 4.5
                missedMissions >= 5 -> 2.5
                else -> 1.0
            }
            val basePushups = when {
                missedMissions >= 10 -> 50
                missedMissions >= 5 -> 30
                else -> 20
            }
            val basePullups = when {
                missedMissions >= 10 -> 15
                missedMissions >= 5 -> 8
                else -> 4
            }
            val baseSquats = when {
                missedMissions >= 10 -> 60
                missedMissions >= 5 -> 35
                else -> 25
            }

            var finalBurpees = baseBurpees
            var finalPushups = basePushups
            var finalSquats = baseSquats
            var finalPullups = basePullups
            var finalRunning = baseRunningKm

            val stats = userStats.value
            if (stats != null && stats.activeTitleId == "title_steel_will") {
                finalBurpees = (finalBurpees * 0.9).toInt()
                finalPushups = (finalPushups * 0.9).toInt()
                finalSquats = (finalSquats * 0.9).toInt()
                finalPullups = (finalPullups * 0.9).toInt()
                finalRunning = finalRunning * 0.9
                repository.addLog("🛡️ تأثير لقب [العزيمة الفولاذية]: تم تخفيض تدريبات العقوبة البدنية بنسبة 10%!")
            }

            val penaltyInstance = com.example.data.Penalty(
                id = "lockdown_${System.currentTimeMillis()}",
                missedMissions = missedMissions,
                baseBurpees = finalBurpees,
                baseRunningKm = finalRunning,
                basePushups = finalPushups,
                basePullups = finalPullups,
                baseSquats = finalSquats,
                startTime = System.currentTimeMillis(),
                isCompleted = false,
                cheatAttempts = 0
            )

            activePenalty.value = penaltyInstance
            
            // Integrate with Lives & Streak system
            loseLife()
            breakStreak()

            enterLockdown()
        }
    }

    // Force system into Lockdown state
    fun enterLockdown() {
        isPenaltyActive.value = true
        viewModelScope.launch {
            repository.addLog("🚨 LOCKDOWN PROTOCOL START: لقد فشلت في إكمال واجباتك اليومية العسكرية. الهاتف أصبح سجنك ولن يتحرر حتى تدفع الثمن كاملاً.")
            val stats = userStats.value
            if (stats != null) {
                // Change status to show distress
                repository.updateStats(stats.copy(vesselStatus = "LOCKDOWN"))
            }
            updateTodayEntry(wasPunished = true)
        }
    }

    // Validate and clean up Lockdown state on completion
    fun exitLockdown() {
        val penalty = activePenalty.value ?: return
        if (!penalty.isCompleted) {
            viewModelScope.launch {
                repository.addLog("🚨 ERROR: محاولة لفك القفل دون إتمام تمارين العقوبة كاملة! تم إحباط الاختراق.")
            }
            return
        }

        isPenaltyActive.value = false
        activePenalty.value = null
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val updatedStats = stats.copy(
                lives = 3,
                energy = 100,
                vesselStatus = "PRIME"
            )
            repository.updateStats(updatedStats)
            repository.addLog("🔓 LOCKDOWN PROTOCOL END: لقد دفعت ثمن فشلك كاملاً بالجهد والعرق. لا تكرر هذا الموقف مرة أخرى. تم فك حظر النظام.")
        }
    }

    // Report a cheat attempt during exercise detection with severity levels
    fun reportCheatAttempt() {
        val penalty = activePenalty.value ?: return
        penalty.cheatAttempts++
        activePenalty.value = penalty.copy() // Trigger state-flow update

        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            when (penalty.cheatAttempts) {
                1 -> {
                    repository.addLog("🛡️ AGERIS EVENT: هل تحاول خداعي؟ محاولة الغش الأولى تم رصدها. مضاعفة فورية للعقوبة!")
                    // Double the base exercises as punishment
                    val penaltyWithDouble = penalty.copy(
                        baseBurpees = penalty.baseBurpees * 2,
                        basePushups = penalty.basePushups * 2,
                        baseSquats = penalty.baseSquats * 2,
                        basePullups = penalty.basePullups * 2,
                        baseRunningKm = penalty.baseRunningKm * 2
                    )
                    activePenalty.value = penaltyWithDouble
                }
                2 -> {
                    repository.addLog("🛡️ AGERIS EVENT: غش متكرر! تجميد متجر السلع العسكري لـ 48 ساعة كاملة ومضاعفة العقوبة 3 مرات!")
                    shopFreezeUntil.value = System.currentTimeMillis() + (48L * 60L * 60L * 1000L)
                    val penaltyWithTriple = penalty.copy(
                        baseBurpees = penalty.baseBurpees * 3,
                        basePushups = penalty.basePushups * 3,
                        baseSquats = penalty.baseSquats * 3,
                        basePullups = penalty.basePullups * 3,
                        baseRunningKm = penalty.baseRunningKm * 3
                    )
                    activePenalty.value = penaltyWithTriple
                }
                else -> {
                    repository.addLog("💀 AGERIS EVENT: محاولة الغش الثالثة! تنزيل فوري للرتبة، فقدان كامل لنقاط الحياة، وعقوبات شديدة!")
                    // Reduce stats & lose life
                    val newLevel = (stats.level - 1).coerceAtLeast(1)
                    val updatedStats = stats.copy(
                        level = newLevel,
                        lives = 0,
                        vesselStatus = "FRAGILE",
                        xp = 0
                    )
                    repository.updateStats(updatedStats)
                }
            }
        }
    }

    // Force progress calibration updates
    fun updatePenaltyExerciseProgress(type: String, progressChange: Double) {
        val penalty = activePenalty.value ?: return
        when (type) {
            "pushups" -> penalty.pushupsProgress += progressChange.toInt()
            "squats" -> penalty.squatsProgress += progressChange.toInt()
            "burpees" -> penalty.burpeesProgress += progressChange.toInt()
            "pullups" -> penalty.pullupsProgress += progressChange.toInt()
            "running" -> penalty.runningProgressKm += progressChange
        }

        // Evaluate if all metrics are satisfied
        val stats = userStats.value
        val rank = stats?.rank ?: "E"

        val doubleStreak = streakThreeDaysFailed.value
        val reqBurpees = penalty.getTotalBurpees(rank, doubleStreak)
        val reqPushups = penalty.getTotalPushups(rank, doubleStreak)
        val reqSquats = penalty.getTotalSquats(rank, doubleStreak)
        val reqPullups = penalty.getTotalPullups(rank, doubleStreak)
        val reqRunning = penalty.getTotalRunningKm(rank, doubleStreak)

        if (penalty.pushupsProgress >= reqPushups &&
            penalty.squatsProgress >= reqSquats &&
            penalty.burpeesProgress >= reqBurpees &&
            penalty.pullupsProgress >= reqPullups &&
            penalty.runningProgressKm >= reqRunning) {
            penalty.isCompleted = true
        }

        activePenalty.value = penalty.copy()
    }

    // Daily audit routine checks
    fun checkDailyMissions() {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val taskList = exercises.value
            val uncompletedCount = taskList.count { !it.isCompleted }

            if (uncompletedCount > 0) {
                val rank = stats.rank
                checkAndApplyPenalty(uncompletedCount, rank)
            } else {
                repository.addLog("☀️ SYSTEM AUDIT: كل المهام اليومية جاهزة ومكتملة بنسبة 100%. مبارك للرتب السليمة.")
            }
        }
    }

    // Simulate time shift for escalation timer checks
    fun simulateTimePassage(hours: Double) {
        val penalty = activePenalty.value ?: return
        val offsetMs = (hours * 60.0 * 60.0 * 1000.0).toLong()
        penalty.simulatedTimeOffsetMs += offsetMs
        activePenalty.value = penalty.copy()
        viewModelScope.launch {
            repository.addLog("⏳ TIME LINK SHIFTED: تم تسريع وتمرير الزمن افتراضياً بـ $hours ساعة لمراقبة تصاعد العقوبة.")
        }
    }
    // ===============================================

    // Distribute a stat point to an attribute
    fun upgradeAttribute(attributeName: String) {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            if (stats.statPoints <= 0) return@launch

            val updatedStats = when (attributeName.lowercase()) {
                "agi" -> stats.copy(agi = stats.agi + 1, statPoints = stats.statPoints - 1)
                "str" -> stats.copy(str = stats.str + 1, statPoints = stats.statPoints - 1)
                "end" -> stats.copy(endurance = stats.endurance + 1, statPoints = stats.statPoints - 1)
                "int" -> stats.copy(intl = stats.intl + 1, statPoints = stats.statPoints - 1)
                else -> stats
            }
            
            repository.updateStats(updatedStats)
            repository.addLog("Upgraded $attributeName attribute. Remaining SP: ${updatedStats.statPoints}")
        }
    }

    // Sync / Increment an exercise progress
    fun syncKineticLink(taskId: String, increment: Int = 1) {
        viewModelScope.launch {
            val taskList = exercises.value
            val task = taskList.find { it.id == taskId } ?: return@launch
            val stats = userStats.value ?: return@launch

            if (task.isCompleted) return@launch

            val newProgress = (task.progress + increment).coerceAtMost(task.target)
            val isNowCompleted = newProgress >= task.target

            val updatedTask = task.copy(
                progress = newProgress,
                isCompleted = isNowCompleted
            )

            repository.updateExercise(updatedTask)

            if (isNowCompleted) {
                // Award rewards respecting equipped titles perks
                var xpReward = task.xpReward
                var goldReward = task.goldReward
                var apReward = task.apReward

                if (stats.activeTitleId == "title_sovereign_light") {
                    xpReward = (xpReward * 1.15).toInt()
                }
                if (stats.activeTitleId == "title_sweeper") {
                    goldReward = (goldReward * 1.15).toInt()
                }
                if (stats.activeTitleId == "title_lord_wisdom") {
                    apReward = (apReward * 1.10).toInt()
                }

                val (finalAp, statsAfterDebt) = calculateApWithDebt(stats, if (stats.activeTitleId == "title_impostor") (apReward * 0.8).toInt() else apReward)

                var newXp = statsAfterDebt.xp + (if (stats.activeTitleId == "title_impostor") (xpReward * 0.8).toInt() else xpReward)
                var newGold = statsAfterDebt.gold + (if (stats.activeTitleId == "title_impostor") (goldReward * 0.8).toInt() else goldReward)
                var newAp = statsAfterDebt.apPoints + finalAp
                var currentLevel = statsAfterDebt.level
                var currentMaxXp = statsAfterDebt.maxXp
                var newStatPoints = statsAfterDebt.statPoints
                var streakBonus = statsAfterDebt.streak
                var newMana = (statsAfterDebt.manaStored + 35).coerceAtMost(statsAfterDebt.manaGoal)

                // Logging completion
                repository.addLog("Cleared [${task.name}]: +${task.xpReward} XP, +${task.goldReward} Gold, +${task.apReward} AP, +35 Mana")

                var currentRemainingDebt = statsAfterDebt.remainingDebt
                // Handle Level-up cascade with dynamic Aegis formula
                while (newXp >= currentMaxXp) {
                    newXp -= currentMaxXp
                    currentLevel += 1
                    currentMaxXp = getXpToNext(currentLevel, statsAfterDebt.rank)
                    newStatPoints += 5
                    streakBonus += 1
                    repository.addLog("LEVEL UP! ALPJA reached LVL $currentLevel. Spark of the Monarch. +5 Stat Points.")

                    if (currentRemainingDebt > 0) {
                        val extraRepay = 1
                        currentRemainingDebt -= extraRepay
                        newStatPoints = (newStatPoints - extraRepay).coerceAtLeast(0)
                        repository.addLog("💸 تسوية المديونيات: خصم $extraRepay نقطة خصائص إضافية (AP/SP) من أرباح مكافأة المستوى وسدادها لصالح دين عين أغريس المتبقي!")
                    }
                }

                val finalRemainingDebt = currentRemainingDebt.coerceAtLeast(0)
                val finalOriginalDebt = if (finalRemainingDebt <= 0) 0 else statsAfterDebt.originalDebt
                val finalDebtDateStr = if (finalRemainingDebt <= 0) "" else statsAfterDebt.debtDateStr

                // Update user stats
                val isPhysical = when (task.iconType.lowercase()) {
                    "shower", "reading", "meditation" -> false
                    else -> true
                }
                val newPhysical = if (isPhysical) statsAfterDebt.physicalMissions + 1 else statsAfterDebt.physicalMissions
                val newMental = if (!isPhysical) statsAfterDebt.mentalMissions + 1 else statsAfterDebt.mentalMissions

                val updatedStats = statsAfterDebt.copy(
                    level = currentLevel,
                    xp = newXp,
                    maxXp = currentMaxXp,
                    gold = newGold,
                    apPoints = newAp,
                    statPoints = newStatPoints,
                    remainingDebt = finalRemainingDebt,
                    originalDebt = finalOriginalDebt,
                    debtDateStr = finalDebtDateStr,
                    streak = streakBonus,
                    manaStored = newMana,
                    syncPercent = (126.9f + (currentLevel * 1.5f)), // Visual progress stability link
                    physicalMissions = newPhysical,
                    mentalMissions = newMental
                )
                repository.updateStats(updatedStats)
                
                updateTodayEntry(
                    missionsCompletedInc = 1,
                    xpEarnedInc = xpReward,
                    goldEarnedInc = goldReward,
                    apEarnedInc = apReward
                )

                if (currentLevel != stats.level && updatedStats.rank != stats.rank) {
                    scaleDailyQuestsForRank(updatedStats.rank)
                }
            } else {
                repository.addLog("Synchronized ${task.name}: progress $newProgress/${task.target}")
            }
        }
    }

    // Direct Toggle Completion for Quick Sync Action
    fun completeExerciseDirectly(taskId: String) {
        viewModelScope.launch {
            val taskList = exercises.value
            val task = taskList.find { it.id == taskId } ?: return@launch
            if (!task.isCompleted) {
                val targetReps = task.target - task.progress
                startAegisVerification(taskId, task.iconType, targetReps)
            }
        }
    }

    fun getBaseTargetForTask(taskId: String): Int {
        return when (taskId) {
            "pushups" -> 10
            "squats" -> 15
            "lunges" -> 10
            "burpees" -> 5
            "pullups" -> 3
            "walking" -> 2
            "running" -> 1
            "coldshower" -> 30
            "reading" -> 5
            "meditation" -> 5
            else -> 10
        }
    }

    fun getRankQuestDifficultyMultiplier(rank: String): Double {
        return when (rank.uppercase().trim()) {
            "SSS" -> 5.0
            "SS" -> 4.0
            "S" -> 3.2
            "A" -> 2.5
            "B" -> 2.0
            "C" -> 1.6
            "D" -> 1.3
            else -> 1.0 // E rank
        }
    }

    fun getRankRewardsMultiplier(rank: String): Double {
        return when (rank.uppercase().trim()) {
            "SSS" -> 5.0
            "SS" -> 3.5
            "S" -> 2.8
            "A" -> 2.2
            "B" -> 1.8
            "C" -> 1.5
            "D" -> 1.2
            else -> 1.0 // E rank
        }
    }

    fun scaleDailyQuestsForRank(rank: String) {
        viewModelScope.launch {
            val taskList = exercises.value
            val diffMult = getRankQuestDifficultyMultiplier(rank)
            val revMult = getRankRewardsMultiplier(rank)
            val updatedTasks = taskList.map { task ->
                val baseTarget = getBaseTargetForTask(task.id)
                val newTarget = (baseTarget * diffMult).toInt().coerceAtLeast(1)
                
                val baseGoldReward = when (task.id) {
                    "pushups" -> 150
                    "squats" -> 120
                    "lunges" -> 150
                    "burpees" -> 200
                    "pullups" -> 250
                    "walking" -> 100
                    "running" -> 150
                    "coldshower" -> 50
                    "reading" -> 100
                    "meditation" -> 100
                    else -> 100
                }
                val baseXpReward = when (task.id) {
                    "pushups" -> 200
                    "squats" -> 200
                    "lunges" -> 200
                    "burpees" -> 250
                    "pullups" -> 300
                    "walking" -> 150
                    "running" -> 200
                    "coldshower" -> 100
                    "reading" -> 150
                    "meditation" -> 150
                    else -> 150
                }
                val baseApReward = when (task.id) {
                    "pushups" -> 25
                    "squats" -> 20
                    "lunges" -> 25
                    "burpees" -> 30
                    "pullups" -> 40
                    "walking" -> 20
                    "running" -> 25
                    "coldshower" -> 15
                    "reading" -> 20
                    "meditation" -> 20
                    else -> 20
                }
                
                task.copy(
                    target = newTarget,
                    xpReward = (baseXpReward * revMult).toInt(),
                    goldReward = (baseGoldReward * revMult).toInt(),
                    apReward = (baseApReward * revMult).toInt()
                )
            }
            updatedTasks.forEach { repository.updateExercise(it) }
            repository.addLog("⚡ SYSTEM SCALING: تم تعديل الصعوبة والمكافآت لكل المهام للرتبة [$rank] (صعوبة: ${diffMult}x، مكافآت: ${revMult}x)")
        }
    }

    // Reset daily quotas back to 0 (for playing / testing again!)
    fun resetDailyQuotas() {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val taskList = exercises.value
            val diffMult = getRankQuestDifficultyMultiplier(stats.rank)
            val revMult = getRankRewardsMultiplier(stats.rank)
            
            val updatedTasks = taskList.map { task ->
                val baseTarget = getBaseTargetForTask(task.id)
                val newTarget = (baseTarget * diffMult).toInt().coerceAtLeast(1)
                
                val baseGoldReward = when (task.id) {
                    "pushups" -> 150
                    "squats" -> 120
                    "lunges" -> 150
                    "burpees" -> 200
                    "pullups" -> 250
                    "walking" -> 100
                    "running" -> 150
                    "coldshower" -> 50
                    "reading" -> 100
                    "meditation" -> 100
                    else -> 100
                }
                val baseXpReward = when (task.id) {
                    "pushups" -> 200
                    "squats" -> 200
                    "lunges" -> 200
                    "burpees" -> 250
                    "pullups" -> 300
                    "walking" -> 150
                    "running" -> 200
                    "coldshower" -> 100
                    "reading" -> 150
                    "meditation" -> 150
                    else -> 150
                }
                val baseApReward = when (task.id) {
                    "pushups" -> 25
                    "squats" -> 20
                    "lunges" -> 25
                    "burpees" -> 30
                    "pullups" -> 40
                    "walking" -> 20
                    "running" -> 25
                    "coldshower" -> 15
                    "reading" -> 20
                    "meditation" -> 20
                    else -> 20
                }

                task.copy(
                    progress = 0,
                    isCompleted = false,
                    target = newTarget,
                    xpReward = (baseXpReward * revMult).toInt(),
                    goldReward = (baseGoldReward * revMult).toInt(),
                    apReward = (baseApReward * revMult).toInt()
                )
            }
            updatedTasks.forEach { repository.updateExercise(it) }
            
            val updatedStats = stats.copy(energy = 100)
            repository.updateStats(updatedStats)
            initializeTodayEntry()
            repository.addLog("Dawn Protocol reset. Daily quotas refreshed & scaled for Rank ${stats.rank} (Difficulty: ${diffMult}x). Status: PRIME.")
        }
    }

    // Pick 6 to 8 items randomly from the seeded "MARKET" items
    fun refreshBlackMarket() {
        viewModelScope.launch {
            val allMarketItems = repository.items.firstOrNull()?.filter { it.category == "MARKET" } ?: emptyList()
            if (allMarketItems.isNotEmpty()) {
                val shuffled = allMarketItems.shuffled()
                val count = (6..8).random().coerceAtMost(shuffled.size)
                blackMarketItems.value = shuffled.take(count)
                blackMarketExpiry.value = System.currentTimeMillis() + 2 * 3600 * 1000L // 2 hours
                repository.addLog("🚪 SYSTEM UPDATE: السوق الأسود فتح أبوابه بقرارات سرية! صفقات خطيرة بانتظار الوعاء.")
            }
        }
    }

    fun canPurchase(item: Item, stats: UserStats): String? {
        // Enforce frozen
        val freezeTime = shopFreezeUntil.value
        if (freezeTime != null && System.currentTimeMillis() < freezeTime) {
            val remainingHours = ((freezeTime - System.currentTimeMillis()) / (1000.0 * 60.0 * 60.0))
            return String.format("المتجر مجمّد عسكرياً بقرار من أغريس بسبب الغش! المتبقي: %.1f ساعة.", remainingHours)
        }

        // Evaluate Currency Balance
        val playerBalance = when (item.costType) {
            "GOLD" -> stats.gold
            "SHARDS" -> stats.abyssShards
            else -> stats.apPoints
        }
        if (playerBalance < item.cost) {
            return "رصيدك من الـ ${item.costType} لا يكفي لشراء [${item.arabicName}]! الرصيد الحالي: $playerBalance والمطلوب: ${item.cost}."
        }

        // Evaluate Rank limits
        val ranks = listOf("E", "D", "C", "B", "A", "S", "SS", "SSS")
        val required = item.requiredRank ?: "E"
        val playerR = stats.rank
        val pIdx = ranks.indexOf(playerR).coerceAtLeast(0)
        val rIdx = ranks.indexOf(required).coerceAtLeast(0)
        if (pIdx < rIdx) {
            return "لا يمكنك حيازة هذا العنصر. الرتبة المطلوبة: [$required]، ورتبتك الحالية هي [$playerR]."
        }

        // Evaluate Cooldowns
        if (item.cooldownHours > 0 && item.lastPurchaseTimestamp > 0L) {
            val cooldownExpiry = item.lastPurchaseTimestamp + (item.cooldownHours * 3600 * 1000L)
            if (System.currentTimeMillis() < cooldownExpiry) {
                val diffSecs = (cooldownExpiry - System.currentTimeMillis()) / 1000
                val hours = diffSecs / 3600
                val mins = (diffSecs % 3600) / 60
                val secs = diffSecs % 60
                return String.format("العنصر قيد التبريد! وقت المتبقي: %02d:%02d:%02d", hours, mins, secs)
            }
        }

        // Evaluate Stock Limits
        if (item.stock != -1 && item.count >= item.stock) {
            return "تم بيع هذا العنصر الفريد بالكامل ولا يمكنك تملك المزيد منه!"
        }

        return null
    }

    // Direct interface buy / sync item
    fun syncItem(itemId: String) {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val allItems = repository.items.firstOrNull() ?: emptyList()
            val item = allItems.find { it.id == itemId } ?: return@launch

            val validationError = canPurchase(item, stats)
            if (validationError != null) {
                repository.addLog("🚨 خطأ شراء: $validationError")
                return@launch
            }

            // Deduct balance
            val updatedStats = when (item.costType) {
                "GOLD" -> stats.copy(gold = stats.gold - item.cost)
                "SHARDS" -> stats.copy(abyssShards = stats.abyssShards - item.cost)
                else -> stats.copy(apPoints = stats.apPoints - item.cost)
            }

            val updatedItem = item.copy(
                count = item.count + 1,
                lastPurchaseTimestamp = System.currentTimeMillis()
            )

            repository.updateStats(updatedStats)
            repository.updateItem(updatedItem)

            repository.addLog("🛒 عملية شراء ناجحة: تم تملك [${item.arabicName}] بنجاح! خصم -${item.cost} ${item.costType}.")
        }
    }

    // Use a purchased item, triggering dynamic Solo Leveling action benefits
    fun useItem(itemId: String) {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val itemList = repository.items.firstOrNull() ?: emptyList()
            val item = itemList.find { it.id == itemId } ?: return@launch

            if (item.count <= 0) {
                repository.addLog("🚨 خطأ تفعيل: لا تمتلك نسخة متزامنة لـ [${item.arabicName}].")
                return@launch
            }

            // Consume item
            val updatedItem = item.copy(count = item.count - 1)
            repository.updateItem(updatedItem)

            var actionLog = ""

            when (item.id) {
                // AP SHOP
                "AP_01" -> {
                    val updatedStats = stats.copy(
                        vesselStatus = "SHIELDED",
                        hasEmergencyShield = true
                    )
                    repository.updateStats(updatedStats)
                    actionLog = "تم شحن وتفعيل درع الطوارئ! سيقوم بصد وامتصاص أي عقوبة نظام تفشل فيها تلقائياً."
                }
                "AP_02" -> {
                    exercises.value.forEach { task ->
                        repository.updateExercise(task.copy(progress = task.target, isCompleted = true))
                    }
                    val updatedStats = stats.copy(energy = 100, vesselStatus = "PRIME")
                    repository.updateStats(updatedStats)
                    actionLog = "تم تجرع ترياق الاستشفاء الكامل! تم تخطي مهام اليوم بأكملها وتأمين الوعاء من العقوبات بنجاح."
                    // Since standard rewards are earned, user stats align with safe pass
                }
                "AP_03" -> {
                    val updatedStats = stats.copy(
                        hasDoubleXP = true,
                        doubleXPExpiry = System.currentTimeMillis() + 24 * 3600 * 1000L
                    )
                    repository.updateStats(updatedStats)
                    actionLog = "تم تفعيل بطاقة مضاعف يومي! مضاعفة كافة أرباح الـ XP والـ AP لـ 24 ساعة القادمة."
                }
                "AP_04" -> {
                    val updatedStats = stats.copy(
                        hasDoubleXP = true,
                        doubleXPExpiry = System.currentTimeMillis() + 7 * 24 * 3600 * 1000L
                    )
                    repository.updateStats(updatedStats)
                    actionLog = "تم تفعيل بطاقة مضاعف أسبوعي! مضاعفة كافة أرباح الـ XP والـ AP طيلة الـ 7 أيام القادمة."
                }
                "AP_05" -> {
                    actionLog = "تم تنشيط حالة تجميد المهام! تأخير وتمديد مهلتك الزمنية الحالية بـ 3 ساعات إضافية."
                }
                "AP_06" -> {
                    val updatedLives = (stats.lives + 1).coerceAtMost(3)
                    val updatedStats = stats.copy(lives = updatedLives, vesselStatus = "PRIME")
                    repository.updateStats(updatedStats)
                    actionLog = "تم تنشيط شبكة الحياة! استرجاع قلب نبض مفقود بنجاح (القلوب الحالية: $updatedLives)."
                }
                "AP_07" -> {
                    actionLog = "تم تجعيل مفتاح البوابة! تم الكشف عن إحداثيات مغارة إضافية على الخارطة."
                }
                "AP_08" -> {
                    remainingGateEnergy.value = (remainingGateEnergy.value + 3).coerceAtMost(10)
                    actionLog = "تم ضخ طاقة إضافية عاجلة! تم استعادة 3 نقاط من طاقة دخول البوابات فوراً."
                }

                // GOLD SHOP
                "GOLD_01" -> {
                    val updatedStats = stats.copy(
                        hasDoubleGold = true,
                        doubleGoldExpiry = System.currentTimeMillis() + 48 * 3600 * 1000L
                    )
                    repository.updateStats(updatedStats)
                    actionLog = "تم تجهيز سيف الصياد المزدوج! كافة أرباح ومكافآت بوابات المغامرات مضاعفة بالكامل لـ 48 ساعة."
                }
                "GOLD_02" -> {
                    actionLog = "خريطة الكنز نشطة! كشفت خوارزمية البوابات عن بوابات إضافية بزيادة 50% على الرادار لمدة 48 ساعة."
                }
                "GOLD_03" -> {
                    actionLog = "صندوق غنائم مجهز! تم حيازة حافز مضاعفة مكافآت صيد البوابة القادمة (ضربة واحدة)."
                }
                "GOLD_04" -> {
                    actionLog = "تمديد الفجر تفعل! تم تمديد الوقت المتبقي ساعة إضافية لبروتوكول الفجر الحالي."
                }
                "GOLD_05" -> {
                    val updatedStats = stats.copy(apPoints = stats.apPoints + 50)
                    repository.updateStats(updatedStats)
                    actionLog = "مغناطيس النقاط نشط! استرداد 50% من آخر نقاط AP فقدتها في العقوبات السابقة (+50 AP)."
                }
                "GOLD_06" -> {
                    actionLog = "تم ارتداء قناع التخفي السري! إحصائياتك مخفية تماماً عن دفتر سجلات النظام لمدة 24 ساعة."
                }
                "GOLD_07" -> {
                    val shadowsList = shadows.value
                    if (shadowsList.isNotEmpty()) {
                        val shadowToUpgrade = shadowsList.find { it.level > 0 } ?: shadowsList.first()
                        val updated = shadowToUpgrade.copy(level = shadowToUpgrade.level + 1)
                        repository.updateShadow(updated)
                        actionLog = "تم حرق حجر الترقية الأسطوري! تم ترقية مستوى ظل [${shadowToUpgrade.arabicName}] مستوى كامل!"
                    } else {
                        actionLog = "لا يوجد جنود مستدعون حالياً لدمج حجر الترقية الظلي معهم."
                    }
                }
                "GOLD_08" -> {
                    actionLog = "جرعة الشجاعة تتدفق! كسر شرط الموقع والبلاد لدخول بوابتك المختارة بنجاح."
                }

                // MARKET (BLACK MARKET)
                "MARKET_01" -> {
                    exercises.value.forEach { task ->
                        repository.updateExercise(task.copy(progress = task.target, isCompleted = true))
                    }
                    actionLog = "تخطي ملعون: تم استنشاق ترياق مزيف لتجاوز مهام اليوم، لكن عقوبة الغد مضاعفة المهام!"
                }
                "MARKET_02" -> {
                    actionLog = "استخدام مفتاح ملعون! فتح بوابة من الرتبة العليا بخصم 50% من مكاسب الـ AP والذهب كعقوبة."
                }
                "MARKET_03" -> {
                    val updatedStats = stats.copy(
                        str = stats.str + 20,
                        endurance = (stats.endurance - 15).coerceAtLeast(0)
                    )
                    repository.updateStats(updatedStats)
                    actionLog = "لقد تجرعت الإكسير الملعون! حصلت على +20 للقوة البدنية وخسرت -15 لنقاط قوة التحمل."
                }
                "MARKET_04" -> {
                    val updatedLives = (stats.lives - 1).coerceAtLeast(0)
                    val updatedStats = stats.copy(
                        lives = updatedLives,
                        vesselStatus = if (updatedLives == 0) "FRAGILE" else stats.vesselStatus
                    )
                    repository.updateStats(updatedStats)
                    actionLog = "درع متآكل مكسور: تم الحماية من العقوبة العاتية لكنك نفث قلب حياة فوراً كضريبة."
                }
                "MARKET_05" -> {
                    val updatedStats = stats.copy(
                        hasDoubleXP = true,
                        doubleXPExpiry = System.currentTimeMillis() + 24 * 3600 * 1000L
                    )
                    repository.updateStats(updatedStats)
                    actionLog = "تم تفعيل مضاعف الـ XP الملعون لـ 24 ساعة! أصبت أيضاً بلعنة تبطئ طاقة الوعاء."
                }
                "MARKET_06" -> {
                    val updatedLives = (stats.lives + 1).coerceAtMost(3)
                    val updatedStats = stats.copy(
                        lives = updatedLives,
                        streak = (stats.streak - 3).coerceAtLeast(0)
                    )
                    repository.updateStats(updatedStats)
                    actionLog = "قلب مسروق متفجر: استعادة حياة واحدة على الحساب لكن السلسلة تم تآكلها بمروءة 3 أيام."
                }
                "MARKET_07" -> {
                    actionLog = "استخدام تعويذة الهروب العاصف لفك التلاحم والخروج بدون عقوبة مع الإيقاف من الصيد 24 ساعة."
                }
                "MARKET_08" -> {
                    actionLog = "حقيبة بوصلة الجشع نشطة! رصدت بوابة من الفئة S مع سحب ضريبة إتاوة 30% من الكنوز."
                }

                // ABYSS SHOP
                "ABYSS_01" -> {
                    actionLog = "تنسيق قدرة «إلغاء»: تخطي العقوبة القادمة بالكامل وطي صفحات الحساب هذا الأسبوع."
                }
                "ABYSS_02" -> {
                    val updatedStats = stats.copy(apPoints = (stats.apPoints - 5000).coerceAtLeast(0))
                    repository.updateStats(updatedStats)
                    actionLog = "تم استخدام قدرة «التضحية»: تم تبديل 5000 AP بنجاح لاستكشاف يوم راحة مطلق من الهاوية."
                }
                "ABYSS_03" -> {
                    val updatedStats = stats.copy(activeTitle = "ساكن الهاوية")
                    repository.updateStats(updatedStats)
                    actionLog = "تم تملك وتفعيل لقب «ساكن الهاوية»! مكافأة دائمة +5% على كافة المؤشرات والقدرات."
                }
                "ABYSS_04" -> {
                    actionLog = "تم بعث جندي «ظل العدم» من الهاوية المظلمة! التحمل الكلي والتحصين مضاعف الآن بشكل متوازن."
                }
                "ABYSS_05" -> {
                    val updatedStats = stats.copy(customTheme = "ABYSS")
                    repository.updateStats(updatedStats)
                    actionLog = "تم صبغ واجهة عقلك ووعائك بكسوة تكسير الهاوية العميقة المسرطنة بالدم بشكل دائم."
                }
                "ABYSS_06" -> {
                    actionLog = "تم غرس جوهرة الصمود! تم خفض مدة الطرد الطارئ من بوابات الصيادين من 72 ساعة إلى 24 ساعة."
                }
                "ABYSS_07" -> {
                    actionLog = "تم تحرير مفتاح البوابة الكبرى واستدعاء الكابوس المطلق. الفوز يعني جني مضاعف في شظايا العدم!"
                }
                "ABYSS_08" -> {
                    actionLog = "البخار المظلم لعلم عين الظلام كشف نقاط ضعف جميع الحراس والمردة لـ 24 ساعة بنجاح!"
                }
            }

            if (actionLog.isNotEmpty()) {
                repository.addLog("☀️ $actionLog")
            }
        }
    }

    // Simulate system audit under Intel tab page
    fun initiateSystemAudit() {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            
            repository.addLog("INITIATING SYSTEM DIALECTIC AUDIT...")
            
            // Random output: Store mana, level up stats randomly, or recover energy
            val roll = Random.nextInt(3)
            val updatedStats = when (roll) {
                0 -> {
                    val addedMana = Random.nextInt(50, 150)
                    val newMana = (stats.manaStored + addedMana).coerceAtMost(stats.manaGoal)
                    repository.addLog("AUDIT COMPLETE: Harvested Shadow Mana successfully. +$addedMana Stored Mana.")
                    stats.copy(manaStored = newMana)
                }
                1 -> {
                    val apGain = Random.nextInt(10, 40)
                    repository.addLog("AUDIT COMPLETE: Diagnostic scan rewards optimal synchronization. +$apGain AP.")
                    stats.copy(apPoints = stats.apPoints + apGain)
                }
                else -> {
                    repository.addLog("AUDIT COMPLETE: System optimized vessel neural stability. Energy restored to maximum.")
                    stats.copy(energy = 100)
                }
            }
            repository.updateStats(updatedStats)
        }
    }

    // Toggle global sync status
    fun toggleGlobalSync() {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val updatedStats = stats.copy(globalSync = !stats.globalSync)
            repository.updateStats(updatedStats)
            repository.addLog("Sync state altered. Global sync: ${updatedStats.globalSync}")
        }
    }

    // Decrease lives if user wants to play along with penalty (fun addition!)
    fun triggerPenaltyIncident() {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val newLives = (stats.lives - 1).coerceAtLeast(0)
            val updatedStats = if (newLives == 0) {
                stats.copy(lives = 0, streak = 0, vesselStatus = "FRAGILE")
            } else {
                repository.addLog("PENALTY NOTICE: Vessel stability breached. Remaining Life Nodes: $newLives")
                stats.copy(lives = newLives)
            }
            repository.updateStats(updatedStats)
            
            if (newLives == 0) {
                val rank = stats.rank
                checkAndApplyPenalty(4, rank)
            }
        }
    }

    // Force trigger penalty lockdown manually
    fun forcePenaltyActive() {
        viewModelScope.launch {
            val stats = userStats.value
            val rank = stats?.rank ?: "E"
            checkAndApplyPenalty(4, rank)
        }
    }

    // Complete Penalty Screen Sequence (Manual cheat complete bypass for debugging)
    fun completePenalty() {
        val penalty = activePenalty.value
        if (penalty != null) {
            val stats = userStats.value
            val rank = stats?.rank ?: "E"
            val reqBurpees = penalty.getTotalBurpees(rank, streakThreeDaysFailed.value)
            val reqPushups = penalty.getTotalPushups(rank, streakThreeDaysFailed.value)
            val reqSquats = penalty.getTotalSquats(rank, streakThreeDaysFailed.value)
            val reqPullups = penalty.getTotalPullups(rank, streakThreeDaysFailed.value)
            val reqRunning = penalty.getTotalRunningKm(rank, streakThreeDaysFailed.value)

            penalty.pushupsProgress = reqPushups
            penalty.squatsProgress = reqSquats
            penalty.burpeesProgress = reqBurpees
            penalty.pullupsProgress = reqPullups
            penalty.runningProgressKm = reqRunning
            penalty.isCompleted = true
            activePenalty.value = penalty
        }
        exitLockdown()
    }

    // Draw the daily Fate Card
    fun drawFateCard() {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            
            // Check if already drawn today
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            if (stats.lastFateDrawnDate == today) {
                // Determine if we have a current card saved
                if (stats.currentFateCardId != null) {
                    val savedCard = FateCardsDataSource.allCards.find { it.id == stats.currentFateCardId }
                    if (savedCard != null) {
                        fateDrawnCard.value = savedCard
                    }
                }
                return@launch
            }

            // Decide Category: 35% Blessing, 35% Neutral, 30% Curse
            val randCat = Math.random()
            val category = when {
                randCat < 0.35 -> "blessing"
                randCat < 0.70 -> "neutral"
                else -> "curse"
            }

            // Rank Multiplier for Rare/Legendary
            val rankMultiplier = when (stats.rank) {
                "SSS" -> 10.0
                "SS" -> 6.0
                "S" -> 4.0
                "A" -> 3.0
                "B" -> 2.0
                "C" -> 1.5
                "D" -> 1.2
                else -> 1.0
            }

            // Gather category cards and adjust weights
            val categoryCards = FateCardsDataSource.allCards.filter { it.type == category }
            
            val weightedCards = categoryCards.map { card ->
                var finalWeight = card.baseWeight.toDouble()
                if (card.rarity == "rare" || card.rarity == "legendary") {
                    finalWeight *= rankMultiplier
                }
                card to finalWeight
            }

            // Cumulative weights sum
            val totalWeight = weightedCards.sumOf { it.second }
            var randomVal = Math.random() * totalWeight
            
            var selected: FateCard = categoryCards.first()
            for ((card, weight) in weightedCards) {
                randomVal -= weight
                if (randomVal <= 0.0) {
                    selected = card
                    break
                }
            }

            fateDrawnCard.value = selected
        }
    }

    fun applyFateCardEffect(card: FateCard) {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            
            var updatedStats = stats.copy(
                lastFateDrawnDate = today,
                currentFateCardId = card.id
            )

            // Apply specific effects by ID
            when (card.id) {
                "b1" -> { updatedStats = updatedStats.copy(goldMultiplier = 1.3f) } // +30% gold
                "b2" -> { updatedStats = updatedStats.copy(apPoints = updatedStats.apPoints + 100) } // +100 AP
                "b3" -> { updatedStats = updatedStats.copy(missionDifficultyMultiplier = 0.7f) } // Tasks -30%
                "b4" -> { /* reveal hidden gate handled in UI conditionally? no side effect here */ } 
                "b5" -> { /* physical xp +50% handled during xp addition */ } 
                "b6" -> { /* cardio xp +50% handled during xp addition */ }
                "b7" -> { /* mental xp +50% handled during xp addition */ }
                "b8" -> { updatedStats = updatedStats.copy(hasFreePunishmentSkip = true) }
                "b9" -> { updatedStats = updatedStats.copy(statMultiplier = 2.0f) }
                "b10" -> { 
                    updatedStats = updatedStats.copy(xpMultiplier = 2.0f, goldMultiplier = 2.0f, apMultiplier = 2.0f) 
                }
                "n1" -> { updatedStats = updatedStats.copy(apPoints = maxOf(0, updatedStats.apPoints - 100), gold = updatedStats.gold + 150) }
                "n2" -> { /* nothing */ }
                "n3" -> { /* Swap two stats - too complex to do safely, leave as roleplay or do arbitrary swap. let's swap str and intl */
                     updatedStats = updatedStats.copy(str = stats.intl, intl = stats.str)
                }
                "n4" -> { 
                     // random small blessing or small curse
                     if (java.util.Random().nextBoolean()) {
                         updatedStats = updatedStats.copy(apPoints = updatedStats.apPoints + 50)
                     } else {
                         updatedStats = updatedStats.copy(gold = maxOf(0, updatedStats.gold - 50))
                     }
                }
                "n5" -> { /* Reset tasks exact same */ }
                "n6" -> { /* +50% diff, +50% reward random mission. Done below */ }
                "n7" -> { updatedStats = updatedStats.copy(apPoints = maxOf(0, updatedStats.apPoints - 200)) } 
                "n8" -> { /* free gate... handle via generic logic */ }
                "n9" -> { /* Undo... */ }
                "n10" -> { /* Choose card... */ }
                "c1" -> { /* running double - logic handled using missionDifficultyMultiplier for now */ }
                "c2" -> { /* pushups double */ }
                "c3" -> { /* squats double */ }
                "c4" -> { /* reading double */ }
                "c5" -> { /* plank double */ }
                "c6" -> { updatedStats = updatedStats.copy(missionDifficultyMultiplier = 1.5f) } 
                "c7" -> { updatedStats = updatedStats.copy(apPoints = maxOf(0, updatedStats.apPoints - 200), extraMissions = 1) } 
                "c8" -> { updatedStats = updatedStats.copy(forcedGateBefore = System.currentTimeMillis() + 8 * 3600 * 1000L) } 
                "c9" -> { updatedStats = updatedStats.copy(missionDifficultyMultiplier = 2.0f) }
                "c10" -> { updatedStats = updatedStats.copy(missionDifficultyMultiplier = 2.0f, deadlineReductionHours = 2) }
            }

            // If modifying mission difficulty statically, we need to update existing quests? No, we will compute them gracefully when we fetch them. But for physical goals, we should multiply their targets.
            if (updatedStats.missionDifficultyMultiplier != 1.0f) {
                val list = exercises.value
                list.forEach { task ->
                    repository.updateExercise(task.copy(target = (task.target * updatedStats.missionDifficultyMultiplier).toInt().coerceAtLeast(1)))
                }
            }

            repository.updateStats(updatedStats)
            
            val logEmotion = when (card.type) {
                "blessing" -> "الحظ يبتسم لك اليوم."
                "curse" -> "اللعنة حلَّت."
                else -> "القدر يلعب لعبته."
            }
            if (card.rarity == "legendary") {
                repository.addLog("Aegris: بطاقة أسطورية! هذا يوم نادر. ($logEmotion)")
            } else {
                repository.addLog("Aegris: $logEmotion تم سحب [${card.name}]")
            }

            updateTodayEntry(
                fateCardDrawn = card.name,
                fateCardType = card.type
            )
        }
    }

    // Reset fate drawn status is no longer manually triggered, but clears when a new day arrives
    fun calculateApWithDebt(stats: UserStats, gainedAp: Int): Pair<Int, UserStats> {
        if (stats.remainingDebt <= 0 || gainedAp <= 0) {
            return Pair(gainedAp, stats)
        }
        val repayAmount = (gainedAp * 0.3).toInt().coerceAtLeast(1).coerceAtMost(stats.remainingDebt)
        val newRemainingDebt = stats.remainingDebt - repayAmount
        val actualApGained = gainedAp - repayAmount
        
        viewModelScope.launch {
            repository.addLog("💸 نظام المديونية: تم سداد $repayAmount AP تلقائياً من نقاط السلوك لصالح عين أغريس! المتبقي: ${newRemainingDebt} AP")
        }

        var updatedStats = stats.copy(
            remainingDebt = newRemainingDebt,
            lastPaymentDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        )
        if (newRemainingDebt <= 0) {
            updatedStats = updatedStats.copy(originalDebt = 0, debtDateStr = "")
            viewModelScope.launch {
                repository.addLog("🎉 تهانينا! لقد تم سداد جميع الديون المترتبة على الوعاء بالكامل!")
            }
        }
        return Pair(actualApGained, updatedStats)
    }

    fun gainLife() {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            if (stats.lives < stats.maxLives) {
                repository.updateStats(stats.copy(lives = stats.lives + 1))
                repository.addLog("Aegris: استعدت حياة. واصل.")
            }
        }
    }

    fun trackCompleteDay() {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val newConsecutive = stats.consecutiveCompleteDays + 1
            repository.updateStats(stats.copy(
                consecutiveCompleteDays = newConsecutive,
                streak = stats.streak + 1,
                bestStreak = maxOf(stats.bestStreak, stats.streak + 1)
            ))
            
            if (newConsecutive >= 3 && stats.lives < stats.maxLives) {
                repository.updateStats(userStats.value!!.copy(consecutiveCompleteDays = 0))
                gainLife()
            }
            
            // Check milestones notification
            val ms = stats.streak + 1
            when (ms) {
                7 -> repository.addLog("Aegris: أسبوع من الانضباط.")
                30 -> repository.addLog("Aegris: شهر كامل.")
                100 -> repository.addLog("Aegris: 100 يوم.")
                365 -> repository.addLog("Aegris: سنة كاملة.")
            }
            checkTitleUnlocks(userStats.value!!)
        }
    }

    fun applyPenaltyWithDebt(penaltyAp: Int) {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            var remainingPenalty = penaltyAp
            
            if (stats.apPoints >= remainingPenalty) {
                repository.updateStats(stats.copy(apPoints = stats.apPoints - remainingPenalty))
            } else {
                val deficit = remainingPenalty - stats.apPoints
                val updatedOriginalDebt = stats.originalDebt + deficit
                val updatedRemainingDebt = stats.remainingDebt + deficit
                val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                
                val updatedStats = stats.copy(
                    apPoints = 0,
                    originalDebt = updatedOriginalDebt,
                    remainingDebt = updatedRemainingDebt,
                    debtDateStr = if (stats.debtDateStr.isEmpty()) todayStr else stats.debtDateStr
                )
                repository.updateStats(updatedStats)
                repository.addLog("Aegris: أقرضتك. كل تمرين الآن يسدد دينك.")
            }
        }
    }
    
    fun manualRepayDebt(amount: Int) {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            if (stats.remainingDebt <= 0 || stats.apPoints < amount) return@launch
            
            val actualPayment = minOf(amount, stats.remainingDebt)
            val updatedRemainingDebt = stats.remainingDebt - actualPayment
            val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            
            var updatedStats = stats.copy(
                apPoints = stats.apPoints - actualPayment,
                remainingDebt = updatedRemainingDebt,
                lastPaymentDateStr = todayStr
            )
            
            if (updatedRemainingDebt <= 0) {
                updatedStats = updatedStats.copy(originalDebt = 0, debtDateStr = "")
                repository.addLog("Aegris: الدين انتهى. لا تقترب من العجز مرة أخرى.")
            }
            repository.updateStats(updatedStats)
        }
    }

    fun loseLife() {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val newLives = maxOf(0, stats.lives - 1)
            var updatedStats = stats.copy(lives = newLives, consecutiveCompleteDays = 0)
            
            if (newLives == 0) {
                updatedStats = updatedStats.copy(isSystemOverridePending = true)
                repository.addLog("Aegris: فقدت كل الأرواح. واجه العواقب.")
            } else {
                repository.addLog("Aegris: فقدت حياة. $newLives متبقية.")
            }
            repository.updateStats(updatedStats)
        }
    }

    fun breakStreak() {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            if (stats.streak > 0) {
                repository.updateStats(stats.copy(
                    streak = 0, 
                    hasStreakBrokenToday = true,
                    consecutiveCompleteDays = 0
                ))
                repository.addLog("Aegris: سقطت السلسلة. قم. ابدأ من جديد.")
            }
        }
    }

    fun acceptSystemOverride() {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            // Takes 2000 AP as debt or deducts
            applyPenaltyWithDebt(2000)
            
            // Reset lives to max to allow continuing
            val updatedStats = userStats.value ?: return@launch
            repository.updateStats(updatedStats.copy(
                lives = updatedStats.maxLives,
                isSystemOverridePending = false
            ))
            repository.addLog("Aegris: لقد قبلت العقوبة. ادفع الثمن وانهض.")
        }
    }

    fun resetStreakBrokenFlag() {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            repository.updateStats(stats.copy(hasStreakBrokenToday = false))
        }
    }

    private fun checkDawnMissionStatus() {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val now = java.util.Calendar.getInstance()
            val hour = now.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = now.get(java.util.Calendar.MINUTE)
            val currentMinutes = hour * 60 + minute
            
            val limitMinutes = if (stats.unlockedTitles.contains("title_dawn_guardian")) {
                6 * 60 + 30
            } else {
                6 * 60
            }
            val startMinutes = 4 * 60
            
            val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(now.time)
            val isCompletedToday = stats.lastDawnMissionDate == todayStr

            if (!isCompletedToday && currentMinutes in startMinutes..limitMinutes) {
                isDawnMissionAvailable.value = true
                if (currentDawnMission.value == null) {
                    generateDawnMission(stats.rank)
                    repository.addLog("Aegris: الفجر يراقب. لا تخذله. (مهمة فجر مخفية متاحة!)")
                }
            } else {
                if (isDawnMissionAvailable.value && !isCompletedToday) {
                    repository.addLog("Aegris: الفجر مر دونك. ربما غداً.")
                }
                isDawnMissionAvailable.value = false
            }
        }
    }

    private fun generateDawnMission(rank: String) {
        val multiplier = when (rank) {
            "E", "D" -> 3f
            "C", "B" -> 4f
            "A", "S" -> 5f
            else -> 6f
        }
        
        val mission = when (rank) {
            "E" -> DawnMission(description = "50 ضغط + 50 قرفصاء + 2 كم جري", exerciseType = "compound", targetReps = 100, timeLimitMinutes = 45, xpMultiplier = multiplier, apMultiplier = multiplier)
            "D" -> DawnMission(description = "100 ضغط + 80 قرفصاء + 3 كم جري", exerciseType = "compound", targetReps = 180, timeLimitMinutes = 55, xpMultiplier = multiplier, apMultiplier = multiplier)
            "C" -> DawnMission(description = "150 ضغط + 5 كم جري", exerciseType = "compound", targetReps = 150, timeLimitMinutes = 60, xpMultiplier = multiplier, apMultiplier = multiplier)
            "B" -> DawnMission(description = "200 ضغط + 8 كم جري", exerciseType = "compound", targetReps = 200, timeLimitMinutes = 75, xpMultiplier = multiplier, apMultiplier = multiplier)
            "A" -> DawnMission(description = "15 كم جري فقط", exerciseType = "running", targetReps = 15, timeLimitMinutes = 90, xpMultiplier = multiplier, apMultiplier = multiplier)
            "S" -> DawnMission(description = "300 ضغط + 10 كم جري", exerciseType = "compound", targetReps = 300, timeLimitMinutes = 100, xpMultiplier = multiplier, apMultiplier = multiplier)
            "SS" -> DawnMission(description = "500 ضغط فقط", exerciseType = "pushups", targetReps = 500, timeLimitMinutes = 110, xpMultiplier = multiplier, apMultiplier = multiplier)
            else -> DawnMission(description = "20 كم جري فقط", exerciseType = "running", targetReps = 20, timeLimitMinutes = 120, xpMultiplier = multiplier, apMultiplier = multiplier)
        }
        
        currentDawnMission.value = mission
    }
    
    fun completeDawnMission() {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val mission = currentDawnMission.value ?: return@launch
            if (mission.isCompleted) return@launch
            
            val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            
            val awardXp = (mission.baseXPReward * mission.xpMultiplier).toInt()
            val awardAp = (mission.baseAPReward * mission.apMultiplier).toInt()
            
            val updatedStats = stats.copy(
                xp = stats.xp + awardXp,
                apPoints = stats.apPoints + awardAp,
                totalDawnMissionsCompleted = stats.totalDawnMissionsCompleted + 1,
                lastDawnMissionDate = todayStr
            )
            
            repository.updateStats(updatedStats)
            checkTitleUnlocks(updatedStats)
            
            updateTodayEntry(dawnMissionCompleted = true)
            
            currentDawnMission.value = mission.copy(isCompleted = true)
            isDawnMissionAvailable.value = false
            
            repository.addLog("Aegris: الفجر يشهد. أنت من القلائل. (+${awardXp} XP, +${awardAp} AP)")
        }
    }
    
    fun completeDawnMeditation() {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val mission = currentDawnMission.value ?: return@launch
            if (!mission.isCompleted || mission.isMeditationDone) return@launch
            
            val extraXp = (mission.baseXPReward * mission.xpMultiplier * 0.5f).toInt()
            val updatedStats = stats.copy(xp = stats.xp + extraXp)
            
            repository.updateStats(updatedStats)
            currentDawnMission.value = mission.copy(isMeditationDone = true)
            
            repository.addLog("Aegris: حتى روحك استيقظت. ممتاز. (+${extraXp} XP)")
        }
    }

    // Attacks the active Gate Boss
    fun attackDungeonBoss(damage: Int) {
        val currentHp = dungeonBossHp.value
        val nextHp = (currentHp - damage).coerceAtLeast(0)
        dungeonBossHp.value = nextHp

        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            if (nextHp == 0) {
                val multiplier = when (dungeonMood.value) {
                    "ANGRY" -> 2.0
                    "GENEROUS" -> 2.0
                    "SLEEPING" -> 1.0
                    else -> 1.5
                }
                val baseGold = 800
                val baseAp = 150
                var finalGold = (baseGold * multiplier).toInt()
                var finalAp = (baseAp * multiplier).toInt()
                var gainedShards = 20

                if (stats.activeTitleId == "title_sweeper") {
                    finalGold = (finalGold * 1.15).toInt()
                }
                if (stats.activeTitleId == "title_lord_wisdom") {
                    finalAp = (finalAp * 1.10).toInt()
                }
                if (stats.activeTitleId == "title_abyss_devourer") {
                    // 10% chance to double earned shards (from 20 to 40)
                    if (Random.nextDouble() < 0.10) {
                        gainedShards = 40
                        repository.addLog("🌌 تأثير لقب [مبتلع الهاوية]: تم مضاعفة شظايا الهاوية المكتسبة!")
                    }
                }

                val updatedStats = stats.copy(
                    gold = stats.gold + finalGold,
                    apPoints = stats.apPoints + finalAp,
                    abyssShards = stats.abyssShards + gainedShards,
                    manaStored = (stats.manaStored + 50).coerceAtMost(stats.manaGoal)
                )
                repository.updateStats(updatedStats)
                repository.addLog("GATE CLEAR: SLAIN [${dungeonBossName.value}] in ${dungeonMood.value} mood! Gained +$finalGold Gold, +$finalAp AP, +50 Mana, +$gainedShards Shards.")

                val moods = listOf("ANGRY", "SLEEPING", "HUNGRY", "GENEROUS", "LEGENDARY")
                val bosses = listOf("WHITE WEREWOLF", "STONE GOLEM", "EVIL EYE", "KING OF DEATH")
                val randomMood = moods[Random.nextInt(moods.size)]
                val randomBoss = bosses[Random.nextInt(bosses.size)]

                dungeonMood.value = randomMood
                dungeonBossName.value = randomBoss
                
                val maxHpVal = if (randomMood == "LEGENDARY") 250 else if (randomMood == "ANGRY") 150 else 100
                dungeonBossMaxHp.value = maxHpVal
                dungeonBossHp.value = maxHpVal
            } else {
                if (Random.nextInt(3) == 0) {
                    repository.addLog("COMBAT: Inflicted $damage damage on [${dungeonBossName.value}]. Boss HP: $nextHp/${dungeonBossMaxHp.value}")
                }
            }
        }
    }

    // Deducts AP for hungry dungeons
    fun payDungeonEntrance(): Boolean {
        if (dungeonMood.value != "HUNGRY") return true
        val stats = userStats.value ?: return false
        if (stats.apPoints < 50) {
            viewModelScope.launch {
                repository.addLog("ENTRY FAILED: Dungeon is HUNGRY. Requires 50 AP sacrifice.")
            }
            return false
        }
        viewModelScope.launch {
            val updatedStats = stats.copy(apPoints = stats.apPoints - 50)
            repository.updateStats(updatedStats)
            repository.addLog("DUNGEON ENTRY: Sacrificed 50 AP to enter HUNGRY gate.")
        }
        return true
    }

    private var isCheckingShadows = false

    fun checkShadowUnlocks(stats: UserStats) {
        if (isCheckingShadows) return
        isCheckingShadows = true
        viewModelScope.launch {
            try {
                val shadowList = shadows.value
                shadowList.forEach { shadow ->
                    if (shadow.level == 0 && !shadow.isMerged) {
                        val meetsRequirement = when (shadow.id) {
                            "iron" -> stats.physicalMissions >= 50
                            "igris" -> stats.mentalMissions >= 30
                            "tank" -> stats.streak >= 21
                            "fang" -> stats.unlockedTitles.contains("title_gate_tamer")
                            "jima" -> stats.unlockedTitles.contains("title_lord_wisdom")
                            "beru" -> stats.defeatedGates >= 20
                            "bellion" -> stats.level >= 50
                            else -> false
                        }
                        if (meetsRequirement) {
                            newlyUnlockedShadow.value = shadow
                            val unlockedShadow = shadow.copy(level = 1, loyalty = 50)
                            repository.updateShadow(unlockedShadow)
                            repository.addLog("🏆 استدعاب عظيم: استجاب جند الظل [${shadow.arabicName}] ملقياً بعهد الطاعة والولاء للوعاء!")
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle silently
            } finally {
                isCheckingShadows = false
            }
        }
    }

    fun dismissShadowUnlock() {
        newlyUnlockedShadow.value = null
    }

    fun extractShadow(shadowId: String) {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val shadowList = shadows.value
            val shadow = shadowList.find { it.id == shadowId } ?: return@launch

            if (shadow.level > 0) return@launch // Already unlocked

            var requiredMana = shadow.manaCost
            if (stats.activeTitleId == "title_shadow_monarch") {
                requiredMana = (requiredMana * 0.8).toInt()
            }

            if (stats.manaStored < requiredMana) {
                repository.addLog("⚠️ النهوض فشل: كمية مانغا الظلال غير كافية! يتطلب ${requiredMana} مانا. المخزون الحالي: ${stats.manaStored}")
                return@launch
            }

            val updatedStats = stats.copy(
                manaStored = stats.manaStored - requiredMana,
                str = if (shadow.passiveStat == "str") stats.str + shadow.passiveAmount else stats.str,
                agi = if (shadow.passiveStat == "agi") stats.agi + shadow.passiveAmount else stats.agi,
                endurance = if (shadow.passiveStat == "endurance") stats.endurance + shadow.passiveAmount else stats.endurance,
                intl = if (shadow.passiveStat == "intl") stats.intl + shadow.passiveAmount else stats.intl
            )
            val updatedShadow = shadow.copy(level = 1, loyalty = 50)

            repository.updateStats(updatedStats)
            repository.updateShadow(updatedShadow)
            repository.addLog("🌌 استنهاض ناجح للظل: [${shadow.arabicName}] رفيقاً للعرش! تم تفعيل ميزاته الإحصائية وسحب ${requiredMana} مانا.")
        }
    }

    fun upgradeShadow(shadowId: String) {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val shadowList = shadows.value
            val shadow = shadowList.find { it.id == shadowId } ?: return@launch

            if (shadow.level == 0) {
                repository.addLog("⚠️ التطور مكسور: الظل لا يزال غير ملتزم بالنهوض.")
                return@launch
            }
            if (shadow.level >= 3) {
                repository.addLog("🏆 رتبة قصوى! الظل [${shadow.arabicName}] بلغ حد التمكين العظيم برتبة قائد فصيلة.")
                return@launch
            }

            val apCost = if (shadow.level == 1) {
                when (shadow.id) {
                    "iron" -> 500
                    "igris" -> 600
                    "tank" -> 700
                    "fang" -> 800
                    "jima" -> 900
                    "beru" -> 1000
                    "bellion" -> 1500
                    else -> 500
                }
            } else {
                when (shadow.id) {
                    "iron" -> 1000
                    "igris" -> 1200
                    "tank" -> 1400
                    "fang" -> 1600
                    "jima" -> 1800
                    "beru" -> 2000
                    "bellion" -> 3000
                    else -> 1000
                }
            }

            if (stats.apPoints < apCost) {
                repository.addLog("❌ فشل الترقية: نقاط AP غير كافية لترقية [${shadow.arabicName}]. يتطلب $apCost AP.")
                return@launch
            }

            val nextLevel = shadow.level + 1
            val increment = 8
            val updatedStats = stats.copy(
                apPoints = stats.apPoints - apCost,
                str = if (shadow.passiveStat == "str") stats.str + increment else stats.str,
                agi = if (shadow.passiveStat == "agi") stats.agi + increment else stats.agi,
                endurance = if (shadow.passiveStat == "endurance") stats.endurance + increment else stats.endurance,
                intl = if (shadow.passiveStat == "intl") stats.intl + increment else stats.intl
            )

            val updatedShadow = shadow.copy(
                level = nextLevel,
                passiveAmount = shadow.passiveAmount + increment,
                loyalty = (shadow.loyalty + 15).coerceAtMost(100)
            )

            repository.updateStats(updatedStats)
            repository.updateShadow(updatedShadow)

            val rankLabel = when (nextLevel) {
                2 -> "النخبة العليا"
                3 -> "قائد الظلال الفاتح"
                else -> "عادي"
            }
            repository.addLog("🧬 ارتقاء كلي! ترقى الظل [${shadow.arabicName}] لرتبة ($rankLabel) بنجاح. تم خصم $apCost AP.")
        }
    }

    fun activateShadow(shadowId: String) {
        viewModelScope.launch {
            val shadowList = shadows.value
            val activeCount = shadowList.count { it.isActive && it.level > 0 }
            if (activeCount >= 5) {
                repository.addLog("⚠️ استدعاء مفرط: لقد بلغت الحد الأقصى للرفاق الخمسة النشطين في الميدان للقتال.")
                return@launch
            }

            val shadow = shadowList.find { it.id == shadowId } ?: return@launch
            if (shadow.level == 0) {
                repository.addLog("⚠️ الظل لا يزال نائماً في عالم العدم.")
                return@launch
            }

            val updatedShadow = shadow.copy(isActive = true)
            repository.updateShadow(updatedShadow)
            repository.addLog("⚔️ انضم الظل [${shadow.arabicName}] إلى التشكيل القتالي النشط (رتبة ${shadow.rankArabic}).")
        }
    }

    fun deactivateShadow(shadowId: String) {
        viewModelScope.launch {
            val shadowList = shadows.value
            val shadow = shadowList.find { it.id == shadowId } ?: return@launch
            val updatedShadow = shadow.copy(isActive = false)
            repository.updateShadow(updatedShadow)
            repository.addLog("👥 تم إعادة الظل [${shadow.arabicName}] لمستقر الأثير وجعله غير نشط.")
        }
    }

    fun increaseShadowLoyalty(shadowId: String, useGold: Boolean) {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val shadowList = shadows.value
            val shadow = shadowList.find { it.id == shadowId } ?: return@launch

            if (shadow.level == 0) return@launch
            if (shadow.loyalty >= 100) {
                repository.addLog("💖 [${shadow.arabicName}] مخلص كدماء الصياد الأبدية، ولاء مطلق 100%.")
                return@launch
            }

            if (useGold) {
                if (stats.gold < 300) {
                    repository.addLog("❌ الذهب غير كافي لتقديم غنائم المعارك لـ [${shadow.arabicName}]. يتطلب 300 ذهبة.")
                    return@launch
                }
                val updatedStats = stats.copy(gold = stats.gold - 300)
                val updatedShadow = shadow.copy(loyalty = (shadow.loyalty + 15).coerceAtMost(100))
                repository.updateStats(updatedStats)
                repository.updateShadow(updatedShadow)
                repository.addLog("🍖 قدمت جوهرة المغارة النفيسة لـ [${shadow.arabicName}]. زادت الرابطة بمقدار +15%!")
            } else {
                if (stats.apPoints < 50) {
                    repository.addLog("❌ نقاط AP غير كافية لتمويل طقس التدريب!")
                    return@launch
                }
                val updatedStats = stats.copy(apPoints = stats.apPoints - 50)
                val updatedShadow = shadow.copy(loyalty = (shadow.loyalty + 15).coerceAtMost(100))
                repository.updateStats(updatedStats)
                repository.updateShadow(updatedShadow)
                repository.addLog("⚔️ قمت بتمارين عسكرية مشتركة مع [${shadow.arabicName}]. ارتفع الولاء بمقدار +15%!")
            }
        }
    }

    fun sacrificeShadow(shadowId: String) {
        viewModelScope.launch {
            val shadowList = shadows.value
            val shadow = shadowList.find { it.id == shadowId } ?: return@launch

            if (shadow.level == 0) return@launch
            if (shadow.loyalty < 100) {
                repository.addLog("⚠️ تضحية مرفوضة: يجب أن يبلغ ولاء [${shadow.arabicName}] نسبة 100% لتفعيل ميثاق الفداء الحقيقي.")
                return@launch
            }

            val returnTime = System.currentTimeMillis() + (7L * 24L * 60L * 60L * 1000L)
            val updatedShadow = shadow.copy(
                isSacrificed = true,
                isActive = false,
                sacrificeReturnTime = returnTime,
                loyalty = 20
            )
            repository.updateShadow(updatedShadow)
            repository.addLog("🩸 تضحية عظمى! وهب [${shadow.arabicName}] جوهره السحري ليفدي الوعاء من عقوبات النظام السبعة أيام القادمة.")
        }
    }

    fun checkSacrificeReturns() {
        viewModelScope.launch {
            val shadowList = shadows.value
            val currentTime = System.currentTimeMillis()
            shadowList.forEach { shadow ->
                if (shadow.isSacrificed && currentTime >= shadow.sacrificeReturnTime) {
                    val returnedShadow = shadow.copy(
                        isSacrificed = false,
                        sacrificeReturnTime = 0L
                    )
                    repository.updateShadow(returnedShadow)
                    repository.addLog("🌌 ميثاق عودة: عاد رفيق الظل المخلص [${shadow.arabicName}] من أثير الهاوية متأهباً للمعركة.")
                }
            }
        }
    }

    fun mergeShadows(id1: String, id2: String) {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val shadowList = shadows.value
            val s1 = shadowList.find { it.id == id1 } ?: return@launch
            val s2 = shadowList.find { it.id == id2 } ?: return@launch

            if (s1.level == 0 || s2.level == 0) {
                repository.addLog("❌ فشل الدمج: كلا الظلين يجب أن يكونا مفعّلين لديك.")
                return@launch
            }
            if (s1.isMerged || s2.isMerged) {
                repository.addLog("❌ فشل الدمج: لا يمكن ترقية مظهر مندمج سابقاً.")
                return@launch
            }

            val mergeCost = 500
            if (stats.apPoints < mergeCost) {
                repository.addLog("❌ نقاط AP غير كافية لإتمام الدمج العصبي للظلال! يتطلب $mergeCost AP.")
                return@launch
            }

            val pair = setOf(id1, id2)
            val mergedId: String
            val mergedName: String
            val mergedArabicName: String
            val mergedRank: String
            val mergedRankArabic: String
            val mergedPassive: String
            val mergedAmount: Int
            val mergedDescEng: String
            val mergedDescAr: String

            when {
                pair == setOf("iron", "igris") -> {
                    mergedId = "merged_iron_igris"
                    mergedName = "Great General"
                    mergedArabicName = "الجنرال العظيم"
                    mergedRank = "Supreme Hybrid"
                    mergedRankArabic = "دمج قوة وسرعة النخبة"
                    mergedPassive = "str"
                    mergedAmount = s1.passiveAmount + s2.passiveAmount + 10
                    mergedDescEng = "Tactical planning + physical double power."
                    mergedDescAr = "الجنرال العظيم: تخطيط استراتيجي مدموج بقوة عضلية مضاعفة، يمنحك ميزة هجومية 25%+."
                }
                pair == setOf("tank", "fang") -> {
                    mergedId = "merged_tank_fang"
                    mergedName = "Speed Fortress"
                    mergedArabicName = "الحارس السريع"
                    mergedRank = "Supreme Hybrid"
                    mergedRankArabic = "دمج الصد والرشاقة"
                    mergedPassive = "endurance"
                    mergedAmount = s1.passiveAmount + s2.passiveAmount + 10
                    mergedDescEng = "Total defense with supersonic agility."
                    mergedDescAr = "الحصن السريع: تفادي كامل لضرر الفشل بنسبة تصاعدية ومضاعفة احتساب مسافات الجري 2x."
                }
                pair == setOf("beru", "bellion") -> {
                    mergedId = "merged_beru_bellion"
                    mergedName = "Slayer King"
                    mergedArabicName = "الملك القاتل"
                    mergedRank = "Supreme Hybrid"
                    mergedRankArabic = "المبايعة الدموية والسيادة"
                    mergedPassive = "agi"
                    mergedAmount = s1.passiveAmount + s2.passiveAmount + 15
                    mergedDescEng = "20% critical boss bypass execution."
                    mergedDescAr = "الملك القاتل: فرصة ضربة قاضية تزيد لـ 20% لتخطي الحراس مع إمكانية استبدال مهامك يومياً مجاناً."
                }
                pair == setOf("jima", "igris") -> {
                    mergedId = "merged_jima_igris"
                    mergedName = "Archmage Diviner"
                    mergedArabicName = "العراف الأكبر"
                    mergedRank = "Supreme Hybrid"
                    mergedRankArabic = "حكيم التخطيط الكوني"
                    mergedPassive = "intl"
                    mergedAmount = s1.passiveAmount + s2.passiveAmount + 12
                    mergedDescEng = "Daily wisdom analysis + total gateway structural weakness view."
                    mergedDescAr = "العراف الأعظم للوعاء: بصرية كاشفة لكافة نقاط ضعف حارس بواباتك ونسب النجاح فوراً."
                }
                pair == setOf("iron", "tank") -> {
                    mergedId = "merged_iron_tank"
                    mergedName = "Aegis Bastion"
                    mergedArabicName = "الحصن المطلق"
                    mergedRank = "Supreme Hybrid"
                    mergedRankArabic = "قلعة الطود الأبدية"
                    mergedPassive = "endurance"
                    mergedAmount = s1.passiveAmount + s2.passiveAmount + 12
                    mergedDescEng = "Protects from major structural failures entirely."
                    mergedDescAr = "الحصن اللانهائي: درع صامد مدى الحياة يصد أي عقوبة أو تراجع 3 مرات متتالية دون كسر."
                }
                pair == setOf("fang", "beru") -> {
                    mergedId = "merged_fang_beru"
                    mergedName = "Quickstrike Predator"
                    mergedArabicName = "الصياد الخاطف"
                    mergedRank = "Supreme Hybrid"
                    mergedRankArabic = "الضوء القاطع"
                    mergedPassive = "agi"
                    mergedAmount = s1.passiveAmount + s2.passiveAmount + 14
                    mergedDescEng = "Hyper velocity sonic swift attributes."
                    mergedDescAr = "الصياد البري الكاسر: زيادة استثنائية لخصائص الرشاقة +25 وحساب تمارين الكارديو بنسب مضاعفة."
                }
                else -> {
                    repository.addLog("❌ خطأ: الكيان الخصم مفقود أو غير متوافق. الوصفة مرفوضة.")
                    return@launch
                }
            }

            val updatedStats = stats.copy(apPoints = stats.apPoints - mergeCost)
            val updatedS1 = s1.copy(isMerged = true, isActive = false, level = -1)
            val updatedS2 = s2.copy(isMerged = true, isActive = false, level = -1)

            val hybridShadow = ShadowSoldier(
                id = mergedId,
                name = mergedName,
                arabicName = mergedArabicName,
                rank = mergedRank,
                rankArabic = mergedRankArabic,
                manaCost = 0,
                goldCost = 0,
                level = 3,
                passiveStat = mergedPassive,
                passiveAmount = mergedAmount,
                description = mergedDescEng,
                arabicDescription = mergedDescAr,
                loyalty = 100,
                isActive = true,
                isSacrificed = false,
                sacrificeReturnTime = 0L,
                isMerged = true,
                originalMergedIds = "$id1,$id2"
            )

            repository.updateStats(updatedStats)
            repository.updateShadow(updatedS1)
            repository.updateShadow(updatedS2)
            repository.updateShadow(hybridShadow)

            repository.addLog("💜 طقس الاندماج اكتمل! اندمج [${s1.arabicName}] مع [${s2.arabicName}] فولد الكيان الكوني الجديد: [${hybridShadow.arabicName}]!")
        }
    }

    fun addLog(message: String) {
        viewModelScope.launch {
            repository.addLog(message)
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    // ==========================================
    // --- GATES & SIDE QUESTS SYSTEM ENGINE ---
    // ==========================================

    fun getGateExpiryDurationMs(playerRank: String): Long {
        val hours = when (playerRank.uppercase().trim()) {
            "E" -> 12
            "D" -> 10
            "C" -> 8
            "B" -> 6
            "A" -> 4
            "S" -> 3
            "SS" -> 2
            "SSS" -> 1
            else -> 12
        }
        return hours * 3600 * 1000L
    }

    fun getPlayerPrizeMultiplier(playerRank: String): Double {
        return when (playerRank.uppercase().trim()) {
            "SSS" -> 10.0
            "SS" -> 6.0
            "S" -> 4.0
            "A" -> 3.0
            "B" -> 2.2
            "C" -> 1.7
            "D" -> 1.3
            else -> 1.0
        }
    }

    fun generateDailyGates(playerRank: String, playerLevel: Int) {
        viewModelScope.launch {
            val allGates = GateData.generateAllGates()
            val mult = getPlayerPrizeMultiplier(playerRank)
            
            val eGates = allGates.filter { it.rank == "E" }
            val dGates = allGates.filter { it.rank == "D" }
            val cGates = allGates.filter { it.rank == "C" }
            val bGates = allGates.filter { it.rank == "B" }
            val aGates = allGates.filter { it.rank == "A" }
            val sGates = allGates.filter { it.rank == "S" }

            // Classify player's rank to safe gate selection
            val mappedRank = when {
                playerRank == "SS" || playerRank == "SSS" -> "S"
                else -> playerRank
            }

            val sameRankPool = when (mappedRank) {
                "E" -> eGates
                "D" -> dGates
                "C" -> cGates
                "B" -> bGates
                "A" -> aGates
                else -> sGates
            }

            val lowerRankPool = when (mappedRank) {
                "E" -> eGates
                "D" -> eGates
                "C" -> eGates + dGates
                "B" -> eGates + dGates + cGates
                "A" -> eGates + dGates + cGates + bGates
                else -> eGates + dGates + cGates + bGates + aGates
            }

            val higherRankPool = when (mappedRank) {
                "E" -> dGates
                "D" -> cGates
                "C" -> bGates
                "B" -> aGates
                else -> sGates
            }

            val selectedGates = mutableListOf<Gate>()
            
            // 2-3 same rank
            val samePoolShuffled = sameRankPool.shuffled()
            val sameCount = Random.nextInt(2, 4).coerceAtMost(samePoolShuffled.size)
            selectedGates.addAll(samePoolShuffled.take(sameCount))

            // 1-2 lower rank
            val lowerPoolShuffled = lowerRankPool.shuffled()
            val lowerCount = Random.nextInt(1, 3).coerceAtMost(lowerPoolShuffled.size)
            selectedGates.addAll(lowerPoolShuffled.take(lowerCount))

            // 1 higher rank (always)
            val higherPoolShuffled = higherRankPool.shuffled()
            if (higherPoolShuffled.isNotEmpty()) {
                selectedGates.addAll(higherPoolShuffled.take(1))
            }

            // 0-1 random
            val allShuffled = allGates.shuffled()
            if (Random.nextBoolean() && allShuffled.isNotEmpty()) {
                selectedGates.addAll(allShuffled.take(1))
            }

            // Filter unique gates and scale rewards
            val uniqueGates = selectedGates.distinctBy { it.id }.map { gate ->
                val now = System.currentTimeMillis()
                gate.copy(
                    appearanceTime = now,
                    expiryTime = now + getGateExpiryDurationMs(playerRank),
                    baseAPReward = (gate.baseAPReward * mult).toInt(),
                    baseGoldReward = (gate.baseGoldReward * mult).toInt(),
                    baseXPReward = (gate.baseXPReward * mult).toInt(),
                    isCompleted = false,
                    isEntered = false
                )
            }

            repository.clearGates()
            repository.insertGates(uniqueGates)
            repository.addLog("⚡ SYSTEM SCALING: تم استدعاء البوابات اليومية وفقاً للرتبة [$playerRank] (مضاعف الجوائز: ${mult}x)")
        }
    }

    fun generateDailySideQuests() {
        viewModelScope.launch {
            val pool = GateData.getSideQuestPool()
            val selected = pool.shuffled().take(3)
            repository.clearSideQuests()
            repository.insertSideQuests(selected)
            repository.addLog("⚡ SYSTEM SCALING: تم تجديد المهام الجانبية الثلاثية لتطور الصياد")
        }
    }

    fun canEnterGate(gate: Gate): Boolean {
        return remainingGateEnergy.value > 0 && 
               System.currentTimeMillis() < gate.expiryTime && 
               !gate.isCompleted && 
               !gate.isEntered
    }

    fun enterGate(gate: Gate) {
        viewModelScope.launch {
            if (canEnterGate(gate)) {
                remainingGateEnergy.value = (remainingGateEnergy.value - 1).coerceAtLeast(0)
                gatesEnteredToday.value++
                
                val updated = gate.copy(isEntered = true)
                repository.updateGate(updated)
                repository.addLog("🚪 SYSTEM ACCESS: تم استهلاك طاقة واحدة ودخول بوابة [${gate.name}] (الحارس: ${gate.guardianName})")
                
                updateTodayEntry(gatesEnteredInc = 1)
            }
        }
    }

    // Direct reward grant helper to keep stats up to date cleanly
    fun addRewards(xpGainedOriginal: Int, goldGainedOriginal: Int, apPointsGainedOriginal: Int) {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            
            // Check double multipliers
            val doubleXpActive = stats.hasDoubleXP && System.currentTimeMillis() < stats.doubleXPExpiry
            val doubleGoldActive = stats.hasDoubleGold && System.currentTimeMillis() < stats.doubleGoldExpiry

            val xpMultiplier = if (doubleXpActive) 2 else 1
            val goldMultiplier = if (doubleGoldActive) 2 else 1
            val apMultiplier = if (doubleXpActive) 2 else 1

            val xpGained = ((xpGainedOriginal * xpMultiplier) * (if (stats.activeTitleId == "title_impostor") 0.8 else 1.0)).toInt()
            val goldGained = ((goldGainedOriginal * goldMultiplier) * (if (stats.activeTitleId == "title_impostor") 0.8 else 1.0)).toInt()
            val apPointsGained = ((apPointsGainedOriginal * apMultiplier) * (if (stats.activeTitleId == "title_impostor") 0.8 else 1.0)).toInt()

            val (finalAp, statsAfterDebt) = calculateApWithDebt(stats, apPointsGained)

            var currentLevel = statsAfterDebt.level
            var newXp = statsAfterDebt.xp + xpGained
            var currentMaxXp = statsAfterDebt.maxXp
            var newStatPoints = statsAfterDebt.statPoints
            var currentRemainingDebt = statsAfterDebt.remainingDebt
            
            while (newXp >= currentMaxXp) {
                newXp -= currentMaxXp
                currentLevel += 1
                currentMaxXp = getXpToNext(currentLevel, statsAfterDebt.rank)
                newStatPoints += 5
                repository.addLog("LEVEL UP! ALPJA reached LVL $currentLevel. Spark of the Monarch. +5 Stat Points.")

                if (currentRemainingDebt > 0) {
                    val extraRepay = 1
                    currentRemainingDebt -= extraRepay
                    newStatPoints = (newStatPoints - extraRepay).coerceAtLeast(0)
                    repository.addLog("💸 تسوية المديونيات: خصم $extraRepay نقطة خصائص إضافية (AP/SP) من أرباح مكافأة المستوى وسدادها لصالح دين عين أغريس المتبقي!")
                }
            }

            val finalRemainingDebt = currentRemainingDebt.coerceAtLeast(0)
            val finalOriginalDebt = if (finalRemainingDebt <= 0) 0 else statsAfterDebt.originalDebt
            val finalDebtDateStr = if (finalRemainingDebt <= 0) "" else statsAfterDebt.debtDateStr
            
            val updatedStats = statsAfterDebt.copy(
                level = currentLevel,
                xp = newXp,
                maxXp = currentMaxXp,
                gold = statsAfterDebt.gold + goldGained,
                apPoints = statsAfterDebt.apPoints + finalAp,
                statPoints = newStatPoints,
                remainingDebt = finalRemainingDebt,
                originalDebt = finalOriginalDebt,
                debtDateStr = finalDebtDateStr
            )
            repository.updateStats(updatedStats)
            
            if (currentLevel != stats.level && updatedStats.rank != stats.rank) {
                scaleDailyQuestsForRank(updatedStats.rank)
            }
        }
    }

    fun completeGate(gate: Gate, useWeakness: Boolean = false) {
        viewModelScope.launch {
            val stats = userStats.value
            if (stats != null) {
                val updatedStats = stats.copy(defeatedGates = stats.defeatedGates + 1)
                repository.updateStats(updatedStats)
            }

            // Calculate final rewards (50% bonus if weakness exploited)
            val factor = if (useWeakness && gate.weaknessName != null) 1.5 else 1.0
            val finalXp = (gate.baseXPReward * factor).toInt()
            val finalGold = (gate.baseGoldReward * factor).toInt()
            val finalAp = (gate.baseAPReward * factor).toInt()

            val updatedGate = gate.copy(isCompleted = true, isEntered = false)
            repository.updateGate(updatedGate)

            // Add rewards
            addRewards(finalXp, finalGold, finalAp)

            updateTodayEntry(
                gatesCompletedInc = 1,
                newGuardDefeated = gate.guardianName,
                xpEarnedInc = finalXp,
                goldEarnedInc = finalGold,
                apEarnedInc = finalAp
            )

            repository.addLog("☀️ SYSTEM AUDIT: لقد سقط الحارس [${gate.guardianName}] وتطهرت البوابة! +$finalXp XP، +$finalGold ذهب، +$finalAp نقاط AP")
            repository.addLog("Aegris: لقد سقط الحارس. أنت جدير.")
        }
    }

    fun failGate(gate: Gate) {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            
            // Deduct 50% gold & AP
            val penaltyGold = (gate.baseGoldReward * 0.5).toInt()
            val penaltyAp = (gate.baseAPReward * 0.5).toInt()

            val updatedStats = stats.copy(
                gold = (stats.gold - penaltyGold).coerceAtLeast(0),
                apPoints = (stats.apPoints - penaltyAp).coerceAtLeast(0)
            )
            repository.updateStats(updatedStats)

            val updatedGate = gate.copy(isEntered = false, isCompleted = false)
            repository.updateGate(updatedGate)

            repository.addLog("🚨 SYSTEM FAILURE: هرب الحارس [${gate.guardianName}]! تم خصم -$penaltyGold ذهب و -$penaltyAp نقاط AP كعقوبة تراجع")
            repository.addLog("Aegris: الحارس هرب. خسارتك محسوبة.")
        }
    }

    fun completeSideQuest(quest: SideQuest) {
        viewModelScope.launch {
            val updated = quest.copy(isCompleted = true)
            repository.updateSideQuest(updated)

            addRewards(quest.xpReward, quest.goldReward, quest.apReward)

            repository.addLog("⚡ SYSTEM AUDIT: اكتمال المهمة الجانبية [${quest.name}]! +${quest.xpReward} XP، +${quest.goldReward} ذهب، +${quest.apReward} نقاط AP")
            repository.addLog("Aegris: مهمة جانبية مكتملة. كل خطوة تقربك.")
        }
    }

    fun resetDaily() {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            remainingGateEnergy.value = 5
            gatesEnteredToday.value = 0
            
            generateDailyGates(stats.rank, stats.level)
            generateDailySideQuests()
            
            repository.addLog("☀️ SYSTEM MIDNIGHT REPORT: تم تصفير طاقة بوابات المغارة (5/5) وتحديث قائمة التحديات بنجاح.")
        }
    }

    fun dismissTitleUnlock() {
        newlyUnlockedTitle.value = null
    }

    fun equipTitle(titleId: String) {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val unlockedSet = stats.unlockedTitles.split(",").toSet()
            if (unlockedSet.contains(titleId)) {
                val title = PlayerTitle.ALL_TITLES.find { it.id == titleId } ?: return@launch
                val updatedStats = stats.copy(
                    activeTitleId = titleId,
                    activeTitle = title.name
                )
                repository.updateStats(updatedStats)
                repository.addLog("🎭 تم تجهيز لقب جديد: [${title.name}] بنجاح.")
            }
        }
    }

    private var isCheckingTitles = false

    fun checkTitleUnlocks(stats: UserStats) {
        if (isCheckingTitles) return
        isCheckingTitles = true
        viewModelScope.launch {
            try {
                val currentUnlocked = stats.unlockedTitles.split(",").map { it.trim() }.toSet()
                val newUnlockedList = mutableListOf<String>()

                PlayerTitle.ALL_TITLES.forEach { title ->
                    if (!currentUnlocked.contains(title.id)) {
                        val meetsRequirement = when (title.id) {
                            "title_sweeper" -> stats.streak >= 5
                            "title_shadow_monarch" -> stats.level >= 10 && stats.str >= 1 && stats.agi >= 1 && stats.endurance >= 1 && stats.intl >= 1
                            "title_sovereign_light" -> stats.gold >= 5000
                            "title_gate_tamer" -> stats.level >= 15
                            "title_abyss_devourer" -> stats.abyssShards >= 1000
                            "title_steel_will" -> stats.streak >= 10
                            "title_lord_wisdom" -> stats.intl >= 15
                            "title_streak_week" -> stats.bestStreak >= 7
                            "title_streak_month" -> stats.bestStreak >= 30
                            "title_streak_hundred" -> stats.bestStreak >= 100
                            "title_streak_year" -> stats.bestStreak >= 365
                            "title_dawn_son" -> stats.totalDawnMissionsCompleted >= 1
                            "title_dawn_guardian" -> stats.totalDawnMissionsCompleted >= 10
                            else -> false
                        }
                        if (meetsRequirement) {
                            newUnlockedList.add(title.id)
                            repository.addLog("🏆 إنجاز مذهل: تم إلغاء قفل لقب جديد! [${title.name}]")
                            newlyUnlockedTitle.value = title
                        }
                    }
                }

                if (newUnlockedList.isNotEmpty()) {
                    val updatedUnlocked = (currentUnlocked + newUnlockedList).joinToString(",")
                    val finalStats = stats.copy(unlockedTitles = updatedUnlocked)
                    repository.updateStats(finalStats)
                }
            } finally {
                isCheckingTitles = false
            }
        }
    }

    // --- Journal Functions ---
    fun initializeTodayEntry() {
        viewModelScope.launch {
            val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val existing = repository.getJournalEntry(todayStr)
            if (existing == null) {
                val totalMissions = exercises.value.size
                val newEntry = DailyJournalEntry(
                    id = todayStr,
                    dateMillis = System.currentTimeMillis(),
                    totalMissions = totalMissions,
                    mood = "orange"
                )
                repository.insertJournalEntry(newEntry)
                todayJournalEntry.value = newEntry
            } else {
                todayJournalEntry.value = existing
            }
        }
    }

    fun updateTodayEntry(
        missionsCompletedInc: Int = 0,
        gatesEnteredInc: Int = 0,
        gatesCompletedInc: Int = 0,
        newGuardDefeated: String? = null,
        sideQuestsCompletedInc: Int = 0,
        fateCardDrawn: String? = null,
        fateCardType: String? = null,
        xpEarnedInc: Int = 0,
        apEarnedInc: Int = 0,
        goldEarnedInc: Int = 0,
        dawnMissionCompleted: Boolean? = null,
        wasPunished: Boolean? = null,
        punishmentDurationMinutes: Int? = null
    ) {
        viewModelScope.launch {
            val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            var entry = repository.getJournalEntry(todayStr) ?: return@launch
            
            val updatedGuards = if (newGuardDefeated != null) {
                if (entry.guardsDefeated.isEmpty()) newGuardDefeated else "${entry.guardsDefeated}, $newGuardDefeated"
            } else entry.guardsDefeated

            val updatedEntry = entry.copy(
                missionsCompleted = entry.missionsCompleted + missionsCompletedInc,
                gatesEntered = entry.gatesEntered + gatesEnteredInc,
                gatesCompleted = entry.gatesCompleted + gatesCompletedInc,
                guardsDefeated = updatedGuards,
                sideQuestsCompleted = entry.sideQuestsCompleted + sideQuestsCompletedInc,
                fateCardDrawn = fateCardDrawn ?: entry.fateCardDrawn,
                fateCardType = fateCardType ?: entry.fateCardType,
                xpEarned = entry.xpEarned + xpEarnedInc,
                apEarned = entry.apEarned + apEarnedInc,
                goldEarned = entry.goldEarned + goldEarnedInc,
                dawnMissionCompleted = dawnMissionCompleted ?: entry.dawnMissionCompleted,
                wasPunished = wasPunished ?: entry.wasPunished,
                punishmentDurationMinutes = punishmentDurationMinutes ?: entry.punishmentDurationMinutes
            )
            val finalEntry = updatedEntry.copy(mood = updatedEntry.calculateMood())
            repository.insertJournalEntry(finalEntry)
            todayJournalEntry.value = finalEntry
        }
    }

    // --- AEGIS EYE ENGINE FUNCTIONS ---

    fun startAegisVerification(taskId: String, exerciseType: String, targetReps: Int) {
        aegisExerciseType.value = exerciseType
        aegisTargetReps.value = targetReps
        showAegisVerification.value = taskId
    }

    fun submitAegisResult(result: com.example.data.PurityResult) {
        viewModelScope.launch {
            val taskId = showAegisVerification.value ?: return@launch
            showAegisVerification.value = null

            val currentAttempts = aegisCheatAttempts.value
            if (result.score < 50) {
                val newCount = currentAttempts + 1
                aegisCheatAttempts.value = newCount

                val stats = userStats.value
                if (stats != null) {
                    var updatedStats = stats

                    // Add Shame Log entry
                    val newLog = com.example.data.ShameRecord(
                        timestamp = System.currentTimeMillis(),
                        exerciseType = aegisExerciseType.value,
                        cheatMethod = result.violations.joinToString(", ").ifEmpty { "تلاعب بنبض الاستجابة ونمط التكرار" },
                        penaltyApplied = if (newCount >= 4) "خصم 200 AP، تجميد المتجر 48 ساعة، فرض لقب المخادع" else "إنذار بالنظام"
                    )
                    aegisShameLog.value = aegisShameLog.value + newLog

                    if (newCount == 1 || newCount == 2) {
                        aegisIsShameMode.value = true
                        repository.addLog("🛡️ عين أغريس: تم رصد نمط تدريب مريف! غير نقي. إنذار $newCount لصاحب الوعاء.")
                    } else if (newCount == 3) {
                        aegisIsDoubtShadowActive.value = true
                        repository.addLog("🛡️ عين أغريس: الإنذار الثالث! ظل الشك يحوم حولك الآن، توقف عن الخداع.")
                    } else {
                        // Level 4+
                        val apPenalty = 200
                        val currentAp = stats.apPoints
                        val frozenTime = System.currentTimeMillis() + (48L * 60L * 60L * 1000L)
                        shopFreezeUntil.value = frozenTime

                        // Force equip impostor title
                        val title = PlayerTitle.ALL_TITLES.find { it.id == "title_impostor" }
                        val unlockedSet = stats.unlockedTitles.split(",").toMutableList()
                        if (!unlockedSet.contains("title_impostor")) {
                            unlockedSet.add("title_impostor")
                        }
                        updatedStats = stats.copy(
                            apPoints = (currentAp - apPenalty).coerceAtLeast(0),
                            activeTitleId = "title_impostor",
                            activeTitle = title?.name ?: "المخادع",
                            unlockedTitles = unlockedSet.joinToString(",")
                        )

                        repository.updateStats(updatedStats)
                        repository.addLog("🚨 عين أغريس: لقد طفح الكيل! غش مثبت للمرة الرابعة أو أكثر. خصم 200 AP، تجميد المتجر 48 ساعة، وتحميل لقب المخادع إجبارياً!")
                    }

                    // Penalty screen cheat double penalty
                    val activePen = activePenalty.value
                    if (activePen != null) {
                        val doubledPen = activePen.copy(
                            basePushups = activePen.basePushups * 2,
                            basePullups = activePen.basePullups * 2,
                            baseSquats = activePen.baseSquats * 2,
                            cheatAttempts = activePen.cheatAttempts + 1
                        )
                        activePenalty.value = doubledPen
                        repository.addLog("🚨 بروتوكول العقاب: تم مضاعفة تكرارات العقاب عقوبةً على تزوير نبض الأغريس!")
                    }
                }
            } else {
                // Exercise passes verification!
                val taskList = exercises.value
                val task = taskList.find { it.id == taskId } ?: return@launch
                val incrementNeeded = task.target - task.progress
                if (incrementNeeded > 0) {
                    syncKineticLink(taskId, incrementNeeded)
                }

                // Adjust payouts depending on scores
                if (result.score >= 90) {
                    // Give 10% bonus
                    val stats = userStats.value
                    if (stats != null) {
                        val bonusXp = (task.xpReward * 0.1).toInt()
                        val bonusGold = (task.goldReward * 0.1).toInt()
                        val bonusAp = (task.apReward * 0.1).toInt()

                        addRewards(bonusXp, bonusGold, bonusAp)
                        repository.addLog("✨ عين أغريس: نقاء مذهل (${result.score}%)! مكافأة +10% لإتقان حركة الأداء الكهرومغناطيسي للجسد.")
                    }
                } else if (result.score < 70) {
                    // Give only 50% reward (deduct 50% from stats immediately as compensation for low quality!)
                    val stats = userStats.value
                    if (stats != null) {
                        val xpDeduct = (task.xpReward * 0.5).toInt()
                        val goldDeduct = (task.goldReward * 0.5).toInt()
                        val apDeduct = (task.apReward * 0.5).toInt()

                        val newX = (stats.xp - xpDeduct).coerceAtLeast(0)
                        val newG = (stats.gold - goldDeduct).coerceAtLeast(0)
                        val newA = (stats.apPoints - apDeduct).coerceAtLeast(0)

                        repository.updateStats(stats.copy(xp = newX, gold = newG, apPoints = newA))
                        repository.addLog("⚠️ عين أغريس: نقاء تمرين متوسط (${result.score}%). تم احتساب 50% فقط من مكافآت الجهد لعدم دقة الزوايا الحركية.")
                    }
                }
                
                // Add positive log to the Hunter's system
                repository.addLog("🛡️ عين أغريس: تم التحقق بنجاح من نقاء تمرين [${task.name}] بنسبة ${result.score}%!")
            }
        }
    }

    fun getXpToNext(level: Int, rank: String): Int {
        val rankMultiplier = when (rank.uppercase()) {
            "E" -> 1.0
            "D" -> 1.2
            "C" -> 1.5
            "B" -> 2.0
            "A" -> 3.0
            "S" -> 5.0
            "SS" -> 8.0
            "SSS" -> 12.0
            else -> 1.0
        }
        return ((level * 150) + 50 * rankMultiplier).toInt()
    }

    fun resetCheatAttemptsIfClean() {
        val count = aegisCheatAttempts.value
        if (count > 0) {
            viewModelScope.launch {
                aegisCheatAttempts.value = 0
                aegisIsShameMode.value = false
                aegisIsDoubtShadowActive.value = false
                repository.addLog("🛡️ عين أغريس: مر أسبوع آمن ونظيف تماماً! تم تصفير سجل الإنذارات وإزالة جميع عقوبات الشبهة بنجاح.")
            }
        }
    }

    fun updateUsername(newName: String) {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val updated = stats.copy(username = newName)
            repository.updateStats(updated)
            repository.addLog("👤 نظام أغريس: تم تسجيل اسم الصياد الجديد بنجاح [$newName]")
        }
    }
}
