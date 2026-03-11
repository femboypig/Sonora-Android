package ru.hippo.Sonora

import android.Manifest
import android.app.DownloadManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.getValue
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.nio.charset.StandardCharsets
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import ru.hippo.Sonora.music.MiniStreamingArtist
import ru.hippo.Sonora.music.MiniStreamingClient
import ru.hippo.Sonora.music.MiniStreamingDownloadPayload
import ru.hippo.Sonora.music.MiniStreamingTrack
import ru.hippo.Sonora.music.PlaybackController
import ru.hippo.Sonora.music.PlaybackHistoryStore
import ru.hippo.Sonora.music.PlaylistStore
import ru.hippo.Sonora.music.RepeatMode
import ru.hippo.Sonora.music.SharedPlaylistEntry
import ru.hippo.Sonora.music.SharedPlaylistStore
import ru.hippo.Sonora.music.SharedPlaylistTrackEntry
import ru.hippo.Sonora.music.SonoraBackupArchive
import ru.hippo.Sonora.music.SonoraBackupSettings
import ru.hippo.Sonora.music.TrackAnalytics
import ru.hippo.Sonora.music.TrackAnalyticsStore
import ru.hippo.Sonora.music.TrackItem
import ru.hippo.Sonora.music.TrackStore
import ru.hippo.Sonora.ui.theme.SonoraMiniPlayerBorderDark
import ru.hippo.Sonora.ui.theme.SonoraMiniPlayerBorderLight
import ru.hippo.Sonora.ui.theme.SonoraMiniPlayerDark
import ru.hippo.Sonora.ui.theme.SonoraMiniPlayerLight
import ru.hippo.Sonora.ui.theme.SonoraTabBarDark
import ru.hippo.Sonora.ui.theme.SonoraTabBarLight
import ru.hippo.Sonora.ui.theme.SonoraTabInactiveDark
import ru.hippo.Sonora.ui.theme.SonoraTabInactiveLight
import ru.hippo.Sonora.ui.theme.SonoraTheme

class MainActivity : ComponentActivity() {
    private val incomingSharedPlaylistUrlState = mutableStateOf<String?>(null)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        incomingSharedPlaylistUrlState.value = intent?.dataString

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            SonoraTheme(dynamicColor = false) {
                SonoraApp(incomingSharedPlaylistUrlState)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingSharedPlaylistUrlState.value = intent.dataString
    }
}

private enum class SonoraTab {
    Home,
    Music,
    Playlists
}

private enum class PlaylistCreateStep {
    Name,
    Tracks
}

private enum class TrackSelectionContext {
    Music,
    Favorites,
    PlaylistDetail
}

private enum class PlayerSliderStyle(val storageValue: String, val label: String) {
    Line("line", "Line"),
    Wave("wave", "Wave");

    companion object {
        fun fromStorage(value: String?): PlayerSliderStyle {
            return values().firstOrNull { it.storageValue == value } ?: Wave
        }
    }
}

private enum class ArtworkStyle(
    val storageValue: String,
    val label: String
) {
    Square("square", "Square"),
    Rounded("rounded", "Rounded");

    companion object {
        fun fromStorage(value: String?): ArtworkStyle {
            return values().firstOrNull { it.storageValue == value } ?: Rounded
        }
    }
}

private enum class AppFontStyle(val storageValue: String, val label: String) {
    System("system", "System"),
    Serif("serif", "Noto Serif");

    companion object {
        fun fromStorage(value: String?): AppFontStyle {
            return values().firstOrNull { it.storageValue == value } ?: System
        }
    }
}

private enum class MyWaveLook(val storageValue: String, val label: String) {
    Clouds("clouds", "Clouds"),
    Contours("contours", "Contours");

    companion object {
        fun fromStorage(value: String?): MyWaveLook {
            return values().firstOrNull { it.storageValue == value } ?: Contours
        }
    }
}

private data class SonoraAppSettings(
    val sliderStyle: PlayerSliderStyle = PlayerSliderStyle.Wave,
    val artworkStyle: ArtworkStyle = ArtworkStyle.Square,
    val fontStyle: AppFontStyle = AppFontStyle.System,
    val myWaveLook: MyWaveLook = MyWaveLook.Contours,
    val accentHex: String = DEFAULT_ACCENT_HEX,
    val preservePlayerModes: Boolean = true,
    val trackGapSeconds: Float = 0f,
    val maxStorageMb: Int = -1,
    val cacheOnlinePlaylistTracks: Boolean = false,
    val onlinePlaylistCacheMaxMb: Int = 1024
)

data class AndroidAppUpdateRelease(
    val id: String,
    val title: String,
    val versionName: String,
    val versionCode: Long,
    val coverUrl: String?,
    val downloadUrl: String,
    val notes: List<String>
)

private data class AndroidAppUpdateState(
    val checking: Boolean = false,
    val latestRelease: AndroidAppUpdateRelease? = null,
    val updateAvailable: Boolean = false,
    val statusMessage: String? = null,
    val downloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadId: Long? = null,
    val downloadedApkPath: String? = null,
    val downloadReady: Boolean = false
)

data class AndroidAppUpdateDownloadSnapshot(
    val release: AndroidAppUpdateRelease,
    val downloadId: Long,
    val apkPath: String,
    val status: Int,
    val downloadedBytes: Long,
    val totalBytes: Long
)

private data class PlaybackSessionSnapshot(
    val queueTrackIds: List<String>,
    val currentTrackId: String,
    val positionMs: Long,
    val isPlaying: Boolean,
    val shuffleEnabled: Boolean,
    val repeatMode: RepeatMode
)

private class SonoraSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("sonora_settings_v1", Context.MODE_PRIVATE)

    fun load(): SonoraAppSettings {
        val storedAccentHex = normalizeHexColor(prefs.getString(KEY_ACCENT_HEX, null))
        val accentHex = storedAccentHex ?: run {
            if (prefs.contains(KEY_ACCENT_HUE)) {
                formatColorHex(resolveLegacyAccentColorFromHue(prefs.getFloat(KEY_ACCENT_HUE, 48f)))
            } else {
                val legacyAccentHue = legacyAccentHueFromToken(prefs.getString(KEY_ACCENT_COLOR_LEGACY, null))
                if (legacyAccentHue != null) {
                    formatColorHex(resolveLegacyAccentColorFromHue(legacyAccentHue))
                } else {
                    DEFAULT_ACCENT_HEX
                }
            }
        }
        return SonoraAppSettings(
            sliderStyle = PlayerSliderStyle.fromStorage(prefs.getString(KEY_SLIDER_STYLE, PlayerSliderStyle.Wave.storageValue)),
            artworkStyle = ArtworkStyle.fromStorage(prefs.getString(KEY_ARTWORK_STYLE, ArtworkStyle.Square.storageValue)),
            fontStyle = AppFontStyle.fromStorage(prefs.getString(KEY_FONT_STYLE, AppFontStyle.System.storageValue)),
            myWaveLook = MyWaveLook.fromStorage(prefs.getString(KEY_MY_WAVE_LOOK, MyWaveLook.Contours.storageValue)),
            accentHex = accentHex,
            preservePlayerModes = prefs.getBoolean(KEY_PRESERVE_PLAYER_MODES, true),
            trackGapSeconds = nearestTrackGapSecondsOption(prefs.getFloat(KEY_TRACK_GAP, 0f)),
            maxStorageMb = nearestMaxStorageOptionMb(prefs.getInt(KEY_MAX_STORAGE_MB, -1)),
            cacheOnlinePlaylistTracks = prefs.getBoolean(KEY_CACHE_ONLINE_PLAYLIST_TRACKS, false),
            onlinePlaylistCacheMaxMb = nearestMaxStorageOptionMb(prefs.getInt(KEY_ONLINE_PLAYLIST_CACHE_MAX_MB, 1024))
        )
    }

    fun save(settings: SonoraAppSettings) {
        prefs.edit()
            .putString(KEY_SLIDER_STYLE, settings.sliderStyle.storageValue)
            .putString(KEY_ARTWORK_STYLE, settings.artworkStyle.storageValue)
            .putString(KEY_FONT_STYLE, settings.fontStyle.storageValue)
            .putString(KEY_MY_WAVE_LOOK, settings.myWaveLook.storageValue)
            .putString(KEY_ACCENT_HEX, normalizeHexColor(settings.accentHex) ?: DEFAULT_ACCENT_HEX)
            .putBoolean(KEY_PRESERVE_PLAYER_MODES, settings.preservePlayerModes)
            .putFloat(KEY_TRACK_GAP, nearestTrackGapSecondsOption(settings.trackGapSeconds))
            .putInt(KEY_MAX_STORAGE_MB, nearestMaxStorageOptionMb(settings.maxStorageMb))
            .putBoolean(KEY_CACHE_ONLINE_PLAYLIST_TRACKS, settings.cacheOnlinePlaylistTracks)
            .putInt(KEY_ONLINE_PLAYLIST_CACHE_MAX_MB, nearestMaxStorageOptionMb(settings.onlinePlaylistCacheMaxMb))
            .apply()
    }

    fun loadPlaybackSessionSnapshot(): PlaybackSessionSnapshot? {
        val queueJson = prefs.getString(KEY_SESSION_QUEUE_JSON, null) ?: return null
        val queueTrackIds = runCatching {
            val jsonArray = JSONArray(queueJson)
            buildList(jsonArray.length()) {
                for (index in 0 until jsonArray.length()) {
                    val value = jsonArray.optString(index, "")
                    if (value.isNotBlank()) {
                        add(value)
                    }
                }
            }
        }.getOrDefault(emptyList())
        if (queueTrackIds.isEmpty()) {
            return null
        }

        val currentTrackId = prefs.getString(KEY_SESSION_TRACK_ID, null)?.takeIf { it.isNotBlank() } ?: return null
        val positionMs = prefs.getLong(KEY_SESSION_POSITION_MS, 0L).coerceAtLeast(0L)
        val isPlaying = prefs.getBoolean(KEY_SESSION_IS_PLAYING, false)
        val shuffleEnabled = prefs.getBoolean(KEY_SESSION_SHUFFLE, false)
        val repeatMode = runCatching {
            RepeatMode.valueOf(prefs.getString(KEY_SESSION_REPEAT_MODE, RepeatMode.None.name) ?: RepeatMode.None.name)
        }.getOrDefault(RepeatMode.None)

        return PlaybackSessionSnapshot(
            queueTrackIds = queueTrackIds,
            currentTrackId = currentTrackId,
            positionMs = positionMs,
            isPlaying = isPlaying,
            shuffleEnabled = shuffleEnabled,
            repeatMode = repeatMode
        )
    }

    fun savePlaybackSessionSnapshot(snapshot: PlaybackSessionSnapshot) {
        val queueJson = JSONArray(snapshot.queueTrackIds).toString()
        prefs.edit()
            .putString(KEY_SESSION_QUEUE_JSON, queueJson)
            .putString(KEY_SESSION_TRACK_ID, snapshot.currentTrackId)
            .putLong(KEY_SESSION_POSITION_MS, snapshot.positionMs.coerceAtLeast(0L))
            .putBoolean(KEY_SESSION_IS_PLAYING, snapshot.isPlaying)
            .putBoolean(KEY_SESSION_SHUFFLE, snapshot.shuffleEnabled)
            .putString(KEY_SESSION_REPEAT_MODE, snapshot.repeatMode.name)
            .apply()
    }

    fun clearPlaybackSessionSnapshot() {
        prefs.edit()
            .remove(KEY_SESSION_QUEUE_JSON)
            .remove(KEY_SESSION_TRACK_ID)
            .remove(KEY_SESSION_POSITION_MS)
            .remove(KEY_SESSION_IS_PLAYING)
            .remove(KEY_SESSION_SHUFFLE)
            .remove(KEY_SESSION_REPEAT_MODE)
            .apply()
    }

    fun loadMiniStreamingInstalledTrackMap(): Map<String, String> {
        val raw = prefs.getString(KEY_MINI_STREAMING_TRACK_MAP_JSON, null)
            ?.takeIf { it.isNotBlank() }
            ?: return emptyMap()
        return runCatching {
            val json = JSONObject(raw)
            buildMap {
                val keys = json.keys()
                while (keys.hasNext()) {
                    val trackId = keys.next()?.trim().orEmpty()
                    val localTrackId = json.optString(trackId).trim()
                    if (trackId.isNotBlank() && localTrackId.isNotBlank()) {
                        put(trackId, localTrackId)
                    }
                }
            }
        }.getOrDefault(emptyMap())
    }

    fun saveMiniStreamingInstalledTrackMap(map: Map<String, String>) {
        val json = JSONObject()
        map.forEach { (trackId, localTrackId) ->
            if (trackId.isNotBlank() && localTrackId.isNotBlank()) {
                json.put(trackId, localTrackId)
            }
        }
        prefs.edit()
            .putString(KEY_MINI_STREAMING_TRACK_MAP_JSON, json.toString())
            .apply()
    }

    private companion object {
        const val KEY_SLIDER_STYLE = "player_slider_style"
        const val KEY_ARTWORK_STYLE = "artwork_style"
        const val KEY_FONT_STYLE = "font_style"
        const val KEY_MY_WAVE_LOOK = "my_wave_look"
        const val KEY_ACCENT_HEX = "accent_hex"
        const val KEY_ACCENT_HUE = "accent_hue"
        const val KEY_ACCENT_COLOR_LEGACY = "accent_color"
        const val KEY_PRESERVE_PLAYER_MODES = "preserve_player_modes"
        const val KEY_TRACK_GAP = "track_gap_seconds"
        const val KEY_MAX_STORAGE_MB = "max_storage_mb"
        const val KEY_CACHE_ONLINE_PLAYLIST_TRACKS = "cache_online_playlist_tracks"
        const val KEY_ONLINE_PLAYLIST_CACHE_MAX_MB = "online_playlist_cache_max_mb"
        const val KEY_SESSION_QUEUE_JSON = "playback_session_queue_json"
        const val KEY_SESSION_TRACK_ID = "playback_session_track_id"
        const val KEY_SESSION_POSITION_MS = "playback_session_position_ms"
        const val KEY_SESSION_IS_PLAYING = "playback_session_is_playing"
        const val KEY_SESSION_SHUFFLE = "playback_session_shuffle"
        const val KEY_SESSION_REPEAT_MODE = "playback_session_repeat_mode"
        const val KEY_MINI_STREAMING_TRACK_MAP_JSON = "mini_streaming_installed_track_map_json"
    }
}

private const val DEFAULT_ACCENT_HEX = "#E6BE00"
private const val MINI_STREAMING_PLAYBACK_PREFIX = "mini-streaming:"
private const val MINI_STREAMING_SEARCH_DEBOUNCE_MS = 280L
private const val MINI_STREAMING_TRACKS_SEARCH_LIMIT = 8
private const val MINI_STREAMING_ARTISTS_SEARCH_LIMIT = 10
private const val MINI_STREAMING_ARTIST_PAGE_SIZE = 60
private val TrackGapSecondsOptions = listOf(0f, 0.5f, 1f, 1.5f, 2f, 3f, 5f, 8f)
private val MaxStorageMbOptions = listOf(-1, 512, 1024, 2048, 3072, 4096, 6144, 8192)

private fun sanitizeSharedPlaylistEntry(entry: SharedPlaylistEntry): SharedPlaylistEntry {
    return entry.copy(
        cachedCoverPath = entry.cachedCoverPath?.takeIf { path ->
            path.isNotBlank() && java.io.File(path).exists()
        },
        tracks = entry.tracks.map { track ->
            track.copy(
                cachedArtworkPath = track.cachedArtworkPath?.takeIf { path ->
                    path.isNotBlank() && java.io.File(path).exists()
                },
                cachedFilePath = track.cachedFilePath?.takeIf { path ->
                    path.isNotBlank() && java.io.File(path).exists()
                }
            )
        }
    )
}

private fun sharedPlaylistSubtitle(entry: SharedPlaylistEntry, cacheAudioEnabled: Boolean): String {
    val total = entry.tracks.size
    if (!cacheAudioEnabled) {
        return "Online playlist • Streaming"
    }
    val cachedCount = entry.tracks.count { track ->
        val path = track.cachedFilePath
        !path.isNullOrBlank() && File(path).exists()
    }
    return when {
        total <= 0 -> "Online playlist"
        cachedCount >= total -> "Online playlist • Cached"
        cachedCount > 0 -> "Online playlist • Cached $cachedCount/$total"
        else -> "Online playlist • 0/$total cached"
    }
}

private fun buildBackupArchiveFileName(timestampMs: Long = System.currentTimeMillis()): String {
    val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    val suffix = formatter.format(Date(timestampMs))
    return "sonora_backup_${suffix}.sonoraarc"
}

private fun nearestTrackGapSecondsOption(value: Float): Float {
    val clamped = value.coerceIn(0f, 8f)
    return TrackGapSecondsOptions.minByOrNull { abs(it - clamped) } ?: 0f
}

private fun nearestMaxStorageOptionMb(value: Int): Int {
    if (value <= 0) {
        return -1
    }
    val clamped = value.coerceIn(256, 8192)
    return MaxStorageMbOptions.minByOrNull { abs(it - clamped) } ?: 2048
}

private fun nearestAccentHue(value: Float): Float {
    if (!value.isFinite()) {
        return 48f
    }
    return value.coerceIn(0f, 360f)
}

private fun legacyAccentHueFromToken(token: String?): Float? {
    return when (token?.lowercase()) {
        "yellow" -> 48f
        "sky" -> 208f
        "mint" -> 154f
        "coral" -> 12f
        else -> null
    }
}

private data class SonoraTabSpec(
    val tab: SonoraTab,
    val title: String,
    val iconRes: Int
)

private data class PlaylistUiItem(
    val id: String,
    val name: String,
    val trackIds: List<String>,
    val subtitle: String,
    val customCoverPath: String?,
    val isLovely: Boolean,
    val isUser: Boolean,
    val isSharedOnline: Boolean = false,
    val shareUrl: String? = null
)

private data class HomeAlbumUiItem(
    val artistKey: String,
    val title: String,
    val trackIds: List<String>,
    val coverTrackId: String?
)

private data class SearchArtistUiItem(
    val key: String,
    val title: String,
    val trackIds: List<String>
)

private data class SwipeTrackAction(
    val label: String,
    val backgroundColor: Color,
    val iconRes: Int,
    val onAction: () -> Unit,
    val fullSwipeEnabled: Boolean = false
)

private val SonoraAndroidYSMusicFontFamily = FontFamily(
    Font(R.font.ysmusic_headline_bold, FontWeight.Bold)
)

private val SonoraAndroidSFProSemiboldFamily = FontFamily(
    Font(R.font.sf_pro_text_semibold, FontWeight.SemiBold),
    Font(R.font.sf_pro_text_bold, FontWeight.Bold)
)

private val SonoraAndroidNotoSerifFamily = FontFamily(
    Font(R.font.noto_serif_regular, FontWeight.Normal),
    Font(R.font.noto_serif_medium, FontWeight.Medium),
    Font(R.font.noto_serif_bold, FontWeight.Bold)
)

private val SonoraAndroidHomeHeadingFontFamily = FontFamily(
    Font(R.font.tt_commons_pro_expanded_extrabold, FontWeight.ExtraBold)
)

