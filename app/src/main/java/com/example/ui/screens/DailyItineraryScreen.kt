package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleItem
import com.example.data.model.Trip
import com.example.util.DateUtils
import com.example.util.NavigationHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class DayItineraryGroup(
    val dayIndex: Int,
    val dayDateMs: Long,
    val dayTitle: String,
    val daySubtitle: String,
    val isToday: Boolean,
    val items: List<ScheduleItem>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyItineraryScreen(
    trip: Trip,
    schedules: List<ScheduleItem>,
    onBack: () -> Unit,
    onAddSchedule: () -> Unit,
    onEditSchedule: (ScheduleItem) -> Unit,
    onDeleteSchedule: (Long) -> Unit,
    onToggleScheduleCompleted: (Long, Boolean) -> Unit,
    onTestNotification: (ScheduleItem) -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val tripColor = try {
        Color(android.graphics.Color.parseColor(trip.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    // Group items into days spanning the entire trip duration
    val dayGroups = remember(trip.startDate, trip.endDate, schedules) {
        computeDayItineraryGroups(trip.startDate, trip.endDate, schedules)
    }

    // Map to track expanded / collapsed state for each day (default: first 3 days expanded)
    val expandedDayMap = remember(dayGroups.size) {
        mutableStateMapOf<Int, Boolean>().apply {
            dayGroups.forEachIndexed { index, group ->
                // Expand if it's the first 2 days, or if it is today, or if it has items
                this[group.dayIndex] = index < 3 || group.isToday
            }
        }
    }

    val allExpanded by remember(expandedDayMap.values.toList()) {
        derivedStateOf {
            dayGroups.isNotEmpty() && dayGroups.all { expandedDayMap[it.dayIndex] == true }
        }
    }

    val totalActivities = schedules.size
    val completedActivities = schedules.count { it.isCompleted }
    val progress = if (totalActivities > 0) completedActivities.toFloat() / totalActivities.toFloat() else 0f
    val totalCost = schedules.sumOf { it.cost }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Daily Itinerary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${trip.title} • ${trip.destination}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("daily_itinerary_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val target = !allExpanded
                            dayGroups.forEach { group ->
                                expandedDayMap[group.dayIndex] = target
                            }
                        },
                        modifier = Modifier.testTag("toggle_all_days_button")
                    ) {
                        Icon(
                            imageVector = if (allExpanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                            contentDescription = if (allExpanded) "Collapse All" else "Expand All",
                            tint = tripColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSchedule,
                containerColor = tripColor,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_schedule_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Activity")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("daily_itinerary_lazy_column"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Hero Summary Card
            item {
                ItineraryHeroCard(
                    trip = trip,
                    tripColor = tripColor,
                    totalActivities = totalActivities,
                    completedActivities = completedActivities,
                    progress = progress,
                    totalCost = totalCost,
                    totalDays = dayGroups.size,
                    allExpanded = allExpanded,
                    onToggleAll = {
                        val target = !allExpanded
                        dayGroups.forEach { group ->
                            expandedDayMap[group.dayIndex] = target
                        }
                    }
                )
            }

            if (dayGroups.isEmpty()) {
                item {
                    EmptyItineraryState(onAddSchedule = onAddSchedule)
                }
            } else {
                items(dayGroups, key = { it.dayIndex }) { group ->
                    val isExpanded = expandedDayMap[group.dayIndex] ?: false

                    CollapsibleDaySection(
                        group = group,
                        tripColor = tripColor,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedDayMap[group.dayIndex] = !isExpanded
                        },
                        onAddActivity = onAddSchedule,
                        onToggleCompleted = onToggleScheduleCompleted,
                        onEditSchedule = onEditSchedule,
                        onDeleteSchedule = onDeleteSchedule,
                        onTestNotification = onTestNotification,
                        context = context
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(88.dp)) }
        }
    }
}

@Composable
private fun ItineraryHeroCard(
    trip: Trip,
    tripColor: Color,
    totalActivities: Int,
    completedActivities: Int,
    progress: Float,
    totalCost: Double,
    totalDays: Int,
    allExpanded: Boolean,
    onToggleAll: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("itinerary_hero_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = DateUtils.formatDateRange(trip.startDate, trip.endDate),
                        style = MaterialTheme.typography.labelMedium,
                        color = tripColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$totalDays-Day Travel Schedule",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = tripColor.copy(alpha = 0.12f),
                    modifier = Modifier.clickable { onToggleAll() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (allExpanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                            contentDescription = null,
                            tint = tripColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (allExpanded) "Collapse All" else "Expand All",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = tripColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Activities Progress",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$completedActivities of $totalActivities completed (${(progress * 100).toInt()}%)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = tripColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = tripColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Stat Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickStatPill(
                    label = "Total Days",
                    value = "$totalDays Days",
                    modifier = Modifier.weight(1f)
                )
                QuickStatPill(
                    label = "Activities",
                    value = "$totalActivities Items",
                    modifier = Modifier.weight(1f)
                )
                QuickStatPill(
                    label = "Planned Cost",
                    value = "$${String.format(Locale.US, "%.0f", totalCost)}",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CollapsibleDaySection(
    group: DayItineraryGroup,
    tripColor: Color,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAddActivity: () -> Unit,
    onToggleCompleted: (Long, Boolean) -> Unit,
    onEditSchedule: (ScheduleItem) -> Unit,
    onDeleteSchedule: (Long) -> Unit,
    onTestNotification: (ScheduleItem) -> Unit,
    context: Context
) {
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrow_rotation"
    )

    val completedCount = group.items.count { it.isCompleted }
    val dayCost = group.items.sumOf { it.cost }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("day_card_${group.dayIndex}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (group.isToday) tripColor.copy(alpha = 0.04f)
            else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder(
            enabled = true
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Clickable Day Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(14.dp)
                    .testTag("day_header_${group.dayIndex}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Day Number Badge
                    Surface(
                        shape = CircleShape,
                        color = if (group.isToday) tripColor else tripColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "D${group.dayIndex}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (group.isToday) Color.White else tripColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = group.dayTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (group.isToday) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = tripColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "TODAY",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = group.daySubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (group.items.isNotEmpty()) {
                                Text(
                                    text = " • ${group.items.size} activities",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = tripColor
                                )
                                if (dayCost > 0) {
                                    Text(
                                        text = " • $${String.format(Locale.US, "%.0f", dayCost)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (group.items.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = if (completedCount == group.items.size) Color(0xFF10B981).copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "$completedCount/${group.items.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (completedCount == group.items.size) Color(0xFF10B981)
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier
                            .rotate(rotationState)
                            .size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Animated Collapsible Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 14.dp)
                ) {
                    if (group.items.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No activities planned for this day yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = onAddActivity,
                                    colors = ButtonDefaults.textButtonColors(contentColor = tripColor)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Activity for ${group.dayTitle}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Sorted activities by time
                        val sortedItems = remember(group.items) {
                            group.items.sortedBy { it.timestamp }
                        }

                        sortedItems.forEachIndexed { idx, item ->
                            val isLast = idx == sortedItems.lastIndex
                            TimelineActivityRow(
                                item = item,
                                tripColor = tripColor,
                                isLast = isLast,
                                onToggleCompleted = { onToggleCompleted(item.id, !item.isCompleted) },
                                onEdit = { onEditSchedule(item) },
                                onDelete = { onDeleteSchedule(item.id) },
                                onTestNotification = { onTestNotification(item) },
                                context = context
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineActivityRow(
    item: ScheduleItem,
    tripColor: Color,
    isLast: Boolean,
    onToggleCompleted: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTestNotification: () -> Unit,
    context: Context
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("activity_row_${item.id}"),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline Dot & Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (item.isCompleted) Color(0xFF10B981) else tripColor,
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onToggleCompleted() }
            ) {
                if (item.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(90.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Activity Card
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isLast) 0.dp else 10.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (item.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = tripColor.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = tripColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = DateUtils.formatTime(item.timestamp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = tripColor
                                )
                            }
                        }

                        if (item.cost > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", item.cost)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Checkbox
                    Checkbox(
                        checked = item.isCompleted,
                        onCheckedChange = { onToggleCompleted() },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )

                if (item.location.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            NavigationHelper.openMapLocation(context, item.location)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (item.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onTestNotification,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Remind me",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (item.location.isNotBlank()) {
                        IconButton(
                            onClick = { NavigationHelper.openTurnByTurnNavigation(context, item.location) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Navigate",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit activity",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete activity",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyItineraryState(onAddSchedule: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Activities Scheduled Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Plan your daily timetable, flights, dining, museum tours, and excursions.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAddSchedule) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add First Activity")
            }
        }
    }
}

/**
 * Computes calendar day groups for the trip, matching schedule items to their respective days.
 */
private fun computeDayItineraryGroups(
    startDateMs: Long,
    endDateMs: Long,
    schedules: List<ScheduleItem>
): List<DayItineraryGroup> {
    val totalDays = DateUtils.getDaysDuration(startDateMs, endDateMs)
    val dayHeaderFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
    val daySubtitleFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

    val groups = mutableListOf<DayItineraryGroup>()
    val cal = Calendar.getInstance().apply {
        timeInMillis = startDateMs
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val maxDaysToGenerate = if (totalDays in 1..60) totalDays else 1

    for (dayIdx in 1..maxDaysToGenerate) {
        val currentDayStart = cal.timeInMillis
        val currentDayEnd = currentDayStart + TimeUnit.DAYS.toMillis(1) - 1

        val itemsForThisDay = schedules.filter { item ->
            item.timestamp in currentDayStart..currentDayEnd || DateUtils.isSameDay(item.timestamp, currentDayStart)
        }

        val dateObj = Date(currentDayStart)
        val dayTitle = "Day $dayIdx • ${dayHeaderFormat.format(dateObj)}"
        val daySubtitle = daySubtitleFormat.format(dateObj)
        val isToday = DateUtils.isToday(currentDayStart)

        groups.add(
            DayItineraryGroup(
                dayIndex = dayIdx,
                dayDateMs = currentDayStart,
                dayTitle = dayTitle,
                daySubtitle = daySubtitle,
                isToday = isToday,
                items = itemsForThisDay
            )
        )

        cal.add(Calendar.DAY_OF_YEAR, 1)
    }

    // Also collect any schedules that might fall outside the initial range
    val coveredIds = groups.flatMap { it.items.map { item -> item.id } }.toSet()
    val orphaned = schedules.filter { it.id !in coveredIds }
    if (orphaned.isNotEmpty()) {
        val orphanGroup = DayItineraryGroup(
            dayIndex = groups.size + 1,
            dayDateMs = System.currentTimeMillis(),
            dayTitle = "Other Scheduled Activities",
            daySubtitle = "Dates outside main trip range",
            isToday = false,
            items = orphaned
        )
        groups.add(orphanGroup)
    }

    return groups
}
