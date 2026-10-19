package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.WeatherCache
import com.example.data.templates.PackingTemplates
import com.example.util.CompassData
import com.example.util.DateUtils
import com.example.util.LocationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Smart Planner", appName)
    }

    @Test
    fun `date range calculation is formatted accurately`() {
        val startMs = 1730000000000L
        val endMs = startMs + (3 * 86400000L)
        val duration = DateUtils.getDaysDuration(startMs, endMs)
        assertTrue(duration >= 3)
    }

    @Test
    fun `packing templates are populated`() {
        val templates = PackingTemplates.templates
        assertTrue(templates.isNotEmpty())
        val weekend = templates.find { it.id == "weekend" }
        assertNotNull(weekend)
        assertTrue(weekend!!.items.isNotEmpty())
    }

    @Test
    fun `weather cache model stores offline forecast`() {
        val weather = WeatherCache(
            locationKey = "Kyoto, Japan",
            temperature = 22.0,
            weatherCode = 1,
            weatherCondition = "Sunny",
            windSpeed = 5.0,
            humidity = 50,
            highTemp = 25.0,
            lowTemp = 18.0,
            smartSuggestion = "Sunny weather"
        )
        assertEquals("Kyoto, Japan", weather.locationKey)
        assertEquals(22.0, weather.temperature, 0.01)
    }

    @Test
    fun `gps distance calculation computes accurately`() {
        // Paris (48.8566, 2.3522) to London (51.5074, -0.1278) ~343 km
        val distance = LocationHelper.calculateDistanceKm(48.8566, 2.3522, 51.5074, -0.1278)
        assertTrue(distance > 300 && distance < 400)
    }
}
