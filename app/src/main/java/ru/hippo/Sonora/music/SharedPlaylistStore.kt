package ru.hippo.Sonora.music

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class SharedPlaylistTrackEntry(
    val id: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val artworkUrl: String?,
    val fileUrl: String?,
    val cachedArtworkPath: String?,
    val cachedFilePath: String?
)

data class SharedPlaylistEntry(
    val remoteId: String,
    val localId: String,
    val name: String,
    val shareUrl: String,
    val sourceBaseUrl: String,
    val contentSha256: String,
    val coverUrl: String?,
    val cachedCoverPath: String?,
    val tracks: List<SharedPlaylistTrackEntry>,
    val createdAt: Long
)

class SharedPlaylistStore(context: Context) {

    private val storeFile = File(context.filesDir, "sonora_shared_playlists_v1.json")
    private val cacheDir = File(context.filesDir, "Sonora/shared_playlist_cache").apply { mkdirs() }
    private val coverCacheDir = File(cacheDir, "covers").apply { mkdirs() }
    private val artworkCacheDir = File(cacheDir, "artworks").apply { mkdirs() }
    private val audioCacheDir = File(cacheDir, "audio").apply { mkdirs() }

    fun loadPlaylists(): List<SharedPlaylistEntry> {
        if (!storeFile.exists()) {
            return emptyList()
        }
        return runCatching {
            val array = JSONArray(storeFile.readText())
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val remoteId = item.optString("remoteId").trim()
                    val localId = item.optString("localId").trim().ifBlank { syntheticLocalId(remoteId) }
                    val name = item.optString("name").trim()
                    val shareUrl = item.optString("shareUrl").trim()
                    val sourceBaseUrl = item.optString("sourceBaseUrl").trim()
                    if (remoteId.isBlank() || localId.isBlank() || name.isBlank()) {
                        continue
                    }
                    val tracksArray = item.optJSONArray("tracks") ?: JSONArray()
                    val tracks = buildList {
                        for (trackIndex in 0 until tracksArray.length()) {
                            val track = tracksArray.optJSONObject(trackIndex) ?: continue
                            val title = track.optString("title").trim()
                            val artist = track.optString("artist").trim()
                            if (title.isBlank() && artist.isBlank()) {
                                continue
                            }
                            add(
                                SharedPlaylistTrackEntry(
                                    id = track.optString("id").trim().ifBlank { "track_${trackIndex + 1}" },
                                    title = title.ifBlank { "Track ${trackIndex + 1}" },
                                    artist = artist,
                                    durationMs = track.optLong("durationMs").coerceAtLeast(0L),
                                    artworkUrl = track.optString("artworkUrl").trim().ifBlank { null },
                                    fileUrl = track.optString("fileUrl").trim().ifBlank { null },
                                    cachedArtworkPath = track.optString("cachedArtworkPath").trim().ifBlank { null }?.takeIf { File(it).exists() },
                                    cachedFilePath = track.optString("cachedFilePath").trim().ifBlank { null }?.takeIf { File(it).exists() }
                                )
                            )
                        }
                    }
                    add(
                        SharedPlaylistEntry(
                            remoteId = remoteId,
                            localId = localId,
                            name = name,
                            shareUrl = shareUrl,
                            sourceBaseUrl = sourceBaseUrl,
                            contentSha256 = item.optString("contentSha256").trim(),
                            coverUrl = item.optString("coverUrl").trim().ifBlank { null },
                            cachedCoverPath = item.optString("cachedCoverPath").trim().ifBlank { null }?.takeIf { File(it).exists() },
                            tracks = tracks,
                            createdAt = item.optLong("createdAt").coerceAtLeast(0L)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun savePlaylists(playlists: List<SharedPlaylistEntry>) {
        val array = JSONArray()
        playlists.forEach { entry ->
            val item = JSONObject()
            item.put("remoteId", entry.remoteId)
            item.put("localId", entry.localId)
            item.put("name", entry.name)
            item.put("shareUrl", entry.shareUrl)
            item.put("sourceBaseUrl", entry.sourceBaseUrl)
            item.put("contentSha256", entry.contentSha256)
            item.put("coverUrl", entry.coverUrl ?: "")
            item.put("cachedCoverPath", entry.cachedCoverPath ?: "")
            item.put("createdAt", entry.createdAt)

            val tracksArray = JSONArray()
            entry.tracks.forEach { track ->
                val trackItem = JSONObject()
                trackItem.put("id", track.id)
                trackItem.put("title", track.title)
                trackItem.put("artist", track.artist)
                trackItem.put("durationMs", track.durationMs)
                trackItem.put("artworkUrl", track.artworkUrl ?: "")
                trackItem.put("fileUrl", track.fileUrl ?: "")
                trackItem.put("cachedArtworkPath", track.cachedArtworkPath ?: "")
                trackItem.put("cachedFilePath", track.cachedFilePath ?: "")
                tracksArray.put(trackItem)
            }
            item.put("tracks", tracksArray)
            array.put(item)
        }
        storeFile.writeText(array.toString())
    }

    fun findByRemoteId(remoteId: String): SharedPlaylistEntry? {
        if (remoteId.isBlank()) {
            return null
        }
        return loadPlaylists().firstOrNull { it.remoteId == remoteId }
    }

    fun upsert(entry: SharedPlaylistEntry) {
        val current = loadPlaylists().toMutableList()
        val index = current.indexOfFirst { it.remoteId == entry.remoteId }
        if (index >= 0) {
            current[index] = entry
        } else {
            current.add(0, entry)
        }
        savePlaylists(current)
    }

    fun removeByRemoteId(remoteId: String) {
        if (remoteId.isBlank()) {
            return
        }
        val current = loadPlaylists()
        current.firstOrNull { it.remoteId == remoteId }?.let { entry ->
            entry.cachedCoverPath?.let { path -> runCatching { File(path).delete() } }
            entry.tracks.forEach { track ->
                track.cachedArtworkPath?.let { path -> runCatching { File(path).delete() } }
                track.cachedFilePath?.let { path -> runCatching { File(path).delete() } }
            }
        }
        val filtered = current.filterNot { it.remoteId == remoteId }
        savePlaylists(filtered)
    }

    fun isLiked(remoteId: String): Boolean {
        if (remoteId.isBlank()) {
            return false
        }
        return loadPlaylists().any { it.remoteId == remoteId }
    }

    fun cacheAssets(
        entry: SharedPlaylistEntry,
        cacheAudio: Boolean = false,
        audioLimitBytes: Long = Long.MAX_VALUE,
        forceRefresh: Boolean = false,
        onProgress: ((SharedPlaylistEntry) -> Unit)? = null
    ): SharedPlaylistEntry {
        val cachedCoverPath = cacheRemoteFile(
            urlString = entry.coverUrl,
            destination = File(coverCacheDir, "${entry.remoteId}.img"),
            forceRefresh = forceRefresh
        ) ?: entry.cachedCoverPath

        val cachedTracks = entry.tracks.toMutableList()
        var updated = entry.copy(
            cachedCoverPath = cachedCoverPath,
            tracks = cachedTracks.toList()
        )
        upsert(updated)
        onProgress?.invoke(updated)

        entry.tracks.forEachIndexed { index, track ->
            val cachedArtworkPath = cacheRemoteFile(
                urlString = track.artworkUrl,
                destination = File(artworkCacheDir, "${entry.remoteId}_$index.img"),
                forceRefresh = forceRefresh
            ) ?: track.cachedArtworkPath
            val cachedFilePath = if (cacheAudio) {
                cacheRemoteAudioFile(
                    urlString = track.fileUrl,
                    destination = File(audioCacheDir, "${entry.remoteId}_$index.audio"),
                    audioLimitBytes = audioLimitBytes,
                    forceRefresh = forceRefresh
                ) ?: track.cachedFilePath
            } else {
                track.cachedFilePath
            }
            cachedTracks[index] = track.copy(
                cachedArtworkPath = cachedArtworkPath,
                cachedFilePath = cachedFilePath
            )
            updated = entry.copy(
                cachedCoverPath = cachedCoverPath,
                tracks = cachedTracks.toList()
            )
            upsert(updated)
            onProgress?.invoke(updated)
        }

        return updated
    }

    fun audioCacheUsageBytes(): Long {
        return audioCacheDir.listFiles()
            ?.filter { it.isFile }
            ?.sumOf { it.length() }
            ?: 0L
    }

    fun trimAudioCache(limitBytes: Long) {
        if (limitBytes == Long.MAX_VALUE) {
            return
        }
        val files = audioCacheDir.listFiles()
            ?.filter { it.isFile }
            ?.sortedBy { it.lastModified() }
            ?: return
        var total = files.sumOf { it.length() }
        files.forEach { file ->
            if (total <= limitBytes) {
                return
            }
            val size = file.length()
            if (runCatching { file.delete() }.getOrDefault(false)) {
                total -= size
            }
        }
    }

    private fun cacheRemoteFile(
        urlString: String?,
        destination: File,
        forceRefresh: Boolean
    ): String? {
        val normalized = urlString?.trim().orEmpty()
        if (normalized.isBlank()) {
            return null
        }
        if (!forceRefresh && destination.exists() && destination.isFile && destination.length() > 0L) {
            return destination.absolutePath
        }
        destination.parentFile?.mkdirs()
        val copied = runCatching {
            val connection = (URL(normalized).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = true
                connectTimeout = 15_000
                readTimeout = 120_000
                setRequestProperty("User-Agent", "SonoraAndroid/1.0")
            }
            connection.connect()
            if (connection.responseCode !in 200..299) {
                connection.disconnect()
                return@runCatching false
            }
            connection.inputStream.use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            connection.disconnect()
            true
        }.getOrDefault(false)
        if (!copied || !destination.exists() || destination.length() <= 0L) {
            runCatching { destination.delete() }
            return null
        }
        return destination.absolutePath
    }

    private fun cacheRemoteAudioFile(
        urlString: String?,
        destination: File,
        audioLimitBytes: Long,
        forceRefresh: Boolean
    ): String? {
        val normalized = urlString?.trim().orEmpty()
        if (normalized.isBlank()) {
            return null
        }
        if (!forceRefresh && destination.exists() && destination.isFile && destination.length() > 0L) {
            runCatching { destination.setLastModified(System.currentTimeMillis()) }
            return destination.absolutePath
        }
        destination.parentFile?.mkdirs()
        val tempFile = File(destination.parentFile, "${destination.name}.tmp")
        val copied = runCatching {
            val connection = (URL(normalized).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = true
                connectTimeout = 20_000
                readTimeout = 600_000
                setRequestProperty("User-Agent", "SonoraAndroid/1.0")
            }
            connection.connect()
            if (connection.responseCode !in 200..299) {
                connection.disconnect()
                return@runCatching false
            }
            connection.inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            connection.disconnect()
            true
        }.getOrDefault(false)
        if (!copied || !tempFile.exists() || tempFile.length() <= 0L) {
            runCatching { tempFile.delete() }
            return null
        }

        if (audioLimitBytes != Long.MAX_VALUE) {
            val existingSize = if (destination.exists()) destination.length() else 0L
            trimAudioCache((audioLimitBytes - (tempFile.length() - existingSize)).coerceAtLeast(0L))
            if (audioCacheUsageBytes() + tempFile.length() - existingSize > audioLimitBytes) {
                runCatching { tempFile.delete() }
                return null
            }
        }

        runCatching { destination.delete() }
        val moved = runCatching { tempFile.renameTo(destination) }.getOrDefault(false)
        if (!moved || !destination.exists() || destination.length() <= 0L) {
            runCatching { tempFile.delete() }
            runCatching { destination.delete() }
            return null
        }
        runCatching { destination.setLastModified(System.currentTimeMillis()) }
        return destination.absolutePath
    }

    companion object {
        fun syntheticLocalId(remoteId: String): String {
            return "shared:$remoteId"
        }
    }
}
