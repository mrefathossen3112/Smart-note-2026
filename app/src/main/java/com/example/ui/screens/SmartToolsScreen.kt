package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Trip
import com.example.data.model.VaultItem
import com.example.data.model.WeatherCache
import com.example.ui.screens.tools.CurrencyConverterSection
import com.example.ui.screens.tools.EmergencyVaultSection
import com.example.ui.screens.tools.PhrasebookSection
import com.example.ui.screens.tools.SunTrackerSection
import com.example.util.CompassData
import com.example.util.DateUtils
import com.example.util.LocationHelper
import com.example.util.NavigationHelper
import com.example.util.SunTimes
import com.example.util.UserLocation
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SmartToolsScreen(
    isOnline: Boolean,
    currentLocation: UserLocation?,
    compassData: CompassData,
    cachedWeatherList: List<WeatherCache>,
    trips: List<Trip>,
    isWeatherRefreshing: Boolean,
    onRefreshWeatherForCity: (String) -> Unit,
    onRefreshCurrentGpsWeather: () -> Unit,
    onRequestLocationTracking: () -> Unit,
    onOpenGeminiChat: (String?) -> Unit = {},
    onOpenLiveVoice: (String?) -> Unit = {},
    sunTimes: SunTimes? = null,
    isRatesRefreshing: Boolean = false,
    onRefreshRates: () -> Unit = {},
    vaultItems: List<VaultItem> = emptyList(),
    onAddVaultItem: () -> Unit = {},
    onDeleteVaultItem: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedToolTab by remember { mutableIntStateOf(0) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                onRequestLocationTracking()
            }
        }
    )

    val toolTabs = listOf(
        "Compass & GPS",
        "Weather Hub",
        "AI Copilot",
        "Currency",
        "Sun & Daylight",
        "Phrasebook",
        "Emergency SOS & Vault",
        "Navigation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Online / Offline Status Bar
        ConnectivityStatusPill(isOnline = isOnline)

        Spacer(modifier = Modifier.height(10.dp))

        // Scrollable Tool Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedToolTab,
            containerColor = MaterialTheme.colorScheme.surface,
            edgePadding = 0.dp
        ) {
            toolTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedToolTab == index,
                    onClick = { selectedToolTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedToolTab == index) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedToolTab) {
            0 -> CompassAndGpsSection(
                compassData = compassData,
                currentLocation = currentLocation,
                onEnableLocation = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )
            1 -> WeatherHubSection(
                isOnline = isOnline,
                cachedWeatherList = cachedWeatherList,
                trips = trips,
                currentLocation = currentLocation,
                isRefreshing = isWeatherRefreshing,
                onRefreshCity = onRefreshWeatherForCity,
                onRefreshGpsWeather = onRefreshCurrentGpsWeather
            )
            2 -> GeminiCopilotToolsSection(
                isOnline = isOnline,
                trips = trips,
                onOpenChat = onOpenGeminiChat,
                onOpenLiveVoice = onOpenLiveVoice
            )
            3 -> CurrencyConverterSection(
                isOnline = isOnline,
                isRefreshing = isRatesRefreshing,
                onRefreshRates = onRefreshRates
            )
            4 -> SunTrackerSection(
                sunTimes = sunTimes ?: com.example.util.SunCalculator.calculate(
                    currentLocation?.latitude ?: 35.0116,
                    currentLocation?.longitude ?: 135.7681
                ),
                currentLocation = currentLocation
            )
            5 -> PhrasebookSection()
            6 -> EmergencyVaultSection(
                vaultItems = vaultItems,
                onAddVaultItem = onAddVaultItem,
                onDeleteVaultItem = onDeleteVaultItem
            )
            7 -> NavigationHubSection(
                trips = trips,
                currentLocation = currentLocation
            )
        }
    }
}

