package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChatMessage
import com.example.util.VoiceInteractionManager
import com.example.util.VoiceState

data class VoiceMessageEntry(
    val role: String, // "user" or "gemini"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiLiveVoiceScreen(
    isOnline: Boolean,
    tripContext: String? = null,
    onSendVoiceQuery: (String, (String) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val voiceManager = remember { VoiceInteractionManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
        }
    }

    val voiceState by voiceManager.voiceState.collectAsStateWithLifecycle()
    val partialTranscription by voiceManager.partialTranscription.collectAsStateWithLifecycle()
    val audioLevelRms by voiceManager.audioLevelRms.collectAsStateWithLifecycle()
    val errorMessage by voiceManager.errorMessage.collectAsStateWithLifecycle()

    var hasRecordAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasRecordAudioPermission = granted
            if (granted) {
                voiceManager.startListening()
            }
        }
    )

    val conversationLog = remember { mutableStateListOf<VoiceMessageEntry>() }
    var isMuted by remember { mutableStateOf(false) }

    // Wire speech result callback to Gemini Live
    LaunchedEffect(Unit) {
        voiceManager.onFinalSpeechResult = { recognizedText ->
            if (recognizedText.isNotBlank()) {
                conversationLog.add(VoiceMessageEntry(role = "user", text = recognizedText))
                voiceManager.setProcessing()
                onSendVoiceQuery(recognizedText) { aiResponse ->
                    conversationLog.add(VoiceMessageEntry(role = "gemini", text = aiResponse))
                    if (!isMuted) {
                        voiceManager.speak(aiResponse)
                    }
                }
            }
        }

        // Add welcome introduction
        if (conversationLog.isEmpty()) {
            conversationLog.add(
                VoiceMessageEntry(
                    role = "gemini",
                    text = "Hello! I am Gemini Live. You can speak with me in real time about your trip, itineraries, or travel tips. Tap the microphone and say something!"
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF6366F1).copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF6366F1),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Gemini Live",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Live badge
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isOnline) Color(0xFF10B981) else Color(0xFFF59E0B),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "gemini-3.8-live",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (tripContext != null) "Context: $tripContext" else "Real-time Voice Conversation",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("gemini_live_back_button")
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
                            isMuted = !isMuted
                            if (isMuted) {
                                voiceManager.stopSpeaking()
                            }
                        },
                        modifier = Modifier.testTag("gemini_live_mute_toggle")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) "Unmute Audio" else "Mute Audio",
                            tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Live Status Banner
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Color(0xFF10B981) else Color(0xFFF59E0B))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOnline) "Real-Time Live API Active" else "Offline Voice Simulation Active",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOnline) Color(0xFF047857) else Color(0xFFB45309)
                        )
                    }

                    if (voiceState == VoiceState.SPEAKING) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Speaking…",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Central Animated Gemini Live Orb
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                GeminiLiveOrb(
                    voiceState = voiceState,
                    audioLevelRms = audioLevelRms,
                    onClick = {
                        if (!hasRecordAudioPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            when (voiceState) {
                                VoiceState.LISTENING -> voiceManager.stopListening()
                                VoiceState.SPEAKING -> voiceManager.stopSpeaking()
                                else -> voiceManager.startListening()
                            }
                        }
                    }
                )
            }

            // Voice State Subtitle
            val statusMessage = when (voiceState) {
                VoiceState.LISTENING -> "Listening to your voice… Speak now"
                VoiceState.PROCESSING -> "Gemini 3.8 Live is thinking…"
                VoiceState.SPEAKING -> "Gemini is speaking response…"
                VoiceState.ERROR -> errorMessage ?: "Voice recognition error"
                VoiceState.IDLE -> "Tap microphone or orb to talk with Gemini Live"
            }

            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = when (voiceState) {
                    VoiceState.LISTENING -> MaterialTheme.colorScheme.primary
                    VoiceState.SPEAKING -> Color(0xFF10B981)
                    VoiceState.ERROR -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            // Partial Real-Time Transcription Pill
            if (partialTranscription.isNotBlank() && voiceState == VoiceState.LISTENING) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "\"$partialTranscription\"",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Voice Topic Suggestions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VoiceQuickChip(
                    text = "Plan 3-day itinerary",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        conversationLog.add(VoiceMessageEntry("user", "Plan a 3-day itinerary for my trip"))
                        voiceManager.setProcessing()
                        onSendVoiceQuery("Plan a 3-day itinerary for my trip") { reply ->
                            conversationLog.add(VoiceMessageEntry("gemini", reply))
                            if (!isMuted) voiceManager.speak(reply)
                        }
                    }
                )
                VoiceQuickChip(
                    text = "Where to eat tonight?",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        conversationLog.add(VoiceMessageEntry("user", "Recommend top local foods and places to eat tonight"))
                        voiceManager.setProcessing()
                        onSendVoiceQuery("Recommend top local foods and places to eat tonight") { reply ->
                            conversationLog.add(VoiceMessageEntry("gemini", reply))
                            if (!isMuted) voiceManager.speak(reply)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Conversation transcript history
            Text(
                text = "Live Conversation Transcript",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conversationLog) { item ->
                    VoiceBubble(entry = item)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Audio Control Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 6.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (voiceState == VoiceState.SPEAKING) {
                        IconButton(
                            onClick = { voiceManager.stopSpeaking() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Interrupt / Stop",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                if (conversationLog.isNotEmpty()) {
                                    val lastGemini = conversationLog.lastOrNull { it.role == "gemini" }
                                    lastGemini?.let { voiceManager.speak(it.text) }
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Replay Last Response",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Main Mic Action Button
                    Surface(
                        shape = CircleShape,
                        color = when (voiceState) {
                            VoiceState.LISTENING -> Color(0xFFEF4444)
                            VoiceState.PROCESSING -> Color(0xFF6366F1)
                            VoiceState.SPEAKING -> Color(0xFF10B981)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier
                            .size(68.dp)
                            .clickable {
                                if (!hasRecordAudioPermission) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    if (voiceState == VoiceState.LISTENING) {
                                        voiceManager.stopListening()
                                    } else {
                                        voiceManager.startListening()
                                    }
                                }
                            }
                            .testTag("gemini_live_mic_fab")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (voiceState == VoiceState.LISTENING) Icons.Default.Mic else Icons.Default.GraphicEq,
                                contentDescription = "Speak",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            conversationLog.clear()
                            voiceManager.stopSpeaking()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MicOff,
                            contentDescription = "Clear Session",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GeminiLiveOrb(
    voiceState: VoiceState,
    audioLevelRms: Float,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rmsScale by animateFloatAsState(
        targetValue = if (voiceState == VoiceState.LISTENING) 1f + (audioLevelRms / 15f) else 1f,
        label = "rms_scale"
    )

    val gradient = when (voiceState) {
        VoiceState.LISTENING -> Brush.radialGradient(
            colors = listOf(
                Color(0xFFEF4444),
                Color(0xFFF59E0B),
                Color(0xFF8B5CF6)
            )
        )
        VoiceState.SPEAKING -> Brush.radialGradient(
            colors = listOf(
                Color(0xFF10B981),
                Color(0xFF06B6D4),
                Color(0xFF3B82F6)
            )
        )
        VoiceState.PROCESSING -> Brush.radialGradient(
            colors = listOf(
                Color(0xFF6366F1),
                Color(0xFFA855F7),
                Color(0xFFEC4899)
            )
        )
        else -> Brush.radialGradient(
            colors = listOf(
                Color(0xFF3B82F6),
                Color(0xFF6366F1),
                Color(0xFF1E1B4B)
            )
        )
    }

    Box(
        modifier = Modifier
            .size(150.dp)
            .scale(if (voiceState == VoiceState.PROCESSING || voiceState == VoiceState.SPEAKING) pulseScale else rmsScale)
            .clip(CircleShape)
            .background(gradient)
            .clickable { onClick() }
            .testTag("gemini_live_orb"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (voiceState) {
                VoiceState.LISTENING -> Icons.Default.Mic
                VoiceState.SPEAKING -> Icons.Default.GraphicEq
                VoiceState.PROCESSING -> Icons.Default.AutoAwesome
                else -> Icons.Default.AutoAwesome
            },
            contentDescription = "Gemini Live",
            tint = Color.White,
            modifier = Modifier.size(54.dp)
        )
    }
}

@Composable
private fun VoiceQuickChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun VoiceBubble(entry: VoiceMessageEntry) {
    val isUser = entry.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isUser) "You" else "Gemini Live",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) MaterialTheme.colorScheme.primary else Color(0xFF6366F1)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = entry.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
