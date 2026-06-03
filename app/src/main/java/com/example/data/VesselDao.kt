package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VesselDao {
    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    fun getUserStats(): Flow<UserStats?>

    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    suspend fun getUserStatsDirect(): UserStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStats)

    @Update
    suspend fun updateUserStats(stats: UserStats)

    @Query("SELECT * FROM exercise_tasks")
    fun getAllExercises(): Flow<List<ExerciseTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(tasks: List<ExerciseTask>)

    @Update
    suspend fun updateExercise(task: ExerciseTask)

    @Query("SELECT * FROM items")
    fun getAllItems(): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<Item>)

    @Update
    suspend fun updateItem(item: Item)

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 50")
    fun getAllLogs(): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog)

    @Query("DELETE FROM activity_logs")
    suspend fun clearLogs()

    @Query("SELECT * FROM shadow_soldiers")
    fun getAllShadows(): Flow<List<ShadowSoldier>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShadows(shadows: List<ShadowSoldier>)

    @Update
    suspend fun updateShadow(shadow: ShadowSoldier)

    // Gates
    @Query("SELECT * FROM gates")
    fun getAllGates(): Flow<List<Gate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGates(gates: List<Gate>)

    @Update
    suspend fun updateGate(gate: Gate)

    @Query("DELETE FROM gates")
    suspend fun clearGates()

    // Side Quests
    @Query("SELECT * FROM side_quests")
    fun getAllSideQuests(): Flow<List<SideQuest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSideQuests(quests: List<SideQuest>)

    @Update
    suspend fun updateSideQuest(quest: SideQuest)

    @Query("DELETE FROM side_quests")
    suspend fun clearSideQuests()

    @Query("SELECT * FROM daily_journal WHERE id = :dateId")
    suspend fun getJournalEntry(dateId: String): DailyJournalEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: DailyJournalEntry)

    @Query("SELECT * FROM daily_journal ORDER BY dateMillis DESC LIMIT 30")
    fun getRecentJournalEntriesFlow(): Flow<List<DailyJournalEntry>>

    @Query("SELECT * FROM daily_journal ORDER BY dateMillis DESC LIMIT 14")
    suspend fun getLast14JournalEntries(): List<DailyJournalEntry>
}
