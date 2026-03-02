package ru.hippo.M2.music

import android.content.Context
import java.io.File
import org.json.JSONArray

class PlaybackHistoryStore(private val context: Context) {

    private val historyFile = File(context.filesDir, "m2_playback_history_v1.json")
    private val maxEntries = 160

    fun recordTrack(trackId: String) {
        if (trackId.isBlank()) {
            return
        }

        val ids = loadMutableIds()
        ids.remove(trackId)
        ids.add(0, trackId)
        if (ids.size > maxEntries) {
            ids.subList(maxEntries, ids.size).clear()
        }
        saveIds(ids)
    }

    fun recentTrackIds(limit: Int = 120): List<String> {
        val ids = loadMutableIds()
        if (limit <= 0 || ids.isEmpty()) {
            return ids
        }
        return ids.take(limit)
    }

    private fun loadMutableIds(): MutableList<String> {
        if (!historyFile.exists()) {
            return mutableListOf()
        }

        return try {
            val raw = historyFile.readText()
            val array = JSONArray(raw)
            val result = mutableListOf<String>()
            val seen = hashSetOf<String>()
            for (index in 0 until array.length()) {
                val value = array.optString(index, "")
                if (value.isBlank() || seen.contains(value)) {
                    continue
                }
                seen.add(value)
                result.add(value)
                if (result.size >= maxEntries) {
                    break
                }
            }
            result
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    private fun saveIds(ids: List<String>) {
        val array = JSONArray()
        ids.forEach { trackId ->
            array.put(trackId)
        }
        historyFile.writeText(array.toString())
    }
}
