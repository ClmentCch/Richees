package com.richyss.app.rpc

import com.richyss.app.data.models.ProfileJson
import com.richyss.app.data.models.ProfileValidator
import com.richyss.app.data.models.RpcProfile

/**
 * Technical state of the Discord integration.
 *
 * There is deliberately no "connected" or "published" state that a local payload
 * hand-off could produce: Discord only renders what its own presence pipeline
 * publishes, and that display patch is not part of 1.0.0.
 */
sealed interface IntegrationState {
    data object DiscordMissing : IntegrationState
    data class PatchMissing(val discordVersion: String) : IntegrationState
    data class BridgeError(val discordVersion: String, val reason: String) : IntegrationState
    data class BridgeActive(
        val discordVersion: String,
        val bridgeVersion: String,
        val protocol: String,
        val hasPresence: Boolean,
        val updatedAt: Long
    ) : IntegrationState
}

/** Outcome of a delivery attempt, with the truthful display status. */
sealed interface DeliveryOutcome {
    data class Delivered(val payloadJson: String, val deliveredAt: Long) : DeliveryOutcome
    data class Rejected(val reason: String) : DeliveryOutcome
    data class Unavailable(val reason: String) : DeliveryOutcome
    data class InvalidProfile(val issues: List<String>) : DeliveryOutcome
}

class DiscordIntegrationService(private val bridge: DiscordBridge) {

    suspend fun inspect(): IntegrationState {
        val install = bridge.discordInstall()
        if (!install.installed) return IntegrationState.DiscordMissing

        val status = bridge.status()
            ?: return IntegrationState.PatchMissing(install.versionName)

        if (status.bridgeVersion.isBlank()) {
            return IntegrationState.BridgeError(install.versionName, "Bridge reported no version")
        }
        return IntegrationState.BridgeActive(
            discordVersion = install.versionName,
            bridgeVersion = status.bridgeVersion,
            protocol = status.protocol,
            hasPresence = status.hasPresence,
            updatedAt = status.updatedAt
        )
    }

    suspend fun deliver(profile: RpcProfile): DeliveryOutcome {
        if (!bridge.discordInstall().installed) {
            return DeliveryOutcome.Unavailable("Discord is not installed")
        }
        val issues = ProfileValidator.validate(profile)
        if (issues.isNotEmpty()) {
            return DeliveryOutcome.InvalidProfile(issues.map { "${it.field}: ${it.messageKey}" })
        }
        val status = bridge.status()
            ?: return DeliveryOutcome.Unavailable("Richyss bridge not installed")
        if (!BridgeJson.protocolIsSupported(status.protocol)) {
            return DeliveryOutcome.Unavailable(
                "Bridge protocol ${status.protocol} is not supported by this app"
            )
        }
        val payload = ProfileJson.encodePayload(profile, System.currentTimeMillis())
        return when (val result = bridge.deliver(payload)) {
            is BridgeResult.Success -> DeliveryOutcome.Delivered(
                payloadJson = result.raw,
                deliveredAt = System.currentTimeMillis()
            )
            is BridgeResult.Rejected -> DeliveryOutcome.Rejected(result.reason)
            is BridgeResult.Unavailable -> DeliveryOutcome.Unavailable(result.reason)
        }
    }

    suspend fun lastPresenceJson(): String? = bridge.rawPresence()

    suspend fun rawDiagnostics(): String? = bridge.rawStatus()
}
