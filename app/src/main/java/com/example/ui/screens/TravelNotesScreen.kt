package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.ModeEdit
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Note
import com.example.data.model.Trip
import com.example.util.DateUtils

/**
 * TravelNotesScreen - A Jetpack Compose screen for managing travel notes, itineraries,
 * flight bookings, memories, and restaurant recommendations saved locally in Room database.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TravelNotesScreen(
    notes: List<Note>,
    trips: List<Trip>,
    searchQuery: String,
    selectedCategory: String,
    onSearchChanged: (String) -> Unit,
    onCategoryChanged: (String) -> Unit,
    onAddNewNote: () -> Unit,
    onEditNote: (Note) -> Unit,
    onQuickSaveNote: (tripId: Long?, title: String, content: String, category: String, isPinned: Boolean) -> Unit,
    onTogglePin: (Long, Boolean) -> Unit,
    onDeleteNote: (Long) -> Unit
) {
    val context = LocalContext.current

    var isGridView by remember { mutableStateOf(false) }
    var selectedTripFilterId by remember { mutableStateOf<Long?>(null) } // null = All Trips
    var isQuickAddOpen by remember { mutableStateOf(false) }

    // Quick scratchpad state
    var quickTitle by remember { mutableStateOf("") }
    var quickContent by remember { mutableStateOf("") }
    var quickCategory by remember { mutableStateOf("Places") }
    var quickTripId by remember { mutableStateOf<Long?>(null) }
    var quickIsPinned by remember { mutableStateOf(false) }

    val categories = listOf("ALL", "Places", "Dining", "Logistics", "Ideas", "Budget", "Emergency", "General")

    // Filter notes by search query, category, and selected trip
    val filteredList = notes.filter { note ->
        val matchesSearch = searchQuery.isBlank() ||
                note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true) ||
                note.category.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategory == "ALL" || note.category.equals(selectedCategory, ignoreCase = true)

        val matchesTrip = selectedTripFilterId == null || note.tripId == selectedTripFilterId

        matchesSearch && matchesCategory && matchesTrip
    }

    val pinnedNotes = filteredList.filter { it.isPinned }
    val unpinnedNotes = filteredList.filter { !it.isPinned }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewNote,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("travel_notes_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Travel Note")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Bar
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
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Travel Notes",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Grid / List View Toggle
                            IconButton(onClick = { isGridView = !isGridView }) {
                                Icon(
                                    imageVector = if (isGridView) Icons.Default.ViewAgenda else Icons.Default.GridView,
                                    contentDescription = if (isGridView) "List View" else "Grid View",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChanged,
                        placeholder = { Text("Search travel notes, spots, booking IDs...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("travel_notes_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Categories Horizontal Tabs
                    ScrollableTabRow(
                        selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                        edgePadding = 0.dp,
                        divider = {}
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { onCategoryChanged(cat) },
                                label = { Text(cat) },
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }
                    }
                }
            }

            // Main Content Area
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Trip Filter Chips (if user has trips created)
                if (trips.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedTripFilterId == null,
                                    onClick = { selectedTripFilterId = null },
                                    label = { Text("All Trips (${notes.size})") }
                                )
                            }
                            items(trips) { trip ->
                                val tripNotesCount = notes.count { it.tripId == trip.id }
                                FilterChip(
                                    selected = selectedTripFilterId == trip.id,
                                    onClick = { selectedTripFilterId = trip.id },
                                    label = { Text("${trip.destination} ($tripNotesCount)") }
                                )
                            }
                        }
                    }
                }

                // Quick Scratchpad / Instant Note Input Card (Saves directly to Room)
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isQuickAddOpen = !isQuickAddOpen },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ModeEdit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isQuickAddOpen) "Quick Note Editor" else "Tap to jot down a quick note...",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isQuickAddOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                TextButton(onClick = { isQuickAddOpen = !isQuickAddOpen }) {
                                    Text(if (isQuickAddOpen) "Collapse" else "Expand", fontSize = 12.sp)
                                }
                            }

                            AnimatedVisibility(visible = isQuickAddOpen) {
                                Column(
                                    modifier = Modifier.padding(top = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = quickTitle,
                                        onValueChange = { quickTitle = it },
                                        label = { Text("Title *") },
                                        placeholder = { Text("e.g. Best Ramen Spot in Shibuya") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("quick_note_title")
                                    )

                                    OutlinedTextField(
                                        value = quickContent,
                                        onValueChange = { quickContent = it },
                                        label = { Text("Details & Notes") },
                                        placeholder = { Text("Write address, timings, reservation notes, ticket numbers...") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .testTag("quick_note_content"),
                                        maxLines = 5
                                    )

                                    // Category chips for quick note
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        val categoriesOptions = listOf("Places", "Dining", "Logistics", "Ideas", "Budget", "Emergency", "General")
                                        items(categoriesOptions) { cat ->
                                            FilterChip(
                                                selected = quickCategory == cat,
                                                onClick = { quickCategory = cat },
                                                label = { Text(cat, fontSize = 12.sp) }
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FilterChip(
                                            selected = quickIsPinned,
                                            onClick = { quickIsPinned = !quickIsPinned },
                                            leadingIcon = {
                                                Icon(
                                                    if (quickIsPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            },
                                            label = { Text(if (quickIsPinned) "Pinned" else "Pin to top") }
                                        )

                                        Button(
                                            onClick = {
                                                if (quickTitle.isNotBlank()) {
                                                    onQuickSaveNote(
                                                        quickTripId ?: selectedTripFilterId,
                                                        quickTitle.trim(),
                                                        quickContent.trim(),
                                                        quickCategory,
                                                        quickIsPinned
                                                    )
                                                    quickTitle = ""
                                                    quickContent = ""
                                                    isQuickAddOpen = false
                                                    Toast.makeText(context, "Note saved to Room database!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            enabled = quickTitle.isNotBlank(),
                                            modifier = Modifier.testTag("quick_note_save_btn")
                                        ) {
                                            Text("Save to Room")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // If no notes match the current filters
                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(54.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "No notes matching \"$searchQuery\""
                                    else "No travel notes yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Write down packing reminders, flight info, cafe recommendations, and travel memories.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(onClick = onAddNewNote) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Create Travel Note")
                                }
                            }
                        }
                    }
                } else {
                    // Pinned Notes Section
                    if (pinnedNotes.isNotEmpty()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PINNED NOTES (${pinnedNotes.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        items(pinnedNotes, key = { it.id }) { note ->
                            val linkedTrip = trips.find { it.id == note.tripId }
                            TravelNoteCard(
                                note = note,
                                linkedTrip = linkedTrip,
                                onClick = { onEditNote(note) },
                                onTogglePin = { onTogglePin(note.id, !note.isPinned) },
                                onDelete = { onDeleteNote(note.id) },
                                onCopy = { copyNoteToClipboard(context, note) },
                                onShare = { shareNote(context, note, linkedTrip?.destination) }
                            )
                        }

                        if (unpinnedNotes.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Text(
                                    text = "OTHER NOTES (${unpinnedNotes.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Unpinned Notes
                    items(unpinnedNotes, key = { it.id }) { note ->
                        val linkedTrip = trips.find { it.id == note.tripId }
                        TravelNoteCard(
                            note = note,
                            linkedTrip = linkedTrip,
                            onClick = { onEditNote(note) },
                            onTogglePin = { onTogglePin(note.id, !note.isPinned) },
                            onDelete = { onDeleteNote(note.id) },
                            onCopy = { copyNoteToClipboard(context, note) },
                            onShare = { shareNote(context, note, linkedTrip?.destination) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

/**
 * Individual Travel Note Card with category visual cues, copy, share, pin and delete actions.
 */
