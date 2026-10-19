package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PackingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PackingDao {
    @Query("SELECT * FROM packing_items WHERE tripId = :tripId ORDER BY category ASC, name ASC")
    fun getPackingItemsForTrip(tripId: Long): Flow<List<PackingItem>>

    @Query("SELECT * FROM packing_items ORDER BY tripId ASC, category ASC")
    fun getAllPackingItems(): Flow<List<PackingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackingItem(item: PackingItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackingItems(items: List<PackingItem>): List<Long>

    @Update
    suspend fun updatePackingItem(item: PackingItem)

    @Delete
    suspend fun deletePackingItem(item: PackingItem)

    @Query("DELETE FROM packing_items WHERE id = :id")
    suspend fun deletePackingItemById(id: Long)

    @Query("UPDATE packing_items SET isPacked = :packed WHERE id = :id")
    suspend fun setPacked(id: Long, packed: Boolean)

    @Query("UPDATE packing_items SET isPacked = :packed WHERE tripId = :tripId")
    suspend fun setAllPackedForTrip(tripId: Long, packed: Boolean)

    @Query("UPDATE packing_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: Long, quantity: Int)

    @Query("DELETE FROM packing_items WHERE tripId = :tripId AND isPacked = 1")
    suspend fun deletePackedItemsForTrip(tripId: Long)

    @Query("DELETE FROM packing_items WHERE tripId = :tripId")
    suspend fun clearPackingForTrip(tripId: Long)
}
