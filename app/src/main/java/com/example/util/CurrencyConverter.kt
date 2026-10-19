package com.example.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class CurrencyRate(
    val code: String,
    val name: String,
    val symbol: String,
    val rateToUsd: Double // 1 USD = rateToUsd in target currency
)

object CurrencyConverter {

    // Reliable baseline offline rates (Updated relative to 1 USD)
    private val baselineRates = mutableMapOf(
        "USD" to CurrencyRate("USD", "US Dollar", "$", 1.0),
        "EUR" to CurrencyRate("EUR", "Euro", "€", 0.92),
        "GBP" to CurrencyRate("GBP", "British Pound", "£", 0.79),
        "JPY" to CurrencyRate("JPY", "Japanese Yen", "¥", 152.4),
        "BDT" to CurrencyRate("BDT", "Bangladeshi Taka", "৳", 121.5),
        "INR" to CurrencyRate("INR", "Indian Rupee", "₹", 86.8),
        "CAD" to CurrencyRate("CAD", "Canadian Dollar", "C$", 1.41),
        "AUD" to CurrencyRate("AUD", "Australian Dollar", "A$", 1.58),
        "CHF" to CurrencyRate("CHF", "Swiss Franc", "CHF", 0.89),
        "SGD" to CurrencyRate("SGD", "Singapore Dollar", "S$", 1.34),
        "AED" to CurrencyRate("AED", "UAE Dirham", "AED", 3.67),
        "SAR" to CurrencyRate("SAR", "Saudi Riyal", "SAR", 3.75),
        "THB" to CurrencyRate("THB", "Thai Baht", "฿", 34.2),
        "MYR" to CurrencyRate("MYR", "Malaysian Ringgit", "RM", 4.45),
        "CNY" to CurrencyRate("CNY", "Chinese Yuan", "¥", 7.28),
        "KRW" to CurrencyRate("KRW", "South Korean Won", "₩", 1435.0),
        "TRY" to CurrencyRate("TRY", "Turkish Lira", "₺", 36.5),
        "IDR" to CurrencyRate("IDR", "Indonesian Rupiah", "Rp", 16250.0),
        "VND" to CurrencyRate("VND", "Vietnamese Dong", "₫", 25400.0),
        "PHP" to CurrencyRate("PHP", "Philippine Peso", "₱", 58.3),
        "NZD" to CurrencyRate("NZD", "New Zealand Dollar", "NZ$", 1.73),
        "BRL" to CurrencyRate("BRL", "Brazilian Real", "R$", 5.75),
        "ZAR" to CurrencyRate("ZAR", "South African Rand", "R", 18.2)
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun getAvailableCurrencies(): List<CurrencyRate> {
        return baselineRates.values.toList().sortedBy { it.code }
    }

    fun convert(amount: Double, fromCode: String, toCode: String): Double {
        val fromRate = baselineRates[fromCode]?.rateToUsd ?: 1.0
        val toRate = baselineRates[toCode]?.rateToUsd ?: 1.0

        // Convert to USD first, then to target
        val amountInUsd = amount / fromRate
        return amountInUsd * toRate
    }

    fun getSymbol(code: String): String {
        return baselineRates[code]?.symbol ?: code
    }

    suspend fun refreshLiveRates(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val url = "https://open.er-api.com/v6/latest/USD"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))

            val json = JSONObject(body)
            if (json.optString("result") == "success") {
                val ratesObj = json.getJSONObject("rates")
                var updatedCount = 0
                for (code in baselineRates.keys) {
                    if (ratesObj.has(code)) {
                        val liveRate = ratesObj.getDouble(code)
                        val current = baselineRates[code]
                        if (current != null) {
                            baselineRates[code] = current.copy(rateToUsd = liveRate)
                            updatedCount++
                        }
                    }
                }
                Result.success(updatedCount)
            } else {
                Result.failure(Exception("API reported failure"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
