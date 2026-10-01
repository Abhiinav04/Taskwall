package com.abhinav.taskwall.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val targetDate: Long? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)
