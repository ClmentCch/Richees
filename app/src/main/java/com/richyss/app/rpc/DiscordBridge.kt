package com.richyss.app.rpc

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Discord installation details read through PackageManager. */
data class DiscordInstall(
    val installed: Boolean,
    val versionName: String = "",
    val versionCode: Long = 0,
    val lastUpdateTime: Long = 0
)

/** Response of the bridge `status` call. */
data class BridgeStatus(
    val bridgeVersion: String,
    val protocol: String,
    val discordVersion: String,
    val hasPresence: Boolean,
    val updatedAt: Long
)

/** How the bridge answered a request. */
sealed interface BridgeResult {
    data class Success(val raw: String) : BridgeResult
    data class Rejected(val reason: String) : BridgeResult
    data class Unavailable(val reason: String) : BridgeResult
}

/**
 * Client for the ContentProvider that the Richyss Bridge patch installs inside
 * Discord. Every call is a real Binder round trip; nothing here pretends a bridge
 * exists when the provider cannot be reached.
 */
class DiscordBridge(private val context: Context) {

    companion object {
        const val DISCORD_PACKAGE = "com.discord"
        const val BRIDGE_AUTHORITY = "com.discord.richyss.bridge"
        const val SUPPORTED_PROTOCOL = 1
        val STATUS_URI: Uri = Uri.parse("content://$BRIDGE_AUTHORITY/status")
        val PRESENCE_URI: Uri = Uri.parse("content://$BRIDGE_AUTHORITY/presence")
    }

    private val resolver: ContentResolver = context.contentResolver

    suspend fun discordInstall(): DiscordInstall = withContext(Dispatchers.IO) {
        try {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    DISCORD_PACKAGE,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(DISCORD_PACKAGE, 0)
            }
            DiscordInstall(
                installed = true,
                versionName = info.versionName ?: "",
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    info.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    info.versionCode.toLong()
                },
                lastUpdateTime = info.lastUpdateTime
            )
        } catch (_: PackageManager.NameNotFoundException) {
            DiscordInstall(installed = false)
        } catch (_: SecurityException) {
            // Package visibility blocked: the <queries> entry normally prevents this.
            DiscordInstall(installed = false)
        }
    }

    /** Reads the bridge status document, or null when the provider is unreachable. */
    suspend fun status(): BridgeStatus? = withContext(Dispatchers.IO) {
        val raw = queryJson(STATUS_URI) ?: return@withContext null
        BridgeJson.parseStatus(raw).getOrNull()
    }

    /** Raw status JSON, shown in the Patch Manager diagnostics. */
    suspend fun rawStatus(): String? = withContext(Dispatchers.IO) { queryJson(STATUS_URI) }

    /** Raw payload the bridge currently stores (may be "null"). */
    suspend fun rawPresence(): String? = withContext(Dispatchers.IO) { queryJson(PRESENCE_URI) }

    /**
     * Delivers a presence payload to the bridge and reads it back. A delivery is
     * only reported as successful when the bridge confirms it stored the payload.
     */
    suspend fun deliver(payloadJson: String): BridgeResult = withContext(Dispatchers.IO) {
        val bundle = call("setPresence", payloadJson) ?: return@withContext BridgeResult.Unavailable(
            "Provider did not answer"
        )
        if (!bundle.getBoolean("ok", false)) {
            val error = bundle.getString("error") ?: "Bridge rejected the payload"
            return@withContext BridgeResult.Rejected(error)
        }
        val stored = bundle.getString("json").orEmpty()
        if (stored.isBlank()) {
            return@withContext BridgeResult.Unavailable("Bridge returned an empty payload")
        }
        // Round trip verification against the provider, not against local state.
        val readBack = queryJson(PRESENCE_URI)
        if (readBack == null || readBack == "null" || readBack.isBlank()) {
            BridgeResult.Unavailable("Bridge did not persist the payload")
        } else {
            BridgeResult.Success(readBack)
        }
    }

    suspend fun clearPresence(): BridgeResult = withContext(Dispatchers.IO) {
        val bundle = call("clearPresence", null)
            ?: return@withContext BridgeResult.Unavailable("Provider did not answer")
        if (bundle.getBoolean("ok", false)) {
            BridgeResult.Success(bundle.getString("json").orEmpty())
        } else {
            BridgeResult.Rejected(bundle.getString("error") ?: "Bridge rejected the request")
        }
    }

    private fun call(method: String, argument: String?): Bundle? = try {
        resolver.call(STATUS_URI, method, argument, null)
    } catch (e: Exception) {
        null
    }

    private fun queryJson(uri: Uri): String? = try {
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst() && cursor.columnCount > 0) cursor.getString(0) else null
        }
    } catch (e: Exception) {
        null
    }
}
