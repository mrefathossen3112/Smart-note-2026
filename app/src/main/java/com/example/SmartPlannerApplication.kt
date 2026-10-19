package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.AppDatabase
import com.example.data.repository.PlannerRepository

class SmartPlannerApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: PlannerRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getDatabase(this)
        repository = PlannerRepository(
            tripDao = database.tripDao(),
            scheduleDao = database.scheduleDao(),
            packingDao = database.packingDao(),
            noteDao = database.noteDao(),
            weatherDao = database.weatherDao(),
            chatDao = database.chatDao(),
            vaultDao = database.vaultDao()
        )
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Trip Schedule & Reminders"
            val descriptionText = "Notifications for upcoming trip activities and schedules"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "trip_schedules_channel"
        lateinit var instance: SmartPlannerApplication
            private set
    }
}
