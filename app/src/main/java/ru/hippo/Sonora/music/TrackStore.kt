package ru.hippo.Sonora.music

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.mpatric.mp3agic.ID3v24Tag
import com.mpatric.mp3agic.Mp3File
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class TrackStore(private val context: Context) {

    private data class TrackMetadata(
        val title: String,
        val artist: String,
        val durationMs: Long,
        val artworkPath: String?
    )

    private val tracksFile = File(context.filesDir, "sonora_tracks_v1.json")
    private val musicDir = File(context.filesDir, "Sonora").apply { mkdirs() }
    private val artworkDir = File(musicDir, "artwork").apply { mkdirs() }
    private val trackStoreLock = Any()
    @Volatile
    private var cachedTracks: List<TrackItem>? = null

    fun loadTracks(): List<TrackItem> {
        cachedTracks?.let { return it }
        synchronized(trackStoreLock) {
            cachedTracks?.let { return it }
            return loadTracksFromDisk()
        }
    }

    fun saveTracks(tracks: List<TrackItem>) {
        synchronized(trackStoreLock) {
            saveTracksInternal(tracks)
        }
    }

    private fun loadTracksFromDisk(): List<TrackItem> {
        if (!tracksFile.exists()) {
            cachedTracks = emptyList()
            return emptyList()
        }
        return try {
            val raw = tracksFile.readText()
            val array = JSONArray(raw)
            val parsed = buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(
                        TrackItem(
                            id = item.optString("id"),
                            title = item.optString("title"),
                            artist = item.optString("artist"),
                            durationMs = item.optLong("durationMs"),
                            filePath = item.optString("filePath"),
                            artworkPath = item.optString("artworkPath").ifBlank { null },
                            addedAt = item.optLong("addedAt"),
                            isFavorite = item.optBoolean("isFavorite", false)
                        )
                    )
                }
            }.filter { it.id.isNotBlank() && it.filePath.isNotBlank() && File(it.filePath).exists() }

            var migratedArtwork = false
            val hydrated = parsed.map { track ->
                if (!track.artworkPath.isNullOrBlank()) {
                    return@map track
                }

                val migratedPath = extractArtworkFromExistingFile(track.filePath)
                if (migratedPath == null) {
                    return@map track
                }

                migratedArtwork = true
                track.copy(artworkPath = migratedPath)
            }

            val hadStaleItems = hydrated.size != array.length()
            if (hadStaleItems || migratedArtwork) {
                saveTracksInternal(hydrated)
            } else {
                cachedTracks = hydrated
            }
            hydrated
        } catch (_: Exception) {
            cachedTracks = emptyList()
            emptyList()
        }
    }

    private fun saveTracksInternal(tracks: List<TrackItem>) {
        val array = JSONArray()
        for (track in tracks) {
            val item = JSONObject()
            item.put("id", track.id)
            item.put("title", track.title)
            item.put("artist", track.artist)
            item.put("durationMs", track.durationMs)
            item.put("filePath", track.filePath)
            item.put("artworkPath", track.artworkPath ?: "")
            item.put("addedAt", track.addedAt)
            item.put("isFavorite", track.isFavorite)
            array.put(item)
        }
        tracksFile.writeText(array.toString())
        cachedTracks = tracks.toList()
    }

    fun importTracks(uris: List<Uri>): ImportResult {
        val current = loadTracks().toMutableList()
        var added = 0
        var failed = 0

        for (uri in uris) {
            val imported = tryImportTrack(uri)
            if (imported == null) {
                failed += 1
                continue
            }
            current.add(0, imported)
            added += 1
        }

        saveTracks(current)
        return ImportResult(added = added, failed = failed)
    }

    fun importRemoteTrack(urlString: String, suggestedName: String, artworkUrl: String? = null): TrackItem? {
        if (urlString.isBlank()) {
            return null
        }

        val fileExtension = sanitizeExtension(
            suggestedName.substringAfterLast('.', urlString.substringAfterLast('.', "mp3"))
        )
        val outputFile = File(
            musicDir,
            "${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.$fileExtension"
        )

        val copied = downloadRemoteFile(
            urlString = urlString,
            outputFile = outputFile,
            connectTimeoutMs = 20_000,
            readTimeoutMs = 300_000
        )

        if (!copied || !outputFile.exists()) {
            if (outputFile.exists()) {
                outputFile.delete()
            }
            return null
        }

        val metadata = readMetadata(outputFile, suggestedName.ifBlank { "Track_${System.currentTimeMillis()}" })
        val importedArtworkPath = metadata.artworkPath ?: importRemoteArtwork(
            urlString = artworkUrl.orEmpty(),
            suggestedName = suggestedName.substringBeforeLast('.', suggestedName).ifBlank { "cover" } + ".jpg"
        )
        val imported = TrackItem(
            id = UUID.randomUUID().toString(),
            title = metadata.title,
            artist = metadata.artist,
            durationMs = metadata.durationMs,
            filePath = outputFile.absolutePath,
            artworkPath = importedArtworkPath,
            addedAt = System.currentTimeMillis(),
            isFavorite = false
        )

        val current = loadTracks().toMutableList()
        current.add(0, imported)
        saveTracks(current)
        return imported
    }

    fun importRemoteArtwork(urlString: String, suggestedName: String = "cover.jpg"): String? {
        val bytes = downloadRemoteBytes(urlString) ?: return null
        return importArtworkBytes(bytes, suggestedName)
    }

    fun downloadRemoteBytes(
        urlString: String,
        connectTimeoutMs: Int = 15_000,
        readTimeoutMs: Int = 120_000
    ): ByteArray? {
        if (urlString.isBlank()) {
            return null
        }

        return runCatching {
            val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = true
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                setRequestProperty("User-Agent", "SonoraAndroid/1.0")
            }
            connection.connect()
            if (connection.responseCode !in 200..299) {
                connection.disconnect()
                return@runCatching null
            }
            val bytes = connection.inputStream.use { it.readBytes() }
            connection.disconnect()
            bytes.takeIf { it.isNotEmpty() }
        }.getOrNull()
    }

    fun importArtworkBytes(bytes: ByteArray, suggestedName: String = "cover.jpg"): String? {
        if (bytes.isEmpty()) {
            return null
        }

        val preparedBytes = squareArtworkBytes(bytes) ?: bytes
        val fileExtension = if (preparedBytes === bytes) {
            sanitizeExtension(suggestedName.substringAfterLast('.', "jpg"))
        } else {
            "jpg"
        }
        val outputFile = File(
            artworkDir,
            "art_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.$fileExtension"
        )

        return try {
            outputFile.outputStream().use { output ->
                output.write(preparedBytes)
                output.flush()
            }
            outputFile.absolutePath
        } catch (_: Exception) {
            if (outputFile.exists()) {
                outputFile.delete()
            }
            null
        }
    }

    fun rewriteDownloadedMp3Metadata(
        filePath: String,
        preferredTitle: String,
        preferredArtist: String,
        preferredArtwork: ByteArray?
    ): Boolean {
        if (filePath.isBlank()) {
            return false
        }

        val sourceFile = File(filePath)
        if (!sourceFile.exists() || sourceFile.extension.lowercase() != "mp3") {
            return false
        }

        val tempFile = File(
            sourceFile.parentFile ?: musicDir,
            "${sourceFile.nameWithoutExtension}_retag.${sourceFile.extension}"
        )

        return try {
            val mp3File = Mp3File(sourceFile.absolutePath)
            val tag = (mp3File.id3v2Tag as? ID3v24Tag) ?: ID3v24Tag()
            val resolvedTitle = preferredTitle.trim()
            val resolvedArtist = sanitizeDownloadedArtist(preferredArtist)
            if (resolvedTitle.isNotBlank()) {
                tag.title = resolvedTitle
            }
            if (resolvedArtist.isNotBlank()) {
                tag.artist = resolvedArtist
                setOptionalId3v2StringField(tag, "setAlbumArtist", resolvedArtist)
                setOptionalId3v2StringField(tag, "setOriginalArtist", resolvedArtist)
            }
            if (preferredArtwork != null && preferredArtwork.isNotEmpty()) {
                val squaredArtwork = squareArtworkBytes(preferredArtwork)
                if (squaredArtwork != null && squaredArtwork.isNotEmpty()) {
                    tag.setAlbumImage(squaredArtwork, "image/jpeg")
                }
            }
            mp3File.id3v2Tag = tag
            mp3File.save(tempFile.absolutePath)
            if (!sourceFile.delete()) {
                tempFile.delete()
                return false
            }
            if (!tempFile.renameTo(sourceFile)) {
                tempFile.delete()
                return false
            }
            true
        } catch (_: Exception) {
            if (tempFile.exists()) {
                tempFile.delete()
            }
            false
        }
    }

    private fun squareArtworkBytes(bytes: ByteArray): ByteArray? {
        if (bytes.isEmpty()) {
            return null
        }

        return try {
            val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
            val side = minOf(original.width, original.height)
            if (side <= 1) {
                return bytes
            }

            val squared = if (original.width == side && original.height == side) {
                original
            } else {
                Bitmap.createBitmap(
                    original,
                    (original.width - side) / 2,
                    (original.height - side) / 2,
                    side,
                    side
                )
            }

            val output = ByteArrayOutputStream()
            val compressed = squared.compress(Bitmap.CompressFormat.JPEG, 94, output)
            if (!compressed) {
                return bytes
            }
            output.toByteArray().takeIf { it.isNotEmpty() } ?: bytes
        } catch (_: Exception) {
            bytes
        }
    }

    private fun sanitizeDownloadedArtist(value: String): String {
        val trimmed = value.trim()
        if (trimmed.isBlank()) {
            return ""
        }

        val suffix = listOf(" - Topic", " – Topic", " — Topic")
            .firstOrNull { candidate -> trimmed.lowercase().endsWith(candidate.lowercase()) }
        return if (suffix != null && trimmed.length > suffix.length) {
            trimmed.dropLast(suffix.length).trim()
        } else {
            trimmed
        }
    }

    private fun setOptionalId3v2StringField(tag: ID3v24Tag, methodName: String, value: String) {
        if (value.isBlank()) {
            return
        }

        runCatching {
            tag.javaClass.methods
                .firstOrNull { method ->
                    method.name == methodName &&
                        method.parameterTypes.size == 1 &&
                        method.parameterTypes[0] == String::class.java
                }
                ?.invoke(tag, value)
        }
    }

    fun deleteTrackFiles(track: TrackItem): Boolean {
        var deletedAny = false
        var failed = false

        val audioPath = track.filePath
        if (audioPath.isNotBlank()) {
            val audioFile = File(audioPath)
            if (audioFile.exists()) {
                val deleted = runCatching { audioFile.delete() }.getOrDefault(false)
                deletedAny = deletedAny || deleted
                if (!deleted) {
                    failed = true
                }
            }
        }

        val artworkPath = track.artworkPath
        if (!artworkPath.isNullOrBlank()) {
            val artworkFile = File(artworkPath)
            if (artworkFile.exists()) {
                val deleted = runCatching { artworkFile.delete() }.getOrDefault(false)
                deletedAny = deletedAny || deleted
                if (!deleted) {
                    failed = true
                }
            }
        }

        return deletedAny && !failed
    }

    private fun tryImportTrack(uri: Uri): TrackItem? {
        val displayName = queryDisplayName(uri) ?: "Track_${System.currentTimeMillis()}"
        val fileExtension = sanitizeExtension(displayName.substringAfterLast('.', "mp3"))
        val outputFile = File(
            musicDir,
            "${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.$fileExtension"
        )

        val copied = try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
                true
            } ?: false
        } catch (_: Exception) {
            false
        }

        if (!copied || !outputFile.exists()) {
            if (outputFile.exists()) {
                outputFile.delete()
            }
            return null
        }

        val metadata = readMetadata(outputFile, displayName)
        return TrackItem(
            id = UUID.randomUUID().toString(),
            title = metadata.title,
            artist = metadata.artist,
            durationMs = metadata.durationMs,
            filePath = outputFile.absolutePath,
            artworkPath = metadata.artworkPath,
            addedAt = System.currentTimeMillis(),
            isFavorite = false
        )
    }

    private fun sanitizeExtension(extension: String): String {
        val normalized = extension.lowercase().replace(Regex("[^a-z0-9]"), "")
        return normalized.ifBlank { "mp3" }
    }

    private fun queryDisplayName(uri: Uri): String? {
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
        return context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index < 0 || !cursor.moveToFirst()) {
                return@use null
            }
            cursor.getString(index)
        }
    }

    private fun downloadRemoteFile(
        urlString: String,
        outputFile: File,
        connectTimeoutMs: Int,
        readTimeoutMs: Int
    ): Boolean {
        return runCatching {
            val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = true
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                setRequestProperty("User-Agent", "SonoraAndroid/1.0")
            }
            connection.connect()
            if (connection.responseCode !in 200..299) {
                connection.disconnect()
                return@runCatching false
            }
            connection.inputStream.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            connection.disconnect()
            true
        }.getOrDefault(false)
    }

    private fun readMetadata(file: File, fallbackName: String): TrackMetadata {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?.takeIf { it.isNotBlank() }
                ?: fallbackName.substringBeforeLast('.')
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?.takeIf { it.isNotBlank() }
                ?: ""
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?: 0L
            val artworkPath = saveArtworkIfPresent(retriever.embeddedPicture)

            TrackMetadata(
                title = title,
                artist = artist,
                durationMs = duration,
                artworkPath = artworkPath
            )
        } catch (_: Exception) {
            TrackMetadata(
                title = fallbackName.substringBeforeLast('.'),
                artist = "",
                durationMs = 0L,
                artworkPath = null
            )
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
                // no-op
            }
        }
    }

    private fun saveArtworkIfPresent(bytes: ByteArray?): String? {
        if (bytes == null || bytes.isEmpty()) {
            return null
        }

        val preparedBytes = squareArtworkBytes(bytes) ?: bytes
        val artworkFile = File(
            artworkDir,
            "art_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
        )

        return try {
            artworkFile.outputStream().use { output ->
                output.write(preparedBytes)
                output.flush()
            }
            artworkFile.absolutePath
        } catch (_: Exception) {
            if (artworkFile.exists()) {
                artworkFile.delete()
            }
            null
        }
    }

    private fun extractArtworkFromExistingFile(filePath: String): String? {
        if (filePath.isBlank()) {
            return null
        }

        val file = File(filePath)
        if (!file.exists()) {
            return null
        }

        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            saveArtworkIfPresent(retriever.embeddedPicture)
        } catch (_: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
                // no-op
            }
        }
    }
}
