package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.crypto.CryptoManager
import com.example.data.db.AppDatabase
import com.example.data.model.VaultItem
import com.example.data.prefs.VaultPrefs
import com.example.data.repository.VaultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class VaultViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = VaultPrefs(application)
    private val db = AppDatabase.getDatabase(application)
    private val repository = VaultRepository(db.vaultDao())

    // UI States
    val isInitialized = prefs.isInitialized.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val themeSelection = prefs.themeSelection.stateIn(viewModelScope, SharingStarted.Eagerly, "system")
    val cardStyle = prefs.cardStyle.stateIn(viewModelScope, SharingStarted.Eagerly, "realistic")
    val clipboardTimeoutSec = prefs.clipboardTimeoutSec.stateIn(viewModelScope, SharingStarted.Eagerly, 30)
    val screenshotsBlocked = prefs.screenshotsBlocked.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val recentAppBlur = prefs.recentAppBlur.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val recoveryHint = prefs.recoveryHint.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val masterPasswordHash = prefs.masterPasswordHash.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val masterPasswordSalt = prefs.masterPasswordSalt.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val fakePin = prefs.fakePin.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val panicPin = prefs.panicPin.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val biometricsEnabled = prefs.biometricsEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val autoLockMinutes = prefs.autoLockMinutes.stateIn(viewModelScope, SharingStarted.Eagerly, 5)

    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked.asStateFlow()

    private val _isFakeVaultActive = MutableStateFlow(false)
    val isFakeVaultActive: StateFlow<Boolean> = _isFakeVaultActive.asStateFlow()

    private val _wrongAttempts = MutableStateFlow(0)
    val wrongAttempts: StateFlow<Int> = _wrongAttempts.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL") // ALL, PASSWORD, NOTE, CARD, DOCUMENT, TOTP, GALLERY, IDENTITY, TRASH
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Loaded items filtered by Real/Fake mode
    private val _rawItems = MutableStateFlow<List<VaultItem>>(emptyList())
    
    // Reactively observe items based on fake mode
    init {
        viewModelScope.launch {
            _isFakeVaultActive.collectLatest { isFake ->
                repository.getAllItems(isFake).collect { list ->
                    _rawItems.value = list
                }
            }
        }
    }

    // Filtered items based on category and search query
    val uiItems: StateFlow<List<VaultItem>> = combine(_rawItems, _selectedCategory, _searchQuery) { items, category, query ->
        items.filter { item ->
            val matchesCategory = when (category) {
                "ALL" -> !item.isTrash
                "TRASH" -> item.isTrash
                else -> !item.isTrash && item.type == category
            }
            val matchesSearch = query.isEmpty() || 
                    item.title.contains(query, ignoreCase = true) || 
                    item.subtitle.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true) ||
                    item.tags.contains(query, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statistics
    val stats = _rawItems.map { list ->
        val total = list.size
        val passwords = list.count { it.type == "PASSWORD" && !it.isTrash }
        val notes = list.count { it.type == "NOTE" && !it.isTrash }
        val cards = list.count { it.type == "CARD" && !it.isTrash }
        val totps = list.count { it.type == "TOTP" && !it.isTrash }
        val docs = list.count { it.type == "DOCUMENT" && !it.isTrash }
        val gallery = list.count { it.type == "GALLERY" && !it.isTrash }
        val identity = list.count { it.type == "IDENTITY" && !it.isTrash }
        
        VaultStats(total, passwords, notes, cards, totps, docs, gallery, identity)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VaultStats())

    // Onboarding / First Launch Setup
    fun initializeVault(password: String, hint: String, fakePin: String?, panicPin: String?) {
        viewModelScope.launch(Dispatchers.Default) {
            val salt = CryptoManager.generateSalt()
            val hash = CryptoManager.hashPassword(password, salt)
            
            prefs.saveOnboarding(hash, salt, hint)
            prefs.setFakePin(fakePin)
            prefs.setPanicPin(panicPin)
            
            // Auto unlock on initialization
            CryptoManager.setSessionKeyFromPassword(password, salt)
            _isFakeVaultActive.value = false
            _unlocked.value = true
            insertSampleData()
        }
    }

    // Unlock
    fun unlockVault(input: String): Boolean {
        var success = false
        val masterHash = masterPasswordHash.value
        val masterSalt = masterPasswordSalt.value
        val fPin = fakePin.value
        val pPin = panicPin.value

        if (pPin != null && input == pPin) {
            // Panic PIN triggered! Self-destruct sequence!
            triggerSelfDestruct()
            return false
        }

        if (fPin != null && input == fPin) {
            // Unlocked in Fake Mode! Generate a temporary fake password session
            val fakeSalt = CryptoManager.generateSalt()
            CryptoManager.setSessionKeyFromPassword("fake_vault_pwd_123", fakeSalt)
            _isFakeVaultActive.value = true
            _unlocked.value = true
            _wrongAttempts.value = 0
            return true
        }

        if (masterHash != null && masterSalt != null) {
            val derivedHash = CryptoManager.hashPassword(input, masterSalt)
            if (derivedHash == masterHash) {
                CryptoManager.setSessionKeyFromPassword(input, masterSalt)
                _isFakeVaultActive.value = false
                _unlocked.value = true
                _wrongAttempts.value = 0
                success = true
            } else {
                _wrongAttempts.value += 1
            }
        }
        return success
    }

    fun manualLockVault() {
        CryptoManager.clearSessionKey()
        _unlocked.value = false
        _isFakeVaultActive.value = false
    }

    fun autoLockVault() {
        _unlocked.value = false
    }

    fun unlockVaultWithBiometrics(): Boolean {
        if (CryptoManager.hasSessionKey()) {
            _unlocked.value = true
            _wrongAttempts.value = 0
            return true
        }
        return false
    }

    private fun triggerSelfDestruct() {
        viewModelScope.launch(Dispatchers.IO) {
            // Delete all entries from the database securely
            db.clearAllTables()
            prefs.clearAllData()
            _unlocked.value = false
            _isFakeVaultActive.value = false
            _wrongAttempts.value = 0
        }
    }

    // Setters for Settings
    fun updateTheme(theme: String) = viewModelScope.launch { prefs.setThemeSelection(theme) }
    fun updateCardStyle(style: String) = viewModelScope.launch { prefs.setCardStyle(style) }
    fun updateClipboardTimeout(seconds: Int) = viewModelScope.launch { prefs.setClipboardTimeout(seconds) }
    fun updateScreenshotsBlocked(blocked: Boolean) = viewModelScope.launch { prefs.setScreenshotsBlocked(blocked) }
    fun updateRecentAppBlur(blur: Boolean) = viewModelScope.launch { prefs.setRecentAppBlur(blur) }
    fun updateFakePin(pin: String?) = viewModelScope.launch { prefs.setFakePin(pin) }
    fun updatePanicPin(pin: String?) = viewModelScope.launch { prefs.setPanicPin(pin) }
    fun updateBiometricsEnabled(enabled: Boolean) = viewModelScope.launch { prefs.setBiometricsEnabled(enabled) }
    fun updateAutoLockMinutes(minutes: Int) = viewModelScope.launch { prefs.setAutoLockMinutes(minutes) }

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setCategory(category: String) { _selectedCategory.value = category }

    // Vault Item Actions
    fun addVaultItem(item: VaultItem) {
        viewModelScope.launch(Dispatchers.Default) {
            val encrypted1 = CryptoManager.encrypt(item.encryptedContent1)
            val encrypted2 = CryptoManager.encrypt(item.encryptedContent2)
            val finalItem = item.copy(
                encryptedContent1 = encrypted1,
                encryptedContent2 = encrypted2,
                isFake = _isFakeVaultActive.value
            )
            repository.insertItem(finalItem)
        }
    }

    fun updateVaultItem(item: VaultItem) {
        viewModelScope.launch(Dispatchers.Default) {
            val encrypted1 = CryptoManager.encrypt(item.encryptedContent1)
            val encrypted2 = CryptoManager.encrypt(item.encryptedContent2)
            val finalItem = item.copy(
                encryptedContent1 = encrypted1,
                encryptedContent2 = encrypted2
            )
            repository.updateItem(finalItem)
        }
    }

    fun toggleFavorite(item: VaultItem) {
        viewModelScope.launch {
            repository.updateItem(item.copy(isFavorite = !item.isFavorite))
        }
    }

    fun togglePin(item: VaultItem) {
        viewModelScope.launch {
            repository.updateItem(item.copy(isPinned = !item.isPinned))
        }
    }

    fun moveToTrash(item: VaultItem) {
        viewModelScope.launch {
            repository.updateItem(item.copy(isTrash = true))
        }
    }

    fun restoreFromTrash(item: VaultItem) {
        viewModelScope.launch {
            repository.updateItem(item.copy(isTrash = false))
        }
    }

    fun deleteItemPermanently(id: Int) {
        viewModelScope.launch {
            repository.deleteItemById(id)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash(_isFakeVaultActive.value)
        }
    }

    // Helper to decrypt an item dynamically for display (ephemeral decrypt)
    fun decryptItem(item: VaultItem): DecryptedVaultItem {
        val content1 = CryptoManager.decrypt(item.encryptedContent1)
        val content2 = CryptoManager.decrypt(item.encryptedContent2)
        return DecryptedVaultItem(
            id = item.id,
            type = item.type,
            title = item.title,
            subtitle = item.subtitle,
            content1 = content1,
            content2 = content2,
            category = item.category,
            tags = item.tags,
            isFavorite = item.isFavorite,
            isPinned = item.isPinned,
            isTrash = item.isTrash,
            isFake = item.isFake,
            timestamp = item.timestamp,
            cardColorHex = item.cardColorHex
        )
    }

    // Sample Local Data Insertion for onboarding richness
    private suspend fun insertSampleData() {
        // Production: Do not insert sample data
    }

    // Encrypted Backup and Restore
    fun backupVault(context: Context): String? {
        try {
            val list = _rawItems.value
            val array = JSONArray()
            for (item in list) {
                val obj = JSONObject().apply {
                    put("type", item.type)
                    put("title", item.title)
                    put("subtitle", item.subtitle)
                    put("encryptedContent1", item.encryptedContent1)
                    put("encryptedContent2", item.encryptedContent2)
                    put("category", item.category)
                    put("tags", item.tags)
                    put("isFavorite", item.isFavorite)
                    put("isPinned", item.isPinned)
                    put("cardColorHex", item.cardColorHex)
                }
                array.put(obj)
            }
            val backupData = JSONObject().apply {
                put("version", 1)
                put("timestamp", System.currentTimeMillis())
                put("items", array)
            }.toString()

            // Save to external files or return string
            val file = File(context.getExternalFilesDir(null), "TopSecret_Backup.json")
            file.writeText(backupData)
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun restoreVault(backupJson: String): Boolean {
        try {
            val obj = JSONObject(backupJson)
            val itemsArray = obj.getJSONArray("items")
            viewModelScope.launch(Dispatchers.Default) {
                for (i in 0 until itemsArray.length()) {
                    val itemObj = itemsArray.getJSONObject(i)
                    val item = VaultItem(
                        type = itemObj.getString("type"),
                        title = itemObj.getString("title"),
                        subtitle = itemObj.getString("subtitle"),
                        encryptedContent1 = itemObj.getString("encryptedContent1"),
                        encryptedContent2 = itemObj.getString("encryptedContent2"),
                        category = itemObj.optString("category", ""),
                        tags = itemObj.optString("tags", ""),
                        isFavorite = itemObj.optBoolean("isFavorite", false),
                        isPinned = itemObj.optBoolean("isPinned", false),
                        isFake = _isFakeVaultActive.value,
                        cardColorHex = itemObj.optString("cardColorHex", "#6750A4")
                    )
                    repository.insertItem(item)
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}

data class VaultStats(
    val total: Int = 0,
    val passwords: Int = 0,
    val notes: Int = 0,
    val cards: Int = 0,
    val totps: Int = 0,
    val docs: Int = 0,
    val gallery: Int = 0,
    val identity: Int = 0
)

data class DecryptedVaultItem(
    val id: Int,
    val type: String,
    val title: String,
    val subtitle: String,
    val content1: String,
    val content2: String,
    val category: String,
    val tags: String,
    val isFavorite: Boolean,
    val isPinned: Boolean,
    val isTrash: Boolean,
    val isFake: Boolean,
    val timestamp: Long,
    val cardColorHex: String
)
