package com.example.data.templates

import com.example.data.model.PackingItem

data class PackingTemplate(
    val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String,
    val items: List<TemplateItem>
)

data class TemplateItem(
    val name: String,
    val category: String,
    val quantity: Int = 1,
    val isEssential: Boolean = false
)

object PackingTemplates {
    val templates = listOf(
        PackingTemplate(
            id = "weekend",
            name = "Weekend Getaway",
            description = "Lightweight essentials for a 2-3 day quick trip",
            iconEmoji = "🎒",
            items = listOf(
                TemplateItem("ID & Driver's License", "Documents", 1, true),
                TemplateItem("Wallet & Payment Cards", "Documents", 1, true),
                TemplateItem("Hotel / Stay Confirmation", "Documents", 1, false),
                TemplateItem("Casual Shirts", "Clothing", 3, true),
                TemplateItem("Pants / Jeans", "Clothing", 2, true),
                TemplateItem("Underwear & Socks", "Clothing", 3, true),
                TemplateItem("Light Jacket / Hoodie", "Clothing", 1, false),
                TemplateItem("Toothbrush & Toothpaste", "Toiletries", 1, true),
                TemplateItem("Deodorant", "Toiletries", 1, true),
                TemplateItem("Hairbrush / Comb", "Toiletries", 1, false),
                TemplateItem("Phone Charger & Cable", "Electronics", 1, true),
                TemplateItem("Power Bank", "Electronics", 1, false),
                TemplateItem("Earbuds / Headphones", "Electronics", 1, false)
            )
        ),
        PackingTemplate(
            id = "beach",
            name = "Beach & Island Holiday",
            description = "Sun protection, swimwear, and seaside relaxation",
            iconEmoji = "🏖️",
            items = listOf(
                TemplateItem("Passport & Tickets", "Documents", 1, true),
                TemplateItem("Travel Insurance Info", "Documents", 1, true),
                TemplateItem("Swimwear", "Clothing", 2, true),
                TemplateItem("Sun Hat / Cap", "Clothing", 1, false),
                TemplateItem("Sunglasses (UV Protected)", "Clothing", 1, true),
                TemplateItem("Flip Flops / Beach Slides", "Clothing", 1, true),
                TemplateItem("Breathable Linen Shirts", "Clothing", 3, false),
                TemplateItem("Sunscreen SPF 50+", "Toiletries", 1, true),
                TemplateItem("Aloe Vera Gel", "Toiletries", 1, false),
                TemplateItem("Lip Balm with SPF", "Toiletries", 1, false),
                TemplateItem("Waterproof Phone Pouch", "Gear", 1, true),
                TemplateItem("Microfiber Beach Towel", "Gear", 1, false),
                TemplateItem("Waterproof Bluetooth Speaker", "Electronics", 1, false),
                TemplateItem("Insect Repellent", "Health", 1, false)
            )
        ),
        PackingTemplate(
            id = "business",
            name = "Business & Conference",
            description = "Professional attire, presentation tech, and work tools",
            iconEmoji = "💼",
            items = listOf(
                TemplateItem("Govt ID / Passport", "Documents", 1, true),
                TemplateItem("Company ID & Business Cards", "Documents", 10, true),
                TemplateItem("Hotel & Flight Itinerary", "Documents", 1, true),
                TemplateItem("Business Suit / Blazer", "Clothing", 2, true),
                TemplateItem("Dress Shirts / Blouses", "Clothing", 3, true),
                TemplateItem("Dress Shoes & Belt", "Clothing", 1, true),
                TemplateItem("Tie / Scarf", "Clothing", 2, false),
                TemplateItem("Laptop & Power Brick", "Electronics", 1, true),
                TemplateItem("USB-C / HDMI Display Dongle", "Electronics", 1, true),
                TemplateItem("Noise-Cancelling Headphones", "Electronics", 1, true),
                TemplateItem("Presenter Clicker / Mouse", "Electronics", 1, false),
                TemplateItem("Grooming & Shaving Kit", "Toiletries", 1, true),
                TemplateItem("Cologne / Perfume (Travel Size)", "Toiletries", 1, false),
                TemplateItem("Notepad & Signature Pen", "Gear", 1, false)
            )
        ),
        PackingTemplate(
            id = "outdoor",
            name = "Hiking & Wilderness",
            description = "Rugged gear, weather layers, and trail safety equipment",
            iconEmoji = "🥾",
            items = listOf(
                TemplateItem("Offline Maps & Permits", "Documents", 1, true),
                TemplateItem("Emergency Whistle & ID Card", "Documents", 1, true),
                TemplateItem("Waterproof Hiking Boots", "Clothing", 1, true),
                TemplateItem("Merino Wool Hiking Socks", "Clothing", 3, true),
                TemplateItem("Moisture-Wicking Tops", "Clothing", 2, true),
                TemplateItem("Hard-Shell Rain Jacket", "Clothing", 1, true),
                TemplateItem("Fleece Thermal Mid-layer", "Clothing", 1, false),
                TemplateItem("Trekking Poles", "Gear", 1, false),
                TemplateItem("LED Headlamp + Extra Batteries", "Gear", 1, true),
                TemplateItem("Multi-tool Pocket Knife", "Gear", 1, true),
                TemplateItem("Water Filter Bottle / Bladder", "Gear", 1, true),
                TemplateItem("Compact First Aid Kit", "Health", 1, true),
                TemplateItem("Blister Prevention Plasters", "Health", 1, true),
                TemplateItem("Emergency Mylar Blanket", "Health", 1, false)
            )
        ),
        PackingTemplate(
            id = "international",
            name = "International Flight & Travel",
            description = "Long haul flight essentials, foreign currency, and adapters",
            iconEmoji = "✈️",
            items = listOf(
                TemplateItem("Original Passport (6+ mo valid)", "Documents", 1, true),
                TemplateItem("Visa / Entry Authorization", "Documents", 1, true),
                TemplateItem("Foreign Currency Cash", "Documents", 1, true),
                TemplateItem("Photocopy of Passports", "Documents", 2, false),
                TemplateItem("Universal Travel Plug Adapter", "Electronics", 1, true),
                TemplateItem("eSIM QR Code / Roaming Card", "Electronics", 1, true),
                TemplateItem("Fast Power Bank (10,000+ mAh)", "Electronics", 1, true),
                TemplateItem("Memory Neck Pillow", "Gear", 1, false),
                TemplateItem("Sleeping Eye Mask & Earplugs", "Gear", 1, true),
                TemplateItem("Compression Socks for Flight", "Clothing", 1, false),
                TemplateItem("Hand Sanitizer & Disinfectant Wipes", "Health", 1, true),
                TemplateItem("Prescription Medicines (in box)", "Health", 1, true),
                TemplateItem("Motion Sickness & Pain Relief Pills", "Health", 1, false)
            )
        )
    )

    fun toPackingItems(template: PackingTemplate, tripId: Long): List<PackingItem> {
        return template.items.map { item ->
            PackingItem(
                tripId = tripId,
                name = item.name,
                category = item.category,
                quantity = item.quantity,
                isPacked = false,
                isEssential = item.isEssential
            )
        }
    }
}
