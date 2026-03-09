package ru.hippo.Sonora.music

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.UUID

data class SonoraBackupSettings(
    val sliderStyle: String,
    val artworkStyle: String,
    val fontStyle: String,
    val accentHex: String,
    val preservePlayerModes: Boolean,
    val trackGapSeconds: Float,
    val maxStorageMb: Int
)

data class SonoraBackupRestoreResult(
    val tracks: List<TrackItem>,
    val playlists: List<PlaylistEntry>,
    val settings: SonoraBackupSettings?
)

object SonoraBackupArchive {

    private const val ARCHIVE_VERSION = 1
    private val ARCHIVE_MAGIC = "SONORAAR".toByteArray(StandardCharsets.US_ASCII)
    private const val MANIFEST_ENTRY_NAME = "meta/manifest.v1"
    private const val MAX_ENTRY_NAME_BYTES = 2048
    private const val MAX_ENTRY_PAYLOAD_BYTES = 2L * 1024L * 1024L * 1024L

    private data class OutgoingEntry(
        val name: String,
        val file: File? = null,
        val bytes: ByteArray? = null
    ) {
        val size: Long
            get() = bytes?.size?.toLong() ?: file?.length() ?: 0L
    }

    private data class ManifestTrack(
        val id: String,
        val title: String,
        val artist: String,
        val durationMs: Long,
        val addedAt: Long,
        val isFavorite: Boolean,
        val songEntry: String,
        val artworkEntry: String?
    )

    private data class ManifestPlaylist(
        val id: String,
        val name: String,
        val trackIds: List<String>,
        val createdAt: Long,
        val coverEntry: String?
    )

    @Throws(Exception::class)
    fun exportToStream(
        output: OutputStream,
        tracks: List<TrackItem>,
        playlists: List<PlaylistEntry>,
        settings: SonoraBackupSettings?
    ) {
        val entries = mutableListOf<OutgoingEntry>()
        val tracksArray = JSONArray()
        val playlistsArray = JSONArray()
        val favoritesArray = JSONArray()
        val usedTrackIds = mutableSetOf<String>()
        val usedPlaylistIds = mutableSetOf<String>()

        for (track in tracks) {
            val audioFile = File(track.filePath)
            if (!audioFile.exists() || !audioFile.isFile) {
                continue
            }

            val backupTrackId = uniqueId(
                preferred = track.id.ifBlank { "track_${System.currentTimeMillis()}" },
                used = usedTrackIds
            )
            val songEntry = "songs/${sanitizeToken(backupTrackId)}.${sanitizeExtension(audioFile.extension)}"
            entries += OutgoingEntry(name = songEntry, file = audioFile)

            val artworkEntry = track.artworkPath
                ?.takeIf { it.isNotBlank() }
                ?.let(::File)
                ?.takeIf { it.exists() && it.isFile }
                ?.let { artworkFile ->
                    val entryName = "artwork/${sanitizeToken(backupTrackId)}.${sanitizeExtension(artworkFile.extension)}"
                    entries += OutgoingEntry(name = entryName, file = artworkFile)
                    entryName
                }

            val item = JSONObject()
            item.put("id", backupTrackId)
            item.put("title", track.title)
            item.put("artist", track.artist)
            item.put("durationMs", track.durationMs.coerceAtLeast(0L))
            item.put("addedAt", track.addedAt.coerceAtLeast(0L))
            item.put("isFavorite", track.isFavorite)
            item.put("songEntry", songEntry)
            if (!artworkEntry.isNullOrBlank()) {
                item.put("artworkEntry", artworkEntry)
            }
            tracksArray.put(item)

            if (track.isFavorite) {
                favoritesArray.put(backupTrackId)
            }
        }

        for (playlist in playlists) {
            val backupPlaylistId = uniqueId(
                preferred = playlist.id.ifBlank { "playlist_${System.currentTimeMillis()}" },
                used = usedPlaylistIds
            )
            val coverEntry = playlist.customCoverPath
                ?.takeIf { it.isNotBlank() }
                ?.let(::File)
                ?.takeIf { it.exists() && it.isFile }
                ?.let { coverFile ->
                    val entryName = "playlist_covers/${sanitizeToken(backupPlaylistId)}.${sanitizeExtension(coverFile.extension)}"
                    entries += OutgoingEntry(name = entryName, file = coverFile)
                    entryName
                }

            val item = JSONObject()
            item.put("id", backupPlaylistId)
            item.put("name", playlist.name)
            item.put("createdAt", playlist.createdAt.coerceAtLeast(0L))
            item.put("trackIds", JSONArray(playlist.trackIds.filter { it.isNotBlank() }))
            if (!coverEntry.isNullOrBlank()) {
                item.put("coverEntry", coverEntry)
            }
            playlistsArray.put(item)
        }

        val manifest = JSONObject()
        manifest.put("format", "sonora-archive")
        manifest.put("version", ARCHIVE_VERSION)
        manifest.put("exportedAt", System.currentTimeMillis())
        manifest.put("tracks", tracksArray)
        manifest.put("playlists", playlistsArray)
        manifest.put("favorites", favoritesArray)
        if (settings != null) {
            val settingsObject = JSONObject()
            settingsObject.put("sliderStyle", settings.sliderStyle)
            settingsObject.put("artworkStyle", settings.artworkStyle)
            settingsObject.put("fontStyle", settings.fontStyle)
            settingsObject.put("accentHex", settings.accentHex)
            settingsObject.put("preservePlayerModes", settings.preservePlayerModes)
            settingsObject.put("trackGapSeconds", settings.trackGapSeconds.toDouble())
            settingsObject.put("maxStorageMb", settings.maxStorageMb)
            manifest.put("settings", settingsObject)
        }

        entries.add(
            index = 0,
            element = OutgoingEntry(
                name = MANIFEST_ENTRY_NAME,
                bytes = manifest.toString().toByteArray(StandardCharsets.UTF_8)
            )
        )

        DataOutputStream(BufferedOutputStream(output)).use { stream ->
            stream.write(ARCHIVE_MAGIC)
            stream.writeInt(ARCHIVE_VERSION)
            stream.writeInt(entries.size)

            for (entry in entries) {
                val nameBytes = entry.name.toByteArray(StandardCharsets.UTF_8)
                stream.writeInt(nameBytes.size)
                stream.write(nameBytes)
                stream.writeLong(entry.size)

                val payload = entry.bytes
                if (payload != null) {
                    stream.write(payload)
                } else {
                    val source = entry.file ?: continue
                    source.inputStream().use { inputFile ->
                        inputFile.copyTo(stream)
                    }
                }
            }
            stream.flush()
        }
    }

