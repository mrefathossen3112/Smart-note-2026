package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId")]
)
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val title: String,
    val location: String = "",
    val timestamp: Long,
    val category: String = "Activity", // Transport, Lodging, Sightseeing, Dining, Activity, Shopping, Meeting, Other
    val notes: String = "",
    val cost: Double = 0.0,
    val isCompleted: Boolean = false,
    val reminderMinutesBefore: Int = 30
)
