package com.example.pilab.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [InjectionHistoryEntity::class, SecurityReportEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PilabDatabase : RoomDatabase() {
    abstract fun injectionHistoryDao(): InjectionHistoryDao

    companion object {
        @Volatile
        private var instance: PilabDatabase? = null

        fun getInstance(context: Context): PilabDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PilabDatabase::class.java,
                    "pilab.db"
                ).build().also { instance = it }
            }
        }
    }
}
