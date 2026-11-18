package com.richyss.app.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** A single Rich Presence button, mirroring the bridge protocol. */
@Serializable
data class ProfileButton(
    val label: String = "",
    val url: String = ""
)

/** Discord activity types that the bridge protocol can carry. */
enum class ActivityType(val id: Int, val discordName: String) {
    PLAYING(0, "playing"),
    STREAMING(1, "streaming"),
    LISTENING(2, "listening"),
    WATCHING(3, "watching"),
    COMPETING(5, "competing");

    companion object {
        fun fromId(id: Int): ActivityType = entries.firstOrNull { it.id == id } ?: PLAYING
    }
}

/** Limits enforced by the bridge extension; kept in sync in [ProfileValidator]. */
object ProfileLimits {
    const val NAME = 64
    const val ICON = 8
    const val DESCRIPTION = 256
    const val ACTIVITY_NAME = 128
    const val DETAILS = 128
    const val STATE = 128
    const val LARGE_IMAGE = 256
    const val SMALL_IMAGE = 256
    const val BUTTON_LABEL = 32
    const val BUTTON_URL = 512
    const val MAX_BUTTONS = 2
    const val PAYLOAD_BYTES = 4096
}

/**
 * A locally stored Rich Presence profile.
 *
 * The model is the single source of truth for the editor, the preview and the
 * payload sent to the bridge. Every field the bridge cannot carry is deliberately
 * absent instead of being stored and silently ignored.
 */
@Serializable
data class RpcProfile(
    @SerialName("schema") val schema: Int = CURRENT_SCHEMA,
    val id: String,
    val name: String,
    val icon: String = "🎮",
    val description: String = "",
    val activityType: Int = ActivityType.PLAYING.id,
    val activityName: String = "",
    val details: String = "",
    val state: String = "",
    val largeImage: String = "",
    val smallImage: String = "",
    val largeText: String = "",
    val smallText: String = "",
    val timerEnabled: Boolean = false,
    val startTimestamp: Long? = null,
    val endTimestamp: Long? = null,
    val buttons: List<ProfileButton> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val activity: ActivityType get() = ActivityType.fromId(activityType)

    companion object {
        const val CURRENT_SCHEMA = 1
        const val DEFAULT_ICON = "🎮"
    }
}

/** Result of validating a profile before persisting or publishing it. */
data class FieldIssue(val field: String, val messageKey: String)

/** Compact preset metadata used by the GitHub preset library. */
@Serializable
data class PresetDocument(
    val schema: Int = 1,
    val id: String,
    val name: String,
    val description: String = "",
    val category: String = "custom",
    val author: String = "Richyss",
    val version: String = "1.0.0",
    val profile: RpcProfile
)

/** Validation shared by the editor, the repository and the publisher. */
object ProfileValidator {

