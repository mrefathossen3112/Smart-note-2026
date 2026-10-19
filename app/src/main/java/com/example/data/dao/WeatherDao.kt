package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.WeatherCache
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE locationKey = :locationKey LIMIT 1")
    fun getWeather(locationKey: String): Flow<WeatherCache?>

    @Query("SELECT * FROM weather_cache WHERE locationKey = :locationKey LIMIT 1")
    suspend fun getWeatherOnce(locationKey: String): WeatherCache?

    @Query("SELECT * FROM weather_cache ORDER BY lastUpdated DESC")
    fun getAllCachedWeather(): Flow<List<WeatherCache>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherCache)

    @Query("DELETE FROM weather_cache WHERE locationKey = :locationKey")
    suspend fun deleteWeather(locationKey: String)
}
