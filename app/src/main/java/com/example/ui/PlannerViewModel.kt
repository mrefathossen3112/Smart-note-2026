package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessage
import com.example.data.model.Note
import com.example.data.model.PackingItem
import com.example.data.model.ScheduleItem
import com.example.data.model.Trip
import com.example.data.model.VaultItem
import com.example.data.model.WeatherCache
import com.example.data.remote.GeminiService
import com.example.data.remote.WeatherService
import com.example.data.repository.PlannerRepository
import com.example.data.templates.PackingTemplates
import com.example.util.CompassData
import com.example.util.CompassSensor
import com.example.util.CurrencyConverter
import com.example.util.DateUtils
import com.example.util.LocationHelper
import com.example.util.NetworkMonitor
import com.example.util.NotificationHelper
import com.example.util.SunCalculator
import com.example.util.SunTimes
import com.example.util.UserLocation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PlannerViewModel(
    private val repository: PlannerRepository,
    private val networkMonitor: NetworkMonitor,
    private val locationHelper: LocationHelper,
    private val compassSensor: CompassSensor,
    private val weatherService: WeatherService = WeatherService(),
    private val geminiService: GeminiService = GeminiService()
) : ViewModel() {

    // Live network connectivity
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    // Telemetry: GPS Location & Digital Compass
    val currentLocation: StateFlow<UserLocation?> = locationHelper.currentLocation
    val compassData: StateFlow<CompassData> = compassSensor.compassData

    // Weather caching
    val allCachedWeather: StateFlow<List<WeatherCache>> = repository.allCachedWeather
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Gemini Chatbot state
    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAiResponding = MutableStateFlow(false)
    val isAiResponding: StateFlow<Boolean> = _isAiResponding.asStateFlow()

    // Emergency Vault Items
    val allVaultItems: StateFlow<List<VaultItem>> = repository.allVaultItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currency Refreshing
    private val _isRefreshingRates = MutableStateFlow(false)
    val isRefreshingRates: StateFlow<Boolean> = _isRefreshingRates.asStateFlow()

    // Sun Times Telemetry
    val sunTimes: StateFlow<SunTimes> = currentLocation.combine(flowOf(Unit)) { loc, _ ->
        val lat = loc?.latitude ?: 35.0116
        val lng = loc?.longitude ?: 135.7681
        SunCalculator.calculate(lat, lng)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SunCalculator.calculate(35.0116, 135.7681)
    )

    // Filter states
    private val _tripFilter = MutableStateFlow("ALL") // ALL, UPCOMING, ACTIVE, COMPLETED
    val tripFilter: StateFlow<String> = _tripFilter.asStateFlow()

    private val _tripSearchQuery = MutableStateFlow("")
    val tripSearchQuery: StateFlow<String> = _tripSearchQuery.asStateFlow()

    private val _notesSearchQuery = MutableStateFlow("")
    val notesSearchQuery: StateFlow<String> = _notesSearchQuery.asStateFlow()

    private val _notesCategoryFilter = MutableStateFlow("ALL")
    val notesCategoryFilter: StateFlow<String> = _notesCategoryFilter.asStateFlow()

    private val _selectedTripId = MutableStateFlow<Long?>(null)
    val selectedTripId: StateFlow<Long?> = _selectedTripId.asStateFlow()

    // Weather refresh loading status
    private val _isWeatherRefreshing = MutableStateFlow(false)
    val isWeatherRefreshing: StateFlow<Boolean> = _isWeatherRefreshing.asStateFlow()

    // All raw trips
    val allTrips: StateFlow<List<Trip>> = repository.allTrips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered trips
    val filteredTrips: StateFlow<List<Trip>> = combine(
        allTrips,
        _tripFilter,
        _tripSearchQuery
    ) { trips, filter, query ->
        val now = System.currentTimeMillis()
        trips.filter { trip ->
            val matchesFilter = when (filter) {
                "UPCOMING" -> trip.startDate > now
                "ACTIVE" -> now in trip.startDate..trip.endDate
                "COMPLETED" -> trip.endDate < now
                else -> true
            }
            val matchesQuery = query.isBlank() ||
                    trip.title.contains(query, ignoreCase = true) ||
                    trip.destination.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active or next upcoming trip for dashboard hero
    val activeOrNextTrip: StateFlow<Trip?> = allTrips.combine(flowOf(Unit)) { trips, _ ->
        val now = System.currentTimeMillis()
        val active = trips.firstOrNull { now in it.startDate..it.endDate }
        if (active != null) return@combine active

        trips.filter { it.startDate > now }.minByOrNull { it.startDate }
            ?: trips.maxByOrNull { it.startDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Selected trip details
    val selectedTrip: StateFlow<Trip?> = _selectedTripId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else repository.getTripById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedTripSchedules: StateFlow<List<ScheduleItem>> = _selectedTripId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getSchedulesForTrip(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedTripPacking: StateFlow<List<PackingItem>> = _selectedTripId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getPackingItemsForTrip(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedTripNotes: StateFlow<List<Note>> = _selectedTripId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getNotesForTrip(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedTripSpent: StateFlow<Double?> = _selectedTripId.flatMapLatest { id ->
        if (id == null) flowOf(0.0)
        else repository.getTotalSpentForTrip(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val selectedTripWeather: StateFlow<WeatherCache?> = selectedTrip.flatMapLatest { trip ->
        if (trip == null) flowOf(null)
        else repository.getWeather(trip.destination)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Upcoming schedules across all trips for Dashboard agenda
    val upcomingSchedules: StateFlow<List<ScheduleItem>> = repository.getUpcomingSchedules(
        fromTime = System.currentTimeMillis() - 86400000L,
        limit = 10
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All schedules
    val allSchedules: StateFlow<List<ScheduleItem>> = repository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All packing items
    val allPackingItems: StateFlow<List<PackingItem>> = repository.allPackingItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All notes filtered
    val filteredNotes: StateFlow<List<Note>> = combine(
        repository.allNotes,
        _notesCategoryFilter,
        _notesSearchQuery
    ) { notes, category, query ->
        notes.filter { note ->
            val matchesCategory = category == "ALL" || note.category == category
            val matchesQuery = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Start compass and location sensors
        compassSensor.startListening()
        if (locationHelper.hasLocationPermission()) {
            locationHelper.startLocationUpdates()
        }

        // Auto-refresh weather when online
        viewModelScope.launch {
            isOnline.collect { online ->
                if (online) {
                    refreshAllTripsWeather()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        compassSensor.stopListening()
        locationHelper.stopLocationUpdates()
    }

    // Telemetry functions
    fun startGpsTracking() {
        locationHelper.startLocationUpdates()
    }

    // Weather functions
    fun refreshWeatherForDestination(destination: String) {
        viewModelScope.launch {
            if (!isOnline.value) return@launch
            _isWeatherRefreshing.value = true
            val weather = weatherService.fetchWeatherForCity(destination)
            if (weather != null) {
                repository.cacheWeather(weather)
            }
            _isWeatherRefreshing.value = false
        }
    }

    fun refreshWeatherForCurrentGps() {
        viewModelScope.launch {
            val loc = currentLocation.value ?: return@launch
            if (!isOnline.value) return@launch
            _isWeatherRefreshing.value = true
            val weather = weatherService.fetchWeatherForCoordinates(
                locationName = "Current GPS Location",
                latitude = loc.latitude,
                longitude = loc.longitude
            )
            if (weather != null) {
                repository.cacheWeather(weather)
            }
            _isWeatherRefreshing.value = false
        }
    }

    fun refreshAllTripsWeather() {
        viewModelScope.launch {
            if (!isOnline.value) return@launch
            val destinations = allTrips.value.map { it.destination }.distinct()
            for (dest in destinations) {
                val weather = weatherService.fetchWeatherForCity(dest)
                if (weather != null) {
                    repository.cacheWeather(weather)
                }
            }
        }
    }

    // Filter controls
    fun setTripFilter(filter: String) {
        _tripFilter.value = filter
    }

    fun setTripSearchQuery(query: String) {
        _tripSearchQuery.value = query
    }

    fun setNotesSearchQuery(query: String) {
        _notesSearchQuery.value = query
    }

    fun setNotesCategoryFilter(category: String) {
        _notesCategoryFilter.value = category
    }

    fun selectTrip(tripId: Long?) {
        _selectedTripId.value = tripId
        if (tripId != null) {
            val trip = allTrips.value.find { it.id == tripId }
            trip?.let {
                if (isOnline.value) {
                    refreshWeatherForDestination(it.destination)
                }
            }
        }
    }

    // Trip CRUD
    fun saveTrip(
        id: Long = 0,
        title: String,
        destination: String,
        startDate: Long,
        endDate: Long,
        budget: Double,
        notes: String,
        category: String,
        colorHex: String,
        onSuccess: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val status = when {
                endDate < now -> "COMPLETED"
                now in startDate..endDate -> "ACTIVE"
                else -> "UPCOMING"
            }
            val trip = Trip(
                id = id,
                title = title.ifBlank { "Trip to $destination" },
                destination = destination,
                startDate = startDate,
                endDate = endDate,
                budget = budget,
                notes = notes,
                category = category,
                colorHex = colorHex,
                status = status
            )
            val newId = if (id == 0L) {
                repository.insertTrip(trip)
            } else {
                repository.updateTrip(trip)
                id
            }

            // Immediately fetch weather if online
            if (isOnline.value) {
                refreshWeatherForDestination(destination)
            }

            onSuccess(newId)
        }
    }

    fun deleteTrip(tripId: Long) {
        viewModelScope.launch {
            repository.deleteTripById(tripId)
            if (_selectedTripId.value == tripId) {
                _selectedTripId.value = null
            }
        }
    }

    // Schedules CRUD
    fun saveScheduleItem(
        id: Long = 0,
        tripId: Long,
        title: String,
        location: String,
        timestamp: Long,
        category: String,
        notes: String,
        cost: Double,
        reminderMinutes: Int
    ) {
        viewModelScope.launch {
            val item = ScheduleItem(
                id = id,
                tripId = tripId,
                title = title,
                location = location,
                timestamp = timestamp,
                category = category,
                notes = notes,
                cost = cost,
                reminderMinutesBefore = reminderMinutes
            )
            if (id == 0L) {
                repository.insertSchedule(item)
            } else {
                repository.updateSchedule(item)
            }
        }
    }

    fun deleteScheduleItem(id: Long) {
        viewModelScope.launch {
            repository.deleteScheduleById(id)
        }
    }

    fun toggleScheduleCompleted(id: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.setScheduleCompleted(id, completed)
        }
    }

    // Packing CRUD
    fun addPackingItem(
        tripId: Long,
        name: String,
        category: String,
        quantity: Int = 1,
        isEssential: Boolean = false
    ) {
        viewModelScope.launch {
            repository.insertPackingItem(
                PackingItem(
                    tripId = tripId,
                    name = name,
                    category = category,
                    quantity = quantity,
                    isPacked = false,
                    isEssential = isEssential
                )
            )
        }
    }

    fun togglePackingItem(id: Long, isPacked: Boolean) {
        viewModelScope.launch {
            repository.setPackingItemPacked(id, isPacked)
        }
    }

    fun updatePackingItem(item: PackingItem) {
        viewModelScope.launch {
            repository.updatePackingItem(item)
        }
    }

    fun updatePackingQuantity(id: Long, quantity: Int) {
        viewModelScope.launch {
            if (quantity > 0) {
                repository.updatePackingQuantity(id, quantity)
            }
        }
    }

    fun setAllPackingItemsPacked(tripId: Long, isPacked: Boolean) {
        viewModelScope.launch {
            repository.setAllPackedForTrip(tripId, isPacked)
        }
    }

    fun deletePackedItems(tripId: Long) {
        viewModelScope.launch {
            repository.deletePackedItemsForTrip(tripId)
        }
    }

    fun clearAllPackingForTrip(tripId: Long) {
        viewModelScope.launch {
            repository.clearPackingForTrip(tripId)
        }
    }

    fun deletePackingItem(id: Long) {
        viewModelScope.launch {
            repository.deletePackingItemById(id)
        }
    }

    fun applyPackingTemplate(tripId: Long, templateId: String) {
        viewModelScope.launch {
            val template = PackingTemplates.templates.find { it.id == templateId } ?: return@launch
            val items = PackingTemplates.toPackingItems(template, tripId)
            repository.insertPackingItems(items)
        }
    }

    // Notes CRUD
    fun saveNote(
        id: Long = 0,
        tripId: Long?,
        title: String,
        content: String,
        category: String,
        isPinned: Boolean
    ) {
        viewModelScope.launch {
            val note = Note(
                id = id,
                tripId = tripId,
                title = title,
                content = content,
                category = category,
                isPinned = isPinned,
                updatedAt = System.currentTimeMillis()
            )
            if (id == 0L) {
                repository.insertNote(note)
            } else {
                repository.updateNote(note)
            }
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNoteById(id)
        }
    }

    fun toggleNotePinned(id: Long, isPinned: Boolean) {
        viewModelScope.launch {
            repository.setNotePinned(id, isPinned)
        }
    }

    fun triggerTestReminder(context: Context, item: ScheduleItem) {
        NotificationHelper.showScheduleReminder(
            context = context,
            notificationId = item.id.toInt(),
            title = item.title,
            location = item.location,
            timeFormatted = DateUtils.formatDateTime(item.timestamp)
        )
    }

    // ---------------- EMERGENCY VAULT & CURRENCY ACTIONS ----------------

    fun saveVaultItem(
        id: Long = 0,
        title: String,
        category: String,
        docNumber: String,
        details: String,
        isImportant: Boolean
    ) {
        viewModelScope.launch {
            val item = VaultItem(
                id = id,
                title = title,
                category = category,
                documentNumber = docNumber,
                details = details,
                isImportant = isImportant
            )
            if (id == 0L) {
                repository.insertVaultItem(item)
            } else {
                repository.updateVaultItem(item)
            }
        }
    }

    fun deleteVaultItem(id: Long) {
        viewModelScope.launch {
            repository.deleteVaultItemById(id)
        }
    }

    fun refreshCurrencyRates() {
        viewModelScope.launch {
            if (!isOnline.value) return@launch
            _isRefreshingRates.value = true
            CurrencyConverter.refreshLiveRates()
            _isRefreshingRates.value = false
        }
    }

    // ---------------- GEMINI CHATBOT ACTIONS ----------------

    fun sendChatMessage(userText: String, tripContext: String? = null) {
        val query = userText.trim()
        if (query.isBlank()) return
        viewModelScope.launch {
            repository.insertChatMessage("user", query)
            _isAiResponding.value = true

            if (isOnline.value) {
                val currentHistory = repository.chatMessages.firstOrNull() ?: emptyList()
                val result = geminiService.sendMessage(currentHistory, query, tripContext)
                result.onSuccess { reply ->
                    repository.insertChatMessage("model", reply)
                }.onFailure { err ->
                    val fallback = buildSmartFallbackReply(query, tripContext, err.message)
                    repository.insertChatMessage("model", fallback)
                }
            } else {
                val offlineReply = buildOfflineTravelAdvice(query, tripContext)
                repository.insertChatMessage("model", offlineReply)
            }

            _isAiResponding.value = false
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
            repository.insertChatMessage(
                role = "model",
                text = "✨ Chat cleared. I am your Gemini Travel Copilot. Ask me about custom itineraries, packing lists, local dining, or offline safety tips!"
            )
        }
    }

    fun sendLiveVoiceQuery(userSpeech: String, tripContext: String?, onResponse: (String) -> Unit) {
        val query = userSpeech.trim()
        if (query.isBlank()) return
        viewModelScope.launch {
            repository.insertChatMessage("user", query)
            _isAiResponding.value = true

            val replyText = if (isOnline.value) {
                val currentHistory = repository.chatMessages.firstOrNull() ?: emptyList()
                val result = geminiService.sendLiveConversationMessage(currentHistory, query, tripContext)
                result.getOrElse { err ->
                    buildSmartFallbackReply(query, tripContext, err.message)
                }
            } else {
                buildOfflineTravelAdvice(query, tripContext)
            }

            repository.insertChatMessage("model", replyText)
            _isAiResponding.value = false
            onResponse(replyText)
        }
    }

    private fun buildSmartFallbackReply(query: String, tripContext: String?, errorMsg: String?): String {
        val baseAdvice = buildOfflineTravelAdvice(query, tripContext)
        return if (errorMsg?.contains("GEMINI_API_KEY", ignoreCase = true) == true ||
            errorMsg?.contains("not configured", ignoreCase = true) == true
        ) {
            "💡 *Tip: Add your `GEMINI_API_KEY` in the AI Studio Secrets panel for live generative intelligence.*\n\n$baseAdvice"
        } else {
            "⚠️ *Network issue reaching Gemini ($errorMsg). Here is offline intelligence from your travel knowledge base:*\n\n$baseAdvice"
        }
    }

    private fun buildOfflineTravelAdvice(query: String, tripContext: String?): String {
        val lower = query.lowercase()
        val contextSnippet = if (!tripContext.isNullOrBlank()) " for your destination ($tripContext)" else ""

        return when {
            lower.contains("pack") || lower.contains("bag") || lower.contains("luggage") -> {
                """
                🎒 **Smart Packing Strategy$contextSnippet:**
                
                • **Rule of 3s:** Pack 3 tops, 3 bottoms, 3 pairs of socks/underwear that mix-and-match in neutral tones.
                • **Essential Documents:** Passport/ID, physical copies of tickets, insurance card, offline hotel confirmations.
                • **Weather Defense:** Check the 'Tools > Weather' tab. Always pack a lightweight windproof/rain shell and compact umbrella.
                • **Electronics:** High-capacity power bank (≤100Wh for flights), universal adapter, charging cables.
                • **Health & Hygiene:** Small first-aid kit with pain relievers, antihistamines, rehydration salts, personal medication.
                
                *You can also import pre-built packing checklists under the **Packing** tab!*
                """.trimIndent()
            }
            lower.contains("itinerary") || lower.contains("plan") || lower.contains("day") || lower.contains("schedule") -> {
                """
                🗺️ **Smart Itinerary Framework$contextSnippet:**
                
                • **Morning (08:30 - 12:00):** Visit primary cultural landmarks, temples, or museums early before tour buses and heat peaks.
                • **Midday (12:30 - 14:30):** Indulge in authentic regional lunch near public markets; recharge at a local café.
                • **Afternoon (15:00 - 18:00):** Explore pedestrian neighborhoods, scenic parks, boutique artisan streets, or viewpoints.
                • **Evening (18:30 - 21:30):** Sunset photography at a high vantage point, street food night market, or local dinner.
                
                *Tap 'Add Schedule' inside your trip to schedule alerts for these stops!*
                """.trimIndent()
            }
            lower.contains("weather") || lower.contains("rain") || lower.contains("cold") || lower.contains("hot") -> {
                """
                ⛅ **Weather Advisory & Prep:**
                
                • **Offline Cache:** This app caches full temperature, wind, and humidity forecasts locally so you can check them without cell coverage.
                • **Sudden Showers:** Carry a packable rain poncho in your daypack.
                • **Sun & Heat:** Hydrate frequently (carry a refillable bottle) and use SPF 50+ sunscreen.
                • **Elevation & Mountains:** Check the 'Tools > GPS' tab for altitude telemetry; temperature drops ~6.5°C per 1,000 meters.
                """.trimIndent()
            }
            lower.contains("food") || lower.contains("eat") || lower.contains("restaurant") || lower.contains("dish") -> {
                """
                🍜 **Culinary Discovery Guide:**
                
                • **The 50-Foot Rule:** Walk one block away from major tourist monuments to find family-run eateries where locals eat.
                • **High Turnover:** Choose street vendors with continuous lines—fresh ingredients and fast turnaround.
                • **Regional Specialties:** Look for single-specialty restaurants (e.g., ramen shops, taquerias, trattorias) rather than places with 100-item menus.
                • **Food Safety:** Prefer freshly cooked, steaming-hot dishes and bottled or boiled water when traveling in new regions.
                """.trimIndent()
            }
            lower.contains("budget") || lower.contains("cost") || lower.contains("money") || lower.contains("expense") -> {
                """
                💰 **Travel Budgeting Rules:**
                
                • **40/30/20/10 Ratio:** 40% Accommodation & transit, 30% Dining & food, 20% Experiences & admissions, 10% Contingency emergency buffer.
                • **Offline Expense Tracking:** Record each expense under your trip in this app to see your budget progress bar in real-time.
                • **Local Currency:** Keep backup physical local cash for rural transport and small stalls that don't accept cards.
                """.trimIndent()
            }
            lower.contains("emergency") || lower.contains("safe") || lower.contains("lost") || lower.contains("help") -> {
                """
                🆘 **Offline Emergency Travel Rules:**
                
                • **Digital Compass & GPS:** Open the 'Tools > Compass' tab in this app to find cardinal direction (N/E/S/W) and copy your exact GPS latitude/longitude.
                • **Embassy & Police:** Store local emergency numbers (e.g. 112 in Europe, 911 in Americas, 110/119 in Japan) in your Notes tab.
                • **Hardcopies:** Keep physical photocopies of your passport photo page and visa in a separate waterproof pouch in your luggage.
                • **Meeting Point:** Always designate a fixed landmark meeting point if traveling with companions.
                """.trimIndent()
            }
            else -> {
                """
                ✈️ **Gemini Travel Copilot:**
                
                I'm here to make your trip effortless! Here are things I can assist with:
                • **Day-by-Day Itineraries:** Suggest morning, afternoon, and evening schedules.
                • **Packing Checklists:** Custom packing advice for tropical, winter, or business trips.
                • **Local Insights:** Hidden gems, navigation advice, street food, and budget hacks.
                • **Emergency Navigation:** Use the app's hardware compass and offline GPS coordinates anytime.
                
                *Ask me any specific question about your destination, packing list, or itinerary!*
                """.trimIndent()
            }
        }
    }
}


class PlannerViewModelFactory(
    private val repository: PlannerRepository,
    private val networkMonitor: NetworkMonitor,
    private val locationHelper: LocationHelper,
    private val compassSensor: CompassSensor
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlannerViewModel::class.java)) {
            return PlannerViewModel(
                repository = repository,
                networkMonitor = networkMonitor,
                locationHelper = locationHelper,
                compassSensor = compassSensor
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
