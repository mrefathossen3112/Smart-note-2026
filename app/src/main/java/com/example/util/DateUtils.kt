package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    private val monthDayFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    private val dayOfWeekFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    fun formatMonthDay(timestamp: Long): String {
        return monthDayFormat.format(Date(timestamp))
    }

    fun formatDayOfWeek(timestamp: Long): String {
        return dayOfWeekFormat.format(Date(timestamp))
    }

    fun formatDateRange(startMs: Long, endMs: Long): String {
        val startCal = Calendar.getInstance().apply { timeInMillis = startMs }
        val endCal = Calendar.getInstance().apply { timeInMillis = endMs }

        return if (startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR)) {
            "${formatMonthDay(startMs)} – ${formatMonthDay(endMs)}, ${startCal.get(Calendar.YEAR)}"
        } else {
            "${formatDate(startMs)} – ${formatDate(endMs)}"
        }
    }

    fun getDaysDuration(startMs: Long, endMs: Long): Int {
        val diff = endMs - startMs
        val days = TimeUnit.MILLISECONDS.toDays(diff).toInt() + 1
        return if (days < 1) 1 else days
    }

    fun getCountdownText(startMs: Long, endMs: Long): String {
        val now = System.currentTimeMillis()
        if (now in startMs..endMs) {
            val daysLeft = TimeUnit.MILLISECONDS.toDays(endMs - now).toInt() + 1
            return if (daysLeft == 1) "Active (Last Day!)" else "Active ($daysLeft days left)"
        }
        if (now > endMs) {
            return "Trip Completed"
        }
        val daysUntil = TimeUnit.MILLISECONDS.toDays(startMs - now).toInt()
        return when {
            daysUntil == 0 -> "Starts Today!"
            daysUntil == 1 -> "Tomorrow!"
            else -> "In $daysUntil days"
        }
    }

    fun isSameDay(t1: Long, t2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    fun isToday(timestamp: Long): Boolean {
        return isSameDay(timestamp, System.currentTimeMillis())
    }
}
