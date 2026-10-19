package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // "PASSPORT", "VISA", "INSURANCE", "TICKET", "EMERGENCY_CONTACT", "MEDICAL"
    val documentNumber: String = "",
    val details: String = "",
    val expiryDate: Long? = null,
    val isImportant: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
