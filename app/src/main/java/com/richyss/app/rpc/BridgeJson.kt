package com.richyss.app.rpc

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/** Parser for the JSON documents the Discord bridge returns. */
object BridgeJson {
    private val json = Json { ignoreUnknownKeys = true }

    fun parseStatus(raw: String): Result<BridgeStatus> = runCatching {
        val root = json.parseToJsonElement(raw).jsonObject
        BridgeStatus(
            bridgeVersion = root["version"]?.jsonPrimitive?.content.orEmpty(),
            protocol = root["protocol"]?.jsonPrimitive?.content.orEmpty(),
            discordVersion = root["discordVersion"]?.jsonPrimitive?.content.orEmpty(),
            hasPresence = root["hasPresence"]?.jsonPrimitive?.booleanOrNull ?: false,
            updatedAt = root["updatedAt"]?.jsonPrimitive?.longOrNull ?: 0L
        )
    }

    fun protocolIsSupported(protocol: String): Boolean =
        protocol.trim().toIntOrNull() == DiscordBridge.SUPPORTED_PROTOCOL
}
