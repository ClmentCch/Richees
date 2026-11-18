package com.richyss.app.ui.patches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.richyss.app.core.AppContainer
import com.richyss.app.core.translationFor
import com.richyss.app.data.models.RpcProfile
import com.richyss.app.rpc.DeliveryOutcome
import com.richyss.app.rpc.IntegrationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PatchesUiState(
    val integration: IntegrationState? = null,
    val activeProfile: RpcProfile? = null,
    val diagnostics: String? = null,
    val lastPayload: String? = null,
    val busy: Boolean = false
)

class PatchesViewModel(private val container: AppContainer) : ViewModel() {

    private val integration = MutableStateFlow<IntegrationState?>(null)
    private val diagnostics = MutableStateFlow<String?>(null)
    private val lastPayload = MutableStateFlow<String?>(null)
    private val busy = MutableStateFlow(false)

    val state: StateFlow<PatchesUiState> = combine(
        integration,
        container.profiles.observeActiveProfile(),
        diagnostics,
        lastPayload,
        busy
    ) { integration, active, diagnostics, payload, busy ->
        PatchesUiState(
            integration = integration,
            activeProfile = active,
            diagnostics = diagnostics,
            lastPayload = payload,
            busy = busy
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PatchesUiState()
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            busy.value = true
            integration.value = container.discord.inspect()
            diagnostics.value = container.discord.rawDiagnostics()
            lastPayload.value = container.discord.lastPresenceJson()
            busy.value = false
        }
    }

    fun deliverActiveProfile() {
        viewModelScope.launch {
            val t = translationFor(container)
            val profile = state.value.activeProfile
            if (profile == null) {
                container.messages.show(t.patchesNoActiveProfile)
                return@launch
            }
            busy.value = true
            when (val outcome = container.discord.deliver(profile)) {
                is DeliveryOutcome.Delivered -> {
                    container.settings.markDelivered(
                        profileId = profile.id,
                        profileName = profile.name,
                        timestamp = outcome.deliveredAt
                    )
                    container.messages.show(t.patchesDelivered)
                    lastPayload.value = outcome.payloadJson
                    integration.value = container.discord.inspect()
                }
                is DeliveryOutcome.Rejected -> container.messages.show(
                    "${t.patchesDeliverFailed}: ${outcome.reason}"
                )
                is DeliveryOutcome.Unavailable -> container.messages.show(
                    "${t.patchesDeliverFailed}: ${outcome.reason}"
                )
                is DeliveryOutcome.InvalidProfile -> container.messages.show(
                    "${t.patchesDeliverFailed}: ${outcome.issues.firstOrNull().orEmpty()}"
                )
            }
            busy.value = false
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { PatchesViewModel(container) }
        }
    }
}
