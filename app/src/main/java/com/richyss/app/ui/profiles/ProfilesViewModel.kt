package com.richyss.app.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.richyss.app.core.AppContainer
import com.richyss.app.core.LocalTranslations
import com.richyss.app.core.translationFor
import com.richyss.app.data.models.RpcProfile
import com.richyss.app.data.repository.ImportResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProfilesUiState(
    val loaded: Boolean = false,
    val profiles: List<RpcProfile> = emptyList(),
    val activeId: String? = null
)

class ProfilesViewModel(private val container: AppContainer) : ViewModel() {

    private val activeId = MutableStateFlow<String?>(null)

    val state: StateFlow<ProfilesUiState> = combine(
        container.profiles.observeProfiles(),
        container.profiles.observeActiveProfile(),
        activeId
    ) { profiles, active, _ ->
        ProfilesUiState(
            loaded = true,
            profiles = profiles,
            activeId = active?.id
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfilesUiState()
    )

    fun setActive(id: String, active: Boolean) {
        viewModelScope.launch {
            container.profiles.toggleActive(id, active)
            val t = translationFor(container)
            container.messages.show(if (active) t.profilesActivated else t.profilesDeactivated)
        }
    }

    fun duplicate(id: String) {
        viewModelScope.launch {
            val copy = container.profiles.duplicate(id)
            val t = translationFor(container)
            container.messages.show(if (copy != null) t.profilesDuplicated else t.profilesImportFailed)
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            container.profiles.delete(id)
            container.messages.show(translationFor(container).commonDelete)
        }
    }

    fun importJson(text: String) {
        viewModelScope.launch {
            val t = translationFor(container)
            when (val result = container.profiles.importJson(text)) {
                is ImportResult.Success -> container.messages.show(t.profilesImported)
                is ImportResult.Failure -> container.messages.show(
                    "${t.profilesImportFailed} (${result.reason})"
                )
            }
        }
    }

    fun exportJson(profile: RpcProfile, onReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = withContext(Dispatchers.IO) { container.profiles.exportJson(profile) }
            onReady(json)
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { ProfilesViewModel(container) }
        }
    }
}