    @Throws(Exception::class)
    fun importFromStream(context: Context, input: InputStream): SonoraBackupRestoreResult {
        DataInputStream(BufferedInputStream(input)).use { stream ->
            val magic = readExactBytes(stream, ARCHIVE_MAGIC.size)
            if (!magic.contentEquals(ARCHIVE_MAGIC)) {
                throw IllegalArgumentException("Invalid backup archive header")
            }

            val archiveVersion = stream.readInt()
            if (archiveVersion != ARCHIVE_VERSION) {
                throw IllegalArgumentException("Unsupported backup archive version")
            }

            val entryCount = stream.readInt()
            if (entryCount <= 0) {
                throw IllegalArgumentException("Backup archive has no entries")
            }

            val manifestHeader = readEntryHeader(stream)
            if (manifestHeader.name != MANIFEST_ENTRY_NAME) {
                throw IllegalArgumentException("Backup archive is missing manifest")
            }
            val manifestBytes = readPayload(stream, manifestHeader.payloadSize)
            val manifest = JSONObject(String(manifestBytes, StandardCharsets.UTF_8))

            val manifestTracks = parseManifestTracks(manifest.optJSONArray("tracks"))
            val manifestPlaylists = parseManifestPlaylists(manifest.optJSONArray("playlists"))
            val favoriteTrackIds = parseStringArray(manifest.optJSONArray("favorites")).toSet()
            val settings = parseSettings(manifest.optJSONObject("settings"))

            val trackBySongEntry = manifestTracks.associateBy { it.songEntry }
            val trackIdByArtworkEntry = buildMap<String, String> {
                manifestTracks.forEach { track ->
                    val artworkEntry = track.artworkEntry
                    if (!artworkEntry.isNullOrBlank()) {
                        put(artworkEntry, track.id)
                    }
                }
            }
            val playlistIdByCoverEntry = buildMap<String, String> {
                manifestPlaylists.forEach { playlist ->
                    val coverEntry = playlist.coverEntry
                    if (!coverEntry.isNullOrBlank()) {
                        put(coverEntry, playlist.id)
                    }
                }
            }

            val musicDir = File(context.filesDir, "Sonora").apply { mkdirs() }
            val artworkDir = File(musicDir, "artwork").apply { mkdirs() }
            val playlistCoverDir = File(context.filesDir, "Sonora/playlist_covers").apply { mkdirs() }

            val audioPathByTrackId = mutableMapOf<String, String>()
            val artworkPathByTrackId = mutableMapOf<String, String>()
            val coverPathByPlaylistId = mutableMapOf<String, String>()

            for (entryIndex in 1 until entryCount) {
                val header = readEntryHeader(stream)
                val trackForAudio = trackBySongEntry[header.name]
                if (trackForAudio != null) {
                    val extension = extensionFromEntryName(header.name)
                    val destination = uniqueDestinationFile(
                        directory = musicDir,
                        prefix = "track",
                        extension = extension
                    )
                    destination.outputStream().use { outputFile ->
                        copyPayload(stream, outputFile, header.payloadSize)
                    }
                    audioPathByTrackId[trackForAudio.id] = destination.absolutePath
                    continue
                }

                val trackIdForArtwork = trackIdByArtworkEntry[header.name]
                if (!trackIdForArtwork.isNullOrBlank()) {
                    val extension = extensionFromEntryName(header.name)
                    val destination = uniqueDestinationFile(
                        directory = artworkDir,
                        prefix = "art",
                        extension = extension
                    )
                    destination.outputStream().use { outputFile ->
                        copyPayload(stream, outputFile, header.payloadSize)
                    }
                    artworkPathByTrackId[trackIdForArtwork] = destination.absolutePath
                    continue
                }

                val playlistIdForCover = playlistIdByCoverEntry[header.name]
                if (!playlistIdForCover.isNullOrBlank()) {
                    val extension = extensionFromEntryName(header.name)
                    val destination = uniqueDestinationFile(
                        directory = playlistCoverDir,
                        prefix = "pl",
                        extension = extension
                    )
                    destination.outputStream().use { outputFile ->
                        copyPayload(stream, outputFile, header.payloadSize)
                    }
                    coverPathByPlaylistId[playlistIdForCover] = destination.absolutePath
                    continue
                }

                skipPayload(stream, header.payloadSize)
            }

            val usedTrackIds = mutableSetOf<String>()
            val localTrackIdByBackupId = mutableMapOf<String, String>()
            val now = System.currentTimeMillis()
            val restoredTracks = buildList {
                for (track in manifestTracks) {
                    val filePath = audioPathByTrackId[track.id] ?: continue
                    val localTrackId = uniqueId(track.id, usedTrackIds)
                    localTrackIdByBackupId[track.id] = localTrackId
                    add(
                        TrackItem(
                            id = localTrackId,
                            title = track.title,
                            artist = track.artist,
                            durationMs = track.durationMs.coerceAtLeast(0L),
                            filePath = filePath,
                            artworkPath = artworkPathByTrackId[track.id],
                            addedAt = if (track.addedAt > 0L) track.addedAt else now,
                            isFavorite = track.isFavorite || favoriteTrackIds.contains(track.id)
                        )
                    )
                }
            }

            val usedPlaylistIds = mutableSetOf<String>()
            val restoredPlaylists = buildList {
                for (playlist in manifestPlaylists) {
                    val name = playlist.name.trim()
                    if (name.isBlank()) {
                        continue
                    }
                    val localTrackIds = playlist.trackIds.mapNotNull { localTrackIdByBackupId[it] }
                    add(
                        PlaylistEntry(
                            id = uniqueId(playlist.id, usedPlaylistIds),
                            name = name,
                            trackIds = localTrackIds.distinct(),
                            createdAt = playlist.createdAt.coerceAtLeast(0L),
                            customCoverPath = coverPathByPlaylistId[playlist.id]
                        )
                    )
                }
            }

            return SonoraBackupRestoreResult(
                tracks = restoredTracks,
                playlists = restoredPlaylists,
                settings = settings
            )
        }
    }

