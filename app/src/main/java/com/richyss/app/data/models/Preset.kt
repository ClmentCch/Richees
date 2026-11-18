package com.richyss.app.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** One entry of `presets/index.json` on the GitHub repository. */
@Serializable
data class PresetIndexEntry(
    val id: String,
    val name: String = "",
    val description: String = "",
    val category: String = "custom",
    val author: String = "Richyss",
    val version: String = "1.0.0",
    val path: String = ""
)

@Serializable
data class PresetIndex(
    val schema: Int = 1,
    val version: String = "",
    val presets: List<PresetIndexEntry> = emptyList()
)

enum class PresetCategory(val key: String) {
    GAMING("gaming"),
    MUSIC("music"),
    PROGRAMMING("programming"),
    SOCIAL("social"),
    CUSTOM("custom");

    companion object {
        fun fromKey(key: String?): PresetCategory =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: CUSTOM
    }
}

/** A preset together with where it came from, for the UI. */
data class PresetItem(
    val document: PresetDocument,
    val category: PresetCategory,
    val source: PresetSource
)

enum class PresetSource { REMOTE, CACHED, BUNDLED }

sealed interface PresetIndexResult {
    data class Success(val index: PresetIndex) : PresetIndexResult
    data class Failure(val message: String) : PresetIndexResult
}

object PresetIndexParser {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Parses an index and rejects entries that could escape the repository or that
     * lack the fields the library needs. Invalid entries are dropped, not fatal:
     * a single broken preset must not take the whole library down.
     */
    fun parse(body: String): PresetIndexResult = runCatching {
        val index = json.decodeFromString(PresetIndex.serializer(), body)
        val seen = mutableSetOf<String>()
        val entries = index.presets.filter { entry ->
            val id = entry.id.trim()
            val path = entry.path.trim()
            id.isNotEmpty() &&
                path.isNotEmpty() &&
                !path.startsWith("/") &&
                !path.contains("..") &&
                path.startsWith("presets/") &&
                path.endsWith(".json") &&
                seen.add(id)
        }
        PresetIndexResult.Success(index.copy(presets = entries))
    }.getOrElse { error ->
        PresetIndexResult.Failure(error.message ?: "invalid_index")
    }
}
