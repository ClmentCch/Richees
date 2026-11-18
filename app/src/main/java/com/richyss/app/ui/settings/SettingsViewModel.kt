package com.richyss.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.richyss.app.core.AppContainer
import com.richyss.app.core.translationFor
import com.richyss.app.data.prefs.LanguageMode
import com.richyss.app.data.prefs.ThemeMode
import com.richyss.app.network.UpdateResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val profileCount: Int = 0,
    val checking: Boolean = false,
    val updateAvailable: String? = null,
    val updateUrl: String? = null,
    val upToDate: Boolean = false
)

private data class UpdateState(
    val checking: Boolean = false,
    val available: String? = null,
    val url: String? = null,
    val upToDate: Boolean = false
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    private val update = MutableStateFlow(UpdateState())

    val state: StateFlow<SettingsUiState> = combine(
        container.profiles.observeCount(),
        update
    ) { count, update ->
        SettingsUiState(
            profileCount = count,
            checking = update.checking,
            updateAvailable = update.available,
            updateUrl = update.url,
            upToDate = update.upToDate
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setLanguage(mode: LanguageMode) {
        viewModelScope.launch { container.settings.setLanguage(mode) }
    }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { container.settings.setTheme(mode) }
    }

    fun saveRepository(owner: String, repo: String, branch: String) {
        viewModelScope.launch {
            container.settings.setPresetRepository(owner, repo, branch)
            container.messages.show(translationFor(container).settingsRepositorySaved)
        }
    }

    fun clearProfiles() {
        viewModelScope.launch {
            container.profiles.deleteAll()
            container.messages.show(translationFor(container).settingsCleared)
        }
    }

    fun checkUpdates() {
        viewModelScope.launch {
            update.value = UpdateState(checking = true)
            val t = translationFor(container)
            when (val result = container.updater.check()) {
                is UpdateResult.Available -> {
                    update.value = UpdateState(available = result.tag, url = result.url)
                    container.messages.show("${t.settingsUpdateAvailable}: ${result.tag}")
                }
                is UpdateResult.UpToDate -> {
                    update.value = UpdateState(upToDate = true)
                    container.messages.show(t.settingsUpToDate)
                }
                is UpdateResult.Failure -> {
                    update.value = UpdateState()
                    container.messages.show("${t.settingsUpdateFailed}: ${result.message}")
                }
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { SettingsViewModel(container) }
        }
    }
}
