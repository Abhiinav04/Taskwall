package com.abhinav.taskwall.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Task::class, Quote::class, SubTask::class], version = 3, exportSchema = false)
abstract class TaskWallDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun quoteDao(): QuoteDao

    companion object {
        @Volatile
        private var INSTANCE: TaskWallDatabase? = null

        fun getDatabase(context: Context): TaskWallDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskWallDatabase::class.java,
                    "taskwall_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
