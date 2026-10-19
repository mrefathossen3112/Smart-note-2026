package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.Note
import com.example.data.model.PackingItem
import com.example.data.model.ScheduleItem
import com.example.data.model.Trip

object TripExportHelper {

    fun generateTripSummary(
        trip: Trip,
        schedules: List<ScheduleItem>,
        packing: List<PackingItem>,
        notes: List<Note>,
        totalSpent: Double
    ): String {
        val sb = StringBuilder()
        sb.append("🌍 TRIP PLAN: ${trip.title.uppercase()}\n")
        sb.append("📍 Destination: ${trip.destination}\n")
        sb.append("📅 Dates: ${DateUtils.formatDateRange(trip.startDate, trip.endDate)}\n")
        sb.append("⏳ Status: ${DateUtils.getCountdownText(trip.startDate, trip.endDate)}\n")
        sb.append("💰 Budget: $${String.format("%.2f", trip.budget)} | Spent: $${String.format("%.2f", totalSpent)}\n\n")

        if (trip.notes.isNotBlank()) {
            sb.append("📝 Trip Overview:\n${trip.notes}\n\n")
        }

        if (schedules.isNotEmpty()) {
            sb.append("🗓️ ITINERARY & ACTIVITIES (${schedules.size}):\n")
            val sorted = schedules.sortedBy { it.timestamp }
            sorted.forEach { item ->
                val timeStr = DateUtils.formatDateTime(item.timestamp)
                val check = if (item.isCompleted) "✅" else "▫️"
                sb.append("$check [$timeStr] ${item.title}")
                if (item.location.isNotBlank()) sb.append(" @ ${item.location}")
                if (item.cost > 0) sb.append(" ($${String.format("%.2f", item.cost)})")
                sb.append("\n")
            }
            sb.append("\n")
        }

        if (packing.isNotEmpty()) {
            val packedCount = packing.count { it.isPacked }
            sb.append("🎒 PACKING CHECKLIST ($packedCount/${packing.size} packed):\n")
            packing.forEach { item ->
                val check = if (item.isPacked) "☑️" else "🔲"
                val star = if (item.isEssential) " ⭐" else ""
                sb.append("$check ${item.name} (x${item.quantity})$star\n")
            }
            sb.append("\n")
        }

        if (notes.isNotEmpty()) {
            sb.append("📌 TRAVEL NOTES & JOURNAL:\n")
            notes.forEach { note ->
                sb.append("• ${note.title}: ${note.content}\n")
            }
            sb.append("\n")
        }

        sb.append("Generated with Smart Offline Planner ✈️")
        return sb.toString()
    }

    fun shareTripSummary(
        context: Context,
        trip: Trip,
        schedules: List<ScheduleItem>,
        packing: List<PackingItem>,
        notes: List<Note>,
        totalSpent: Double
    ) {
        val summary = generateTripSummary(trip, schedules, packing, notes, totalSpent)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Trip Plan: ${trip.title}")
            putExtra(Intent.EXTRA_TEXT, summary)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Trip Plan")
        context.startActivity(shareIntent)
    }
}
