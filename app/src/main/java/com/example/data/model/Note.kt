package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long? = null,
    val title: String,
    val content: String,
    val category: String = "General", // General, Ideas, Important, Budget, Places, Emergency
    val isPinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
