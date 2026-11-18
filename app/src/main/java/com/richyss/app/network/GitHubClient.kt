package com.richyss.app.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tagName: String = "",
    @SerialName("html_url") val htmlUrl: String = "",
    @SerialName("body") val body: String = "",
    @SerialName("published_at") val publishedAt: String = ""
)

/** GitHub raw content and release lookups used by the preset library and the updater. */
class GitHubClient(private val http: HttpClient) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun rawFile(owner: String, repo: String, branch: String, path: String): HttpResult {
        val url = "https://raw.githubusercontent.com/$owner/$repo/$branch/${path.trimStart('/')}"
        return http.get(url, accept = "text/plain")
    }

    suspend fun latestRelease(owner: String, repo: String): Result<GitHubRelease> {
        return when (val result = http.get("https://api.github.com/repos/$owner/$repo/releases/latest")) {
            is HttpResult.Failure -> Result.failure(IllegalStateException(result.message))
            is HttpResult.Success -> runCatching {
                json.decodeFromString(GitHubRelease.serializer(), result.body)
            }
        }
    }
}