@Composable
private fun TravelNoteCard(
    note: Note,
    linkedTrip: Trip?,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    val (categoryIcon, categoryColor) = getNoteCategoryVisuals(note.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("note_card_${note.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (note.isPinned) categoryColor.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (note.isPinned) 2.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Category badge, title, and pin action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = categoryColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = note.category,
                                tint = categoryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = DateUtils.formatDate(note.updatedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Top right actions: Pin & Options
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Note",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Note Content
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Category chip, linked trip, and quick copy/share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = categoryColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = note.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (linkedTrip != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flight,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = linkedTrip.destination,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy note",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share note",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visual metadata for each note category.
 */
private fun getNoteCategoryVisuals(category: String): Pair<ImageVector, Color> {
    return when (category.lowercase()) {
        "places" -> Pair(Icons.Default.Place, Color(0xFF3B82F6))
        "dining" -> Pair(Icons.Default.LocalDining, Color(0xFFF97316))
        "logistics" -> Pair(Icons.Default.Flight, Color(0xFF6366F1))
        "ideas" -> Pair(Icons.Default.Lightbulb, Color(0xFFEAB308))
        "budget" -> Pair(Icons.Default.Paid, Color(0xFF10B981))
        "emergency" -> Pair(Icons.Default.Warning, Color(0xFFEF4444))
        else -> Pair(Icons.AutoMirrored.Filled.MenuBook, Color(0xFF8B5CF6))
    }
}

/**
 * Copies note contents to the Android system clipboard.
 */
private fun copyNoteToClipboard(context: Context, note: Note) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText("Travel Note", "${note.title}\n\n${note.content}")
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, "Note copied to clipboard", Toast.LENGTH_SHORT).show()
}

/**
 * Shares note text via Android system Share sheet.
 */
private fun shareNote(context: Context, note: Note, tripDestination: String?) {
    val subject = if (tripDestination != null) "[${tripDestination}] ${note.title}" else note.title
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, "${note.title}\n\n${note.content}\n\n— Smart Travel Planner")
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Note"))
}