    fun validate(profile: RpcProfile): List<FieldIssue> {
        val issues = mutableListOf<FieldIssue>()
        fun check(value: String, limit: Int, field: String, required: Boolean = false) {
            if (required && value.isBlank()) {
                issues += FieldIssue(field, "required")
            } else if (value.length > limit) {
                issues += FieldIssue(field, "too_long")
            }
        }

        check(profile.name, ProfileLimits.NAME, "name", required = true)
        check(profile.icon, ProfileLimits.ICON, "icon")
        check(profile.description, ProfileLimits.DESCRIPTION, "description")
        check(profile.activityName, ProfileLimits.ACTIVITY_NAME, "activityName", required = true)
        check(profile.details, ProfileLimits.DETAILS, "details")
        check(profile.state, ProfileLimits.STATE, "state")
        check(profile.largeImage, ProfileLimits.LARGE_IMAGE, "largeImage")
        check(profile.smallImage, ProfileLimits.SMALL_IMAGE, "smallImage")
        check(profile.largeText, ProfileLimits.DETAILS, "largeText")
        check(profile.smallText, ProfileLimits.DETAILS, "smallText")

        if (profile.buttons.size > ProfileLimits.MAX_BUTTONS) {
            issues += FieldIssue("buttons", "too_many")
        }
        profile.buttons.forEachIndexed { index, button ->
            if (button.label.isBlank()) issues += FieldIssue("button$index.label", "required")
            if (button.label.length > ProfileLimits.BUTTON_LABEL) {
                issues += FieldIssue("button$index.label", "too_long")
            }
            if (button.url.length > ProfileLimits.BUTTON_URL) {
                issues += FieldIssue("button$index.url", "too_long")
            }
            if (button.url.isNotBlank() && !button.url.isHttpUrl()) {
                issues += FieldIssue("button$index.url", "invalid_url")
            }
        }

        val start = profile.startTimestamp
        val end = profile.endTimestamp
        if (start != null && start <= 0) issues += FieldIssue("startTimestamp", "invalid")
        if (end != null && end <= 0) issues += FieldIssue("endTimestamp", "invalid")
        if (start != null && end != null && end < start) {
            issues += FieldIssue("endTimestamp", "before_start")
        }
        if (profile.timerEnabled && start == null && end == null) {
            issues += FieldIssue("timerEnabled", "no_times")
        }

        // The bridge refuses payloads above 4096 bytes, so refuse them here too
        // instead of letting a delivery fail after the user pressed send.
        runCatching {
            ProfileJson.encodePayload(profile, System.currentTimeMillis())
                .toByteArray(Charsets.UTF_8).size
        }.getOrNull()?.let { size ->
            if (size > ProfileLimits.PAYLOAD_BYTES) {
                issues += FieldIssue("payload", "too_large")
            }
        }
        return issues
    }

    fun isValid(profile: RpcProfile): Boolean = validate(profile).isEmpty()

    /**
     * Builds the presence payload handed to the Discord bridge.
     * Only fields the protocol carries are included.
     */
    fun toPayloadJson(profile: RpcProfile, now: Long = System.currentTimeMillis()): String =
        ProfileJson.encodePayload(profile, now)
}

internal fun String.isHttpUrl(): Boolean =
    (startsWith("https://") || startsWith("http://")) && length > 8 && !contains(' ')

/** JSON codec used for storage, export, import and the bridge payload. */
object ProfileJson {
    val format: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
        explicitNulls = false
    }

    fun encode(profile: RpcProfile): String = format.encodeToString(RpcProfile.serializer(), profile)

    fun decode(text: String): Result<RpcProfile> = runCatching {
        val profile = format.decodeFromString(RpcProfile.serializer(), text)
        if (profile.id.isBlank()) error("Profile id is missing")
        if (profile.schema > RpcProfile.CURRENT_SCHEMA) {
            error("Profile schema ${profile.schema} is newer than this app")
        }
        profile
    }

    fun encodePreset(document: PresetDocument): String =
        format.encodeToString(PresetDocument.serializer(), document)

    fun decodePreset(text: String): Result<PresetDocument> = runCatching {
        format.decodeFromString(PresetDocument.serializer(), text)
    }

    /** Payload for the bridge: no local-only fields, timestamps in milliseconds. */
    fun encodePayload(profile: RpcProfile, now: Long): String {
        val payload = BridgePayload(
            activityName = profile.activityName.ifBlank { profile.name },
            details = profile.details,
            state = profile.state,
            largeImage = profile.largeImage,
            smallImage = profile.smallImage,
            largeText = profile.largeText,
            smallText = profile.smallText,
            startTimestamp = if (profile.timerEnabled) (profile.startTimestamp ?: now) else null,
            endTimestamp = if (profile.timerEnabled) profile.endTimestamp else null,
            buttons = profile.buttons
                .filter { it.label.isNotBlank() }
                .take(ProfileLimits.MAX_BUTTONS)
                .map { ProfileButton(it.label.trim(), it.url.trim()) }
        )
        return format.encodeToString(BridgePayload.serializer(), payload)
    }
}

/** Wire format accepted by the bridge extension (`setPresence`). */
@Serializable
data class BridgePayload(
    val activityName: String,
    val details: String = "",
    val state: String = "",
    val largeImage: String = "",
    val smallImage: String = "",
    val largeText: String = "",
    val smallText: String = "",
    val startTimestamp: Long? = null,
    val endTimestamp: Long? = null,
    val buttons: List<ProfileButton> = emptyList()
)
