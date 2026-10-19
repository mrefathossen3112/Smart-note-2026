package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val destination: String,
    val startDate: Long,
    val endDate: Long,
    val budget: Double = 0.0,
    val notes: String = "",
    val category: String = "Vacation", // Vacation, Business, Adventure, Roadtrip, Family, Solo, Weekend
    val colorHex: String = "#0D9488",
    val status: String = "UPCOMING", // UPCOMING, ACTIVE, COMPLETED
    val createdAt: Long = System.currentTimeMillis()
)
