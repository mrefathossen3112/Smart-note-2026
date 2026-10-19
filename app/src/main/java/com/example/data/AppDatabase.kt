package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.NoteDao
import com.example.data.dao.PackingDao
import com.example.data.dao.ScheduleDao
import com.example.data.dao.TripDao
import com.example.data.model.Note
import com.example.data.model.PackingItem
import com.example.data.model.ScheduleItem
import com.example.data.model.Trip
import com.example.data.templates.PackingTemplates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

import com.example.data.dao.ChatDao
import com.example.data.dao.VaultDao
import com.example.data.dao.WeatherDao
import com.example.data.model.ChatMessage
import com.example.data.model.VaultItem
import com.example.data.model.WeatherCache

@Database(
    entities = [
        Trip::class,
        ScheduleItem::class,
        PackingItem::class,
        Note::class,
        WeatherCache::class,
        ChatMessage::class,
        VaultItem::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun packingDao(): PackingDao
    abstract fun noteDao(): NoteDao
    abstract fun weatherDao(): WeatherDao
    abstract fun chatDao(): ChatDao
    abstract fun vaultDao(): VaultDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_offline_planner.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Pre-seed default data
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    seedDefaultData(database)
                }
            }
        }

        private suspend fun seedDefaultData(db: AppDatabase) {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()

            // Trip 1: Kyoto Japan Expedition (Active / Upcoming in 2 days)
            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, 2)
            cal.set(Calendar.HOUR_OF_DAY, 9)
            cal.set(Calendar.MINUTE, 0)
            val trip1Start = cal.timeInMillis

            cal.add(Calendar.DAY_OF_YEAR, 7)
            val trip1End = cal.timeInMillis

            val trip1Id = db.tripDao().insertTrip(
                Trip(
                    title = "Autumn in Kyoto & Osaka",
                    destination = "Kyoto, Japan",
                    startDate = trip1Start,
                    endDate = trip1End,
                    budget = 2400.0,
                    notes = "Ryokan reservation in Gion. JR Rail Pass activated at Haneda airport. Remember pocket WiFi pick up.",
                    category = "Vacation",
                    colorHex = "#0D9488",
                    status = "UPCOMING"
                )
            )

            // Schedules for Trip 1
            cal.timeInMillis = trip1Start
            db.scheduleDao().insertSchedules(
                listOf(
                    ScheduleItem(
                        tripId = trip1Id,
                        title = "Flight JL 042 (Departs Terminal 3)",
                        location = "International Airport",
                        timestamp = cal.timeInMillis,
                        category = "Transport",
                        notes = "Seat 14A. Carry-on baggage limit 10kg.",
                        cost = 850.0,
                        isCompleted = false,
                        reminderMinutesBefore = 120
                    ),
                    ScheduleItem(
                        tripId = trip1Id,
                        title = "Check-in at Machiya Ryokan",
                        location = "Gion District, Kyoto",
                        timestamp = cal.apply { add(Calendar.HOUR_OF_DAY, 8) }.timeInMillis,
                        category = "Lodging",
                        notes = "Traditional tatami suite with private onsen bath. Gate closes at 11 PM.",
                        cost = 320.0,
                        isCompleted = false,
                        reminderMinutesBefore = 30
                    ),
                    ScheduleItem(
                        tripId = trip1Id,
                        title = "Fushimi Inari Taisha Torii Hike",
                        location = "Fushimi Ward, Kyoto",
                        timestamp = cal.apply { add(Calendar.DAY_OF_YEAR, 1); set(Calendar.HOUR_OF_DAY, 7) }.timeInMillis,
                        category = "Sightseeing",
                        notes = "Early morning visit to avoid tourist crowds. Wear comfortable hiking shoes.",
                        cost = 0.0,
                        isCompleted = false,
                        reminderMinutesBefore = 45
                    ),
                    ScheduleItem(
                        tripId = trip1Id,
                        title = "Matcha Tasting & Kaiseki Dinner",
                        location = "Pontocho Alley, Kyoto",
                        timestamp = cal.apply { set(Calendar.HOUR_OF_DAY, 18); set(Calendar.MINUTE, 30) }.timeInMillis,
                        category = "Dining",
                        notes = "Table reserved under Alex. 8-course seasonal menu.",
                        cost = 140.0,
                        isCompleted = false,
                        reminderMinutesBefore = 30
                    ),
                    ScheduleItem(
                        tripId = trip1Id,
                        title = "Shinkansen Express to Shin-Osaka",
                        location = "Kyoto Station Platform 12",
                        timestamp = cal.apply { add(Calendar.DAY_OF_YEAR, 2); set(Calendar.HOUR_OF_DAY, 10) }.timeInMillis,
                        category = "Transport",
                        notes = "15-minute bullet train ride. Use JR Green Car pass.",
                        cost = 25.0,
                        isCompleted = false,
                        reminderMinutesBefore = 20
                    )
                )
            )

            // Seed Packing for Trip 1 using International template
            val intlTemplate = PackingTemplates.templates.first { it.id == "international" }
            val packingItems = PackingTemplates.toPackingItems(intlTemplate, trip1Id).toMutableList()
            // Mark a few as packed for demo
            val updatedPacking = packingItems.mapIndexed { index, item ->
                if (index < 4) item.copy(isPacked = true) else item
            }
            db.packingDao().insertPackingItems(updatedPacking)

            // Trip 2: Alpine Mountain Weekend
            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, 24)
            val trip2Start = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 3)
            val trip2End = cal.timeInMillis

            val trip2Id = db.tripDao().insertTrip(
                Trip(
                    title = "Highland Peak Trail & Lake Camping",
                    destination = "Interlaken, Switzerland",
                    startDate = trip2Start,
                    endDate = trip2End,
                    budget = 750.0,
                    notes = "Cabin rented at Lake Brienz. Bring trail poles and thermal fleece.",
                    category = "Adventure",
                    colorHex = "#2563EB",
                    status = "UPCOMING"
                )
            )

            val outdoorTemplate = PackingTemplates.templates.first { it.id == "outdoor" }
            db.packingDao().insertPackingItems(PackingTemplates.toPackingItems(outdoorTemplate, trip2Id))

            // Seed sample Notes
            db.noteDao().insertNotes(
                listOf(
                    Note(
                        tripId = trip1Id,
                        title = "Kyoto Food & Tea Recommendations",
                        content = "1. Gion Karyo for dinner kaiseki.\n2. Ippodo Tea Main Store near Kyoto Imperial Palace.\n3. Nishiki Market for fresh skewers and tamagoyaki.\n4. Arabica Coffee Arashiyama overlooking the river.",
                        category = "Places",
                        isPinned = true,
                        updatedAt = now
                    ),
                    Note(
                        tripId = trip1Id,
                        title = "Emergency Contacts & Embassy",
                        content = "Embassy 24h Assistance Line: +81 3-3224-5000\nTravel Insurance policy #99281-INT-TRV\nHotel Front Desk: +81 75-555-0199",
                        category = "Emergency",
                        isPinned = true,
                        updatedAt = now
                    ),
                    Note(
                        tripId = null,
                        title = "Travel Photography Packing Tip",
                        content = "Always charge extra camera batteries before flights. Keep all lithium batteries strictly in carry-on bag, never checked luggage.",
                        category = "Ideas",
                        isPinned = false,
                        updatedAt = now
                    )
                )
            )

            // Seed sample cached Weather for offline viewing
            db.weatherDao().insertWeather(
                WeatherCache(
                    locationKey = "Kyoto, Japan",
                    temperature = 22.5,
                    weatherCode = 1,
                    weatherCondition = "Mostly Sunny",
                    windSpeed = 8.5,
                    humidity = 58,
                    highTemp = 24.0,
                    lowTemp = 15.0,
                    smartSuggestion = "Pleasant & sunny! Perfect for temple walking. Pack comfortable shoes & sun protection.",
                    lastUpdated = now
                )
            )
            db.weatherDao().insertWeather(
                WeatherCache(
                    locationKey = "Interlaken, Switzerland",
                    temperature = 14.2,
                    weatherCode = 61,
                    weatherCondition = "Light Rain",
                    windSpeed = 12.0,
                    humidity = 78,
                    highTemp = 16.0,
                    lowTemp = 8.0,
                    smartSuggestion = "Cool with light showers. Bring a waterproof rain shell and warm thermal mid-layer.",
                    lastUpdated = now
                )
            )

            // Seed initial welcome message from Gemini Assistant
            db.chatDao().insertMessage(
                ChatMessage(
                    role = "model",
                    text = "Hello! I am your Gemini Travel Copilot 🌍. Ask me to plan itineraries, recommend packing lists for your destination's weather, suggest local cuisines, or give budget advice!",
                    timestamp = now
                )
            )

            // Seed initial Emergency Travel Vault items
            db.vaultDao().insertVaultItem(
                VaultItem(
                    title = "Primary Passport",
                    category = "PASSPORT",
                    documentNumber = "EP7849201",
                    details = "Issued by Immigration & Passport Authority. Valid for 10 years. Stored in hotel room safe deposit.",
                    isImportant = true
                )
            )
            db.vaultDao().insertVaultItem(
                VaultItem(
                    title = "Emergency Contact (Family ICE)",
                    category = "EMERGENCY_CONTACT",
                    documentNumber = "+1 (555) 349-2041",
                    details = "Brother • WhatsApp & Cellular • Blood Group: O Positive • Allergies: None",
                    isImportant = true
                )
            )
        }
    }
}
