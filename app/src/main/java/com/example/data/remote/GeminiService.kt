package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val modelName = "gemini-3.5-flash"

    suspend fun sendMessage(
        history: List<ChatMessage>,
        userMessage: String,
        tripContext: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }

            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(
                    IllegalStateException(
                        "Gemini API key is not configured. Please add your GEMINI_API_KEY in the Secrets panel in AI Studio."
                    )
                )
            }

            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            // Construct System Instruction
            var systemPrompt = "You are Gemini Travel Copilot, an expert AI travel guide and itinerary assistant embedded inside Smart Offline Planner. " +
                    "Your role is to help travelers with day-by-day itineraries, smart packing advice tailored to destinations and weather, " +
                    "hidden gems, local street foods, emergency travel phrases, and budget tips. " +
                    "Keep answers concise, clear, engaging, and well-structured with bullet points and emojis."

            if (!tripContext.isNullOrBlank()) {
                systemPrompt += "\n\nCurrent user trip context in app: $tripContext"
            }

            val rootJson = JSONObject()

            // System Instruction
            val sysInstructionObj = JSONObject().apply {
                val partsArray = JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                }
                put("parts", partsArray)
            }
            rootJson.put("systemInstruction", sysInstructionObj)

            // Conversation history + new prompt
            val contentsArray = JSONArray()

            // Add previous history turns (limit to last 12 messages for fast tokens)
            val recentHistory = history.takeLast(12)
            for (msg in recentHistory) {
                val turnObj = JSONObject().apply {
                    put("role", if (msg.role == "user") "user" else "model")
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", msg.text) })
                    }
                    put("parts", parts)
                }
                contentsArray.put(turnObj)
            }

            // Add current user prompt
            val userTurn = JSONObject().apply {
                put("role", "user")
                val parts = JSONArray().apply {
                    put(JSONObject().apply { put("text", userMessage) })
                }
                put("parts", parts)
            }
            contentsArray.put(userTurn)

            rootJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 1200)
            }
            rootJson.put("generationConfig", genConfig)

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    errJson.getJSONObject("error").getString("message")
                } catch (e: Exception) {
                    "API Error (${response.code})"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("No response received from Gemini."))
            }

            val firstCandidate = candidates.getJSONObject(0)
            val contentObj = firstCandidate.getJSONObject("content")
            val parts = contentObj.getJSONArray("parts")
            val answerText = parts.getJSONObject(0).getString("text")

            Result.success(answerText)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun sendLiveConversationMessage(
        history: List<ChatMessage>,
        userSpeechText: String,
        tripContext: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please add your GEMINI_API_KEY in the Secrets panel in AI Studio.")
            )
        }

        // Live conversational system instruction
        var systemPrompt = "You are Gemini Live Voice Copilot for travel, running the real-time gemini-3.8-live model. " +
                "You are having a direct, spoken voice conversation with a traveler. " +
                "Speak naturally, warmly, concisely, and conversationally in 2 to 3 sentences maximum per turn. " +
                "Avoid markdown bullets, markdown tables, asterisks, or unpronounceable characters because your reply is spoken aloud directly into the user's ears."

        if (!tripContext.isNullOrBlank()) {
            systemPrompt += "\n\nActive Trip Context: $tripContext"
        }

        val primaryModel = "gemini-3.8-live"
        val fallbackModel = "gemini-3.5-flash"

        // First attempt with gemini-3.8-live
        val primaryResult = executeGeneration(primaryModel, apiKey, systemPrompt, history, userSpeechText)
        if (primaryResult.isSuccess) {
            return@withContext primaryResult
        }

        // Graceful fallback to gemini-3.5-flash if live model endpoint is unavailable
        executeGeneration(fallbackModel, apiKey, systemPrompt, history, userSpeechText)
    }

    private fun executeGeneration(
        model: String,
        apiKey: String,
        systemPrompt: String,
        history: List<ChatMessage>,
        userPrompt: String
    ): Result<String> {
        return try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            val rootJson = JSONObject()

            // System instruction
            val sysInstructionObj = JSONObject().apply {
                val partsArray = JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                }
                put("parts", partsArray)
            }
            rootJson.put("systemInstruction", sysInstructionObj)

            // Conversation history (limit to last 8 messages for low latency live voice)
            val contentsArray = JSONArray()
            val recent = history.takeLast(8)
            for (msg in recent) {
                contentsArray.put(JSONObject().apply {
                    put("role", if (msg.role == "user") "user" else "model")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", msg.text) })
                    })
                })
            }

            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", userPrompt) })
                })
            })
            rootJson.put("contents", contentsArray)

            // Low-latency voice configuration
            val genConfig = JSONObject().apply {
                put("temperature", 0.6)
                put("maxOutputTokens", 400)
            }
            rootJson.put("generationConfig", genConfig)

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody).getJSONObject("error").getString("message")
                } catch (e: Exception) {
                    "API Error (${response.code})"
                }
                return Result.failure(Exception(errorMsg))
            }

            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates") ?: return Result.failure(Exception("No candidate in response"))
            if (candidates.length() == 0) return Result.failure(Exception("Empty candidates"))

            val answerText = candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            Result.success(answerText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
