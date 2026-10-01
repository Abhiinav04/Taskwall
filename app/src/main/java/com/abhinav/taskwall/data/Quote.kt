package com.abhinav.taskwall.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class Quote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val author: String? = null,
    val source: String? = null,
    val topic: String? = null,
    val lastShownAt: Long? = null,
    val isFavorite: Boolean = false
)
