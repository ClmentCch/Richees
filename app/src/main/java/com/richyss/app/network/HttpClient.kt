package com.richyss.app.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Result of a network read, with the information the UI needs to explain failures. */
sealed interface HttpResult {
    data class Success(val body: String) : HttpResult
    data class Failure(val message: String, val code: Int? = null) : HttpResult
}

/**
 * Tiny HTTP helper on top of [HttpURLConnection]: no extra dependency, explicit
 * timeouts and a hard cap on the response size so a hostile or broken endpoint
 * cannot exhaust memory.
 */
class HttpClient(
    private val timeoutMs: Int = 15_000,
    private val maxBytes: Int = 2 * 1024 * 1024
) {

    suspend fun get(url: String, accept: String = "application/json"): HttpResult =
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = timeoutMs
                    readTimeout = timeoutMs
                    setRequestProperty("Accept", accept)
                    setRequestProperty("User-Agent", "Richyss-Android/1.0.0")
                    instanceFollowRedirects = true
                }
                val code = connection.responseCode
                if (code !in 200..299) {
                    return@withContext HttpResult.Failure("HTTP $code", code)
                }
                val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                    val builder = StringBuilder()
                    var total = 0
                    val buffer = CharArray(8 * 1024)
                    while (true) {
                        val read = reader.read(buffer)
                        if (read < 0) break
                        total += read
                        if (total > maxBytes) return@withContext HttpResult.Failure("Response too large")
                        builder.append(buffer, 0, read)
                    }
                    builder.toString()
                }
                HttpResult.Success(body)
            } catch (e: IOException) {
                HttpResult.Failure(e.message ?: "network_error")
            } finally {
                connection?.disconnect()
            }
        }
}
