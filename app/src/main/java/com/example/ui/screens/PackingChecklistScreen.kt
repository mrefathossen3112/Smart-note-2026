package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PackingItem
import com.example.data.model.Trip
import com.example.data.templates.PackingTemplate
import com.example.data.templates.PackingTemplates

/**
 * PackingChecklistScreen - Manages packing checklists backed by the local Room database.
 * Supports toggling packed status, quick inline addition, quantity adjustment,
 * category filtering, search, bulk packing/unpacking, and template importing.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PackingChecklistScreen(
    trips: List<Trip>,
    allPackingItems: List<PackingItem>,
    initialSelectedTripId: Long? = null,
    onTogglePacked: (itemId: Long, isPacked: Boolean) -> Unit,
    onUpdateQuantity: (itemId: Long, quantity: Int) -> Unit,
    onDeleteItem: (itemId: Long) -> Unit,
    onAddItem: (tripId: Long) -> Unit,
    onEditItem: ((PackingItem) -> Unit)? = null,
    onQuickAddItem: (tripId: Long, name: String, category: String, isEssential: Boolean) -> Unit,
    onSetAllPacked: (tripId: Long, isPacked: Boolean) -> Unit,
    onDeletePackedItems: (tripId: Long) -> Unit,
    onApplyTemplate: (tripId: Long, templateId: String) -> Unit
) {
    val context = LocalContext.current

    // Active trip selection state
    var selectedTripId by remember(trips, initialSelectedTripId) {
        mutableStateOf(initialSelectedTripId ?: trips.firstOrNull()?.id)
    }

    var selectedModeTab by remember { mutableIntStateOf(0) } // 0 = Checklist, 1 = Templates
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var filterStatus by remember { mutableStateOf("ALL") } // ALL, UNPACKED, PACKED, ESSENTIAL
    var showInlineQuickAdd by remember { mutableStateOf(false) }
    var tripDropdownExpanded by remember { mutableStateOf(false) }
    var overflowMenuExpanded by remember { mutableStateOf(false) }

    // Quick add fields
    var quickItemName by remember { mutableStateOf("") }
    var quickItemCategory by remember { mutableStateOf("Clothing") }
    var quickItemIsEssential by remember { mutableStateOf(false) }

    val currentTrip = trips.find { it.id == selectedTripId }
    val tripPackingItems = allPackingItems.filter { it.tripId == selectedTripId }

    // Filtered items based on search and selected categories
    val displayedItems = tripPackingItems.filter { item ->
        val matchesSearch = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategory == "ALL" || item.category.equals(selectedCategory, ignoreCase = true)

        val matchesStatus = when (filterStatus) {
            "UNPACKED" -> !item.isPacked
            "PACKED" -> item.isPacked
            "ESSENTIAL" -> item.isEssential
            else -> true
        }

        matchesSearch && matchesCategory && matchesStatus
    }

    val totalCount = tripPackingItems.size
    val packedCount = tripPackingItems.count { it.isPacked }
    val remainingCount = totalCount - packedCount
    val essentialCount = tripPackingItems.count { it.isEssential }
    val essentialUnpackedCount = tripPackingItems.count { it.isEssential && !it.isPacked }
    val progress = if (totalCount > 0) packedCount.toFloat() / totalCount.toFloat() else 0f

    val categories = listOf("ALL", "Documents", "Clothing", "Electronics", "Toiletries", "Health", "Gear", "Miscellaneous")

    Scaffold(
        floatingActionButton = {
            if (selectedModeTab == 0 && selectedTripId != null) {
                FloatingActionButton(
                    onClick = { selectedTripId?.let { onAddItem(it) } },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("packing_checklist_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Packing Item")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Top Header / Tabs
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Luggage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Packing Checklist",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Top Action Icons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectedModeTab == 0 && tripPackingItems.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        sharePackingChecklist(
                                            context = context,
                                            tripTitle = currentTrip?.title ?: "Trip",
                                            destination = currentTrip?.destination ?: "",
                                            items = tripPackingItems
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share Checklist",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box {
                                    IconButton(onClick = { overflowMenuExpanded = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Checklist Options",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = overflowMenuExpanded,
                                        onDismissRequest = { overflowMenuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Pack All Items") },
                                            leadingIcon = { Icon(Icons.Default.DoneAll, contentDescription = null) },
                                            onClick = {
                                                overflowMenuExpanded = false
                                                selectedTripId?.let { onSetAllPacked(it, true) }
                                                Toast.makeText(context, "All items marked as packed!", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Unpack All Items") },
                                            leadingIcon = { Icon(Icons.Default.RestartAlt, contentDescription = null) },
                                            onClick = {
                                                overflowMenuExpanded = false
                                                selectedTripId?.let { onSetAllPacked(it, false) }
                                                Toast.makeText(context, "All items reset to unpacked", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                        Divider()
                                        DropdownMenuItem(
                                            text = { Text("Remove Packed Items") },
                                            leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                            onClick = {
                                                overflowMenuExpanded = false
                                                selectedTripId?.let { onDeletePackedItems(it) }
                                                Toast.makeText(context, "Cleared packed items", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tab selector between Active Checklist and Curated Templates
                    TabRow(
                        selectedTabIndex = selectedModeTab,
                        containerColor = Color.Transparent
                    ) {
                        Tab(
                            selected = selectedModeTab == 0,
                            onClick = { selectedModeTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("My Checklist", fontWeight = if (selectedModeTab == 0) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        )
                        Tab(
                            selected = selectedModeTab == 1,
                            onClick = { selectedModeTab = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Smart Templates", fontWeight = if (selectedModeTab == 1) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        )
                    }
                }
            }

            if (selectedModeTab == 0) {
                // Checklist Mode
                if (trips.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlightTakeoff,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Trips Found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Create a trip first to manage and organize its packing checklist.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))

                            // Trip Selector Dropdown
                            ExposedDropdownMenuBox(
                                expanded = tripDropdownExpanded,
                                onExpandedChange = { tripDropdownExpanded = !tripDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = currentTrip?.let { "${it.destination} — ${it.title}" } ?: "Select a Trip",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Trip Checklist") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.FlightTakeoff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
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
                                            text = {
                                                Column {
                                                    Text(trip.destination, fontWeight = FontWeight.Bold)
                                                    Text(trip.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            },
                                            onClick = {
                                                selectedTripId = trip.id
                                                tripDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Progress and Stats Overview Card
                        item {
                            PackingProgressOverviewCard(
                                totalCount = totalCount,
                                packedCount = packedCount,
                                remainingCount = remainingCount,
                                essentialUnpackedCount = essentialUnpackedCount,
                                progress = progress,
                                onPackAll = { selectedTripId?.let { onSetAllPacked(it, true) } },
                                onUnpackAll = { selectedTripId?.let { onSetAllPacked(it, false) } }
                            )
                        }

                        // Search and Filters Bar
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search items (e.g. passport, jacket)") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Status filter chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilterChip(
                                        selected = filterStatus == "ALL",
                                        onClick = { filterStatus = "ALL" },
                                        label = { Text("All ($totalCount)") },
                                        modifier = Modifier.testTag("filter_all")
                                    )
                                    FilterChip(
                                        selected = filterStatus == "UNPACKED",
                                        onClick = { filterStatus = "UNPACKED" },
                                        label = { Text("To Pack ($remainingCount)") },
                                        modifier = Modifier.testTag("filter_unpacked")
                                    )
                                    FilterChip(
                                        selected = filterStatus == "PACKED",
                                        onClick = { filterStatus = "PACKED" },
                                        label = { Text("Packed ($packedCount)") },
                                        modifier = Modifier.testTag("filter_packed")
                                    )
                                    if (essentialCount > 0) {
                                        FilterChip(
                                            selected = filterStatus == "ESSENTIAL",
                                            onClick = { filterStatus = "ESSENTIAL" },
                                            label = { Text("⭐ Essentials ($essentialCount)") }
                                        )
                                    }
                                }

                                // Category Horizontal Filter
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
                            }
                        }

                        // Quick Inline Add Accordion/Button
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showInlineQuickAdd = !showInlineQuickAdd },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Quick Add Item",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Icon(
                                            imageVector = if (showInlineQuickAdd) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Toggle Quick Add",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    AnimatedVisibility(visible = showInlineQuickAdd) {
                                        Column(
                                            modifier = Modifier.padding(top = 10.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = quickItemName,
                                                onValueChange = { quickItemName = it },
                                                label = { Text("Item Name") },
                                                placeholder = { Text("e.g. Power bank, Sunglasses") },
                                                singleLine = true,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("quick_add_input"),
                                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                                keyboardActions = KeyboardActions(
                                                    onDone = {
                                                        if (quickItemName.isNotBlank() && selectedTripId != null) {
                                                            onQuickAddItem(
                                                                selectedTripId!!,
                                                                quickItemName.trim(),
                                                                quickItemCategory,
                                                                quickItemIsEssential
                                                            )
                                                            quickItemName = ""
                                                        }
                                                    }
                                                )
                                            )

                                            // Category selector pills
                                            LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                val addCats = listOf("Clothing", "Documents", "Electronics", "Toiletries", "Health", "Gear", "Miscellaneous")
                                                items(addCats) { cat ->
                                                    FilterChip(
                                                        selected = quickItemCategory == cat,
                                                        onClick = { quickItemCategory = cat },
                                                        label = { Text(cat, fontSize = 12.sp) }
                                                    )
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.clickable { quickItemIsEssential = !quickItemIsEssential }
                                                ) {
                                                    Checkbox(
                                                        checked = quickItemIsEssential,
                                                        onCheckedChange = { quickItemIsEssential = it }
                                                    )
                                                    Text(
                                                        text = "Essential item",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }

                                                Button(
                                                    onClick = {
                                                        if (quickItemName.isNotBlank() && selectedTripId != null) {
                                                            onQuickAddItem(
                                                                selectedTripId!!,
                                                                quickItemName.trim(),
                                                                quickItemCategory,
                                                                quickItemIsEssential
                                                            )
                                                            quickItemName = ""
                                                        }
                                                    },
                                                    enabled = quickItemName.isNotBlank(),
                                                    modifier = Modifier.testTag("quick_add_confirm_button")
                                                ) {
                                                    Text("Add to List")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Checklist Items List
                        if (displayedItems.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Checklist,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        )
                                        Text(
                                            text = if (searchQuery.isNotEmpty()) "No items matching \"$searchQuery\""
                                            else "No packing items found in this filter",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(onClick = { selectedModeTab = 1 }) {
                                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Use Template")
                                            }
                                            Button(onClick = { selectedTripId?.let { onAddItem(it) } }) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Add Item")
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            items(displayedItems, key = { it.id }) { item ->
                                PackingItemRow(
                                    item = item,
                                    onTogglePacked = { onTogglePacked(item.id, !item.isPacked) },
                                    onQuantityChange = { newQty -> onUpdateQuantity(item.id, newQty) },
                                    onDeleteItem = { onDeleteItem(item.id) },
                                    onEditItem = { onEditItem?.invoke(item) }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            } else {
                // Smart Templates Mode
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Smart Packing Templates",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Choose a curated checklist to instantly populate your luggage list for ${currentTrip?.destination ?: "your trip"}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    items(PackingTemplates.templates) { template ->
                        TemplateCard(
                            template = template,
                            targetTripTitle = currentTrip?.title ?: "Trip",
                            canApply = selectedTripId != null,
                            onApply = {
                                selectedTripId?.let { tripId ->
                                    onApplyTemplate(tripId, template.id)
                                    selectedModeTab = 0
                                    Toast.makeText(context, "Imported ${template.name} checklist!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }
}

/**
 * Packing progress overview card showing percentage, bar, and quick actions.
 */
