package com.richyss.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class ThemeMode { DARK, LIGHT, SYSTEM }

enum class LanguageMode { SYSTEM, FRENCH, ENGLISH }

data class AppSettings(
    val language: LanguageMode = LanguageMode.SYSTEM,
    val theme: ThemeMode = ThemeMode.DARK,
    val presetOwner: String = DEFAULT_PRESET_OWNER,
    val presetRepo: String = DEFAULT_PRESET_REPO,
    val presetBranch: String = DEFAULT_PRESET_BRANCH,
    val favoritePresetIds: Set<String> = emptySet(),
    val lastPresetSyncAt: Long = 0L,
    val lastDeliveredAt: Long = 0L,
    val lastDeliveredProfileId: String? = null,
    val lastDeliveredProfileName: String? = null,
    val dismissedUpdateTag: String? = null,
    val seenFirstRun: Boolean = false
) {
    companion object {
        const val DEFAULT_PRESET_OWNER = "ClmentCch"
        const val DEFAULT_PRESET_REPO = "Richees"
        const val DEFAULT_PRESET_BRANCH = "apk"
    }
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "richyss_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val language = stringPreferencesKey("language")
        val theme = stringPreferencesKey("theme")
        val presetOwner = stringPreferencesKey("preset_owner")
        val presetRepo = stringPreferencesKey("preset_repo")
        val presetBranch = stringPreferencesKey("preset_branch")
        val favorites = stringSetPreferencesKey("favorite_presets")
        val lastPresetSyncAt = longPreferencesKey("last_preset_sync_at")
        val lastDeliveredAt = longPreferencesKey("last_delivered_at")
        val lastDeliveredProfileId = stringPreferencesKey("last_delivered_profile_id")
        val lastDeliveredProfileName = stringPreferencesKey("last_delivered_profile_name")
        val dismissedUpdateTag = stringPreferencesKey("dismissed_update_tag")
        val seenFirstRun = booleanPreferencesKey("seen_first_run")
    }

    /** Emits settings, falling back to defaults when the file is unreadable. */
    val settings: Flow<AppSettings> = context.dataStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw error
        }
        .map { preferences ->
            AppSettings(
                language = preferences[Keys.language].toLanguageMode(),
                theme = preferences[Keys.theme].toThemeMode(),
                presetOwner = preferences[Keys.presetOwner].orEmptyDefault(AppSettings.DEFAULT_PRESET_OWNER),
                presetRepo = preferences[Keys.presetRepo].orEmptyDefault(AppSettings.DEFAULT_PRESET_REPO),
                presetBranch = preferences[Keys.presetBranch]
                    .orEmptyDefault(AppSettings.DEFAULT_PRESET_BRANCH),
                favoritePresetIds = preferences[Keys.favorites] ?: emptySet(),
                lastPresetSyncAt = preferences[Keys.lastPresetSyncAt] ?: 0L,
                lastDeliveredAt = preferences[Keys.lastDeliveredAt] ?: 0L,
                lastDeliveredProfileId = preferences[Keys.lastDeliveredProfileId],
                lastDeliveredProfileName = preferences[Keys.lastDeliveredProfileName],
                dismissedUpdateTag = preferences[Keys.dismissedUpdateTag],
                seenFirstRun = preferences[Keys.seenFirstRun] ?: false
            )
        }
        .distinctUntilChanged()

    suspend fun setLanguage(mode: LanguageMode) = edit { it[Keys.language] = mode.name }

    suspend fun setTheme(mode: ThemeMode) = edit { it[Keys.theme] = mode.name }

    suspend fun setPresetRepository(owner: String, repo: String, branch: String) = edit {
        it[Keys.presetOwner] = owner.trim()
        it[Keys.presetRepo] = repo.trim()
        it[Keys.presetBranch] = branch.trim().ifBlank { AppSettings.DEFAULT_PRESET_BRANCH }
    }

    suspend fun toggleFavorite(presetId: String) = edit { preferences ->
        val current = preferences[Keys.favorites] ?: emptySet()
        preferences[Keys.favorites] =
            if (current.contains(presetId)) current - presetId else current + presetId
    }

    suspend fun markPresetSync(timestamp: Long) = edit { it[Keys.lastPresetSyncAt] = timestamp }

    suspend fun markDelivered(profileId: String, profileName: String, timestamp: Long) = edit {
        it[Keys.lastDeliveredAt] = timestamp
        it[Keys.lastDeliveredProfileId] = profileId
        it[Keys.lastDeliveredProfileName] = profileName
    }

    suspend fun dismissUpdate(tag: String) = edit { it[Keys.dismissedUpdateTag] = tag }

    suspend fun markFirstRunSeen() = edit { it[Keys.seenFirstRun] = true }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit { preferences -> block(preferences) }
    }

    private fun String?.toLanguageMode(): LanguageMode =
        LanguageMode.entries.firstOrNull { it.name.equals(this, ignoreCase = true) } ?: LanguageMode.SYSTEM

    private fun String?.toThemeMode(): ThemeMode =
        ThemeMode.entries.firstOrNull { it.name.equals(this, ignoreCase = true) } ?: ThemeMode.DARK

    private fun String?.orEmptyDefault(default: String): String =
        if (this.isNullOrBlank()) default else this
}
