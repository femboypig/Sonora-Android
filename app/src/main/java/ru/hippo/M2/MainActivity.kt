package ru.hippo.M2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
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
import java.io.File
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hippo.M2.music.PlaybackController
import ru.hippo.M2.music.PlaybackHistoryStore
import ru.hippo.M2.music.PlaylistStore
import ru.hippo.M2.music.RepeatMode
import ru.hippo.M2.music.TrackAnalytics
import ru.hippo.M2.music.TrackAnalyticsStore
import ru.hippo.M2.music.TrackItem
import ru.hippo.M2.music.TrackStore
import ru.hippo.M2.ui.theme.M2MiniPlayerBorderDark
import ru.hippo.M2.ui.theme.M2MiniPlayerBorderLight
import ru.hippo.M2.ui.theme.M2MiniPlayerDark
import ru.hippo.M2.ui.theme.M2MiniPlayerLight
import ru.hippo.M2.ui.theme.M2TabActiveDark
import ru.hippo.M2.ui.theme.M2TabActiveLight
import ru.hippo.M2.ui.theme.M2TabBarDark
import ru.hippo.M2.ui.theme.M2TabBarLight
import ru.hippo.M2.ui.theme.M2TabInactiveDark
import ru.hippo.M2.ui.theme.M2TabInactiveLight
import ru.hippo.M2.ui.theme.M2Theme

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
            M2Theme(dynamicColor = false) {
                M2App()
            }
        }
    }
}

private enum class M2Tab {
    Home,
    Music,
    Playlists
}

private enum class PlaylistCreateStep {
    Name,
    Tracks
}

