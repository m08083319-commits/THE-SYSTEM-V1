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

        // Seed data in background
        viewModelScope.launch {
            repository.initializeDatabase()
        }
    }

    // System: Awakening Constitutional State Flows
    val isPenaltyActive = MutableStateFlow(false)
    val fateDrawnToday = MutableStateFlow(false)
    val fateType = MutableStateFlow<String?>(null)
    val fateBonus = MutableStateFlow<String?>(null)
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

            val penaltyInstance = com.example.data.Penalty(
                id = "lockdown_${System.currentTimeMillis()}",
                missedMissions = missedMissions,
                baseBurpees = baseBurpees,
                baseRunningKm = baseRunningKm,
                basePushups = basePushups,
                basePullups = basePullups,
                baseSquats = baseSquats,
                startTime = System.currentTimeMillis(),
                isCompleted = false,
                cheatAttempts = 0
            )

            activePenalty.value = penaltyInstance
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
                // Award rewards
                var newXp = stats.xp + task.xpReward
                var newGold = stats.gold + task.goldReward
                var newAp = stats.apPoints + task.apReward
                var currentLevel = stats.level
                var currentMaxXp = stats.maxXp
                var newStatPoints = stats.statPoints
                var streakBonus = stats.streak
                var newMana = (stats.manaStored + 35).coerceAtMost(stats.manaGoal)

                // Logging completion
                repository.addLog("Cleared [${task.name}]: +${task.xpReward} XP, +${task.goldReward} Gold, +${task.apReward} AP, +35 Mana")

                // Handle Level-up cascade
                while (newXp >= currentMaxXp) {
                    newXp -= currentMaxXp
                    currentLevel += 1
                    currentMaxXp += 500
                    newStatPoints += 5
                    streakBonus += 1
                    repository.addLog("LEVEL UP! ALPJA reached LVL $currentLevel. Spark of the Monarch. +5 Stat Points.")
                }

                // Update user stats
                val updatedStats = stats.copy(
                    level = currentLevel,
                    xp = newXp,
                    maxXp = currentMaxXp,
                    gold = newGold,
                    apPoints = newAp,
                    statPoints = newStatPoints,
                    streak = streakBonus,
                    manaStored = newMana,
                    syncPercent = (126.9f + (currentLevel * 1.5f)) // Visual progress stability link
                )
                repository.updateStats(updatedStats)

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
            val incrementNeeded = task.target - task.progress
            if (incrementNeeded > 0) {
                syncKineticLink(taskId, incrementNeeded)
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
            repository.addLog("Dawn Protocol reset. Daily quotas refreshed & scaled for Rank ${stats.rank} (Difficulty: ${diffMult}x). Status: PRIME.")
        }
    }

    // Buy / Sync Item from shop
    fun syncItem(itemId: String) {
        viewModelScope.launch {
            val freezeTime = shopFreezeUntil.value
            if (freezeTime != null && System.currentTimeMillis() < freezeTime) {
                val remainingHours = ((freezeTime - System.currentTimeMillis()) / (1000.0 * 60.0 * 60.0))
                repository.addLog(String.format("🛡️ DENIED: المتجر مجمّد عسكرياً بقرار من أغريس بسبب الغش المتكرر! المتبقي: %.1f ساعة.", remainingHours))
                return@launch
            }

            val stats = userStats.value ?: return@launch
            val itemList = items.value
            val item = itemList.find { it.id == itemId } ?: return@launch

            // Check if user has enough currency
            if (item.costType == "AP") {
                if (stats.apPoints < item.cost) {
                    repository.addLog("FAILED: Insufficient AP to synchronize ${item.name}!")
                    return@launch
                }
                // Deduct AP, Increment item count
                val updatedStats = stats.copy(apPoints = stats.apPoints - item.cost)
                val updatedItem = item.copy(count = item.count + 1)
                
                repository.updateStats(updatedStats)
                repository.updateItem(updatedItem)
                repository.addLog("SYNC SUCCESS: Acquired ${item.name} (${item.arabicName}). -${item.cost} AP")
            } else {
                // GOLD
                if (stats.gold < item.cost) {
                    repository.addLog("FAILED: Insufficient Gold to acquire ${item.name}!")
                    return@launch
                }
                // Deduct Gold, Increment item count
                val updatedStats = stats.copy(gold = stats.gold - item.cost)
                val updatedItem = item.copy(count = item.count + 1)

                repository.updateStats(updatedStats)
                repository.updateItem(updatedItem)
                repository.addLog("SYNC SUCCESS: Purchased ${item.name} (${item.arabicName}). -${item.cost} Gold")
            }
        }
    }

    // Use a purchased item, triggering dynamic Solo Leveling action benefits
    fun useItem(itemId: String) {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val itemList = items.value
            val item = itemList.find { it.id == itemId } ?: return@launch

            if (item.count <= 0) {
                repository.addLog("FAILED: No synchronized instances of ${item.name} available!")
                return@launch
            }

            // Deduct item count
            val updatedItem = item.copy(count = item.count - 1)
            repository.updateItem(updatedItem)

            // Implement item effect
            when (item.usageType) {
                "RECOVERY" -> {
                    // Full energy, full lives, and auto-completes 3 remaining exercises
                    val unfinished = exercises.value.filter { !it.isCompleted }
                    unfinished.take(3).forEach { task ->
                        syncKineticLink(task.id, task.target - task.progress)
                    }
                    val updatedStats = stats.copy(
                        energy = 100,
                        lives = 3,
                        vesselStatus = "PRIME"
                    )
                    repository.updateStats(updatedStats)
                    repository.addLog("RECOVERY INCIDENT: Consumed Rec Potion. Vital parameters optimized to 100%. Synchronized 3 activities.")
                }
                "SHIELD" -> {
                    // Fully heals lives and adds security buffer
                    val updatedStats = stats.copy(
                        lives = 3,
                        vesselStatus = "SHIELDED"
                    )
                    repository.updateStats(updatedStats)
                    repository.addLog("ACTIVATED PENALTY SHIELD: Neutralized current alert. Status: SECURE.")
                }
                "SWORD" -> {
                    // Instantly awards bonus Gold & Stat slots
                    val updatedStats = stats.copy(
                        gold = stats.gold + 500,
                        statPoints = stats.statPoints + 2
                    )
                    repository.updateStats(updatedStats)
                    repository.addLog("HUNTER BLADE LINK: Channeling high-rank mana core. Earned +500 Gold & 2 SP instantly.")
                }
                "MAP" -> {
                    // Instantly grants XP
                    var newXp = stats.xp + 400
                    var currentLevel = stats.level
                    var currentMaxXp = stats.maxXp
                    var sp = stats.statPoints

                    while (newXp >= currentMaxXp) {
                        newXp -= currentMaxXp
                        currentLevel += 1
                        currentMaxXp += 500
                        sp += 5
                    }

                    val updatedStats = stats.copy(
                        level = currentLevel,
                        xp = newXp,
                        maxXp = currentMaxXp,
                        statPoints = sp
                    )
                    repository.updateStats(updatedStats)
                    repository.addLog("MAP DEPLOYED: Visualized rank gate. Obtained +400 XP mapping database.")
                }
                "FAKE" -> {
                    // Skips quota but adds standard warning
                    val updatedStats = stats.copy(
                        energy = 80,
                        streak = (stats.streak - 1).coerceAtLeast(0)
                    )
                    repository.updateStats(updatedStats)
                    repository.addLog("CURSED POTION CONSUMED: System audit flagged divergence. Quotas filled but streak decayed.")
                    
                    // Force complete all exercises
                    exercises.value.forEach {
                        repository.updateExercise(it.copy(progress = it.target, isCompleted = true))
                    }
                }
                "PRESSURE" -> {
                    // Amplified neural link
                    val updatedStats = stats.copy(
                        syncPercent = stats.syncPercent + 5.0f,
                        vesselStatus = "OVERCHARGED"
                    )
                    repository.updateStats(updatedStats)
                    repository.addLog("PRESSURE REMOVED: Neural Link stabilizer locked at ${updatedStats.syncPercent}%. Overcharged.")
                }
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

    // Draw the mystical Fate Thread
    fun drawFateThread(type: String) {
        if (fateDrawnToday.value) return
        fateDrawnToday.value = true
        fateType.value = type
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            when (type) {
                "GOLD" -> {
                    fateBonus.value = "+300 Gold & +150 AP"
                    val updatedStats = stats.copy(gold = stats.gold + 300, apPoints = stats.apPoints + 150)
                    repository.updateStats(updatedStats)
                    repository.addLog("FATE: Drew the Golden Thread of Glory! +300 Gold & +150 AP acquired.")
                }
                "SILVER" -> {
                    fateBonus.value = "+100 Shadow Mana Unit"
                    val updatedStats = stats.copy(manaStored = (stats.manaStored + 100).coerceAtMost(stats.manaGoal))
                    repository.updateStats(updatedStats)
                    repository.addLog("FATE: Drew the Silver Mystic Thread! Gained +100 Shadow Mana.")
                }
                "BLACK" -> {
                    fateBonus.value = "CURSE: Quota Doubled BUT +250 AP"
                    val updatedStats = stats.copy(apPoints = stats.apPoints + 250)
                    repository.updateStats(updatedStats)
                    // Double exercises goals
                    val list = exercises.value
                    list.forEach { task ->
                        repository.updateExercise(task.copy(target = task.target * 2))
                    }
                    repository.addLog("FATE: Drew the Cursed Black Thread! All targets doubled but earned +250 AP.")
                }
            }
        }
    }

    // Reset fate drawn status (useful when dailies are reset or upon action)
    fun resetFateDrawn() {
        fateDrawnToday.value = false
        fateType.value = null
        fateBonus.value = null
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
                val finalGold = (baseGold * multiplier).toInt()
                val finalAp = (baseAp * multiplier).toInt()

                val updatedStats = stats.copy(
                    gold = stats.gold + finalGold,
                    apPoints = stats.apPoints + finalAp,
                    manaStored = (stats.manaStored + 50).coerceAtMost(stats.manaGoal)
                )
                repository.updateStats(updatedStats)
                repository.addLog("GATE CLEAR: SLAIN [${dungeonBossName.value}] in ${dungeonMood.value} mood! Gained +$finalGold Gold, +$finalAp AP, +50 Mana.")

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

    fun extractShadow(shadowId: String) {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val shadowList = shadows.value
            val shadow = shadowList.find { it.id == shadowId } ?: return@launch

            if (shadow.level > 0) return@launch // Already unlocked

            // Check if user has enough manaStored
            if (stats.manaStored < shadow.manaCost) {
                repository.addLog("ARISE FAILED: Insufficient Shadow Mana! Requires ${shadow.manaCost} mana. Stored: ${stats.manaStored}")
                return@launch
            }

            // Deduct mana, unlock shadow
            val updatedStats = stats.copy(
                manaStored = stats.manaStored - shadow.manaCost,
                // On extraction, give them a static attribute boost matching their passiveStat!
                str = if (shadow.passiveStat == "str") stats.str + shadow.passiveAmount else stats.str,
                agi = if (shadow.passiveStat == "agi") stats.agi + shadow.passiveAmount else stats.agi,
                endurance = if (shadow.passiveStat == "end") stats.endurance + shadow.passiveAmount else stats.endurance,
                intl = if (shadow.passiveStat == "int") stats.intl + shadow.passiveAmount else stats.intl
            )
            val updatedShadow = shadow.copy(level = 1)

            repository.updateStats(updatedStats)
            repository.updateShadow(updatedShadow)
            repository.addLog("ARISE SUCCESS: Extracted shadow [${shadow.name}] (${shadow.arabicName})! Spent ${shadow.manaCost} Mana. Passive [${shadow.passiveStat.uppercase()} +${shadow.passiveAmount}] applied.")
        }
    }

    fun upgradeShadow(shadowId: String) {
        viewModelScope.launch {
            val stats = userStats.value ?: return@launch
            val shadowList = shadows.value
            val shadow = shadowList.find { it.id == shadowId } ?: return@launch

            if (shadow.level == 0) return@launch // Cannot upgrade locked shadow

            // Upgrade cost in Gold setup
            val currentCost = shadow.goldCost * shadow.level
            if (stats.gold < currentCost) {
                repository.addLog("EVOLUTION FAILED: Insufficient Gold! Requires $currentCost Gold. Stored: ${stats.gold}")
                return@launch
            }

            val incrementalStatBoost = 5 // Each upgrade adds 5 to the passive
            val updatedStats = stats.copy(
                gold = stats.gold - currentCost,
                str = if (shadow.passiveStat == "str") stats.str + incrementalStatBoost else stats.str,
                agi = if (shadow.passiveStat == "agi") stats.agi + incrementalStatBoost else stats.agi,
                endurance = if (shadow.passiveStat == "end") stats.endurance + incrementalStatBoost else stats.endurance,
                intl = if (shadow.passiveStat == "int") stats.intl + incrementalStatBoost else stats.intl
            )
            val updatedShadow = shadow.copy(
                level = shadow.level + 1,
                passiveAmount = shadow.passiveAmount + incrementalStatBoost
            )

            repository.updateStats(updatedStats)
            repository.updateShadow(updatedShadow)
            repository.addLog("EVOLUTION SUCCESS: Upgraded shadow [${shadow.name}] (${shadow.arabicName}) to Rank Level ${updatedShadow.level}! Spent $currentCost Gold. Extra [${shadow.passiveStat.uppercase()} +$incrementalStatBoost] registered.")
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
}
