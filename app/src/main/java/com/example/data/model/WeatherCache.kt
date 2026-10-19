package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCache(
    @PrimaryKey val locationKey: String, // Destination name or "current_location"
    val temperature: Double,
    val weatherCode: Int,
    val weatherCondition: String,
    val windSpeed: Double,
    val humidity: Int,
    val highTemp: Double,
    val lowTemp: Double,
    val smartSuggestion: String,
    val lastUpdated: Long = System.currentTimeMillis()
)
