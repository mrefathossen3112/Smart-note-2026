package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ScheduleItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules WHERE tripId = :tripId ORDER BY timestamp ASC")
    fun getSchedulesForTrip(tripId: Long): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedules ORDER BY timestamp ASC")
    fun getAllSchedules(): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedules WHERE timestamp >= :fromTime ORDER BY timestamp ASC LIMIT :limit")
    fun getUpcomingSchedules(fromTime: Long, limit: Int = 10): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): ScheduleItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(item: ScheduleItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(items: List<ScheduleItem>): List<Long>

    @Update
    suspend fun updateSchedule(item: ScheduleItem)

    @Delete
    suspend fun deleteSchedule(item: ScheduleItem)

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Long)

    @Query("UPDATE schedules SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean)

    @Query("SELECT SUM(cost) FROM schedules WHERE tripId = :tripId")
    fun getTotalSpentForTrip(tripId: Long): Flow<Double?>
}
