package ru.hippo.Sonora.music

import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.net.URL
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private const val DEFAULT_BACKEND_BASE_URL = "https://api.corebrew.ru"
private const val BACKEND_SEARCH_PATH = "/api/spotify/search"
private const val BACKEND_DOWNLOAD_PATH = "/api/download"
private const val BACKEND_TOP_TRACKS_PATH_TEMPLATE = "/api/spotify/artists/%s/top-tracks"
private const val INSTALL_UNAVAILABLE_MESSAGE = "Установка временно недоступна, попробуйте завтра."
private const val VPN_REQUIRED_MESSAGE = "Требуется VPN из-за региональных ограничений (451)."

data class MiniStreamingTrack(
    val trackId: String,
    val title: String,
    val artists: String,
    val durationMs: Long,
    val artworkUrl: String
)

data class MiniStreamingArtist(
    val artistId: String,
    val name: String,
    val artworkUrl: String
)

data class MiniStreamingDownloadPayload(
    val trackId: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val artworkUrl: String,
    val mediaUrl: String,
    val extension: String
)

private data class BackendHttpResult(
    val statusCode: Int,
    val payload: JSONObject?,
    val transportError: String
)

class MiniStreamingClient(
    backendBaseUrl: String = DEFAULT_BACKEND_BASE_URL
) {
    private val normalizedBackendBaseUrl: String = normalizeBackendBaseUrl(backendBaseUrl)

    @Volatile
    private var lastResolveFailureMessage: String = ""

    fun isConfigured(): Boolean {
        return normalizedBackendBaseUrl.isNotBlank()
    }

    fun consumeLastResolveFailureMessage(): String? {
        val message = lastResolveFailureMessage.trim()
        lastResolveFailureMessage = ""
        return message.ifBlank { null }
    }

    suspend fun searchTracks(query: String, limit: Int = 8): List<MiniStreamingTrack> {
        if (!isConfigured()) {
            return emptyList()
        }
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            return emptyList()
        }

        return withContext(Dispatchers.IO) {
            val boundedLimit = limit.coerceIn(1, 50)
            val requestUrl = buildBackendUrl(
                path = BACKEND_SEARCH_PATH,
                query = listOf(
                    "q" to normalizedQuery,
                    "type" to "track",
                    "limit" to boundedLimit.toString()
                )
            ) ?: return@withContext emptyList()

            val response = requestJson(url = requestUrl)
            if (response.statusCode !in 200..299) {
                return@withContext emptyList()
            }

            val payload = extractPayload(response.payload)
            val tracks = payload.optJSONObject("tracks")?.optJSONArray("items")
                ?: response.payload?.optJSONObject("tracks")?.optJSONArray("items")
                ?: JSONArray()
            parseTracksArray(tracks, limit = boundedLimit)
        }
    }

    suspend fun searchArtists(query: String, limit: Int = 10): List<MiniStreamingArtist> {
        if (!isConfigured()) {
            return emptyList()
        }
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            return emptyList()
        }

        return withContext(Dispatchers.IO) {
            val boundedLimit = limit.coerceIn(1, 50)
            val requestUrl = buildBackendUrl(
                path = BACKEND_SEARCH_PATH,
                query = listOf(
                    "q" to normalizedQuery,
                    "type" to "artist",
                    "limit" to boundedLimit.toString()
                )
            ) ?: return@withContext emptyList()

            val response = requestJson(url = requestUrl)
            if (response.statusCode !in 200..299) {
                return@withContext emptyList()
            }

            val payload = extractPayload(response.payload)
            val artists = payload.optJSONObject("artists")?.optJSONArray("items")
                ?: response.payload?.optJSONObject("artists")?.optJSONArray("items")
                ?: JSONArray()

            buildList {
                for (index in 0 until artists.length()) {
                    val item = artists.optJSONObject(index) ?: continue
                    val artistId = item.optString("id").trim()
                    if (artistId.isBlank()) continue
                    add(
                        MiniStreamingArtist(
                            artistId = artistId,
                            name = item.optString("name").ifBlank { "Artist" },
                            artworkUrl = bestImageUrl(item.optJSONArray("images"))
                        )
                    )
                    if (size >= boundedLimit) {
                        break
                    }
                }
            }
        }
    }

    suspend fun fetchTopTracksForArtist(artistId: String, limit: Int = 30): List<MiniStreamingTrack> {
        if (!isConfigured()) {
            return emptyList()
        }
        val normalizedArtistId = artistId.trim()
        if (normalizedArtistId.isBlank()) {
            return emptyList()
        }

        return withContext(Dispatchers.IO) {
            val boundedLimit = if (limit <= 0) 50 else limit.coerceIn(1, 200)
            val encodedArtistId = encodeQueryValue(normalizedArtistId)
            val path = BACKEND_TOP_TRACKS_PATH_TEMPLATE.format(encodedArtistId)
            val requestUrl = buildBackendUrl(
                path = path,
                query = listOf("limit" to boundedLimit.toString())
            ) ?: return@withContext emptyList()

            val response = requestJson(url = requestUrl)
            if (response.statusCode !in 200..299) {
                return@withContext emptyList()
            }

            val payload = extractPayload(response.payload)
            val tracksArray = payload.optJSONArray("tracks")
                ?: payload.optJSONObject("tracks")?.optJSONArray("items")
                ?: payload.optJSONArray("items")
                ?: response.payload?.optJSONArray("tracks")
                ?: JSONArray()
            parseTracksArray(tracksArray, limit = boundedLimit)
        }
    }

    suspend fun resolveDownload(trackId: String): MiniStreamingDownloadPayload? {
        if (!isConfigured()) {
            lastResolveFailureMessage = INSTALL_UNAVAILABLE_MESSAGE
            return null
        }
        val normalizedTrackId = trackId.trim()
        if (normalizedTrackId.isBlank()) {
            lastResolveFailureMessage = INSTALL_UNAVAILABLE_MESSAGE
            return null
        }

        return withContext(Dispatchers.IO) {
            val trackUrl = "https://open.spotify.com/track/$normalizedTrackId"
            val requestUrl = buildBackendUrl(
                path = BACKEND_DOWNLOAD_PATH,
                query = listOf(
                    "trackId" to normalizedTrackId,
                    "trackUrl" to trackUrl
                )
            )

            if (requestUrl == null) {
                lastResolveFailureMessage = INSTALL_UNAVAILABLE_MESSAGE
                return@withContext null
            }

            val response = requestJson(url = requestUrl)

            if (response.transportError.isNotBlank()) {
                val transport = response.transportError
                lastResolveFailureMessage = when {
                    isVpnErrorText(transport) -> VPN_REQUIRED_MESSAGE
                    else -> transport
                }
                return@withContext null
            }

            val parsed = parseDownloadPayload(response.payload, normalizedTrackId)
            if (response.statusCode in 200..299 && parsed != null) {
                lastResolveFailureMessage = ""
                return@withContext parsed
            }

            val status = response.statusCode
            val extractedMessage = extractErrorMessage(response.payload)
            val normalizedMessage = when {
                status == 451 -> VPN_REQUIRED_MESSAGE
                extractedMessage.isNotBlank() -> extractedMessage
                status !in 200..299 -> "RapidAPI request failed ($status)."
                else -> "RapidAPI did not return media url."
            }

            lastResolveFailureMessage = if (isQuotaMessage(normalizedMessage)) {
                INSTALL_UNAVAILABLE_MESSAGE
            } else {
                normalizedMessage
            }
            null
        }
    }

    suspend fun downloadToFile(sourceUrl: String, destinationFile: File): Boolean {
        val normalizedUrl = sourceUrl.trim()
        if (normalizedUrl.isBlank()) {
            return false
        }

        return withContext(Dispatchers.IO) {
            runCatching {
                destinationFile.parentFile?.mkdirs()
                val connection = (URL(normalizedUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    instanceFollowRedirects = true
                    connectTimeout = 25_000
                    readTimeout = 120_000
                    setRequestProperty("User-Agent", "SonoraAndroid/1.0")
                }
                try {
                    connection.connect()
                    if (connection.responseCode !in 200..299) {
                        return@runCatching false
                    }
                    connection.inputStream.use { input ->
                        destinationFile.outputStream().use { output ->
                            val buffer = ByteArray(64 * 1024)
                            while (true) {
                                ensureActive()
                                val read = input.read(buffer)
                                if (read < 0) {
                                    break
                                }
                                output.write(buffer, 0, read)
                            }
                        }
                    }
                    destinationFile.exists() && destinationFile.length() > 0L
                } finally {
                    connection.disconnect()
                }
            }.getOrDefault(false)
        }
    }

    private fun requestJson(
        url: String,
        method: String = "GET",
        body: String? = null,
        headers: Map<String, String> = emptyMap()
    ): BackendHttpResult {
        return runCatching {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                instanceFollowRedirects = true
                connectTimeout = 25_000
                readTimeout = 30_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "SonoraAndroid/1.0")
                for ((header, value) in headers) {
                    setRequestProperty(header, value)
                }
                if (!body.isNullOrBlank()) {
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                }
            }

            if (!body.isNullOrBlank()) {
                connection.outputStream.use { output ->
                    output.write(body.toByteArray(StandardCharsets.UTF_8))
                }
            }

            connection.connect()
            val status = connection.responseCode
            val responseText = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()
            connection.disconnect()

            val payload = if (responseText.isBlank()) {
                null
            } else {
                runCatching { JSONObject(responseText) }.getOrNull()
            }

            BackendHttpResult(
                statusCode = status,
                payload = payload,
                transportError = ""
            )
        }.getOrElse { error ->
            BackendHttpResult(
                statusCode = -1,
                payload = null,
                transportError = error.message.orEmpty()
            )
        }
    }

    private fun buildBackendUrl(path: String, query: List<Pair<String, String>> = emptyList()): String? {
        val base = normalizedBackendBaseUrl
        if (base.isBlank()) {
            return null
        }
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        val queryPart = query
            .filter { it.first.isNotBlank() }
            .joinToString("&") { (key, value) ->
                "${encodeQueryValue(key)}=${encodeQueryValue(value)}"
            }

        return if (queryPart.isBlank()) {
            "$base$normalizedPath"
        } else {
            "$base$normalizedPath?$queryPart"
        }
    }

    private fun extractPayload(payload: JSONObject?): JSONObject {
        if (payload == null) {
            return JSONObject()
        }
        return payload.optJSONObject("data") ?: payload
    }

    private fun parseTracksArray(items: JSONArray?, limit: Int): List<MiniStreamingTrack> {
        if (items == null) {
            return emptyList()
        }
        val boundedLimit = limit.coerceAtLeast(1)
        val parsed = ArrayList<MiniStreamingTrack>(items.length())
        for (index in 0 until items.length()) {
            val item = items.optJSONObject(index) ?: continue
            val track = parseTrackItem(item) ?: continue
            parsed.add(track)
            if (parsed.size >= boundedLimit) {
                break
            }
        }
        return parsed
    }

    private fun parseTrackItem(item: JSONObject): MiniStreamingTrack? {
        val trackId = item.optString("id").trim()
            .ifBlank {
                item.optString("trackId").trim()
                    .ifBlank { item.optString("spotifyTrackId").trim() }
            }
        if (trackId.isBlank()) {
            return null
        }

        val artwork = bestImageUrl(item.optJSONObject("album")?.optJSONArray("images"))
            .ifBlank {
                item.optString("artworkUrl").trim()
                    .ifBlank { item.optString("thumbnail").trim() }
            }

        return MiniStreamingTrack(
            trackId = trackId,
            title = item.optString("name").ifBlank {
                item.optString("title").ifBlank { "Track" }
            },
            artists = parseArtists(item.optJSONArray("artists")).ifBlank {
                item.optString("artist").ifBlank { "Spotify" }
            },
            durationMs = item.optLong("duration_ms", 0L)
                .coerceAtLeast(0L)
                .takeIf { it > 0L }
                ?: parseDurationToMs(item.optString("duration")),
            artworkUrl = artwork
        )
    }

    private fun parseDownloadPayload(payload: JSONObject?, trackId: String): MiniStreamingDownloadPayload? {
        if (payload == null) {
            return null
        }

        val level1 = payload.optJSONObject("data") ?: payload
        val level2 = level1.optJSONObject("data")
        val data = level2 ?: level1
        val topSuccess = if (payload.has("success")) parseTruthy(payload.opt("success")) else true
        val level1Success = if (level1.has("success")) parseTruthy(level1.opt("success")) else true
        val dataSuccess = if (data.has("success")) parseTruthy(data.opt("success")) else true
        val success = topSuccess && level1Success && dataSuccess

        var mediaUrl = ""
        var extension = ""

        val medias = data.optJSONArray("medias") ?: JSONArray()
        for (index in 0 until medias.length()) {
            val media = medias.optJSONObject(index) ?: continue
            val candidateUrl = media.optString("url").trim()
            if (candidateUrl.isBlank()) {
                continue
            }
            if (mediaUrl.isBlank()) {
                mediaUrl = candidateUrl
            }
            val mediaType = media.optString("type").trim().lowercase()
            val mediaExt = media.optString("extension").trim().lowercase()
            if (mediaType == "audio" || mediaExt == "mp3") {
                mediaUrl = candidateUrl
                extension = mediaExt
                break
            }
            if (extension.isBlank()) {
                extension = mediaExt
            }
        }

        if (mediaUrl.isBlank()) mediaUrl = data.optString("downloadLink").trim()
        if (mediaUrl.isBlank()) mediaUrl = data.optString("mediaUrl").trim()
        if (mediaUrl.isBlank()) mediaUrl = data.optString("link").trim()
        if (mediaUrl.isBlank()) mediaUrl = data.optString("url").trim()

        if (!success || mediaUrl.isBlank()) {
            return null
        }

        val title = data.optString("title").ifBlank { "Track" }
        val artist = data.optString("author")
            .ifBlank { data.optString("artist") }
            .ifBlank { "Spotify" }
        val durationMs = parseDurationToMs(data.optString("duration"))
        val artworkUrl = data.optString("thumbnail")
            .ifBlank { data.optString("cover") }
            .ifBlank { data.optString("artworkUrl") }
            .orEmpty()

        val normalizedExtension = extension.ifBlank {
            guessExtensionFromUrl(mediaUrl)
        }.ifBlank { "mp3" }

        return MiniStreamingDownloadPayload(
            trackId = trackId,
            title = title,
            artist = artist,
            durationMs = durationMs,
            artworkUrl = artworkUrl,
            mediaUrl = mediaUrl,
            extension = normalizedExtension
        )
    }

    private fun parseArtists(items: JSONArray?): String {
        if (items == null) {
            return ""
        }
        val names = buildList {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue
                val name = item.optString("name").trim()
                if (name.isNotBlank()) {
                    add(name)
                }
            }
        }
        return names.joinToString(", ")
    }

    private fun bestImageUrl(items: JSONArray?): String {
        if (items == null || items.length() == 0) {
            return ""
        }
        var bestUrl = ""
        var bestSize = -1
        for (index in 0 until items.length()) {
            val image = items.optJSONObject(index) ?: continue
            val candidateUrl = image.optString("url").trim()
            if (candidateUrl.isBlank()) continue
            val width = image.optInt("width", 0)
            if (width > bestSize) {
                bestUrl = candidateUrl
                bestSize = width
            }
        }
        return bestUrl
    }

    private fun parseDurationToMs(raw: String?): Long {
        val normalized = raw?.trim().orEmpty()
        if (normalized.isBlank()) {
            return 0L
        }
        val chunks = normalized.split(":")
        val seconds = when (chunks.size) {
            1 -> chunks[0].toLongOrNull() ?: 0L
            2 -> {
                val minutes = chunks[0].toLongOrNull() ?: 0L
                val restSeconds = chunks[1].toLongOrNull() ?: 0L
                minutes * 60L + restSeconds
            }
            3 -> {
                val hours = chunks[0].toLongOrNull() ?: 0L
                val minutes = chunks[1].toLongOrNull() ?: 0L
                val restSeconds = chunks[2].toLongOrNull() ?: 0L
                hours * 3600L + minutes * 60L + restSeconds
            }
            else -> 0L
        }
        return seconds.coerceAtLeast(0L) * 1000L
    }

    private fun guessExtensionFromUrl(url: String): String {
        return runCatching {
            val path = URI(url).path
            val ext = path.substringAfterLast('.', "")
            ext.trim().lowercase().ifBlank { "mp3" }
        }.getOrDefault("mp3")
    }

    private fun extractErrorMessage(payload: JSONObject?): String {
        if (payload == null) {
            return ""
        }

        val rootMessage = payload.optString("message").trim()
        if (rootMessage.isNotBlank()) {
            return rootMessage
        }

        val rootError = payload.opt("error")
        if (rootError is String && rootError.trim().isNotBlank()) {
            return rootError.trim()
        }
        if (rootError is JSONObject) {
            val nested = rootError.optString("message").trim()
            if (nested.isNotBlank()) {
                return nested
            }
        }

        val data = payload.optJSONObject("data")
        if (data != null) {
            val dataMessage = data.optString("message").trim()
            if (dataMessage.isNotBlank()) {
                return dataMessage
            }
            val dataError = data.optString("error").trim()
            if (dataError.isNotBlank()) {
                return dataError
            }
            val nestedData = data.optJSONObject("data")
            if (nestedData != null) {
                val nestedMessage = nestedData.optString("message").trim()
                if (nestedMessage.isNotBlank()) {
                    return nestedMessage
                }
                val nestedError = nestedData.optString("error").trim()
                if (nestedError.isNotBlank()) {
                    return nestedError
                }
            }
        }

        return ""
    }

    private fun isQuotaMessage(message: String): Boolean {
        val normalized = message.trim().lowercase()
        if (normalized.isBlank()) {
            return false
        }
        return normalized.contains("daily quota") ||
            normalized.contains("quota exceeded") ||
            normalized.contains("quota") ||
            normalized.contains("exceeded")
    }

    private fun isVpnErrorText(message: String): Boolean {
        val normalized = message.trim().lowercase()
        if (normalized.isBlank()) {
            return false
        }
        return normalized.contains("unable to resolve host") ||
            normalized.contains("no address associated with hostname") ||
            normalized.contains("could not resolve host")
    }

    private fun parseTruthy(value: Any?): Boolean {
        return when (value) {
            null -> true
            is Boolean -> value
            is Number -> value.toInt() != 0
            is String -> {
                val normalized = value.trim().lowercase()
                normalized == "true" || normalized == "1" || normalized == "yes"
            }
            else -> true
        }
    }

    private fun encodeQueryValue(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name())
    }

    private fun normalizeBackendBaseUrl(rawBase: String): String {
        val trimmed = rawBase.trim().ifBlank { DEFAULT_BACKEND_BASE_URL }
        val withScheme = when {
            trimmed.startsWith("http://", ignoreCase = true) -> trimmed
            trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            else -> "https://$trimmed"
        }
        return withScheme.trimEnd('/')
    }
}
