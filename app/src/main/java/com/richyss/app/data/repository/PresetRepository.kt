package com.richyss.app.data.repository

import android.content.Context
import com.richyss.app.data.models.PresetCategory
import com.richyss.app.data.models.PresetDocument
import com.richyss.app.data.models.PresetIndexParser
import com.richyss.app.data.models.PresetIndexResult
import com.richyss.app.data.models.PresetItem
import com.richyss.app.data.models.PresetSource
import com.richyss.app.data.models.ProfileJson
import com.richyss.app.data.prefs.SettingsRepository
import com.richyss.app.network.GitHubClient
import com.richyss.app.network.HttpResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

sealed interface PresetSyncResult {
    data class Success(val count: Int, val source: PresetSource) : PresetSyncResult
    data class Failure(val message: String, val cachedCount: Int) : PresetSyncResult
}

@Serializable
private data class PresetCache(
    val fetchedAt: Long = 0,
    val source: String = "bundled",
    val documents: List<PresetDocument> = emptyList()
)

/**
 * Loads the preset library from the GitHub repository configured in settings,
 * falling back to the last successful download and then to the library bundled in
 * the APK. Presets are never trusted blindly: each document is parsed with
 * [ProfileJson] and cross-checked against its index entry.
 */
class PresetRepository(
    private val context: Context,
    private val gitHub: GitHubClient,
    private val settings: SettingsRepository
) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    private fun cacheFile(): File = File(context.filesDir, "presets_cache.json")

    suspend fun loadCached(): List<PresetItem> = withContext(Dispatchers.IO) {
        val cache = readCache()
        if (cache != null && cache.documents.isNotEmpty()) {
            return@withContext cache.documents.map {
                PresetItem(it, PresetCategory.fromKey(it.category), presetSource(cache.source))
            }
        }
        bundled()
    }

    suspend fun refresh(): PresetSyncResult = withContext(Dispatchers.IO) {
        val current = settings.settings.first()
        val owner = current.presetOwner
        val repo = current.presetRepo
        val branch = current.presetBranch
        val cachedCount = loadCached().size

        val indexBody = when (val result = gitHub.rawFile(owner, repo, branch, "presets/index.json")) {
            is HttpResult.Failure -> {
                return@withContext PresetSyncResult.Failure(
                    "index: ${result.message}",
                    cachedCount
                )
            }
            is HttpResult.Success -> result.body
        }

        val index = when (val parsed = PresetIndexParser.parse(indexBody)) {
            is PresetIndexResult.Failure -> {
                return@withContext PresetSyncResult.Failure(
                    "index: ${parsed.message}",
                    cachedCount
                )
            }
            is PresetIndexResult.Success -> parsed.index
        }

        val documents = mutableListOf<PresetDocument>()
        var failures = 0
        for (entry in index.presets) {
            when (val result = gitHub.rawFile(owner, repo, branch, entry.path)) {
                is HttpResult.Failure -> failures++
                is HttpResult.Success -> {
                    val document = ProfileJson.decodePreset(result.body).getOrNull()
                    if (document != null && document.id == entry.id) {
                        documents += document
                    } else {
                        failures++
                    }
                }
            }
        }

        if (documents.isEmpty()) {
            return@withContext PresetSyncResult.Failure(
                if (failures > 0) "all presets failed to load" else "empty index",
                cachedCount
            )
        }

        writeCache(PresetCache(System.currentTimeMillis(), "remote", documents))
        settings.markPresetSync(System.currentTimeMillis())
        PresetSyncResult.Success(documents.size, PresetSource.REMOTE)
    }

    suspend fun bundled(): List<PresetItem> = withContext(Dispatchers.IO) {
        val body = runCatching {
            context.assets.open("presets/library.json").bufferedReader().use { it.readText() }
        }.getOrNull() ?: return@withContext emptyList()

        val documents = runCatching {
            json.decodeFromString(PresetCache.serializer(), body).documents
        }.getOrElse { emptyList() }

        documents.map { PresetItem(it, PresetCategory.fromKey(it.category), PresetSource.BUNDLED) }
    }

    private fun readCache(): PresetCache? = runCatching {
        val file = cacheFile()
        if (!file.exists()) return null
        json.decodeFromString(PresetCache.serializer(), file.readText())
    }.getOrNull()

    private fun writeCache(cache: PresetCache) {
        runCatching { cacheFile().writeText(json.encodeToString(PresetCache.serializer(), cache)) }
    }

    private fun presetSource(raw: String): PresetSource =
        if (raw == "remote") PresetSource.CACHED else PresetSource.BUNDLED
}
