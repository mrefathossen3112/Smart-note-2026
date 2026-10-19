package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Note
import com.example.data.model.ScheduleItem
import com.example.data.model.Trip
import com.example.ui.dialogs.NoteDialog
import com.example.ui.dialogs.PackingItemDialog
import com.example.ui.dialogs.ScheduleDialog
import com.example.ui.dialogs.TemplatePickerDialog
import com.example.ui.dialogs.TripDialog
import com.example.ui.dialogs.VaultItemDialog
import com.example.ui.screens.DailyItineraryScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GeminiChatScreen
import com.example.ui.screens.GeminiLiveVoiceScreen
import com.example.ui.screens.NotesScreen
import com.example.data.model.PackingItem
import com.example.ui.screens.PackingChecklistScreen
import com.example.ui.screens.PackingScreen
import com.example.ui.screens.SmartToolsScreen
import com.example.ui.screens.TravelNotesScreen
import com.example.ui.screens.TripDetailScreen
import com.example.ui.screens.TripsScreen

@Composable
fun HomeShell(viewModel: PlannerViewModel) {
    val context = LocalContext.current

    // Request notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Collect states
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val compassData by viewModel.compassData.collectAsStateWithLifecycle()
    val allCachedWeather by viewModel.allCachedWeather.collectAsStateWithLifecycle()
    val isWeatherRefreshing by viewModel.isWeatherRefreshing.collectAsStateWithLifecycle()

    val allTrips by viewModel.allTrips.collectAsStateWithLifecycle()
    val filteredTrips by viewModel.filteredTrips.collectAsStateWithLifecycle()
    val activeOrNextTrip by viewModel.activeOrNextTrip.collectAsStateWithLifecycle()
    val selectedTripId by viewModel.selectedTripId.collectAsStateWithLifecycle()
    val selectedTrip by viewModel.selectedTrip.collectAsStateWithLifecycle()
    val selectedTripSchedules by viewModel.selectedTripSchedules.collectAsStateWithLifecycle()
    val selectedTripPacking by viewModel.selectedTripPacking.collectAsStateWithLifecycle()
    val selectedTripNotes by viewModel.selectedTripNotes.collectAsStateWithLifecycle()
    val selectedTripSpent by viewModel.selectedTripSpent.collectAsStateWithLifecycle()
    val selectedTripWeather by viewModel.selectedTripWeather.collectAsStateWithLifecycle()

    val allSchedules by viewModel.allSchedules.collectAsStateWithLifecycle()
    val upcomingSchedules by viewModel.upcomingSchedules.collectAsStateWithLifecycle()
    val allPackingItems by viewModel.allPackingItems.collectAsStateWithLifecycle()
    val filteredNotes by viewModel.filteredNotes.collectAsStateWithLifecycle()

    val currentTripFilter by viewModel.tripFilter.collectAsStateWithLifecycle()
    val tripSearchQuery by viewModel.tripSearchQuery.collectAsStateWithLifecycle()

    val notesSearchQuery by viewModel.notesSearchQuery.collectAsStateWithLifecycle()
    val notesCategoryFilter by viewModel.notesCategoryFilter.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiResponding by viewModel.isAiResponding.collectAsStateWithLifecycle()

    val allVaultItems by viewModel.allVaultItems.collectAsStateWithLifecycle()
    val sunTimes by viewModel.sunTimes.collectAsStateWithLifecycle()
    val isRatesRefreshing by viewModel.isRefreshingRates.collectAsStateWithLifecycle()

    var showGeminiChat by remember { mutableStateOf(false) }
    var chatTripContext by remember { mutableStateOf<String?>(null) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Dashboard, 1: Trips, 2: Tools, 3: Packing, 4: Notes

    // Dialog control states
    var showTripDialog by remember { mutableStateOf(false) }
    var tripToEdit by remember { mutableStateOf<Trip?>(null) }

    var showScheduleDialog by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<ScheduleItem?>(null) }
    var scheduleTargetTripId by remember { mutableStateOf<Long?>(null) }

    var showPackingItemDialog by remember { mutableStateOf(false) }
    var packingTargetTripId by remember { mutableStateOf<Long?>(null) }
    var packingItemToEdit by remember { mutableStateOf<PackingItem?>(null) }

    var showTemplatePickerDialog by remember { mutableStateOf(false) }
    var templateTargetTripId by remember { mutableStateOf<Long?>(null) }
    var templateTargetTripTitle by remember { mutableStateOf("") }

    var showNoteDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var noteTargetTripId by remember { mutableStateOf<Long?>(null) }

    var showVaultDialog by remember { mutableStateOf(false) }
    var showDailyItineraryForTrip by remember { mutableStateOf<Trip?>(null) }
    var showGeminiLiveVoice by remember { mutableStateOf(false) }

    // If Gemini Live Voice is open, show GeminiLiveVoiceScreen
    if (showGeminiLiveVoice) {
        GeminiLiveVoiceScreen(
            isOnline = isOnline,
            tripContext = chatTripContext,
            onSendVoiceQuery = { speech, callback ->
                viewModel.sendLiveVoiceQuery(speech, chatTripContext, callback)
            },
            onBack = {
                showGeminiLiveVoice = false
            }
        )
    } else if (showGeminiChat) {
        GeminiChatScreen(
            messages = chatMessages,
            isOnline = isOnline,
            isResponding = isAiResponding,
            tripContext = chatTripContext,
            onSendMessage = { query ->
                viewModel.sendChatMessage(query, chatTripContext)
            },
            onClearHistory = { viewModel.clearChatHistory() },
            onBack = {
                showGeminiChat = false
                chatTripContext = null
            },
            onOpenLiveVoice = {
                showGeminiLiveVoice = true
            }
        )
    } else if (showDailyItineraryForTrip != null) {
        val targetTrip = showDailyItineraryForTrip!!
        val tripSchedules = allSchedules.filter { it.tripId == targetTrip.id }
        DailyItineraryScreen(
            trip = targetTrip,
            schedules = tripSchedules,
            onBack = { showDailyItineraryForTrip = null },
            onAddSchedule = {
                scheduleToEdit = null
                scheduleTargetTripId = targetTrip.id
                showScheduleDialog = true
            },
            onEditSchedule = { item ->
                scheduleToEdit = item
                scheduleTargetTripId = item.tripId
                showScheduleDialog = true
            },
            onDeleteSchedule = { id -> viewModel.deleteScheduleItem(id) },
            onToggleScheduleCompleted = { id, comp -> viewModel.toggleScheduleCompleted(id, comp) },
            onTestNotification = { item -> viewModel.triggerTestReminder(context, item) }
        )
    } else if (selectedTripId != null && selectedTrip != null) {
        TripDetailScreen(
            trip = selectedTrip!!,
            weather = selectedTripWeather,
            isOnline = isOnline,
            schedules = selectedTripSchedules,
            packingItems = selectedTripPacking,
            notes = selectedTripNotes,
            totalSpent = selectedTripSpent ?: 0.0,
            onBack = { viewModel.selectTrip(null) },
            onRefreshWeather = {
                selectedTrip?.let { viewModel.refreshWeatherForDestination(it.destination) }
            },
            onEditTrip = { trip ->
                tripToEdit = trip
                showTripDialog = true
            },
            onDeleteTrip = { id ->
                viewModel.deleteTrip(id)
            },
            onAddSchedule = {
                scheduleToEdit = null
                scheduleTargetTripId = selectedTrip!!.id
                showScheduleDialog = true
            },
            onEditSchedule = { item ->
                scheduleToEdit = item
                scheduleTargetTripId = item.tripId
                showScheduleDialog = true
            },
            onDeleteSchedule = { id ->
                viewModel.deleteScheduleItem(id)
            },
            onToggleScheduleCompleted = { id, comp ->
                viewModel.toggleScheduleCompleted(id, comp)
            },
            onTestNotification = { item ->
                viewModel.triggerTestReminder(context, item)
            },
            onAddPackingItem = {
                packingTargetTripId = selectedTrip!!.id
                showPackingItemDialog = true
            },
            onImportPackingTemplate = {
                templateTargetTripId = selectedTrip!!.id
                templateTargetTripTitle = selectedTrip!!.title
                showTemplatePickerDialog = true
            },
            onTogglePackingItem = { id, packed ->
                viewModel.togglePackingItem(id, packed)
            },
            onDeletePackingItem = { id ->
                viewModel.deletePackingItem(id)
            },
            onAddNote = {
                noteToEdit = null
                noteTargetTripId = selectedTrip!!.id
                showNoteDialog = true
            },
            onEditNote = { note ->
                noteToEdit = note
                noteTargetTripId = note.tripId
                showNoteDialog = true
            },
            onOpenGeminiChat = { ctx ->
                chatTripContext = ctx
                showGeminiChat = true
            },
            onOpenDailyItinerary = {
                showDailyItineraryForTrip = selectedTrip
            }
        )
    } else {
        Scaffold(
            floatingActionButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gemini Live Voice Quick Action
                    FloatingActionButton(
                        onClick = {
                            chatTripContext = activeOrNextTrip?.let { "${it.title} (${it.destination})" }
                            showGeminiLiveVoice = true
                        },
                        containerColor = Color(0xFF6366F1),
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_gemini_live_voice")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Gemini Live Voice Call",
                            tint = Color.White
                        )
                    }

                    // Ask Gemini Text Action
                    ExtendedFloatingActionButton(
                        onClick = {
                            chatTripContext = activeOrNextTrip?.let { "${it.title} (${it.destination})" }
                            showGeminiChat = true
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        text = {
                            Text(
                                text = "Ask Gemini",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("fab_gemini_chat")
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                if (selectedTab == 0) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                                contentDescription = "Dashboard"
                            )
                        },
                        label = { Text("Dashboard") },
                        modifier = Modifier.testTag("nav_dashboard")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                if (selectedTab == 1) Icons.Filled.FlightTakeoff else Icons.Outlined.FlightTakeoff,
                                contentDescription = "Trips"
                            )
                        },
                        label = { Text("Trips") },
                        modifier = Modifier.testTag("nav_trips")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Icon(
                                if (selectedTab == 2) Icons.Filled.Explore else Icons.Outlined.Explore,
                                contentDescription = "Tools"
                            )
                        },
                        label = { Text("Tools") },
                        modifier = Modifier.testTag("nav_tools")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = {
                            Icon(
                                if (selectedTab == 3) Icons.Filled.Checklist else Icons.Outlined.Checklist,
                                contentDescription = "Packing"
                            )
                        },
                        label = { Text("Packing") },
                        modifier = Modifier.testTag("nav_packing")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = {
                            Icon(
                                if (selectedTab == 4) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                                contentDescription = "Notes"
                            )
                        },
                        label = { Text("Notes") },
                        modifier = Modifier.testTag("nav_notes")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> {
                        val featuredWeather = allCachedWeather.find {
                            it.locationKey.equals(activeOrNextTrip?.destination, ignoreCase = true)
                        }
                        DashboardScreen(
                            featuredTrip = activeOrNextTrip,
                            featuredWeather = featuredWeather,
                            isOnline = isOnline,
                            allTrips = allTrips,
                            upcomingSchedules = upcomingSchedules,
                            packingItems = allPackingItems,
                            pinnedNotes = filteredNotes.filter { it.isPinned },
                            onSelectTrip = { viewModel.selectTrip(it) },
                            onAddNewTrip = {
                                tripToEdit = null
                                showTripDialog = true
                            },
                            onAddNewSchedule = {
                                scheduleToEdit = null
                                scheduleTargetTripId = activeOrNextTrip?.id ?: allTrips.firstOrNull()?.id
                                if (scheduleTargetTripId != null) {
                                    showScheduleDialog = true
                                } else {
                                    tripToEdit = null
                                    showTripDialog = true
                                }
                            },
                            onAddNewNote = {
                                noteToEdit = null
                                noteTargetTripId = null
                                showNoteDialog = true
                            },
                            onNavigateToPacking = { selectedTab = 3 },
                            onNavigateToTools = { selectedTab = 2 },
                            onToggleScheduleCompleted = { id, comp ->
                                viewModel.toggleScheduleCompleted(id, comp)
                            },
                            onTestNotification = { item ->
                                viewModel.triggerTestReminder(context, item)
                            },
                            onEditNote = { note ->
                                noteToEdit = note
                                noteTargetTripId = note.tripId
                                showNoteDialog = true
                            },
                            onOpenGeminiChat = { ctx ->
                                chatTripContext = ctx
                                showGeminiChat = true
                            }
                        )
                    }

                    1 -> TripsScreen(
                        trips = filteredTrips,
                        allSchedules = allSchedules,
                        allPackingItems = allPackingItems,
                        currentFilter = currentTripFilter,
                        searchQuery = tripSearchQuery,
                        onFilterChanged = { viewModel.setTripFilter(it) },
                        onSearchChanged = { viewModel.setTripSearchQuery(it) },
                        onTripClick = { viewModel.selectTrip(it) },
                        onAddNewTrip = {
                            tripToEdit = null
                            showTripDialog = true
                        },
                        onEditTrip = { trip ->
                            tripToEdit = trip
                            showTripDialog = true
                        },
                        onDeleteTrip = { id -> viewModel.deleteTrip(id) }
                    )

                    2 -> SmartToolsScreen(
                        isOnline = isOnline,
                        currentLocation = currentLocation,
                        compassData = compassData,
                        cachedWeatherList = allCachedWeather,
                        trips = allTrips,
                        isWeatherRefreshing = isWeatherRefreshing,
                        onRefreshWeatherForCity = { viewModel.refreshWeatherForDestination(it) },
                        onRefreshCurrentGpsWeather = { viewModel.refreshWeatherForCurrentGps() },
                        onRequestLocationTracking = { viewModel.startGpsTracking() },
                        onOpenGeminiChat = { ctx ->
                            chatTripContext = ctx
                            showGeminiChat = true
                        },
                        onOpenLiveVoice = { ctx ->
                            chatTripContext = ctx
                            showGeminiLiveVoice = true
                        },
                        sunTimes = sunTimes,
                        isRatesRefreshing = isRatesRefreshing,
                        onRefreshRates = { viewModel.refreshCurrencyRates() },
                        vaultItems = allVaultItems,
                        onAddVaultItem = { showVaultDialog = true },
                        onDeleteVaultItem = { viewModel.deleteVaultItem(it) }
                    )

                    3 -> PackingChecklistScreen(
                        trips = allTrips,
                        allPackingItems = allPackingItems,
                        initialSelectedTripId = activeOrNextTrip?.id ?: allTrips.firstOrNull()?.id,
                        onTogglePacked = { id, packed -> viewModel.togglePackingItem(id, packed) },
                        onUpdateQuantity = { id, qty -> viewModel.updatePackingQuantity(id, qty) },
                        onDeleteItem = { id -> viewModel.deletePackingItem(id) },
                        onAddItem = { tripId ->
                            packingTargetTripId = tripId
                            packingItemToEdit = null
                            showPackingItemDialog = true
                        },
                        onEditItem = { item ->
                            packingTargetTripId = item.tripId
                            packingItemToEdit = item
                            showPackingItemDialog = true
                        },
                        onQuickAddItem = { tripId, name, category, isEssential ->
                            viewModel.addPackingItem(tripId, name, category, 1, isEssential)
                        },
                        onSetAllPacked = { tripId, isPacked ->
                            viewModel.setAllPackingItemsPacked(tripId, isPacked)
                        },
                        onDeletePackedItems = { tripId ->
                            viewModel.deletePackedItems(tripId)
                        },
                        onApplyTemplate = { tripId, templateId ->
                            viewModel.applyPackingTemplate(tripId, templateId)
                        }
                    )

                    4 -> TravelNotesScreen(
                        notes = filteredNotes,
                        trips = allTrips,
                        searchQuery = notesSearchQuery,
                        selectedCategory = notesCategoryFilter,
                        onSearchChanged = { viewModel.setNotesSearchQuery(it) },
                        onCategoryChanged = { viewModel.setNotesCategoryFilter(it) },
                        onAddNewNote = {
                            noteToEdit = null
                            noteTargetTripId = null
                            showNoteDialog = true
                        },
                        onEditNote = { note ->
                            noteToEdit = note
                            noteTargetTripId = note.tripId
                            showNoteDialog = true
                        },
                        onQuickSaveNote = { tripId, title, content, category, isPinned ->
                            viewModel.saveNote(
                                id = 0L,
                                tripId = tripId,
                                title = title,
                                content = content,
                                category = category,
                                isPinned = isPinned
                            )
                        },
                        onTogglePin = { id, pin -> viewModel.toggleNotePinned(id, pin) },
                        onDeleteNote = { id -> viewModel.deleteNote(id) }
                    )
                }
            }
        }
    }

    // Modal Dialogs
    if (showTripDialog) {
        TripDialog(
            initialTrip = tripToEdit,
            onDismiss = {
                showTripDialog = false
                tripToEdit = null
            },
            onSave = { title, dest, start, end, budget, notes, category, color ->
                viewModel.saveTrip(
                    id = tripToEdit?.id ?: 0L,
                    title = title,
                    destination = dest,
                    startDate = start,
                    endDate = end,
                    budget = budget,
                    notes = notes,
                    category = category,
                    colorHex = color
                ) { newId ->
                    showTripDialog = false
                    tripToEdit = null
                }
            }
        )
    }

    if (showScheduleDialog && scheduleTargetTripId != null) {
        ScheduleDialog(
            tripId = scheduleTargetTripId!!,
            initialItem = scheduleToEdit,
            onDismiss = {
                showScheduleDialog = false
                scheduleToEdit = null
            },
            onSave = { title, location, timestamp, category, notes, cost, reminder ->
                viewModel.saveScheduleItem(
                    id = scheduleToEdit?.id ?: 0L,
                    tripId = scheduleTargetTripId!!,
                    title = title,
                    location = location,
                    timestamp = timestamp,
                    category = category,
                    notes = notes,
                    cost = cost,
                    reminderMinutes = reminder
                )
                showScheduleDialog = false
                scheduleToEdit = null
            }
        )
    }

    if (showPackingItemDialog && packingTargetTripId != null) {
        PackingItemDialog(
            tripId = packingTargetTripId!!,
            itemToEdit = packingItemToEdit,
            onDismiss = {
                showPackingItemDialog = false
                packingTargetTripId = null
                packingItemToEdit = null
            },
            onSave = { name, category, quantity, isEssential ->
                if (packingItemToEdit == null) {
                    viewModel.addPackingItem(
                        tripId = packingTargetTripId!!,
                        name = name,
                        category = category,
                        quantity = quantity,
                        isEssential = isEssential
                    )
                } else {
                    viewModel.updatePackingItem(
                        packingItemToEdit!!.copy(
                            name = name,
                            category = category,
                            quantity = quantity,
                            isEssential = isEssential
                        )
                    )
                }
                showPackingItemDialog = false
                packingTargetTripId = null
                packingItemToEdit = null
            }
        )
    }

    if (showTemplatePickerDialog && templateTargetTripId != null) {
        TemplatePickerDialog(
            tripTitle = templateTargetTripTitle,
            onDismiss = {
                showTemplatePickerDialog = false
                templateTargetTripId = null
            },
            onSelectTemplate = { templateId ->
                viewModel.applyPackingTemplate(templateTargetTripId!!, templateId)
                showTemplatePickerDialog = false
                templateTargetTripId = null
            }
        )
    }

    if (showNoteDialog) {
        NoteDialog(
            initialNote = noteToEdit,
            availableTrips = allTrips,
            defaultTripId = noteTargetTripId,
            onDismiss = {
                showNoteDialog = false
                noteToEdit = null
            },
            onSave = { tripId, title, content, category, isPinned ->
                viewModel.saveNote(
                    id = noteToEdit?.id ?: 0L,
                    tripId = tripId,
                    title = title,
                    content = content,
                    category = category,
                    isPinned = isPinned
                )
                showNoteDialog = false
                noteToEdit = null
            }
        )
    }

    if (showVaultDialog) {
        VaultItemDialog(
            initialItem = null,
            onDismiss = { showVaultDialog = false },
            onSave = { title, category, docNumber, details, isImportant ->
                viewModel.saveVaultItem(
                    title = title,
                    category = category,
                    docNumber = docNumber,
                    details = details,
                    isImportant = isImportant
                )
                showVaultDialog = false
            }
        )
    }
}