// ---------------- STATUS PILL ----------------
@Composable
fun ConnectivityStatusPill(isOnline: Boolean) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isOnline) Color(0xFF10B981).copy(alpha = 0.15f)
        else Color(0xFFF59E0B).copy(alpha = 0.18f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = if (isOnline) Color(0xFF047857) else Color(0xFFB45309),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isOnline) "Connected Online" else "Offline Mode Active",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnline) Color(0xFF047857) else Color(0xFFB45309)
                    )
                    Text(
                        text = if (isOnline) "Live weather updates & geocoding active" else "Running 100% on local SQLite cache",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ---------------- COMPASS & GPS SECTION ----------------
@Composable
fun CompassAndGpsSection(
    compassData: CompassData,
    currentLocation: UserLocation?,
    onEnableLocation: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Digital Compass Dial
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("compass_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hardware Compass Sensor",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${compassData.azimuthDegrees.toInt()}° ${compassData.cardinalDirection}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Compass Dial Canvas
                    Box(
                        modifier = Modifier.size(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CompassDial(azimuth = compassData.azimuthDegrees)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Pitch", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${compassData.pitchDegrees.toInt()}°", fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Roll", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${compassData.rollDegrees.toInt()}°", fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sensor State", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Active", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                }
            }
        }

        // GPS Telemetry Card
        item {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GPS Location & Telemetry", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        if (currentLocation != null) {
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("GPS", "${currentLocation.latitude}, ${currentLocation.longitude}")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Coordinates copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Coordinates", modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (currentLocation == null) {
                        Text(
                            text = "GPS coordinates not available. Grant location permissions to track altitude, speed, and exact coordinates.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = onEnableLocation) {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enable GPS Access")
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Latitude", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(String.format("%.5f°", currentLocation.latitude), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            }
                            Column {
                                Text("Longitude", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(String.format("%.5f°", currentLocation.longitude), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Altitude", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${currentLocation.altitudeMeters.toInt()} meters", fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("Speed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${currentLocation.speedKmh.toInt()} km/h", fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("Accuracy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("±${currentLocation.accuracyMeters.toInt()}m", fontWeight = FontWeight.SemiBold, color = Color(0xFF10B981))
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun CompassDial(azimuth: Float) {
    val animatedRotation by animateFloatAsState(
        targetValue = -azimuth,
        animationSpec = tween(durationMillis = 150),
        label = "compassRotation"
    )

    Canvas(modifier = Modifier.size(210.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2 - 12.dp.toPx()

        // Outer ring
        drawCircle(
            color = Color(0xFF1E293B),
            radius = radius + 6.dp.toPx(),
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw ticks and labels rotated
        val cardinalLabels = listOf(
            0 to "N", 30 to "•", 60 to "•", 90 to "E",
            120 to "•", 150 to "•", 180 to "S", 210 to "•",
            240 to "•", 270 to "W", 300 to "•", 330 to "•"
        )

        cardinalLabels.forEach { (deg, label) ->
            val angleRad = Math.toRadians((deg + animatedRotation - 90).toDouble())
            val tickStart = Offset(
                center.x + (radius - 10.dp.toPx()) * cos(angleRad).toFloat(),
                center.y + (radius - 10.dp.toPx()) * sin(angleRad).toFloat()
            )
            val tickEnd = Offset(
                center.x + radius * cos(angleRad).toFloat(),
                center.y + radius * sin(angleRad).toFloat()
            )

            val tickColor = if (deg == 0) Color(0xFFEF4444) else Color(0xFF94A3B8)
            drawLine(
                color = tickColor,
                start = tickStart,
                end = tickEnd,
                strokeWidth = if (deg % 90 == 0) 3.dp.toPx() else 1.5.dp.toPx()
            )

            if (label != "•") {
                val labelPos = Offset(
                    center.x + (radius - 24.dp.toPx()) * cos(angleRad).toFloat(),
                    center.y + (radius - 24.dp.toPx()) * sin(angleRad).toFloat() + 5.dp.toPx()
                )
                val paint = android.graphics.Paint().apply {
                    color = if (deg == 0) android.graphics.Color.parseColor("#EF4444") else android.graphics.Color.parseColor("#E2E8F0")
                    textSize = 34f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }
                drawContext.canvas.nativeCanvas.drawText(label, labelPos.x, labelPos.y, paint)
            }
        }

        // Draw North Needle (Red triangle pointing up)
        val northNeedle = Path().apply {
            moveTo(center.x, center.y - radius + 32.dp.toPx())
            lineTo(center.x - 10.dp.toPx(), center.y)
            lineTo(center.x + 10.dp.toPx(), center.y)
            close()
        }
        drawPath(northNeedle, Color(0xFFEF4444))

        // Draw South Needle (Silver triangle pointing down)
        val southNeedle = Path().apply {
            moveTo(center.x, center.y + radius - 32.dp.toPx())
            lineTo(center.x - 10.dp.toPx(), center.y)
            lineTo(center.x + 10.dp.toPx(), center.y)
            close()
        }
        drawPath(southNeedle, Color(0xFF94A3B8))

        // Center hub
        drawCircle(color = Color(0xFF0F172A), radius = 8.dp.toPx(), center = center)
        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = center)
    }
}

// ---------------- WEATHER HUB SECTION ----------------
@Composable
fun WeatherHubSection(
    isOnline: Boolean,
    cachedWeatherList: List<WeatherCache>,
    trips: List<Trip>,
    currentLocation: UserLocation?,
    isRefreshing: Boolean,
    onRefreshCity: (String) -> Unit,
    onRefreshGpsWeather: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Destination Weather", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        if (isOnline) "Live forecasts with smart travel advice" else "Offline mode • Stored forecasts available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isOnline) {
                    FilledTonalButton(
                        onClick = onRefreshGpsWeather,
                        enabled = currentLocation != null && !isRefreshing
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Refresh GPS")
                    }
                }
            }
        }

        if (isRefreshing) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        if (cachedWeatherList.isEmpty()) {
            item {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Weather Data Cached", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Connect online to automatically fetch weather for your trips.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(cachedWeatherList) { weather ->
                WeatherCard(
                    weather = weather,
                    isOnline = isOnline,
                    onRefresh = { onRefreshCity(weather.locationKey) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun WeatherCard(
    weather: WeatherCache,
    isOnline: Boolean,
    onRefresh: () -> Unit
) {
    val weatherIcon = when {
        weather.weatherCondition.contains("Rain", ignoreCase = true) -> Icons.Default.Grain
        weather.weatherCondition.contains("Snow", ignoreCase = true) -> Icons.Default.Grain
        weather.weatherCondition.contains("Cloud", ignoreCase = true) -> Icons.Default.Cloud
        else -> Icons.Default.LightMode
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = weather.locationKey,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${weather.weatherCondition} • Updated ${DateUtils.formatMonthDay(weather.lastUpdated)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${weather.temperature.toInt()}°C",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (isOnline) {
                        IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weather Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Thermostat, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("H: ${weather.highTemp.toInt()}° / L: ${weather.lowTemp.toInt()}°", style = MaterialTheme.typography.bodySmall)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Air, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${weather.windSpeed.toInt()} km/h", style = MaterialTheme.typography.bodySmall)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${weather.humidity}% humidity", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Smart Travel Suggestion Banner
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = weather.smartSuggestion,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

// ---------------- NAVIGATION HUB SECTION ----------------
@Composable
fun NavigationHubSection(
    trips: List<Trip>,
    currentLocation: UserLocation?
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Turn-by-Turn Navigation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Launch maps and GPS routing for your planned destinations.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (trips.isEmpty()) {
            item {
                Text("No trips available to navigate to.", style = MaterialTheme.typography.bodySmall)
            }
        } else {
            items(trips) { trip ->
                val distanceText = if (currentLocation != null) {
                    // Estimate distance if coordinates or city known
                    "GPS Active"
                } else null

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(trip.destination, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            }
                            Text(trip.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Button(
                            onClick = {
                                NavigationHelper.openTurnByTurnNavigation(context, trip.destination)
                            }
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Navigate")
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// ---------------- GEMINI COPILOT SECTION ----------------
@Composable
fun GeminiCopilotToolsSection(
    isOnline: Boolean,
    trips: List<Trip>,
    onOpenChat: (String?) -> Unit,
    onOpenLiveVoice: (String?) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tools_gemini_hub_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Gemini Travel Copilot",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = if (isOnline) "gemini-3.8-live & 3.5-flash Active" else "Offline AI Knowledge Engine",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isOnline) Color(0xFF10B981) else Color(0xFFF59E0B)
                        ) {
                            Text(
                                text = if (isOnline) "ONLINE" else "OFFLINE",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Your intelligent travel copilot with real-time Live Voice conversation (gemini-3.8-live) and text planning. Speak directly to get instant spoken responses or chat for itineraries, packing, and navigation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Voice Call Action Button (gemini-3.8-live)
                    Button(
                        onClick = { onOpenLiveVoice(trips.firstOrNull()?.let { "${it.title} (${it.destination})" }) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tools_open_gemini_live_voice_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Gemini Live Voice Call", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { onOpenChat(trips.firstOrNull()?.let { "${it.title} (${it.destination})" }) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tools_open_gemini_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Travel Copilot Chat")
                    }
                }
            }
        }

        item {
            Text(
                text = "Instant Travel Prompts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap any query to launch the assistant with pre-loaded guidance:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val quickTopics = listOf(
            Triple("🎒 Smart Packing Strategy", "What essentials should I pack for my upcoming trip?", "Packing Advice"),
            Triple("🗺️ 3-Day Itinerary Builder", "Create an optimized 3-day itinerary with cultural stops and dining", "Itinerary Planning"),
            Triple("🍜 Local Food & Street Eats", "What local street foods and culinary customs should I experience?", "Food Guide"),
            Triple("💰 Budgeting & Cost Saving", "How should I structure my travel budget and avoid hidden tourist fees?", "Budget Tips"),
            Triple("🆘 Offline Emergency Advice", "What are the most crucial emergency guidelines when traveling offline?", "Safety Rules")
        )

        items(quickTopics) { (title, prompt, _) ->
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenChat(prompt) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = prompt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

