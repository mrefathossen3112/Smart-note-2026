package com.example.util

data class CountryEmergency(
    val country: String,
    val flag: String,
    val police: String,
    val ambulance: String,
    val fire: String,
    val touristHelpline: String? = null
)

object EmergencyDirectory {

    val directory: List<CountryEmergency> = listOf(
        CountryEmergency("Bangladesh", "🇧🇩", "999", "999", "999", "+8801320222222"),
        CountryEmergency("United States & Canada", "🇺🇸", "911", "911", "911", null),
        CountryEmergency("United Kingdom", "🇬🇧", "999", "999", "999", "111 (Non-Emergency)"),
        CountryEmergency("European Union (All EU)", "🇪🇺", "112", "112", "112", "112"),
        CountryEmergency("Japan", "🇯🇵", "110", "119", "119", "050-3816-2720 (JNTO)"),
        CountryEmergency("Thailand", "🇹🇭", "191", "1669", "199", "1155 (Tourist Police)"),
        CountryEmergency("India", "🇮🇳", "112", "102", "101", "1363 (Tourist Helpline)"),
        CountryEmergency("United Arab Emirates (UAE)", "🇦🇪", "999", "998", "997", "800-4888"),
        CountryEmergency("Saudi Arabia", "🇸🇦", "999", "997", "998", "930 (Tourism)"),
        CountryEmergency("Singapore", "🇸🇬", "999", "995", "995", "1800-736-2000"),
        CountryEmergency("Malaysia", "🇲🇾", "999", "999", "999", "1300-88-5005"),
        CountryEmergency("Turkey", "🇹🇷", "112", "112", "112", "179 (Tourism)"),
        CountryEmergency("Australia", "🇦🇺", "000", "000", "000", null),
        CountryEmergency("Switzerland", "🇨🇭", "117", "144", "118", "112")
    )
}
