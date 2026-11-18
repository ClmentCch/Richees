package com.richyss.app.network

import com.richyss.app.BuildConfig
import com.richyss.app.data.prefs.AppSettings

/** Result of asking GitHub for the latest Richyss release. */
sealed interface UpdateResult {
    data class UpToDate(val currentVersion: String) : UpdateResult
    data class Available(val tag: String, val url: String) : UpdateResult
    data class Failure(val message: String) : UpdateResult
}

class UpdateChecker(private val gitHub: GitHubClient) {

    suspend fun check(
        owner: String = AppSettings.DEFAULT_PRESET_OWNER,
        repo: String = AppSettings.DEFAULT_PRESET_REPO
    ): UpdateResult {
        val release = gitHub.latestRelease(owner, repo).getOrElse {
            return UpdateResult.Failure(it.message ?: "network_error")
        }
        if (release.tagName.isBlank()) {
            return UpdateResult.Failure("No release published yet")
        }
        return if (isNewer(release.tagName, BuildConfig.VERSION_NAME)) {
            UpdateResult.Available(release.tagName, release.htmlUrl)
        } else {
            UpdateResult.UpToDate(BuildConfig.VERSION_NAME)
        }
    }

    /** Compares semantic versions, ignoring a leading `v` and pre-release suffixes. */
    fun isNewer(remote: String, current: String): Boolean {
        val remoteParts = parse(remote) ?: return false
        val currentParts = parse(current) ?: return false
        for (index in 0 until maxOf(remoteParts.size, currentParts.size)) {
            val left = remoteParts.getOrElse(index) { 0 }
            val right = currentParts.getOrElse(index) { 0 }
            if (left != right) return left > right
        }
        return false
    }

    /**
     * Extracts the first `major.minor.patch` group from a release tag. Richyss tags
     * look like `richyss-apk-v1.0.0`, so a plain `removePrefix("v")` is not enough.
     */
    private fun parse(version: String): List<Int>? {
        val match = Regex("""(\d+)(?:\.(\d+))?(?:\.(\d+))?""")
            .find(version.trim()) ?: return null
        return listOf(1, 2, 3).map { index ->
            match.groupValues.getOrNull(index)?.toIntOrNull() ?: 0
        }
    }
}
