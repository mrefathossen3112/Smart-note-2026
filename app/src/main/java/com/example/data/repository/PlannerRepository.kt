package com.example.data.repository

import com.example.data.dao.ChatDao
import com.example.data.dao.NoteDao
import com.example.data.dao.PackingDao
import com.example.data.dao.ScheduleDao
import com.example.data.dao.TripDao
import com.example.data.dao.VaultDao
import com.example.data.dao.WeatherDao
import com.example.data.model.ChatMessage
import com.example.data.model.Note
import com.example.data.model.PackingItem
import com.example.data.model.ScheduleItem
import com.example.data.model.Trip
import com.example.data.model.VaultItem
import com.example.data.model.WeatherCache
import kotlinx.coroutines.flow.Flow

class PlannerRepository(
    private val tripDao: TripDao,
    private val scheduleDao: ScheduleDao,
    private val packingDao: PackingDao,
    private val noteDao: NoteDao,
    private val weatherDao: WeatherDao,
    private val chatDao: ChatDao,
    private val vaultDao: VaultDao
) {
    // Trips
    val allTrips: Flow<List<Trip>> = tripDao.getAllTrips()

    fun getTripById(id: Long): Flow<Trip?> = tripDao.getTripById(id)
    suspend fun getTripByIdOnce(id: Long): Trip? = tripDao.getTripByIdOnce(id)

    suspend fun insertTrip(trip: Trip): Long = tripDao.insertTrip(trip)
    suspend fun updateTrip(trip: Trip) = tripDao.updateTrip(trip)
    suspend fun deleteTrip(trip: Trip) = tripDao.deleteTrip(trip)
    suspend fun deleteTripById(id: Long) = tripDao.deleteTripById(id)

    // Schedules
    val allSchedules: Flow<List<ScheduleItem>> = scheduleDao.getAllSchedules()

    fun getSchedulesForTrip(tripId: Long): Flow<List<ScheduleItem>> =
        scheduleDao.getSchedulesForTrip(tripId)

    fun getUpcomingSchedules(fromTime: Long, limit: Int = 10): Flow<List<ScheduleItem>> =
        scheduleDao.getUpcomingSchedules(fromTime, limit)

    fun getTotalSpentForTrip(tripId: Long): Flow<Double?> =
        scheduleDao.getTotalSpentForTrip(tripId)

    suspend fun insertSchedule(item: ScheduleItem): Long = scheduleDao.insertSchedule(item)
    suspend fun updateSchedule(item: ScheduleItem) = scheduleDao.updateSchedule(item)
    suspend fun deleteSchedule(item: ScheduleItem) = scheduleDao.deleteSchedule(item)
    suspend fun deleteScheduleById(id: Long) = scheduleDao.deleteScheduleById(id)
    suspend fun setScheduleCompleted(id: Long, completed: Boolean) =
        scheduleDao.setCompleted(id, completed)

    // Packing
    val allPackingItems: Flow<List<PackingItem>> = packingDao.getAllPackingItems()

    fun getPackingItemsForTrip(tripId: Long): Flow<List<PackingItem>> =
        packingDao.getPackingItemsForTrip(tripId)

    suspend fun insertPackingItem(item: PackingItem): Long = packingDao.insertPackingItem(item)
    suspend fun insertPackingItems(items: List<PackingItem>): List<Long> =
        packingDao.insertPackingItems(items)

    suspend fun updatePackingItem(item: PackingItem) = packingDao.updatePackingItem(item)
    suspend fun updatePackingQuantity(id: Long, quantity: Int) = packingDao.updateQuantity(id, quantity)
    suspend fun deletePackingItem(item: PackingItem) = packingDao.deletePackingItem(item)
    suspend fun deletePackingItemById(id: Long) = packingDao.deletePackingItemById(id)
    suspend fun setPackingItemPacked(id: Long, packed: Boolean) =
        packingDao.setPacked(id, packed)
    suspend fun setAllPackedForTrip(tripId: Long, packed: Boolean) =
        packingDao.setAllPackedForTrip(tripId, packed)
    suspend fun deletePackedItemsForTrip(tripId: Long) =
        packingDao.deletePackedItemsForTrip(tripId)
    suspend fun clearPackingForTrip(tripId: Long) = packingDao.clearPackingForTrip(tripId)

    // Notes
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    fun getNotesForTrip(tripId: Long): Flow<List<Note>> = noteDao.getNotesForTrip(tripId)
    fun getNoteById(id: Long): Flow<Note?> = noteDao.getNoteById(id)

    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)
    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)
    suspend fun setNotePinned(id: Long, pinned: Boolean) = noteDao.setPinned(id, pinned)

    // Weather Cache (Offline & Live)
    fun getWeather(locationKey: String): Flow<WeatherCache?> = weatherDao.getWeather(locationKey)
    val allCachedWeather: Flow<List<WeatherCache>> = weatherDao.getAllCachedWeather()
    suspend fun cacheWeather(weather: WeatherCache) = weatherDao.insertWeather(weather)

    // Chatbot History
    val chatMessages: Flow<List<ChatMessage>> = chatDao.getAllMessages()
    suspend fun insertChatMessage(role: String, text: String): Long {
        return chatDao.insertMessage(ChatMessage(role = role, text = text))
    }
    suspend fun clearChatHistory() = chatDao.clearHistory()

    // Emergency Travel Vault
    val allVaultItems: Flow<List<VaultItem>> = vaultDao.getAllVaultItems()
    suspend fun insertVaultItem(item: VaultItem) = vaultDao.insertVaultItem(item)
    suspend fun updateVaultItem(item: VaultItem) = vaultDao.updateVaultItem(item)
    suspend fun deleteVaultItem(item: VaultItem) = vaultDao.deleteVaultItem(item)
    suspend fun deleteVaultItemById(id: Long) = vaultDao.deleteVaultItemById(id)
}
