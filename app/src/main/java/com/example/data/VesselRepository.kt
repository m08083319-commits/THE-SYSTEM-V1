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
                // AP Tab
                Item("potion_recovery", "Healing Potion", "ترياق الاستشفاء", "AP", 800, "RECOVERY", "Skip today's tasks in full without penalty", "تخطي مهام اليوم بالكامل بدون عقوبة", 0, false, 0),
                Item("shield_penalty", "Penalty Shield", "درع العقوبة", "AP", 400, "SHIELD", "Cancel one active system penalty", "إلغاء عقوبة نظام نشطة واحدة", 0, false, 0),
                
                // GOLD Tab
                Item("hunter_sword", "Hunter's Sword", "سيف الصياد", "GOLD", 2000, "SWORD", "Double dungeon search and combat rewards", "ضاعفة مكافآت الزنزانات", 0, false, 48),
                Item("dungeon_map", "Dungeon Map", "خريطة المغارات", "GOLD", 1500, "MAP", "Double visible dungeon active gates count", "ضاعفة عدد الزنزانات المرئية", 0, false, 48),
                
                // MARKET Tab (which has Curative/Fake potions with time counters)
                Item("potion_fake", "Fake Potion", "ترياق مزيف", "MARKET", 100, "FAKE", "Skip today's tasks but tomorrow's count increases by 5", "تخطي اليوم ولكن مهام الغد تزيد بـ 5", 0, false, 0),
                
                // FATE Tab
                Item("potion_pressure", "Pressure Antidote", "ترياق الضغط", "FATE", 500, "PRESSURE", "Reduce curse stress level probability by 50%", "تقليل فرصة لعنة الضغط بـ 50%", 0, false, 168)
            )
            vesselDao.insertItems(defaultItems)

            val defaultShadows = listOf(
                ShadowSoldier(
                    id = "igris",
                    name = "Igris",
                    arabicName = "إيغريس",
                    rank = "Elite Knight",
                    rankArabic = "قائد الفرسان الأحمر",
                    manaCost = 150,
                    goldCost = 1200,
                    level = 0, // 0 = Locked
                    passiveStat = "str",
                    passiveAmount = 10,
                    description = "A noble knight of red blood. Fierce warrior devoted to the Monarch.",
                    arabicDescription = "فارس نبيل ذو دماء حمراء ملتهبة. مقاتل شرس يكرس كل قوته لخدمة الملك."
                ),
                ShadowSoldier(
                    id = "beru",
                    name = "Beru",
                    arabicName = "بيرو",
                    rank = "Shadow General",
                    rankArabic = "جنرال النمل الظلي",
                    manaCost = 300,
                    goldCost = 2500,
                    level = 0,
                    passiveStat = "agi",
                    passiveAmount = 15,
                    description = "The Ant King reborn in shadows. Possesses hyper-sonic speed and high lethality.",
                    arabicDescription = "ملك النمل المعاد إحياؤه من الظلال. يمتلك سرعة خارقة تفوق الصوت وفتكاً هائلاً."
                ),
                ShadowSoldier(
                    id = "tusk",
                    name = "Tusk",
                    arabicName = "تاسك",
                    rank = "High Shaman",
                    rankArabic = "الكاهن الأوك العظيم",
                    manaCost = 200,
                    goldCost = 1800,
                    level = 0,
                    passiveStat = "int",
                    passiveAmount = 12,
                    description = "Lord of Orc Spellcasters. Amplifies the Monarch's magical capacity.",
                    arabicDescription = "سيد سحرة الأورك والتعاويذ. يُضخّم السعة السحرية والذكاء العصبي للملك."
                ),
                ShadowSoldier(
                    id = "iron",
                    name = "Iron",
                    arabicName = "آيرون",
                    rank = "Shield Knight",
                    rankArabic = "الفارس المدرع الضخم",
                    manaCost = 100,
                    goldCost = 1000,
                    level = 0,
                    passiveStat = "end",
                    passiveAmount = 8,
                    description = "An impenetrable shield. Aggressively absorbs damage to protect the vessel.",
                    arabicDescription = "درع منيع غير قابل للاختراق. يمتص الضرر والضغط البدني بشراسة لحماية الصياد."
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
}
