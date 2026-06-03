package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import android.util.Log

class VesselRepository(private val vesselDao: VesselDao) {

    val userStats: Flow<UserStats?> = vesselDao.getUserStats()
    val exercises: Flow<List<ExerciseTask>> = vesselDao.getAllExercises()
    val items: Flow<List<Item>> = vesselDao.getAllItems()
    val logs: Flow<List<ActivityLog>> = vesselDao.getAllLogs()
    val shadows: Flow<List<ShadowSoldier>> = vesselDao.getAllShadows()
    val gates: Flow<List<Gate>> = vesselDao.getAllGates()
    val sideQuests: Flow<List<SideQuest>> = vesselDao.getAllSideQuests()

    suspend fun insertGates(gatesList: List<Gate>) {
        vesselDao.insertGates(gatesList)
    }

    suspend fun updateGate(gate: Gate) {
        vesselDao.updateGate(gate)
    }

    suspend fun clearGates() {
        vesselDao.clearGates()
    }

    suspend fun insertSideQuests(questsList: List<SideQuest>) {
        vesselDao.insertSideQuests(questsList)
    }

    suspend fun updateSideQuest(quest: SideQuest) {
        vesselDao.updateSideQuest(quest)
    }

    suspend fun clearSideQuests() {
        vesselDao.clearSideQuests()
    }

