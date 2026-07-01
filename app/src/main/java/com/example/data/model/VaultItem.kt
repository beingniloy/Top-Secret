package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,               // PASSWORD, NOTE, CARD, DOCUMENT, TOTP, GALLERY, IDENTITY, QR, SECURE_CONTACT, WILL
    val title: String,              // website, note title, card name, doc name, TOTP issuer, contact name
    val subtitle: String,           // username, note preview, hidden card number, TOTP email/label, contact phone
    val encryptedContent1: String,  // password, full note text/checklist, card detail JSON, TOTP secret, doc path/uri
    val encryptedContent2: String,  // custom fields JSON, attachment list, CVV/Expiry, OCR text/backup codes, contact email
    val category: String = "",      // folders/categories
    val tags: String = "",          // comma separated tags
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val isTrash: Boolean = false,
    val isFake: Boolean = false,    // True if created in Fake Vault mode
    val timestamp: Long = System.currentTimeMillis(),
    val cardColorHex: String = "#6750A4" // For realistic wallet cards
)