    private data class EntryHeader(
        val name: String,
        val payloadSize: Long
    )

    private fun parseManifestTracks(array: JSONArray?): List<ManifestTrack> {
        if (array == null) {
            return emptyList()
        }
        val usedIds = mutableSetOf<String>()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val rawId = item.optString("id").trim()
                val songEntry = item.optString("songEntry").trim()
                if (songEntry.isBlank()) {
                    continue
                }
                val id = uniqueId(
                    preferred = rawId.ifBlank { "track_${index}_${System.currentTimeMillis()}" },
                    used = usedIds
                )
                add(
                    ManifestTrack(
                        id = id,
                        title = item.optString("title"),
                        artist = item.optString("artist"),
                        durationMs = item.optLong("durationMs").coerceAtLeast(0L),
                        addedAt = item.optLong("addedAt").coerceAtLeast(0L),
                        isFavorite = item.optBoolean("isFavorite", false),
                        songEntry = songEntry,
                        artworkEntry = item.optString("artworkEntry").trim().ifBlank { null }
                    )
                )
            }
        }
    }

    private fun parseManifestPlaylists(array: JSONArray?): List<ManifestPlaylist> {
        if (array == null) {
            return emptyList()
        }
        val usedIds = mutableSetOf<String>()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val playlistName = item.optString("name").trim()
                if (playlistName.isBlank()) {
                    continue
                }
                val id = uniqueId(
                    preferred = item.optString("id").trim().ifBlank { "playlist_${index}_${System.currentTimeMillis()}" },
                    used = usedIds
                )
                add(
                    ManifestPlaylist(
                        id = id,
                        name = playlistName,
                        trackIds = parseStringArray(item.optJSONArray("trackIds")),
                        createdAt = item.optLong("createdAt").coerceAtLeast(0L),
                        coverEntry = item.optString("coverEntry").trim().ifBlank { null }
                    )
                )
            }
        }
    }

    private fun parseStringArray(array: JSONArray?): List<String> {
        if (array == null) {
            return emptyList()
        }
        return buildList {
            for (index in 0 until array.length()) {
                val value = array.optString(index).trim()
                if (value.isNotBlank()) {
                    add(value)
                }
            }
        }
    }

    private fun parseSettings(item: JSONObject?): SonoraBackupSettings? {
        if (item == null) {
            return null
        }
        return SonoraBackupSettings(
            sliderStyle = item.optString("sliderStyle").ifBlank { "wave" },
            artworkStyle = item.optString("artworkStyle").ifBlank { "square" },
            fontStyle = item.optString("fontStyle").ifBlank { "system" },
            accentHex = item.optString("accentHex").ifBlank { "#E6BE00" },
            preservePlayerModes = item.optBoolean("preservePlayerModes", true),
            trackGapSeconds = item.optDouble("trackGapSeconds", 0.0).toFloat(),
            maxStorageMb = item.optInt("maxStorageMb", -1)
        )
    }

    private fun uniqueDestinationFile(directory: File, prefix: String, extension: String): File {
        val safeExtension = sanitizeExtension(extension)
        while (true) {
            val name = "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.$safeExtension"
            val candidate = File(directory, name)
            if (!candidate.exists()) {
                return candidate
            }
        }
    }

    private fun readEntryHeader(input: DataInputStream): EntryHeader {
        val nameSize = input.readInt()
        if (nameSize <= 0 || nameSize > MAX_ENTRY_NAME_BYTES) {
            throw IllegalArgumentException("Invalid archive entry name size")
        }
        val name = String(readExactBytes(input, nameSize), StandardCharsets.UTF_8)
        val payloadSize = input.readLong()
        if (payloadSize < 0L || payloadSize > MAX_ENTRY_PAYLOAD_BYTES) {
            throw IllegalArgumentException("Invalid archive entry payload size")
        }
        return EntryHeader(name = name, payloadSize = payloadSize)
    }

    private fun readPayload(input: DataInputStream, size: Long): ByteArray {
        if (size > Int.MAX_VALUE.toLong()) {
            throw IllegalArgumentException("Manifest payload is too large")
        }
        return readExactBytes(input, size.toInt())
    }

    private fun copyPayload(input: DataInputStream, output: OutputStream, size: Long) {
        var remaining = size
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (remaining > 0L) {
            val chunk = minOf(remaining, buffer.size.toLong()).toInt()
            val read = input.read(buffer, 0, chunk)
            if (read <= 0) {
                throw EOFException("Unexpected end of archive payload")
            }
            output.write(buffer, 0, read)
            remaining -= read.toLong()
        }
    }

    private fun skipPayload(input: DataInputStream, size: Long) {
        var remaining = size
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (remaining > 0L) {
            val chunk = minOf(remaining, buffer.size.toLong()).toInt()
            val read = input.read(buffer, 0, chunk)
            if (read <= 0) {
                throw EOFException("Unexpected end while skipping archive payload")
            }
            remaining -= read.toLong()
        }
    }

    private fun readExactBytes(input: DataInputStream, length: Int): ByteArray {
        val target = ByteArray(length)
        var offset = 0
        while (offset < length) {
            val read = input.read(target, offset, length - offset)
            if (read <= 0) {
                throw EOFException("Unexpected end of archive stream")
            }
            offset += read
        }
        return target
    }

    private fun extensionFromEntryName(entryName: String): String {
        val extension = entryName.substringAfterLast('.', "")
        return sanitizeExtension(extension)
    }

    private fun sanitizeToken(value: String): String {
        val sanitized = value.lowercase().replace(Regex("[^a-z0-9_\\-]"), "_")
        return sanitized.ifBlank { UUID.randomUUID().toString().replace("-", "") }
    }

    private fun sanitizeExtension(value: String): String {
        val sanitized = value.lowercase().replace(Regex("[^a-z0-9]"), "")
        return sanitized.ifBlank { "bin" }
    }

    private fun uniqueId(preferred: String, used: MutableSet<String>): String {
        var candidate = preferred.trim().ifBlank { UUID.randomUUID().toString() }
        if (used.add(candidate)) {
            return candidate
        }
        while (true) {
            candidate = UUID.randomUUID().toString()
            if (used.add(candidate)) {
                return candidate
            }
        }
    }
}
