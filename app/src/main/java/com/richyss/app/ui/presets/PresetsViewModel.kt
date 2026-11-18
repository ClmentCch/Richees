package com.richyss.app.ui.presets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.richyss.app.core.AppContainer
import com.richyss.app.core.translationFor
import com.richyss.app.data.models.PresetCategory
import com.richyss.app.data.models.PresetDocument
import com.richyss.app.data.models.PresetItem
import com.richyss.app.data.models.PresetSource
import com.richyss.app.data.repository.PresetSyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PresetsUiState(
    val loaded: Boolean = false,
    val all: List<PresetItem> = emptyList(),
    val visible: List<PresetItem> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val category: PresetCategory? = null,
    val favoritesOnly: Boolean = false,
    val refreshing: Boolean = false,
    val source: PresetSource = PresetSource.BUNDLED,
    val lastSyncAt: Long = 0L,
    val error: String? = null
)

private data class PresetsFilter(
    val category: PresetCategory? = null,
    val favoritesOnly: Boolean = false
)

class PresetsViewModel(private val container: AppContainer) : ViewModel() {

    private val items = MutableStateFlow<List<PresetItem>>(emptyList())
    private val source = MutableStateFlow(PresetSource.BUNDLED)
    private val refreshing = MutableStateFlow(false)
    private val filter = MutableStateFlow(PresetsFilter())
    private val error = MutableStateFlow<String?>(null)
    private val loaded = MutableStateFlow(false)

    val state: StateFlow<PresetsUiState> = combine(
        combine(items, source, refreshing) { list, source, refreshing ->
            Triple(list, source, refreshing)
        },
        container.settings.settings,
        filter,
        combine(error, loaded) { error, loaded -> error to loaded }
    ) { (list, source, refreshing), settings, filter, (error, loaded) ->
        val favorites = settings.favoritePresetIds
        val visible = list.filter { item ->
            (filter.category == null || item.category == filter.category) &&
                (!filter.favoritesOnly || favorites.contains(item.document.id))
        }
        PresetsUiState(
            loaded = loaded,
            all = list,
            visible = visible,
            favorites = favorites,
            category = filter.category,
            favoritesOnly = filter.favoritesOnly,
            refreshing = refreshing,
            source = source,
            lastSyncAt = settings.lastPresetSyncAt,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PresetsUiState()
    )

    init {
        viewModelScope.launch {
            val cached = container.presets.loadCached()
            items.value = cached
            source.value = cached.firstOrNull()?.source ?: PresetSource.BUNDLED
            loaded.value = true
        }
    }

    fun refresh() {
        if (refreshing.value) return
        viewModelScope.launch {
            refreshing.value = true
            error.value = null
            when (val result = container.presets.refresh()) {
                is PresetSyncResult.Success -> {
                    items.value = container.presets.loadCached()
                    source.value = result.source
                    container.messages.show(translationFor(container).presetsRefreshDone)
                }
                is PresetSyncResult.Failure -> {
                    error.value = result.message
                    container.messages.show(
                        "${translationFor(container).presetsRefreshFailed}: ${result.message}"
                    )
                }
            }
            refreshing.value = false
        }
    }

    fun selectCategory(category: PresetCategory?) {
        filter.value = filter.value.copy(category = category)
    }

    fun toggleFavoritesOnly() {
        filter.value = filter.value.copy(favoritesOnly = !filter.value.favoritesOnly)
    }

    fun toggleFavorite(presetId: String) {
        viewModelScope.launch { container.settings.toggleFavorite(presetId) }
    }

    /** Imports the preset as a new local profile and returns its id. */
    fun importPreset(document: PresetDocument, onImported: (String) -> Unit) {
        viewModelScope.launch {
            val profile = document.profile.copy(
                id = container.profiles.newId(),
                name = document.profile.name.ifBlank { document.name },
                updatedAt = System.currentTimeMillis()
            )
            val stored = container.profiles.upsert(profile)
            container.messages.show(translationFor(container).profilesImported)
            onImported(stored.id)
        }
    }

    /** Imports the preset without opening the editor. */
    fun importOnly(document: PresetDocument) {
        importPreset(document) { }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { PresetsViewModel(container) }
        }
    }
}
