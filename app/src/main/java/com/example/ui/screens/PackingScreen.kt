package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PackingItem
import com.example.data.model.Trip
import com.example.data.templates.PackingTemplate
import com.example.data.templates.PackingTemplates

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackingScreen(
    trips: List<Trip>,
    allPackingItems: List<PackingItem>,
    onTogglePacked: (Long, Boolean) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onAddItem: (tripId: Long) -> Unit,
    onApplyTemplate: (tripId: Long, templateId: String) -> Unit
) {
    var selectedTripId by remember(trips) {
        mutableStateOf(trips.firstOrNull()?.id)
    }
    var topTabMode by remember { mutableIntStateOf(0) } // 0 = Trip Checklist, 1 = Master Templates
    var selectedCategory by remember { mutableStateOf("ALL") }
    var tripDropdownExpanded by remember { mutableStateOf(false) }

    val currentTrip = trips.find { it.id == selectedTripId }
    val tripPackingItems = allPackingItems.filter { it.tripId == selectedTripId }

    val categories = listOf("ALL", "Documents", "Clothing", "Electronics", "Toiletries", "Health", "Gear", "Miscellaneous")

    val filteredItems = tripPackingItems.filter {
        selectedCategory == "ALL" || it.category == selectedCategory
    }

    val totalCount = tripPackingItems.size
    val packedCount = tripPackingItems.count { it.isPacked }
    val progress = if (totalCount > 0) packedCount.toFloat() / totalCount.toFloat() else 0f

    var previewTemplate by remember { mutableStateOf<PackingTemplate?>(null) }

    Scaffold(
        floatingActionButton = {
            if (topTabMode == 0 && selectedTripId != null) {
                FloatingActionButton(
                    onClick = { selectedTripId?.let { onAddItem(it) } },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("packing_fab_add")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Main mode tab: Trip Checklists vs Master Templates
            TabRow(
                selectedTabIndex = topTabMode,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = topTabMode == 0,
                    onClick = { topTabMode = 0 },
                    text = { Text("Trip Checklists", fontWeight = if (topTabMode == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = topTabMode == 1,
                    onClick = { topTabMode = 1 },
                    text = { Text("Master Templates", fontWeight = if (topTabMode == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (topTabMode == 0) {
                // Trip Checklist mode
                if (trips.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FlightTakeoff, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Trips Created", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Create a trip first to manage its packing checklist.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    // Trip selector dropdown
                    ExposedDropdownMenuBox(
                        expanded = tripDropdownExpanded,
                        onExpandedChange = { tripDropdownExpanded = !tripDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentTrip?.title ?: "Select a Trip",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Packing For Trip") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tripDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = tripDropdownExpanded,
                            onDismissRequest = { tripDropdownExpanded = false }
                        ) {
                            trips.forEach { trip ->
                                DropdownMenuItem(
                                    text = { Text("${trip.destination} (${trip.title})") },
                                    onClick = {
                                        selectedTripId = trip.id
                                        tripDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Bar Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Checklist Status", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "$packedCount of $totalCount packed (${(progress * 100).toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category scrollable filter
                    ScrollableTabRow(
                        selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                        edgePadding = 0.dp,
                        divider = {}
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) },
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Checklist,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No items in this category", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(onClick = { topTabMode = 1 }) {
                                    Text("Browse Packing Templates")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredItems, key = { it.id }) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (item.isPacked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = item.isPacked,
                                            onCheckedChange = { onTogglePacked(item.id, it) }
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = item.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (item.isEssential) FontWeight.Bold else FontWeight.Normal,
                                                    textDecoration = if (item.isPacked) TextDecoration.LineThrough else null,
                                                    color = if (item.isPacked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (item.isEssential) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        Icons.Default.Star,
                                                        contentDescription = "Essential",
                                                        tint = MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text(
                                                    text = item.category,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                if (item.quantity > 1) {
                                                    Text(
                                                        text = "Qty: ${item.quantity}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }

                                        IconButton(onClick = { onDeleteItem(item.id) }, modifier = Modifier.size(32.dp)) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            } else {
                // Master Templates Mode
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Pre-Curated Packing Templates",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Import expert-crafted checklists into any trip with a single tap.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(PackingTemplates.templates) { template ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { previewTemplate = template },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(template.iconEmoji, fontSize = 32.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(template.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            template.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${template.items.size} essential items",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )

                                    if (trips.isNotEmpty()) {
                                        FilledTonalButton(
                                            onClick = {
                                                selectedTripId?.let { tripId ->
                                                    onApplyTemplate(tripId, template.id)
                                                    topTabMode = 0
                                                }
                                            }
                                        ) {
                                            Text("Apply to Selected Trip")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(40.dp)) }
                }
            }
        }
    }
}
