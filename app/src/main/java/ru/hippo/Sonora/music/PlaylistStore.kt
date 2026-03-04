package ru.hippo.Sonora.music

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class PlaylistEntry(
    val id: String,
    val name: String,
    val trackIds: List<String>,
    val createdAt: Long,
    val customCoverPath: String?
)

class PlaylistStore(private val context: Context) {

    private val playlistsFile = File(context.filesDir, "sonora_playlists_v1.json")
    private val coverDir = File(context.filesDir, "Sonora/playlist_covers").apply { mkdirs() }

    fun loadPlaylists(): List<PlaylistEntry> {
        if (!playlistsFile.exists()) {
            return emptyList()
        }

        return try {
            val raw = playlistsFile.readText()
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val id = item.optString("id")
                    val name = item.optString("name")
                    if (id.isBlank() || name.isBlank()) {
                        continue
                    }

                    val trackArray = item.optJSONArray("trackIds") ?: JSONArray()
                    val trackIds = buildList {
                        for (trackIndex in 0 until trackArray.length()) {
                            val trackId = trackArray.optString(trackIndex)
                            if (trackId.isNotBlank()) {
                                add(trackId)
                            }
                        }
                    }
                    val customCoverPath = item.optString("customCoverPath")
                        .ifBlank { null }
                        ?.takeIf { File(it).exists() }

                    add(
                        PlaylistEntry(
                            id = id,
                            name = name,
                            trackIds = trackIds,
                            createdAt = item.optLong("createdAt"),
                            customCoverPath = customCoverPath
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun savePlaylists(playlists: List<PlaylistEntry>) {
        val array = JSONArray()
        for (playlist in playlists) {
            val item = JSONObject()
            item.put("id", playlist.id)
            item.put("name", playlist.name)
            item.put("createdAt", playlist.createdAt)
            item.put("customCoverPath", playlist.customCoverPath ?: "")

            val trackArray = JSONArray()
            playlist.trackIds.forEach(trackArray::put)
            item.put("trackIds", trackArray)

            array.put(item)
        }
        playlistsFile.writeText(array.toString())
    }

    fun createPlaylist(name: String): PlaylistEntry? {
        val normalized = name.trim()
        if (normalized.isBlank()) {
            return null
        }

        val current = loadPlaylists().toMutableList()
        val entry = PlaylistEntry(
            id = UUID.randomUUID().toString(),
            name = normalized,
            trackIds = emptyList(),
            createdAt = System.currentTimeMillis(),
            customCoverPath = null
        )
        current.add(0, entry)
        savePlaylists(current)
        return entry
    }

    fun renamePlaylist(playlistId: String, newName: String): Boolean {
        val normalized = newName.trim()
        if (playlistId.isBlank() || normalized.isBlank()) {
            return false
        }

        val current = loadPlaylists().toMutableList()
        val index = current.indexOfFirst { it.id == playlistId }
        if (index < 0) {
            return false
        }

        current[index] = current[index].copy(name = normalized)
        savePlaylists(current)
        return true
    }

    fun deletePlaylist(playlistId: String): Boolean {
        if (playlistId.isBlank()) {
            return false
        }

        val current = loadPlaylists().toMutableList()
        val removedPlaylist = current.firstOrNull { it.id == playlistId }
        val removed = current.removeAll { it.id == playlistId }
        if (!removed) {
            return false
        }

        removedPlaylist?.customCoverPath
            ?.takeIf { it.isNotBlank() }
            ?.let { path ->
                runCatching { File(path).delete() }
            }
        savePlaylists(current)
        return true
    }

    fun setCustomCover(playlistId: String, sourceUri: android.net.Uri): Boolean {
        if (playlistId.isBlank()) {
            return false
        }

        val current = loadPlaylists().toMutableList()
        val index = current.indexOfFirst { it.id == playlistId }
        if (index < 0) {
            return false
        }

        val destination = File(
            coverDir,
            "pl_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
        )
        val copied = runCatching {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            } != null
        }.getOrDefault(false)
        if (!copied || !destination.exists()) {
            runCatching { destination.delete() }
            return false
        }

        val previousPath = current[index].customCoverPath
        current[index] = current[index].copy(customCoverPath = destination.absolutePath)
        savePlaylists(current)

        if (!previousPath.isNullOrBlank() && previousPath != destination.absolutePath) {
            runCatching { File(previousPath).delete() }
        }

        return true
    }

    fun addTrackIds(playlistId: String, trackIds: List<String>): Boolean {
        if (playlistId.isBlank() || trackIds.isEmpty()) {
            return false
        }

        val uniqueIds = trackIds.filter { it.isNotBlank() }.distinct()
        if (uniqueIds.isEmpty()) {
            return false
        }

        val current = loadPlaylists().toMutableList()
        val index = current.indexOfFirst { it.id == playlistId }
        if (index < 0) {
            return false
        }

        val merged = (current[index].trackIds + uniqueIds).distinct()
        current[index] = current[index].copy(trackIds = merged)
        savePlaylists(current)
        return true
    }

    fun replaceTrackIds(playlistId: String, trackIds: List<String>): Boolean {
        if (playlistId.isBlank()) {
            return false
        }

        val normalized = trackIds.filter { it.isNotBlank() }.distinct()
        val current = loadPlaylists().toMutableList()
        val index = current.indexOfFirst { it.id == playlistId }
        if (index < 0) {
            return false
        }

        current[index] = current[index].copy(trackIds = normalized)
        savePlaylists(current)
        return true
    }

    fun removeTrackIdFromAllPlaylists(trackId: String): Boolean {
        if (trackId.isBlank()) {
            return false
        }

        val current = loadPlaylists().toMutableList()
        var changed = false

        for (index in current.indices) {
            val original = current[index]
            val updatedTrackIds = original.trackIds.filterNot { it == trackId }
            if (updatedTrackIds.size != original.trackIds.size) {
                current[index] = original.copy(trackIds = updatedTrackIds)
                changed = true
            }
        }

        if (!changed) {
            return false
        }

        savePlaylists(current)
        return true
    }

    fun removeTrackId(playlistId: String, trackId: String): Boolean {
        if (playlistId.isBlank() || trackId.isBlank()) {
            return false
        }

        val current = loadPlaylists().toMutableList()
        val index = current.indexOfFirst { it.id == playlistId }
        if (index < 0) {
            return false
        }

        val original = current[index]
        val updatedTrackIds = original.trackIds.filterNot { it == trackId }
        if (updatedTrackIds.size == original.trackIds.size) {
            return false
        }

        current[index] = original.copy(trackIds = updatedTrackIds)
        savePlaylists(current)
        return true
    }
}
