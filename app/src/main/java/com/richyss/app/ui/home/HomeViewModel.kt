package com.richyss.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.richyss.app.core.AppContainer
import com.richyss.app.data.models.RpcProfile
import com.richyss.app.rpc.IntegrationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val loaded: Boolean = false,
    val profileCount: Int = 0,
    val activeProfile: RpcProfile? = null,
    val recentProfiles: List<RpcProfile> = emptyList(),
    val integration: IntegrationState? = null,
    val probing: Boolean = false,
    val lastDeliveredAt: Long = 0L,
    val lastDeliveredName: String? = null
)

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    private val integration = MutableStateFlow<IntegrationState?>(null)
    private val probing = MutableStateFlow(false)

    val state: StateFlow<HomeUiState> = combine(
        container.profiles.observeProfiles(),
        container.profiles.observeActiveProfile(),
        container.settings.settings,
        integration,
        probing
    ) { profiles, active, settings, integrationState, isProbing ->
        HomeUiState(
            loaded = true,
            profileCount = profiles.size,
            activeProfile = active,
            recentProfiles = profiles.take(3),
            integration = integrationState,
            probing = isProbing,
            lastDeliveredAt = settings.lastDeliveredAt,
            lastDeliveredName = settings.lastDeliveredProfileName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            probing.value = true
            integration.value = container.discord.inspect()
            probing.value = false
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(container) }
        }
    }
}
