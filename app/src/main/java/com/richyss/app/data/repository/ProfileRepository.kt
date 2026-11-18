package com.richyss.app.data.repository

import androidx.room.withTransaction
import com.richyss.app.data.database.ProfileEntity
import com.richyss.app.data.database.RichyssDatabase
import com.richyss.app.data.models.ProfileJson
import com.richyss.app.data.models.RpcProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/** Outcome of importing a profile document. */
sealed interface ImportResult {
    data class Success(val profile: RpcProfile) : ImportResult
    data class Failure(val reason: String) : ImportResult
}

class ProfileRepository(private val database: RichyssDatabase) {

    private val dao = database.profileDao()

    fun observeProfiles(): Flow<List<RpcProfile>> =
        dao.observeAll().map { rows -> rows.mapNotNull(::toProfile) }

    fun observeActiveProfile(): Flow<RpcProfile?> =
        dao.observeActive().map { row -> row?.let(::toProfile) }

    fun observeCount(): Flow<Int> = dao.observeCount()

    suspend fun recent(limit: Int = 3): List<RpcProfile> =
        dao.recent(limit).mapNotNull(::toProfile)

    suspend fun find(id: String): RpcProfile? = dao.findById(id)?.let(::toProfile)

    suspend fun upsert(profile: RpcProfile): RpcProfile {
        val stored = profile.copy(updatedAt = System.currentTimeMillis())
        // Editing a profile must not silently deactivate it: the active flag only
        // lives in the table, so it is read back before writing.
        val isActive = dao.findById(stored.id)?.isActive ?: false
        dao.upsert(toEntity(stored, isActive))
        return stored
    }

    suspend fun create(profile: RpcProfile): RpcProfile {
        val withId = if (profile.id.isBlank()) profile.copy(id = newId()) else profile
        return upsert(withId)
    }

    suspend fun delete(id: String) = dao.delete(id)

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun duplicate(id: String): RpcProfile? {
        val original = find(id) ?: return null
        val copy = original.copy(
            id = newId(),
            name = "${original.name} (copie)".take(64),
            updatedAt = System.currentTimeMillis()
        )
        return upsert(copy)
    }

    /**
     * Activates [id] and deactivates every other profile. Passing null simply
     * deactivates everything, which is what the "disable" action does.
     */
    suspend fun setActive(id: String?) = database.withTransaction {
        dao.clearActive()
        if (id != null) dao.markActive(id)
    }

    suspend fun toggleActive(id: String, active: Boolean) =
        setActive(if (active) id else null)

    suspend fun importJson(text: String): ImportResult {
        val decoded = ProfileJson.decode(text)
            .getOrElse { return ImportResult.Failure(it.message ?: "invalid_json") }

        // Importing never steals an existing id: the document is treated as a new profile.
        val imported = decoded.copy(
            id = newId(),
            name = if (decoded.name.isBlank()) "Profil importé" else decoded.name,
            updatedAt = System.currentTimeMillis()
        )
        upsert(imported)
        return ImportResult.Success(imported)
    }

    fun exportJson(profile: RpcProfile): String = ProfileJson.encode(profile)

    private fun toProfile(entity: ProfileEntity): RpcProfile? =
        ProfileJson.decode(entity.payload).getOrNull()
            ?: runCatching {
                // A row written by a newer schema still yields the columns we can rely on.
                RpcProfile(id = entity.id, name = entity.name, icon = entity.icon)
            }.getOrNull()

    private fun toEntity(profile: RpcProfile, isActive: Boolean) = ProfileEntity(
        id = profile.id,
        name = profile.name,
        description = profile.description,
        icon = profile.icon,
        isActive = isActive,
        updatedAt = profile.updatedAt,
        payload = ProfileJson.encode(profile)
    )

    fun newId(): String = UUID.randomUUID().toString()
}
