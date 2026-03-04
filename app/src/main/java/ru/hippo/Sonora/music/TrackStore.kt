package ru.hippo.Sonora.music

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
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

    fun loadTracks(): List<TrackItem> {
        if (!tracksFile.exists()) {
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
                saveTracks(hydrated)
            }
            hydrated
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveTracks(tracks: List<TrackItem>) {
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

        val artworkFile = File(
            artworkDir,
            "art_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
        )

        return try {
            artworkFile.outputStream().use { output ->
                output.write(bytes)
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