    suspend fun initializeDatabase() {
        val existingStats = vesselDao.getUserStatsDirect()
        if (existingStats == null) {
            Log.d("VesselRepository", "Seeding initial data...")
            
            // Seed UserStats matching images
            val defaultStats = UserStats(
                id = 1,
                username = "ALPJA",
                level = 1,
                xp = 1269,
                maxXp = 1000,
                gold = 1000,
                apPoints = 100,
                energy = 100,
                syncPercent = 1.0f,
                streak = 3,
                lives = 3,
                agi = 0,
                str = 0,
                endurance = 0,
                intl = 0,
                statPoints = 5,
                vesselStatus = "PRIME",
                manaStored = 0,
                manaGoal = 500,
                globalSync = true
            )
            vesselDao.insertUserStats(defaultStats)

            // Seed Exercises matching images
            val defaultExercises = listOf(
                ExerciseTask("pushups", "PUSH-UPS", "REPS", 10, 10, 200, 150, 25, true, "pushups"),
                ExerciseTask("squats", "SQUATS", "REPS", 15, 0, 200, 120, 20, false, "squats"),
                ExerciseTask("lunges", "LUNGES", "REPS", 10, 0, 200, 150, 25, false, "lunges"),
                ExerciseTask("burpees", "BURPEES", "REPS", 5, 0, 250, 200, 30, false, "burpees"),
                ExerciseTask("pullups", "PULL-UPS", "REPS", 3, 0, 300, 250, 40, false, "pullups"),
                ExerciseTask("walking", "WALKING", "KM", 2, 0, 150, 100, 20, false, "walking"),
                ExerciseTask("running", "RUNNING", "KM", 1, 0, 200, 150, 25, false, "running"),
                ExerciseTask("coldshower", "COLD SHOWER", "SEC", 30, 0, 100, 50, 15, false, "shower"),
                ExerciseTask("reading", "READING", "PAGES", 5, 0, 150, 100, 20, false, "reading"),
                ExerciseTask("meditation", "MEDITATION", "MIN", 5, 0, 150, 100, 20, false, "meditation")
            )
            vesselDao.insertExercises(defaultExercises)

            // Seed Shop items matching images
            val defaultItems = listOf(
                // AP Shop (8 items)
                Item("AP_01", "Emergency Shield", "درع الطوارئ", "AP", 800, "SHIELD", "Protects from a penalty once. Consumed automatically upon failure.", "حماية من العقوبة مرة واحدة. يُستهلك تلقائياً عند الفشل.", 0, false, 0, "E", 0, -1, 0L),
                Item("AP_02", "Healing Elixir", "ترياق الاستشفاء", "AP", 1200, "RECOVERY", "Skip the entire day without penalty. Receive only 50% quest points.", "تخطي اليوم كاملاً بدون عقاب. تحصل على 50% من نقاط المهام فقط.", 0, false, 0, "D", 0, -1, 0L),
                Item("AP_03", "Daily Booster", "بطاقة مضاعف يومي", "AP", 500, "DOUBLE_XP_24H", "Double all AP and XP earned for 24 hours.", "مضاعفة كل AP وXP المكتسبة لمدة 24 ساعة.", 0, false, 24, "E", 48, -1, 0L),
                Item("AP_04", "Weekly Booster", "بطاقة مضاعف أسبوعي", "AP", 2000, "DOUBLE_XP_7D", "Double all AP and XP earned for 7 days.", "مضاعفة كل AP وXP المكتسبة لمدة 7 أيام.", 0, false, 168, "C", 168, -1, 0L),
                Item("AP_05", "Quest Freeze", "تجميد المهام", "AP", 300, "FREEZE_QUESTS", "Delay quest deadlines by 3 hours.", "تجميد وتأخير المهام 3 ساعات.", 0, false, 3, "E", 24, -1, 0L),
                Item("AP_06", "Life Recovery", "استعادة حياة", "AP", 1500, "HEAL_LIFE", "Recover one lost heart/life.", "استعادة حياة وإرجاع قلب واحد مفقود.", 0, false, 0, "D", 72, -1, 0L),
                Item("AP_07", "Gate Key", "مفتاح بوابة", "AP", 600, "GATE_KEY", "Unlock an extra gate on the map.", "مفتاح لفتح بوابة إضافية على الخريطة.", 0, false, 0, "C", 48, -1, 0L),
                Item("AP_08", "Energy Pump", "ضخ الطاقة", "AP", 250, "REFUEL_ENERGY", "Instantly restore 3 gate entry energy points.", "ضخ الطاقة واستعادة 3 طاقات دخول للبوابات فوراً.", 0, false, 0, "E", 24, -1, 0L),

                // GOLD Shop (8 items)
                Item("GOLD_01", "Hunter's Sword", "سيف الصياد", "GOLD", 2000, "SWORD", "Double gate rewards (AP+XP) for 48 hours.", "مضاعفة مكافآت البوابات (AP+XP) لمدة 48 ساعة.", 0, false, 48, "D", 48, -1, 0L),
                Item("GOLD_02", "Treasure Map", "خريطة الكنز", "GOLD", 1500, "MAP", "Increase visible gates count by 50% for 48 hours.", "زيادة البوابات الظاهرة 50% لمدة 48 ساعة.", 0, false, 48, "C", 72, -1, 0L),
                Item("GOLD_03", "Loot Box", "صندوق الغنائم", "GOLD", 800, "LOOT_BOX", "Double reward of next gate completion (one-time).", "مضاعفة مكافأة البوابة التالية (مرة واحدة).", 0, false, 0, "D", 24, -1, 0L),
                Item("GOLD_04", "Dawn Extension", "تمديد الفجر", "GOLD", 600, "EXTEND_DAWN", "Extend dawn protocol quest time limit by 1 hour.", "تمديد وقت مهمة الفجر ساعة إضافية.", 0, false, 1, "D", 24, -1, 0L),
                Item("GOLD_05", "Points Magnet", "مغناطيس النقاط", "GOLD", 1000, "RECLAIM_AP", "Reclaim 50% of last AP points lost in penalty.", "استرداد 50% من آخر AP خسرتها في عقوبة.", 0, false, 0, "C", 72, -1, 0L),
                Item("GOLD_06", "Stealth Mask", "قناع التخفي", "GOLD", 500, "STEALTH_MASK", "Hide user stats from logs for 24 hours.", "قناع إخفاء الإحصائيات عن دفتر اليوميات 24 ساعة.", 0, false, 24, "E", 0, -1, 0L),
                Item("GOLD_07", "Shadow Evolve Stone", "حجر الترقية", "GOLD", 3000, "EVOLVE_STONE", "Upgrade one shadow soldier level instantly.", "ترقية ظل واحد مستوى كامل فوراً.", 0, false, 0, "B", 336, -1, 0L),
                Item("GOLD_08", "Courage Potion", "جرعة الشجاعة", "GOLD", 400, "BYPASS_LOCATION", "Bypass location requirements for one gate.", "إزالة شرط الموقع من بوابة واحدة وكسب الشجاعة.", 0, false, 0, "D", 48, -1, 0L),

                // Market Shop (8 items)
                Item("MARKET_01", "Fake Elixir", "ترياق مزيف", "MARKET", 200, "FAKE_POTION", "Skip today without penalty, but tomorrow's quotas double.", "تخطي اليوم بدون عقاب، لكن مهام الغد تضاعف.", 0, false, 0, "E", 0, -1, 0L),
                Item("MARKET_02", "Cursed Key", "مفتاح ملعون", "MARKET", 300, "CURSED_KEY", "Open high rank gate, but its reward is cut by 50%.", "فتح بوابة عالية الرتبة، لكن المكافأة 50%.", 0, false, 0, "D", 0, -1, 0L),
                Item("MARKET_03", "Cursed Elixir", "إكسير ملعون", "MARKET", 250, "CURSED_ELIXIR", "+20 to one attribute, -15 to another attribute.", "إكسير ملعون: +20 لإحصائية، -15 لأخرى.", 0, false, 0, "C", 0, -1, 0L),
                Item("MARKET_04", "Broken Shield", "درع مكسور", "MARKET", 150, "BROKEN_SHIELD", "Protects from penalty, but lose 1 life instantly.", "حماية من عقوبة، لكن تفقد حياة فوراً.", 0, false, 0, "E", 0, -1, 0L),
                Item("MARKET_05", "Cursed XP Booster", "بوستر XP ملعون", "MARKET", 200, "CURSED_XP", "Double XP for 24h + apply a random curse.", "مضاعف XP 24 ساعة + لعنة عشوائية غامضة.", 0, false, 24, "D", 0, -1, 0L),
                Item("MARKET_06", "Stolen Heart", "قلب مسروق", "MARKET", 350, "STOLEN_HEART", "Recover 1 life, but penalize streak by -3 days.", "استعادة حياة، لكن خصم 3 أيام من السلسلة.", 0, false, 0, "C", 0, -1, 0L),
                Item("MARKET_07", "Escape Charm", "تعويذة الهروب", "MARKET", 400, "ESCAPE_CHARM", "Escape gate immediately without penalty, but locked out for 24h.", "خروج فوري من بوابة بدون عقوبة، لكن تُطرد 24 ساعة.", 0, false, 24, "D", 0, -1, 0L),
                Item("MARKET_08", "Compass of Greed", "بوصلة الجشع", "MARKET", 500, "GREED_COMPASS", "Detect S-Rank gate, but reward suffers a 30% tax.", "كشف بوابة رتبة S، لكن المكافأة تخضع لضريبة 30%.", 0, false, 0, "B", 0, -1, 0L),

                // Abyss Shop (8 items)
                Item("ABYSS_01", "Annihilator Skills", "قدرة الإلغاء", "ABYSS", 500, "VOID_CANCEL", "Skip any penalty once every two weeks.", "تخطي أي عقوبة مرة كل أسبوعين.", 0, false, 336, "SSS", 336, -1, 0L),
                Item("ABYSS_02", "Sacrifice Skills", "قدرة التضحية", "ABYSS", 300, "VOID_SACRIFICE", "Trade 5000 AP for a full day of rest.", "استبدال 5000 AP بيوم راحة كامل.", 0, false, 168, "SSS", 168, -1, 0L),
                Item("ABYSS_03", "Abyss Inhabitant Title", "لقب ساكن الهاوية", "ABYSS", 1000, "ABYSS_TITLE", "Permanent title +5% power boost to all parameters.", "لقب أبدي +5% لجميع الإحصائيات.", 0, false, 0, "SSS", 0, 1, 0L),
                Item("ABYSS_04", "Abyss Void Shadow", "ظل العدم", "ABYSS", 2000, "VOID_SHADOW", "Summon a silent shadow soldier that doubles stamina.", "استدعاء ظل صامت يضاعف التحمل والقوة.", 0, false, 0, "SSS", 0, 1, 0L),
                Item("ABYSS_05", "Void Appearance App Skins", "كسوة الهاوية", "ABYSS", 1500, "VOID_SKIN", "Unlocks permanent deep dark crimson void theme interface.", "واجهة سوداء بكسوة وتشققات حمراء دائمية.", 0, false, 0, "SSS", 0, 1, 0L),
                Item("ABYSS_06", "Fortitude Jewel", "جوهرة الصمود", "ABYSS", 800, "VOID_FORTITUDE", "Reduce lockouts/temporary bans from 72h to 24h.", "تقليل الطرد المؤقت والعقوبات من 72 إلى 24 ساعة.", 0, false, 0, "SSS", 720, -1, 0L),
                Item("ABYSS_07", "Grand Gate Key", "مفتاح البوابة الكبرى", "ABYSS", 1200, "GRAND_GATE_KEY", "Summon extra guardian boss. Defeating them doubles Shards.", "استدعاء حارس إضافي في المغامرات. هزيمته = ضعف الشظايا.", 0, false, 0, "SSS", 168, -1, 0L),
                Item("ABYSS_08", "Eye of Darkness", "عين الظلام", "ABYSS", 600, "EYE_OF_DARKNESS", "Reveal any gate guardian weakness for 24h.", "كشف نقطة ضعف أي حارس بوابات لمدة 24 ساعة.", 0, false, 24, "SSS", 48, -1, 0L)
            )
            vesselDao.insertItems(defaultItems)

            val defaultShadows = listOf(
                ShadowSoldier(
                    id = "iron",
                    name = "Iron",
                    arabicName = "آيرون",
                    rank = "المدرب القاسي",
                    rankArabic = "المدرب القاسي",
                    manaCost = 500, // base Ap Cost
                    goldCost = 1000, // Next level gold/Ap cost or dynamic calculation
                    level = 0,
                    passiveStat = "str",
                    passiveAmount = 10,
                    description = "إكمال 50 مهمة بدنية",
                    arabicDescription = "تحدي الصباح: أكمل تمارينك قبل 8 مساءً بنجاح لمضاعفة نقاط القوة بالكامل. الفشل يزيد عبء الغد بـ 50 عدة بيربي.",
                    loyalty = 20,
                    isActive = false,
                    isSacrificed = false,
                    sacrificeReturnTime = 0L,
                    isMerged = false,
                    originalMergedIds = null
                ),
                ShadowSoldier(
                    id = "igris",
                    name = "Igris",
                    arabicName = "إيغريس",
                    rank = "الاستراتيجي",
                    rankArabic = "الاستراتيجي",
                    manaCost = 600,
                    goldCost = 1200,
                    level = 0,
                    passiveStat = "intl",
                    passiveAmount = 15,
                    description = "إكمال 30 مهمة عقلية",
                    arabicDescription = "مهمة استراتيجية أسبوعية كبرى تستهدف أضعف إحصائية لديك لرفع سوية الوعاء تدريجياً.",
                    loyalty = 20,
                    isActive = false,
                    isSacrificed = false,
                    sacrificeReturnTime = 0L,
                    isMerged = false,
                    originalMergedIds = null
                ),
                ShadowSoldier(
                    id = "tank",
                    name = "Tank",
                    arabicName = "تانك",
                    rank = "الحارس الصامت",
                    rankArabic = "الحارس الصامت",
                    manaCost = 700,
                    goldCost = 1400,
                    level = 0,
                    passiveStat = "endurance",
                    passiveAmount = 12,
                    description = "21 يوم بدون عقوبة",
                    arabicDescription = "دفاع صامت منيع: يمنحك بطاقة درع طوارئ وتخطي مجانية كل أسبوعين كدعم وقائي دائم.",
                    loyalty = 20,
                    isActive = false,
                    isSacrificed = false,
                    sacrificeReturnTime = 0L,
                    isMerged = false,
                    originalMergedIds = null
                ),
                ShadowSoldier(
                    id = "fang",
                    name = "Fang",
                    arabicName = "فانغ",
                    rank = "عداء الرياح",
                    rankArabic = "عداء الرياح",
                    manaCost = 800,
                    goldCost = 1600,
                    level = 0,
                    passiveStat = "agi",
                    passiveAmount = 14,
                    description = "الحصول على لقب \"صياد الظلال\"",
                    arabicDescription = "عداء الرياح: احتساب تمارين الجري الخارجي بمعدل سحري مضاعف 1.5x لـ AGI.",
                    loyalty = 20,
                    isActive = false,
                    isSacrificed = false,
                    sacrificeReturnTime = 0L,
                    isMerged = false,
                    originalMergedIds = null
                ),
                ShadowSoldier(
                    id = "jima",
                    name = "Jima",
                    arabicName = "جيما",
                    rank = "الحكيم",
                    rankArabic = "الحكيم",
                    manaCost = 900,
                    goldCost = 1800,
                    level = 0,
                    passiveStat = "intl",
                    passiveAmount = 16,
                    description = "الحصول على لقب \"عقل متقد\"",
                    arabicDescription = "اقتباس نثري حكيم وتدبري يومي: اتباع إرشادات وبصيرة جيما يمنح مضاعفاً إضافياً لـ INT طوال اليوم.",
                    loyalty = 20,
                    isActive = false,
                    isSacrificed = false,
                    sacrificeReturnTime = 0L,
                    isMerged = false,
                    originalMergedIds = null
                ),
                ShadowSoldier(
                    id = "beru",
                    name = "Beru",
                    arabicName = "بيرو",
                    rank = "القاتل المخلص",
                    rankArabic = "القاتل المخلص",
                    manaCost = 1000,
                    goldCost = 2000,
                    level = 0,
                    passiveStat = "agi",
                    passiveAmount = 18,
                    description = "هزيمة 20 حارس بوابة",
                    arabicDescription = "الفتك والمبايعة المطلقة: فرصة 10% لتنفيذ ضربة حرجة وإكمال فوري مع مضاعفة غنائم البوابة 5x تلقائياً.",
                    loyalty = 20,
                    isActive = false,
                    isSacrificed = false,
                    sacrificeReturnTime = 0L,
                    isMerged = false,
                    originalMergedIds = null
                ),
                ShadowSoldier(
                    id = "bellion",
                    name = "Bellion",
                    arabicName = "بيليون",
                    rank = "الصياد الأعلى",
                    rankArabic = "الصياد الأعلى",
                    manaCost = 1500,
                    goldCost = 3000,
                    level = 0,
                    passiveStat = "str",
                    passiveAmount = 25,
                    description = "الوصول إلى مستوى 50",
                    arabicDescription = "السيادة الإدارية المطلقة: يتيح للوعاء استبدال مهمة يومية صعبة بأخرى ملائمة مرة واحدة كل أسبوع بلا شروط.",
                    loyalty = 20,
                    isActive = false,
                    isSacrificed = false,
                    sacrificeReturnTime = 0L,
                    isMerged = false,
                    originalMergedIds = null
                )
            )
            vesselDao.insertShadows(defaultShadows)

            // Seed initial activity log
            vesselDao.insertLog(ActivityLog(message = "System initialized. Welcome, ALPJA. Link Stable."))
        }
    }

    suspend fun updateStats(stats: UserStats) {
        vesselDao.updateUserStats(stats)
    }

    suspend fun updateShadow(shadow: ShadowSoldier) {
        vesselDao.updateShadow(shadow)
    }

    suspend fun updateExercise(task: ExerciseTask) {
        vesselDao.updateExercise(task)
    }

    suspend fun updateItem(item: Item) {
        vesselDao.updateItem(item)
    }

    suspend fun addLog(message: String) {
        vesselDao.insertLog(ActivityLog(message = message))
    }

    suspend fun clearLogs() {
        vesselDao.clearLogs()
    }

    suspend fun getJournalEntry(dateId: String): DailyJournalEntry? {
        return vesselDao.getJournalEntry(dateId)
    }

    suspend fun insertJournalEntry(entry: DailyJournalEntry) {
        vesselDao.insertJournalEntry(entry)
    }

    fun getRecentJournalEntriesFlow(): Flow<List<DailyJournalEntry>> {
        return vesselDao.getRecentJournalEntriesFlow()
    }

    suspend fun getLast14JournalEntries(): List<DailyJournalEntry> {
        return vesselDao.getLast14JournalEntries()
    }
}