@Composable
private fun PackingProgressOverviewCard(
    totalCount: Int,
    packedCount: Int,
    remainingCount: Int,
    essentialUnpackedCount: Int,
    progress: Float,
    onPackAll: () -> Unit,
    onUnpackAll: () -> Unit
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progressAnim")
    val isComplete = totalCount > 0 && packedCount == totalCount

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isComplete) Color(0xFF10B981).copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isComplete) "All Packed & Ready! 🎉" else "Packing Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isComplete) Color(0xFF059669) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$packedCount of $totalCount items packed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isComplete) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isComplete) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "Remaining: $remainingCount",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (essentialUnpackedCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "⚠️ $essentialUnpackedCount essential left",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Quick pack buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = onPackAll,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Pack All", fontSize = 12.sp)
                    }
                    TextButton(
                        onClick = onUnpackAll,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Reset", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Individual row for a packing item with status toggle, quantity adjustment, and category.
 */
@Composable
private fun PackingItemRow(
    item: PackingItem,
    onTogglePacked: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onDeleteItem: () -> Unit,
    onEditItem: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (item.isPacked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        else MaterialTheme.colorScheme.surface,
        label = "itemBg"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("packing_item_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isPacked) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox for toggling packed status directly in Room database
            Checkbox(
                checked = item.isPacked,
                onCheckedChange = { onTogglePacked() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF10B981),
                    checkmarkColor = Color.White
                ),
                modifier = Modifier.testTag("checkbox_packed_${item.id}")
            )

            // Category Icon Badge
            val (icon, color) = getCategoryVisuals(item.category)
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = item.category,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Title, category, and essential badge
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTogglePacked() }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (item.isEssential) FontWeight.Bold else FontWeight.Medium,
                        textDecoration = if (item.isPacked) TextDecoration.LineThrough else null,
                        color = if (item.isPacked) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (item.isEssential) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Essential Item",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (item.isPacked) "Packed" else "To Pack",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (item.isPacked) Color(0xFF059669) else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Quantity Stepper: [-] qty [+]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                if (item.quantity > 1) {
                    IconButton(
                        onClick = { onQuantityChange(item.quantity - 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease Quantity",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = "×${item.quantity}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                IconButton(
                    onClick = { onQuantityChange(item.quantity + 1) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase Quantity",
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Edit button
            IconButton(
                onClick = onEditItem,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Item",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Delete button
            IconButton(
                onClick = onDeleteItem,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Item",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Pre-curated packing template card.
 */
@Composable
private fun TemplateCard(
    template: PackingTemplate,
    targetTripTitle: String,
    canApply: Boolean,
    onApply: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = template.iconEmoji,
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Preview items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                template.items.take(4).forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (template.items.size > 4) {
                    Text(
                        text = "+${template.items.size - 4} more",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically)
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
                    text = "${template.items.size} curated items",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = onApply,
                    enabled = canApply,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add to $targetTripTitle")
                }
            }
        }
    }
}

/**
 * Returns an appropriate icon and theme tint for each category.
 */
private fun getCategoryVisuals(category: String): Pair<ImageVector, Color> {
    return when (category.lowercase()) {
        "documents" -> Pair(Icons.Default.Description, Color(0xFF3B82F6))
        "clothing" -> Pair(Icons.Default.Luggage, Color(0xFF8B5CF6))
        "electronics" -> Pair(Icons.Default.Work, Color(0xFFF59E0B))
        "toiletries" -> Pair(Icons.Default.Spa, Color(0xFF10B981))
        "health" -> Pair(Icons.Default.LocalPharmacy, Color(0xFFEF4444))
        "gear" -> Pair(Icons.Default.FitnessCenter, Color(0xFF06B6D4))
        else -> Pair(Icons.Default.Category, Color(0xFF6B7280))
    }
}

/**
 * Exports/shares the packing checklist as clean text.
 */
private fun sharePackingChecklist(
    context: Context,
    tripTitle: String,
    destination: String,
    items: List<PackingItem>
) {
    val total = items.size
    val packed = items.count { it.isPacked }
    val remaining = total - packed

    val sb = StringBuilder()
    sb.appendLine("🧳 Packing Checklist: $tripTitle ($destination)")
    sb.appendLine("Status: $packed/$total items packed (${if (total > 0) (packed * 100 / total) else 0}% complete)")
    sb.appendLine("----------------------------------------")

    val grouped = items.groupBy { it.category }
    grouped.forEach { (category, categoryItems) ->
        sb.appendLine("\n📂 $category:")
        categoryItems.forEach { item ->
            val check = if (item.isPacked) " [✓] " else " [ ] "
            val star = if (item.isEssential) " ⭐" else ""
            val qty = if (item.quantity > 1) " (×${item.quantity})" else ""
            sb.appendLine("$check${item.name}$qty$star")
        }
    }

    sb.appendLine("\n----------------------------------------")
    sb.appendLine("Created with Smart Travel Planner")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Packing Checklist - $destination")
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Share Packing Checklist"))
}