private data class M2TabSpec(
    val tab: M2Tab,
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
    val isUser: Boolean
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

private val M2AndroidYSMusicFontFamily = FontFamily(
    Font(R.font.ysmusic_headline_bold, FontWeight.Bold)
)

private val M2AndroidSFProSemiboldFamily = FontFamily(
    Font(R.font.sf_pro_text_semibold, FontWeight.SemiBold),
    Font(R.font.sf_pro_text_bold, FontWeight.Bold)
)

private val M2AndroidHomeHeadingFontFamily = FontFamily(
    Font(R.font.tt_commons_pro_expanded_extrabold, FontWeight.ExtraBold)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun M2App() {
    val context = androidx.compose.ui.platform.LocalContext.current.applicationContext
    val trackStore = remember(context) { TrackStore(context) }
    val playlistStore = remember(context) { PlaylistStore(context) }
    val analyticsStore = remember(context) { TrackAnalyticsStore(context) }
    val playbackHistoryStore = remember(context) { PlaybackHistoryStore(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var tracks by remember { mutableStateOf(trackStore.loadTracks()) }
    var userPlaylists by remember { mutableStateOf(playlistStore.loadPlaylists()) }

    var selectedTab by rememberSaveable { mutableStateOf(M2Tab.Home) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var showMusicPage by rememberSaveable { mutableStateOf(false) }
    var musicQuery by rememberSaveable { mutableStateOf("") }
    var favoritesQuery by rememberSaveable { mutableStateOf("") }
    var playlistsQuery by rememberSaveable { mutableStateOf("") }
    var showHistoryPage by rememberSaveable { mutableStateOf(false) }
    var showSettingsPage by rememberSaveable { mutableStateOf(false) }
    var showFavoritesPage by rememberSaveable { mutableStateOf(false) }
    var showPlaylistsListPage by rememberSaveable { mutableStateOf(false) }

    var openedPlaylistID by rememberSaveable { mutableStateOf<String?>(null) }
    var openedHomeAlbumArtistKey by rememberSaveable { mutableStateOf<String?>(null) }
    var playlistCreateStep by rememberSaveable { mutableStateOf<PlaylistCreateStep?>(null) }
    var playlistCreateName by rememberSaveable { mutableStateOf("") }
    var playlistCreateQuery by rememberSaveable { mutableStateOf("") }
    var playlistCreateSelectedTrackIDs by remember { mutableStateOf(setOf<String>()) }
    var addTracksTargetPlaylistID by rememberSaveable { mutableStateOf<String?>(null) }
    var addTracksSelectedIDs by remember { mutableStateOf(setOf<String>()) }
    var playerVisible by rememberSaveable { mutableStateOf(false) }
    var showSleepTimerDialog by rememberSaveable { mutableStateOf(false) }
    var quickAddTrackID by rememberSaveable { mutableStateOf<String?>(null) }
    var showPlaylistOptionsDialog by rememberSaveable { mutableStateOf(false) }
    var showRenamePlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var renamePlaylistDraft by rememberSaveable { mutableStateOf("") }
    var coverTargetPlaylistID by rememberSaveable { mutableStateOf<String?>(null) }
    var homeVisitCount by rememberSaveable { mutableIntStateOf(1) }
    var wasHomeSelected by rememberSaveable { mutableStateOf(true) }
    var lastPresentedHomeHeroTrackID by rememberSaveable { mutableStateOf<String?>(null) }

    var analyticsVersion by remember { mutableIntStateOf(0) }
    var historyVersion by remember { mutableIntStateOf(0) }

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

    val density = LocalDensity.current
    val revealThreshold = remember(density) { with(density) { 62.dp.toPx() } }
    val dismissThreshold = remember(density) { with(density) { 40.dp.toPx().toInt() } }
    val compactPlaylistTitleThreshold = remember(density) { with(density) { 175.dp.toPx().toInt() } }
    var pullRevealDistance by remember { mutableFloatStateOf(0f) }
    var pullHideDistance by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(selectedTab) {
        showSearch = false
        pullRevealDistance = 0f
        pullHideDistance = 0f
        showHistoryPage = false
        showSettingsPage = false
        showFavoritesPage = false
        showPlaylistsListPage = false
    }

    LaunchedEffect(playbackController.currentTrackId) {
        if (playbackController.currentTrackId == null) {
            playerVisible = false
        }
    }

    val nestedScrollConnection = remember(
        openedPlaylistID,
        openedHomeAlbumArtistKey,
        selectedTab,
        showMusicPage,
        showFavoritesPage,
        showPlaylistsListPage,
        showSearch,
        musicQuery,
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

                val inPlaylistDetail = openedPlaylistID != null || openedHomeAlbumArtistKey != null
                if (selectedTab == M2Tab.Home) {
                    if (showSearch) {
                        showSearch = false
                    }
                    pullRevealDistance = 0f
                    pullHideDistance = 0f
                    return Offset.Zero
                }
                val isCollectionsSubpage = showMusicPage || showFavoritesPage || showPlaylistsListPage
                if (selectedTab == M2Tab.Playlists && !isCollectionsSubpage && !showSearch) {
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

                    selectedTab == M2Tab.Home -> homeListState.firstVisibleItemIndex == 0 &&
                        homeListState.firstVisibleItemScrollOffset == 0

                    selectedTab == M2Tab.Music -> musicListState.firstVisibleItemIndex == 0 &&
                        musicListState.firstVisibleItemScrollOffset == 0

                    selectedTab == M2Tab.Playlists -> playlistsListState.firstVisibleItemIndex == 0 &&
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

                    selectedTab == M2Tab.Home -> homeListState.firstVisibleItemIndex > 0 ||
                        homeListState.firstVisibleItemScrollOffset > dismissThreshold

                    selectedTab == M2Tab.Music -> musicListState.firstVisibleItemIndex > 0 ||
                        musicListState.firstVisibleItemScrollOffset > dismissThreshold

                    selectedTab == M2Tab.Playlists -> playlistsListState.firstVisibleItemIndex > 0 ||
                        playlistsListState.firstVisibleItemScrollOffset > dismissThreshold

                    else -> false
                }

                val hasQuery = when {
                    showMusicPage -> musicQuery.isNotBlank()
                    showFavoritesPage -> favoritesQuery.isNotBlank()
                    showPlaylistsListPage -> playlistsQuery.isNotBlank()
                    selectedTab == M2Tab.Home -> false
                    selectedTab == M2Tab.Music -> musicQuery.isNotBlank()
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
        M2TabSpec(M2Tab.Music, "Search", R.drawable.ic_global_magnifyingglass),
        M2TabSpec(M2Tab.Home, "Home", R.drawable.tab_home),
        M2TabSpec(M2Tab.Playlists, "Collections", R.drawable.tab_lib)
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

    val allPlaylists = remember(userPlaylists, lovelyTrackIDs) {
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
        }
    }

    val historyTracks = remember(trackByID, historyVersion, tracks) {
        playbackHistoryStore.recentTrackIds(limit = 120).mapNotNull { trackByID[it] }
    }

    val homeWaveTracks = remember(tracks, analyticsByID, favoriteTrackIDs) {
        buildHomeForYouTracks(tracks, analyticsByID, favoriteTrackIDs, limit = 120)
    }
    val homeRotationSeed = remember(homeVisitCount) {
        ((homeVisitCount - 1) / 4) + 1
    }
    val homeNeedThisTracks = remember(tracks, analyticsByID, favoriteTrackIDs, homeRotationSeed) {
        buildHomeNeedThisTracks(
            tracks = tracks,
            analyticsByID = analyticsByID,
            favoriteTrackIDs = favoriteTrackIDs,
            limit = 12,
            rotationSeed = homeRotationSeed
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

        val queueMatches = playbackController.isQueueMatching(homeWaveTracks)
        if (queueMatches) {
            val liveTrackID = playbackController.currentTrackId
            if (!liveTrackID.isNullOrBlank() && homeWaveTracks.any { it.id == liveTrackID }) {
                homeWaveDisplayTrackID = liveTrackID
            } else if (homeWaveDisplayTrackID.isNullOrBlank() ||
                homeWaveTracks.none { it.id == homeWaveDisplayTrackID }
            ) {
                homeWaveDisplayTrackID = homeWaveStartTrack?.id ?: homeWaveTracks.first().id
            }
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
    val homeFreshChoiceTracks = remember(tracks, analyticsByID, favoriteTrackIDs, homeRotationSeed) {
        buildHomeFreshChoiceTracks(
            tracks = tracks,
            analyticsByID = analyticsByID,
            favoriteTrackIDs = favoriteTrackIDs,
            limit = 10,
            rotationSeed = homeRotationSeed
        )
    }
    val homeAlbums = remember(tracks) {
        buildHomeAlbumItems(tracks, limit = 14)
    }

    val musicFiltered = remember(tracks, musicQuery) {
        tracks.filter { it.matchesQuery(musicQuery) }
    }
    val musicPlaylistsFiltered = remember(allPlaylists, musicQuery) {
        val normalized = musicQuery.trim().lowercase()
        if (normalized.isBlank()) {
            allPlaylists.take(8)
        } else {
            allPlaylists.filter { playlist ->
                playlist.name.lowercase().contains(normalized)
            }.take(8)
        }
    }
    val musicArtistsFiltered = remember(tracks, musicQuery) {
        buildSearchArtistItems(
            tracks = tracks,
            query = musicQuery,
            limit = 12
        )
    }
    val musicTracksForSearch = remember(musicFiltered, musicQuery) {
        if (musicQuery.trim().isBlank()) {
            musicFiltered.take(36)
        } else {
            musicFiltered
        }
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
    val openedPlaylistTracks = remember(openedPlaylist, trackByID) {
        openedPlaylist?.trackIds?.mapNotNull { trackByID[it] } ?: emptyList()
    }
    val openedHomeAlbum = remember(openedHomeAlbumArtistKey, homeAlbums) {
        homeAlbums.firstOrNull { it.artistKey == openedHomeAlbumArtistKey }
    }
    val openedHomeAlbumTracks = remember(openedHomeAlbum, trackByID) {
        openedHomeAlbum?.trackIds?.mapNotNull { trackByID[it] } ?: emptyList()
    }
    val openedDetailPlaylist = remember(openedPlaylist, openedHomeAlbum) {
        openedPlaylist ?: openedHomeAlbum?.let {
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
    val openedDetailTracks = remember(openedPlaylist, openedPlaylistTracks, openedHomeAlbumTracks) {
        if (openedPlaylist != null) {
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
        userPlaylists = playlistStore.loadPlaylists()
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == M2Tab.Home) {
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

    fun persistTracks(updated: List<TrackItem>) {
        tracks = updated
        scope.launch(Dispatchers.IO) {
            trackStore.saveTracks(updated)
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

                val result = withContext(Dispatchers.IO) {
                    trackStore.importTracks(uris)
                }
                tracks = withContext(Dispatchers.IO) {
                    trackStore.loadTracks()
                }

                val message = when {
                    result.added > 0 && result.failed == 0 -> "Added ${result.added} track(s)"
                    result.added > 0 -> "Added ${result.added}, failed ${result.failed}"
                    else -> "Could not add selected files"
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

    DisposableEffect(Unit) {
        onDispose {
            playbackController.release()
        }
    }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val tabBarBackground = if (isDark) M2TabBarDark else M2TabBarLight
    val tabActiveColor = if (isDark) M2TabActiveDark else M2TabActiveLight
    val tabInactiveColor = if (isDark) M2TabInactiveDark else M2TabInactiveLight

    val isCreateNameScreen = playlistCreateStep == PlaylistCreateStep.Name
    val isCreateTracksScreen = playlistCreateStep == PlaylistCreateStep.Tracks
    val isAddTracksScreen = addTracksTargetPlaylistID != null
    val inOverlayScreen = isCreateNameScreen || isCreateTracksScreen || isAddTracksScreen
    val inSubPage = showHistoryPage || showSettingsPage || showFavoritesPage || showPlaylistsListPage || showMusicPage

    val inPlaylistDetail = openedDetailPlaylist != null
    val isRootSearchPage = (selectedTab == M2Tab.Music) && !inSubPage && !inPlaylistDetail
    val showCompactPlaylistTitle = inPlaylistDetail && (
        playlistDetailListState.firstVisibleItemIndex > 0 ||
            playlistDetailListState.firstVisibleItemScrollOffset > compactPlaylistTitleThreshold
        )
    val miniPlayerTrack = playbackController.currentTrack
    val miniPlayerVisible = !inPlaylistDetail &&
        !inOverlayScreen &&
        !inSubPage &&
        !playerVisible &&
        miniPlayerTrack != null
    val listBottomInset = if (miniPlayerVisible) 76.dp else 2.dp

    val title = when {
        inPlaylistDetail -> if (showCompactPlaylistTitle) (openedDetailPlaylist?.name ?: "Playlist") else ""
        showHistoryPage -> "History"
        showSettingsPage -> "Settings"
        showFavoritesPage -> "Favorites"
        showPlaylistsListPage -> "Playlists"
        showMusicPage -> "Music"
        selectedTab == M2Tab.Home -> "Home"
        selectedTab == M2Tab.Music -> "Search"
        else -> "Collections"
    }

    val activeSearchQuery = when {
        inPlaylistDetail -> ""
        showMusicPage -> musicQuery
        showFavoritesPage -> favoritesQuery
        selectedTab == M2Tab.Music -> musicQuery
        selectedTab == M2Tab.Playlists -> playlistsQuery
        else -> ""
    }
    val activeSearchPlaceholder = when {
        showMusicPage || showFavoritesPage -> "Search"
        selectedTab == M2Tab.Music -> "Search"
        selectedTab == M2Tab.Playlists -> "Search Collections"
        else -> ""
    }
    val searchVisible = when {
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
            showMusicPage -> musicQuery = value
            showFavoritesPage -> favoritesQuery = value
            else -> when (selectedTab) {
                M2Tab.Home -> Unit
                M2Tab.Music -> musicQuery = value
                M2Tab.Playlists -> playlistsQuery = value
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!playerVisible && !inOverlayScreen) {
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
                                        style = if (showFavoritesPage || showPlaylistsListPage || showMusicPage) {
                                            TextStyle(
                                                fontFamily = M2AndroidYSMusicFontFamily,
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
                                        modifier = if (inPlaylistDetail) {
                                            Modifier.align(Alignment.Center)
                                        } else if (showHistoryPage) {
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
                                        style = if (selectedTab == M2Tab.Home) {
                                            TextStyle(
                                                fontFamily = M2AndroidYSMusicFontFamily,
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
                                    inPlaylistDetail && openedDetailPlaylist?.isUser == true -> {
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
                                                showSearch = true
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

                                    !inPlaylistDetail && !inSubPage && selectedTab == M2Tab.Home -> {
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

                        if (inPlaylistDetail && showCompactPlaylistTitle) {
                            Text(
                                text = openedDetailPlaylist?.name ?: "Playlist",
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

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                }
            }
        },
        bottomBar = {
            if (!inPlaylistDetail && !inOverlayScreen && !inSubPage && !playerVisible) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                    NavigationBar(
                        containerColor = tabBarBackground,
                        modifier = Modifier.height(49.dp),
                        windowInsets = WindowInsets(0, 0, 0, 0)
                    ) {
                        tabSpecs.forEach { spec ->
                            val selected = selectedTab == spec.tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (spec.tab == M2Tab.Music) {
                                        showMusicPage = false
                                    }
                                    selectedTab = spec.tab
                                    showHistoryPage = false
                                    showSettingsPage = false
                                    showFavoritesPage = false
                                    showPlaylistsListPage = false
                                    openedPlaylistID = null
                                    openedHomeAlbumArtistKey = null
                                },
                                icon = {
                                    val iconSize = if (spec.tab == M2Tab.Home) 22.dp else 20.dp
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
                        openedPlaylistID = null
                        openedHomeAlbumArtistKey = null
                    }
                ) {
                    PlaylistDetailPage(
                        listState = playlistDetailListState,
                        playlist = openedDetailPlaylist,
                        tracks = openedDetailTracks,
                        canRemoveTracks = openedPlaylist?.isUser == true,
                        currentTrackID = playbackController.currentTrackId,
                        isPlaying = playbackController.isPlaying,
                        isCurrentQueueMatching = playbackController.isQueueMatching(openedDetailTracks),
                        isSleepTimerActive = playbackController.isSleepTimerActive,
                        onTrackTap = { tapped ->
                            playbackController.playOrToggleFromQueue(
                                queue = openedDetailTracks,
                                targetTrackId = tapped.id
                            )
                            playerVisible = true
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
                            onFavoriteToggle(track.id)
                            scope.launch {
                                snackbarHostState.showSnackbar("Favorite state updated")
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

                showSettingsPage -> SettingsPage()

                showFavoritesPage -> SwipeDismissPage(onDismiss = { showFavoritesPage = false }) {
                    FavoritesPage(
                        listState = favoritesListState,
                        allFavorites = favoriteTracks,
                        filteredTracks = favoritesFiltered,
                        bottomInset = listBottomInset,
                        currentTrackID = playbackController.currentTrackId,
                        isPlaying = playbackController.isPlaying,
                        onTrackTap = { tapped ->
                            playbackController.playOrToggleFromQueue(
                                queue = favoriteTracks,
                                targetTrackId = tapped.id
                            )
                            playerVisible = true
                        },
                        onTrackLongPress = { track ->
                            onFavoriteToggle(track.id)
                            scope.launch {
                                snackbarHostState.showSnackbar("Removed from favorites")
                            }
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
                        },
                        onCreatePlaylistTap = {
                            playlistCreateStep = PlaylistCreateStep.Name
                            playlistCreateName = ""
                            playlistCreateQuery = ""
                            playlistCreateSelectedTrackIDs = emptySet()
                        }
                    )
                }

                selectedTab == M2Tab.Home -> HomePage(
                    listState = homeListState,
                    waveQueue = homeWaveTracks,
                    waveStartTrack = homeWaveDisplayTrack,
                    tasteTracks = homeTasteTracks,
                    freshChoiceTracks = homeFreshChoiceTracks,
                    isWavePlaying = playbackController.isQueueMatching(homeWaveTracks) && playbackController.isPlaying,
                    bottomInset = listBottomInset,
                    onWaveToggleTap = {
                        val waveTarget = homeWaveStartTrack ?: homeWaveTracks.firstOrNull()
                        if (waveTarget != null) {
                            if (playbackController.isQueueMatching(homeWaveTracks) &&
                                playbackController.currentTrackId != null
                            ) {
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
                    filteredTracks = musicFiltered,
                    bottomInset = listBottomInset,
                    currentTrackID = playbackController.currentTrackId,
                    isPlaying = playbackController.isPlaying,
                    onTrackTap = { tapped ->
                        playbackController.playOrToggleFromQueue(
                            queue = tracks,
                            targetTrackId = tapped.id
                        )
                        playerVisible = true
                    },
                    onTrackLongPress = { track ->
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

                selectedTab == M2Tab.Music -> SearchPage(
                    listState = musicListState,
                    searchQuery = musicQuery,
                    trackResults = musicTracksForSearch,
                    playlistResults = musicPlaylistsFiltered,
                    artistResults = musicArtistsFiltered,
                    trackByID = trackByID,
                    bottomInset = listBottomInset,
                    onTrackTap = { tapped ->
                        playbackController.playOrToggleFromQueue(
                            queue = tracks,
                            targetTrackId = tapped.id
                        )
                        playerVisible = true
                    },
                    onPlaylistTap = { playlist ->
                        openedPlaylistID = playlist.id
                    },
                    onArtistTap = { artist ->
                        musicQuery = artist.title
                        showSearch = true
                    }
                )

                selectedTab == M2Tab.Playlists -> PlaylistsPage(
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

            val activeMiniTrack = miniPlayerTrack
            if (miniPlayerVisible && activeMiniTrack != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    MiniPlayer(
                        modifier = Modifier,
                        track = activeMiniTrack,
                        hasQueue = playbackController.queueCount > 0,
                        isPlaying = playbackController.isPlaying,
                        canStep = playbackController.canStep,
                        onOpen = { playerVisible = true },
                        onTogglePlayPause = { playbackController.togglePlayPause() },
                        onPrevious = { playbackController.playPreviousFromUser() },
                        onNext = { playbackController.playNextFromUser() }
                    )
                }
            }

            val playerTrack = playbackController.currentTrack
            if (playerVisible && playerTrack != null) {
                val isFavorite = tracks.firstOrNull { it.id == playerTrack.id }?.isFavorite
                    ?: playerTrack.isFavorite
                PlayerView(
                    modifier = Modifier.fillMaxSize(),
                    track = playerTrack,
                    hasQueue = playbackController.queueCount > 0,
                    isPlaying = playbackController.isPlaying,
                    canStep = playbackController.canStep,
                    isFavorite = isFavorite,
                    isShuffleEnabled = playbackController.isShuffleEnabled,
                    repeatMode = playbackController.repeatMode,
                    isSleepTimerActive = playbackController.isSleepTimerActive,
                    sleepTimerRemainingMs = playbackController.sleepTimerRemainingMs,
                    nextTrack = playbackController.predictedNextTrackForSkip(),
                    onClose = { playerVisible = false },
                    onTogglePlayPause = { playbackController.togglePlayPause() },
                    onPrevious = { playbackController.playPreviousFromUser() },
                    onNext = { playbackController.playNextFromUser() },
                    onToggleShuffle = { playbackController.toggleShuffleEnabled() },
                    onCycleRepeat = { playbackController.cycleRepeatMode() },
                    onSleepTimerTap = { showSleepTimerDialog = true },
                    onToggleFavorite = { onFavoriteToggle(playerTrack.id) },
                    onSeek = { positionMs -> playbackController.seekTo(positionMs) },
                    positionMsProvider = { playbackController.currentPositionMs() },
                    durationMsProvider = { playbackController.durationMs() }
                )
            }
        }
    }

    if (showPlaylistOptionsDialog && openedPlaylist?.isUser == true) {
        AlertDialog(
            onDismissRequest = { showPlaylistOptionsDialog = false },
            title = {
                Text(
                    text = openedPlaylist.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(
                        onClick = {
                            addTracksTargetPlaylistID = openedPlaylist.id
                            addTracksSelectedIDs = emptySet()
                            showPlaylistOptionsDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Music")
                    }
                    TextButton(
                        onClick = {
                            coverTargetPlaylistID = openedPlaylist.id
                            showPlaylistOptionsDialog = false
                            changePlaylistCoverLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Cover")
                    }
                    TextButton(
                        onClick = {
                            renamePlaylistDraft = openedPlaylist.name
                            showRenamePlaylistDialog = true
                            showPlaylistOptionsDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Rename Playlist")
                    }
                    TextButton(
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
            },
            confirmButton = {
                TextButton(onClick = { showPlaylistOptionsDialog = false }) {
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
                TextButton(
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
                TextButton(onClick = { showRenamePlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
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
                TextButton(onClick = { quickAddTrackID = null }) {
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
                    TextButton(
                        onClick = {
                            playbackController.clearSleepTimer()
                            showSleepTimerDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Off")
                    }
                    TextButton(
                        onClick = {
                            playbackController.startSleepTimer(15)
                            showSleepTimerDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("15 min")
                    }
                    TextButton(
                        onClick = {
                            playbackController.startSleepTimer(30)
                            showSleepTimerDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("30 min")
                    }
                    TextButton(
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
                    TextButton(
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
                TextButton(onClick = { showSleepTimerDialog = false }) {
                    Text("Close")
                }
            }
        )
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
                        fontFamily = M2AndroidSFProSemiboldFamily,
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
                ru.hippo.M2.ui.theme.M2AccentYellow
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
                fontFamily = M2AndroidSFProSemiboldFamily,
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
                tint = ru.hippo.M2.ui.theme.M2AccentYellow,
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
    onTrackTap: (TrackItem) -> Unit,
    onTrackLongPress: (TrackItem) -> Unit,
    onTrackSwipeFavoriteToggle: (TrackItem) -> Unit,
    onTrackSwipeDelete: (TrackItem) -> Unit,
    onTrackSwipeAddToPlaylist: (TrackItem) -> Unit
) {
    if (filteredTracks.isEmpty()) {
        val message = if (tracks.isEmpty()) {
            "No music files in On My iPhone/M2/M2"
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
                onClick = { onTrackTap(track) },
                onLongPress = { onTrackLongPress(track) },
                startActions = listOf(
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
                ),
                endActions = listOf(
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
    trackResults: List<TrackItem>,
    playlistResults: List<PlaylistUiItem>,
    artistResults: List<SearchArtistUiItem>,
    trackByID: Map<String, TrackItem>,
    bottomInset: androidx.compose.ui.unit.Dp,
    onTrackTap: (TrackItem) -> Unit,
    onPlaylistTap: (PlaylistUiItem) -> Unit,
    onArtistTap: (SearchArtistUiItem) -> Unit
) {
    val hasQuery = searchQuery.trim().isNotBlank()
    val hasResults = trackResults.isNotEmpty() || playlistResults.isNotEmpty() || artistResults.isNotEmpty()
    if (!hasResults) {
        EmptyListLabel(text = "No search results.")
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 2.dp, bottom = bottomInset)
    ) {
        item(key = "search_intro") {
            // SearchIntroCard() removed
        }

        if (playlistResults.isNotEmpty()) {
            item(key = "search_playlists_heading") {
                SearchSectionHeading(text = "Playlists")
            }
            item(key = "search_playlists_row") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(playlistResults, key = { it.id }) { playlist ->
                        SearchCollectionCard(
                            title = playlist.name,
                            subtitle = "${playlist.trackIds.size} tracks",
                            coverTrack = playlist.trackIds.firstNotNullOfOrNull { trackByID[it] },
                            placeholderRes = R.drawable.tab_lib,
                            onClick = { onPlaylistTap(playlist) }
                        )
                    }
                }
            }
        }

        if (artistResults.isNotEmpty()) {
            item(key = "search_artists_heading") {
                SearchSectionHeading(text = "Artists")
            }
            item(key = "search_artists_row") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(artistResults, key = { it.key }) { artist ->
                        val coverTrack = artist.trackIds.firstNotNullOfOrNull { trackByID[it] }
                        SearchArtistCard(
                            artist = artist,
                            coverTrack = coverTrack,
                            onClick = { onArtistTap(artist) }
                        )
                    }
                }
            }
        }

        if (trackResults.isNotEmpty()) {
            item(key = "search_tracks_heading") {
                SearchSectionHeading(text = if (hasQuery) "Tracks" else "Music")
            }
            item(key = "search_tracks_row") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trackResults, key = { it.id }) { track ->
                        HomeRecommendationCard(
                            track = track,
                            onClick = { onTrackTap(track) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchIntroCard() {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val bg = if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.Black.copy(alpha = 0.05f)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Search",
            style = TextStyle(
                fontFamily = M2AndroidHomeHeadingFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Find tracks, artists and playlists in one place.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SearchSectionHeading(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = M2AndroidYSMusicFontFamily,
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
private fun SearchCollectionCard(
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
private fun SearchArtistCard(
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
private fun HomePage(
    listState: androidx.compose.foundation.lazy.LazyListState,
    waveQueue: List<TrackItem>,
    waveStartTrack: TrackItem?,
    tasteTracks: List<TrackItem>,
    freshChoiceTracks: List<TrackItem>,
    isWavePlaying: Boolean,
    bottomInset: androidx.compose.ui.unit.Dp,
    onWaveToggleTap: () -> Unit,
    onTasteTap: (TrackItem) -> Unit,
    onFreshChoiceTap: (TrackItem) -> Unit
) {
    if (waveQueue.isEmpty()) {
        EmptyListLabel(text = "No music files in On My iPhone/M2/M2")
        return
    }
    val waveTrack = waveStartTrack ?: waveQueue.firstOrNull()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 4.dp, bottom = bottomInset)
    ) {
        if (waveTrack != null) {
            item(key = "home_wave") {
                HomeMyWaveCard(
                    track = waveTrack,
                    isPlaying = isWavePlaying,
                    onPlayToggle = onWaveToggleTap
                )
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
    }
}

@Composable
private fun HomeSectionHeading(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = M2AndroidHomeHeadingFontFamily,
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
            fontFamily = M2AndroidYSMusicFontFamily,
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
    isPlaying: Boolean,
    onPlayToggle: () -> Unit
) {
    val paletteTarget = remember(track.id, track.artworkPath, track.filePath) {
        buildWavePaletteForTrack(track)
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
        Canvas(modifier = Modifier.matchParentSize()) {
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

        WaveGrainOverlay(seed = track.id.hashCode(), modifier = Modifier.matchParentSize())

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
                    fontFamily = M2AndroidHomeHeadingFontFamily,
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

    val fallback = Color(0xFF4A3E35)
    return listOf(
        fallback,
        blendColors(fallback, Color(0xFF7E6B59), 0.32f),
        blendColors(fallback, Color.Black, 0.18f),
        blendColors(fallback, Color(0xFF7A5B64), 0.24f)
    )
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
private fun SettingsPage() {
    EmptyListLabel(text = "Settings UI")
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
                        fontFamily = M2AndroidYSMusicFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "›",
                    style = TextStyle(
                        fontFamily = M2AndroidYSMusicFontFamily,
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
                            fontFamily = M2AndroidYSMusicFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 23.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "›",
                        style = TextStyle(
                            fontFamily = M2AndroidYSMusicFontFamily,
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
                        fontFamily = M2AndroidYSMusicFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = "›",
                    style = TextStyle(
                        fontFamily = M2AndroidYSMusicFontFamily,
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
            text = "${item.trackIds.size} tracks",
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
                fontFamily = M2AndroidSFProSemiboldFamily,
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
                tint = if (isSleepTimerActive) ru.hippo.M2.ui.theme.M2AccentYellow else primaryColor
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
                onClick = { onTrackTap(track) },
                onLongPress = { onTrackLongPress(track) },
                activeColor = playlistAccent,
                startActions = emptyList(),
                endActions = buildList {
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
                onClick = { onTrackTap(track) },
                onLongPress = { onTrackLongPress(track) },
                startActions = emptyList(),
                endActions = listOf(
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
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    startActions: List<SwipeTrackAction>,
    endActions: List<SwipeTrackAction>
) {
    val density = LocalDensity.current
    val actionWidth = 74.dp
    val actionWidthPx = with(density) { actionWidth.toPx() }
    val maxStartOffset = if (startActions.isEmpty()) 0f else actionWidthPx * startActions.size
    val maxEndOffset = if (endActions.isEmpty()) 0f else actionWidthPx * endActions.size
    val fullSwipeTrigger = with(density) { 108.dp.toPx() }

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
                    enabled = startActions.isNotEmpty() || endActions.isNotEmpty(),
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
                        if (dragOffsetPx != 0f) {
                            dragOffsetPx = 0f
                        } else {
                            onClick()
                        }
                    },
                    onLongClick = {
                        if (dragOffsetPx != 0f) {
                            dragOffsetPx = 0f
                        } else {
                            onLongPress()
                        }
                    }
                )
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showsPlaybackIndicator) {
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
                color = if (isCurrent) activeColor else MaterialTheme.colorScheme.onSurface,
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
                    fontFamily = M2AndroidSFProSemiboldFamily,
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

        val decoded = withContext(Dispatchers.IO) {
            decodeTrackArtworkBitmap(track, maxSize = maxSize)
        }
        if (decoded != null) {
            artworkCachePut(key, decoded)
        } else {
            artworkMissingCachePut(key)
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

        val decoded = withContext(Dispatchers.IO) {
            decodeArtworkBitmap(normalizedPath, maxSize = maxSize)
        }
        if (decoded != null) {
            artworkCachePut(key, decoded)
        } else {
            artworkMissingCachePut(key)
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
private fun PlainControlButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean,
    size: Dp,
    iconSize: Dp,
    iconWidth: Dp = iconSize,
    iconHeight: Dp = iconSize,
    tint: Color,
    disabledAlpha: Float = 0.45f
) {
    Box(
        modifier = Modifier
            .size(size)
            .alpha(if (enabled) 1f else disabledAlpha)
            .clip(RoundedCornerShape(999.dp))
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
    val background = if (isDark) M2MiniPlayerDark else M2MiniPlayerLight
    val border = if (isDark) M2MiniPlayerBorderDark else M2MiniPlayerBorderLight
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

    LaunchedEffect(track.id, isPlaying, hasQueue) {
        if (!isPlaying || !hasQueue) {
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
                TrackArtwork(
                    track = track,
                    placeholderRes = R.drawable.tab_note,
                    placeholderSize = 96.dp,
                    decodeMaxSize = 1024,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(artworkHeight)
                )

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
                        WaveSliderTrack(
                            sliderState = sliderState,
                            activeColor = sliderActiveColor,
                            inactiveColor = sliderInactiveColor,
                            wavePhase = wavePhase,
                            waveSeed = waveSeed
                        )
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
                            fontFamily = M2AndroidSFProSemiboldFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = (-0.1).sp
                        ),
                        color = secondaryColor
                    )
                    Text(
                        text = formatDuration(durationMs),
                        style = TextStyle(
                            fontFamily = M2AndroidSFProSemiboldFamily,
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
                        .padding(start = 14.dp, end = 14.dp, bottom = 6.dp)
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
                            ru.hippo.M2.ui.theme.M2AccentYellow
                        }
                        PlainControlButton(
                            iconRes = repeatIcon,
                            contentDescription = "Repeat",
                            onClick = onCycleRepeat,
                            enabled = hasQueue,
                            size = 42.dp,
                            iconSize = 24.dp,
                            tint = repeatTint
                        )
                        PlainControlButton(
                            iconRes = R.drawable.ic_player_shuffle,
                            contentDescription = "Shuffle",
                            onClick = onToggleShuffle,
                            enabled = hasQueue,
                            size = 42.dp,
                            iconSize = 24.dp,
                            tint = if (isShuffleEnabled) {
                                ru.hippo.M2.ui.theme.M2AccentYellow
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
                            tint = controlColor
                        )
                        PlainControlButton(
                            iconRes = if (isPlaying) R.drawable.ic_global_pause else R.drawable.ic_global_play,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            onClick = onTogglePlayPause,
                            enabled = hasQueue,
                            size = 76.dp,
                            iconSize = 56.dp,
                            tint = controlColor
                        )
                        PlainControlButton(
                            iconRes = R.drawable.ic_global_next,
                            contentDescription = "Next",
                            onClick = onNext,
                            enabled = canStep,
                            size = 64.dp,
                            iconSize = 44.dp,
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
                            tint = if (isSleepTimerActive) {
                                ru.hippo.M2.ui.theme.M2AccentYellow
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
                            .padding(bottom = 6.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = secondaryColor
                    )
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
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
            TextButton(onClick = onAdd) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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

private val artworkCacheLock = Any()
private val artworkCache = object : LinkedHashMap<String, androidx.compose.ui.graphics.ImageBitmap>(180, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, androidx.compose.ui.graphics.ImageBitmap>?): Boolean {
        return size > 180
    }
}
private val artworkMissingCache = object : LinkedHashSet<String>() {
    override fun add(element: String): Boolean {
        if (size >= 360) {
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

private fun decodeArtworkBitmap(path: String, maxSize: Int = 1024): androidx.compose.ui.graphics.ImageBitmap? {
    return runCatching {
        if (path.isBlank()) {
            return@runCatching null
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
            inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
        }
        BitmapFactory.decodeFile(file.absolutePath, options)?.asImageBitmap()
    }.getOrNull()
}

private fun decodeArtworkBitmapRaw(path: String, maxSize: Int = 1024): android.graphics.Bitmap? {
    return runCatching {
        if (path.isBlank()) {
            return@runCatching null
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
            inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
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
            inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
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
            inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
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
private fun M2AppPreview() {
    M2Theme(dynamicColor = false) {
        M2App()
    }
}
