package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserStats::class,
        ExerciseTask::class,
        Item::class,
        ActivityLog::class,
        ShadowSoldier::class
    ],
    version = 2,
    exportSchema = false
)
abstract class VesselDatabase : RoomDatabase() {
    abstract fun vesselDao(): VesselDao

    companion object {
        @Volatile
        private var INSTANCE: VesselDatabase? = null

        fun getDatabase(context: Context): VesselDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VesselDatabase::class.java,
                    "vessel_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
