package com.example.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "top_secret_prefs")

class VaultPrefs(private val context: Context) {

    companion object {
        val MASTER_PASSWORD_HASH = stringPreferencesKey("master_password_hash")
        val MASTER_PASSWORD_SALT = stringPreferencesKey("master_password_salt")
        val RECOVERY_HINT = stringPreferencesKey("recovery_hint")
        val BIOMETRICS_ENABLED = booleanPreferencesKey("biometrics_enabled")
        val AUTO_LOCK_MINUTES = intPreferencesKey("auto_lock_minutes")
        val FAKE_PIN = stringPreferencesKey("fake_pin")
        val PANIC_PIN = stringPreferencesKey("panic_pin")
        val IS_INITIALIZED = booleanPreferencesKey("is_initialized")
        val THEME_SELECTION = stringPreferencesKey("theme_selection")
        val CARD_STYLE = stringPreferencesKey("card_style")
        val CLIPBOARD_TIMEOUT_SEC = intPreferencesKey("clipboard_timeout_sec")
        val SCREENSHOTS_BLOCKED = booleanPreferencesKey("screenshots_blocked")
        val RECENT_APP_BLUR = booleanPreferencesKey("recent_app_blur")
        val FAKE_MODE_ACTIVE = booleanPreferencesKey("fake_mode_active")
    }

    val isInitialized: Flow<Boolean> = context.dataStore.data.map { it[IS_INITIALIZED] ?: false }
    val masterPasswordHash: Flow<String?> = context.dataStore.data.map { it[MASTER_PASSWORD_HASH] }
    val masterPasswordSalt: Flow<String?> = context.dataStore.data.map { it[MASTER_PASSWORD_SALT] }
    val recoveryHint: Flow<String?> = context.dataStore.data.map { it[RECOVERY_HINT] }
    val biometricsEnabled: Flow<Boolean> = context.dataStore.data.map { it[BIOMETRICS_ENABLED] ?: false }
    val autoLockMinutes: Flow<Int> = context.dataStore.data.map { it[AUTO_LOCK_MINUTES] ?: 5 }
    val fakePin: Flow<String?> = context.dataStore.data.map { it[FAKE_PIN] }
    val panicPin: Flow<String?> = context.dataStore.data.map { it[PANIC_PIN] }
    val themeSelection: Flow<String> = context.dataStore.data.map { it[THEME_SELECTION] ?: "system" }
    val cardStyle: Flow<String> = context.dataStore.data.map { it[CARD_STYLE] ?: "realistic" }
    val clipboardTimeoutSec: Flow<Int> = context.dataStore.data.map { it[CLIPBOARD_TIMEOUT_SEC] ?: 30 }
    val screenshotsBlocked: Flow<Boolean> = context.dataStore.data.map { it[SCREENSHOTS_BLOCKED] ?: true }
    val recentAppBlur: Flow<Boolean> = context.dataStore.data.map { it[RECENT_APP_BLUR] ?: true }
    val fakeModeActive: Flow<Boolean> = context.dataStore.data.map { it[FAKE_MODE_ACTIVE] ?: false }

    suspend fun saveOnboarding(hash: String, salt: String, hint: String) {
        context.dataStore.edit { prefs ->
            prefs[MASTER_PASSWORD_HASH] = hash
            prefs[MASTER_PASSWORD_SALT] = salt
            prefs[RECOVERY_HINT] = hint
            prefs[IS_INITIALIZED] = true
        }
    }

    suspend fun setBiometricsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[BIOMETRICS_ENABLED] = enabled }
    }

    suspend fun setAutoLockMinutes(minutes: Int) {
        context.dataStore.edit { prefs -> prefs[AUTO_LOCK_MINUTES] = minutes }
    }

    suspend fun setFakePin(pin: String?) {
        context.dataStore.edit { prefs -> 
            if (pin != null) prefs[FAKE_PIN] = pin else prefs.remove(FAKE_PIN)
        }
    }

    suspend fun setPanicPin(pin: String?) {
        context.dataStore.edit { prefs ->
            if (pin != null) prefs[PANIC_PIN] = pin else prefs.remove(PANIC_PIN)
        }
    }

    suspend fun setThemeSelection(theme: String) {
        context.dataStore.edit { prefs -> prefs[THEME_SELECTION] = theme }
    }

    suspend fun setCardStyle(style: String) {
        context.dataStore.edit { prefs -> prefs[CARD_STYLE] = style }
    }

    suspend fun setClipboardTimeout(seconds: Int) {
        context.dataStore.edit { prefs -> prefs[CLIPBOARD_TIMEOUT_SEC] = seconds }
    }

    suspend fun setScreenshotsBlocked(blocked: Boolean) {
        context.dataStore.edit { prefs -> prefs[SCREENSHOTS_BLOCKED] = blocked }
    }

    suspend fun setRecentAppBlur(blur: Boolean) {
        context.dataStore.edit { prefs -> prefs[RECENT_APP_BLUR] = blur }
    }

    suspend fun setFakeModeActive(active: Boolean) {
        context.dataStore.edit { prefs -> prefs[FAKE_MODE_ACTIVE] = active }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
