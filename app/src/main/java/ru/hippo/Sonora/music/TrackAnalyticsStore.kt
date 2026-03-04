package ru.hippo.Sonora.music

import android.content.Context
import org.json.JSONObject
import java.io.File

data class TrackAnalytics(
    val playCount: Int,
    val skipCount: Int
) {
    val score: Double
        get() = playCount.toDouble() / (playCount.toDouble() + skipCount.toDouble() + 1.0)
}

class TrackAnalyticsStore(private val context: Context) {

    private val analyticsFile = File(context.filesDir, "sonora_track_analytics_v1.json")

    fun recordPlay(trackId: String) {
        if (trackId.isBlank()) {
            return
        }

        val map = loadMutableMap()
        val current = map[trackId] ?: TrackAnalytics(playCount = 0, skipCount = 0)
        map[trackId] = current.copy(playCount = current.playCount + 1)
        saveMap(map)
    }

    fun recordSkip(trackId: String) {
        if (trackId.isBlank()) {
            return
        }

        val map = loadMutableMap()
        val current = map[trackId] ?: TrackAnalytics(playCount = 0, skipCount = 0)
        map[trackId] = current.copy(skipCount = current.skipCount + 1)
        saveMap(map)
    }

    fun analyticsByTrackIDs(trackIds: List<String>): Map<String, TrackAnalytics> {
        if (trackIds.isEmpty()) {
            return emptyMap()
        }

        val source = loadMutableMap()
        val result = LinkedHashMap<String, TrackAnalytics>()
        trackIds.distinct().forEach { trackId ->
            val analytics = source[trackId] ?: TrackAnalytics(playCount = 0, skipCount = 0)
            result[trackId] = analytics
        }
        return result
    }

    private fun loadMutableMap(): MutableMap<String, TrackAnalytics> {
        if (!analyticsFile.exists()) {
            return LinkedHashMap()
        }

        return try {
            val root = JSONObject(analyticsFile.readText())
            val map = LinkedHashMap<String, TrackAnalytics>()
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = root.optJSONObject(key) ?: continue
                map[key] = TrackAnalytics(
                    playCount = value.optInt("playCount", 0),
                    skipCount = value.optInt("skipCount", 0)
                )
            }
            map
        } catch (_: Exception) {
            LinkedHashMap()
        }
    }

    private fun saveMap(map: Map<String, TrackAnalytics>) {
        val root = JSONObject()
        map.forEach { (trackId, analytics) ->
            val value = JSONObject()
            value.put("playCount", analytics.playCount)
            value.put("skipCount", analytics.skipCount)
            root.put(trackId, value)
        }
        analyticsFile.writeText(root.toString())
    }
}
