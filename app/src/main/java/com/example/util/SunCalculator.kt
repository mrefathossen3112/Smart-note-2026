package com.example.util

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

data class SunTimes(
    val sunriseFormatted: String,
    val sunsetFormatted: String,
    val solarNoonFormatted: String,
    val goldenHourMorning: String,
    val goldenHourEvening: String,
    val dayLengthHours: Double,
    val statusText: String,
    val isDaytime: Boolean
)

object SunCalculator {

    private const val ZENITH_OFFICIAL = 90.833 // Official civil sunrise/sunset with atmospheric refraction
    private const val ZENITH_GOLDEN_HOUR = 84.0 // Golden hour top threshold (+6 degrees elevation)
    private const val ZENITH_CIVIL_TWILIGHT = 96.0 // Civil twilight bottom threshold (-6 degrees elevation)

    fun calculate(
        latitude: Double,
        longitude: Double,
        calendar: Calendar = Calendar.getInstance()
    ): SunTimes {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val timeZoneOffsetHours = calendar.timeZone.getOffset(calendar.timeInMillis) / 3600000.0

        val sunriseHours = calculateSunEventHours(latitude, longitude, year, month, day, timeZoneOffsetHours, ZENITH_OFFICIAL, isSunrise = true)
        val sunsetHours = calculateSunEventHours(latitude, longitude, year, month, day, timeZoneOffsetHours, ZENITH_OFFICIAL, isSunrise = false)

        val goldenMorningEnd = calculateSunEventHours(latitude, longitude, year, month, day, timeZoneOffsetHours, ZENITH_GOLDEN_HOUR, isSunrise = true)
        val goldenEveningStart = calculateSunEventHours(latitude, longitude, year, month, day, timeZoneOffsetHours, ZENITH_GOLDEN_HOUR, isSunrise = false)

        val solarNoonHours = if (sunriseHours != null && sunsetHours != null) (sunriseHours + sunsetHours) / 2.0 else 12.0
        val dayLength = if (sunriseHours != null && sunsetHours != null) sunsetHours - sunriseHours else 12.0

        val currentHour = calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60.0
        val isDay = sunriseHours != null && sunsetHours != null && currentHour in sunriseHours..sunsetHours

        val status = when {
            sunriseHours == null || sunsetHours == null -> "Polar Region"
            currentHour in (sunsetHours - 1.0)..sunsetHours -> "Evening Golden Hour 🌅"
            currentHour in sunriseHours..(sunriseHours + 1.0) -> "Morning Golden Hour 🌄"
            currentHour < sunriseHours -> "Night • Before Sunrise"
            currentHour > sunsetHours -> "Night • After Sunset"
            else -> "Daytime ☀️"
        }

        return SunTimes(
            sunriseFormatted = formatHours(sunriseHours ?: 6.0),
            sunsetFormatted = formatHours(sunsetHours ?: 18.0),
            solarNoonFormatted = formatHours(solarNoonHours),
            goldenHourMorning = "${formatHours(sunriseHours ?: 6.0)} - ${formatHours(goldenMorningEnd ?: 7.0)}",
            goldenHourEvening = "${formatHours(goldenEveningStart ?: 17.0)} - ${formatHours(sunsetHours ?: 18.0)}",
            dayLengthHours = dayLength,
            statusText = status,
            isDaytime = isDay
        )
    }

    private fun calculateSunEventHours(
        latitude: Double,
        longitude: Double,
        year: Int,
        month: Int,
        day: Int,
        timeZoneOffsetHours: Double,
        zenith: Double,
        isSunrise: Boolean
    ): Double? {
        val n1 = floor(275.0 * month / 9.0)
        val n2 = floor((month + 9.0) / 12.0)
        val n3 = 1.0 + floor((year - 4.0 * floor(year / 4.0) + 2.0) / 3.0)
        val dayOfYear = n1 - (n2 * n3) + day - 30.0

        val lngHour = longitude / 15.0
        val t = if (isSunrise) {
            dayOfYear + ((6.0 - lngHour) / 24.0)
        } else {
            dayOfYear + ((18.0 - lngHour) / 24.0)
        }

        val m = Math.toRadians((0.9856 * t) - 3.289)
        var l = Math.toDegrees(m) + (1.916 * sin(m)) + (0.020 * sin(2.0 * m)) + 282.634
        l = normalizeDegrees(l)

        val lRad = Math.toRadians(l)
        var ra = Math.toDegrees(atan(0.91764 * tan(lRad)))
        ra = normalizeDegrees(ra)

        val lQuadrant = floor(l / 90.0) * 90.0
        val raQuadrant = floor(ra / 90.0) * 90.0
        ra += (lQuadrant - raQuadrant)
        ra /= 15.0

        val sinDec = 0.39782 * sin(lRad)
        val cosDec = cos(asin(sinDec))

        val cosH = (cos(Math.toRadians(zenith)) - (sinDec * sin(Math.toRadians(latitude)))) /
                (cosDec * cos(Math.toRadians(latitude)))

        if (cosH > 1.0 || cosH < -1.0) {
            return null // Sun does not rise or set on this day at this latitude
        }

        val h = if (isSunrise) {
            360.0 - Math.toDegrees(acos(cosH))
        } else {
            Math.toDegrees(acos(cosH))
        } / 15.0

        val localMeanTime = h + ra - (0.06571 * t) - 6.622
        val utcTime = localMeanTime - lngHour
        var localTime = utcTime + timeZoneOffsetHours

        while (localTime < 0.0) localTime += 24.0
        while (localTime >= 24.0) localTime -= 24.0

        return localTime
    }

    private fun normalizeDegrees(deg: Double): Double {
        var d = deg % 360.0
        if (d < 0.0) d += 360.0
        return d
    }

    private fun formatHours(hoursDouble: Double): String {
        val totalMinutes = (hoursDouble * 60).toInt()
        var hours = (totalMinutes / 60) % 24
        val minutes = totalMinutes % 60
        val amPm = if (hours >= 12) "PM" else "AM"
        if (hours == 0) hours = 12
        else if (hours > 12) hours -= 12
        return String.format("%d:%02d %s", hours, minutes, amPm)
    }
}
