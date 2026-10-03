package com.abhinav.taskwall.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Task::class, Quote::class, SubTask::class], version = 4, exportSchema = false)
abstract class TaskWallDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun quoteDao(): QuoteDao

    companion object {
        @Volatile
        private var INSTANCE: TaskWallDatabase? = null

        fun getDatabase(context: Context): TaskWallDatabase {
            return INSTANCE ?: synchronized(this) {
                val MIGRATION_3_4 = object : Migration(3, 4) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL("ALTER TABLE tasks ADD COLUMN showOnWallpaper INTEGER NOT NULL DEFAULT 1")
                    }
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskWallDatabase::class.java,
                    "taskwall_database"
                )
                .addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