private val LocalAccentColor = staticCompositionLocalOf {
    ru.hippo.Sonora.ui.theme.SonoraAccentYellow
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SonoraApp(incomingSharedPlaylistUrlState: MutableState<String?>) {
    val uiContext = androidx.compose.ui.platform.LocalContext.current
    val context = uiContext.applicationContext
    val trackStore = remember(context) { TrackStore(context) }
    val playlistStore = remember(context) { PlaylistStore(context) }
    val sharedPlaylistStore = remember(context) { SharedPlaylistStore(context) }
    val analyticsStore = remember(context) { TrackAnalyticsStore(context) }
    val playbackHistoryStore = remember(context) { PlaybackHistoryStore(context) }
    val settingsStore = remember(context) { SonoraSettingsStore(context) }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val appVersionLabel = remember(context) { resolveAppVersionLabel(context) }
    val appVersionCode = remember(context) { resolveAppVersionCode(context) }
    val githubProjectURL = remember { "https://github.com/femboypig/Sonora-Android" }
    val githubProjectLabel = remember { "femboypig/Sonora-Android" }
    val storageRootPath = remember(context) { formatStoragePathForAbout(context.filesDir.absolutePath) }
    val miniStreamingClient = remember(context) {
        MiniStreamingClient(
            backendBaseUrl = BuildConfig.BACKEND_BASE_URL.ifBlank { "https://api.corebrew.ru" }
        )
    }

    var tracks by remember { mutableStateOf(trackStore.loadTracks()) }
    var userPlaylists by remember { mutableStateOf(playlistStore.loadPlaylists()) }
    var likedSharedPlaylists by remember {
        mutableStateOf(sharedPlaylistStore.loadPlaylists().map(::sanitizeSharedPlaylistEntry))
    }
    var appSettings by remember { mutableStateOf(settingsStore.load()) }
    var storageUsageBytes by remember { mutableLongStateOf(0L) }
    var sharedPlaylistAudioCacheUsageBytes by remember {
        mutableLongStateOf(sharedPlaylistStore.audioCacheUsageBytes())
    }
    var miniStreamingInstalledTrackMap by remember {
        mutableStateOf(settingsStore.loadMiniStreamingInstalledTrackMap())
    }

    var selectedTab by rememberSaveable { mutableStateOf(SonoraTab.Home) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var showMusicPage by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var myMusicQuery by rememberSaveable { mutableStateOf("") }
    var favoritesQuery by rememberSaveable { mutableStateOf("") }
    var playlistsQuery by rememberSaveable { mutableStateOf("") }
    var showHistoryPage by rememberSaveable { mutableStateOf(false) }
    var showSettingsPage by rememberSaveable { mutableStateOf(false) }
    var showFavoritesPage by rememberSaveable { mutableStateOf(false) }
    var showPlaylistsListPage by rememberSaveable { mutableStateOf(false) }

    var openedPlaylistID by rememberSaveable { mutableStateOf<String?>(null) }
    var openedTransientSharedPlaylist by remember { mutableStateOf<SharedPlaylistEntry?>(null) }
    var openedHomeAlbumArtistKey by rememberSaveable { mutableStateOf<String?>(null) }
    var playlistCreateStep by rememberSaveable { mutableStateOf<PlaylistCreateStep?>(null) }
    var playlistCreateName by rememberSaveable { mutableStateOf("") }
    var playlistCreateQuery by rememberSaveable { mutableStateOf("") }
    var playlistCreateSelectedTrackIDs by remember { mutableStateOf(setOf<String>()) }
    var addTracksTargetPlaylistID by rememberSaveable { mutableStateOf<String?>(null) }
    var addTracksSelectedIDs by remember { mutableStateOf(setOf<String>()) }
    var playerVisible by rememberSaveable { mutableStateOf(false) }
    var trackSelectionContext by rememberSaveable { mutableStateOf<TrackSelectionContext?>(null) }
    var selectedTrackIDs by remember { mutableStateOf(setOf<String>()) }
    var showSleepTimerDialog by rememberSaveable { mutableStateOf(false) }
    var quickAddTrackID by rememberSaveable { mutableStateOf<String?>(null) }
    var showPlaylistOptionsDialog by rememberSaveable { mutableStateOf(false) }
    var showRenamePlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var renamePlaylistDraft by rememberSaveable { mutableStateOf("") }
    var coverTargetPlaylistID by rememberSaveable { mutableStateOf<String?>(null) }
    var homeVisitCount by rememberSaveable { mutableIntStateOf(1) }
    var homeRecommendationsSessionSeed by rememberSaveable {
        mutableIntStateOf((((System.currentTimeMillis() / 1000L) % 100_000L).toInt()).coerceAtLeast(1))
    }
    var wasHomeSelected by rememberSaveable { mutableStateOf(true) }
    var lastPresentedHomeHeroTrackID by rememberSaveable { mutableStateOf<String?>(null) }
    var miniStreamingTracks by remember { mutableStateOf<List<MiniStreamingTrack>>(emptyList()) }
    var miniStreamingArtists by remember { mutableStateOf<List<MiniStreamingArtist>>(emptyList()) }
    var miniStreamingTracksLoading by remember { mutableStateOf(false) }
    var miniStreamingArtistsLoading by remember { mutableStateOf(false) }
    var miniStreamingArtistsSectionVisible by remember { mutableStateOf(true) }
    var miniStreamingSearchToken by remember { mutableIntStateOf(0) }
    var miniStreamingResolvingTrackIds by remember { mutableStateOf(setOf<String>()) }
    var miniStreamingInstallingTrackIds by remember { mutableStateOf(setOf<String>()) }
    val miniStreamingInstallingJobs = remember { mutableMapOf<String, Job>() }
    val miniStreamingResolvingJobs = remember { mutableMapOf<String, Job>() }
    var miniStreamingPlaybackQueue by remember { mutableStateOf<List<MiniStreamingTrack>>(emptyList()) }
    var miniStreamingActiveTrackId by rememberSaveable { mutableStateOf<String?>(null) }
    var miniStreamingResolvedPayloadByTrackId by remember {
        mutableStateOf<Map<String, MiniStreamingDownloadPayload>>(emptyMap())
    }
    var miniStreamingPendingTrack by remember { mutableStateOf<TrackItem?>(null) }
    var openedMiniStreamingArtist by remember { mutableStateOf<MiniStreamingArtist?>(null) }
    var openedMiniStreamingArtistTracks by remember { mutableStateOf<List<MiniStreamingTrack>>(emptyList()) }
    var openedMiniStreamingArtistLoading by remember { mutableStateOf(false) }
    var openedMiniStreamingArtistLoadingMore by remember { mutableStateOf(false) }
    var openedMiniStreamingArtistHasMore by remember { mutableStateOf(false) }
    var openedMiniStreamingArtistRequestedLimit by remember { mutableIntStateOf(MINI_STREAMING_ARTIST_PAGE_SIZE) }
    var openedMiniStreamingArtistRequestToken by remember { mutableIntStateOf(0) }
    var sharedPlaylistOpening by remember { mutableStateOf(false) }
    var sharedPlaylistOpeningMessage by remember { mutableStateOf("Loading tracks from server...") }
    var sharedPlaylistUploading by remember { mutableStateOf(false) }
    var sharedPlaylistUploadingMessage by remember { mutableStateOf("Uploading tracks to server...") }
    var sharedPlaylistImporting by remember { mutableStateOf(false) }
    var sharedPlaylistImportingMessage by remember { mutableStateOf("Saving tracks to library...") }
    var androidAppUpdateState by remember { mutableStateOf(AndroidAppUpdateState()) }
    var lastShownCompletedAppUpdateDownloadId by rememberSaveable { mutableLongStateOf(-1L) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    fun mergeAndroidAppUpdateState(
        base: AndroidAppUpdateState,
        snapshot: AndroidAppUpdateDownloadSnapshot?
    ): AndroidAppUpdateState {
        if (snapshot == null) {
            return base.copy(
                downloading = false,
                downloadProgress = 0f,
                downloadId = null,
                downloadedApkPath = null,
                downloadReady = false
            )
        }
        val progress = if (snapshot.totalBytes > 0L) {
            (snapshot.downloadedBytes.toFloat() / snapshot.totalBytes.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
        val downloading = snapshot.status == DownloadManager.STATUS_RUNNING ||
            snapshot.status == DownloadManager.STATUS_PENDING ||
            snapshot.status == DownloadManager.STATUS_PAUSED
        val ready = snapshot.status == DownloadManager.STATUS_SUCCESSFUL &&
            snapshot.apkPath.isNotBlank() &&
            File(snapshot.apkPath).exists()
        val statusMessage = when {
            ready -> "Download complete. Return to Sonora and tap Install update."
            downloading && snapshot.totalBytes > 0L -> "Downloading ${(progress * 100f).roundToInt()}%"
            downloading -> "Downloading update..."
            snapshot.status == DownloadManager.STATUS_FAILED -> "Could not download update"
            else -> base.statusMessage
        }
        return base.copy(
            latestRelease = snapshot.release,
            updateAvailable = snapshot.release.versionCode > appVersionCode,
            statusMessage = statusMessage,
            downloading = downloading,
            downloadProgress = progress,
            downloadId = snapshot.downloadId,
            downloadedApkPath = snapshot.apkPath.takeIf { it.isNotBlank() },
            downloadReady = ready
        )
    }

    fun refreshAndroidAppUpdateDownloadState() {
        val snapshot = readAndroidAppUpdateDownloadSnapshot(context)
        androidAppUpdateState = mergeAndroidAppUpdateState(androidAppUpdateState, snapshot)
    }

    LaunchedEffect(Unit) {
        refreshAndroidAppUpdateDownloadState()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                refreshAndroidAppUpdateDownloadState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(androidAppUpdateState.downloadId, androidAppUpdateState.downloading) {
        val activeDownloadId = androidAppUpdateState.downloadId ?: return@LaunchedEffect
        if (!androidAppUpdateState.downloading) {
            return@LaunchedEffect
        }
        while (true) {
            val snapshot = withContext(Dispatchers.IO) {
                readAndroidAppUpdateDownloadSnapshot(context, activeDownloadId)
            }
            androidAppUpdateState = mergeAndroidAppUpdateState(androidAppUpdateState, snapshot)
            if (snapshot == null ||
                snapshot.status == DownloadManager.STATUS_SUCCESSFUL ||
                snapshot.status == DownloadManager.STATUS_FAILED
            ) {
                break
            }
            delay(500L)
        }
    }

    LaunchedEffect(androidAppUpdateState.downloadId, androidAppUpdateState.downloadReady) {
        val readyDownloadId = androidAppUpdateState.downloadId ?: return@LaunchedEffect
        if (!androidAppUpdateState.downloadReady || lastShownCompletedAppUpdateDownloadId == readyDownloadId) {
            return@LaunchedEffect
        }
        val snapshot = withContext(Dispatchers.IO) {
            readAndroidAppUpdateDownloadSnapshot(context, readyDownloadId)
        } ?: return@LaunchedEffect
        if (snapshot.status == DownloadManager.STATUS_SUCCESSFUL) {
            showAndroidAppUpdateReadyNotification(context, snapshot)
            lastShownCompletedAppUpdateDownloadId = readyDownloadId
        }
    }
    val applySharedPlaylistProgressUpdate: (SharedPlaylistEntry) -> Unit = { updatedEntry ->
        val sanitized = sanitizeSharedPlaylistEntry(updatedEntry)
        likedSharedPlaylists = buildList {
            var replaced = false
            likedSharedPlaylists.forEach { existing ->
                if (existing.remoteId == sanitized.remoteId) {
                    add(sanitized)
                    replaced = true
                } else {
                    add(existing)
                }
            }
            if (!replaced) {
                add(0, sanitized)
            }
        }
        if (openedTransientSharedPlaylist?.remoteId == sanitized.remoteId ||
            openedPlaylistID == sanitized.localId) {
            openedTransientSharedPlaylist = sanitized
        }
        sharedPlaylistAudioCacheUsageBytes = sharedPlaylistStore.audioCacheUsageBytes()
    }

    var analyticsVersion by remember { mutableIntStateOf(0) }
    var historyVersion by remember { mutableIntStateOf(0) }
    var playbackSessionRestored by rememberSaveable { mutableStateOf(false) }

    val playbackController = remember(analyticsStore, playbackHistoryStore, context) {
        PlaybackController(
            context = context,
            onTrackPlayed = { trackID ->
                analyticsStore.recordPlay(trackID)
                analyticsVersion += 1
                playbackHistoryStore.recordTrack(trackID)
                historyVersion += 1
            },
            onTrackSkipped = { trackID ->
                analyticsStore.recordSkip(trackID)
                analyticsVersion += 1
                val normalizedPlaybackId = trackID.trim()
                val miniTrackId = when {
                    normalizedPlaybackId.startsWith(MINI_STREAMING_PLAYBACK_PREFIX) -> {
                        normalizedPlaybackId.removePrefix(MINI_STREAMING_PLAYBACK_PREFIX).trim().ifBlank { null }
                    }
                    normalizedPlaybackId.isNotBlank() -> {
                        miniStreamingInstalledTrackMap.entries.firstOrNull { it.value == normalizedPlaybackId }?.key
                    }
                    else -> null
                }
                if (!miniTrackId.isNullOrBlank()) {
                    miniStreamingResolvingJobs[miniTrackId]?.cancel()
                    miniStreamingResolvingJobs.remove(miniTrackId)
                    miniStreamingResolvingTrackIds = miniStreamingResolvingTrackIds - miniTrackId
                    miniStreamingInstallingJobs[miniTrackId]?.cancel()
                    miniStreamingInstallingJobs.remove(miniTrackId)
                    miniStreamingInstallingTrackIds = miniStreamingInstallingTrackIds - miniTrackId
                    if (miniStreamingActiveTrackId == miniTrackId) {
                        miniStreamingActiveTrackId = null
                    }
                }
            }
        )
    }

    val homeListState = rememberLazyListState()
    val musicListState = rememberLazyListState()
    val playlistsListState = rememberLazyListState()
    val favoritesListState = rememberLazyListState()
    val historyListState = rememberLazyListState()
    val playlistsBrowseListState = rememberLazyListState()
    val playlistDetailListState = rememberLazyListState()

    LaunchedEffect(incomingSharedPlaylistUrlState.value) {
        val url = incomingSharedPlaylistUrlState.value
        if (url.isNullOrBlank()) {
            return@LaunchedEffect
        }
        val cachedSharedPlaylist = extractSharedPlaylistRemoteId(url)?.let { remoteId ->
            likedSharedPlaylists.firstOrNull { it.remoteId == remoteId }
        }
        sharedPlaylistOpeningMessage = "Loading tracks from server..."
        sharedPlaylistOpening = true
        val fetched = withContext(Dispatchers.IO) {
            fetchSharedPlaylistFromDeepLink(
                url,
                BuildConfig.BACKEND_BASE_URL.ifBlank { "https://api.corebrew.ru" },
                cachedSharedPlaylist
            )
        }
        sharedPlaylistOpening = false
        incomingSharedPlaylistUrlState.value = null
        if (fetched == null) {
            snackbarHostState.showSnackbar("Could not open shared playlist")
        } else {
            val hydrated = if (
                cachedSharedPlaylist != null &&
                fetched.remoteId == cachedSharedPlaylist.remoteId &&
                fetched.contentSha256.isNotBlank() &&
                fetched.contentSha256 != cachedSharedPlaylist.contentSha256
            ) {
                withContext(Dispatchers.IO) {
                    val fresh = fetched.copy(
                        cachedCoverPath = null,
                        tracks = fetched.tracks.map {
                            it.copy(
                                cachedArtworkPath = null,
                                cachedFilePath = null
                            )
                        }
                    )
                    sharedPlaylistStore.upsert(fresh)
                    sharedPlaylistStore.cacheAssets(
                        fresh,
                        cacheAudio = appSettings.cacheOnlinePlaylistTracks,
                        audioLimitBytes = if (appSettings.onlinePlaylistCacheMaxMb > 0) {
                            appSettings.onlinePlaylistCacheMaxMb.toLong() * 1_048_576L
                        } else {
                            Long.MAX_VALUE
                        },
                        forceRefresh = true,
                        onProgress = { partial ->
                            mainHandler.post {
                                applySharedPlaylistProgressUpdate(partial)
                            }
                        }
                    )
                }
            } else {
                fetched
            }
            if (cachedSharedPlaylist != null) {
                likedSharedPlaylists = sharedPlaylistStore.loadPlaylists().map(::sanitizeSharedPlaylistEntry)
                sharedPlaylistAudioCacheUsageBytes = sharedPlaylistStore.audioCacheUsageBytes()
            }
            openedTransientSharedPlaylist = hydrated
            openedPlaylistID = hydrated.localId
            showPlaylistsListPage = true
            selectedTab = SonoraTab.Playlists
        }
    }

    val density = LocalDensity.current
    val revealThreshold = remember(density) { with(density) { 62.dp.toPx() } }
    val dismissThreshold = remember(density) { with(density) { 40.dp.toPx().toInt() } }
    val compactPlaylistTitleThreshold = remember(density) { with(density) { 175.dp.toPx().toInt() } }
    var pullRevealDistance by remember { mutableFloatStateOf(0f) }
    var pullHideDistance by remember { mutableFloatStateOf(0f) }

    val persistPlaybackSession: () -> Unit = {
        val currentTrackId = playbackController.currentTrackId
        val queueTrackIds = playbackController.currentQueueTrackIds()
        if (currentTrackId.isNullOrBlank() || queueTrackIds.isEmpty()) {
            settingsStore.clearPlaybackSessionSnapshot()
        } else {
            val preserveModes = appSettings.preservePlayerModes
            settingsStore.savePlaybackSessionSnapshot(
                PlaybackSessionSnapshot(
                    queueTrackIds = queueTrackIds,
                    currentTrackId = currentTrackId,
                    positionMs = playbackController.currentPositionMs(),
                    isPlaying = playbackController.isPlaying,
                    shuffleEnabled = if (preserveModes) playbackController.isShuffleEnabled else false,
                    repeatMode = if (preserveModes) playbackController.repeatMode else RepeatMode.None
                )
            )
        }
    }

    LaunchedEffect(tracks) {
        storageUsageBytes = withContext(Dispatchers.IO) {
            computeAppStorageUsageBytes(context)
        }
    }

    LaunchedEffect(playbackController, appSettings.trackGapSeconds) {
        playbackController.applyAudioSettings(trackGapSeconds = appSettings.trackGapSeconds)
    }

    LaunchedEffect(playbackController, tracks, appSettings.preservePlayerModes) {
        if (playbackSessionRestored || tracks.isEmpty()) {
            return@LaunchedEffect
        }
        val snapshot = settingsStore.loadPlaybackSessionSnapshot()
        if (snapshot == null) {
            playbackSessionRestored = true
            return@LaunchedEffect
        }

        val trackById = tracks.associateBy { it.id }
        val resolvedQueue = snapshot.queueTrackIds.mapNotNull { trackById[it] }
        if (resolvedQueue.isEmpty()) {
            settingsStore.clearPlaybackSessionSnapshot()
            playbackSessionRestored = true
            return@LaunchedEffect
        }
        val resolvedTargetId = if (resolvedQueue.any { it.id == snapshot.currentTrackId }) {
            snapshot.currentTrackId
        } else {
            resolvedQueue.first().id
        }
        playbackController.restoreSession(
            queue = resolvedQueue,
            targetTrackId = resolvedTargetId,
            positionMs = snapshot.positionMs,
            shouldPlay = false,
            shuffleEnabled = if (appSettings.preservePlayerModes) snapshot.shuffleEnabled else false,
            repeatMode = if (appSettings.preservePlayerModes) snapshot.repeatMode else RepeatMode.None
        )
        playbackSessionRestored = true
    }

    LaunchedEffect(
        playbackSessionRestored,
        playbackController.currentTrackId,
        playbackController.queueCount,
        playbackController.isPlaying,
        playbackController.isShuffleEnabled,
        playbackController.repeatMode,
        appSettings.preservePlayerModes
    ) {
        if (!playbackSessionRestored) {
            return@LaunchedEffect
        }
        persistPlaybackSession()
    }

    LaunchedEffect(
        playbackSessionRestored,
        playbackController.currentTrackId,
        playbackController.isPlaying,
        appSettings.preservePlayerModes
    ) {
        if (!playbackSessionRestored || !playbackController.isPlaying || playbackController.currentTrackId == null) {
            return@LaunchedEffect
        }
        while (playbackController.isPlaying && playbackController.currentTrackId != null) {
            delay(1500L)
            persistPlaybackSession()
        }
    }

    LaunchedEffect(selectedTab) {
        showSearch = false
        pullRevealDistance = 0f
        pullHideDistance = 0f
        showHistoryPage = false
        showSettingsPage = false
        showFavoritesPage = false
        showPlaylistsListPage = false
        openedMiniStreamingArtist = null
        openedMiniStreamingArtistTracks = emptyList()
        openedMiniStreamingArtistLoading = false
        openedMiniStreamingArtistLoadingMore = false
        openedMiniStreamingArtistHasMore = false
        openedMiniStreamingArtistRequestedLimit = MINI_STREAMING_ARTIST_PAGE_SIZE
    }

    LaunchedEffect(
        showMusicPage,
        myMusicQuery,
        musicListState.firstVisibleItemIndex,
        musicListState.firstVisibleItemScrollOffset
    ) {
        if (!showMusicPage || myMusicQuery.isNotBlank()) {
            return@LaunchedEffect
        }
        val scrolledIntoContent = musicListState.firstVisibleItemIndex > 0 ||
            musicListState.firstVisibleItemScrollOffset > dismissThreshold
        if (scrolledIntoContent && showSearch) {
            showSearch = false
            pullRevealDistance = 0f
            pullHideDistance = 0f
        }
    }

    LaunchedEffect(playbackController.currentTrackId, miniStreamingPendingTrack?.id, playbackController.isPreparing) {
        val currentPlaybackId = playbackController.currentTrackId
        if (currentPlaybackId != null) {
            val pending = miniStreamingPendingTrack
            if (pending != null && pending.id == currentPlaybackId && !playbackController.isPreparing) {
                miniStreamingPendingTrack = null
            }
            val miniStreamingTrackId = if (currentPlaybackId.startsWith(MINI_STREAMING_PLAYBACK_PREFIX)) {
                currentPlaybackId.removePrefix(MINI_STREAMING_PLAYBACK_PREFIX).trim().ifBlank { null }
            } else {
                miniStreamingInstalledTrackMap.entries
                    .firstOrNull { (_, localTrackId) -> localTrackId == currentPlaybackId }
                    ?.key
            }
            if (!miniStreamingTrackId.isNullOrBlank()) {
                miniStreamingActiveTrackId = miniStreamingTrackId
            }
            return@LaunchedEffect
        }
        if (miniStreamingPendingTrack == null) {
            playerVisible = false
            miniStreamingActiveTrackId = null
        }
    }

    val nestedScrollConnection = remember(
        openedPlaylistID,
        openedHomeAlbumArtistKey,
        openedMiniStreamingArtist?.artistId,
        selectedTab,
        showMusicPage,
        showFavoritesPage,
        showPlaylistsListPage,
        showSearch,
        searchQuery,
        myMusicQuery,
        favoritesQuery,
        playlistsQuery,
        revealThreshold,
        dismissThreshold,
        homeListState,
        musicListState,
        favoritesListState,
        playlistsListState,
        playlistsBrowseListState,
        playlistDetailListState
    ) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput) {
                    return Offset.Zero
                }

                val inPlaylistDetail = openedPlaylistID != null ||
                    openedHomeAlbumArtistKey != null ||
                    openedMiniStreamingArtist != null
                if (selectedTab == SonoraTab.Home) {
                    if (showSearch) {
                        showSearch = false
                    }
                    pullRevealDistance = 0f
                    pullHideDistance = 0f
                    return Offset.Zero
                }
                val isCollectionsSubpage = showMusicPage || showFavoritesPage || showPlaylistsListPage
                if (selectedTab == SonoraTab.Playlists && !isCollectionsSubpage && !showSearch) {
                    // Keep root Collections clean: no pull-to-reveal there.
                    pullRevealDistance = 0f
                    pullHideDistance = 0f
                    return Offset.Zero
                }

                val atTop = when {
                    inPlaylistDetail -> playlistDetailListState.firstVisibleItemIndex == 0 &&
                        playlistDetailListState.firstVisibleItemScrollOffset == 0

                    showMusicPage -> musicListState.firstVisibleItemIndex == 0 &&
                        musicListState.firstVisibleItemScrollOffset == 0

                    showFavoritesPage -> favoritesListState.firstVisibleItemIndex == 0 &&
                        favoritesListState.firstVisibleItemScrollOffset == 0

                    showPlaylistsListPage -> playlistsBrowseListState.firstVisibleItemIndex == 0 &&
                        playlistsBrowseListState.firstVisibleItemScrollOffset == 0

                    selectedTab == SonoraTab.Home -> homeListState.firstVisibleItemIndex == 0 &&
                        homeListState.firstVisibleItemScrollOffset == 0

                    selectedTab == SonoraTab.Music -> musicListState.firstVisibleItemIndex == 0 &&
                        musicListState.firstVisibleItemScrollOffset == 0

                    selectedTab == SonoraTab.Playlists -> playlistsListState.firstVisibleItemIndex == 0 &&
                        playlistsListState.firstVisibleItemScrollOffset == 0

                    else -> false
                }

                val scrolledIntoContent = when {
                    inPlaylistDetail -> playlistDetailListState.firstVisibleItemIndex > 0 ||
                        playlistDetailListState.firstVisibleItemScrollOffset > dismissThreshold

                    showMusicPage -> musicListState.firstVisibleItemIndex > 0 ||
                        musicListState.firstVisibleItemScrollOffset > dismissThreshold

                    showFavoritesPage -> favoritesListState.firstVisibleItemIndex > 0 ||
                        favoritesListState.firstVisibleItemScrollOffset > dismissThreshold

                    showPlaylistsListPage -> playlistsBrowseListState.firstVisibleItemIndex > 0 ||
                        playlistsBrowseListState.firstVisibleItemScrollOffset > dismissThreshold

                    selectedTab == SonoraTab.Home -> homeListState.firstVisibleItemIndex > 0 ||
                        homeListState.firstVisibleItemScrollOffset > dismissThreshold

                    selectedTab == SonoraTab.Music -> musicListState.firstVisibleItemIndex > 0 ||
                        musicListState.firstVisibleItemScrollOffset > dismissThreshold

                    selectedTab == SonoraTab.Playlists -> playlistsListState.firstVisibleItemIndex > 0 ||
                        playlistsListState.firstVisibleItemScrollOffset > dismissThreshold

                    else -> false
                }

                val hasQuery = when {
                    showMusicPage -> myMusicQuery.isNotBlank()
                    showFavoritesPage -> favoritesQuery.isNotBlank()
                    showPlaylistsListPage -> playlistsQuery.isNotBlank()
                    selectedTab == SonoraTab.Home -> false
                    selectedTab == SonoraTab.Music -> searchQuery.isNotBlank()
                    else -> playlistsQuery.isNotBlank()
                }
                val pullRevealEnabled = inPlaylistDetail || isCollectionsSubpage

                if (showSearch && !hasQuery && scrolledIntoContent) {
                    showSearch = false
                }

                if (pullRevealEnabled && !showSearch && !hasQuery && atTop && available.y > 0f) {
                    pullRevealDistance += available.y
                    pullHideDistance = 0f
                    if (pullRevealDistance >= revealThreshold) {
                        showSearch = true
                        pullRevealDistance = 0f
                    }
                } else if (showSearch && !hasQuery && atTop && available.y < 0f) {
                    pullHideDistance += -available.y
                    pullRevealDistance = 0f
                    if (pullHideDistance >= dismissThreshold.toFloat()) {
                        showSearch = false
                        pullHideDistance = 0f
                    }
                } else if (!showSearch && available.y < 0f && atTop) {
                    pullRevealDistance = (pullRevealDistance + available.y).coerceAtLeast(0f)
                } else if (!atTop) {
                    pullRevealDistance = 0f
                    pullHideDistance = 0f
                } else if (available.y > 0f) {
                    pullHideDistance = 0f
                }

                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (!showSearch) {
                    pullRevealDistance = 0f
                } else {
                    pullHideDistance = 0f
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    val tabSpecs = listOf(
        SonoraTabSpec(SonoraTab.Music, "Search", R.drawable.ic_global_magnifyingglass),
        SonoraTabSpec(SonoraTab.Home, "Home", R.drawable.tab_home),
        SonoraTabSpec(SonoraTab.Playlists, "Collections", R.drawable.tab_lib)
    )

    val favoriteTracks = remember(tracks) { tracks.filter { it.isFavorite } }
    val favoritesFiltered = remember(favoriteTracks, favoritesQuery) {
        favoriteTracks.filter { track -> track.matchesQuery(favoritesQuery) }
    }
    val favoriteTrackIDs = remember(favoriteTracks) { favoriteTracks.map { it.id }.toSet() }
    val trackByID = remember(tracks) { tracks.associateBy { it.id } }

    val analyticsByID = remember(tracks, analyticsVersion) {
        analyticsStore.analyticsByTrackIDs(tracks.map { it.id })
    }

    val tracksByAffinity = remember(tracks, analyticsByID) {
        tracks.sortedWith(
            compareByDescending<TrackItem> { analyticsByID[it.id]?.score ?: 0.0 }
                .thenByDescending { analyticsByID[it.id]?.playCount ?: 0 }
                .thenBy { analyticsByID[it.id]?.skipCount ?: 0 }
                .thenBy { it.displayTitle().lowercase() }
        )
    }

    val lovelyTrackIDs = remember(tracks, analyticsByID) {
        buildLovelyTrackIDs(tracks, analyticsByID)
    }

    val allPlaylists = remember(userPlaylists, lovelyTrackIDs, likedSharedPlaylists, appSettings.cacheOnlinePlaylistTracks) {
        buildList {
            if (lovelyTrackIDs.isNotEmpty()) {
                add(
                    PlaylistUiItem(
                        id = "lovely",
                        name = "Lovely songs",
                        trackIds = lovelyTrackIDs,
                        subtitle = "",
                        customCoverPath = null,
                        isLovely = true,
                        isUser = false
                    )
                )
            }

            userPlaylists.forEach { entry ->
                add(
                    PlaylistUiItem(
                        id = entry.id,
                        name = entry.name,
                        trackIds = entry.trackIds,
                        subtitle = "${entry.trackIds.size} tracks",
                        customCoverPath = entry.customCoverPath,
                        isLovely = false,
                        isUser = true
                    )
                )
            }

            likedSharedPlaylists.forEach { entry ->
                add(
                    PlaylistUiItem(
                        id = entry.localId,
                        name = entry.name,
                        trackIds = entry.tracks.mapIndexed { index, _ -> "shared:${entry.remoteId}:$index" },
                        subtitle = sharedPlaylistSubtitle(entry, appSettings.cacheOnlinePlaylistTracks),
                        customCoverPath = entry.cachedCoverPath ?: entry.coverUrl,
                        isLovely = false,
                        isUser = false,
                        isSharedOnline = true,
                        shareUrl = entry.shareUrl
                    )
                )
            }
        }
    }

    val historyTracks = remember(trackByID, historyVersion, tracks) {
        playbackHistoryStore.recentTrackIds(limit = 120).mapNotNull { trackByID[it] }
    }

    val homeWaveTracks = remember(tracks, analyticsByID, favoriteTrackIDs) {
        buildHomeForYouTracks(tracks, analyticsByID, favoriteTrackIDs, limit = 120)
    }
    val homeNeedThisTracks = remember(tracks, analyticsByID, favoriteTrackIDs, homeRecommendationsSessionSeed) {
        buildHomeNeedThisTracks(
            tracks = tracks,
            analyticsByID = analyticsByID,
            favoriteTrackIDs = favoriteTrackIDs,
            limit = 12,
            rotationSeed = homeRecommendationsSessionSeed
        )
    }
    val homeWaveStartTrack = remember(homeWaveTracks, homeVisitCount, lastPresentedHomeHeroTrackID) {
        if (homeWaveTracks.isEmpty()) {
            return@remember null
        }
        val candidateLimit = minOf(homeWaveTracks.size, 6)
        val baseIndex = homeVisitCount.coerceAtLeast(0) % candidateLimit
        var selected = homeWaveTracks[baseIndex]
        if (candidateLimit > 1 && selected.id == lastPresentedHomeHeroTrackID) {
            selected = homeWaveTracks[(baseIndex + 1) % candidateLimit]
        }
        selected
    }
    var homeWaveDisplayTrackID by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(homeWaveTracks, homeWaveStartTrack?.id, playbackController.currentTrackId, playbackController.isPlaying) {
        if (homeWaveTracks.isEmpty()) {
            homeWaveDisplayTrackID = null
            return@LaunchedEffect
        }

        val liveTrackID = playbackController.currentTrackId
        if (!liveTrackID.isNullOrBlank() && homeWaveTracks.any { it.id == liveTrackID }) {
            homeWaveDisplayTrackID = liveTrackID
        } else if (homeWaveDisplayTrackID.isNullOrBlank() ||
            homeWaveTracks.none { it.id == homeWaveDisplayTrackID }
        ) {
            homeWaveDisplayTrackID = homeWaveStartTrack?.id ?: homeWaveTracks.first().id
        }
    }
    val homeWaveDisplayTrack = remember(homeWaveTracks, homeWaveDisplayTrackID, homeWaveStartTrack?.id) {
        val stableID = homeWaveDisplayTrackID
        homeWaveTracks.firstOrNull { it.id == stableID } ?: homeWaveStartTrack
    }
    val homeTasteTracks = remember(homeNeedThisTracks, homeWaveDisplayTrack) {
        val waveTrack = homeWaveDisplayTrack ?: return@remember homeNeedThisTracks
        homeNeedThisTracks.filterNot { it.id == waveTrack.id }
    }
    val homeLastAdded = remember(tracks) {
        tracks.sortedByDescending { it.addedAt }.take(14)
    }
    val homeFreshChoiceTracks = remember(tracks, analyticsByID, favoriteTrackIDs, homeRecommendationsSessionSeed) {
        buildHomeFreshChoiceTracks(
            tracks = tracks,
            analyticsByID = analyticsByID,
            favoriteTrackIDs = favoriteTrackIDs,
            limit = 10,
            rotationSeed = homeRecommendationsSessionSeed
        )
    }
    val homeAlbums = remember(tracks) {
        buildHomeAlbumItems(tracks, limit = 14)
    }

    val myMusicFiltered = remember(tracks, myMusicQuery) {
        tracks.filter { it.matchesQuery(myMusicQuery) }
    }
    val localSearchPlaylistsFiltered by produceState(
        initialValue = emptyList<PlaylistUiItem>(),
        allPlaylists,
        searchQuery
    ) {
        val playlistsSnapshot = allPlaylists
        val querySnapshot = searchQuery
        value = withContext(Dispatchers.Default) {
            val normalized = querySnapshot.trim().lowercase()
            if (normalized.isBlank()) {
                playlistsSnapshot.take(8)
            } else {
                playlistsSnapshot.filter { playlist ->
                    playlist.name.lowercase().contains(normalized)
                }.take(8)
            }
        }
    }
    val localSearchArtistsFiltered by produceState(
        initialValue = emptyList<SearchArtistUiItem>(),
        tracks,
        searchQuery
    ) {
        val tracksSnapshot = tracks
        val querySnapshot = searchQuery
        value = withContext(Dispatchers.Default) {
            buildSearchArtistItems(
                tracks = tracksSnapshot,
                query = querySnapshot,
                limit = 12
            )
        }
    }
    val localSearchTracksForSearch by produceState(
        initialValue = emptyList<TrackItem>(),
        tracks,
        searchQuery
    ) {
        val tracksSnapshot = tracks
        val querySnapshot = searchQuery
        value = withContext(Dispatchers.Default) {
            val directMatches = tracksSnapshot.filter { it.matchesQuery(querySnapshot) }
            if (querySnapshot.trim().isBlank()) {
                tracksSnapshot.take(24)
            } else {
                directMatches.take(24)
            }
        }
    }
    val onlineSearchTracksForSearch = miniStreamingTracks
    val onlineSearchArtistsForSearch = miniStreamingArtists
    val miniStreamingQueuePositions = remember(miniStreamingPlaybackQueue) {
        miniStreamingPlaybackQueue
            .mapIndexed { index, track -> track.trackId to (index + 1) }
            .toMap()
    }
    val playlistsFiltered = remember(allPlaylists, playlistsQuery) {
        val query = playlistsQuery.trim().lowercase()
        if (query.isBlank()) {
            allPlaylists
        } else {
            allPlaylists.filter { it.name.lowercase().contains(query) }
        }
    }

    val openedPlaylist = remember(openedPlaylistID, allPlaylists) {
        allPlaylists.firstOrNull { it.id == openedPlaylistID }
    }
    val openedSharedPlaylist = remember(openedPlaylistID, likedSharedPlaylists, openedTransientSharedPlaylist) {
        val currentID = openedPlaylistID
        if (currentID.isNullOrBlank()) {
            null
        } else {
            likedSharedPlaylists.firstOrNull { it.localId == currentID }
                ?: openedTransientSharedPlaylist?.takeIf { it.localId == currentID }
        }
    }
    val openedPlaylistTracks = remember(openedPlaylist, trackByID) {
        openedPlaylist?.trackIds?.mapNotNull { trackByID[it] } ?: emptyList()
    }
    val openedHomeAlbum = remember(openedHomeAlbumArtistKey, homeAlbums) {
        homeAlbums.firstOrNull { it.artistKey == openedHomeAlbumArtistKey }
    }
    val openedHomeAlbumTracks = remember(openedHomeAlbum, trackByID) {
        openedHomeAlbum?.trackIds?.mapNotNull { trackByID[it] } ?: emptyList()
    }
    val openedDetailPlaylist = remember(openedPlaylist, openedSharedPlaylist, openedHomeAlbum, appSettings.cacheOnlinePlaylistTracks) {
        openedPlaylist ?: openedSharedPlaylist?.let {
            PlaylistUiItem(
                id = it.localId,
                name = it.name,
                trackIds = it.tracks.mapIndexed { index, _ -> "shared:${it.remoteId}:$index" },
                subtitle = sharedPlaylistSubtitle(it, appSettings.cacheOnlinePlaylistTracks),
                customCoverPath = it.cachedCoverPath ?: it.coverUrl,
                isLovely = false,
                isUser = false,
                isSharedOnline = true,
                shareUrl = it.shareUrl
            )
        } ?: openedHomeAlbum?.let {
            PlaylistUiItem(
                id = "album:${it.artistKey}",
                name = it.title,
                trackIds = it.trackIds,
                subtitle = "${it.trackIds.size} tracks",
                customCoverPath = null,
                isLovely = false,
                isUser = false
            )
        }
    }
    val openedDetailTracks = remember(openedPlaylist, openedSharedPlaylist, openedPlaylistTracks, openedHomeAlbumTracks) {
        if (openedPlaylist?.isSharedOnline == true || openedSharedPlaylist != null) {
            val shared = openedSharedPlaylist ?: return@remember emptyList()
            shared.tracks.mapIndexed { index, track ->
                TrackItem(
                    id = "shared:${shared.remoteId}:$index",
                    title = track.title,
                    artist = track.artist,
                    durationMs = track.durationMs,
                    filePath = track.cachedFilePath ?: track.fileUrl.orEmpty(),
                    artworkPath = track.cachedArtworkPath ?: track.artworkUrl,
                    addedAt = shared.createdAt,
                    isFavorite = false
                )
            }
        } else if (openedPlaylist != null) {
            openedPlaylistTracks
        } else {
            openedHomeAlbumTracks
        }
    }

    val createTrackFiltered = remember(tracksByAffinity, playlistCreateQuery) {
        tracksByAffinity.filter { it.matchesQuery(playlistCreateQuery) }
    }

    val addTracksAvailable = remember(addTracksTargetPlaylistID, tracks, userPlaylists) {
        val playlistID = addTracksTargetPlaylistID
        if (playlistID.isNullOrBlank()) {
            emptyList()
        } else {
            val existing = userPlaylists.firstOrNull { it.id == playlistID }?.trackIds?.toSet() ?: emptySet()
            tracks.filterNot { existing.contains(it.id) }
        }
    }

    fun reloadPlaylists() {
        scope.launch(Dispatchers.IO) {
            val reloadedPlaylists = playlistStore.loadPlaylists()
            withContext(Dispatchers.Main.immediate) {
                userPlaylists = reloadedPlaylists
            }
        }
    }

    fun reloadSharedPlaylists() {
        scope.launch(Dispatchers.IO) {
            val reloadedSharedPlaylists = sharedPlaylistStore.loadPlaylists().map(::sanitizeSharedPlaylistEntry)
            val cacheUsageBytes = sharedPlaylistStore.audioCacheUsageBytes()
            withContext(Dispatchers.Main.immediate) {
                likedSharedPlaylists = reloadedSharedPlaylists
                sharedPlaylistAudioCacheUsageBytes = cacheUsageBytes
                openedTransientSharedPlaylist = openedTransientSharedPlaylist?.let { transient ->
                    reloadedSharedPlaylists.firstOrNull { it.remoteId == transient.remoteId || it.localId == transient.localId }
                        ?: transient
                }
            }
        }
    }

    val sharedPlaylistCacheRefreshKeys = remember(likedSharedPlaylists) {
        likedSharedPlaylists.map { "${it.remoteId}:${it.contentSha256}" }
    }

    LaunchedEffect(
        appSettings.cacheOnlinePlaylistTracks,
        appSettings.onlinePlaylistCacheMaxMb,
        sharedPlaylistCacheRefreshKeys
    ) {
        withContext(Dispatchers.IO) {
            if (!appSettings.cacheOnlinePlaylistTracks) {
                sharedPlaylistStore.trimAudioCache(0L)
                return@withContext
            }
            if (likedSharedPlaylists.isEmpty()) {
                return@withContext
            }
            val limitBytes = if (appSettings.onlinePlaylistCacheMaxMb > 0) {
                appSettings.onlinePlaylistCacheMaxMb.toLong() * 1_048_576L
            } else {
                Long.MAX_VALUE
            }
            likedSharedPlaylists.forEach { entry ->
                sharedPlaylistStore.cacheAssets(
                    entry,
                    cacheAudio = true,
                    audioLimitBytes = limitBytes,
                    onProgress = { partial ->
                        mainHandler.post {
                            applySharedPlaylistProgressUpdate(partial)
                        }
                    }
                )
            }
        }
        reloadSharedPlaylists()
    }

    fun openSharedPlaylist(entry: SharedPlaylistEntry, transient: Boolean) {
        if (transient) {
            openedTransientSharedPlaylist = entry
        }
        openedPlaylistID = entry.localId
        showPlaylistsListPage = true
        selectedTab = SonoraTab.Playlists
    }

    fun sharedTrackMatchesLocal(track: SharedPlaylistTrackEntry, local: TrackItem): Boolean {
        fun normalize(value: String): String {
            return value.trim().lowercase().replace(Regex("\\s+"), " ")
        }

        fun artistTokens(value: String): List<String> {
            val normalized = normalize(value)
            if (normalized.isBlank()) {
                return emptyList()
            }
            return normalized
                .split(",", "/", "&", ";", "|")
                .map { it.trim() }
                .filter { it.length > 1 }
                .distinct()
        }

        val sharedTitle = normalize(track.title)
        val localTitle = normalize(local.title)
        if (sharedTitle.isBlank() || sharedTitle != localTitle) {
            return false
        }

        val sharedArtist = normalize(track.artist)
        val localArtist = normalize(local.artist)
        if (sharedArtist.isBlank() || localArtist.isBlank()) {
            return false
        }
        if (localArtist.contains(sharedArtist) || sharedArtist.contains(localArtist)) {
            return track.durationMs <= 0L || local.durationMs <= 0L ||
                kotlin.math.abs(track.durationMs - local.durationMs) <= 3_000L
        }

        val tokens = artistTokens(track.artist)
        if (!tokens.any { token -> localArtist.contains(token) }) {
            return false
        }
        return track.durationMs <= 0L || local.durationMs <= 0L ||
            kotlin.math.abs(track.durationMs - local.durationMs) <= 3_000L
    }

    fun importSharedPlaylistLocally(
        entry: SharedPlaylistEntry,
        localTracks: List<TrackItem>,
        onProgress: (String) -> Unit = {}
    ): Pair<List<String>, String?> {
        val importedIds = mutableListOf<String>()
        val downloadedCoverPath = entry.coverUrl
            ?.takeIf { it.isNotBlank() }
            ?.let { coverUrl ->
                onProgress("Downloading cover...")
                trackStore.importRemoteArtwork(
                    urlString = coverUrl,
                    suggestedName = "${entry.remoteId.ifBlank { "playlist" }}_cover.jpg"
                )
            }
        entry.tracks.forEachIndexed { index, sharedTrack ->
            val existing = localTracks.firstOrNull { local -> sharedTrackMatchesLocal(sharedTrack, local) }?.id
            if (!existing.isNullOrBlank()) {
                importedIds += existing
                return@forEachIndexed
            }
            val fileUrl = sharedTrack.fileUrl
            if (fileUrl.isNullOrBlank()) {
                return@forEachIndexed
            }
            onProgress("Downloading track ${index + 1}/${entry.tracks.size}...")
            val suggestedName = buildString {
                if (sharedTrack.artist.isNotBlank()) {
                    append(sharedTrack.artist)
                    append(" - ")
                }
                append(sharedTrack.title.ifBlank { "Track ${index + 1}" })
                append(".mp3")
            }
            val imported = trackStore.importRemoteTrack(fileUrl, suggestedName, sharedTrack.artworkUrl)
            if (imported != null) {
                importedIds += imported.id
            }
        }
        return importedIds.distinct() to downloadedCoverPath
    }

    fun shareText(text: String) {
        if (text.isBlank()) {
            return
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, "Share Playlist").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    fun miniStreamingPlaybackIdForTrack(trackId: String): String {
        return "$MINI_STREAMING_PLAYBACK_PREFIX${trackId.trim()}"
    }

    fun miniStreamingTrackIdFromPlaybackId(playbackId: String?): String? {
        val normalized = playbackId?.trim().orEmpty()
        if (normalized.startsWith(MINI_STREAMING_PLAYBACK_PREFIX)) {
            val trackId = normalized.removePrefix(MINI_STREAMING_PLAYBACK_PREFIX).trim()
            if (trackId.isNotBlank()) {
                return trackId
            }
        }
        if (normalized.isNotBlank()) {
            miniStreamingInstalledTrackMap.entries.firstOrNull { it.value == normalized }?.let { return it.key }
        }
        return null
    }

    fun normalizedMiniStreamingMeta(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")
    }

    fun miniStreamingArtistTokens(value: String): List<String> {
        val normalized = normalizedMiniStreamingMeta(value)
        if (normalized.isBlank()) {
            return emptyList()
        }
        return normalized
            .split(",", "/", "&", ";", "|")
            .map { it.trim() }
            .filter { it.length > 1 }
            .distinct()
    }

    fun isStrictMiniStreamingMetadataMatch(track: MiniStreamingTrack, item: TrackItem): Boolean {
        val normalizedTitle = normalizedMiniStreamingMeta(track.title)
        val normalizedArtist = normalizedMiniStreamingMeta(track.artists)
        val itemTitle = normalizedMiniStreamingMeta(item.title)
        val itemArtist = normalizedMiniStreamingMeta(item.artist)
        if (normalizedTitle.isBlank() || itemTitle.isBlank() || normalizedTitle != itemTitle) {
            return false
        }
        if (normalizedArtist.isBlank() || itemArtist.isBlank()) {
            return false
        }
        if (itemArtist.contains(normalizedArtist) || normalizedArtist.contains(itemArtist)) {
            if (track.durationMs > 0L && item.durationMs > 0L) {
                return kotlin.math.abs(track.durationMs - item.durationMs) <= 3_000L
            }
            return true
        }
        val artistTokens = miniStreamingArtistTokens(track.artists)
        if (!artistTokens.any { token -> itemArtist.contains(token) }) {
            return false
        }
        if (track.durationMs > 0L && item.durationMs > 0L) {
            return kotlin.math.abs(track.durationMs - item.durationMs) <= 3_000L
        }
        return true
    }

    fun knownInstalledMiniStreamingTrack(track: MiniStreamingTrack): TrackItem? {
        val mappedLocalTrackId = miniStreamingInstalledTrackMap[track.trackId]
        if (!mappedLocalTrackId.isNullOrBlank()) {
            val mapped = trackByID[mappedLocalTrackId]
            if (mapped != null && isStrictMiniStreamingMetadataMatch(track, mapped)) {
                return mapped
            }
            val cleaned = miniStreamingInstalledTrackMap - track.trackId
            miniStreamingInstalledTrackMap = cleaned
            settingsStore.saveMiniStreamingInstalledTrackMap(cleaned)
        }

        val normalizedTitle = normalizedMiniStreamingMeta(track.title)
        if (normalizedTitle.isBlank()) {
            return null
        }
        val fallback = tracks.firstOrNull { item ->
            isStrictMiniStreamingMetadataMatch(track, item)
        } ?: return null

        val updatedMap = miniStreamingInstalledTrackMap + (track.trackId to fallback.id)
        miniStreamingInstalledTrackMap = updatedMap
        settingsStore.saveMiniStreamingInstalledTrackMap(updatedMap)
        return fallback
    }

    fun rememberInstalledMiniStreamingTrack(trackId: String, localTrackId: String) {
        if (trackId.isBlank() || localTrackId.isBlank()) {
            return
        }
        val updatedMap = miniStreamingInstalledTrackMap + (trackId to localTrackId)
        miniStreamingInstalledTrackMap = updatedMap
        settingsStore.saveMiniStreamingInstalledTrackMap(updatedMap)
    }

    fun miniStreamingPlaybackTrackFor(
        track: MiniStreamingTrack,
        payload: MiniStreamingDownloadPayload?
    ): TrackItem? {
        val installed = knownInstalledMiniStreamingTrack(track)
        if (installed != null) {
            return TrackItem(
                id = miniStreamingPlaybackIdForTrack(track.trackId),
                title = installed.title.ifBlank { track.title.ifBlank { "Track" } },
                artist = installed.artist.ifBlank { track.artists.ifBlank { "Spotify" } },
                durationMs = if (installed.durationMs > 0L) installed.durationMs else track.durationMs,
                filePath = installed.filePath,
                artworkPath = installed.artworkPath ?: track.artworkUrl.ifBlank { payload?.artworkUrl.orEmpty() },
                addedAt = installed.addedAt,
                isFavorite = installed.isFavorite
            )
        }

        val source = payload?.mediaUrl?.trim().orEmpty()
        if (source.isBlank()) {
            return null
        }

        return TrackItem(
            id = miniStreamingPlaybackIdForTrack(track.trackId),
            title = track.title.ifBlank { payload?.title?.ifBlank { "Track" } ?: "Track" },
            artist = track.artists.ifBlank { payload?.artist?.ifBlank { "Spotify" } ?: "Spotify" },
            durationMs = when {
                track.durationMs > 0L -> track.durationMs
                payload != null && payload.durationMs > 0L -> payload.durationMs
                else -> 0L
            },
            filePath = source,
            artworkPath = track.artworkUrl.ifBlank { payload?.artworkUrl.orEmpty() },
            addedAt = System.currentTimeMillis(),
            isFavorite = false
        )
    }

    fun buildMiniStreamingPlaybackQueue(queue: List<MiniStreamingTrack>): List<TrackItem> {
        return queue.mapNotNull { miniTrack ->
            miniStreamingPlaybackTrackFor(
                track = miniTrack,
                payload = miniStreamingResolvedPayloadByTrackId[miniTrack.trackId]
            )
        }
    }

    LaunchedEffect(trackByID, miniStreamingInstalledTrackMap) {
        val sanitized = miniStreamingInstalledTrackMap.filterValues { localTrackId ->
            trackByID.containsKey(localTrackId)
        }
        if (sanitized != miniStreamingInstalledTrackMap) {
            miniStreamingInstalledTrackMap = sanitized
            settingsStore.saveMiniStreamingInstalledTrackMap(sanitized)
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == SonoraTab.Home) {
            if (!wasHomeSelected) {
                homeVisitCount += 1
                wasHomeSelected = true
            }
        } else {
            wasHomeSelected = false
        }
    }

    LaunchedEffect(homeVisitCount, homeWaveStartTrack?.id) {
        val heroID = homeWaveStartTrack?.id
        if (!heroID.isNullOrBlank()) {
            lastPresentedHomeHeroTrackID = heroID
        }
    }

    LaunchedEffect(
        selectedTab,
        showMusicPage,
        showFavoritesPage,
        showPlaylistsListPage,
        openedPlaylistID,
        openedHomeAlbumArtistKey,
        openedMiniStreamingArtist?.artistId
    ) {
        val isSearchRoot = selectedTab == SonoraTab.Music &&
            !showMusicPage &&
            !showFavoritesPage &&
            !showPlaylistsListPage &&
            openedPlaylistID == null &&
            openedHomeAlbumArtistKey == null &&
            openedMiniStreamingArtist == null
        if (!isSearchRoot) {
            miniStreamingTracksLoading = false
            miniStreamingArtistsLoading = false
            miniStreamingArtistsSectionVisible = miniStreamingClient.areArtistsEnabled()
            miniStreamingTracks = emptyList()
            miniStreamingArtists = emptyList()
            return@LaunchedEffect
        }
        snapshotFlow { searchQuery.trim() }
            .collectLatest { normalizedQuery ->
                delay(MINI_STREAMING_SEARCH_DEBOUNCE_MS)

                if (!miniStreamingClient.isConfigured()) {
                    miniStreamingTracksLoading = false
                    miniStreamingArtistsLoading = false
                    miniStreamingArtistsSectionVisible = miniStreamingClient.areArtistsEnabled()
                    miniStreamingTracks = emptyList()
                    miniStreamingArtists = emptyList()
                    return@collectLatest
                }

                if (normalizedQuery.isBlank()) {
                    miniStreamingTracksLoading = false
                    miniStreamingArtistsLoading = false
                    miniStreamingArtistsSectionVisible = miniStreamingClient.areArtistsEnabled()
                    miniStreamingTracks = emptyList()
                    miniStreamingArtists = emptyList()
                    return@collectLatest
                }

                miniStreamingTracksLoading = true
                miniStreamingArtistsLoading = true

                val resolvedTracks = withContext(Dispatchers.IO) {
                    miniStreamingClient.searchTracks(normalizedQuery, limit = MINI_STREAMING_TRACKS_SEARCH_LIMIT)
                }
                val artistsVisibleAfterTracks = miniStreamingClient.areArtistsEnabled()
                val resolvedArtists = if (artistsVisibleAfterTracks) {
                    withContext(Dispatchers.IO) {
                        miniStreamingClient.searchArtists(normalizedQuery, limit = MINI_STREAMING_ARTISTS_SEARCH_LIMIT)
                    }
                } else {
                    emptyList()
                }
                val artistsVisibleAfterSearch = miniStreamingClient.areArtistsEnabled()

                miniStreamingTracks = resolvedTracks
                miniStreamingArtists = if (artistsVisibleAfterSearch) resolvedArtists else emptyList()
                miniStreamingArtistsSectionVisible = artistsVisibleAfterSearch
                miniStreamingTracksLoading = false
                miniStreamingArtistsLoading = false
            }
    }

    LaunchedEffect(openedMiniStreamingArtist?.artistId) {
        openedMiniStreamingArtistTracks = emptyList()
        openedMiniStreamingArtistLoading = false
        openedMiniStreamingArtistLoadingMore = false
        openedMiniStreamingArtistHasMore = false
        openedMiniStreamingArtistRequestedLimit = MINI_STREAMING_ARTIST_PAGE_SIZE
    }

    LaunchedEffect(openedMiniStreamingArtist?.artistId, openedMiniStreamingArtistRequestedLimit) {
        val openedArtist = openedMiniStreamingArtist ?: return@LaunchedEffect
        if (!miniStreamingClient.isConfigured()) {
            openedMiniStreamingArtistTracks = emptyList()
            openedMiniStreamingArtistLoading = false
            openedMiniStreamingArtistLoadingMore = false
            openedMiniStreamingArtistHasMore = false
            return@LaunchedEffect
        }

        openedMiniStreamingArtistRequestToken += 1
        val requestToken = openedMiniStreamingArtistRequestToken
        val requestedLimit = openedMiniStreamingArtistRequestedLimit.coerceAtLeast(MINI_STREAMING_ARTIST_PAGE_SIZE)
        val firstLoad = openedMiniStreamingArtistTracks.isEmpty()

        if (firstLoad) {
            openedMiniStreamingArtistLoading = true
            openedMiniStreamingArtistLoadingMore = false
        } else {
            openedMiniStreamingArtistLoadingMore = true
        }

        val resolved = withContext(Dispatchers.IO) {
            miniStreamingClient.fetchTopTracksForArtist(openedArtist.artistId, limit = requestedLimit)
        }
        if (requestToken != openedMiniStreamingArtistRequestToken) {
            return@LaunchedEffect
        }

        openedMiniStreamingArtistTracks = resolved
        openedMiniStreamingArtistHasMore = resolved.size >= requestedLimit && resolved.isNotEmpty()
        openedMiniStreamingArtistLoading = false
        openedMiniStreamingArtistLoadingMore = false
    }

    fun persistTracks(updated: List<TrackItem>) {
        tracks = updated
        scope.launch(Dispatchers.IO) {
            trackStore.saveTracks(updated)
        }
    }

    fun refreshMiniStreamingQueueInPlayer() {
        val refreshedQueue = buildMiniStreamingPlaybackQueue(miniStreamingPlaybackQueue)
        if (refreshedQueue.isEmpty()) {
            return
        }
        playbackController.replaceQueuePreservingCurrent(refreshedQueue)
    }

    fun requestMoreOpenedMiniStreamingArtistTracks() {
        if (openedMiniStreamingArtist == null) {
            return
        }
        if (openedMiniStreamingArtistLoading || openedMiniStreamingArtistLoadingMore || !openedMiniStreamingArtistHasMore) {
            return
        }
        openedMiniStreamingArtistRequestedLimit += MINI_STREAMING_ARTIST_PAGE_SIZE
    }

    fun launchMiniStreamingResolve(
        track: MiniStreamingTrack,
        onResolved: (MiniStreamingDownloadPayload) -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        if (track.trackId.isBlank()) {
            onFailure()
            return
        }
        val cachedPayload = miniStreamingResolvedPayloadByTrackId[track.trackId]
        if (cachedPayload != null) {
            onResolved(cachedPayload)
            return
        }
        if (miniStreamingResolvingTrackIds.contains(track.trackId)) {
            return
        }
        miniStreamingResolvingTrackIds = miniStreamingResolvingTrackIds + track.trackId
        val resolvingJob = scope.launch {
            val payload = withContext(Dispatchers.IO) {
                miniStreamingClient.resolveDownload(track.trackId)
            }
            if (payload == null) {
                onFailure()
                return@launch
            }
            miniStreamingResolvedPayloadByTrackId =
                miniStreamingResolvedPayloadByTrackId + (track.trackId to payload)
            refreshMiniStreamingQueueInPlayer()
            onResolved(payload)
        }
        miniStreamingResolvingJobs[track.trackId] = resolvingJob
        resolvingJob.invokeOnCompletion {
            miniStreamingResolvingJobs.remove(track.trackId)
            miniStreamingResolvingTrackIds = miniStreamingResolvingTrackIds - track.trackId
        }
        }

    fun prefetchMiniStreamingQueuePayloads(
        queue: List<MiniStreamingTrack>,
        startIndexExclusive: Int,
        maxCount: Int = 12
    ) {
        // Disabled on purpose:
        // resolve/download must happen only for the track explicitly opened by user,
        // without background prefetch of next queue items.
        return
    }

    fun sanitizeMiniStreamingExtension(extension: String): String {
        val normalized = extension.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
        return normalized.ifBlank { "mp3" }
    }

    fun consumeMiniStreamingResolveFailureMessage(fallback: String): String {
        val resolved = miniStreamingClient.consumeLastResolveFailureMessage()?.trim().orEmpty()
        return if (resolved.isNotBlank()) resolved else fallback
    }

    suspend fun installMiniStreamingTrackToLibrary(
        track: MiniStreamingTrack,
        payload: MiniStreamingDownloadPayload
    ): TrackItem? {
        val existing = knownInstalledMiniStreamingTrack(track)
        if (existing != null) {
            return existing
        }

        val extension = sanitizeMiniStreamingExtension(payload.extension)
        val libraryDir = File(context.filesDir, "Sonora/mini_streaming").apply { mkdirs() }
        val destination = File(libraryDir, "${track.trackId}.$extension")
        val downloadOk = withContext(Dispatchers.IO) {
            if (destination.exists() && destination.length() > 0L) {
                true
            } else {
                miniStreamingClient.downloadToFile(payload.mediaUrl, destination)
            }
        }
        if (!downloadOk || !destination.exists() || destination.length() <= 0L) {
            return null
        }

        val alreadyInstalled = knownInstalledMiniStreamingTrack(track)
        if (alreadyInstalled != null) {
            return alreadyInstalled
        }

        val newTrack = TrackItem(
            id = UUID.randomUUID().toString(),
            title = track.title.ifBlank { payload.title.ifBlank { "Track" } },
            artist = track.artists.ifBlank { payload.artist.ifBlank { "Spotify" } },
            durationMs = when {
                track.durationMs > 0L -> track.durationMs
                payload.durationMs > 0L -> payload.durationMs
                else -> 0L
            },
            filePath = destination.absolutePath,
            artworkPath = track.artworkUrl.ifBlank { payload.artworkUrl },
            addedAt = System.currentTimeMillis(),
            isFavorite = false
        )

        val deduped = tracks.filterNot { item ->
            item.filePath == destination.absolutePath ||
                (item.title.equals(newTrack.title, ignoreCase = true) &&
                    item.artist.equals(newTrack.artist, ignoreCase = true))
        }
        val updated = listOf(newTrack) + deduped
        persistTracks(updated)
        rememberInstalledMiniStreamingTrack(track.trackId, newTrack.id)
        refreshMiniStreamingQueueInPlayer()
        return newTrack
    }

    fun launchMiniStreamingInstall(
        track: MiniStreamingTrack,
        initialPayload: MiniStreamingDownloadPayload? = null,
        showErrorMessage: Boolean
    ) {
        if (track.trackId.isBlank()) {
            return
        }
        if (knownInstalledMiniStreamingTrack(track) != null) {
            return
        }
        if (miniStreamingInstallingTrackIds.contains(track.trackId)) {
            return
        }
        miniStreamingInstallingTrackIds = miniStreamingInstallingTrackIds + track.trackId

        val installJob = scope.launch {
            val payload = initialPayload
                ?: miniStreamingResolvedPayloadByTrackId[track.trackId]
                ?: withContext(Dispatchers.IO) {
                    miniStreamingClient.resolveDownload(track.trackId)
                }

            if (payload == null) {
                if (showErrorMessage) {
                    snackbarHostState.showSnackbar(
                        consumeMiniStreamingResolveFailureMessage("Install failed for ${track.title}")
                    )
                }
                return@launch
            }

            miniStreamingResolvedPayloadByTrackId = miniStreamingResolvedPayloadByTrackId + (track.trackId to payload)
            refreshMiniStreamingQueueInPlayer()

            val installed = installMiniStreamingTrackToLibrary(track, payload)
            if (installed == null && showErrorMessage) {
                snackbarHostState.showSnackbar("Install failed for ${track.title}")
            } else if (installed != null && miniStreamingActiveTrackId == track.trackId && playbackController.currentTrackId == null) {
                val targetPlaybackId = miniStreamingPlaybackIdForTrack(track.trackId)
                val playbackQueue = buildMiniStreamingPlaybackQueue(miniStreamingPlaybackQueue)
                if (playbackQueue.any { it.id == targetPlaybackId }) {
                    playbackController.playOrToggleFromQueue(playbackQueue, targetPlaybackId)
                    playerVisible = true
                }
            }
        }
        miniStreamingInstallingJobs[track.trackId] = installJob
        installJob.invokeOnCompletion {
            miniStreamingInstallingTrackIds = miniStreamingInstallingTrackIds - track.trackId
            miniStreamingInstallingJobs.remove(track.trackId)
        }
    }

    fun playMiniStreamingTrack(
        track: MiniStreamingTrack,
        queue: List<MiniStreamingTrack>,
        startIndex: Int
    ) {
        if (track.trackId.isBlank()) {
            return
        }
        val targetPlaybackId = miniStreamingPlaybackIdForTrack(track.trackId)
        if (playbackController.currentTrackId == targetPlaybackId && playbackController.queueCount > 0) {
            playbackController.togglePlayPause()
            playerVisible = true
            return
        }
        if (miniStreamingInstallingTrackIds.contains(track.trackId) ||
            miniStreamingResolvingTrackIds.contains(track.trackId)
        ) {
            return
        }

        val playbackSeedQueue = if (queue.isEmpty()) listOf(track) else queue
        val queueTrackIndex = playbackSeedQueue.indexOfFirst { it.trackId == track.trackId }
        val normalizedStartIndex = if (queueTrackIndex >= 0) {
            queueTrackIndex
        } else {
            startIndex.coerceIn(0, (playbackSeedQueue.size - 1).coerceAtLeast(0))
        }

        miniStreamingActiveTrackId = track.trackId
        miniStreamingPlaybackQueue = playbackSeedQueue
        miniStreamingPendingTrack = TrackItem(
            id = targetPlaybackId,
            title = track.title.ifBlank { "Track" },
            artist = track.artists.ifBlank { "Spotify" },
            durationMs = track.durationMs,
            filePath = "",
            artworkPath = track.artworkUrl,
            addedAt = System.currentTimeMillis(),
            isFavorite = false
        )
        playerVisible = true
        playbackController.stop()

        val installed = knownInstalledMiniStreamingTrack(track)
        if (installed != null) {
                val playbackQueue = buildMiniStreamingPlaybackQueue(miniStreamingPlaybackQueue)
                if (playbackQueue.any { it.id == targetPlaybackId }) {
                    playbackController.playOrToggleFromQueue(playbackQueue, targetPlaybackId)
                    prefetchMiniStreamingQueuePayloads(
                        queue = miniStreamingPlaybackQueue,
                        startIndexExclusive = normalizedStartIndex + 1
                    )
                }
            return
        }

        launchMiniStreamingResolve(
            track = track,
            onResolved = { payload ->
                val playbackQueue = buildMiniStreamingPlaybackQueue(miniStreamingPlaybackQueue)
                if (playbackQueue.any { it.id == targetPlaybackId }) {
                    playbackController.playOrToggleFromQueue(playbackQueue, targetPlaybackId)
                    prefetchMiniStreamingQueuePayloads(
                        queue = miniStreamingPlaybackQueue,
                        startIndexExclusive = normalizedStartIndex + 1
                    )
                } else {
                    miniStreamingPendingTrack = null
                    miniStreamingActiveTrackId = null
                    scope.launch {
                        snackbarHostState.showSnackbar("Cannot start ${track.title}")
                    }
                }
            },
            onFailure = {
                miniStreamingPendingTrack = null
                miniStreamingActiveTrackId = null
                scope.launch {
                    snackbarHostState.showSnackbar(
                        consumeMiniStreamingResolveFailureMessage("Cannot open ${track.title}")
                    )
                }
            }
        )
    }

    LaunchedEffect(
        playbackController.currentTrackId,
        miniStreamingPlaybackQueue,
        miniStreamingResolvedPayloadByTrackId
    ) {
        val currentMiniTrackId = miniStreamingTrackIdFromPlaybackId(playbackController.currentTrackId)
            ?: return@LaunchedEffect
        val currentIndex = miniStreamingPlaybackQueue.indexOfFirst { it.trackId == currentMiniTrackId }
        if (currentIndex >= 0) {
            prefetchMiniStreamingQueuePayloads(
                queue = miniStreamingPlaybackQueue,
                startIndexExclusive = currentIndex + 1
            )
        }
    }

    val onFavoriteToggle: (String) -> Unit = { trackID ->
        val updated = tracks.map { track ->
            if (track.id == trackID) {
                track.copy(isFavorite = !track.isFavorite)
            } else {
                track
            }
        }
        persistTracks(updated)
    }

    fun deleteTrackFromLibrary(trackID: String) {
        val target = tracks.firstOrNull { it.id == trackID } ?: return
        val updated = tracks.filterNot { it.id == trackID }
        persistTracks(updated)
        val cleanedMap = miniStreamingInstalledTrackMap.filterValues { localTrackID ->
            localTrackID != trackID
        }
        if (cleanedMap != miniStreamingInstalledTrackMap) {
            miniStreamingInstalledTrackMap = cleanedMap
            settingsStore.saveMiniStreamingInstalledTrackMap(cleanedMap)
        }
        playlistStore.removeTrackIdFromAllPlaylists(trackID)
        reloadPlaylists()
        if (playbackController.currentTrackId == trackID) {
            playbackController.stop()
        }
        scope.launch(Dispatchers.IO) {
            trackStore.deleteTrackFiles(target)
        }
    }

    fun removeTrackFromOpenedPlaylist(trackID: String) {
        val playlistID = openedPlaylistID ?: return
        val removed = playlistStore.removeTrackId(playlistID, trackID)
        if (removed) {
            reloadPlaylists()
        }
    }

    fun clearTrackSelection() {
        trackSelectionContext = null
        selectedTrackIDs = emptySet()
    }

    fun toggleTrackSelection(
        context: TrackSelectionContext,
        trackID: String,
        forceSelect: Boolean = false
    ) {
        if (trackID.isBlank()) {
            return
        }

        if (trackSelectionContext != context) {
            trackSelectionContext = context
            selectedTrackIDs = setOf(trackID)
            return
        }

        val currentlySelected = selectedTrackIDs.contains(trackID)
        selectedTrackIDs = when {
            forceSelect && !currentlySelected -> selectedTrackIDs + trackID
            forceSelect && currentlySelected -> selectedTrackIDs
            currentlySelected -> selectedTrackIDs - trackID
            else -> selectedTrackIDs + trackID
        }

        if (selectedTrackIDs.isEmpty()) {
            trackSelectionContext = null
        }
    }

    val addMusicLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (uris.isEmpty()) {
                return@rememberLauncherForActivityResult
            }

            scope.launch {
                uris.forEach { uri ->
                    runCatching {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }
                }

                val hasStorageLimit = appSettings.maxStorageMb > 0
                val maxStorageBytes = if (hasStorageLimit) {
                    appSettings.maxStorageMb.toLong() * 1_048_576L
                } else {
                    Long.MAX_VALUE
                }
                var currentUsageBytes = withContext(Dispatchers.IO) {
                    computeAppStorageUsageBytes(context)
                }

                if (hasStorageLimit && currentUsageBytes >= maxStorageBytes) {
                    snackbarHostState.showSnackbar(
                        "Storage limit reached (${formatStorageSize(maxStorageBytes)}). Increase max space in Settings."
                    )
                    return@launch
                }

                val allowedUris = mutableListOf<Uri>()
                var skippedUnknownSize = 0
                var skippedOverLimit = 0
                var remainingBytes = if (hasStorageLimit) {
                    maxStorageBytes - currentUsageBytes
                } else {
                    Long.MAX_VALUE
                }

                uris.forEach { uri ->
                    val uriSize = queryUriSizeBytes(context, uri)
                    if (uriSize == null) {
                        skippedUnknownSize += 1
                        return@forEach
                    }
                    if (hasStorageLimit && uriSize > remainingBytes) {
                        skippedOverLimit += 1
                        return@forEach
                    }
                    allowedUris += uri
                    if (hasStorageLimit) {
                        remainingBytes -= uriSize
                    }
                }

                if (allowedUris.isEmpty()) {
                    val message = when {
                        skippedOverLimit > 0 && skippedUnknownSize > 0 ->
                            "Nothing imported: over storage limit and unknown file sizes."
                        skippedOverLimit > 0 ->
                            "Nothing imported: selected files exceed storage limit."
                        skippedUnknownSize > 0 ->
                            "Nothing imported: couldn't read selected file sizes."
                        else -> "Nothing imported."
                    }
                    snackbarHostState.showSnackbar(message)
                    return@launch
                }

                val result = withContext(Dispatchers.IO) {
                    trackStore.importTracks(allowedUris)
                }
                tracks = withContext(Dispatchers.IO) {
                    trackStore.loadTracks()
                }
                currentUsageBytes = withContext(Dispatchers.IO) {
                    computeAppStorageUsageBytes(context)
                }
                storageUsageBytes = currentUsageBytes

                val baseMessage = when {
                    result.added > 0 && result.failed == 0 -> "Added ${result.added} track(s)"
                    result.added > 0 -> "Added ${result.added}, failed ${result.failed}"
                    else -> "Could not add selected files"
                }
                val suffixParts = mutableListOf<String>()
                if (skippedOverLimit > 0) {
                    suffixParts += "limit skipped: $skippedOverLimit"
                }
                if (skippedUnknownSize > 0) {
                    suffixParts += "unknown size skipped: $skippedUnknownSize"
                }
                val message = if (suffixParts.isEmpty()) {
                    baseMessage
                } else {
                    "$baseMessage (${suffixParts.joinToString()})"
                }
                snackbarHostState.showSnackbar(message)
            }
        }
    val changePlaylistCoverLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val playlistID = coverTargetPlaylistID
            coverTargetPlaylistID = null
            if (uri == null || playlistID.isNullOrBlank()) {
                return@rememberLauncherForActivityResult
            }

            scope.launch {
                val changed = withContext(Dispatchers.IO) {
                    playlistStore.setCustomCover(playlistID, uri)
                }
                if (changed) {
                    reloadPlaylists()
                    snackbarHostState.showSnackbar("Playlist cover updated")
                } else {
                    snackbarHostState.showSnackbar("Could not set playlist cover")
                }
            }
        }
    val exportBackupLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/octet-stream")
        ) { uri ->
            if (uri == null) {
                return@rememberLauncherForActivityResult
            }

            val tracksSnapshot = tracks
            val playlistsSnapshot = userPlaylists
            val settingsSnapshot = appSettings

            scope.launch {
                val exported = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            SonoraBackupArchive.exportToStream(
                                output = output,
                                tracks = tracksSnapshot,
                                playlists = playlistsSnapshot,
                                settings = SonoraBackupSettings(
                                    sliderStyle = settingsSnapshot.sliderStyle.storageValue,
                                    artworkStyle = settingsSnapshot.artworkStyle.storageValue,
                                    fontStyle = settingsSnapshot.fontStyle.storageValue,
                                    accentHex = settingsSnapshot.accentHex,
                                    preservePlayerModes = settingsSnapshot.preservePlayerModes,
                                    trackGapSeconds = settingsSnapshot.trackGapSeconds,
                                    maxStorageMb = settingsSnapshot.maxStorageMb
                                )
                            )
                        } != null
                    }.getOrDefault(false)
                }

                if (exported) {
                    snackbarHostState.showSnackbar("Backup archive exported")
                } else {
                    snackbarHostState.showSnackbar("Could not export backup archive")
                }
            }
        }
    val importBackupLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                return@rememberLauncherForActivityResult
            }

            scope.launch {
                val restored = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            SonoraBackupArchive.importFromStream(context, input)
                        }
                    }.getOrNull()
                }

                if (restored == null) {
                    snackbarHostState.showSnackbar("Could not import backup archive")
                    return@launch
                }

                val currentTracks = tracks
                val currentPlaylists = userPlaylists
                val applied = withContext(Dispatchers.IO) {
                    runCatching {
                        currentTracks.forEach { track ->
                            trackStore.deleteTrackFiles(track)
                        }
                        currentPlaylists.forEach { playlist ->
                            playlist.customCoverPath
                                ?.takeIf { it.isNotBlank() }
                                ?.let { coverPath ->
                                    runCatching { File(coverPath).delete() }
                                }
                        }
                        trackStore.saveTracks(restored.tracks)
                        playlistStore.savePlaylists(restored.playlists)
                        true
                    }.getOrDefault(false)
                }

                if (!applied) {
                    snackbarHostState.showSnackbar("Backup import failed while applying data")
                    return@launch
                }

                tracks = restored.tracks
                userPlaylists = restored.playlists
                miniStreamingInstalledTrackMap = emptyMap()
                settingsStore.saveMiniStreamingInstalledTrackMap(emptyMap())
                settingsStore.clearPlaybackSessionSnapshot()

                restored.settings?.let { restoredSettings ->
                    val updatedSettings = SonoraAppSettings(
                        sliderStyle = PlayerSliderStyle.fromStorage(restoredSettings.sliderStyle),
                        artworkStyle = ArtworkStyle.fromStorage(restoredSettings.artworkStyle),
                        fontStyle = AppFontStyle.fromStorage(restoredSettings.fontStyle),
                        accentHex = normalizeHexColor(restoredSettings.accentHex) ?: DEFAULT_ACCENT_HEX,
                        preservePlayerModes = restoredSettings.preservePlayerModes,
                        trackGapSeconds = nearestTrackGapSecondsOption(restoredSettings.trackGapSeconds),
                        maxStorageMb = nearestMaxStorageOptionMb(restoredSettings.maxStorageMb)
                    )
                    appSettings = updatedSettings
                    settingsStore.save(updatedSettings)
                }

                storageUsageBytes = withContext(Dispatchers.IO) {
                    computeAppStorageUsageBytes(context)
                }
                snackbarHostState.showSnackbar(
                    "Backup imported: ${restored.tracks.size} track(s), ${restored.playlists.size} playlist(s)"
                )
            }
        }

    DisposableEffect(Unit) {
        onDispose {
            playbackController.release()
        }
    }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val tabBarBackground = if (isDark) SonoraTabBarDark else SonoraTabBarLight
    val tabInactiveColor = if (isDark) SonoraTabInactiveDark else SonoraTabInactiveLight

    val isCreateNameScreen = playlistCreateStep == PlaylistCreateStep.Name
    val isCreateTracksScreen = playlistCreateStep == PlaylistCreateStep.Tracks
    val isAddTracksScreen = addTracksTargetPlaylistID != null
    val inOverlayScreen = isCreateNameScreen || isCreateTracksScreen || isAddTracksScreen
    val inSubPage = showHistoryPage || showSettingsPage || showFavoritesPage || showPlaylistsListPage || showMusicPage

    val inMiniStreamingArtistDetail = openedMiniStreamingArtist != null
    val inPlaylistDetail = openedDetailPlaylist != null || inMiniStreamingArtistDetail
    val isRootSearchPage = (selectedTab == SonoraTab.Music) && !inSubPage && !inPlaylistDetail
    val showCompactPlaylistTitle = inPlaylistDetail && (
        playlistDetailListState.firstVisibleItemIndex > 0 ||
            playlistDetailListState.firstVisibleItemScrollOffset > compactPlaylistTitleThreshold
        )
    val activeTrackSelectionContext = when {
        showMusicPage -> TrackSelectionContext.Music
        showFavoritesPage -> TrackSelectionContext.Favorites
        inPlaylistDetail && openedDetailPlaylist?.isUser == true -> TrackSelectionContext.PlaylistDetail
        else -> null
    }
    val isTrackSelectionMode = selectedTrackIDs.isNotEmpty() &&
        trackSelectionContext != null &&
        trackSelectionContext == activeTrackSelectionContext
    val selectedTrackCount = if (isTrackSelectionMode) selectedTrackIDs.size else 0
    val isRootHomePage = !inPlaylistDetail && !inSubPage && selectedTab == SonoraTab.Home

    LaunchedEffect(activeTrackSelectionContext) {
        if (trackSelectionContext != null && trackSelectionContext != activeTrackSelectionContext) {
            clearTrackSelection()
        }
    }

    LaunchedEffect(isTrackSelectionMode) {
        if (isTrackSelectionMode) {
            showSearch = false
        }
    }

    val miniPlayerTrack = playbackController.currentTrack ?: miniStreamingPendingTrack
    val miniPlayerVisible = !inPlaylistDetail &&
        !inOverlayScreen &&
        !inSubPage &&
        !playerVisible &&
        miniPlayerTrack != null
    val listBottomInset = if (miniPlayerVisible) 76.dp else 2.dp

    val title = when {
        isTrackSelectionMode -> "$selectedTrackCount Selected"
        inMiniStreamingArtistDetail -> ""
        inPlaylistDetail -> ""
        showHistoryPage -> "History"
        showSettingsPage -> "Settings"
        showFavoritesPage -> "Favorites"
        showPlaylistsListPage -> "Playlists"
        showMusicPage -> "Music"
        selectedTab == SonoraTab.Home -> "Home"
        selectedTab == SonoraTab.Music -> "Search"
        else -> "Collections"
    }

    val activeSearchQuery = when {
        inPlaylistDetail -> ""
        showMusicPage -> myMusicQuery
        showFavoritesPage -> favoritesQuery
        selectedTab == SonoraTab.Music -> searchQuery
        selectedTab == SonoraTab.Playlists -> playlistsQuery
        else -> ""
    }
    val activeSearchPlaceholder = when {
        showMusicPage || showFavoritesPage -> "Search"
        selectedTab == SonoraTab.Music -> "Search"
        selectedTab == SonoraTab.Playlists -> "Search Collections"
        else -> ""
    }
    val searchVisible = when {
        isTrackSelectionMode -> false
        inPlaylistDetail -> false
        isRootSearchPage -> true
        else -> showSearch || activeSearchQuery.isNotBlank()
    }
    val searchRevealTarget = when {
        inPlaylistDetail -> 0f
        searchVisible -> 1f
        else -> 0f
    }
    val searchRevealProgress by animateFloatAsState(
        targetValue = searchRevealTarget,
        animationSpec = tween(durationMillis = 140),
        label = "searchReveal"
    )

    val onSearchQueryChange: (String) -> Unit = { value ->
        when {
            showMusicPage -> myMusicQuery = value
            showFavoritesPage -> favoritesQuery = value
            else -> when (selectedTab) {
                SonoraTab.Home -> Unit
                SonoraTab.Music -> searchQuery = value
                SonoraTab.Playlists -> playlistsQuery = value
            }
        }
    }

    val onSelectionFavoriteTap: () -> Unit = {
        if (isTrackSelectionMode && selectedTrackIDs.isNotEmpty()) {
            val targetIDs = selectedTrackIDs
            val updated = tracks.map { track ->
                if (targetIDs.contains(track.id) && !track.isFavorite) {
                    track.copy(isFavorite = true)
                } else {
                    track
                }
            }
            persistTracks(updated)
            clearTrackSelection()
            scope.launch {
                snackbarHostState.showSnackbar("Added to favorites")
            }
        }
    }

    val onSelectionDeleteTap: () -> Unit = {
        if (isTrackSelectionMode && selectedTrackIDs.isNotEmpty()) {
            val targetIDs = selectedTrackIDs.toList()
            when (trackSelectionContext) {
                TrackSelectionContext.Music -> {
                    targetIDs.forEach { trackID ->
                        deleteTrackFromLibrary(trackID)
                    }
                    scope.launch {
                        snackbarHostState.showSnackbar("Deleted ${targetIDs.size} track(s)")
                    }
                }

                TrackSelectionContext.Favorites -> {
                    val updated = tracks.map { track ->
                        if (targetIDs.contains(track.id) && track.isFavorite) {
                            track.copy(isFavorite = false)
                        } else {
                            track
                        }
                    }
                    persistTracks(updated)
                    scope.launch {
                        snackbarHostState.showSnackbar("Removed from favorites")
                    }
                }

                TrackSelectionContext.PlaylistDetail -> {
                    targetIDs.forEach { trackID ->
                        removeTrackFromOpenedPlaylist(trackID)
                    }
                    scope.launch {
                        snackbarHostState.showSnackbar("Removed from playlist")
                    }
                }

                null -> Unit
            }
            clearTrackSelection()
        }
    }

    val showScaffoldTopBar = !playerVisible && !inOverlayScreen && !isRootHomePage
    val accentColor = remember(appSettings.accentHex) {
        resolveAccentColor(appSettings.accentHex)
    }
    val tabActiveColor = accentColor

    CompositionLocalProvider(LocalAccentColor provides accentColor) {
        Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (showScaffoldTopBar) {
                Column(
                    modifier = Modifier.statusBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .padding(horizontal = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (inPlaylistDetail || inSubPage) {
                                val showBackButton = false
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    if (showBackButton) {
                                        TextButton(
                                            onClick = {
                                                when {
                                                    inPlaylistDetail -> {
                                                        openedPlaylistID = null
                                                        openedHomeAlbumArtistKey = null
                                                    }

                                                    showHistoryPage -> showHistoryPage = false
                                                    showSettingsPage -> showSettingsPage = false
                                                    showMusicPage -> showMusicPage = false
                                                }
                                            },
                                            modifier = Modifier.align(Alignment.CenterStart)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Image(
                                                    painter = painterResource(R.drawable.ic_chevron_left),
                                                    contentDescription = "Back",
                                                    colorFilter = ColorFilter.tint(if (isDark) Color.White else MaterialTheme.colorScheme.onSurface),
                                                    modifier = Modifier.size(22.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Back",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontSize = 17.sp,
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    color = if (isDark) {
                                                        Color.White
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = title,
                                        style = if (isTrackSelectionMode) {
                                            TextStyle(
                                                fontFamily = SonoraAndroidYSMusicFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 28.sp
                                            )
                                        } else if (showFavoritesPage || showPlaylistsListPage || showMusicPage) {
                                            TextStyle(
                                                fontFamily = SonoraAndroidYSMusicFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 28.sp
                                            )
                                        } else {
                                            MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = if (isTrackSelectionMode) {
                                            Modifier
                                                .align(Alignment.CenterStart)
                                                .padding(start = 8.dp)
                                        } else if (inPlaylistDetail) {
                                            Modifier.align(Alignment.Center)
                                        } else if (showHistoryPage || showSettingsPage) {
                                            Modifier.align(Alignment.Center)
                                        } else {
                                            Modifier
                                                .align(Alignment.CenterStart)
                                                .padding(start = 8.dp)
                                        }
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = title,
                                        style = if (selectedTab == SonoraTab.Home) {
                                            TextStyle(
                                                fontFamily = SonoraAndroidYSMusicFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 30.sp
                                            )
                                        } else {
                                            MaterialTheme.typography.headlineMedium.copy(
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                when {
                                    isTrackSelectionMode -> {
                                        AppIconButton(
                                            iconRes = R.drawable.heart_fill,
                                            contentDescription = "Favorite selected",
                                            iconWidth = 20.dp,
                                            iconHeight = 20.dp,
                                            tint = Color(0xFFFF5966),
                                            onClick = onSelectionFavoriteTap
                                        )
                                        AppIconButton(
                                            iconRes = R.drawable.ic_global_trash_fill,
                                            contentDescription = "Delete selected",
                                            iconWidth = 20.dp,
                                            iconHeight = 20.dp,
                                            tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                            onClick = onSelectionDeleteTap
                                        )
                                        AppVectorIconButton(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Cancel selection",
                                            tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                            onClick = { clearTrackSelection() }
                                        )
                                    }

                                    inPlaylistDetail && (openedDetailPlaylist?.isUser == true || openedDetailPlaylist?.isSharedOnline == true) -> {
                                        AppIconButton(
                                            iconRes = R.drawable.ic_global_ellipsis,
                                            contentDescription = "Playlist options",
                                            iconWidth = 22.dp,
                                            iconHeight = 22.dp,
                                            tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                            onClick = {
                                                showPlaylistOptionsDialog = true
                                            }
                                        )
                                    }

                                    inSubPage && showMusicPage -> {
                                        AppIconButton(
                                            iconRes = R.drawable.ic_global_plus,
                                            contentDescription = "Add music",
                                            iconWidth = 17.dp,
                                            iconHeight = 17.dp,
                                            tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                            onClick = { addMusicLauncher.launch(arrayOf("audio/*")) }
                                        )
                                        AppIconButton(
                                            iconRes = R.drawable.ic_global_magnifyingglass,
                                            contentDescription = "Search",
                                            iconWidth = 18.dp,
                                            iconHeight = 18.dp,
                                            tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                            onClick = {
                                                showSearch = !(showSearch && myMusicQuery.isBlank())
                                                pullRevealDistance = 0f
                                                pullHideDistance = 0f
                                            }
                                        )
                                    }

                                    inSubPage && showFavoritesPage -> {
                                        AppIconButton(
                                            iconRes = R.drawable.ic_global_magnifyingglass,
                                            contentDescription = "Search",
                                            iconWidth = 18.dp,
                                            iconHeight = 18.dp,
                                            tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                            onClick = {
                                                showSearch = true
                                                pullRevealDistance = 0f
                                                pullHideDistance = 0f
                                            }
                                        )
                                    }

                                    inSubPage && showPlaylistsListPage -> {
                                        AppIconButton(
                                            iconRes = R.drawable.ic_global_plus,
                                            contentDescription = "Create playlist",
                                            iconWidth = 17.dp,
                                            iconHeight = 17.dp,
                                            tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                            onClick = {
                                                playlistCreateStep = PlaylistCreateStep.Name
                                                playlistCreateName = ""
                                                playlistCreateQuery = ""
                                                playlistCreateSelectedTrackIDs = emptySet()
                                            }
                                        )
                                    }

                                    !inPlaylistDetail && !inSubPage && selectedTab == SonoraTab.Home -> {
                                        if (tracks.isNotEmpty()) {
                                            AppIconButton(
                                                iconRes = R.drawable.ic_global_clock,
                                                contentDescription = "History",
                                                iconWidth = 18.dp,
                                                iconHeight = 18.dp,
                                                tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                                onClick = {
                                                    showHistoryPage = true
                                                }
                                            )
                                        }
                                    }

                                }
                            }
                        }

                        if (inPlaylistDetail && showCompactPlaylistTitle) {
                            Text(
                                text = if (inMiniStreamingArtistDetail) {
                                    openedMiniStreamingArtist?.name ?: "Artist"
                                } else {
                                    openedDetailPlaylist?.name ?: "Playlist"
                                },
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    val canShowSearchField = !inPlaylistDetail && (!inSubPage || showMusicPage || showFavoritesPage || showPlaylistsListPage)
                    if (canShowSearchField && searchRevealProgress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp * searchRevealProgress)
                                .graphicsLayer {
                                    alpha = searchRevealProgress
                                    clip = true
                                }
                        ) {
                            SearchField(
                                value = activeSearchQuery,
                                onValueChange = onSearchQueryChange,
                                placeholder = activeSearchPlaceholder
                            )
                        }
                    }

                    if (!isRootHomePage) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (!inPlaylistDetail && !inOverlayScreen && !inSubPage && !playerVisible) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(tabBarBackground)
                            .navigationBarsPadding()
                    ) {
                        NavigationBar(
                            containerColor = tabBarBackground,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(49.dp),
                            windowInsets = WindowInsets(0, 0, 0, 0)
                        ) {
                            tabSpecs.forEach { spec ->
                                val selected = selectedTab == spec.tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        if (spec.tab == SonoraTab.Music) {
                                            showMusicPage = false
                                        }
                                        selectedTab = spec.tab
                                        showHistoryPage = false
                                        showSettingsPage = false
                                        showFavoritesPage = false
                                        showPlaylistsListPage = false
                                        openedPlaylistID = null
                                        openedHomeAlbumArtistKey = null
                                        openedMiniStreamingArtist = null
                                        openedMiniStreamingArtistTracks = emptyList()
                                    },
                                    icon = {
                                        val iconSize = if (spec.tab == SonoraTab.Home) 22.dp else 20.dp
                                        Icon(
                                            painter = painterResource(spec.iconRes),
                                            contentDescription = spec.title,
                                            modifier = Modifier.size(iconSize),
                                            tint = if (selected) tabActiveColor else tabInactiveColor
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = Color.Transparent
                                    ),
                                    alwaysShowLabel = false
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .nestedScroll(nestedScrollConnection)
        ) {
            when {
                isCreateNameScreen -> PlaylistCreateNamePage(
                    name = playlistCreateName,
                    onNameChange = { value ->
                        playlistCreateName = value.take(32)
                    },
                    onBack = {
                        playlistCreateStep = null
                        playlistCreateName = ""
                        playlistCreateQuery = ""
                        playlistCreateSelectedTrackIDs = emptySet()
                    },
                    onNext = {
                        val normalized = playlistCreateName.trim()
                        if (normalized.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Name required")
                            }
                        } else if (tracksByAffinity.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("No music in library")
                            }
                        } else {
                            playlistCreateName = normalized
                            playlistCreateStep = PlaylistCreateStep.Tracks
                        }
                    }
                )

                isCreateTracksScreen -> PlaylistCreateTrackPickerPage(
                    allTracks = tracksByAffinity,
                    filteredTracks = createTrackFiltered,
                    query = playlistCreateQuery,
                    selectedTrackIDs = playlistCreateSelectedTrackIDs,
                    onQueryChange = { playlistCreateQuery = it },
                    onToggleTrack = { trackID ->
                        playlistCreateSelectedTrackIDs = if (playlistCreateSelectedTrackIDs.contains(trackID)) {
                            playlistCreateSelectedTrackIDs - trackID
                        } else {
                            playlistCreateSelectedTrackIDs + trackID
                        }
                    },
                    onBack = {
                        playlistCreateStep = PlaylistCreateStep.Name
                    },
                    onCreate = {
                        if (playlistCreateSelectedTrackIDs.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Select at least one track")
                            }
                        } else {
                            val created = playlistStore.createPlaylist(playlistCreateName.trim())
                            if (created == null) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Could not create playlist")
                                }
                            } else {
                                val orderedTrackIDs = tracksByAffinity
                                    .map { it.id }
                                    .filter { playlistCreateSelectedTrackIDs.contains(it) }
                                playlistStore.replaceTrackIds(created.id, orderedTrackIDs)
                                reloadPlaylists()
                                openedPlaylistID = created.id

                                playlistCreateStep = null
                                playlistCreateName = ""
                                playlistCreateQuery = ""
                                playlistCreateSelectedTrackIDs = emptySet()
                            }
                        }
                    }
                )

                isAddTracksScreen -> PlaylistAddTracksPage(
                    tracks = addTracksAvailable,
                    selectedTrackIDs = addTracksSelectedIDs,
                    onToggleTrack = { trackID ->
                        addTracksSelectedIDs = if (addTracksSelectedIDs.contains(trackID)) {
                            addTracksSelectedIDs - trackID
                        } else {
                            addTracksSelectedIDs + trackID
                        }
                    },
                    onBack = {
                        addTracksTargetPlaylistID = null
                        addTracksSelectedIDs = emptySet()
                    },
                    onAdd = {
                        val playlistID = addTracksTargetPlaylistID
                        if (playlistID != null && addTracksSelectedIDs.isNotEmpty()) {
                            playlistStore.addTrackIds(playlistID, addTracksSelectedIDs.toList())
                            reloadPlaylists()
                        }
                        addTracksTargetPlaylistID = null
                        addTracksSelectedIDs = emptySet()
                    }
                )

                inPlaylistDetail -> SwipeDismissPage(
                    onDismiss = {
                        clearTrackSelection()
                        openedPlaylistID = null
                        openedHomeAlbumArtistKey = null
                        openedMiniStreamingArtist = null
                        openedMiniStreamingArtistTracks = emptyList()
                    }
                ) {
                    val openedArtist = openedMiniStreamingArtist
                    if (openedArtist != null) {
                        val currentMiniStreamingTrackId =
                            miniStreamingTrackIdFromPlaybackId(playbackController.currentTrackId)
                        MiniStreamingArtistPage(
                            listState = playlistDetailListState,
                            artist = openedArtist,
                            tracks = openedMiniStreamingArtistTracks,
                            isLoading = openedMiniStreamingArtistLoading,
                            currentTrackId = currentMiniStreamingTrackId,
                            isPlaying = playbackController.isPlaying,
                            isSleepTimerActive = playbackController.isSleepTimerActive,
                            resolvingTrackIds = miniStreamingResolvingTrackIds,
                            installingTrackIds = miniStreamingInstallingTrackIds,
                            queuePositionsByTrackId = miniStreamingQueuePositions,
                            canLoadMore = openedMiniStreamingArtistHasMore,
                            isLoadingMore = openedMiniStreamingArtistLoadingMore,
                            onPlayTap = {
                                val firstTrack = openedMiniStreamingArtistTracks.firstOrNull()
                                if (firstTrack != null) {
                                    playMiniStreamingTrack(
                                        track = firstTrack,
                                        queue = openedMiniStreamingArtistTracks,
                                        startIndex = 0
                                    )
                                }
                            },
                            onShuffleTap = {
                                val shuffled = openedMiniStreamingArtistTracks.shuffled()
                                val firstTrack = shuffled.firstOrNull()
                                if (firstTrack != null) {
                                    playMiniStreamingTrack(
                                        track = firstTrack,
                                        queue = shuffled,
                                        startIndex = 0
                                    )
                                }
                            },
                            onSleepTap = {
                                showSleepTimerDialog = true
                            },
                            onLoadMore = {
                                requestMoreOpenedMiniStreamingArtistTracks()
                            },
                            onTrackTap = { tapped, index ->
                                playMiniStreamingTrack(
                                    track = tapped,
                                    queue = openedMiniStreamingArtistTracks,
                                    startIndex = index
                                )
                            }
                        )
                    } else {
                        PlaylistDetailPage(
                            listState = playlistDetailListState,
                            playlist = openedDetailPlaylist,
                            tracks = openedDetailTracks,
                            canRemoveTracks = openedPlaylist?.isUser == true,
                            currentTrackID = playbackController.currentTrackId,
                            isPlaying = playbackController.isPlaying,
                            isCurrentQueueMatching = playbackController.isQueueMatching(openedDetailTracks),
                            isSleepTimerActive = playbackController.isSleepTimerActive,
                            selectionMode = isTrackSelectionMode &&
                                trackSelectionContext == TrackSelectionContext.PlaylistDetail,
                            selectedTrackIDs = selectedTrackIDs,
                            onTrackTap = { tapped ->
                                if (isTrackSelectionMode &&
                                    trackSelectionContext == TrackSelectionContext.PlaylistDetail
                                ) {
                                    toggleTrackSelection(
                                        context = TrackSelectionContext.PlaylistDetail,
                                        trackID = tapped.id
                                    )
                                } else {
                                    playbackController.playOrToggleFromQueue(
                                        queue = openedDetailTracks,
                                        targetTrackId = tapped.id
                                    )
                                    playerVisible = true
                                }
                            },
                            onHeaderPlayPauseTap = {
                                if (openedDetailTracks.isNotEmpty()) {
                                    if (playbackController.isQueueMatching(openedDetailTracks) &&
                                        playbackController.currentTrackId != null
                                    ) {
                                        playbackController.togglePlayPause()
                                    } else {
                                        playbackController.playOrToggleFromQueue(
                                            queue = openedDetailTracks,
                                            targetTrackId = openedDetailTracks.first().id
                                        )
                                    }
                                }
                            },
                            onHeaderShuffleTap = {
                                if (openedDetailTracks.isNotEmpty()) {
                                    val randomTrack = openedDetailTracks.randomOrNull()
                                    if (randomTrack != null) {
                                        playbackController.playOrToggleFromQueue(
                                            queue = openedDetailTracks,
                                            targetTrackId = randomTrack.id
                                        )
                                        if (!playbackController.isShuffleEnabled) {
                                            playbackController.toggleShuffleEnabled()
                                        }
                                    }
                                }
                            },
                            onHeaderSleepTap = { showSleepTimerDialog = true },
                            onTrackLongPress = { track ->
                                if (openedDetailPlaylist?.isUser == true) {
                                    toggleTrackSelection(
                                        context = TrackSelectionContext.PlaylistDetail,
                                        trackID = track.id,
                                        forceSelect = true
                                    )
                                } else {
                                    onFavoriteToggle(track.id)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Favorite state updated")
                                    }
                                }
                            },
                            onTrackSwipeFavorite = { track ->
                                onFavoriteToggle(track.id)
                                scope.launch {
                                    val message = if (track.isFavorite) {
                                        "Removed from favorites"
                                    } else {
                                        "Added to favorites"
                                    }
                                    snackbarHostState.showSnackbar(message)
                                }
                            },
                            onTrackSwipeRemove = { track ->
                                removeTrackFromOpenedPlaylist(track.id)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Removed from playlist")
                                }
                            }
                        )
                    }
                }

                showHistoryPage -> SwipeDismissPage(onDismiss = { showHistoryPage = false }) {
                    HistoryPage(
                        listState = historyListState,
                        tracks = historyTracks,
                        bottomInset = listBottomInset,
                        currentTrackID = playbackController.currentTrackId,
                        isPlaying = playbackController.isPlaying,
                        onTrackTap = { tapped ->
                            playbackController.playOrToggleFromQueue(
                                queue = historyTracks,
                                targetTrackId = tapped.id
                            )
                            playerVisible = true
                        }
                    )
                }

                showSettingsPage -> SwipeDismissPage(onDismiss = { showSettingsPage = false }) {
                    SettingsPage(
                        settings = appSettings,
                        storageUsedBytes = storageUsageBytes,
                        onlinePlaylistCacheUsedBytes = sharedPlaylistAudioCacheUsageBytes,
                        appUpdateState = androidAppUpdateState,
                        appVersionLabel = appVersionLabel,
                        githubProjectLabel = githubProjectLabel,
                        storageRootPath = storageRootPath,
                        libraryTrackCount = tracks.size,
                        onSettingsChange = { updated ->
                            appSettings = updated
                            settingsStore.save(updated)
                        },
                        onClearOnlinePlaylistCache = {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    sharedPlaylistStore.trimAudioCache(0L)
                                }
                                reloadSharedPlaylists()
                            }
                        },
                        onCheckForUpdates = {
                            scope.launch {
                                androidAppUpdateState = androidAppUpdateState.copy(
                                    checking = true,
                                    statusMessage = "Checking..."
                                )
                                val fetched = withContext(Dispatchers.IO) {
                                    fetchLatestAndroidAppUpdate(
                                        baseUrl = BuildConfig.BACKEND_BASE_URL.ifBlank { "https://api.corebrew.ru" },
                                        currentVersionCode = appVersionCode
                                    )
                                }
                                val baseState = fetched ?: AndroidAppUpdateState(
                                    checking = false,
                                    latestRelease = androidAppUpdateState.latestRelease,
                                    updateAvailable = false,
                                    statusMessage = "Could not check for updates"
                                )
                                val snapshot = withContext(Dispatchers.IO) {
                                    readAndroidAppUpdateDownloadSnapshot(context)
                                }
                                androidAppUpdateState = mergeAndroidAppUpdateState(baseState, snapshot)
                                if (fetched?.updateAvailable == true) {
                                    snackbarHostState.showSnackbar("Update found")
                                } else if (fetched != null) {
                                    snackbarHostState.showSnackbar(fetched.statusMessage ?: "No updates")
                                }
                            }
                        },
                        onInstallUpdate = { release ->
                            scope.launch {
                                val readyPath = androidAppUpdateState.downloadedApkPath
                                if (androidAppUpdateState.downloadReady && !readyPath.isNullOrBlank()) {
                                    val installerOpened = installDownloadedApk(uiContext, File(readyPath))
                                    androidAppUpdateState = androidAppUpdateState.copy(
                                        statusMessage = if (installerOpened) {
                                            "Installer opened"
                                        } else {
                                            "Could not open installer"
                                        }
                                    )
                                    if (!installerOpened) {
                                        snackbarHostState.showSnackbar("Could not open installer")
                                    }
                                } else {
                                    val snapshot = withContext(Dispatchers.IO) {
                                        enqueueAndroidAppUpdateDownload(context, release)
                                    }
                                    if (snapshot == null) {
                                        androidAppUpdateState = androidAppUpdateState.copy(
                                            downloading = false,
                                            downloadProgress = 0f,
                                            statusMessage = "Could not start download"
                                        )
                                        snackbarHostState.showSnackbar("Could not start download")
                                    } else {
                                        androidAppUpdateState = mergeAndroidAppUpdateState(
                                            androidAppUpdateState.copy(
                                                latestRelease = release,
                                                statusMessage = "Downloading ${release.versionName}..."
                                            ),
                                            snapshot
                                        )
                                        snackbarHostState.showSnackbar("Update download started in background")
                                    }
                                }
                            }
                        },
                        onCancelUpdateDownload = {
                            scope.launch {
                                val canceled = withContext(Dispatchers.IO) {
                                    cancelAndroidAppUpdateDownload(context, androidAppUpdateState.downloadId)
                                }
                                androidAppUpdateState = androidAppUpdateState.copy(
                                    downloading = false,
                                    downloadProgress = 0f,
                                    downloadId = null,
                                    downloadedApkPath = null,
                                    downloadReady = false,
                                    statusMessage = if (canceled) "Update download canceled" else "No active update download"
                                )
                                withContext(Dispatchers.IO) {
                                    clearAndroidAppUpdateDownloadState(context)
                                }
                            }
                        },
                        onOpenGithub = {
                            openExternalUrl(uiContext, githubProjectURL)
                        },
                        onExportBackup = {
                            exportBackupLauncher.launch(buildBackupArchiveFileName())
                        },
                        onImportBackup = {
                            importBackupLauncher.launch(arrayOf("*/*"))
                        }
                    )
                }

                showFavoritesPage -> SwipeDismissPage(onDismiss = { showFavoritesPage = false }) {
                    FavoritesPage(
                        listState = favoritesListState,
                        allFavorites = favoriteTracks,
                        filteredTracks = favoritesFiltered,
                        bottomInset = listBottomInset,
                        currentTrackID = playbackController.currentTrackId,
                        isPlaying = playbackController.isPlaying,
                        selectionMode = isTrackSelectionMode &&
                            trackSelectionContext == TrackSelectionContext.Favorites,
                        selectedTrackIDs = selectedTrackIDs,
                        onTrackTap = { tapped ->
                            if (isTrackSelectionMode &&
                                trackSelectionContext == TrackSelectionContext.Favorites
                            ) {
                                toggleTrackSelection(
                                    context = TrackSelectionContext.Favorites,
                                    trackID = tapped.id
                                )
                            } else {
                                playbackController.playOrToggleFromQueue(
                                    queue = favoriteTracks,
                                    targetTrackId = tapped.id
                                )
                                playerVisible = true
                            }
                        },
                        onTrackLongPress = { track ->
                            toggleTrackSelection(
                                context = TrackSelectionContext.Favorites,
                                trackID = track.id,
                                forceSelect = true
                            )
                        },
                        onTrackSwipeUnfavorite = { track ->
                            onFavoriteToggle(track.id)
                            scope.launch {
                                snackbarHostState.showSnackbar("Removed from favorites")
                            }
                        },
                        onTrackSwipeAddToPlaylist = { track ->
                            quickAddTrackID = track.id
                        }
                    )
                }

                showPlaylistsListPage -> SwipeDismissPage(onDismiss = { showPlaylistsListPage = false }) {
                    PlaylistsListPage(
                        listState = playlistsBrowseListState,
                        playlists = allPlaylists,
                        trackByID = trackByID,
                        bottomInset = listBottomInset,
                        onPlaylistTap = { playlist ->
                            showPlaylistsListPage = false
                            openedPlaylistID = playlist.id
                            openedHomeAlbumArtistKey = null
                            openedMiniStreamingArtist = null
                        },
                        onCreatePlaylistTap = {
                            playlistCreateStep = PlaylistCreateStep.Name
                            playlistCreateName = ""
                            playlistCreateQuery = ""
                            playlistCreateSelectedTrackIDs = emptySet()
                        }
                    )
                }

                tracks.isEmpty() && selectedTab != SonoraTab.Music -> FirstMusicOnboardingPage(
                    onAddMusicTap = {
                        addMusicLauncher.launch(arrayOf("audio/*"))
                    }
                )

                selectedTab == SonoraTab.Home -> HomePage(
                    listState = homeListState,
                    waveQueue = homeWaveTracks,
                    waveStartTrack = homeWaveDisplayTrack,
                    waveLook = appSettings.myWaveLook,
                    tasteTracks = homeTasteTracks,
                    freshChoiceTracks = homeFreshChoiceTracks,
                    isWavePlaying = !playbackController.currentTrackId.isNullOrBlank() &&
                        homeWaveTracks.any { it.id == playbackController.currentTrackId } &&
                        playbackController.isPlaying,
                    topInset = 52.dp,
                    bottomInset = listBottomInset,
                    onWaveToggleTap = {
                        val waveTarget = homeWaveDisplayTrack ?: homeWaveStartTrack ?: homeWaveTracks.firstOrNull()
                        val liveTrackID = playbackController.currentTrackId
                        val currentInWave = !liveTrackID.isNullOrBlank() &&
                            homeWaveTracks.any { it.id == liveTrackID }
                        if (waveTarget != null) {
                            if (currentInWave) {
                                playbackController.togglePlayPause()
                            } else {
                                playbackController.playOrToggleFromQueue(
                                    queue = homeWaveTracks,
                                    targetTrackId = waveTarget.id
                                )
                                playerVisible = true
                            }
                        }
                    },
                    onTasteTap = { tapped ->
                        playbackController.playOrToggleFromQueue(
                            queue = homeTasteTracks,
                            targetTrackId = tapped.id
                        )
                        playerVisible = true
                    },
                    onFreshChoiceTap = { tapped ->
                        playbackController.playOrToggleFromQueue(
                            queue = homeFreshChoiceTracks,
                            targetTrackId = tapped.id
                        )
                        playerVisible = true
                    }
                )

                showMusicPage -> SwipeDismissPage(onDismiss = { showMusicPage = false }) {
                    MusicPage(
                    listState = musicListState,
                    tracks = tracks,
                    filteredTracks = myMusicFiltered,
                    bottomInset = listBottomInset,
                    currentTrackID = playbackController.currentTrackId,
                    isPlaying = playbackController.isPlaying,
                    selectionMode = isTrackSelectionMode &&
                        trackSelectionContext == TrackSelectionContext.Music,
                    selectedTrackIDs = selectedTrackIDs,
                    onTrackTap = { tapped ->
                        if (isTrackSelectionMode &&
                            trackSelectionContext == TrackSelectionContext.Music
                        ) {
                            toggleTrackSelection(
                                context = TrackSelectionContext.Music,
                                trackID = tapped.id
                            )
                        } else {
                            playbackController.playOrToggleFromQueue(
                                queue = tracks,
                                targetTrackId = tapped.id
                            )
                            playerVisible = true
                        }
                    },
                    onTrackLongPress = { track ->
                        toggleTrackSelection(
                            context = TrackSelectionContext.Music,
                            trackID = track.id,
                            forceSelect = true
                        )
                    },
                    onTrackSwipeFavoriteToggle = { track ->
                        onFavoriteToggle(track.id)
                        scope.launch {
                            val message = if (track.isFavorite) {
                                "Removed from favorites"
                            } else {
                                "Added to favorites"
                            }
                            snackbarHostState.showSnackbar(message)
                        }
                    },
                    onTrackSwipeDelete = { track ->
                        deleteTrackFromLibrary(track.id)
                        scope.launch {
                            snackbarHostState.showSnackbar("Track deleted")
                        }
                    },
                    onTrackSwipeAddToPlaylist = { track ->
                        quickAddTrackID = track.id
                    }
                )
                }

                selectedTab == SonoraTab.Music -> SearchPage(
                    listState = musicListState,
                    searchQuery = searchQuery,
                    localPlaylistResults = localSearchPlaylistsFiltered,
                    localArtistResults = localSearchArtistsFiltered,
                    onlineTrackResults = onlineSearchTracksForSearch,
                    onlineArtistResults = onlineSearchArtistsForSearch,
                    musicResults = localSearchTracksForSearch,
                    onlineTracksLoading = miniStreamingTracksLoading,
                    onlineArtistsLoading = miniStreamingArtistsLoading,
                    onlineArtistsVisible = miniStreamingArtistsSectionVisible,
                    onlineConfigured = miniStreamingClient.isConfigured(),
                    currentLocalTrackId = playbackController.currentTrackId
                        ?.takeUnless { it.startsWith(MINI_STREAMING_PLAYBACK_PREFIX) },
                    currentOnlineTrackId = miniStreamingTrackIdFromPlaybackId(playbackController.currentTrackId),
                    isPlaying = playbackController.isPlaying,
                    onlineResolvingTrackIds = miniStreamingResolvingTrackIds,
                    onlineInstallingTrackIds = miniStreamingInstallingTrackIds,
                    onlineQueuePositionsByTrackId = miniStreamingQueuePositions,
                    trackByID = trackByID,
                    bottomInset = listBottomInset,
                    onMusicTrackTap = { tapped ->
                        playbackController.playOrToggleFromQueue(
                            queue = tracks,
                            targetTrackId = tapped.id
                        )
                        playerVisible = true
                    },
                    onOnlineTrackTap = { tapped, index ->
                        playMiniStreamingTrack(
                            track = tapped,
                            queue = onlineSearchTracksForSearch,
                            startIndex = index
                        )
                    },
                    onLocalPlaylistTap = { playlist ->
                        openedPlaylistID = playlist.id
                        openedMiniStreamingArtist = null
                        openedHomeAlbumArtistKey = null
                    },
                    onLocalArtistTap = { artist ->
                        searchQuery = artist.title
                        showSearch = true
                    },
                    onOnlineArtistTap = { artist ->
                        openedMiniStreamingArtist = artist
                        openedMiniStreamingArtistTracks = emptyList()
                        openedPlaylistID = null
                        openedHomeAlbumArtistKey = null
                    }
                )

                selectedTab == SonoraTab.Playlists -> PlaylistsPage(
                    listState = playlistsListState,
                    playlists = playlistsFiltered,
                    trackByID = trackByID,
                    myMusicTracks = tracksByAffinity.take(14),
                    favorites = favoriteTracks,
                    lastAddedTracks = homeLastAdded,
                    albums = homeAlbums,
                    bottomInset = listBottomInset,
                    currentTrackID = playbackController.currentTrackId,
                    isPlaying = playbackController.isPlaying,
                    onFavoriteSummaryTap = {
                        showFavoritesPage = true
                    },
                    onFavoriteTrackTap = { tapped ->
                        playbackController.playOrToggleFromQueue(
                            queue = favoriteTracks,
                            targetTrackId = tapped.id
                        )
                        playerVisible = true
                    },
                    onPlaylistTap = { playlist ->
                        openedPlaylistID = playlist.id
                        openedMiniStreamingArtist = null
                    },
                    onLastAddedTap = { tapped ->
                        playbackController.playOrToggleFromQueue(
                            queue = homeLastAdded,
                            targetTrackId = tapped.id
                        )
                        playerVisible = true
                    },
                    onAlbumTap = { album ->
                        openedHomeAlbumArtistKey = album.artistKey
                        openedPlaylistID = null
                        openedMiniStreamingArtist = null
                    },
                    onCreatePlaylistTap = {
                        playlistCreateStep = PlaylistCreateStep.Name
                        playlistCreateName = ""
                        playlistCreateQuery = ""
                        playlistCreateSelectedTrackIDs = emptySet()
                    },
                    onMyMusicTap = {
                        showMusicPage = true
                        showSearch = false
                    },
                    onMyMusicTrackTap = { tapped ->
                        playbackController.playOrToggleFromQueue(
                            queue = tracks,
                            targetTrackId = tapped.id
                        )
                        playerVisible = true
                    },
                    onPlaylistsHeaderTap = {
                        showPlaylistsListPage = true
                    }
                )
            }

            if (isRootHomePage && !playerVisible && !inOverlayScreen) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(3f)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(44.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Home",
                                style = TextStyle(
                                    fontFamily = SonoraAndroidYSMusicFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 30.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (tracks.isNotEmpty()) {
                                AppIconButton(
                                    iconRes = R.drawable.ic_global_clock,
                                    contentDescription = "History",
                                    iconWidth = 18.dp,
                                    iconHeight = 18.dp,
                                    tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                    onClick = { showHistoryPage = true }
                                )
                            }
                            AppIconButton(
                                iconRes = R.drawable.ic_global_settings,
                                contentDescription = "Settings",
                                iconWidth = 18.dp,
                                iconHeight = 18.dp,
                                tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                onClick = { showSettingsPage = true }
                            )
                        }
                    }
                }
            }

            val activeMiniTrack = miniPlayerTrack
            val visiblePlaybackId = playbackController.currentTrackId ?: miniStreamingPendingTrack?.id
            val visibleMiniTrackId = miniStreamingTrackIdFromPlaybackId(visiblePlaybackId)
            val visibleMiniTrackIndex = if (visibleMiniTrackId.isNullOrBlank()) {
                -1
            } else {
                miniStreamingPlaybackQueue.indexOfFirst { it.trackId == visibleMiniTrackId }
            }
            val miniStreamingQueueCanStep = visibleMiniTrackIndex >= 0 &&
                miniStreamingPlaybackQueue.size > 1
            val playerHasQueue = playbackController.queueCount > 1 || miniStreamingQueueCanStep
            val playerCanStep = playbackController.canStep || miniStreamingQueueCanStep
            val miniStreamingNextPreviewTrack: TrackItem? = if (
                visibleMiniTrackIndex >= 0 &&
                visibleMiniTrackIndex + 1 < miniStreamingPlaybackQueue.size
            ) {
                val nextMiniTrack = miniStreamingPlaybackQueue[visibleMiniTrackIndex + 1]
                TrackItem(
                    id = miniStreamingPlaybackIdForTrack(nextMiniTrack.trackId),
                    title = nextMiniTrack.title.ifBlank { "Track" },
                    artist = nextMiniTrack.artists.ifBlank { "Spotify" },
                    durationMs = nextMiniTrack.durationMs,
                    filePath = "",
                    artworkPath = nextMiniTrack.artworkUrl,
                    addedAt = System.currentTimeMillis(),
                    isFavorite = false
                )
            } else {
                null
            }

            val onMiniStreamingPreviousOrDefault: () -> Unit = {
                val currentIndex = visibleMiniTrackIndex
                if (currentIndex > 0 && currentIndex < miniStreamingPlaybackQueue.size) {
                    val previousTrack = miniStreamingPlaybackQueue[currentIndex - 1]
                    playMiniStreamingTrack(
                        track = previousTrack,
                        queue = miniStreamingPlaybackQueue,
                        startIndex = currentIndex - 1
                    )
                } else {
                    playbackController.playPreviousFromUser()
                }
            }

            val onMiniStreamingNextOrDefault: () -> Unit = {
                val currentIndex = visibleMiniTrackIndex
                if (currentIndex >= 0 && currentIndex + 1 < miniStreamingPlaybackQueue.size) {
                    val nextTrack = miniStreamingPlaybackQueue[currentIndex + 1]
                    playMiniStreamingTrack(
                        track = nextTrack,
                        queue = miniStreamingPlaybackQueue,
                        startIndex = currentIndex + 1
                    )
                } else {
                    playbackController.playNextFromUser()
                }
            }

            if (miniPlayerVisible && activeMiniTrack != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    MiniPlayer(
                        modifier = Modifier,
                        track = activeMiniTrack,
                        hasQueue = playerHasQueue,
                        isPlaying = playbackController.isPlaying,
                        canStep = playerCanStep,
                        onOpen = { playerVisible = true },
                        onTogglePlayPause = { playbackController.togglePlayPause() },
                        onPrevious = onMiniStreamingPreviousOrDefault,
                        onNext = onMiniStreamingNextOrDefault
                    )
                }
            }

            val playerTrack = playbackController.currentTrack ?: miniStreamingPendingTrack
            if (playerVisible && playerTrack != null) {
                val isFavorite = tracks.firstOrNull { it.id == playerTrack.id }?.isFavorite
                    ?: playerTrack.isFavorite
                    PlayerView(
                        modifier = Modifier.fillMaxSize(),
                        track = playerTrack,
                        hasQueue = playerHasQueue,
                        isPlaying = playbackController.isPlaying,
                        canStep = playerCanStep,
                    isFavorite = isFavorite,
                    isShuffleEnabled = playbackController.isShuffleEnabled,
                    repeatMode = playbackController.repeatMode,
                    isSleepTimerActive = playbackController.isSleepTimerActive,
                    sleepTimerRemainingMs = playbackController.sleepTimerRemainingMs,
                    nextTrack = miniStreamingNextPreviewTrack ?: playbackController.predictedNextTrackForSkip(),
                    useWaveSlider = appSettings.sliderStyle == PlayerSliderStyle.Wave,
                    artworkStyle = appSettings.artworkStyle,
                    accentColor = resolveAccentColor(appSettings.accentHex),
                    preferredFontFamily = resolveSettingsFontFamily(appSettings.fontStyle),
                        onClose = { playerVisible = false },
                        onTogglePlayPause = { playbackController.togglePlayPause() },
                        onPrevious = onMiniStreamingPreviousOrDefault,
                        onNext = onMiniStreamingNextOrDefault,
                    onToggleShuffle = { playbackController.toggleShuffleEnabled() },
                    onCycleRepeat = { playbackController.cycleRepeatMode() },
                    onSleepTimerTap = { showSleepTimerDialog = true },
                    onToggleFavorite = {
                        if (!playerTrack.id.startsWith(MINI_STREAMING_PLAYBACK_PREFIX)) {
                            onFavoriteToggle(playerTrack.id)
                        }
                    },
                    onSeek = { positionMs -> playbackController.seekTo(positionMs) },
                    positionMsProvider = { playbackController.currentPositionMs() },
                    durationMsProvider = { playbackController.durationMs() }
                )
            }
        }
    }

    if (showPlaylistOptionsDialog && (openedDetailPlaylist?.isUser == true || openedDetailPlaylist?.isSharedOnline == true)) {
        AlertDialog(
            onDismissRequest = { showPlaylistOptionsDialog = false },
            title = {
                Text(
                    text = openedDetailPlaylist?.name ?: "Playlist",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (openedDetailPlaylist?.isSharedOnline == true) {
                        val sharedEntry = openedSharedPlaylist
                        if (sharedEntry != null) {
                            val isLiked = likedSharedPlaylists.any { it.remoteId == sharedEntry.remoteId }
                            AccentTextButton(
                                onClick = {
                                    if (isLiked) {
                                        openedTransientSharedPlaylist = sharedEntry
                                        sharedPlaylistStore.removeByRemoteId(sharedEntry.remoteId)
                                        reloadSharedPlaylists()
                                    } else {
                                        scope.launch {
                                            val cachedEntry = withContext(Dispatchers.IO) {
                                                sharedPlaylistStore.upsert(sharedEntry)
                                                sharedPlaylistStore.cacheAssets(
                                                    sharedEntry,
                                                    cacheAudio = appSettings.cacheOnlinePlaylistTracks,
                                                    audioLimitBytes = if (appSettings.onlinePlaylistCacheMaxMb > 0) {
                                                        appSettings.onlinePlaylistCacheMaxMb.toLong() * 1_048_576L
                                                    } else {
                                                        Long.MAX_VALUE
                                                    },
                                                    onProgress = { partial ->
                                                        mainHandler.post {
                                                            applySharedPlaylistProgressUpdate(partial)
                                                        }
                                                    }
                                                )
                                            }
                                            reloadSharedPlaylists()
                                            openedTransientSharedPlaylist = cachedEntry
                                        }
                                    }
                                    showPlaylistOptionsDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (isLiked) "Remove from Collections" else "Add to Collections")
                            }
                            AccentTextButton(
                                onClick = {
                                    showPlaylistOptionsDialog = false
                                    scope.launch {
                                        sharedPlaylistImportingMessage = "Saving tracks to library..."
                                        sharedPlaylistImporting = true
                                        val localTracksSnapshot = tracks
                                        val (importedTrackIds, downloadedCoverPath) = withContext(Dispatchers.IO) {
                                            importSharedPlaylistLocally(sharedEntry, localTracksSnapshot) { message ->
                                                mainHandler.post {
                                                    sharedPlaylistImportingMessage = message
                                                }
                                            }
                                        }
                                        sharedPlaylistImporting = false
                                        if (importedTrackIds.isEmpty()) {
                                            snackbarHostState.showSnackbar("Could not save playlist to library")
                                        } else {
                                            val created = playlistStore.createPlaylist(sharedEntry.name.trim())
                                            if (created == null) {
                                                snackbarHostState.showSnackbar("Could not save playlist to library")
                                            } else {
                                                playlistStore.replaceTrackIds(created.id, importedTrackIds)
                                                if (!downloadedCoverPath.isNullOrBlank()) {
                                                    playlistStore.setCustomCover(created.id, Uri.fromFile(File(downloadedCoverPath)))
                                                    runCatching { File(downloadedCoverPath).delete() }
                                                }
                                                tracks = trackStore.loadTracks()
                                                reloadPlaylists()
                                                openedPlaylistID = created.id
                                                openedTransientSharedPlaylist = null
                                                snackbarHostState.showSnackbar("Saved ${importedTrackIds.size} track(s) to library")
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Import to Library")
                            }
                            AccentTextButton(
                                onClick = {
                                    shareText(sharedEntry.shareUrl)
                                    showPlaylistOptionsDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Share Link")
                            }
                        }
                    } else if (openedPlaylist?.isUser == true) {
                        AccentTextButton(
                            onClick = {
                                addTracksTargetPlaylistID = openedPlaylist.id
                                addTracksSelectedIDs = emptySet()
                                showPlaylistOptionsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Music")
                        }
                        AccentTextButton(
                            onClick = {
                                coverTargetPlaylistID = openedPlaylist.id
                                showPlaylistOptionsDialog = false
                                changePlaylistCoverLauncher.launch("image/*")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Change Cover")
                        }
                        AccentTextButton(
                            onClick = {
                                renamePlaylistDraft = openedPlaylist.name
                                showRenamePlaylistDialog = true
                                showPlaylistOptionsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Rename Playlist")
                        }
                        AccentTextButton(
                            onClick = {
                                val shareTarget = openedPlaylist ?: return@AccentTextButton
                                val shareTracks = openedPlaylistTracks
                                showPlaylistOptionsDialog = false
                                scope.launch {
                                    sharedPlaylistUploadingMessage = "Creating playlist..."
                                    sharedPlaylistUploading = true
                                    val shared = withContext(Dispatchers.IO) {
                                        uploadSharedPlaylist(
                                            baseUrl = BuildConfig.BACKEND_BASE_URL.ifBlank { "https://api.corebrew.ru" },
                                            name = shareTarget.name,
                                            coverPath = shareTarget.customCoverPath ?: shareTracks.firstOrNull()?.artworkPath,
                                            tracks = shareTracks,
                                            onProgress = { message ->
                                                mainHandler.post {
                                                    sharedPlaylistUploadingMessage = message
                                                }
                                            }
                                        )
                                    }
                                    sharedPlaylistUploading = false
                                    if (shared == null) {
                                        snackbarHostState.showSnackbar("Could not share playlist")
                                    } else {
                                        shareText(shared.shareUrl)
                                        snackbarHostState.showSnackbar("Playlist link ready")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Share Playlist")
                        }
                        AccentTextButton(
                            onClick = {
                                playlistStore.deletePlaylist(openedPlaylist.id)
                                reloadPlaylists()
                                openedPlaylistID = null
                                showPlaylistOptionsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Delete Playlist")
                        }
                    }
                }
            },
            confirmButton = {
                AccentTextButton(onClick = { showPlaylistOptionsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showRenamePlaylistDialog && openedPlaylist?.isUser == true) {
        AlertDialog(
            onDismissRequest = { showRenamePlaylistDialog = false },
            title = { Text("Rename Playlist") },
            text = {
                TextField(
                    value = renamePlaylistDraft,
                    onValueChange = { renamePlaylistDraft = it },
                    singleLine = true
                )
            },
            confirmButton = {
                AccentTextButton(
                    onClick = {
                        val normalized = renamePlaylistDraft.trim()
                        if (normalized.isNotBlank()) {
                            playlistStore.renamePlaylist(openedPlaylist.id, normalized)
                            reloadPlaylists()
                        }
                        showRenamePlaylistDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                AccentTextButton(onClick = { showRenamePlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (sharedPlaylistOpening) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Opening Playlist") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(strokeWidth = 2.6.dp, modifier = Modifier.size(22.dp))
                    Text(sharedPlaylistOpeningMessage)
                }
            },
            confirmButton = { }
        )
    }

    if (sharedPlaylistUploading) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Sharing Playlist") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(strokeWidth = 2.6.dp, modifier = Modifier.size(22.dp))
                    Text(sharedPlaylistUploadingMessage)
                }
            },
            confirmButton = { }
        )
    }

    if (sharedPlaylistImporting) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Saving Playlist") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(strokeWidth = 2.6.dp, modifier = Modifier.size(22.dp))
                    Text(sharedPlaylistImportingMessage)
                }
            },
            confirmButton = { }
        )
    }

    if (quickAddTrackID != null) {
        val trackID = quickAddTrackID
        AlertDialog(
            onDismissRequest = { quickAddTrackID = null },
            title = { Text("Add To Playlist") },
            text = {
                if (userPlaylists.isEmpty()) {
                    Text(
                        text = "No Playlists. Create a playlist first.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(240.dp)
                    ) {
                        items(userPlaylists, key = { it.id }) { playlist ->
                            val alreadyAdded = trackID != null && playlist.trackIds.contains(trackID)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !alreadyAdded) {
                                        if (trackID != null) {
                                            playlistStore.addTrackIds(playlist.id, listOf(trackID))
                                            reloadPlaylists()
                                            quickAddTrackID = null
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Added to ${playlist.name}"
                                                )
                                            }
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = playlist.name,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (alreadyAdded) {
                                    Text(
                                        text = "Added",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                AccentTextButton(onClick = { quickAddTrackID = null }) {
                    Text("Close")
                }
            }
        )
    }

    if (showSleepTimerDialog) {
        var customSleepMinutes by rememberSaveable { mutableStateOf("") }
        val parsedCustomMinutes = customSleepMinutes.toIntOrNull() ?: 0
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            title = { Text("Sleep Timer") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AccentTextButton(
                        onClick = {
                            playbackController.clearSleepTimer()
                            showSleepTimerDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Off")
                    }
                    AccentTextButton(
                        onClick = {
                            playbackController.startSleepTimer(15)
                            showSleepTimerDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("15 min")
                    }
                    AccentTextButton(
                        onClick = {
                            playbackController.startSleepTimer(30)
                            showSleepTimerDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("30 min")
                    }
                    AccentTextButton(
                        onClick = {
                            playbackController.startSleepTimer(60)
                            showSleepTimerDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("1 hour")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = customSleepMinutes,
                        onValueChange = { value ->
                            customSleepMinutes = value.filter { it.isDigit() }.take(3)
                        },
                        singleLine = true,
                        placeholder = { Text("Custom minutes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    AccentTextButton(
                        onClick = {
                            playbackController.startSleepTimer(parsedCustomMinutes)
                            showSleepTimerDialog = false
                        },
                        enabled = parsedCustomMinutes > 0,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start custom")
                    }
                }
            },
            confirmButton = {
                AccentTextButton(onClick = { showSleepTimerDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
    }
}

@Composable
private fun AppIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    iconWidth: Dp = 20.dp,
    iconHeight: Dp = 20.dp,
    touchSize: Dp = 32.dp,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = Modifier
            .size(touchSize)
            .alpha(if (enabled) 1f else 0.45f)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconWidth, iconHeight),
        )
    }
}

@Composable
private fun AppVectorIconButton(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    iconSize: Dp = 20.dp,
    touchSize: Dp = 32.dp,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = Modifier
            .size(touchSize)
            .alpha(if (enabled) 1f else 0.45f)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun AccentTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = LocalAccentColor.current,
            disabledContentColor = LocalAccentColor.current.copy(alpha = 0.45f)
        ),
        content = content
    )
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val fieldBackground = if (isDark) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color.Black.copy(alpha = 0.06f)
    }
    Surface(
        color = fieldBackground,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_global_magnifyingglass),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.92f),
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(7.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun PushTopBar(
    title: String,
    onBack: () -> Unit,
    trailingText: String? = null,
    trailingEnabled: Boolean = true,
    onTrailing: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .height(44.dp)
                .width(78.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 2.dp)
            ) {
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (trailingText != null && onTrailing != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .height(44.dp)
                    .width(78.dp)
                    .alpha(if (trailingEnabled) 1f else 0.45f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = trailingEnabled, onClick = onTrailing),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 2.dp)
                ) {
                    Text(
                        text = trailingText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistCreateNamePage(
    name: String,
    onNameChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    androidx.activity.compose.BackHandler(onBack = onBack)

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val cardBackground = if (isDark) {
        Color.White.copy(alpha = 0.06f)
    } else {
        Color.Black.copy(alpha = 0.03f)
    }
    val cardBorder = if (isDark) {
        Color.White.copy(alpha = 0.14f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }
    val canContinue = name.trim().isNotBlank()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        PushTopBar(
            title = "Create Playlist",
            onBack = onBack
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Name Your Playlist",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBackground)
                    .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Playlist Name",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextField(
                    value = name,
                    onValueChange = { onNameChange(it.take(32)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    shape = RoundedCornerShape(0.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                )

                Text(
                    text = "${name.length}/32",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    style = TextStyle(
                        fontFamily = SonoraAndroidSFProSemiboldFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .imePadding()
                .height(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(enabled = canContinue, onClick = onNext),
            color = if (canContinue) {
                LocalAccentColor.current
            } else {
                Color(0xFF999999).copy(alpha = 0.4f)
            }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Next: Choose Music",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistCreateTrackPickerPage(
    allTracks: List<TrackItem>,
    filteredTracks: List<TrackItem>,
    query: String,
    selectedTrackIDs: Set<String>,
    onQueryChange: (String) -> Unit,
    onToggleTrack: (String) -> Unit,
    onBack: () -> Unit,
    onCreate: () -> Unit
) {
    androidx.activity.compose.BackHandler(onBack = onBack)
    val listState = rememberLazyListState()
    val createEnabled = selectedTrackIDs.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        PushTopBar(
            title = "Select Music",
            onBack = onBack,
            trailingText = "Create",
            trailingEnabled = createEnabled,
            onTrailing = onCreate
        )

        SearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Search Tracks"
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

        if (filteredTracks.isEmpty()) {
            val message = if (allTracks.isEmpty()) {
                "No tracks in library."
            } else {
                "No search results."
            }
            EmptyListLabel(text = message)
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 2.dp, bottom = 2.dp)
            ) {
                itemsIndexed(filteredTracks, key = { _, item -> item.id }) { index, track ->
                    SelectableTrackRow(
                        track = track,
                        selected = selectedTrackIDs.contains(track.id),
                        onToggle = { onToggleTrack(track.id) }
                    )
                    if (index < filteredTracks.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 54.dp, end = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistAddTracksPage(
    tracks: List<TrackItem>,
    selectedTrackIDs: Set<String>,
    onToggleTrack: (String) -> Unit,
    onBack: () -> Unit,
    onAdd: () -> Unit
) {
    androidx.activity.compose.BackHandler(onBack = onBack)
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        PushTopBar(
            title = "Add Music",
            onBack = onBack,
            trailingText = "Add",
            trailingEnabled = selectedTrackIDs.isNotEmpty(),
            onTrailing = onAdd
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

        if (tracks.isEmpty()) {
            EmptyListLabel(text = "All tracks are already in this playlist.")
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 2.dp, bottom = 2.dp)
            ) {
                itemsIndexed(tracks, key = { _, item -> item.id }) { index, track ->
                    SelectableTrackRow(
                        track = track,
                        selected = selectedTrackIDs.contains(track.id),
                        onToggle = { onToggleTrack(track.id) }
                    )
                    if (index < tracks.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 54.dp, end = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun SelectableTrackRow(
    track: TrackItem,
    selected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .combinedClickable(onClick = onToggle, onLongClick = onToggle)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrackArtwork(
            track = track,
            placeholderRes = R.drawable.tab_note,
            placeholderSize = 20.dp,
            decodeMaxSize = 128,
            modifier = Modifier.size(34.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = track.displayTitle(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = formatDuration(track.durationMs),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = TextStyle(
                fontFamily = SonoraAndroidSFProSemiboldFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Right,
            modifier = Modifier.widthIn(min = 44.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected",
                tint = LocalAccentColor.current,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun MusicPage(
    listState: androidx.compose.foundation.lazy.LazyListState,
    tracks: List<TrackItem>,
    filteredTracks: List<TrackItem>,
    bottomInset: androidx.compose.ui.unit.Dp,
    currentTrackID: String?,
    isPlaying: Boolean,
    selectionMode: Boolean,
    selectedTrackIDs: Set<String>,
    onTrackTap: (TrackItem) -> Unit,
    onTrackLongPress: (TrackItem) -> Unit,
    onTrackSwipeFavoriteToggle: (TrackItem) -> Unit,
    onTrackSwipeDelete: (TrackItem) -> Unit,
    onTrackSwipeAddToPlaylist: (TrackItem) -> Unit
) {
    if (filteredTracks.isEmpty()) {
        val message = if (tracks.isEmpty()) {
            "No music files in On My iPhone/Sonora/Sonora"
        } else {
            "No search results."
        }
        EmptyListLabel(text = message)
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 2.dp, bottom = bottomInset)
    ) {
        itemsIndexed(filteredTracks, key = { _, item -> item.id }) { index, track ->
            TrackRow(
                track = track,
                isCurrent = currentTrackID == track.id,
                showsPlaybackIndicator = (currentTrackID == track.id && isPlaying),
                selectionMode = selectionMode,
                isSelected = selectedTrackIDs.contains(track.id),
                onClick = { onTrackTap(track) },
                onLongPress = { onTrackLongPress(track) },
                startActions = if (selectionMode) {
                    emptyList()
                } else {
                    listOf(
                        SwipeTrackAction(
                            label = if (track.isFavorite) "Unfav" else "Fav",
                            backgroundColor = if (track.isFavorite) {
                                Color(0xFF6A6A6A)
                            } else {
                                Color(0xFFFF5966)
                            },
                            iconRes = if (track.isFavorite) R.drawable.heart_slash_fill else R.drawable.heart_fill,
                            onAction = { onTrackSwipeFavoriteToggle(track) },
                            fullSwipeEnabled = true
                        )
                    )
                },
                endActions = if (selectionMode) {
                    emptyList()
                } else {
                    listOf(
                        SwipeTrackAction(
                            label = "Delete",
                            backgroundColor = Color(0xFFFF3B30),
                            iconRes = R.drawable.ic_global_trash_fill,
                            onAction = { onTrackSwipeDelete(track) }
                        ),
                        SwipeTrackAction(
                            label = "Add",
                            backgroundColor = Color(0xFF2979F2),
                            iconRes = R.drawable.ic_global_text_badge_plus,
                            onAction = { onTrackSwipeAddToPlaylist(track) }
                        )
                    )
                }
            )
            if (index < filteredTracks.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 54.dp, end = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            }
        }
    }
}

@Composable
private fun SearchPage(
    listState: androidx.compose.foundation.lazy.LazyListState,
    searchQuery: String,
    localPlaylistResults: List<PlaylistUiItem>,
    localArtistResults: List<SearchArtistUiItem>,
    onlineTrackResults: List<MiniStreamingTrack>,
    onlineArtistResults: List<MiniStreamingArtist>,
    musicResults: List<TrackItem>,
    onlineTracksLoading: Boolean,
    onlineArtistsLoading: Boolean,
    onlineArtistsVisible: Boolean,
    onlineConfigured: Boolean,
    currentLocalTrackId: String?,
    currentOnlineTrackId: String?,
    isPlaying: Boolean,
    onlineResolvingTrackIds: Set<String>,
    onlineInstallingTrackIds: Set<String>,
    onlineQueuePositionsByTrackId: Map<String, Int>,
    trackByID: Map<String, TrackItem>,
    bottomInset: androidx.compose.ui.unit.Dp,
    onMusicTrackTap: (TrackItem) -> Unit,
    onOnlineTrackTap: (MiniStreamingTrack, Int) -> Unit,
    onLocalPlaylistTap: (PlaylistUiItem) -> Unit,
    onLocalArtistTap: (SearchArtistUiItem) -> Unit,
    onOnlineArtistTap: (MiniStreamingArtist) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val hasQuery = searchQuery.trim().isNotBlank()
    val showLocalDiscoverySections = !hasQuery
    val hasLocalDiscoveryResults = localPlaylistResults.isNotEmpty() || localArtistResults.isNotEmpty()
    val hasLocalResults = musicResults.isNotEmpty() || (showLocalDiscoverySections && hasLocalDiscoveryResults)
    val hasOnlineArtistContent = onlineArtistsVisible && (onlineArtistResults.isNotEmpty() || onlineArtistsLoading)
    val hasOnlineContent = onlineTrackResults.isNotEmpty() ||
        onlineTracksLoading ||
        hasOnlineArtistContent
    val hasOnlineSections = onlineConfigured && hasQuery
    if (!hasLocalResults && !(hasOnlineSections && hasOnlineContent)) {
        EmptyListLabel(text = "No search results.")
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                })
            },
        contentPadding = PaddingValues(top = 2.dp, bottom = bottomInset)
    ) {
        if (showLocalDiscoverySections) {
            if (localPlaylistResults.isNotEmpty()) {
                item(key = "search_local_playlists_heading") {
                    SearchSectionHeading(text = "Playlists")
                }
                item(key = "search_local_playlists_row") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(localPlaylistResults, key = { it.id }) { playlist ->
                            LocalSearchCollectionCard(
                                title = playlist.name,
                                subtitle = "${playlist.trackIds.size} tracks",
                                coverTrack = playlist.trackIds.firstNotNullOfOrNull { trackByID[it] },
                                placeholderRes = R.drawable.tab_lib,
                                onClick = { onLocalPlaylistTap(playlist) }
                            )
                        }
                    }
                }
            }

            if (localArtistResults.isNotEmpty()) {
                item(key = "search_local_artists_heading") {
                    SearchSectionHeading(text = "Artists")
                }
                item(key = "search_local_artists_row") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(localArtistResults, key = { it.key }) { artist ->
                            val coverTrack = artist.trackIds.firstNotNullOfOrNull { trackByID[it] }
                            LocalSearchArtistCard(
                                artist = artist,
                                coverTrack = coverTrack,
                                onClick = { onLocalArtistTap(artist) }
                            )
                        }
                    }
                }
            }
        }

        if (hasOnlineSections) {
            item(key = "search_tracks_heading") {
                SearchSectionHeading(text = "Tracks")
            }

            if (onlineTracksLoading) {
                item(key = "search_tracks_loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = LocalAccentColor.current
                        )
                    }
                }
            } else if (onlineTrackResults.isEmpty()) {
                item(key = "search_tracks_empty") {
                    SearchSubtleEmptyLabel(text = "No tracks found.")
                }
            } else {
                itemsIndexed(onlineTrackResults, key = { _, item -> item.trackId }) { index, track ->
                    MiniStreamingTrackRow(
                        track = track,
                        isCurrent = currentOnlineTrackId == track.trackId,
                        isPlaying = isPlaying,
                        isResolving = onlineResolvingTrackIds.contains(track.trackId),
                        isInstalling = onlineInstallingTrackIds.contains(track.trackId),
                        queuePosition = onlineQueuePositionsByTrackId[track.trackId],
                        onClick = { onOnlineTrackTap(track, index) }
                    )
                    if (index < onlineTrackResults.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 58.dp, end = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    }
                }
            }

            if (onlineArtistsVisible) {
                item(key = "search_artists_heading") {
                    SearchSectionHeading(text = "Artists")
                }

                if (onlineArtistsLoading) {
                    item(key = "search_artists_loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = LocalAccentColor.current
                            )
                        }
                    }
                } else if (onlineArtistResults.isEmpty()) {
                    item(key = "search_artists_empty") {
                        SearchSubtleEmptyLabel(text = "No artists found.")
                    }
                } else {
                    item(key = "search_artists_row") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(onlineArtistResults, key = { it.artistId }) { artist ->
                                OnlineSearchArtistCard(
                                    artist = artist,
                                    onClick = { onOnlineArtistTap(artist) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (musicResults.isNotEmpty()) {
            item(key = "search_music_heading") {
                SearchSectionHeading(text = "Music")
            }
            item(key = "search_music_row") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(musicResults, key = { it.id }) { track ->
                        SearchMusicTrackCard(
                            track = track,
                            isCurrent = currentLocalTrackId == track.id,
                            isPlaying = isPlaying,
                            onClick = { onMusicTrackTap(track) }
                        )
                    }
                }
            }
        } else if ((!hasOnlineSections || !hasOnlineContent) &&
            !(showLocalDiscoverySections && hasLocalDiscoveryResults)
        ) {
            item(key = "search_music_empty") {
                SearchSubtleEmptyLabel(text = "No local music found.")
            }
        }
    }
}
@Composable
private fun SearchSectionHeading(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = SonoraAndroidYSMusicFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp)
    )
}

@Composable
private fun SearchSubtleEmptyLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    )
}

@Composable
private fun SearchMusicTrackCard(
    track: TrackItem,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(184.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(184.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            TrackArtwork(
                track = track,
                placeholderRes = R.drawable.tab_note,
                placeholderSize = 48.dp,
                decodeMaxSize = 512,
                modifier = Modifier.fillMaxSize()
            )
            if (isCurrent) {
                Surface(
                    color = Color.Black.copy(alpha = 0.30f),
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(999.dp))
                ) {
                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(
                                if (isPlaying) R.drawable.ic_global_pause else R.drawable.ic_global_play
                            ),
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            colorFilter = ColorFilter.tint(Color.White),
                            modifier = Modifier.size(15.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = track.displayTitle(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = if (isCurrent) LocalAccentColor.current else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.artist.ifBlank { "Unknown Artist" },
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LocalSearchCollectionCard(
    title: String,
    subtitle: String,
    coverTrack: TrackItem?,
    placeholderRes: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(184.dp)
            .clickable(onClick = onClick)
    ) {
        TrackArtwork(
            track = coverTrack,
            placeholderRes = placeholderRes,
            placeholderSize = 48.dp,
            decodeMaxSize = 320,
            modifier = Modifier
                .fillMaxWidth()
                .height(184.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LocalSearchArtistCard(
    artist: SearchArtistUiItem,
    coverTrack: TrackItem?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(184.dp)
            .clickable(onClick = onClick)
    ) {
        TrackArtwork(
            track = coverTrack,
            placeholderRes = R.drawable.tab_note,
            placeholderSize = 52.dp,
            decodeMaxSize = 320,
            modifier = Modifier
                .fillMaxWidth()
                .height(184.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artist.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${artist.trackIds.size} tracks",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LocalSearchTrackRow(
    track: TrackItem,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
            if (isCurrent && isPlaying) {
                Image(
                    painter = painterResource(R.drawable.ic_global_pause),
                    contentDescription = "Playing",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.size(17.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                TrackArtwork(
                    track = track,
                    placeholderRes = R.drawable.tab_note,
                    placeholderSize = 18.dp,
                    decodeMaxSize = 160,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = track.displayTitle(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (isCurrent) LocalAccentColor.current else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist.ifBlank { "Unknown Artist" },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatDuration(track.durationMs),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = TextStyle(
                fontFamily = SonoraAndroidSFProSemiboldFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Right,
            modifier = Modifier.widthIn(min = 44.dp)
        )
    }
}

@Composable
private fun MiniStreamingTrackRow(
    track: MiniStreamingTrack,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isResolving: Boolean,
    isInstalling: Boolean,
    queuePosition: Int?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(enabled = !(isInstalling || isResolving), onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
            if (isCurrent && isPlaying) {
                Image(
                    painter = painterResource(R.drawable.ic_global_pause),
                    contentDescription = "Playing",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.size(17.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                MiniStreamingArtwork(
                    artworkUrl = track.artworkUrl,
                    placeholderRes = R.drawable.tab_note,
                    placeholderSize = 18.dp,
                    decodeMaxSize = 160,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = track.title.ifBlank { "Track" },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (isCurrent) LocalAccentColor.current else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artists.ifBlank { "Spotify" },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        if (isInstalling || isResolving) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = LocalAccentColor.current
            )
        } else {
            val durationLabel = formatDuration(track.durationMs)
            val trailingLabel = queuePosition?.let { "$durationLabel · #$it" } ?: durationLabel
            Text(
                text = trailingLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = TextStyle(
                    fontFamily = SonoraAndroidSFProSemiboldFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Right,
                modifier = Modifier.widthIn(min = 44.dp)
            )
        }
    }
}

@Composable
private fun OnlineSearchArtistCard(
    artist: MiniStreamingArtist,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(184.dp)
            .clickable(onClick = onClick)
    ) {
        MiniStreamingArtwork(
            artworkUrl = artist.artworkUrl,
            placeholderRes = R.drawable.tab_note,
            placeholderSize = 52.dp,
            decodeMaxSize = 320,
            modifier = Modifier
                .fillMaxWidth()
                .height(184.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artist.name.ifBlank { "Artist" },
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MiniStreamingArtistPage(
    listState: androidx.compose.foundation.lazy.LazyListState,
    artist: MiniStreamingArtist,
    tracks: List<MiniStreamingTrack>,
    isLoading: Boolean,
    currentTrackId: String?,
    isPlaying: Boolean,
    isSleepTimerActive: Boolean,
    resolvingTrackIds: Set<String>,
    installingTrackIds: Set<String>,
    queuePositionsByTrackId: Map<String, Int>,
    canLoadMore: Boolean,
    isLoadingMore: Boolean,
    onPlayTap: () -> Unit,
    onShuffleTap: () -> Unit,
    onSleepTap: () -> Unit,
    onLoadMore: () -> Unit,
    onTrackTap: (MiniStreamingTrack, Int) -> Unit
) {
    val isArtistQueuePlaying = isPlaying &&
        !currentTrackId.isNullOrBlank() &&
        tracks.any { it.trackId == currentTrackId }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 2.dp, bottom = 2.dp)
    ) {
        item(key = "artist_header") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(364.dp)
            ) {
                MiniStreamingArtwork(
                    artworkUrl = artist.artworkUrl,
                    placeholderRes = R.drawable.tab_note,
                    placeholderSize = 92.dp,
                    decodeMaxSize = 640,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .size(212.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Text(
                    text = artist.name.ifBlank { "Artist" },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 236.dp, start = 14.dp, end = 14.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 272.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PlainControlButton(
                        iconRes = if (isSleepTimerActive) R.drawable.sleep_fill else R.drawable.sleep,
                        contentDescription = "Sleep timer",
                        onClick = onSleepTap,
                        enabled = true,
                        size = 46.dp,
                        iconSize = 29.dp,
                        iconWidth = 24.5.dp,
                        iconHeight = 29.dp,
                        tint = if (isSleepTimerActive) {
                            LocalAccentColor.current
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Surface(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .clickable(enabled = tracks.isNotEmpty(), onClick = onPlayTap),
                        color = LocalAccentColor.current
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(
                                    if (isArtistQueuePlaying) {
                                        R.drawable.ic_global_pause
                                    } else {
                                        R.drawable.ic_global_play
                                    }
                                ),
                                contentDescription = if (isArtistQueuePlaying) "Pause artist" else "Play artist",
                                colorFilter = ColorFilter.tint(Color.White),
                                modifier = Modifier
                                    .size(29.dp)
                                    .offset(x = if (isArtistQueuePlaying) 0.dp else 1.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    PlainControlButton(
                        iconRes = R.drawable.ic_player_shuffle,
                        contentDescription = "Shuffle artist",
                        onClick = onShuffleTap,
                        enabled = tracks.isNotEmpty(),
                        size = 46.dp,
                        iconSize = 29.dp,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (isLoading) {
            item(key = "artist_loading") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.dp,
                        color = LocalAccentColor.current
                    )
                }
            }
        } else if (tracks.isEmpty()) {
            item(key = "artist_empty") {
                EmptyListLabel(text = "No tracks found for this artist.")
            }
        } else {
            itemsIndexed(tracks, key = { _, item -> item.trackId }) { index, track ->
                MiniStreamingTrackRow(
                    track = track,
                    isCurrent = currentTrackId == track.trackId,
                    isPlaying = isPlaying,
                    isResolving = resolvingTrackIds.contains(track.trackId),
                    isInstalling = installingTrackIds.contains(track.trackId),
                    queuePosition = queuePositionsByTrackId[track.trackId],
                    onClick = { onTrackTap(track, index) }
                )
                if (index < tracks.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 58.dp, end = 12.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                }
            }

            if (isLoadingMore) {
                item(key = "artist_loading_more") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = LocalAccentColor.current
                        )
                    }
                }
            } else if (canLoadMore) {
                item(key = "artist_load_more_trigger") {
                    LaunchedEffect(tracks.size) {
                        onLoadMore()
                    }
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomePage(
    listState: androidx.compose.foundation.lazy.LazyListState,
    waveQueue: List<TrackItem>,
    waveStartTrack: TrackItem?,
    waveLook: MyWaveLook,
    tasteTracks: List<TrackItem>,
    freshChoiceTracks: List<TrackItem>,
    isWavePlaying: Boolean,
    topInset: androidx.compose.ui.unit.Dp = 0.dp,
    bottomInset: androidx.compose.ui.unit.Dp,
    onWaveToggleTap: () -> Unit,
    onTasteTap: (TrackItem) -> Unit,
    onFreshChoiceTap: (TrackItem) -> Unit
) {
    if (waveQueue.isEmpty()) {
        EmptyListLabel(text = "No music files in On My iPhone/Sonora/Sonora")
        return
    }
    val waveTrack = waveStartTrack ?: waveQueue.firstOrNull()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = topInset + 4.dp, bottom = bottomInset)
    ) {
        if (waveTrack != null) {
            item(key = "home_wave") {
                HomeMyWaveCard(
                    track = waveTrack,
                    look = waveLook,
                    isPlaying = isWavePlaying,
                    onPlayToggle = onWaveToggleTap
                )
            }
        }

        if (freshChoiceTracks.isNotEmpty()) {
            item(key = "home_fresh_choice_heading") {
                HomeSectionHeading(text = "Fresh choice")
            }
            item(key = "home_fresh_choice_groups") {
                val grouped = freshChoiceTracks.chunked(2)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(grouped) { group ->
                        Column(
                            modifier = Modifier
                                .width(304.dp)
                                .height(140.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            group.forEach { track ->
                                HomeCompactTrackRow(
                                    track = track,
                                    onClick = { onFreshChoiceTap(track) }
                                )
                            }
                            if (group.size == 1) {
                                Spacer(modifier = Modifier.height(66.dp))
                            }
                        }
                    }
                }
            }
        }

        if (tasteTracks.isNotEmpty()) {
            item(key = "home_need_this_heading") {
                HomeSectionHeading(text = "Based on your taste")
            }
            item(key = "home_need_this_row") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasteTracks, key = { it.id }) { track ->
                        HomeRecommendationCard(
                            track = track,
                            onClick = { onTasteTap(track) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSectionHeading(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = SonoraAndroidHomeHeadingFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 18.dp, top = 12.dp, bottom = 8.dp)
    )
}

@Composable
private fun CollectionsSectionHeading(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = SonoraAndroidYSMusicFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 23.sp
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 18.dp, top = 10.dp, bottom = 8.dp)
    )
}

@Composable
private fun HomeMyWaveCard(
    track: TrackItem,
    look: MyWaveLook,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit
) {
    val paletteCacheKey = remember(track.id, track.artworkPath, track.filePath) {
        wavePaletteCacheKey(track)
    }
    val paletteTarget by produceState(
        initialValue = paletteCacheKey?.let { wavePaletteCacheGet(it) } ?: defaultWavePalette(),
        key1 = paletteCacheKey
    ) {
        val key = paletteCacheKey ?: run {
            value = defaultWavePalette()
            return@produceState
        }
        wavePaletteCacheGet(key)?.let { cached ->
            value = cached
            return@produceState
        }
        val resolved = withContext(Dispatchers.IO) {
            buildWavePaletteForTrack(track)
        }
        wavePaletteCachePut(key, resolved)
        value = resolved
    }
    val wavePaletteAnim = tween<Color>(durationMillis = 1500, easing = LinearEasing)
    val c0 by animateColorAsState(targetValue = paletteTarget[0], animationSpec = wavePaletteAnim, label = "wave_c0")
    val c1 by animateColorAsState(targetValue = paletteTarget[1], animationSpec = wavePaletteAnim, label = "wave_c1")
    val c2 by animateColorAsState(targetValue = paletteTarget[2], animationSpec = wavePaletteAnim, label = "wave_c2")
    val c3 by animateColorAsState(targetValue = paletteTarget[3], animationSpec = wavePaletteAnim, label = "wave_c3")

    val transition = rememberInfiniteTransition(label = "wave_motion")
    val phaseA by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10_000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "wave_phase_a"
    )
    val phaseB by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8_000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "wave_phase_b"
    )
    val breathe by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7_000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "wave_breathe"
    )
    val pulsar by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10_800, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "wave_pulsar"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(396.dp)
    ) {
        when (look) {
            MyWaveLook.Clouds -> MyWaveCloudsBackground(
                colors = listOf(c0, c1, c2, c3),
                phaseA = phaseA,
                phaseB = phaseB,
                breathe = breathe,
                pulsar = pulsar,
                modifier = Modifier.matchParentSize()
            )
            MyWaveLook.Contours -> Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 8.dp, vertical = 18.dp)
            ) {
                MyWaveContoursBackground(
                    colors = listOf(c0, c1, c2, c3),
                    phaseA = phaseA,
                    phaseB = phaseB,
                    breathe = breathe,
                    pulsar = pulsar,
                    isPlaying = isPlaying,
                    modifier = Modifier.matchParentSize()
                )
            }
        }

        if (look == MyWaveLook.Clouds) {
            WaveGrainOverlay(seed = track.id.hashCode(), modifier = Modifier.matchParentSize())
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "My wave",
                style = TextStyle(
                    fontFamily = SonoraAndroidHomeHeadingFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 34.sp
                ),
                color = Color.White
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(13.dp))
                    .clickable(onClick = onPlayToggle)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(if (isPlaying) R.drawable.ic_global_pause else R.drawable.ic_global_play),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPlaying) "Pause" else "Play",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun MyWaveCloudsBackground(
    colors: List<Color>,
    phaseA: Float,
    phaseB: Float,
    breathe: Float,
    pulsar: Float,
    modifier: Modifier = Modifier
) {
    val c0 = colors.getOrElse(0) { Color(0xFF4A3E35) }
    val c1 = colors.getOrElse(1) { c0 }
    val c2 = colors.getOrElse(2) { c1 }
    val c3 = colors.getOrElse(3) { c2 }
    Canvas(
        modifier = modifier.padding(horizontal = 10.dp, vertical = 16.dp)
    ) {
        val pulseBoost = (0.90f + ((pulsar - 0.92f) * 0.80f))
            .coerceIn(0.82f, 1.22f)
        val pulseCenter = Offset(size.width * 0.5f, size.height * 0.48f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    blendColors(c0, c1, 0.5f).copy(alpha = 0.22f * pulseBoost),
                    Color.Transparent
                ),
                center = pulseCenter,
                radius = size.minDimension * 0.64f
            ),
            center = pulseCenter,
            radius = size.minDimension * 0.64f
        )

        val pulseRadius = size.minDimension * (0.16f + (0.08f * pulsar))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    blendColors(c3, Color.White, 0.24f).copy(alpha = (0.16f * pulseBoost).coerceAtMost(0.30f)),
                    Color.Transparent
                ),
                center = pulseCenter,
                radius = pulseRadius
            ),
            center = pulseCenter,
            radius = pulseRadius
        )

        fun drawBlob(
            color: Color,
            baseX: Float,
            baseY: Float,
            ampX: Float,
            ampY: Float,
            phase: Float,
            pulse: Float
        ) {
            val driftX = sin(((phaseA * 6.283f) + phase).toDouble()).toFloat()
            val driftY = cos(((phaseB * 6.283f) + phase).toDouble()).toFloat()
            val x = size.width * (baseX + (driftX * ampX))
            val y = size.height * (baseY + (driftY * ampY))
            val radius = size.minDimension * (0.18f + (pulse * 0.74f * breathe))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = (0.32f * pulseBoost).coerceAtMost(0.40f)),
                        color.copy(alpha = (0.11f * pulseBoost).coerceAtMost(0.16f)),
                        Color.Transparent
                    ),
                    center = Offset(x, y),
                    radius = radius
                ),
                center = Offset(x, y),
                radius = radius
            )
        }

        drawBlob(c1, 0.37f, 0.43f, 0.04f, 0.03f, 0.5f, 0.05f)
        drawBlob(c2, 0.63f, 0.42f, 0.04f, 0.03f, 1.2f, 0.05f)
        drawBlob(c3, 0.50f, 0.56f, 0.05f, 0.04f, 2.0f, 0.06f)
        drawBlob(c0, 0.34f, 0.58f, 0.04f, 0.03f, 2.7f, 0.05f)
        drawBlob(c2, 0.66f, 0.57f, 0.04f, 0.03f, 3.3f, 0.05f)
        drawBlob(c3, 0.48f, 0.32f, 0.04f, 0.03f, 3.9f, 0.05f)
        drawBlob(c1, 0.52f, 0.70f, 0.04f, 0.03f, 4.6f, 0.04f)
    }
}

@Composable
private fun MyWaveContoursBackground(
    colors: List<Color>,
    phaseA: Float,
    phaseB: Float,
    breathe: Float,
    pulsar: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val c0 = colors.getOrElse(0) { Color(0xFF4A3E35) }
    val c1 = colors.getOrElse(1) { c0 }
    val c2 = colors.getOrElse(2) { c1 }
    val c3 = colors.getOrElse(3) { c2 }
    val lightTheme = !androidx.compose.foundation.isSystemInDarkTheme()
    val colorSeed = (
        (c0.red * 0.31f) +
            (c1.green * 0.27f) +
            (c2.blue * 0.23f) +
            (c3.red * 0.19f)
        ).coerceIn(0f, 1f)
    val ringCount = 7
    var contourClock by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying, colorSeed) {
        var lastFrameNanos = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                if (lastFrameNanos != 0L) {
                    val deltaSeconds = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceAtMost(0.05f)
                    val speed = if (isPlaying) 1.06f else 0.44f
                    contourClock += deltaSeconds * speed
                }
                lastFrameNanos = frameNanos
            }
        }
    }

    Canvas(modifier = modifier) {
        val tau = (Math.PI * 2.0).toFloat()
        val motion = if (isPlaying) 1.0f else 0.66f
        val seedPhase = (colorSeed * tau) + (phaseA * tau * 0.014f) + (phaseB * tau * 0.010f)
        val t = contourClock
        val driverA = sin(((t * 0.24f) + seedPhase).toDouble()).toFloat()
        val driverB = cos(((t * 0.17f) - (seedPhase * 0.33f)).toDouble()).toFloat()
        val driverC = sin(((t * 0.13f) + (seedPhase * 0.61f)).toDouble()).toFloat()
        val driverD = cos(((t * 0.20f) + (seedPhase * 0.19f)).toDouble()).toFloat()
        val macroA = (driverA * 0.52f) + (driverC * 0.28f) + (driverD * 0.20f)
        val macroB = (driverB * 0.48f) + (driverC * 0.24f) + (driverD * 0.28f)
        val macroC = (driverA * driverB * 0.22f) + (driverC * 0.42f) + (driverD * 0.36f)
        val sharedCompression = (driverA * 0.46f) + (driverB * 0.22f) + (driverC * 0.14f)
        val sharedLift = (driverD * 0.36f) + (driverB * 0.18f)
        val audioLift = ((((phaseA - phaseB) * 0.5f) + 0.5f).coerceIn(0f, 1f) - 0.5f) * 0.06f
        val center = Offset(
            x = (size.width * 0.5f) + ((macroA * 0.62f) + (macroC * 0.20f)) * size.width * 0.0042f * motion,
            y = (size.height * 0.53f) + (((macroB * 0.60f) + (macroC * 0.16f)) + audioLift) * size.height * 0.0062f * motion
        )
        val haloRadius = size.minDimension * (0.35f + ((pulsar - 0.92f) * 0.11f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    blendColors(if (lightTheme) blendColors(c0, Color.White, 0.16f) else c0, c1, 0.52f)
                        .copy(alpha = if (isPlaying) 0.30f else 0.20f),
                    Color.Transparent
                ),
                center = center,
                radius = haloRadius
            ),
            center = center,
            radius = haloRadius
        )

        val coreRadius = size.minDimension * (0.24f + ((breathe - 0.95f) * 0.06f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    blendColors(if (lightTheme) blendColors(c1, Color.White, 0.20f) else c1, c2, 0.48f)
                        .copy(alpha = if (isPlaying) 0.22f else 0.14f),
                    blendColors(if (lightTheme) blendColors(c2, Color.White, 0.18f) else c2, c3, 0.42f)
                        .copy(alpha = if (isPlaying) 0.10f else 0.05f),
                    Color.Transparent
                ),
                center = center,
                radius = coreRadius
            ),
            center = center,
            radius = coreRadius
        )

        val palette = listOf(c0, c1, c2, c3)
        repeat(ringCount) { index ->
            val progress = index / (ringCount - 1f)
            val envelope = (1.0f - kotlin.math.abs(progress - 0.5f) * 1.08f).coerceAtLeast(0.38f)
            val ringPhase = seedPhase + (progress * 1.15f) + (t * (0.40f + (progress * 0.075f)))
            val neighborPush =
                sin(((t * (0.31f + (progress * 0.030f))) + seedPhase + (progress * 2.6f)).toDouble()).toFloat() * 0.48f +
                    cos(((t * (0.22f + (progress * 0.018f))) - (seedPhase * 0.44f) + (progress * 1.7f)).toDouble()).toFloat() * 0.24f
            val compression = (sharedCompression * 0.58f) + (neighborPush * 0.42f)
            val ringCenterX =
                center.x +
                    sin(((ringPhase * 0.72f) + (macroA * 0.18f)).toDouble()).toFloat() * size.width * (0.018f + (envelope * 0.004f))
            val ringCenterY =
                center.y +
                    cos(((ringPhase * 0.54f) + 0.6f + (macroB * 0.16f)).toDouble()).toFloat() * size.height * (0.022f + (envelope * 0.006f)) +
                    (((compression * 0.010f) + (sharedLift * 0.004f)) * size.height * motion)
            val radiusPush = ((compression * 0.080f) + (neighborPush * 0.050f)) * envelope * motion
            val radiusX = (size.width * (0.17f + (progress * 0.23f))) * (1.0f + (radiusPush * 0.50f))
            val radiusY = (size.height * (0.13f + (progress * 0.17f))) * (1.0f - (radiusPush * 0.42f))
            val amplitude =
                size.minDimension * (0.014f + (progress * 0.010f)) *
                    (if (isPlaying) 1.0f else 0.82f) *
                    (1.0f + (((compression * 0.16f) + (neighborPush * 0.10f)) * envelope))
            val pointCount = 56

            val contour = Path().apply {
                for (point in 0..pointCount) {
                    val angle = (point / pointCount.toFloat()) * tau
                    val wobbleA =
                        sin((((angle * 2f) + ringPhase) + (neighborPush * 0.52f) + (macroA * 0.14f)).toDouble())
                            .toFloat() * amplitude
                    val wobbleB =
                        cos((((angle * 3f) - (ringPhase * 0.74f)) + (compression * 0.36f) + (macroB * 0.10f)).toDouble())
                            .toFloat() * amplitude * 0.54f
                    val wobbleC =
                        sin((((angle * 5f) + (ringPhase * 1.12f)) + (neighborPush * 0.24f) + (macroC * 0.08f)).toDouble())
                            .toFloat() * amplitude * 0.20f
                    val orbitX = cos(angle.toDouble()).toFloat() * (radiusX + wobbleA + wobbleB)
                    val orbitY = sin(angle.toDouble()).toFloat() * (radiusY + (wobbleA * 0.72f) - (wobbleB * 0.16f) + wobbleC)
                    val x = ringCenterX + orbitX
                    val y = ringCenterY + orbitY
                    if (point == 0) {
                        moveTo(x, y)
                    } else {
                        lineTo(x, y)
                    }
                }
                close()
            }

            val linePalette = listOf(
                if (lightTheme) blendColors(c3, Color.White, 0.14f) else blendColors(c3, Color.White, 0.06f),
                if (lightTheme) blendColors(c1, Color.White, 0.20f) else blendColors(c1, Color.White, 0.08f),
                if (lightTheme) blendColors(c2, Color.White, 0.16f) else blendColors(c2, Color.White, 0.06f),
                if (lightTheme) blendColors(c0, Color.White, 0.10f) else blendColors(c0, Color.White, 0.02f)
            )
            val base = linePalette[index % linePalette.size]
            val baseOpacity = if (isPlaying) {
                if (index < 2) 0.96f else if (index < 4) 0.82f else 0.66f
            } else {
                if (index < 2) 0.78f else if (index < 4) 0.64f else 0.52f
            }
            val swing = if (isPlaying) 0.16f else 0.10f
            val alpha = (baseOpacity + (sin(((t * (0.58f + (progress * 0.06f))) + ringPhase).toDouble()).toFloat() * swing))
                .coerceIn(0.24f, 1.0f)
            val lineColor = base.copy(alpha = alpha)
            val lineBrush = Brush.linearGradient(
                colors = listOf(
                    lineColor,
                    blendColors(lineColor, Color.White, if (lightTheme) 0.14f else 0.08f),
                    lineColor
                ),
                start = Offset(ringCenterX - radiusX, ringCenterY - radiusY),
                end = Offset(ringCenterX + radiusX, ringCenterY + radiusY)
            )

            drawPath(
                path = contour,
                color = lineColor.copy(
                    alpha = if (lightTheme) {
                        if (index < 3) 0.24f else 0.12f
                    } else {
                        if (index < 3) 0.42f else 0.22f
                    }
                ),
                style = Stroke(
                    width = size.minDimension * (0.0138f - (progress * 0.00088f)),
                    cap = StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
            drawPath(
                path = contour,
                brush = lineBrush,
                style = Stroke(
                    width = size.minDimension * (0.0078f - (progress * 0.00058f)),
                    cap = StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
        }
    }
}

@Composable
private fun WaveGrainOverlay(seed: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (width <= 1f || height <= 1f) return@Canvas

        var state = (seed.toLong() xor 0x5DEECE66DL)
        fun nextFloat(): Float {
            state = state xor (state shl 13)
            state = state xor (state ushr 7)
            state = state xor (state shl 17)
            val value = (state and 0x7FFFFFFF).toFloat() / 2147483647f
            return value.coerceIn(0f, 1f)
        }

        repeat(420) {
            val x = nextFloat() * width
            val y = nextFloat() * height
            val radius = 0.45f + (nextFloat() * 0.95f)
            val alpha = 0.010f + (nextFloat() * 0.028f)
            val color = if (nextFloat() > 0.5f) {
                Color.White.copy(alpha = alpha)
            } else {
                Color.Black.copy(alpha = alpha * 0.75f)
            }
            drawCircle(color = color, radius = radius, center = Offset(x, y))
        }
    }
}

private fun buildWavePaletteForTrack(track: TrackItem): List<Color> {
    val artworkPath = track.artworkPath
    val bitmap = when {
        !artworkPath.isNullOrBlank() -> decodeArtworkBitmapRaw(artworkPath, maxSize = 512)
            ?: decodeEmbeddedArtworkBitmapRaw(track.filePath, maxSize = 512)
        else -> decodeEmbeddedArtworkBitmapRaw(track.filePath, maxSize = 512)
    }

    if (bitmap != null) {
        val extracted = extractWavePaletteFromBitmap(bitmap)
        if (extracted.size >= 4) {
            return extracted.take(4)
        }
        if (extracted.size >= 2) {
            return buildList(capacity = 4) {
                repeat(4) { idx -> add(extracted[idx % extracted.size]) }
            }
        }
        if (extracted.size == 1) {
            val base = extracted.first()
            return listOf(base, base, blendColors(base, Color.Black, 0.10f), blendColors(base, Color.White, 0.08f))
        }
    }

    return defaultWavePalette()
}

private fun extractWavePaletteFromBitmap(bitmap: android.graphics.Bitmap): List<Color> {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= 0 || height <= 0) {
        return emptyList()
    }

    data class Bucket(var weight: Double = 0.0, var r: Double = 0.0, var g: Double = 0.0, var b: Double = 0.0)
    val buckets = HashMap<Int, Bucket>(192)
    val hsv = FloatArray(3)
    val stepX = maxOf(1, width / 36)
    val stepY = maxOf(1, height / 36)
    val cx = (width - 1) * 0.5f
    val cy = (height - 1) * 0.5f
    val maxDist = kotlin.math.sqrt((cx * cx) + (cy * cy)).coerceAtLeast(1f)

    var y = 0
    while (y < height) {
        var x = 0
        while (x < width) {
            val pixel = bitmap.getPixel(x, y)
            val alpha = android.graphics.Color.alpha(pixel)
            if (alpha >= 180) {
                android.graphics.Color.colorToHSV(pixel, hsv)
                val saturation = hsv[1]
                val value = hsv[2]
                if (saturation >= 0.12f && value >= 0.10f) {
                    val r = android.graphics.Color.red(pixel)
                    val g = android.graphics.Color.green(pixel)
                    val b = android.graphics.Color.blue(pixel)
                    val qr = (r / 24) * 24
                    val qg = (g / 24) * 24
                    val qb = (b / 24) * 24
                    val key = (qr shl 16) or (qg shl 8) or qb

                    val dx = x - cx
                    val dy = y - cy
                    val distNorm = kotlin.math.sqrt((dx * dx) + (dy * dy)) / maxDist
                    val centerBias = (1.0 - (distNorm * 0.45)).coerceIn(0.45, 1.10)
                    val alphaNorm = alpha / 255.0
                    val weight = (0.36 + (saturation * 0.46) + (value * 0.18)) * alphaNorm * centerBias

                    val bucket = buckets.getOrPut(key) { Bucket() }
                    bucket.weight += weight
                    bucket.r += r * weight
                    bucket.g += g * weight
                    bucket.b += b * weight
                }
            }
            x += stepX
        }
        y += stepY
    }

    if (buckets.isEmpty()) {
        return emptyList()
    }

    val ranked = buckets.values
        .filter { it.weight > 0.0 }
        .sortedByDescending { it.weight }

    if (ranked.isEmpty()) {
        return emptyList()
    }

    val dominantBucket = ranked.first()
    val dominantRed = (dominantBucket.r / dominantBucket.weight).toInt().coerceIn(0, 255)
    val dominantGreen = (dominantBucket.g / dominantBucket.weight).toInt().coerceIn(0, 255)
    val dominantBlue = (dominantBucket.b / dominantBucket.weight).toInt().coerceIn(0, 255)
    val dominantColor = Color(android.graphics.Color.rgb(dominantRed, dominantGreen, dominantBlue))
    val topWeight = dominantBucket.weight

    fun colorDistance(left: Color, right: Color): Double {
        val dr = left.red - right.red
        val dg = left.green - right.green
        val db = left.blue - right.blue
        return kotlin.math.sqrt((dr * dr + dg * dg + db * db).toDouble())
    }

    val picked = mutableListOf<Color>()
    for (bucket in ranked) {
        if (bucket.weight < (topWeight * 0.22) && picked.isNotEmpty()) {
            continue
        }
        val r = (bucket.r / bucket.weight).toInt().coerceIn(0, 255)
        val g = (bucket.g / bucket.weight).toInt().coerceIn(0, 255)
        val bl = (bucket.b / bucket.weight).toInt().coerceIn(0, 255)
        val out = FloatArray(3)
        android.graphics.Color.RGBToHSV(r, g, bl, out)
        out[1] = out[1].coerceIn(0.20f, 0.88f)
        out[2] = out[2].coerceIn(0.24f, 0.92f)
        val baseColor = Color(android.graphics.Color.HSVToColor(out))
        val color = blendColors(baseColor, dominantColor, 0.20f)

        val distinct = picked.none { existing -> colorDistance(existing, color) < 0.18 }
        if (distinct || picked.isEmpty()) {
            picked.add(color)
            if (picked.size >= 4) {
                break
            }
        }
    }

    if (picked.isEmpty()) {
        return emptyList()
    }
    if (picked.size >= 4) {
        return picked.take(4)
    }

    val seed = picked.toList()
    var idx = 0
    while (picked.size < 4) {
        picked.add(seed[idx % seed.size])
        idx += 1
    }
    return picked
}

private fun blendColors(from: Color, to: Color, ratio: Float): Color {
    val t = ratio.coerceIn(0f, 1f)
    return Color(
        red = from.red + ((to.red - from.red) * t),
        green = from.green + ((to.green - from.green) * t),
        blue = from.blue + ((to.blue - from.blue) * t),
        alpha = from.alpha + ((to.alpha - from.alpha) * t)
    )
}

@Composable
private fun HomeRecommendationCard(
    track: TrackItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(184.dp)
            .clickable(onClick = onClick)
    ) {
        TrackArtwork(
            track = track,
            placeholderRes = R.drawable.tab_note,
            placeholderSize = 48.dp,
            decodeMaxSize = 512,
            modifier = Modifier
                .fillMaxWidth()
                .height(184.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = track.displayTitle(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (track.artist.isNotBlank()) {
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HomeCompactTrackRow(
    track: TrackItem,
    onClick: () -> Unit
) {
    val rowBackground = rememberHomeLastAddedBackgroundColor(track)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(rowBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrackArtwork(
            track = track,
            placeholderRes = R.drawable.tab_note,
            placeholderSize = 20.dp,
            decodeMaxSize = 144,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.displayTitle(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (track.artist.isNotBlank()) {
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HomeLastAddedTrackRow(
    track: TrackItem,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val rowBackground = rememberHomeLastAddedBackgroundColor(track)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(rowBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isCurrent && isPlaying) {
                Image(
                    painter = painterResource(R.drawable.ic_global_pause),
                    contentDescription = "Playing",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.size(17.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                TrackArtwork(
                    track = track,
                    placeholderRes = R.drawable.tab_note,
                    placeholderSize = 20.dp,
                    decodeMaxSize = 144,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.displayTitle(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (track.artist.isNotBlank()) {
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HomeAlbumCard(
    album: HomeAlbumUiItem,
    coverTrack: TrackItem?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(126.dp)
            .clickable(onClick = onClick)
    ) {
        TrackArtwork(
            track = coverTrack,
            placeholderRes = R.drawable.tab_lib,
            placeholderSize = 36.dp,
            decodeMaxSize = 320,
            modifier = Modifier
                .fillMaxWidth()
                .height(126.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = album.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HistoryPage(
    listState: androidx.compose.foundation.lazy.LazyListState,
    tracks: List<TrackItem>,
    bottomInset: androidx.compose.ui.unit.Dp,
    currentTrackID: String?,
    isPlaying: Boolean,
    onTrackTap: (TrackItem) -> Unit
) {
    if (tracks.isEmpty()) {
        EmptyListLabel(text = "No listening history yet.")
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 2.dp, bottom = bottomInset)
    ) {
        itemsIndexed(tracks, key = { _, item -> item.id }) { index, track ->
            TrackRow(
                track = track,
                isCurrent = currentTrackID == track.id,
                showsPlaybackIndicator = (currentTrackID == track.id && isPlaying),
                onClick = { onTrackTap(track) },
                onLongPress = { onTrackTap(track) },
                startActions = emptyList(),
                endActions = emptyList()
            )
            if (index < tracks.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 54.dp, end = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            }
        }
    }
}

@Composable
private fun HistoryTrackRow(
    track: TrackItem,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(34.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isCurrent && isPlaying) {
                Image(
                    painter = painterResource(R.drawable.ic_global_pause),
                    contentDescription = "Playing",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.size(17.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                TrackArtwork(
                    track = track,
                    placeholderRes = R.drawable.tab_note,
                    placeholderSize = 18.dp,
                    decodeMaxSize = 128,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.displayTitle(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (track.artist.isNotBlank()) {
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SettingsPage(
    settings: SonoraAppSettings,
    storageUsedBytes: Long,
    onlinePlaylistCacheUsedBytes: Long,
    appUpdateState: AndroidAppUpdateState,
    appVersionLabel: String,
    githubProjectLabel: String,
    storageRootPath: String,
    libraryTrackCount: Int,
    onSettingsChange: (SonoraAppSettings) -> Unit,
    onClearOnlinePlaylistCache: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onInstallUpdate: (AndroidAppUpdateRelease) -> Unit,
    onCancelUpdateDownload: () -> Unit,
    onOpenGithub: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit
) {
    val selectedTrackGap = nearestTrackGapSecondsOption(settings.trackGapSeconds)
    val selectedMaxStorage = nearestMaxStorageOptionMb(settings.maxStorageMb)
    val selectedOnlinePlaylistCache = nearestMaxStorageOptionMb(settings.onlinePlaylistCacheMaxMb)
    val hasStorageLimit = selectedMaxStorage > 0
    val maxStorageBytes = if (hasStorageLimit) {
        selectedMaxStorage.toLong() * 1_048_576L
    } else {
        Long.MAX_VALUE
    }
    val usedStorageLabel = remember(storageUsedBytes) { formatStorageSize(storageUsedBytes) }
    val overLimit = hasStorageLimit && storageUsedBytes > maxStorageBytes
    val hasOnlinePlaylistCacheLimit = selectedOnlinePlaylistCache > 0
    val onlinePlaylistCacheBytes = if (hasOnlinePlaylistCacheLimit) {
        selectedOnlinePlaylistCache.toLong() * 1_048_576L
    } else {
        Long.MAX_VALUE
    }
    val usedOnlinePlaylistCacheLabel = remember(onlinePlaylistCacheUsedBytes) {
        formatStorageSize(onlinePlaylistCacheUsedBytes)
    }
    val onlinePlaylistCacheOverLimit =
        hasOnlinePlaylistCacheLimit && onlinePlaylistCacheUsedBytes > onlinePlaylistCacheBytes
    val accentHex = normalizeHexColor(settings.accentHex) ?: DEFAULT_ACCENT_HEX
    val accentPreview = remember(accentHex) { resolveAccentColor(accentHex) }
    var showAccentColorDialog by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
    ) {
        item {
            SettingsSectionHeading(text = "Customization")
        }
        item {
            SettingsCard {
                SettingsChoiceRow(
                    title = "Slider in player",
                    subtitle = "Show wave timeline or clean line",
                    options = PlayerSliderStyle.values().map { it.label },
                    selectedIndex = PlayerSliderStyle.values().indexOf(settings.sliderStyle),
                    onSelect = { index ->
                        val selected = PlayerSliderStyle.values().getOrElse(index) { PlayerSliderStyle.Wave }
                        onSettingsChange(settings.copy(sliderStyle = selected))
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsSliderStylePreview(useWaveSlider = settings.sliderStyle == PlayerSliderStyle.Wave)
                Spacer(modifier = Modifier.height(12.dp))
                SettingsChoiceRow(
                    title = "Artwork style",
                    subtitle = "Cover shape in player",
                    options = ArtworkStyle.values().map { it.label },
                    selectedIndex = ArtworkStyle.values().indexOf(settings.artworkStyle),
                    onSelect = { index ->
                        val selected = ArtworkStyle.values().getOrElse(index) { ArtworkStyle.Square }
                        onSettingsChange(settings.copy(artworkStyle = selected))
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                SettingsColorPickerRow(
                    title = "Accent color",
                    subtitle = "Custom color with #RRGGBB",
                    valueLabel = accentHex,
                    color = accentPreview,
                    onClick = {
                        showAccentColorDialog = true
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                SettingsChoiceRow(
                    title = "Font",
                    subtitle = "Player title and artist",
                    options = AppFontStyle.values().map { it.label },
                    selectedIndex = AppFontStyle.values().indexOf(settings.fontStyle),
                    onSelect = { index ->
                        val selected = AppFontStyle.values().getOrElse(index) { AppFontStyle.System }
                        onSettingsChange(settings.copy(fontStyle = selected))
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                SettingsChoiceRow(
                    title = "My Wave look",
                    subtitle = "Contours or previous cloud animation",
                    options = MyWaveLook.values().map { it.label },
                    selectedIndex = MyWaveLook.values().indexOf(settings.myWaveLook),
                    onSelect = { index ->
                        val selected = MyWaveLook.values().getOrElse(index) { MyWaveLook.Contours }
                        onSettingsChange(settings.copy(myWaveLook = selected))
                    }
                )
            }
        }

        item {
            SettingsSectionHeading(text = "Sound")
        }
        item {
            SettingsCard {
                SettingsScrollableChoiceRow(
                    title = "Delay between tracks",
                    subtitle = "",
                    options = TrackGapSecondsOptions.map { formatTrackGapOptionLabel(it) },
                    selectedIndex = TrackGapSecondsOptions.indexOf(selectedTrackGap).coerceAtLeast(0),
                    onSelect = { index ->
                        val selected = TrackGapSecondsOptions.getOrElse(index) { selectedTrackGap }
                        onSettingsChange(settings.copy(trackGapSeconds = selected))
                    }
                )
            }
        }

        item {
            SettingsSectionHeading(text = "Memory")
        }
        item {
            SettingsCard {
                SettingsInfoRow(
                    title = "Used by app + songs",
                    value = usedStorageLabel,
                    valueColor = if (overLimit) Color(0xFFD93025) else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsScrollableChoiceRow(
                    title = "Max player space",
                    subtitle = if (!hasStorageLimit) {
                        "No storage limit"
                    } else if (overLimit) {
                        "Current usage is above limit. New imports are blocked."
                    } else {
                        "Concrete storage presets"
                    },
                    options = MaxStorageMbOptions.map { formatMaxStorageOptionLabel(it) },
                    selectedIndex = MaxStorageMbOptions.indexOf(selectedMaxStorage).coerceAtLeast(0),
                    onSelect = { index ->
                        val selected = MaxStorageMbOptions.getOrElse(index) { selectedMaxStorage }
                        onSettingsChange(settings.copy(maxStorageMb = selected))
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                SettingsSwitchRow(
                    title = "Preserve player settings",
                    subtitle = "Keep shuffle/repeat after app restart",
                    checked = settings.preservePlayerModes,
                    onCheckedChange = { enabled ->
                        onSettingsChange(settings.copy(preservePlayerModes = enabled))
                    }
                )
            }
        }

        item {
            SettingsSectionHeading(text = "Cache")
        }
        item {
            SettingsCard {
                SettingsSwitchRow(
                    title = "Cache tracks from online playlists",
                    subtitle = "Keep liked shared playlists available offline",
                    checked = settings.cacheOnlinePlaylistTracks,
                    onCheckedChange = { enabled ->
                        onSettingsChange(settings.copy(cacheOnlinePlaylistTracks = enabled))
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                SettingsInfoRow(
                    title = "Used by online playlists",
                    value = usedOnlinePlaylistCacheLabel,
                    valueColor = if (onlinePlaylistCacheOverLimit) Color(0xFFD93025) else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsScrollableChoiceRow(
                    title = "Max online cache space",
                    subtitle = if (!settings.cacheOnlinePlaylistTracks) {
                        "Caching is off"
                    } else if (!hasOnlinePlaylistCacheLimit) {
                        "No storage limit"
                    } else if (onlinePlaylistCacheOverLimit) {
                        "Current usage is above limit. Old cache will be evicted."
                    } else {
                        "Concrete storage presets"
                    },
                    options = MaxStorageMbOptions.map { formatMaxStorageOptionLabel(it) },
                    selectedIndex = MaxStorageMbOptions.indexOf(selectedOnlinePlaylistCache).coerceAtLeast(0),
                    onSelect = { index ->
                        val selected = MaxStorageMbOptions.getOrElse(index) { selectedOnlinePlaylistCache }
                        onSettingsChange(settings.copy(onlinePlaylistCacheMaxMb = selected))
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                SettingsLinkRow(
                    title = "Clear online cache",
                    value = "Delete",
                    onClick = onClearOnlinePlaylistCache
                )
            }
        }

        item {
            SettingsSectionHeading(text = "Updates")
        }
        item {
            SettingsCard {
                SettingsLinkRow(
                    title = "Check for updates",
                    value = if (appUpdateState.checking) "Checking..." else "Check now",
                    onClick = {
                        if (!appUpdateState.checking) {
                            onCheckForUpdates()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (appUpdateState.checking) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Checking backend for new Android build...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (appUpdateState.updateAvailable && appUpdateState.latestRelease != null) {
                    val latestRelease = appUpdateState.latestRelease
                    if (latestRelease != null) {
                        AndroidAppUpdateCardGate(
                            release = latestRelease,
                            updateState = appUpdateState,
                            onUpdate = { onInstallUpdate(latestRelease) },
                            onCancel = onCancelUpdateDownload
                        )
                    }
                } else {
                    SettingsInfoRow(
                        title = "Status",
                        value = appUpdateState.statusMessage ?: "Installed version: $appVersionLabel"
                    )
                }
            }
        }

        item {
            SettingsSectionHeading(text = "About")
        }
        item {
            SettingsCard {
                SettingsLinkRow(
                    title = "GitHub project",
                    value = githubProjectLabel,
                    onClick = onOpenGithub
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsInfoRow(
                    title = "Developers",
                    value = "hippopotamus"
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsInfoRow(
                    title = "Version",
                    value = appVersionLabel
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsInfoRow(
                    title = "Tracks in library",
                    value = libraryTrackCount.toString()
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsInfoRow(
                    title = "Storage path",
                    value = storageRootPath
                )
            }
        }

        item {
            SettingsSectionHeading(text = "Backup")
        }
        item {
            SettingsCard {
                SettingsLinkRow(
                    title = "Export backup",
                    value = "Create .sonoraarc archive",
                    onClick = onExportBackup
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingsLinkRow(
                    title = "Import backup",
                    value = "Restore songs, playlists, favorites",
                    onClick = onImportBackup
                )
            }
        }
    }

    if (showAccentColorDialog) {
        AccentColorPickerDialog(
            selectedHex = accentHex,
            onDismiss = { showAccentColorDialog = false },
            onColorSelected = { selected ->
                onSettingsChange(settings.copy(accentHex = selected))
                showAccentColorDialog = false
            }
        )
    }
}

@Composable
private fun SettingsSectionHeading(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = SonoraAndroidYSMusicFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val cardBackground = if (isDark) {
        Color.White.copy(alpha = 0.06f)
    } else {
        Color.Black.copy(alpha = 0.03f)
    }
    val cardBorder = if (isDark) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color.Black.copy(alpha = 0.09f)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = LocalAccentColor.current
            )
        )
    }
}

@Composable
private fun SettingsChoiceRow(
    title: String,
    subtitle: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = selectedIndex == index
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelect(index) },
                color = if (selected) {
                    LocalAccentColor.current
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                }
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (selected) Color.Black else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsScrollableChoiceRow(
    title: String,
    subtitle: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    var expanded by remember(title, options) { mutableStateOf(false) }
    val resolvedIndex = if (options.isEmpty()) {
        -1
    } else {
        selectedIndex.coerceIn(0, options.lastIndex)
    }
    val selectedLabel = if (resolvedIndex >= 0) {
        options[resolvedIndex]
    } else {
        "-"
    }

    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.onSurface
    )
    if (subtitle.isNotBlank()) {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(modifier = Modifier.height(8.dp))

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(enabled = options.isNotEmpty()) {
                    expanded = true
                },
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Open options",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded && options.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, label ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    },
                    leadingIcon = if (index == resolvedIndex) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = LocalAccentColor.current,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        expanded = false
                        onSelect(index)
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsColorPickerRow(
    title: String,
    subtitle: String,
    valueLabel: String,
    color: Color,
    onClick: () -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun AccentColorPickerDialog(
    selectedHex: String,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    val initialColor = remember(selectedHex) { resolveAccentColor(selectedHex) }
    var red by rememberSaveable(selectedHex) {
        mutableIntStateOf((initialColor.red * 255f).roundToInt().coerceIn(0, 255))
    }
    var green by rememberSaveable(selectedHex) {
        mutableIntStateOf((initialColor.green * 255f).roundToInt().coerceIn(0, 255))
    }
    var blue by rememberSaveable(selectedHex) {
        mutableIntStateOf((initialColor.blue * 255f).roundToInt().coerceIn(0, 255))
    }
    var hexInput by rememberSaveable(selectedHex) { mutableStateOf(formatColorHex(initialColor)) }

    val previewColor = remember(red, green, blue) {
        Color(red / 255f, green / 255f, blue / 255f, 1f)
    }
    val normalizedHex = remember(red, green, blue) { formatColorHex(previewColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Accent color") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(previewColor)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = normalizedHex,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextField(
                    value = hexInput,
                    onValueChange = { value ->
                        var cleaned = value.uppercase()
                            .filter { it == '#' || it in '0'..'9' || it in 'A'..'F' }
                        cleaned = if (cleaned.startsWith("#")) cleaned.drop(1) else cleaned
                        cleaned = cleaned.take(6)
                        hexInput = if (cleaned.isEmpty()) "#" else "#$cleaned"

                        if (cleaned.length == 6) {
                            parseHexColor("#$cleaned")?.let { parsed ->
                                red = (parsed.red * 255f).roundToInt().coerceIn(0, 255)
                                green = (parsed.green * 255f).roundToInt().coerceIn(0, 255)
                                blue = (parsed.blue * 255f).roundToInt().coerceIn(0, 255)
                                hexInput = formatColorHex(parsed)
                            }
                        }
                    },
                    singleLine = true,
                    label = { Text("Custom #RRGGBB") },
                    modifier = Modifier.fillMaxWidth()
                )

                AccentChannelSlider(
                    title = "R",
                    value = red,
                    trackColor = Color(0xFFE65A5A),
                    onValueChange = { updated ->
                        red = updated
                        hexInput = formatColorHex(Color(red / 255f, green / 255f, blue / 255f, 1f))
                    }
                )
                AccentChannelSlider(
                    title = "G",
                    value = green,
                    trackColor = Color(0xFF47B35D),
                    onValueChange = { updated ->
                        green = updated
                        hexInput = formatColorHex(Color(red / 255f, green / 255f, blue / 255f, 1f))
                    }
                )
                AccentChannelSlider(
                    title = "B",
                    value = blue,
                    trackColor = Color(0xFF4F90FF),
                    onValueChange = { updated ->
                        blue = updated
                        hexInput = formatColorHex(Color(red / 255f, green / 255f, blue / 255f, 1f))
                    }
                )
            }
        },
        dismissButton = {
            AccentTextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            AccentTextButton(
                onClick = {
                    onColorSelected(normalizedHex)
                }
            ) {
                Text("Apply")
            }
        }
    )
}

@Composable
private fun AccentChannelSlider(
    title: String,
    value: Int,
    trackColor: Color,
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { updated ->
                onValueChange(updated.roundToInt().coerceIn(0, 255))
            },
            valueRange = 0f..255f,
            steps = 254,
            colors = SliderDefaults.colors(
                thumbColor = trackColor,
                activeTrackColor = trackColor,
                inactiveTrackColor = trackColor.copy(alpha = 0.24f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SettingsSliderStylePreview(useWaveSlider: Boolean) {
    val activeColor = MaterialTheme.colorScheme.onSurface
    val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Preview",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "How timeline looks in player",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
        ) {
            val progress = 0.42f
            val progressX = size.width * progress
            val centerY = size.height / 2f
            val lineHeight = 2.dp.toPx()

            if (progressX > 0f) {
                if (useWaveSlider) {
                    val path = androidx.compose.ui.graphics.Path()
                    val waveStep = 2.dp.toPx().coerceAtLeast(1f)
                    val wavelength = 18.dp.toPx()
                    val amplitude = 2.1.dp.toPx()
                    val rampLength = 12.dp.toPx()
                    val twoPi = (Math.PI * 2).toFloat()

                    var x = 0f
                    path.moveTo(0f, centerY)
                    while (x <= progressX) {
                        val phase = ((x / wavelength) * twoPi)
                        val envelope = (x / rampLength).coerceIn(0f, 1f)
                        val y = centerY + (sin(phase.toDouble()).toFloat() * amplitude * envelope)
                        path.lineTo(x, y)
                        x += waveStep
                    }
                    drawPath(
                        path = path,
                        color = activeColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 1.8.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                        )
                    )
                } else {
                    drawRoundRect(
                        color = activeColor,
                        topLeft = Offset(0f, centerY - (lineHeight * 0.5f)),
                        size = Size(progressX, lineHeight),
                        cornerRadius = CornerRadius(lineHeight * 0.5f, lineHeight * 0.5f)
                    )
                }
            }

            if (progressX < size.width) {
                drawRoundRect(
                    color = inactiveColor,
                    topLeft = Offset(progressX, centerY - (lineHeight * 0.5f)),
                    size = Size(size.width - progressX, lineHeight),
                    cornerRadius = CornerRadius(lineHeight * 0.5f, lineHeight * 0.5f)
                )
            }

            val thumbWidth = 4.dp.toPx()
            val thumbHeight = 18.dp.toPx()
            drawRoundRect(
                color = activeColor,
                topLeft = Offset(
                    (progressX - (thumbWidth * 0.5f)).coerceIn(0f, size.width - thumbWidth),
                    centerY - (thumbHeight * 0.5f)
                ),
                size = Size(thumbWidth, thumbHeight),
                cornerRadius = CornerRadius(thumbWidth * 0.5f, thumbWidth * 0.5f)
            )
        }
    }
}

@Composable
private fun SettingsSliderRow(
    title: String,
    subtitle: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = valueLabel,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.onSurface
    )
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps
    )
}

@Composable
private fun SettingsInfoRow(
    title: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        ),
        color = valueColor
    )
}

@Composable
private fun AndroidAppUpdateCardGate(
    release: AndroidAppUpdateRelease,
    updateState: AndroidAppUpdateState,
    onUpdate: () -> Unit,
    onCancel: () -> Unit
) {
    val coverBitmap by produceState<android.graphics.Bitmap?>(initialValue = null, release.coverUrl) {
        if (release.coverUrl.isNullOrBlank()) {
            value = null
            return@produceState
        }
        value = withContext(Dispatchers.IO) {
            runCatching {
                val connection = (URL(release.coverUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    instanceFollowRedirects = true
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    setRequestProperty("User-Agent", "SonoraAndroid/1.0")
                }
                connection.connect()
                if (connection.responseCode !in 200..299) {
                    connection.disconnect()
                    return@runCatching null
                }
                val bytes = connection.inputStream.use { it.readBytes() }
                connection.disconnect()
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }.getOrNull()
        }
    }

    if (!release.coverUrl.isNullOrBlank() && coverBitmap == null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Loading update...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    AndroidAppUpdateCard(
        release = release,
        coverBitmap = coverBitmap,
        updateState = updateState,
        onUpdate = onUpdate,
        onCancel = onCancel
    )
}

@Composable
private fun AndroidAppUpdateCard(
    release: AndroidAppUpdateRelease,
    coverBitmap: android.graphics.Bitmap?,
    updateState: AndroidAppUpdateState,
    onUpdate: () -> Unit,
    onCancel: () -> Unit
) {
    Spacer(modifier = Modifier.height(2.dp))
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StaticUpdateCover(
                bitmap = coverBitmap,
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = release.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Version ${release.versionName} (${release.versionCode})",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (release.notes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                release.notes.take(6).forEach { note ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (updateState.downloading) {
            Spacer(modifier = Modifier.height(14.dp))
            WaveDownloadProgressBar(
                progress = updateState.downloadProgress.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = updateState.statusMessage ?: "Downloading update...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (!updateState.statusMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = updateState.statusMessage.orEmpty(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp))
                .background(if (updateState.downloading) Color(0xFFFF3B30) else Color(0xFF0A84FF))
                .clickable(onClick = if (updateState.downloading) onCancel else onUpdate)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when {
                    updateState.downloading -> "Cancel"
                    updateState.downloadReady -> "Install update"
                    else -> "Update"
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun WaveDownloadProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "update_wave_progress")
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val waveShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1700, easing = LinearEasing)
        ),
        label = "update_wave_progress_shift"
    )
    Canvas(modifier = modifier) {
        val progressValue = progress.coerceIn(0f, 1f)
        val centerY = size.height * 0.5f
        val lineHeight = 2.dp.toPx()
        val activeWidth = size.width * progressValue
        val wavelength = 22.dp.toPx()
        val amplitude = 2.2.dp.toPx()
        val step = 2.dp.toPx().coerceAtLeast(1f)
        val phaseOffset = waveShift * (Math.PI.toFloat() * 2f)

        drawRoundRect(
            color = trackColor,
            topLeft = Offset(0f, centerY - (lineHeight * 0.5f)),
            size = Size(size.width, lineHeight),
            cornerRadius = CornerRadius(lineHeight * 0.5f, lineHeight * 0.5f)
        )

        if (activeWidth > 0f) {
            val wavePath = Path()
            var x = 0f
            wavePath.moveTo(0f, centerY)
            while (x <= activeWidth) {
                val y = centerY + (sin(((x / wavelength) * (Math.PI.toFloat() * 2f) + phaseOffset).toDouble()).toFloat() * amplitude)
                wavePath.lineTo(x, y)
                x += step
            }
            drawPath(
                path = wavePath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF3EA2FF),
                        Color(0xFF0A84FF),
                        Color(0xFF69B9FF)
                    ),
                    start = Offset(0f, centerY),
                    end = Offset(activeWidth.coerceAtLeast(1f), centerY)
                ),
                style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun StaticUpdateCover(
    bitmap: android.graphics.Bitmap?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = "APK",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
private fun SettingsLinkRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        SettingsInfoRow(title = title, value = value)
    }
}

@Composable
private fun PlaylistsListPage(
    listState: androidx.compose.foundation.lazy.LazyListState,
    playlists: List<PlaylistUiItem>,
    trackByID: Map<String, TrackItem>,
    bottomInset: androidx.compose.ui.unit.Dp,
    onPlaylistTap: (PlaylistUiItem) -> Unit,
    onCreatePlaylistTap: () -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 2.dp, bottom = bottomInset)
    ) {
        if (playlists.isEmpty()) {
            item(key = "playlists_list_empty") {
                EmptyListLabel(text = "Tap + to create a playlist")
            }
        } else {
            itemsIndexed(playlists, key = { _, item -> item.id }) { index, playlist ->
                PlaylistRow(
                    item = playlist,
                    coverTrack = playlist.trackIds.firstNotNullOfOrNull { trackByID[it] },
                    onClick = { onPlaylistTap(playlist) }
                )
                if (index < playlists.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp, end = 12.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                }
            }
        }

    }
}

@Composable
private fun NewPlaylistListRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_global_plus),
                contentDescription = "New playlist",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "New playlist",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PlaylistsPage(
    listState: androidx.compose.foundation.lazy.LazyListState,
    playlists: List<PlaylistUiItem>,
    trackByID: Map<String, TrackItem>,
    myMusicTracks: List<TrackItem>,
    favorites: List<TrackItem>,
    lastAddedTracks: List<TrackItem>,
    albums: List<HomeAlbumUiItem>,
    bottomInset: androidx.compose.ui.unit.Dp,
    currentTrackID: String?,
    isPlaying: Boolean,
    onFavoriteSummaryTap: () -> Unit,
    onFavoriteTrackTap: (TrackItem) -> Unit,
    onPlaylistTap: (PlaylistUiItem) -> Unit,
    onLastAddedTap: (TrackItem) -> Unit,
    onAlbumTap: (HomeAlbumUiItem) -> Unit,
    onCreatePlaylistTap: () -> Unit,
    onMyMusicTap: () -> Unit,
    onMyMusicTrackTap: (TrackItem) -> Unit,
    onPlaylistsHeaderTap: () -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 2.dp, bottom = bottomInset)
    ) {
        item(key = "favorites_summary") {
            CollectionsMyFavoritesSummaryRow(
                trackCount = favorites.size,
                onClick = onFavoriteSummaryTap
            )
        }
        if (favorites.isNotEmpty()) {
            item(key = "favorites_tracks_groups") {
                val grouped = favorites.chunked(2)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(grouped) { group ->
                        val groupHeight = if (group.size > 1) 140.dp else 66.dp
                        Column(
                            modifier = Modifier
                                .width(304.dp)
                                .height(groupHeight),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            group.forEach { track ->
                                CollectionsFavoriteTrackRow(
                                    track = track,
                                    isCurrent = currentTrackID == track.id,
                                    isPlaying = isPlaying,
                                    onClick = { onFavoriteTrackTap(track) }
                                )
                            }
                        }
                    }
                }
            }
        }

        item(key = "playlists_header") {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp)
                    .clickable(onClick = onPlaylistsHeaderTap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My playlists",
                    style = TextStyle(
                        fontFamily = SonoraAndroidYSMusicFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "›",
                    style = TextStyle(
                        fontFamily = SonoraAndroidYSMusicFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item(key = "playlists_cards_row") {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(playlists, key = { it.id }) { item ->
                    CollectionsPlaylistCard(
                        item = item,
                        coverTrack = item.trackIds.firstNotNullOfOrNull { trackByID[it] },
                        onClick = { onPlaylistTap(item) }
                    )
                }
                item(key = "new_playlist_card") {
                    CollectionsNewPlaylistCard(onClick = onCreatePlaylistTap)
                }
            }
        }

        if (lastAddedTracks.isNotEmpty()) {
            item(key = "collections_last_added_heading") {
                CollectionsSectionHeading(text = "Last added")
            }
            item(key = "collections_last_added_groups") {
                val grouped = lastAddedTracks.chunked(2)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(grouped) { group ->
                        Column(
                            modifier = Modifier
                                .width(304.dp)
                                .height(140.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            group.forEach { track ->
                                HomeLastAddedTrackRow(
                                    track = track,
                                    isCurrent = currentTrackID == track.id,
                                    isPlaying = isPlaying,
                                    onClick = { onLastAddedTap(track) }
                                )
                            }
                            if (group.size == 1) {
                                Spacer(modifier = Modifier.height(66.dp))
                            }
                        }
                    }
                }
            }
        }

        if (albums.isNotEmpty()) {
            item(key = "collections_albums_heading") {
                CollectionsSectionHeading(text = "Your albums")
            }
            item(key = "collections_albums_row") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(albums, key = { it.artistKey }) { album ->
                        HomeAlbumCard(
                            album = album,
                            coverTrack = trackByID[album.coverTrackId] ?: album.trackIds.firstNotNullOfOrNull { trackByID[it] },
                            onClick = { onAlbumTap(album) }
                        )
                    }
                }
            }
        }

        if (myMusicTracks.isNotEmpty()) {
            item(key = "collections_my_music_heading") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 18.dp, top = 10.dp, bottom = 8.dp)
                        .clickable(onClick = onMyMusicTap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My music",
                        style = TextStyle(
                            fontFamily = SonoraAndroidYSMusicFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 23.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "›",
                        style = TextStyle(
                            fontFamily = SonoraAndroidYSMusicFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item(key = "collections_my_music_groups") {
                val grouped = myMusicTracks.chunked(2)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(grouped) { group ->
                        val groupHeight = if (group.size > 1) 140.dp else 66.dp
                        Column(
                            modifier = Modifier
                                .width(304.dp)
                                .height(groupHeight),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            group.forEach { track ->
                                HomeLastAddedTrackRow(
                                    track = track,
                                    isCurrent = currentTrackID == track.id,
                                    isPlaying = isPlaying,
                                    onClick = { onMyMusicTrackTap(track) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionsMyFavoritesSummaryRow(
    trackCount: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .padding(horizontal = 18.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.lovely_cover),
            contentDescription = null,
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "My Favorites",
                    style = TextStyle(
                        fontFamily = SonoraAndroidYSMusicFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = "›",
                    style = TextStyle(
                        fontFamily = SonoraAndroidYSMusicFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$trackCount ${if (trackCount == 1) "track" else "tracks"}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CollectionsFavoriteTrackRow(
    track: TrackItem,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isCurrent && isPlaying) {
                Image(
                    painter = painterResource(R.drawable.ic_global_pause),
                    contentDescription = "Playing",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.size(17.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                TrackArtwork(
                    track = track,
                    placeholderRes = R.drawable.tab_note,
                    placeholderSize = 20.dp,
                    decodeMaxSize = 144,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.displayTitle(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (track.artist.isNotBlank()) {
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CollectionsPlaylistCard(
    item: PlaylistUiItem,
    coverTrack: TrackItem?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(184.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (item.isLovely) {
                Image(
                    painter = painterResource(R.drawable.lovely_cover),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (!item.customCoverPath.isNullOrBlank()) {
                LocalPathArtwork(
                    path = item.customCoverPath,
                    placeholderRes = R.drawable.tab_lib,
                    placeholderSize = 48.dp,
                    decodeMaxSize = 512,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                TrackArtwork(
                    track = coverTrack,
                    placeholderRes = R.drawable.tab_lib,
                    placeholderSize = 48.dp,
                    decodeMaxSize = 512,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = item.subtitle.ifBlank { "${item.trackIds.size} tracks" },
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CollectionsNewPlaylistCard(
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(184.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (androidx.compose.foundation.isSystemInDarkTheme()) {
                        Color.White.copy(alpha = 0.06f)
                    } else {
                        Color.Black.copy(alpha = 0.04f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_global_plus),
                contentDescription = "New playlist",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "New playlist",
            style = TextStyle(
                fontFamily = SonoraAndroidSFProSemiboldFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PlaylistDetailHeader(
    playlist: PlaylistUiItem,
    coverTrack: TrackItem?,
    customCoverPath: String?,
    accentColor: Color,
    isPlaying: Boolean,
    isSleepTimerActive: Boolean,
    onPlayPauseTap: () -> Unit,
    onShuffleTap: () -> Unit,
    onSleepTap: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.onSurface
    val playButtonColor = if (playlist.isLovely) {
        Color(0xFFE61F26)
    } else {
        accentColor
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(374.dp)
    ) {
        if (playlist.isLovely) {
            Image(
                painter = painterResource(R.drawable.lovely_cover),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .size(212.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        } else if (!customCoverPath.isNullOrBlank()) {
            LocalPathArtwork(
                path = customCoverPath,
                placeholderRes = R.drawable.tab_lib,
                placeholderSize = 92.dp,
                decodeMaxSize = 512,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .size(212.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        } else {
            TrackArtwork(
                track = coverTrack,
                placeholderRes = R.drawable.tab_lib,
                placeholderSize = 92.dp,
                decodeMaxSize = 512,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .size(212.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        Text(
            text = playlist.name,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 236.dp, start = 14.dp, end = 14.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            ),
            color = primaryColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 272.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PlainControlButton(
                iconRes = if (isSleepTimerActive) R.drawable.sleep_fill else R.drawable.sleep,
                contentDescription = "Sleep timer",
                onClick = onSleepTap,
                enabled = true,
                size = 46.dp,
                iconSize = 29.dp,
                iconWidth = 24.5.dp,
                iconHeight = 29.dp,
                tint = if (isSleepTimerActive) LocalAccentColor.current else primaryColor
            )

            Surface(
                modifier = Modifier
                    .size(66.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(onClick = onPlayPauseTap),
                color = playButtonColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(
                            if (isPlaying) R.drawable.ic_global_pause else R.drawable.ic_global_play
                        ),
                        contentDescription = if (isPlaying) "Pause playlist" else "Play playlist",
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier
                            .size(29.dp)
                            .offset(x = if (isPlaying) 0.dp else 1.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            PlainControlButton(
                iconRes = R.drawable.ic_player_shuffle,
                contentDescription = "Shuffle",
                onClick = onShuffleTap,
                enabled = true,
                size = 46.dp,
                iconSize = 29.dp,
                tint = primaryColor
            )
        }
    }
}

@Composable
private fun rememberPlaylistAccentColor(
    playlist: PlaylistUiItem,
    coverTrack: TrackItem?,
    customCoverPath: String?
): Color {
    val fallback = MaterialTheme.colorScheme.onSurface
    if (playlist.isLovely) {
        return Color(0xFFE61F26)
    }

    val accentKey = remember(
        playlist.id,
        coverTrack?.id,
        coverTrack?.artworkPath,
        coverTrack?.filePath,
        customCoverPath
    ) {
        buildPlaylistAccentKey(playlist, coverTrack, customCoverPath)
    }
    var accentColor by remember(accentKey) {
        mutableStateOf(playlistAccentCacheGet(accentKey) ?: fallback)
    }

    LaunchedEffect(accentKey) {
        val cached = playlistAccentCacheGet(accentKey)
        if (cached != null) {
            accentColor = cached
            return@LaunchedEffect
        }

        val resolved = withContext(Dispatchers.IO) {
            val bitmap = if (!customCoverPath.isNullOrBlank()) {
                decodeArtworkBitmapRaw(customCoverPath, maxSize = 96)
            } else {
                decodeTrackArtworkBitmapRaw(coverTrack, maxSize = 96)
            } ?: return@withContext null
            extractDominantAccentColor(bitmap)
        } ?: fallback

        playlistAccentCachePut(accentKey, resolved)
        accentColor = resolved
    }

    return accentColor
}

@Composable
private fun PlaylistDetailPage(
    listState: androidx.compose.foundation.lazy.LazyListState,
    playlist: PlaylistUiItem?,
    tracks: List<TrackItem>,
    canRemoveTracks: Boolean,
    currentTrackID: String?,
    isPlaying: Boolean,
    isCurrentQueueMatching: Boolean,
    isSleepTimerActive: Boolean,
    selectionMode: Boolean,
    selectedTrackIDs: Set<String>,
    onTrackTap: (TrackItem) -> Unit,
    onHeaderPlayPauseTap: () -> Unit,
    onHeaderShuffleTap: () -> Unit,
    onHeaderSleepTap: () -> Unit,
    onTrackLongPress: (TrackItem) -> Unit,
    onTrackSwipeFavorite: (TrackItem) -> Unit,
    onTrackSwipeRemove: (TrackItem) -> Unit
) {
    if (playlist == null) {
        EmptyListLabel(text = "Playlist not found")
        return
    }
    val playlistAccent = rememberPlaylistAccentColor(
        playlist = playlist,
        coverTrack = tracks.firstOrNull(),
        customCoverPath = playlist.customCoverPath
    )

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 2.dp, bottom = 2.dp)
    ) {
        item(key = "playlist_header") {
            PlaylistDetailHeader(
                playlist = playlist,
                coverTrack = tracks.firstOrNull(),
                customCoverPath = playlist.customCoverPath,
                accentColor = playlistAccent,
                isPlaying = isPlaying && isCurrentQueueMatching && currentTrackID != null,
                isSleepTimerActive = isSleepTimerActive,
                onPlayPauseTap = onHeaderPlayPauseTap,
                onShuffleTap = onHeaderShuffleTap,
                onSleepTap = onHeaderSleepTap
            )
        }

        if (tracks.isEmpty()) {
            item(key = "playlist_empty") {
                val message = if (playlist.isLovely) {
                    "Lovely songs will appear after listening activity."
                } else {
                    "No tracks in this playlist"
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            return@LazyColumn
        }

        itemsIndexed(tracks, key = { _, item -> item.id }) { index, track ->
            TrackRow(
                track = track,
                isCurrent = currentTrackID == track.id,
                showsPlaybackIndicator = (currentTrackID == track.id && isPlaying),
                selectionMode = selectionMode,
                isSelected = selectedTrackIDs.contains(track.id),
                onClick = { onTrackTap(track) },
                onLongPress = { onTrackLongPress(track) },
                activeColor = playlistAccent,
                startActions = emptyList(),
                endActions = if (selectionMode) {
                    emptyList()
                } else {
                    buildList {
                        if (canRemoveTracks) {
                            add(
                                SwipeTrackAction(
                                    label = "Remove",
                                    backgroundColor = Color(0xFFFF3B30),
                                    iconRes = R.drawable.ic_global_trash_fill,
                                    onAction = { onTrackSwipeRemove(track) }
                                )
                            )
                        }
                        if (!playlist.isSharedOnline) {
                            add(
                                SwipeTrackAction(
                                    label = if (track.isFavorite) "Unfav" else "Fav",
                                    backgroundColor = if (track.isFavorite) {
                                        Color(0xFF6A6A6A)
                                    } else {
                                        Color(0xFFFF5966)
                                    },
                                    iconRes = if (track.isFavorite) R.drawable.heart_slash_fill else R.drawable.heart_fill,
                                    onAction = { onTrackSwipeFavorite(track) },
                                    fullSwipeEnabled = true
                                )
                            )
                        }
                    }
                }
            )
            if (index < tracks.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 54.dp, end = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            }
        }
    }
}

@Composable
private fun FavoritesPage(
    listState: androidx.compose.foundation.lazy.LazyListState,
    allFavorites: List<TrackItem>,
    filteredTracks: List<TrackItem>,
    bottomInset: androidx.compose.ui.unit.Dp,
    currentTrackID: String?,
    isPlaying: Boolean,
    selectionMode: Boolean,
    selectedTrackIDs: Set<String>,
    onTrackTap: (TrackItem) -> Unit,
    onTrackLongPress: (TrackItem) -> Unit,
    onTrackSwipeUnfavorite: (TrackItem) -> Unit,
    onTrackSwipeAddToPlaylist: (TrackItem) -> Unit
) {
    if (filteredTracks.isEmpty()) {
        val message = if (allFavorites.isEmpty()) {
            "No favorites yet.\nTap heart in player to add tracks."
        } else {
            "No search results."
        }
        EmptyListLabel(text = message)
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 2.dp, bottom = bottomInset)
    ) {
        itemsIndexed(filteredTracks, key = { _, item -> item.id }) { index, track ->
            TrackRow(
                track = track,
                isCurrent = currentTrackID == track.id,
                showsPlaybackIndicator = (currentTrackID == track.id && isPlaying),
                selectionMode = selectionMode,
                isSelected = selectedTrackIDs.contains(track.id),
                onClick = { onTrackTap(track) },
                onLongPress = { onTrackLongPress(track) },
                startActions = emptyList(),
                endActions = if (selectionMode) {
                    emptyList()
                } else {
                    listOf(
                        SwipeTrackAction(
                            label = "Unfollow",
                            backgroundColor = Color(0xFF6E6E6E),
                            iconRes = R.drawable.heart_slash_fill,
                            onAction = { onTrackSwipeUnfavorite(track) }
                        ),
                        SwipeTrackAction(
                            label = "Add",
                            backgroundColor = Color(0xFF2979F2),
                            iconRes = R.drawable.ic_global_text_badge_plus,
                            onAction = { onTrackSwipeAddToPlaylist(track) }
                        )
                    )
                }
            )
            if (index < filteredTracks.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 54.dp, end = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            }
        }
    }
}

@Composable
private fun SwipeDismissPage(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.activity.compose.BackHandler(onBack = onDismiss)
    var dragX by remember { mutableFloatStateOf(0f) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(onDismiss) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        dragX = 0f
                    },
                    onDragCancel = {
                        dragX = 0f
                    },
                    onDragEnd = {
                        if (dragX > 88f) {
                            onDismiss()
                        }
                        dragX = 0f
                    }
                ) { change, dragAmount ->
                    if (dragAmount > 0f || dragX > 0f) {
                        dragX += dragAmount
                        change.consume()
                    }
                }
            }
    ) {
        content()
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun TrackRow(
    track: TrackItem,
    isCurrent: Boolean,
    showsPlaybackIndicator: Boolean,
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    activeColor: Color = LocalAccentColor.current,
    startActions: List<SwipeTrackAction>,
    endActions: List<SwipeTrackAction>
) {
    val density = LocalDensity.current
    val actionWidth = 74.dp
    val actionWidthPx = with(density) { actionWidth.toPx() }
    val maxStartOffset = if (startActions.isEmpty()) 0f else actionWidthPx * startActions.size
    val maxEndOffset = if (endActions.isEmpty()) 0f else actionWidthPx * endActions.size
    val fullSwipeTrigger = with(density) { 108.dp.toPx() }
    val rowBackground = if (isSelected) {
        LocalAccentColor.current.copy(alpha = 0.14f)
    } else {
        MaterialTheme.colorScheme.background
    }

    var dragOffsetPx by remember(track.id, startActions, endActions) { mutableFloatStateOf(0f) }
    val animatedOffsetPx by animateFloatAsState(
        targetValue = dragOffsetPx,
        animationSpec = tween(durationMillis = 160),
        label = "trackSwipeOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (startActions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    startActions.forEach { action ->
                        SwipeActionButton(
                            action = action,
                            width = actionWidth,
                            onAction = {
                                action.onAction()
                                dragOffsetPx = 0f
                            }
                        )
                    }
                }
            }

            if (endActions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.End
                ) {
                    endActions.reversed().forEach { action ->
                        SwipeActionButton(
                            action = action,
                            width = actionWidth,
                            onAction = {
                                action.onAction()
                                dragOffsetPx = 0f
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .offset {
                    androidx.compose.ui.unit.IntOffset(animatedOffsetPx.toInt(), 0)
                }
                .draggable(
                    orientation = Orientation.Horizontal,
                    enabled = !selectionMode && (startActions.isNotEmpty() || endActions.isNotEmpty()),
                    state = rememberDraggableState { delta ->
                        val nextOffset = dragOffsetPx + delta
                        dragOffsetPx = nextOffset.coerceIn(-maxEndOffset, maxStartOffset)
                    },
                    onDragStopped = {
                        when {
                            dragOffsetPx > 0f && startActions.isNotEmpty() -> {
                                val primaryAction = startActions.first()
                                if (primaryAction.fullSwipeEnabled && dragOffsetPx >= fullSwipeTrigger) {
                                    primaryAction.onAction()
                                    dragOffsetPx = 0f
                                } else if (dragOffsetPx >= maxStartOffset * 0.52f) {
                                    dragOffsetPx = maxStartOffset
                                } else {
                                    dragOffsetPx = 0f
                                }
                            }

                            dragOffsetPx < 0f && endActions.isNotEmpty() -> {
                                if (abs(dragOffsetPx) >= maxEndOffset * 0.52f) {
                                    dragOffsetPx = -maxEndOffset
                                } else {
                                    dragOffsetPx = 0f
                                }
                            }

                            else -> {
                                dragOffsetPx = 0f
                            }
                        }
                    }
                )
                .combinedClickable(
                    onClick = {
                        if (!selectionMode && dragOffsetPx != 0f) {
                            dragOffsetPx = 0f
                        } else {
                            onClick()
                        }
                    },
                    onLongClick = {
                        if (!selectionMode && dragOffsetPx != 0f) {
                            dragOffsetPx = 0f
                        } else {
                            onLongPress()
                        }
                    }
                )
                .background(rowBackground)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectionMode && isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = LocalAccentColor.current,
                        modifier = Modifier.size(18.dp)
                    )
                } else if (showsPlaybackIndicator && !selectionMode) {
                    Image(
                        painter = painterResource(R.drawable.ic_global_pause),
                        contentDescription = "Playing",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.size(17.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    TrackArtwork(
                        track = track,
                        placeholderRes = R.drawable.tab_note,
                        placeholderSize = 20.dp,
                        decodeMaxSize = 128,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = track.displayTitle(),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onSurface
                    isCurrent -> activeColor
                    else -> MaterialTheme.colorScheme.onSurface
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = formatDuration(track.durationMs),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = TextStyle(
                    fontFamily = SonoraAndroidSFProSemiboldFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Right,
                modifier = Modifier.widthIn(min = 44.dp)
            )
        }
    }
}

@Composable
private fun SwipeActionButton(
    action: SwipeTrackAction,
    width: Dp,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(width)
            .background(action.backgroundColor)
            .clickable(onClick = onAction),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(action.iconRes),
            contentDescription = action.label,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier.size(22.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun PlaylistRow(
    item: PlaylistUiItem,
    coverTrack: TrackItem?,
    onClick: () -> Unit
) {
    val coverSize = 34.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(coverSize),
            contentAlignment = Alignment.Center
        ) {
            if (item.isLovely) {
                Image(
                    painter = painterResource(R.drawable.lovely_cover),
                    contentDescription = null,
                    modifier = Modifier
                        .size(coverSize)
                        .clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
            } else if (!item.customCoverPath.isNullOrBlank()) {
                LocalPathArtwork(
                    path = item.customCoverPath,
                    placeholderRes = R.drawable.tab_lib,
                    placeholderSize = 18.dp,
                    decodeMaxSize = 144,
                    modifier = Modifier
                        .size(coverSize)
                        .clip(RoundedCornerShape(6.dp))
                )
            } else {
                TrackArtwork(
                    track = coverTrack,
                    placeholderRes = R.drawable.tab_lib,
                    placeholderSize = 18.dp,
                    decodeMaxSize = 144,
                    modifier = Modifier
                        .size(coverSize)
                        .clip(RoundedCornerShape(6.dp))
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (item.subtitle.isBlank()) " " else item.subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = if (item.subtitle.isBlank()) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MiniStreamingArtwork(
    artworkUrl: String?,
    placeholderRes: Int,
    placeholderSize: androidx.compose.ui.unit.Dp = 20.dp,
    decodeMaxSize: Int = 512,
    modifier: Modifier = Modifier
) {
    val cacheKey = remember(artworkUrl, decodeMaxSize) {
        val normalized = artworkUrl?.trim().orEmpty()
        if (normalized.isBlank()) {
            null
        } else {
            "mini|$normalized|$decodeMaxSize"
        }
    }

    var imageBitmap by remember(cacheKey) {
        mutableStateOf(cacheKey?.let { artworkCacheGet(it) })
    }
    var isLoading by remember(cacheKey) {
        mutableStateOf(cacheKey != null && imageBitmap == null)
    }

    LaunchedEffect(cacheKey, artworkUrl) {
        val key = cacheKey ?: run {
            imageBitmap = null
            isLoading = false
            return@LaunchedEffect
        }
        if (imageBitmap != null) {
            isLoading = false
            return@LaunchedEffect
        }
        if (artworkMissingCacheContains(key)) {
            imageBitmap = null
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        val url = artworkUrl?.trim().orEmpty()
        val decoded = loadArtworkBitmapCached(key) {
            decodeArtworkBitmap(url, maxSize = decodeMaxSize)
        }
        imageBitmap = decoded
        isLoading = false
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap!!,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(placeholderRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.size(placeholderSize),
                contentScale = ContentScale.Fit
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.22f))
            )
            CircularProgressIndicator(
                modifier = Modifier.size((placeholderSize.value + 8f).dp),
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
private fun TrackArtwork(
    track: TrackItem?,
    placeholderRes: Int,
    placeholderSize: androidx.compose.ui.unit.Dp = 20.dp,
    decodeMaxSize: Int = 512,
    modifier: Modifier = Modifier
) {
    val imageBitmap = rememberTrackArtwork(track, decodeMaxSize)

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(placeholderRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.size(placeholderSize),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun LocalPathArtwork(
    path: String?,
    placeholderRes: Int,
    placeholderSize: androidx.compose.ui.unit.Dp = 20.dp,
    decodeMaxSize: Int = 512,
    modifier: Modifier = Modifier
) {
    val imageBitmap = rememberArtworkByPath(path, decodeMaxSize)

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(placeholderRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.size(placeholderSize),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun rememberTrackArtwork(
    track: TrackItem?,
    maxSize: Int
): androidx.compose.ui.graphics.ImageBitmap? {
    val cacheKey = remember(track?.id, track?.artworkPath, track?.filePath, maxSize) {
        if (track == null) {
            null
        } else {
            "${track.id}|${track.artworkPath.orEmpty()}|${track.filePath}|$maxSize"
        }
    }

    var imageBitmap by remember(cacheKey) {
        mutableStateOf(cacheKey?.let { artworkCacheGet(it) })
    }

    LaunchedEffect(cacheKey) {
        val key = cacheKey ?: run {
            imageBitmap = null
            return@LaunchedEffect
        }
        if (imageBitmap != null) {
            return@LaunchedEffect
        }
        if (artworkMissingCacheContains(key)) {
            imageBitmap = null
            return@LaunchedEffect
        }
        val decoded = loadArtworkBitmapCached(key) {
            decodeTrackArtworkBitmap(track, maxSize = maxSize)
        }
        imageBitmap = decoded
    }

    return imageBitmap
}

@Composable
private fun rememberArtworkByPath(
    path: String?,
    maxSize: Int
): androidx.compose.ui.graphics.ImageBitmap? {
    val cacheKey = remember(path, maxSize) {
        val normalized = path?.takeIf { it.isNotBlank() } ?: return@remember null
        "path|$normalized|$maxSize"
    }

    var imageBitmap by remember(cacheKey) {
        mutableStateOf(cacheKey?.let { artworkCacheGet(it) })
    }

    LaunchedEffect(cacheKey, path) {
        val key = cacheKey ?: run {
            imageBitmap = null
            return@LaunchedEffect
        }
        if (imageBitmap != null) {
            return@LaunchedEffect
        }
        if (artworkMissingCacheContains(key)) {
            imageBitmap = null
            return@LaunchedEffect
        }
        val normalizedPath = path?.takeIf { it.isNotBlank() } ?: run {
            artworkMissingCachePut(key)
            imageBitmap = null
            return@LaunchedEffect
        }
        val decoded = loadArtworkBitmapCached(key) {
            decodeArtworkBitmap(normalizedPath, maxSize = maxSize)
        }
        imageBitmap = decoded
    }

    return imageBitmap
}

@Composable
private fun EmptyListLabel(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FirstMusicOnboardingPage(
    onAddMusicTap: () -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val buttonBackground = if (isDark) {
        Color.White.copy(alpha = 0.10f)
    } else {
        Color.Black.copy(alpha = 0.06f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Add first music",
                style = TextStyle(
                    fontFamily = SonoraAndroidYSMusicFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Add audio files from your device to start listening.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(buttonBackground)
                    .clickable(onClick = onAddMusicTap)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_global_plus),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add first music",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun PlainControlButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean,
    size: Dp,
    iconSize: Dp,
    iconWidth: Dp = iconSize,
    iconHeight: Dp = iconSize,
    cornerRadius: Dp = 999.dp,
    tint: Color,
    disabledAlpha: Float = 0.45f
) {
    Box(
        modifier = Modifier
            .size(size)
            .alpha(if (enabled) 1f else disabledAlpha)
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(tint),
            modifier = Modifier.size(iconWidth, iconHeight),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun MiniPlayer(
    modifier: Modifier = Modifier,
    track: TrackItem,
    hasQueue: Boolean,
    isPlaying: Boolean,
    canStep: Boolean,
    onOpen: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val background = if (isDark) SonoraMiniPlayerDark else SonoraMiniPlayerLight
    val border = if (isDark) SonoraMiniPlayerBorderDark else SonoraMiniPlayerBorderLight
    val canStepQueue = hasQueue && canStep
    val artworkBitmap = rememberTrackArtwork(track, maxSize = 192)
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(width = 1.dp, color = border, shape = RoundedCornerShape(16.dp))
            .clickable(enabled = hasQueue, onClick = onOpen)
            .pointerInput(canStepQueue) {
                detectDragGestures(
                    onDragStart = {
                        dragX = 0f
                        dragY = 0f
                    },
                    onDragCancel = {
                        dragX = 0f
                        dragY = 0f
                    },
                    onDragEnd = {
                        val horizontalIntent = abs(dragX) > abs(dragY) * 1.1f
                        if (horizontalIntent && canStepQueue) {
                            if (dragX <= -20f) {
                                onNext()
                            } else if (dragX >= 20f) {
                                onPrevious()
                            }
                        }
                        dragX = 0f
                        dragY = 0f
                    },
                    onDrag = { change, amount ->
                        dragX += amount.x
                        dragY += amount.y
                        change.consume()
                    }
                )
            },
        color = background,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, end = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (artworkBitmap != null) {
                Image(
                    bitmap = artworkBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_global_music_note),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            val hasArtist = track.artist.isNotBlank()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = track.displayTitle(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (hasArtist) {
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 12.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlainControlButton(
                    iconRes = R.drawable.ic_global_previous,
                    contentDescription = "Previous",
                    onClick = onPrevious,
                    enabled = canStepQueue,
                    size = 28.dp,
                    iconSize = 15.dp,
                    tint = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(1.dp))

                PlainControlButton(
                    iconRes = if (isPlaying) R.drawable.ic_global_pause else R.drawable.ic_global_play,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    onClick = onTogglePlayPause,
                    enabled = hasQueue,
                    size = 34.dp,
                    iconSize = 18.dp,
                    tint = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(1.dp))

                PlainControlButton(
                    iconRes = R.drawable.ic_global_next,
                    contentDescription = "Next",
                    onClick = onNext,
                    enabled = canStepQueue,
                    size = 28.dp,
                    iconSize = 15.dp,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerView(
    modifier: Modifier,
    track: TrackItem,
    hasQueue: Boolean,
    isPlaying: Boolean,
    canStep: Boolean,
    isFavorite: Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    isSleepTimerActive: Boolean,
    sleepTimerRemainingMs: Long,
    nextTrack: TrackItem?,
    useWaveSlider: Boolean,
    artworkStyle: ArtworkStyle,
    accentColor: Color,
    preferredFontFamily: FontFamily?,
    onClose: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onSleepTimerTap: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSeek: (Long) -> Unit,
    positionMsProvider: () -> Long,
    durationMsProvider: () -> Long
) {
    androidx.activity.compose.BackHandler(onBack = onClose)

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val primaryColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryColor = if (isDark) Color.White.copy(alpha = 0.66f) else MaterialTheme.colorScheme.onSurfaceVariant
    val controlColor = if (hasQueue) primaryColor else secondaryColor.copy(alpha = 0.65f)
    val playerButtonCornerRadius = 0.dp
    val nextPreviewText = remember(nextTrack?.id, nextTrack?.title, nextTrack?.artist) {
        buildNextPreviewText(nextTrack)
    }
    val waveSeed = remember(track.id) { track.id.hashCode() }

    var isScrubbing by remember(track.id) { mutableStateOf(false) }
    var elapsedMs by remember(track.id) { mutableLongStateOf(0L) }
    var durationMs by remember(track.id) { mutableLongStateOf(maxOf(1L, track.durationMs)) }
    var sliderValue by remember(track.id) { mutableFloatStateOf(0f) }
    var wavePhase by remember(track.id) { mutableFloatStateOf(0f) }

    LaunchedEffect(track.id, isPlaying, hasQueue) {
        while (true) {
            val latestDuration = maxOf(1L, durationMsProvider(), track.durationMs)
            durationMs = latestDuration
            if (!isScrubbing) {
                val latestPosition = positionMsProvider().coerceIn(0L, latestDuration)
                elapsedMs = latestPosition
                sliderValue = latestPosition.toFloat()
            }
            delay(if (isPlaying) 250L else 500L)
        }
    }

    LaunchedEffect(track.id, isPlaying, hasQueue, useWaveSlider) {
        if (!useWaveSlider || !isPlaying || !hasQueue) {
            return@LaunchedEffect
        }
        val angularSpeed = ((Math.PI * 2.0) / 2.6).toFloat()
        var lastFrameNanos = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                if (lastFrameNanos != 0L) {
                    val deltaSeconds = (frameNanos - lastFrameNanos) / 1_000_000_000f
                    wavePhase += deltaSeconds * angularSpeed
                }
                lastFrameNanos = frameNanos
            }
        }
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val artworkHeight = minOf(maxWidth, maxHeight * 0.56f)
            val sliderMax = durationMs.toFloat().coerceAtLeast(1f)
            val sliderBaseColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
            val sliderActiveColor = sliderBaseColor.copy(alpha = if (hasQueue) 1f else 0.45f)
            val sliderInactiveColor = sliderBaseColor.copy(alpha = if (hasQueue) 0.26f else 0.12f)
            val controlsBottomPadding = 6.dp
            val sliderColors = SliderDefaults.colors(
                thumbColor = sliderActiveColor,
                activeTrackColor = sliderActiveColor,
                inactiveTrackColor = sliderInactiveColor
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                val artworkInset = if (artworkStyle == ArtworkStyle.Rounded) 12.dp else 0.dp
                val artworkShape = RoundedCornerShape(22.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(artworkHeight)
                ) {
                    val artworkModifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = artworkInset)
                        .then(
                            if (artworkStyle == ArtworkStyle.Rounded) {
                                Modifier.clip(artworkShape)
                            } else {
                                Modifier
                            }
                        )

                    TrackArtwork(
                        track = track,
                        placeholderRes = R.drawable.tab_note,
                        placeholderSize = 96.dp,
                        decodeMaxSize = 1024,
                        modifier = artworkModifier
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = sliderValue.coerceIn(0f, sliderMax),
                    onValueChange = { value ->
                        isScrubbing = true
                        sliderValue = value
                        elapsedMs = value.toLong()
                    },
                    onValueChangeFinished = {
                        isScrubbing = false
                        val target = sliderValue.toLong().coerceIn(0L, durationMs)
                        elapsedMs = target
                        onSeek(target)
                    },
                    enabled = hasQueue,
                    valueRange = 0f..sliderMax,
                    colors = sliderColors,
                    track = { sliderState ->
                        if (useWaveSlider) {
                            WaveSliderTrack(
                                sliderState = sliderState,
                                activeColor = sliderActiveColor,
                                inactiveColor = sliderInactiveColor,
                                wavePhase = wavePhase,
                                waveSeed = waveSeed
                            )
                        } else {
                            LinearSliderTrack(
                                sliderState = sliderState,
                                activeColor = sliderActiveColor,
                                inactiveColor = sliderInactiveColor
                            )
                        }
                    },
                    thumb = {
                        Canvas(modifier = Modifier.size(width = 22.dp, height = 26.dp)) {
                            val thumbWidth = 4.dp.toPx()
                            val thumbHeight = 18.dp.toPx()
                            val thumbLeft = (size.width - thumbWidth) / 2f
                            val thumbTop = (size.height - thumbHeight) / 2f
                            drawRoundRect(
                                color = sliderActiveColor,
                                topLeft = Offset(thumbLeft, thumbTop),
                                size = Size(thumbWidth, thumbHeight),
                                cornerRadius = CornerRadius(
                                    x = thumbWidth * 0.5f,
                                    y = thumbWidth * 0.5f
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .offset(y = (-4).dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDuration(elapsedMs),
                        style = TextStyle(
                            fontFamily = SonoraAndroidSFProSemiboldFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = (-0.1).sp
                        ),
                        color = secondaryColor
                    )
                    Text(
                        text = formatDuration(durationMs),
                        style = TextStyle(
                            fontFamily = SonoraAndroidSFProSemiboldFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = (-0.1).sp
                        ),
                        color = secondaryColor
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = track.artist.ifBlank { "" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = preferredFontFamily ?: MaterialTheme.typography.bodyMedium.fontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center,
                    color = secondaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = track.displayTitle(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = preferredFontFamily ?: MaterialTheme.typography.bodyMedium.fontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center,
                    color = primaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = nextPreviewText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = preferredFontFamily ?: MaterialTheme.typography.bodyMedium.fontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = secondaryColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                        .padding(start = 14.dp, end = 14.dp, bottom = controlsBottomPadding)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val repeatIcon = when (repeatMode) {
                            RepeatMode.None,
                            RepeatMode.Queue -> R.drawable.ic_player_repeat
                            RepeatMode.Track -> R.drawable.ic_player_repeat_one
                        }
                        val repeatTint = if (repeatMode == RepeatMode.None) {
                            primaryColor.copy(alpha = 0.92f)
                        } else {
                            accentColor
                        }
                        PlainControlButton(
                            iconRes = repeatIcon,
                            contentDescription = "Repeat",
                            onClick = onCycleRepeat,
                            enabled = hasQueue,
                            size = 40.dp,
                            iconSize = 24.dp,
                            cornerRadius = playerButtonCornerRadius,
                            tint = repeatTint
                        )
                        PlainControlButton(
                            iconRes = R.drawable.ic_player_shuffle,
                            contentDescription = "Shuffle",
                            onClick = onToggleShuffle,
                            enabled = hasQueue,
                            size = 40.dp,
                            iconSize = 24.dp,
                            cornerRadius = playerButtonCornerRadius,
                            tint = if (isShuffleEnabled) {
                                accentColor
                            } else {
                                primaryColor.copy(alpha = 0.92f)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlainControlButton(
                            iconRes = R.drawable.ic_global_previous,
                            contentDescription = "Previous",
                            onClick = onPrevious,
                            enabled = canStep,
                            size = 64.dp,
                            iconSize = 44.dp,
                            cornerRadius = playerButtonCornerRadius,
                            tint = controlColor
                        )
                        PlainControlButton(
                            iconRes = if (isPlaying) R.drawable.ic_global_pause else R.drawable.ic_global_play,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            onClick = onTogglePlayPause,
                            enabled = hasQueue,
                            size = 76.dp,
                            iconSize = 56.dp,
                            cornerRadius = playerButtonCornerRadius,
                            tint = controlColor
                        )
                        PlainControlButton(
                            iconRes = R.drawable.ic_global_next,
                            contentDescription = "Next",
                            onClick = onNext,
                            enabled = canStep,
                            size = 64.dp,
                            iconSize = 44.dp,
                            cornerRadius = playerButtonCornerRadius,
                            tint = controlColor
                        )
                    }

                    Column(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PlainControlButton(
                            iconRes = if (isSleepTimerActive) R.drawable.sleep_fill else R.drawable.sleep,
                            contentDescription = "Sleep timer",
                            onClick = onSleepTimerTap,
                            enabled = true,
                            size = 40.dp,
                            iconSize = 29.dp,
                            iconWidth = 24.5.dp,
                            iconHeight = 29.dp,
                            cornerRadius = playerButtonCornerRadius,
                            tint = if (isSleepTimerActive) {
                                accentColor
                            } else {
                                primaryColor.copy(alpha = 0.92f)
                            }
                        )
                        PlainControlButton(
                            iconRes = if (isFavorite) R.drawable.heart_fill else R.drawable.heart,
                            contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                            onClick = onToggleFavorite,
                            enabled = true,
                            size = 40.dp,
                            iconSize = 24.dp,
                            cornerRadius = playerButtonCornerRadius,
                            tint = if (isFavorite) {
                                Color(0xFFFF5966)
                            } else {
                                primaryColor.copy(alpha = 0.92f)
                            }
                        )
                    }
                }

                if (isSleepTimerActive) {
                    Text(
                        text = "Sleep: ${formatDuration(sleepTimerRemainingMs)}",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = controlsBottomPadding),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = secondaryColor
                    )
                } else {
                    Spacer(modifier = Modifier.height(controlsBottomPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WaveSliderTrack(
    sliderState: SliderState,
    activeColor: Color,
    inactiveColor: Color,
    wavePhase: Float,
    waveSeed: Int
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
    ) {
        val start = sliderState.valueRange.start
        val end = sliderState.valueRange.endInclusive
        val total = (end - start).coerceAtLeast(1f)
        val value = sliderState.value.coerceIn(start, end)
        val progress = ((value - start) / total).coerceIn(0f, 1f)
        val progressX = size.width * progress

        val centerY = size.height / 2f
        val inactiveLineHeight = 2.dp.toPx()

        if (progressX < size.width) {
            drawRoundRect(
                color = inactiveColor,
                topLeft = Offset(progressX, centerY - (inactiveLineHeight * 0.5f)),
                size = Size(size.width - progressX, inactiveLineHeight),
                cornerRadius = CornerRadius(
                    x = inactiveLineHeight * 0.5f,
                    y = inactiveLineHeight * 0.5f
                )
            )
        }

        if (progressX <= 0f) {
            return@Canvas
        }

        val path = androidx.compose.ui.graphics.Path()
        val waveStep = 2.dp.toPx().coerceAtLeast(1f)
        val wavelength = 18.dp.toPx()
        val amplitude = 2.1.dp.toPx()
        val seedShift = (waveSeed and 0xFF) * 0.025f
        val rampLength = 14.dp.toPx()
        val twoPi = (Math.PI * 2).toFloat()

        var x = 0f
        path.moveTo(0f, centerY)
        while (x <= progressX) {
            val phase = ((x / wavelength) * twoPi) + wavePhase + seedShift
            val envelope = (x / rampLength).coerceIn(0f, 1f)
            val y = centerY + (sin(phase.toDouble()).toFloat() * amplitude * envelope)
            path.lineTo(x, y)
            x += waveStep
        }

        drawPath(
            path = path,
            color = activeColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 1.8.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinearSliderTrack(
    sliderState: SliderState,
    activeColor: Color,
    inactiveColor: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
    ) {
        val start = sliderState.valueRange.start
        val end = sliderState.valueRange.endInclusive
        val total = (end - start).coerceAtLeast(1f)
        val value = sliderState.value.coerceIn(start, end)
        val progress = ((value - start) / total).coerceIn(0f, 1f)
        val progressX = size.width * progress
        val centerY = size.height / 2f
        val lineHeight = 2.dp.toPx()

        if (progressX > 0f) {
            drawRoundRect(
                color = activeColor,
                topLeft = Offset(0f, centerY - (lineHeight * 0.5f)),
                size = Size(progressX, lineHeight),
                cornerRadius = CornerRadius(lineHeight * 0.5f, lineHeight * 0.5f)
            )
        }

        if (progressX < size.width) {
            drawRoundRect(
                color = inactiveColor,
                topLeft = Offset(progressX, centerY - (lineHeight * 0.5f)),
                size = Size(size.width - progressX, lineHeight),
                cornerRadius = CornerRadius(lineHeight * 0.5f, lineHeight * 0.5f)
            )
        }
    }
}

private fun buildNextPreviewText(nextTrack: TrackItem?): String {
    if (nextTrack == null) {
        return "Next: -"
    }
    val nextTitle = nextTrack.title.ifBlank { "Unknown" }
    if (nextTrack.artist.isNotBlank()) {
        return "Next: ${nextTrack.artist} - $nextTitle"
    }
    return "Next: $nextTitle"
}

@Composable
private fun AddTracksDialog(
    tracks: List<TrackItem>,
    selectedTrackIDs: Set<String>,
    alreadyInPlaylistIDs: List<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit,
    onAdd: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Tracks") },
        text = {
            LazyColumn(modifier = Modifier.height(320.dp)) {
                items(tracks, key = { it.id }) { track ->
                    val alreadyAdded = alreadyInPlaylistIDs.contains(track.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !alreadyAdded) { onToggle(track.id) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedTrackIDs.contains(track.id) || alreadyAdded,
                            onCheckedChange = if (alreadyAdded) {
                                null
                            } else {
                                { onToggle(track.id) }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = track.displayTitle(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            if (alreadyAdded) {
                                Text(
                                    text = "Already in playlist",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            AccentTextButton(onClick = onAdd) {
                Text("Add")
            }
        },
        dismissButton = {
            AccentTextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun TrackItem.displayTitle(): String {
    return title.ifBlank { "Unknown Track" }
}

private fun TrackItem.matchesQuery(query: String): Boolean {
    val normalized = query.trim().lowercase()
    if (normalized.isBlank()) {
        return true
    }
    return displayTitle().lowercase().contains(normalized) ||
        artist.lowercase().contains(normalized)
}

private fun normalizedArtistKey(artist: String): String {
    return artist.trim().lowercase()
}

private data class ArtistParticipant(
    val key: String,
    val title: String
)

private fun artistParticipants(artist: String): List<ArtistParticipant> {
    val trimmed = artist.trim()
    if (trimmed.isBlank()) {
        return emptyList()
    }

    val seen = LinkedHashSet<String>()
    return trimmed
        .split(",")
        .map { it.trim() }
        .mapNotNull { value ->
            val key = normalizedArtistKey(value)
            if (key.isBlank() || !seen.add(key)) {
                null
            } else {
                ArtistParticipant(key = key, title = value)
            }
        }
}

private fun buildSearchArtistItems(
    tracks: List<TrackItem>,
    query: String,
    limit: Int
): List<SearchArtistUiItem> {
    if (tracks.isEmpty() || limit <= 0) {
        return emptyList()
    }
    val normalizedQuery = query.trim().lowercase()

    data class MutableArtist(
        val key: String,
        var title: String,
        val trackById: LinkedHashMap<String, TrackItem>
    )

    val byArtist = LinkedHashMap<String, MutableArtist>()
    tracks.forEach { track ->
        artistParticipants(track.artist).forEach { participant ->
            if (normalizedQuery.isNotBlank() && !participant.title.lowercase().contains(normalizedQuery)) {
                return@forEach
            }
            val artist = byArtist.getOrPut(participant.key) {
                MutableArtist(
                    key = participant.key,
                    title = participant.title,
                    trackById = LinkedHashMap()
                )
            }
            if (artist.title.isBlank()) {
                artist.title = participant.title
            }
            artist.trackById[track.id] = track
        }
    }

    return byArtist.values
        .sortedWith(
            compareByDescending<MutableArtist> { it.trackById.size }
                .thenBy { it.title.lowercase() }
        )
        .take(limit)
        .map { artist ->
            SearchArtistUiItem(
                key = artist.key,
                title = artist.title.ifBlank { "Artist" },
                trackIds = artist.trackById.keys.toList()
            )
        }
}

private fun homeAnalyticsWeight(
    track: TrackItem,
    analyticsByID: Map<String, TrackAnalytics>,
    favoriteTrackIDs: Set<String>
): Double {
    val analytics = analyticsByID[track.id] ?: TrackAnalytics(playCount = 0, skipCount = 0)
    val playCount = analytics.playCount.coerceAtLeast(0)
    val skipCount = analytics.skipCount.coerceAtLeast(0)
    val score = stabilizedAnalyticsScore(
        playCount = playCount,
        skipCount = skipCount,
        rawScore = analytics.score.coerceIn(0.0, 1.0)
    )
    val isFavorite = favoriteTrackIDs.contains(track.id)
    val playBoost = java.lang.Math.log(1.0 + playCount.toDouble()) * 0.42
    val skipPenalty = java.lang.Math.log(1.0 + skipCount.toDouble()) * 0.30
    val momentum = ((playCount - skipCount).toDouble() / 20.0).coerceIn(-0.25, 0.35)
    val favoriteBoost = if (isFavorite) 0.48 else 0.0
    return maxOf(0.05, 0.24 + (score * 3.0) + playBoost - skipPenalty + momentum + favoriteBoost)
}

private fun stabilizedAnalyticsScore(
    playCount: Int,
    skipCount: Int,
    rawScore: Double
): Double {
    val plays = playCount.coerceAtLeast(0).toDouble()
    val skips = skipCount.coerceAtLeast(0).toDouble()
    val interactions = plays + skips
    val confidence = 1.0 - kotlin.math.exp(-interactions / 4.5)
    val clampedRaw = rawScore.coerceIn(0.0, 1.0)
    val smoothed = (clampedRaw * confidence) + (0.52 * (1.0 - confidence))
    val skipRatioPenalty = (skips / (interactions + 1.0)) * 0.20
    return (smoothed - skipRatioPenalty).coerceIn(0.0, 1.0)
}

private fun buildHomeForYouTracks(
    tracks: List<TrackItem>,
    analyticsByID: Map<String, TrackAnalytics>,
    favoriteTrackIDs: Set<String>,
    limit: Int
): List<TrackItem> {
    if (tracks.isEmpty() || limit <= 0) {
        return emptyList()
    }

    return tracks.sortedWith(
        compareByDescending<TrackItem> {
            homeAnalyticsWeight(it, analyticsByID, favoriteTrackIDs)
        }
            .thenByDescending { analyticsByID[it.id]?.playCount ?: 0 }
            .thenBy { analyticsByID[it.id]?.skipCount ?: 0 }
            .thenBy { it.displayTitle().lowercase() }
    ).take(limit)
}

private fun buildHomeNeedThisTracks(
    tracks: List<TrackItem>,
    analyticsByID: Map<String, TrackAnalytics>,
    favoriteTrackIDs: Set<String>,
    limit: Int,
    rotationSeed: Int
): List<TrackItem> {
    if (tracks.isEmpty() || limit <= 0) {
        return emptyList()
    }

    val ranked = tracks.sortedWith(
        compareByDescending<TrackItem> {
            homeAnalyticsWeight(it, analyticsByID, favoriteTrackIDs)
        }.thenBy { it.displayTitle().lowercase() }
    )
    val poolLimit = minOf(ranked.size, maxOf(limit * 3, 12))
    return ranked
        .take(poolLimit)
        .shuffled(kotlin.random.Random(rotationSeed.toLong()))
        .take(limit)
}

private fun buildHomeFreshChoiceTracks(
    tracks: List<TrackItem>,
    analyticsByID: Map<String, TrackAnalytics>,
    favoriteTrackIDs: Set<String>,
    limit: Int,
    rotationSeed: Int
): List<TrackItem> {
    if (tracks.isEmpty() || limit <= 0) {
        return emptyList()
    }

    val nowSec = (System.currentTimeMillis() / 1000L).coerceAtLeast(1L)
    val ranked = tracks.sortedWith(
        compareByDescending<TrackItem> { track ->
            val analytics = analyticsByID[track.id] ?: TrackAnalytics(playCount = 0, skipCount = 0)
            val playCount = analytics.playCount.coerceAtLeast(0)
            val skipCount = analytics.skipCount.coerceAtLeast(0)
            val score = stabilizedAnalyticsScore(
                playCount = playCount,
                skipCount = skipCount,
                rawScore = analytics.score.coerceIn(0.0, 1.0)
            )
            val isFavorite = favoriteTrackIDs.contains(track.id)
            val ageDays = ((nowSec - track.addedAt).coerceAtLeast(0L)).toDouble() / 86_400.0
            val freshness = kotlin.math.exp(-ageDays / 24.0)
            val underPlayed = 1.0 - (minOf(playCount.toDouble(), 40.0) / 40.0)

            (freshness * 0.50) +
                (score * 0.32) +
                (underPlayed * 0.16) -
                (minOf(skipCount.toDouble(), 20.0) * 0.01) +
                (if (isFavorite) 0.08 else 0.0)
        }.thenBy { it.displayTitle().lowercase() }
    )

    val poolLimit = minOf(ranked.size, maxOf(limit * 3, 14))
    return ranked
        .take(poolLimit)
        .shuffled(kotlin.random.Random((rotationSeed * 31L) + 7L))
        .take(limit)
}

private fun buildHomeAlbumItems(tracks: List<TrackItem>, limit: Int): List<HomeAlbumUiItem> {
    if (tracks.isEmpty() || limit <= 0) {
        return emptyList()
    }

    data class MutableAlbum(
        val artistKey: String,
        var title: String,
        var latestAddedAt: Long,
        val trackById: LinkedHashMap<String, TrackItem>
    )

    val albumsByArtist = LinkedHashMap<String, MutableAlbum>()
    tracks.forEach { track ->
        val participants = artistParticipants(track.artist)
        if (participants.isEmpty()) {
            return@forEach
        }

        participants.forEach { participant ->
            val album = albumsByArtist.getOrPut(participant.key) {
                MutableAlbum(
                    artistKey = participant.key,
                    title = participant.title,
                    latestAddedAt = track.addedAt,
                    trackById = LinkedHashMap()
                )
            }
            if (track.addedAt > album.latestAddedAt) {
                album.latestAddedAt = track.addedAt
            }
            if (album.title.isBlank()) {
                album.title = participant.title
            }
            album.trackById[track.id] = track
        }
    }

    return albumsByArtist.values
        .sortedByDescending { it.latestAddedAt }
        .take(limit)
        .map { album ->
            val sortedTracks = album.trackById.values.sortedBy { it.displayTitle().lowercase() }
            HomeAlbumUiItem(
                artistKey = album.artistKey,
                title = album.title.ifBlank { "Album" },
                trackIds = sortedTracks.map { it.id },
                coverTrackId = sortedTracks.maxByOrNull { it.addedAt }?.id
            )
        }
}

private val homeLastAddedAccentCacheLock = Any()
private val homeLastAddedAccentCache = object : LinkedHashMap<String, Color>(180, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Color>?): Boolean {
        return size > 180
    }
}

private fun buildHomeLastAddedAccentKey(track: TrackItem): String {
    return buildString {
        append(track.id)
        append('|')
        append(track.artworkPath.orEmpty())
        append('|')
        append(track.filePath)
    }
}

private fun homeLastAddedAccentCacheGet(key: String): Color? {
    synchronized(homeLastAddedAccentCacheLock) {
        return homeLastAddedAccentCache[key]
    }
}

private fun homeLastAddedAccentCachePut(key: String, color: Color) {
    synchronized(homeLastAddedAccentCacheLock) {
        homeLastAddedAccentCache[key] = color
    }
}

@Composable
private fun rememberHomeLastAddedBackgroundColor(track: TrackItem): Color {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val fallback = if (isDark) {
        Color.White.copy(alpha = 0.10f)
    } else {
        Color.Black.copy(alpha = 0.05f)
    }
    val key = remember(track.id, track.artworkPath, track.filePath, isDark) {
        buildHomeLastAddedAccentKey(track) + "|${if (isDark) "dark" else "light"}"
    }
    var backgroundColor by remember(key) {
        mutableStateOf(homeLastAddedAccentCacheGet(key) ?: fallback)
    }

    LaunchedEffect(key, track) {
        val cached = homeLastAddedAccentCacheGet(key)
        if (cached != null) {
            backgroundColor = cached
            return@LaunchedEffect
        }

        val dominant = withContext(Dispatchers.IO) {
            decodeTrackArtworkBitmapRaw(track, maxSize = 96)?.let { extractDominantAccentColor(it) }
        }
        val resolved = dominant?.copy(alpha = if (isDark) 0.20f else 0.12f) ?: fallback
        homeLastAddedAccentCachePut(key, resolved)
        backgroundColor = resolved
    }

    return backgroundColor
}

private fun buildLovelyTrackIDs(
    tracks: List<TrackItem>,
    analyticsByID: Map<String, TrackAnalytics>
): List<String> {
    if (tracks.isEmpty()) {
        return emptyList()
    }

    val eligible = tracks.filter { track ->
        val analytics = analyticsByID[track.id] ?: TrackAnalytics(playCount = 0, skipCount = 0)
        val activity = analytics.playCount + analytics.skipCount
        val matchesFavoriteRule = track.isFavorite && analytics.score >= 0.60 && analytics.playCount >= 2
        val matchesHighScoreRule = analytics.score >= 0.80 && analytics.playCount >= 4
        activity >= 3 && analytics.skipCount <= 3 && (matchesFavoriteRule || matchesHighScoreRule)
    }

    return eligible.sortedWith(
        compareByDescending<TrackItem> { analyticsByID[it.id]?.score ?: 0.0 }
            .thenByDescending { analyticsByID[it.id]?.playCount ?: 0 }
            .thenBy { analyticsByID[it.id]?.skipCount ?: 0 }
            .thenByDescending { it.isFavorite }
            .thenBy { it.displayTitle().lowercase() }
    ).map { it.id }
}

private val playlistAccentCacheLock = Any()
private val playlistAccentCache = object : LinkedHashMap<String, Color>(64, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Color>?): Boolean {
        return size > 64
    }
}

private fun buildPlaylistAccentKey(
    playlist: PlaylistUiItem,
    coverTrack: TrackItem?,
    customCoverPath: String?
): String {
    return buildString {
        append(playlist.id)
        append('|')
        append(coverTrack?.id ?: "")
        append('|')
        append(coverTrack?.artworkPath.orEmpty())
        append('|')
        append(coverTrack?.filePath.orEmpty())
        append('|')
        append(customCoverPath.orEmpty())
    }
}

private fun playlistAccentCacheGet(key: String): Color? {
    synchronized(playlistAccentCacheLock) {
        return playlistAccentCache[key]
    }
}

private fun playlistAccentCachePut(key: String, color: Color) {
    synchronized(playlistAccentCacheLock) {
        playlistAccentCache[key] = color
    }
}

private val wavePaletteCacheLock = Any()
private val wavePaletteCache = object : LinkedHashMap<String, List<Color>>(48, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<Color>>?): Boolean {
        return size > 48
    }
}

private fun wavePaletteCacheKey(track: TrackItem?): String? {
    if (track == null) {
        return null
    }
    return "${track.id}|${track.artworkPath.orEmpty()}|${track.filePath}"
}

private fun wavePaletteCacheGet(key: String): List<Color>? {
    synchronized(wavePaletteCacheLock) {
        return wavePaletteCache[key]
    }
}

private fun wavePaletteCachePut(key: String, palette: List<Color>) {
    synchronized(wavePaletteCacheLock) {
        wavePaletteCache[key] = palette
    }
}

private fun defaultWavePalette(): List<Color> {
    val fallback = Color(0xFF4A3E35)
    return listOf(
        fallback,
        blendColors(fallback, Color(0xFF7E6B59), 0.32f),
        blendColors(fallback, Color.Black, 0.18f),
        blendColors(fallback, Color(0xFF7A5B64), 0.24f)
    )
}

private val artworkCacheLock = Any()
private val artworkCache = object : LinkedHashMap<String, androidx.compose.ui.graphics.ImageBitmap>(96, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, androidx.compose.ui.graphics.ImageBitmap>?): Boolean {
        return size > 96
    }
}
private val artworkMissingCache = object : LinkedHashSet<String>() {
    override fun add(element: String): Boolean {
        if (size >= 192) {
            val first = firstOrNull()
            if (first != null) {
                remove(first)
            }
        }
        return super.add(element)
    }
}

private fun artworkCacheGet(key: String): androidx.compose.ui.graphics.ImageBitmap? {
    synchronized(artworkCacheLock) {
        return artworkCache[key]
    }
}

private fun artworkCachePut(key: String, bitmap: androidx.compose.ui.graphics.ImageBitmap) {
    synchronized(artworkCacheLock) {
        artworkCache[key] = bitmap
        artworkMissingCache.remove(key)
    }
}

private fun artworkMissingCacheContains(key: String): Boolean {
    synchronized(artworkCacheLock) {
        return artworkMissingCache.contains(key)
    }
}

private fun artworkMissingCachePut(key: String) {
    synchronized(artworkCacheLock) {
        artworkMissingCache.add(key)
    }
}

private val artworkDecodeInFlightLock = Any()
private val artworkDecodeInFlight = mutableMapOf<String, CompletableDeferred<androidx.compose.ui.graphics.ImageBitmap?>>()

private suspend fun loadArtworkBitmapCached(
    key: String,
    loader: () -> androidx.compose.ui.graphics.ImageBitmap?
): androidx.compose.ui.graphics.ImageBitmap? {
    artworkCacheGet(key)?.let { return it }
    if (artworkMissingCacheContains(key)) {
        return null
    }

    val (deferred, isOwner) = synchronized(artworkDecodeInFlightLock) {
        val existing = artworkDecodeInFlight[key]
        if (existing != null) {
            existing to false
        } else {
            val created = CompletableDeferred<androidx.compose.ui.graphics.ImageBitmap?>()
            artworkDecodeInFlight[key] = created
            created to true
        }
    }

    if (!isOwner) {
        return deferred.await()
    }

    return try {
        val decoded = withContext(Dispatchers.IO) {
            loader()
        }
        if (decoded != null) {
            artworkCachePut(key, decoded)
        } else {
            artworkMissingCachePut(key)
        }
        deferred.complete(decoded)
        decoded
    } catch (error: Throwable) {
        deferred.complete(null)
        throw error
    } finally {
        synchronized(artworkDecodeInFlightLock) {
            if (artworkDecodeInFlight[key] === deferred) {
                artworkDecodeInFlight.remove(key)
            }
        }
    }
}

private fun decodeTrackArtworkBitmap(
    track: TrackItem?,
    maxSize: Int = 1024
): androidx.compose.ui.graphics.ImageBitmap? {
    if (track == null) {
        return null
    }

    val path = track.artworkPath
    if (!path.isNullOrBlank()) {
        decodeArtworkBitmap(path, maxSize)?.let { return it }
    }

    return decodeEmbeddedArtworkBitmap(track.filePath, maxSize)
}

private fun decodeTrackArtworkBitmapRaw(
    track: TrackItem?,
    maxSize: Int = 1024
): android.graphics.Bitmap? {
    if (track == null) {
        return null
    }

    val path = track.artworkPath
    if (!path.isNullOrBlank()) {
        decodeArtworkBitmapRaw(path, maxSize)?.let { return it }
    }

    return decodeEmbeddedArtworkBitmapRaw(track.filePath, maxSize)
}

private fun isRemoteArtworkPath(path: String): Boolean {
    return path.startsWith("http://", ignoreCase = true) ||
        path.startsWith("https://", ignoreCase = true)
}

private fun preferredArtworkBitmapConfig(maxSize: Int): android.graphics.Bitmap.Config {
    return if (maxSize <= 512) {
        android.graphics.Bitmap.Config.RGB_565
    } else {
        android.graphics.Bitmap.Config.ARGB_8888
    }
}

private fun decodeRemoteArtworkBitmapRaw(path: String, maxSize: Int = 1024): android.graphics.Bitmap? {
    return runCatching {
        val connection = (URL(path).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            instanceFollowRedirects = true
            connectTimeout = 15_000
            readTimeout = 20_000
            setRequestProperty("User-Agent", "SonoraAndroid/1.0")
        }
        connection.connect()
        if (connection.responseCode !in 200..299) {
            connection.disconnect()
            return@runCatching null
        }
        val bytes = connection.inputStream.use { it.readBytes() }
        connection.disconnect()
        if (bytes.isEmpty()) {
            return@runCatching null
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return@runCatching null
        }

        var sampleSize = 1
        while ((bounds.outWidth / sampleSize) > maxSize || (bounds.outHeight / sampleSize) > maxSize) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = preferredArtworkBitmapConfig(maxSize)
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }.getOrNull()
}

private fun decodeArtworkBitmap(path: String, maxSize: Int = 1024): androidx.compose.ui.graphics.ImageBitmap? {
    return runCatching {
        if (path.isBlank()) {
            return@runCatching null
        }
        if (isRemoteArtworkPath(path)) {
            return@runCatching decodeRemoteArtworkBitmapRaw(path, maxSize)?.asImageBitmap()
        }
        val file = File(path)
        if (!file.exists()) {
            return@runCatching null
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return@runCatching null
        }

        var sampleSize = 1
        while ((bounds.outWidth / sampleSize) > maxSize || (bounds.outHeight / sampleSize) > maxSize) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = preferredArtworkBitmapConfig(maxSize)
        }
        BitmapFactory.decodeFile(file.absolutePath, options)?.asImageBitmap()
    }.getOrNull()
}

private fun decodeArtworkBitmapRaw(path: String, maxSize: Int = 1024): android.graphics.Bitmap? {
    return runCatching {
        if (path.isBlank()) {
            return@runCatching null
        }
        if (isRemoteArtworkPath(path)) {
            return@runCatching decodeRemoteArtworkBitmapRaw(path, maxSize)
        }
        val file = File(path)
        if (!file.exists()) {
            return@runCatching null
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return@runCatching null
        }

        var sampleSize = 1
        while ((bounds.outWidth / sampleSize) > maxSize || (bounds.outHeight / sampleSize) > maxSize) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = preferredArtworkBitmapConfig(maxSize)
        }
        BitmapFactory.decodeFile(file.absolutePath, options)
    }.getOrNull()
}

private fun decodeEmbeddedArtworkBitmap(
    filePath: String,
    maxSize: Int = 1024
): androidx.compose.ui.graphics.ImageBitmap? {
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
        val bytes = retriever.embeddedPicture ?: return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null
        }

        var sampleSize = 1
        while ((bounds.outWidth / sampleSize) > maxSize || (bounds.outHeight / sampleSize) > maxSize) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = preferredArtworkBitmapConfig(maxSize)
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)?.asImageBitmap()
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

private fun decodeEmbeddedArtworkBitmapRaw(
    filePath: String,
    maxSize: Int = 1024
): android.graphics.Bitmap? {
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
        val bytes = retriever.embeddedPicture ?: return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null
        }

        var sampleSize = 1
        while ((bounds.outWidth / sampleSize) > maxSize || (bounds.outHeight / sampleSize) > maxSize) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = preferredArtworkBitmapConfig(maxSize)
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
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

private fun extractDominantAccentColor(bitmap: android.graphics.Bitmap): Color? {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= 0 || height <= 0) {
        return null
    }

    val stepX = maxOf(1, width / 24)
    val stepY = maxOf(1, height / 24)
    var sumR = 0L
    var sumG = 0L
    var sumB = 0L
    var count = 0
    val hsv = FloatArray(3)

    var y = 0
    while (y < height) {
        var x = 0
        while (x < width) {
            val pixel = bitmap.getPixel(x, y)
            val alpha = android.graphics.Color.alpha(pixel)
            if (alpha >= 200) {
                android.graphics.Color.colorToHSV(pixel, hsv)
                val saturation = hsv[1]
                val value = hsv[2]
                if (saturation >= 0.18f && value >= 0.20f) {
                    sumR += android.graphics.Color.red(pixel)
                    sumG += android.graphics.Color.green(pixel)
                    sumB += android.graphics.Color.blue(pixel)
                    count += 1
                }
            }
            x += stepX
        }
        y += stepY
    }

    if (count == 0) {
        return null
    }

    val red = (sumR / count).toInt().coerceIn(0, 255)
    val green = (sumG / count).toInt().coerceIn(0, 255)
    val blue = (sumB / count).toInt().coerceIn(0, 255)
    val accentHsv = FloatArray(3)
    android.graphics.Color.colorToHSV(android.graphics.Color.rgb(red, green, blue), accentHsv)
    accentHsv[1] = (accentHsv[1] * 1.14f).coerceIn(0f, 1f)
    accentHsv[2] = (accentHsv[2] * 1.04f).coerceIn(0f, 1f)
    return Color(android.graphics.Color.HSVToColor(accentHsv))
}

private fun resolveLegacyAccentColorFromHue(hue: Float): Color {
    val normalized = nearestAccentHue(hue)
    return Color.hsv(
        hue = normalized,
        saturation = 0.76f,
        value = 1.0f
    )
}

private fun resolveAccentColor(hex: String): Color {
    return parseHexColor(hex)
        ?: parseHexColor(DEFAULT_ACCENT_HEX)
        ?: Color(0xFFE6BE00)
}

private fun parseHexColor(raw: String?): Color? {
    if (raw.isNullOrBlank()) {
        return null
    }
    val trimmed = raw.trim().removePrefix("#")
    if (trimmed.length != 6 || trimmed.any { it !in '0'..'9' && it !in 'a'..'f' && it !in 'A'..'F' }) {
        return null
    }
    val rgb = trimmed.toIntOrNull(16) ?: return null
    val red = ((rgb shr 16) and 0xFF) / 255f
    val green = ((rgb shr 8) and 0xFF) / 255f
    val blue = (rgb and 0xFF) / 255f
    return Color(red, green, blue, 1f)
}

private fun normalizeHexColor(raw: String?): String? {
    return parseHexColor(raw)?.let { formatColorHex(it) }
}

private fun formatColorHex(color: Color): String {
    val red = (color.red * 255f).roundToInt().coerceIn(0, 255)
    val green = (color.green * 255f).roundToInt().coerceIn(0, 255)
    val blue = (color.blue * 255f).roundToInt().coerceIn(0, 255)
    return String.format("#%02X%02X%02X", red, green, blue)
}

private fun resolveSettingsFontFamily(style: AppFontStyle): FontFamily? {
    return when (style) {
        AppFontStyle.System -> null
        AppFontStyle.Serif -> SonoraAndroidNotoSerifFamily
    }
}

private fun encodeImagePathToBase64(path: String?): Pair<String, String>? {
    if (path.isNullOrBlank()) {
        return null
    }
    return runCatching {
        val bytes = if (path.startsWith("http://", ignoreCase = true) || path.startsWith("https://", ignoreCase = true)) {
            val connection = (URL(path).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = true
                connectTimeout = 15_000
                readTimeout = 20_000
                setRequestProperty("User-Agent", "SonoraAndroid/1.0")
            }
            connection.connect()
            if (connection.responseCode !in 200..299) {
                connection.disconnect()
                return@runCatching null
            }
            val data = connection.inputStream.use { it.readBytes() }
            connection.disconnect()
            data
        } else {
            val file = File(path)
            if (!file.exists() || !file.isFile) {
                return@runCatching null
            }
            file.readBytes()
        }
        if (bytes.isEmpty()) {
            null
        } else {
            val lowered = path.lowercase()
            val mimeType = when {
                lowered.endsWith(".png") -> "image/png"
                lowered.endsWith(".webp") -> "image/webp"
                else -> "image/jpeg"
            }
            mimeType to Base64.encodeToString(bytes, Base64.NO_WRAP)
        }
    }.getOrNull()
}

private fun encodeAudioFileToBase64(path: String?): Pair<String, String>? {
    if (path.isNullOrBlank()) {
        return null
    }
    return runCatching {
        val file = File(path)
        if (!file.exists() || !file.isFile) {
            return@runCatching null
        }
        val extension = file.extension.lowercase().ifBlank { "mp3" }
        val mimeType = when (extension) {
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "flac" -> "audio/flac"
            "opus" -> "audio/ogg"
            "webm" -> "audio/webm"
            else -> "audio/mpeg"
        }
        mimeType to Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
    }.getOrNull()
}

private fun guessAudioMimeType(path: String): String {
    return when (File(path).extension.lowercase()) {
        "m4a" -> "audio/mp4"
        "aac" -> "audio/aac"
        "wav" -> "audio/wav"
        "ogg" -> "audio/ogg"
        "flac" -> "audio/flac"
        "opus" -> "audio/ogg"
        "webm" -> "audio/webm"
        else -> "audio/mpeg"
    }
}

private fun uploadSharedPlaylistBinaryPart(
    urlString: String,
    mimeType: String,
    file: File
): Boolean {
    if (!file.exists() || !file.isFile) {
        return true
    }
    return runCatching {
        val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            instanceFollowRedirects = true
            connectTimeout = 30_000
            readTimeout = 600_000
            setRequestProperty("Content-Type", mimeType)
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "SonoraAndroid/1.0")
        }
        connection.outputStream.use { output ->
            file.inputStream().use { input -> input.copyTo(output) }
        }
        val status = connection.responseCode
        connection.inputStream?.close()
        connection.errorStream?.close()
        connection.disconnect()
        status in 200..299
    }.getOrDefault(false)
}

private fun uploadSharedPlaylist(
    baseUrl: String,
    name: String,
    coverPath: String?,
    tracks: List<TrackItem>,
    onProgress: (String) -> Unit = {}
): SharedPlaylistEntry? {
    if (tracks.isEmpty()) {
        return null
    }

    val payload = JSONObject()
    payload.put("name", name.ifBlank { "Playlist" })
    val tracksArray = JSONArray()
    tracks.forEachIndexed { index, track ->
        val item = JSONObject()
        item.put("id", track.id)
        item.put("title", track.title)
        item.put("artist", track.artist)
        item.put("durationMs", track.durationMs)
        val artworkFile = track.artworkPath?.let { File(it) }?.takeIf { it.exists() && it.isFile }
        if (artworkFile != null) {
            item.put("artworkField", "track_artwork_$index")
        }
        val audioFile = File(track.filePath)
        if (audioFile.exists() && audioFile.isFile) {
            item.put("fileField", "track_file_$index")
            item.put("fileExtension", audioFile.extension.lowercase().ifBlank { "mp3" })
        }
        tracksArray.put(item)
    }
    payload.put("tracks", tracksArray)

    val requestUrl = "${baseUrl.trimEnd('/')}/api/shared-playlists"
    return runCatching {
        onProgress("Creating playlist...")
        val connection = (URL(requestUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            instanceFollowRedirects = true
            connectTimeout = 30_000
            readTimeout = 120_000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "SonoraAndroid/1.0")
        }
        connection.outputStream.use { output ->
            output.write(payload.toString().toByteArray(StandardCharsets.UTF_8))
        }
        val status = connection.responseCode
        val raw = if (status in 200..299) {
            connection.inputStream.use { it.readBytes().toString(StandardCharsets.UTF_8) }
        } else {
            connection.errorStream?.use { it.readBytes().toString(StandardCharsets.UTF_8) }.orEmpty()
        }
        connection.disconnect()
        if (status !in 200..299) {
            return@runCatching null
        }
        val created = parseSharedPlaylistPayload(JSONObject(raw)) ?: return@runCatching null
        val normalizedBase = baseUrl.trimEnd('/')
        coverPath?.let { path ->
            val coverFile = File(path)
            if (coverFile.exists() && coverFile.isFile) {
                onProgress("Uploading cover...")
                val mimeType = when (coverFile.extension.lowercase()) {
                    "png" -> "image/png"
                    "webp" -> "image/webp"
                    else -> "image/jpeg"
                }
                val encodedName = java.net.URLEncoder.encode(coverFile.name, StandardCharsets.UTF_8.toString())
                if (!uploadSharedPlaylistBinaryPart("$normalizedBase/api/shared-playlists/${created.remoteId}/cover?filename=$encodedName", mimeType, coverFile)) {
                    return@runCatching null
                }
            }
        }
        tracks.forEachIndexed { index, track ->
            onProgress("Uploading track ${index + 1}/${tracks.size}...")
            track.artworkPath?.let { artworkPath ->
                val artworkFile = File(artworkPath)
                if (artworkFile.exists() && artworkFile.isFile) {
                    val mimeType = when (artworkFile.extension.lowercase()) {
                        "png" -> "image/png"
                        "webp" -> "image/webp"
                        else -> "image/jpeg"
                    }
                    val encodedName = java.net.URLEncoder.encode(artworkFile.name, StandardCharsets.UTF_8.toString())
                    if (!uploadSharedPlaylistBinaryPart("$normalizedBase/api/shared-playlists/${created.remoteId}/tracks/$index/artwork?filename=$encodedName", mimeType, artworkFile)) {
                        return@runCatching null
                    }
                }
            }
            val audioFile = File(track.filePath)
            if (audioFile.exists() && audioFile.isFile) {
                val encodedName = java.net.URLEncoder.encode(audioFile.name, StandardCharsets.UTF_8.toString())
                if (!uploadSharedPlaylistBinaryPart("$normalizedBase/api/shared-playlists/${created.remoteId}/tracks/$index/file?filename=$encodedName", guessAudioMimeType(audioFile.absolutePath), audioFile)) {
                    return@runCatching null
                }
            }
        }
        created
    }.getOrNull()
}

private fun parseSharedPlaylistPayload(payload: JSONObject): SharedPlaylistEntry? {
    val remoteId = payload.optString("id").trim()
    val name = payload.optString("name").trim()
    if (remoteId.isBlank() || name.isBlank()) {
        return null
    }
    val tracksArray = payload.optJSONArray("tracks") ?: JSONArray()
    val tracks = buildList {
        for (index in 0 until tracksArray.length()) {
            val item = tracksArray.optJSONObject(index) ?: continue
            val title = item.optString("title").trim()
            val artist = item.optString("artist").trim()
            if (title.isBlank() && artist.isBlank()) {
                continue
            }
            add(
                SharedPlaylistTrackEntry(
                    id = item.optString("id").trim().ifBlank { "track_${index + 1}" },
                    title = title.ifBlank { "Track ${index + 1}" },
                    artist = artist,
                    durationMs = item.optLong("durationMs").coerceAtLeast(0L),
                    artworkUrl = item.optString("artworkUrl").trim().ifBlank { null },
                    fileUrl = item.optString("fileUrl").trim().ifBlank { null },
                    cachedArtworkPath = null,
                    cachedFilePath = null
                )
            )
        }
    }
    return SharedPlaylistEntry(
        remoteId = remoteId,
        localId = SharedPlaylistStore.syntheticLocalId(remoteId),
        name = name,
        shareUrl = payload.optString("shareUrl").trim().ifBlank {
            payload.optString("url").trim().ifBlank { payload.optString("webUrl").trim() }
        },
        sourceBaseUrl = payload.optString("sourceBaseUrl").trim(),
        contentSha256 = payload.optString("contentSha256").trim(),
        coverUrl = payload.optString("coverUrl").trim().ifBlank { null },
        cachedCoverPath = null,
        tracks = tracks,
        createdAt = System.currentTimeMillis()
    )
}

private fun fetchSharedPlaylistFromDeepLink(
    uriString: String,
    fallbackBaseUrl: String,
    cachedEntry: SharedPlaylistEntry? = null
): SharedPlaylistEntry? {
    val uri = runCatching { Uri.parse(uriString) }.getOrNull() ?: return null
    val isCustomScheme = uri.scheme.equals("sonora", ignoreCase = true) &&
        uri.host.equals("playlist", ignoreCase = true) &&
        uri.path == "/shared"
    val isWebLink = uri.scheme.equals("https", ignoreCase = true) &&
        !uri.host.isNullOrBlank() &&
        (uri.pathSegments.firstOrNull() == "playlists") &&
        uri.pathSegments.size >= 2
    if (!isCustomScheme && !isWebLink) {
        return null
    }
    val playlistId = if (isCustomScheme) {
        uri.getQueryParameter("id")?.trim().orEmpty()
    } else {
        uri.pathSegments.getOrNull(1)?.trim().orEmpty()
    }
    if (playlistId.isBlank()) {
        return null
    }
    val baseUrl = if (isCustomScheme) {
        uri.getQueryParameter("source")?.trim()?.ifBlank { null }
            ?: fallbackBaseUrl.trim().ifBlank { "https://api.corebrew.ru" }
    } else {
        "${uri.scheme}://${uri.host}${if (uri.port > 0) ":${uri.port}" else ""}"
    }
    val requestUrl = "${baseUrl.trimEnd('/')}/api/shared-playlists/$playlistId"
    return runCatching {
        val connection = (URL(requestUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            instanceFollowRedirects = true
            connectTimeout = 30_000
            readTimeout = 120_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "SonoraAndroid/1.0")
        }
        connection.connect()
        val status = connection.responseCode
        val raw = if (status in 200..299) {
            connection.inputStream.use { it.readBytes().toString(StandardCharsets.UTF_8) }
        } else {
            connection.errorStream?.use { it.readBytes().toString(StandardCharsets.UTF_8) }.orEmpty()
        }
        connection.disconnect()
        if (status !in 200..299 || raw.isBlank()) {
            return@runCatching cachedEntry
        }
        val fetched = parseSharedPlaylistPayload(JSONObject(raw)) ?: return@runCatching cachedEntry
        if (
            cachedEntry != null &&
            fetched.remoteId == cachedEntry.remoteId &&
            fetched.contentSha256.isNotBlank() &&
            fetched.contentSha256 == cachedEntry.contentSha256
        ) {
            return@runCatching cachedEntry
        }
        fetched
    }.getOrNull() ?: cachedEntry
}

private fun extractSharedPlaylistRemoteId(uriString: String): String? {
    val uri = runCatching { Uri.parse(uriString) }.getOrNull() ?: return null
    val isCustomScheme = uri.scheme.equals("sonora", ignoreCase = true) &&
        uri.host.equals("playlist", ignoreCase = true) &&
        uri.path == "/shared"
    val isWebLink = uri.scheme.equals("https", ignoreCase = true) &&
        !uri.host.isNullOrBlank() &&
        (uri.pathSegments.firstOrNull() == "playlists") &&
        uri.pathSegments.size >= 2
    return when {
        isCustomScheme -> uri.getQueryParameter("id")?.trim()?.ifBlank { null }
        isWebLink -> uri.pathSegments.getOrNull(1)?.trim()?.ifBlank { null }
        else -> null
    }
}

private fun openExternalUrl(context: Context, url: String) {
    if (url.isBlank()) {
        return
    }
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching {
        context.startActivity(intent)
    }
}

private fun parseAndroidAppUpdateRelease(payload: JSONObject?): AndroidAppUpdateRelease? {
    payload ?: return null
    val id = payload.optString("id").trim()
    val title = payload.optString("title").trim()
    val versionName = payload.optString("versionName").trim()
    val versionCode = payload.optLong("versionCode").coerceAtLeast(0L)
    val downloadUrl = payload.optString("downloadUrl").trim()
    if (id.isBlank() || title.isBlank() || versionName.isBlank() || versionCode <= 0L || downloadUrl.isBlank()) {
        return null
    }
    val notes = buildList {
        val array = payload.optJSONArray("notes") ?: JSONArray()
        for (index in 0 until array.length()) {
            val value = array.optString(index).trim()
            if (value.isNotBlank()) {
                add(value)
            }
        }
    }
    return AndroidAppUpdateRelease(
        id = id,
        title = title,
        versionName = versionName,
        versionCode = versionCode,
        coverUrl = payload.optString("coverUrl").trim().ifBlank { null },
        downloadUrl = downloadUrl,
        notes = notes
    )
}

private fun fetchLatestAndroidAppUpdate(
    baseUrl: String,
    currentVersionCode: Long
): AndroidAppUpdateState? {
    val normalizedBase = baseUrl.trim().ifBlank { "https://api.corebrew.ru" }.trimEnd('/')
    val requestUrl = "$normalizedBase/api/app-updates/android/latest?channel=stable&currentVersionCode=$currentVersionCode"
    return runCatching {
        val connection = (URL(requestUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            instanceFollowRedirects = true
            connectTimeout = 20_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "SonoraAndroid/1.0")
        }
        connection.connect()
        val status = connection.responseCode
        val raw = if (status in 200..299) {
            connection.inputStream.use { it.readBytes().toString(StandardCharsets.UTF_8) }
        } else {
            connection.errorStream?.use { it.readBytes().toString(StandardCharsets.UTF_8) }.orEmpty()
        }
        connection.disconnect()
        if (status !in 200..299 || raw.isBlank()) {
            return@runCatching AndroidAppUpdateState(
                checking = false,
                statusMessage = "Could not check for updates"
            )
        }
        val payload = JSONObject(raw)
        val latestRelease = parseAndroidAppUpdateRelease(payload.optJSONObject("latest"))
        val updateAvailable = payload.optBoolean("updateAvailable", false) && latestRelease != null
        val statusMessage = when {
            latestRelease == null -> "No published updates yet"
            updateAvailable -> "Version ${latestRelease.versionName} is available"
            else -> "You're using latest version"
        }
        AndroidAppUpdateState(
            checking = false,
            latestRelease = latestRelease,
            updateAvailable = updateAvailable,
            statusMessage = statusMessage
        )
    }.getOrNull()
}

private const val APP_UPDATE_DOWNLOAD_PREFS = "android_app_update_download"
private const val APP_UPDATE_DOWNLOAD_ID_KEY = "download_id"
private const val APP_UPDATE_RELEASE_ID_KEY = "release_id"
private const val APP_UPDATE_RELEASE_TITLE_KEY = "release_title"
private const val APP_UPDATE_RELEASE_VERSION_NAME_KEY = "release_version_name"
private const val APP_UPDATE_RELEASE_VERSION_CODE_KEY = "release_version_code"
private const val APP_UPDATE_RELEASE_COVER_URL_KEY = "release_cover_url"
private const val APP_UPDATE_RELEASE_DOWNLOAD_URL_KEY = "release_download_url"
private const val APP_UPDATE_RELEASE_NOTES_KEY = "release_notes_json"
private const val APP_UPDATE_APK_PATH_KEY = "apk_path"

private fun appUpdateDownloadPrefs(context: Context) =
    context.getSharedPreferences(APP_UPDATE_DOWNLOAD_PREFS, Context.MODE_PRIVATE)

fun clearAndroidAppUpdateDownloadState(context: Context) {
    appUpdateDownloadPrefs(context).edit().clear().apply()
}

fun cancelAndroidAppUpdateDownload(context: Context, downloadId: Long?): Boolean {
    val activeDownloadId = downloadId ?: return false
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return false
    return manager.remove(activeDownloadId) > 0L
}

fun enqueueAndroidAppUpdateDownload(
    context: Context,
    release: AndroidAppUpdateRelease
): AndroidAppUpdateDownloadSnapshot? {
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return null
    val rootDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return null
    val releaseDir = File(rootDir, "updates/${release.id}").apply { mkdirs() }
    val apkFile = File(releaseDir, "sonora-${release.versionCode}.apk")
    if (apkFile.exists()) {
        apkFile.delete()
    }
    val request = DownloadManager.Request(Uri.parse(release.downloadUrl)).apply {
        setTitle("Sonora ${release.versionName}")
        setDescription("Downloading update in background")
        setMimeType("application/vnd.android.package-archive")
        addRequestHeader("User-Agent", "SonoraAndroid/1.0")
        setAllowedOverMetered(true)
        setAllowedOverRoaming(true)
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setDestinationUri(Uri.fromFile(apkFile))
    }
    val downloadId = runCatching { manager.enqueue(request) }.getOrNull() ?: return null
    appUpdateDownloadPrefs(context).edit()
        .putLong(APP_UPDATE_DOWNLOAD_ID_KEY, downloadId)
        .putString(APP_UPDATE_RELEASE_ID_KEY, release.id)
        .putString(APP_UPDATE_RELEASE_TITLE_KEY, release.title)
        .putString(APP_UPDATE_RELEASE_VERSION_NAME_KEY, release.versionName)
        .putLong(APP_UPDATE_RELEASE_VERSION_CODE_KEY, release.versionCode)
        .putString(APP_UPDATE_RELEASE_COVER_URL_KEY, release.coverUrl)
        .putString(APP_UPDATE_RELEASE_DOWNLOAD_URL_KEY, release.downloadUrl)
        .putString(APP_UPDATE_RELEASE_NOTES_KEY, JSONArray(release.notes).toString())
        .putString(APP_UPDATE_APK_PATH_KEY, apkFile.absolutePath)
        .apply()
    return readAndroidAppUpdateDownloadSnapshot(context, downloadId)
}

fun readAndroidAppUpdateDownloadSnapshot(
    context: Context,
    requestedDownloadId: Long? = null
): AndroidAppUpdateDownloadSnapshot? {
    val prefs = appUpdateDownloadPrefs(context)
    val downloadId = requestedDownloadId
        ?: prefs.getLong(APP_UPDATE_DOWNLOAD_ID_KEY, -1L).takeIf { it > 0L }
        ?: return null
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return null
    val release = AndroidAppUpdateRelease(
        id = prefs.getString(APP_UPDATE_RELEASE_ID_KEY, null).orEmpty(),
        title = prefs.getString(APP_UPDATE_RELEASE_TITLE_KEY, null).orEmpty().ifBlank { "Sonora Android" },
        versionName = prefs.getString(APP_UPDATE_RELEASE_VERSION_NAME_KEY, null).orEmpty().ifBlank { "Update" },
        versionCode = prefs.getLong(APP_UPDATE_RELEASE_VERSION_CODE_KEY, 0L),
        coverUrl = prefs.getString(APP_UPDATE_RELEASE_COVER_URL_KEY, null),
        downloadUrl = prefs.getString(APP_UPDATE_RELEASE_DOWNLOAD_URL_KEY, null).orEmpty(),
        notes = runCatching {
            val raw = prefs.getString(APP_UPDATE_RELEASE_NOTES_KEY, null).orEmpty()
            if (raw.isBlank()) {
                emptyList()
            } else {
                val array = JSONArray(raw)
                buildList {
                    for (index in 0 until array.length()) {
                        add(array.optString(index))
                    }
                }
            }
        }.getOrDefault(emptyList())
    )
    val apkPath = prefs.getString(APP_UPDATE_APK_PATH_KEY, null).orEmpty()
    val cursor = manager.query(DownloadManager.Query().setFilterById(downloadId)) ?: return null
    cursor.use {
        if (!it.moveToFirst()) {
            clearAndroidAppUpdateDownloadState(context)
            return null
        }
        return AndroidAppUpdateDownloadSnapshot(
            release = release,
            downloadId = downloadId,
            apkPath = apkPath,
            status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)),
            downloadedBytes = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)),
            totalBytes = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)).coerceAtLeast(0L)
        )
    }
}

private fun installDownloadedApk(context: Context, apkFile: File): Boolean {
    return runCatching {
        if (!apkFile.exists()) {
            return@runCatching false
        }

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val intents = listOf(
            Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                clipData = ClipData.newUri(context.contentResolver, apkFile.name, contentUri)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                clipData = ClipData.newUri(context.contentResolver, apkFile.name, contentUri)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        )
        intents.forEach { intent ->
            context.packageManager.queryIntentActivities(intent, 0).forEach { resolveInfo ->
                context.grantUriPermission(
                    resolveInfo.activityInfo.packageName,
                    contentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val opened = runCatching {
                context.startActivity(intent)
                true
            }.getOrDefault(false)
            if (opened) {
                return@runCatching true
            }
        }
        false
    }.getOrDefault(false)
}

private fun resolveAppVersionCode(context: Context): Long {
    val packageInfo = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }.getOrNull() ?: return 0L

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode
    } else {
        @Suppress("DEPRECATION")
        packageInfo.versionCode.toLong()
    }
}

private fun resolveAppVersionLabel(context: Context): String {
    val packageInfo = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }.getOrNull() ?: return "1.0"

    val versionName = packageInfo.versionName?.takeIf { it.isNotBlank() }
    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode.toString()
    } else {
        @Suppress("DEPRECATION")
        packageInfo.versionCode.toString()
    }

    return if (!versionName.isNullOrBlank() && versionName != versionCode) {
        "$versionName ($versionCode)"
    } else {
        versionName ?: versionCode
    }
}

private fun computeAppStorageUsageBytes(context: Context): Long {
    return directorySizeBytes(context.filesDir)
}

private fun directorySizeBytes(file: File): Long {
    if (!file.exists()) {
        return 0L
    }
    if (file.isFile) {
        return file.length().coerceAtLeast(0L)
    }

    val children = file.listFiles() ?: return 0L
    var total = 0L
    for (child in children) {
        total += directorySizeBytes(child)
    }
    return total
}

private fun queryUriSizeBytes(context: Context, uri: Uri): Long? {
    return runCatching {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.SIZE),
            null,
            null,
            null
        )?.use { cursor ->
            val column = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (column == -1 || !cursor.moveToFirst() || cursor.isNull(column)) {
                null
            } else {
                cursor.getLong(column).takeIf { it >= 0L }
            }
        }
    }.getOrNull()
}

private fun formatStoragePathForAbout(path: String): String {
    if (path.isBlank()) {
        return "-"
    }
    val withoutFilesSuffix = path.removeSuffix("/files")
    val normalized = if (withoutFilesSuffix.isBlank()) path else withoutFilesSuffix
    if (normalized.length <= 38) {
        return normalized
    }
    val parts = normalized.split('/').filter { it.isNotBlank() }
    return if (parts.size >= 2) {
        ".../${parts.takeLast(2).joinToString("/")}"
    } else {
        normalized.takeLast(38)
    }
}

private fun formatStorageSize(bytes: Long): String {
    if (bytes <= 0L) {
        return "0 B"
    }
    val kb = 1024.0
    val mb = kb * 1024.0
    val gb = mb * 1024.0
    val tb = gb * 1024.0
    val value = bytes.toDouble()
    return when {
        value >= tb -> String.format("%.2f TB", value / tb)
        value >= gb -> String.format("%.2f GB", value / gb)
        value >= mb -> String.format("%.1f MB", value / mb)
        value >= kb -> String.format("%.1f KB", value / kb)
        else -> "$bytes B"
    }
}

private fun formatTrackGapOptionLabel(seconds: Float): String {
    if (seconds <= 0.01f) {
        return "Off"
    }
    val rounded = ((seconds * 10f).roundToInt() / 10f).coerceAtLeast(0f)
    val whole = rounded.roundToInt().toFloat()
    return if (abs(rounded - whole) < 0.05f) {
        "${whole.toInt()} s"
    } else {
        "${String.format("%.1f", rounded)} s"
    }
}

private fun formatMaxStorageOptionLabel(sizeMb: Int): String {
    val normalized = nearestMaxStorageOptionMb(sizeMb)
    if (normalized <= 0) {
        return "Unlimited"
    }
    val gb = normalized / 1024f
    val rounded = ((gb * 10f).roundToInt() / 10f).coerceAtLeast(0f)
    val whole = rounded.roundToInt().toFloat()
    return if (rounded >= 1f) {
        if (abs(rounded - whole) < 0.05f) {
            "${whole.toInt()} GB"
        } else {
            "${String.format("%.1f", rounded)} GB"
        }
    } else {
        "$normalized MB"
    }
}

private fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0L) {
        return "0:00"
    }
    val totalSeconds = durationMs / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}

@Preview(showBackground = true)
@Composable
private fun SonoraAppPreview() {
    SonoraTheme(dynamicColor = false) {
        SonoraApp(incomingSharedPlaylistUrlState = remember { mutableStateOf(null) })
    }
}
