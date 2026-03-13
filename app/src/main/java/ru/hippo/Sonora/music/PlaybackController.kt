package ru.hippo.Sonora.music

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaMetadata
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.PowerManager
import android.os.SystemClock
import androidx.media3.common.AudioAttributes as Media3AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToLong
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.hippo.Sonora.MainActivity
import ru.hippo.Sonora.R

enum class RepeatMode {
    None,
    Queue,
    Track
}

class PlaybackController(
    context: Context,
    private val onTrackPlayed: (String) -> Unit = {},
    private val onTrackSkipped: (String) -> Unit = {},
    private val onTrackPlaybackFailed: (String) -> Unit = {}
) {

    private data class SkipRollbackState(
        val queue: List<TrackItem>,
        val index: Int,
        val trackId: String,
        val positionMs: Long,
        val shouldResumePlaying: Boolean,
        val playbackOrder: List<Int>,
        val playbackOrderPosition: Int
    )

    var currentTrackId: String? by mutableStateOf(null)
        private set

    var isPlaying: Boolean by mutableStateOf(false)
        private set

    var isPreparing: Boolean by mutableStateOf(false)
        private set

    val visualIsPlaying: Boolean
        get() = isPlaying || (isPreparing && pendingPlayWhenPrepared && (currentTrackId != null || pendingTrackId != null))

    var queueCount: Int by mutableIntStateOf(0)
        private set

    var isShuffleEnabled: Boolean by mutableStateOf(false)
        private set

    var repeatMode: RepeatMode by mutableStateOf(RepeatMode.None)
        private set

    var sleepTimerRemainingMs: Long by mutableLongStateOf(0L)
        private set

    val canStep: Boolean
        get() = queueCount > 1 || (queueCount > 0 && (isShuffleEnabled || repeatMode != RepeatMode.None))

    val isSleepTimerActive: Boolean
        get() = sleepTimerRemainingMs > 0L

    val currentTrack: TrackItem?
        get() = queue.getOrNull(currentIndex)

    private val pendingTrack: TrackItem?
        get() {
            val targetId = pendingTrackId
            if (!targetId.isNullOrBlank()) {
                queue.firstOrNull { it.id == targetId }?.let { return it }
            }
            return queue.getOrNull(pendingTrackIndex)
        }

    val displayTrack: TrackItem?
        get() {
            if (!isPreparing) {
                return currentTrack
            }
            pendingTrack?.let { return it }
            currentTrack?.let { return it }
            val stableTrackId = lastPreparedTrackState?.trackId
            if (!stableTrackId.isNullOrBlank()) {
                queue.firstOrNull { it.id == stableTrackId }?.let { return it }
                lastPreparedTrackState?.queue?.firstOrNull { it.id == stableTrackId }?.let { return it }
            }
            return null
        }

    val displayTrackId: String?
        get() = displayTrack?.id ?: pendingTrackId ?: currentTrackId

    private val appContext = context.applicationContext
    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val mediaSession = MediaSession(appContext, "SonoraPlayback")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val random = Random(SystemClock.elapsedRealtime())
    private val artworkCache = mutableMapOf<String, Bitmap?>()
    private val artworkLoadingTrackIds = mutableSetOf<String>()

    private var mediaPlayer: MediaPlayer? = null
    private var exoPlayer: ExoPlayer? = null
    private var queue: List<TrackItem> = emptyList()
    private var currentIndex: Int = -1
    private var playbackOrder: List<Int> = emptyList()
    private var playbackOrderPosition: Int = -1
    private var sleepTimerJob: Job? = null
    private var pendingAutoNextJob: Job? = null
    private var trackGapDelayMs: Long = 0L
    private var pendingSeekPositionMs: Long = -1L
    private var pendingSeekSetAtMs: Long = 0L
    private val pendingSeekHoldTimeoutMs: Long = 15_000L
    private var playRequestToken: Long = 0L
    private var playerPrepared: Boolean = false
    private var pendingPlayWhenPrepared: Boolean = false
    private var lastPreparedTrackState: SkipRollbackState? = null
    private var pendingTrackIndex: Int = -1
    private var pendingTrackId: String? = null
    private var pendingSkippedTrackId: String? = null
    private var pendingSkipRollback: SkipRollbackState? = null

    private fun clearPendingSeekHold() {
        pendingSeekPositionMs = -1L
        pendingSeekSetAtMs = 0L
    }

    init {
        createNotificationChannel()
        mediaSession.setCallback(object : MediaSession.Callback() {
            override fun onPlay() {
                if (!isPlaying) {
                    togglePlayPause()
                }
            }

            override fun onPause() {
                if (isPlaying) {
                    togglePlayPause()
                }
            }

            override fun onSkipToNext() {
                playNextFromUser()
            }

            override fun onSkipToPrevious() {
                playPreviousFromUser()
            }

            override fun onSeekTo(pos: Long) {
                seekTo(pos)
            }

            override fun onStop() {
                stop()
            }
        })
        mediaSession.setSessionActivity(
            PendingIntent.getActivity(
                appContext,
                7001,
                Intent(appContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        mediaSession.isActive = false
        PlaybackRuntime.attach(this)
    }

    fun currentPositionMs(): Long {
        val mediaPlayerRef = mediaPlayer
        val exoPlayerRef = exoPlayer
        if (mediaPlayerRef == null && exoPlayerRef == null && isPreparing) {
            val pendingSeek = pendingSeekPositionMs
            if (pendingSeek >= 0L) {
                return pendingSeek
            }
            lastPreparedTrackState?.positionMs?.coerceAtLeast(0L)?.let { return it }
        }
        val position = when {
            exoPlayerRef != null -> exoPlayerRef.currentPosition.coerceAtLeast(0L)
            else -> (mediaPlayerRef?.currentPosition ?: 0).toLong().coerceAtLeast(0L)
        }
        val pendingSeek = pendingSeekPositionMs
        if (pendingSeek < 0L) {
            return position
        }
        val elapsed = android.os.SystemClock.elapsedRealtime() - pendingSeekSetAtMs
        val reached = kotlin.math.abs(position - pendingSeek) <= 1500L
        val expired = elapsed >= pendingSeekHoldTimeoutMs
        if (reached || expired || (mediaPlayerRef == null && exoPlayerRef == null)) {
            clearPendingSeekHold()
            return position
        }
        return pendingSeek
    }

    fun durationMs(): Long {
        val playerDuration = when {
            exoPlayer != null -> exoPlayer?.duration?.coerceAtLeast(0L) ?: 0L
            else -> mediaPlayer?.duration?.toLong() ?: 0L
        }
        if (playerDuration > 0L) {
            return playerDuration
        }
        return displayTrack?.durationMs ?: 0L
    }

    fun seekTo(positionMs: Long) {
        val mediaPlayerRef = mediaPlayer
        val exoPlayerRef = exoPlayer
        if (mediaPlayerRef == null && exoPlayerRef == null) {
            clearPendingSeekHold()
            return
        }
        val playerDuration = when {
            exoPlayerRef != null -> exoPlayerRef.duration.coerceAtLeast(0L)
            else -> mediaPlayerRef?.duration?.toLong()?.coerceAtLeast(0L) ?: 0L
        }
        val fallbackDuration = currentTrack?.durationMs?.coerceAtLeast(0L) ?: 0L
        val knownDuration = maxOf(playerDuration, fallbackDuration)
        val clamped = if (knownDuration > 0L) {
            positionMs.coerceIn(0L, knownDuration)
        } else {
            positionMs.coerceAtLeast(0L)
        }
        pendingSeekPositionMs = clamped
        pendingSeekSetAtMs = android.os.SystemClock.elapsedRealtime()
        if (!playerPrepared || playerDuration <= 0L) {
            updateExternalState()
            return
        }
        if (exoPlayerRef != null) {
            exoPlayerRef.seekTo(clamped)
        } else {
            mediaPlayerRef?.seekTo(clamped.toInt())
        }
        updateExternalState()
    }

    fun applyAudioSettings(trackGapSeconds: Float) {
        trackGapDelayMs = (trackGapSeconds.coerceIn(0f, 8f) * 1000f).roundToLong()
    }

    fun predictedNextTrackForSkip(): TrackItem? {
        val nextIndex = predictedNextIndex() ?: return null
        return queue.getOrNull(nextIndex)
    }

    fun playOrToggleFromQueue(queue: List<TrackItem>, targetTrackId: String) {
        pendingSkipRollback = null
        if (queue.isEmpty() || targetTrackId.isBlank()) {
            stop()
            return
        }

        cancelPendingAutoNext()

        if (currentTrackId == targetTrackId && isSameQueue(queue)) {
            togglePlayPause()
            return
        }

        val index = queue.indexOfFirst { it.id == targetTrackId }
        if (index < 0) {
            return
        }

        val skippedTrackId = currentTrackId
            ?.takeIf { isPlaying && it != targetTrackId }

        this.queue = queue
        queueCount = queue.size
        rebuildPlaybackOrder(anchorIndex = index)
        val started = playAt(index)
        if (started) {
            stagePendingSkippedTrack(skippedTrackId, targetTrackId)
        } else {
            clearPendingSkippedTrack()
        }
    }

    fun isQueueMatching(queue: List<TrackItem>): Boolean {
        return isSameQueue(queue)
    }

    fun currentQueueTrackIds(): List<String> {
        return queue.map { it.id }
    }

    fun replaceQueuePreservingCurrent(updatedQueue: List<TrackItem>) {
        if (updatedQueue.isEmpty()) {
            return
        }
        val currentId = currentTrackId ?: return
        val nextIndex = updatedQueue.indexOfFirst { it.id == currentId }
        if (nextIndex < 0) {
            return
        }

        queue = updatedQueue
        queueCount = updatedQueue.size
        currentIndex = nextIndex
        if (!pendingTrackId.isNullOrBlank()) {
            pendingTrackIndex = updatedQueue.indexOfFirst { it.id == pendingTrackId }
            if (pendingTrackIndex < 0) {
                clearPendingTrackTarget()
            }
        }
        rebuildPlaybackOrder(anchorIndex = nextIndex)
        updateExternalState()
    }

    fun restoreSession(
        queue: List<TrackItem>,
        targetTrackId: String,
        positionMs: Long,
        shouldPlay: Boolean,
        shuffleEnabled: Boolean,
        repeatMode: RepeatMode
    ) {
        if (queue.isEmpty() || targetTrackId.isBlank()) {
            return
        }

        applyRepeatMode(repeatMode)
        applyShuffleEnabled(shuffleEnabled)
        playOrToggleFromQueue(queue = queue, targetTrackId = targetTrackId)
        val clampedPosition = positionMs.coerceAtLeast(0L)

        if (!shouldPlay) {
            pendingPlayWhenPrepared = false
            pendingSeekPositionMs = clampedPosition
            pendingSeekSetAtMs = android.os.SystemClock.elapsedRealtime()
            exoPlayer?.let { player ->
                if (playerPrepared) {
                    seekTo(clampedPosition)
                }
                if (playerPrepared && player.isPlaying) {
                    player.pause()
                }
                isPlaying = false
                updateExternalState()
            }
            mediaPlayer?.let { player ->
                runCatching { player.setVolume(0f, 0f) }
                if (playerPrepared) {
                    seekTo(clampedPosition)
                }
                if (playerPrepared && player.isPlaying) {
                    player.pause()
                }
                isPlaying = false
                runCatching { player.setVolume(1f, 1f) }
                updateExternalState()
            }
            return
        }

        seekTo(clampedPosition)
        if (!isPlaying) {
            togglePlayPause()
        }
    }

    fun togglePlayPause() {
        cancelPendingAutoNext()
        val exoPlayerRef = exoPlayer
        val mediaPlayerRef = mediaPlayer
        if (exoPlayerRef == null && mediaPlayerRef == null && !isPreparing) {
            return
        }
        if (!playerPrepared) {
            pendingPlayWhenPrepared = !pendingPlayWhenPrepared
            isPlaying = pendingPlayWhenPrepared
            exoPlayerRef?.playWhenReady = pendingPlayWhenPrepared
            updateExternalState()
            return
        }
        if (isPlaying) {
            if (exoPlayerRef != null) {
                exoPlayerRef.pause()
            } else {
                mediaPlayerRef?.pause()
            }
            isPlaying = false
        } else {
            if (exoPlayerRef != null) {
                exoPlayerRef.play()
            } else {
                mediaPlayerRef?.start()
            }
            isPlaying = true
        }
        updateExternalState()
    }

    fun toggleShuffleEnabled() {
        applyShuffleEnabled(!isShuffleEnabled)
    }

    fun applyShuffleEnabled(enabled: Boolean) {
        if (isShuffleEnabled == enabled) {
            return
        }
        isShuffleEnabled = enabled
        rebuildPlaybackOrder(anchorIndex = currentIndex)
        updateExternalState()
    }

    fun cycleRepeatMode() {
        val nextMode = when (repeatMode) {
            RepeatMode.None -> RepeatMode.Queue
            RepeatMode.Queue -> RepeatMode.Track
            RepeatMode.Track -> RepeatMode.None
        }
        applyRepeatMode(nextMode)
    }

    fun applyRepeatMode(mode: RepeatMode) {
        if (repeatMode == mode) {
            return
        }
        repeatMode = mode
        updateExternalState()
    }

    fun startSleepTimer(minutes: Int) {
        if (minutes <= 0) {
            clearSleepTimer()
            return
        }
        startSleepTimerMs(minutes * 60_000L)
    }

    fun clearSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        sleepTimerRemainingMs = 0L
    }

    fun playNextFromUser(): Boolean {
        cancelPendingAutoNext()
        captureSkipRollbackState()?.let { pendingSkipRollback = it }
        val skippedTrackId = currentTrackId
        val nextIndex = resolveOrderedNextIndex() 
        val advanced = nextIndex?.let { playAt(it) } ?: false
        if (!advanced) {
            pendingSkipRollback = null
            clearPendingSkippedTrack()
        } else {
            stagePendingSkippedTrack(skippedTrackId, pendingTrackId ?: currentTrackId)
        }
        return advanced
    }

    fun playPreviousFromUser(): Boolean {
        cancelPendingAutoNext()
        captureSkipRollbackState()?.let { pendingSkipRollback = it }
        val skippedTrackId = currentTrackId
        val previousIndex = resolveOrderedPreviousIndex()
        val rewound = when {
            previousIndex != null -> playAt(previousIndex)
            else -> restartCurrentTrack()
        }
        if (!rewound) {
            pendingSkipRollback = null
            clearPendingSkippedTrack()
        } else {
            stagePendingSkippedTrack(skippedTrackId, pendingTrackId ?: currentTrackId)
        }
        return rewound
    }

    fun handleExternalAction(action: String) {
        when (action) {
            ACTION_PLAY_PAUSE -> togglePlayPause()
            ACTION_NEXT -> playNextFromUser()
            ACTION_PREVIOUS -> playPreviousFromUser()
            ACTION_STOP -> stop()
        }
    }

    fun stop() {
        lastPreparedTrackState = null
        pendingSkipRollback = null
        clearPendingTrackTarget()
        clearPendingSkippedTrack()
        cancelPendingAutoNext()
        stopPlayer()
        queue = emptyList()
        queueCount = 0
        currentIndex = -1
        currentTrackId = null
        playbackOrder = emptyList()
        playbackOrderPosition = -1
        isPlaying = false
        clearSleepTimer()
        updateExternalState()
    }

    fun release() {
        stop()
        scope.cancel()
        mediaSession.isActive = false
        mediaSession.release()
        PlaybackRuntime.detach(this)
    }

    private fun playNextInternal(automatic: Boolean): Boolean {
        val nextIndex = resolveNextIndex(automatic) ?: run {
            if (automatic) {
                stopAtQueueEnd()
            }
            return false
        }
        return playAt(nextIndex)
    }

    private fun resolveNextIndex(automatic: Boolean): Int? {
        if (queue.isEmpty()) {
            return null
        }

        if (automatic && repeatMode == RepeatMode.Track) {
            return currentIndex.takeIf { it in queue.indices } ?: currentOrderIndex()
        }
        return resolveOrderedNextIndex()
    }

    private fun predictedNextIndex(): Int? {
        if (queue.isEmpty()) {
            return null
        }

        val currentPosition = currentOrderPosition()
        if (currentPosition < 0) {
            return queue.indices.firstOrNull()
        }
        val nextPosition = currentPosition + 1
        if (nextPosition in playbackOrder.indices) {
            return playbackOrder[nextPosition]
        }
        return if (repeatMode == RepeatMode.Queue) playbackOrder.firstOrNull() else null
    }

    private fun startSleepTimerMs(durationMs: Long) {
        sleepTimerJob?.cancel()
        val endAt = SystemClock.elapsedRealtime() + durationMs
        sleepTimerJob = scope.launch {
            while (isActive) {
                val remaining = (endAt - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
                sleepTimerRemainingMs = remaining
                if (remaining == 0L) {
                    stop()
                    break
                }
                delay(1000L)
            }
        }
    }

    private fun restartCurrentTrack(): Boolean {
        if (queue.isEmpty()) {
            return false
        }

        cancelPendingAutoNext()

        if (currentIndex !in queue.indices) {
            currentIndex = 0
            currentTrackId = queue.firstOrNull()?.id
        }

        if (exoPlayer != null || mediaPlayer != null) {
            seekTo(0L)
            if (playerPrepared) {
                exoPlayer?.play()
                mediaPlayer?.start()
                isPlaying = true
                updateExternalState()
            }
            return true
        }

        return playAt(currentIndex)
    }

    private fun playAt(
        index: Int,
        shouldAutoPlay: Boolean = true,
        initialSeekPositionMs: Long = -1L
    ): Boolean {
        val track = queue.getOrNull(index) ?: return false

        cancelPendingAutoNext()
        if (!isPreparing && playerPrepared) {
            snapshotCurrentTrackState()?.let { lastPreparedTrackState = it }
        }
        clearPendingSeekHold()
        if (initialSeekPositionMs >= 0L) {
            pendingSeekPositionMs = initialSeekPositionMs
            pendingSeekSetAtMs = SystemClock.elapsedRealtime()
        }
        playRequestToken += 1L
        val requestToken = playRequestToken
        stopPlayer()
        pendingTrackIndex = index
        pendingTrackId = track.id
        if (index in queue.indices) {
            if (playbackOrder.isEmpty() || playbackOrder.none { it in queue.indices }) {
                rebuildPlaybackOrder(anchorIndex = index)
            }
            val orderPosition = playbackOrder.indexOf(index)
            if (orderPosition >= 0) {
                playbackOrderPosition = orderPosition
            } else {
                rebuildPlaybackOrder(anchorIndex = index)
            }
        }
        playerPrepared = false
        pendingPlayWhenPrepared = shouldAutoPlay
        isPreparing = true
        if (!shouldAutoPlay) {
            isPlaying = false
        }
        updateExternalState()

        val source = track.filePath.trim()
        val isRemoteSource = source.startsWith("http://", ignoreCase = true) ||
            source.startsWith("https://", ignoreCase = true)
        val sourceUri = if (isRemoteSource) {
            Uri.parse(source)
        } else {
            Uri.fromFile(File(source))
        }
        val exoPlaybackPlayer = try {
            if (isRemoteSource) {
                val httpFactory = DefaultHttpDataSource.Factory()
                    .setUserAgent(REMOTE_STREAM_USER_AGENT)
                    .setAllowCrossProtocolRedirects(true)
                    .setConnectTimeoutMs(40_000)
                    .setReadTimeoutMs(600_000)
                ExoPlayer.Builder(appContext)
                    .setMediaSourceFactory(DefaultMediaSourceFactory(httpFactory))
                    .build()
            } else {
                ExoPlayer.Builder(appContext).build()
            }
        } catch (_: Exception) {
            null
        }

        if (exoPlaybackPlayer == null) {
            if (restorePendingSkipRollback()) {
                return true
            }
            clearPendingTrackTarget()
            clearPendingSkippedTrack()
            playerPrepared = false
            pendingPlayWhenPrepared = false
            isPreparing = false
            isPlaying = false
            updateExternalState()
            return false
        }

        var readyHandled = false
        exoPlaybackPlayer.setAudioAttributes(
            Media3AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true
        )
        exoPlaybackPlayer.playWhenReady = pendingPlayWhenPrepared
        exoPlaybackPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playRequestToken != requestToken || exoPlayer !== exoPlaybackPlayer) {
                        runCatching { exoPlaybackPlayer.release() }
                        return
                    }
                    when (playbackState) {
                        Player.STATE_READY -> {
                            if (!readyHandled) {
                                readyHandled = true
                                commitPreparedTrack(index, track.id)
                                playerPrepared = true
                                isPreparing = false
                                val pendingSeek = pendingSeekPositionMs
                                if (pendingSeek >= 0L && exoPlaybackPlayer.duration > 0L) {
                                    exoPlaybackPlayer.seekTo(pendingSeek.coerceIn(0L, exoPlaybackPlayer.duration))
                                }
                                dispatchPendingSkippedTrack(currentTrackId)
                                if (pendingPlayWhenPrepared) {
                                    exoPlaybackPlayer.playWhenReady = true
                                    this@PlaybackController.isPlaying = true
                                    onTrackPlayed(track.id)
                                } else {
                                    this@PlaybackController.isPlaying = false
                                }
                                snapshotCurrentTrackState(
                                    positionMs = pendingSeek.takeIf { it >= 0L } ?: 0L,
                                    shouldResumePlaying = pendingPlayWhenPrepared
                                )?.let { lastPreparedTrackState = it }
                                pendingSkipRollback = null
                            }
                            updateExternalState()
                        }
                        Player.STATE_ENDED -> {
                            handleTrackCompleted()
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                    if (playRequestToken == requestToken && exoPlayer === exoPlaybackPlayer) {
                        if (isPreparing && pendingPlayWhenPrepared && !isPlayingNow) {
                            updateExternalState()
                            return
                        }
                        this@PlaybackController.isPlaying = isPlayingNow
                        updateExternalState()
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    if (playRequestToken == requestToken && exoPlayer === exoPlaybackPlayer) {
                        if (restorePendingSkipRollback()) {
                            return
                        }
                        val failedTrackId = pendingTrackId ?: currentTrackId
                        clearPendingTrackTarget()
                        clearPendingSkippedTrack()
                        playerPrepared = false
                        pendingPlayWhenPrepared = false
                        isPreparing = false
                        stopPlayer()
                        this@PlaybackController.isPlaying = false
                        updateExternalState()
                        if (!failedTrackId.isNullOrBlank()) {
                            onTrackPlaybackFailed(failedTrackId)
                        }
                    }
                }
            })
        exoPlayer = exoPlaybackPlayer
        exoPlaybackPlayer.setMediaItem(MediaItem.fromUri(sourceUri))
        exoPlaybackPlayer.prepare()
        return true
    }

    private fun stopPlayer() {
        cancelPendingAutoNext()
        clearPendingSeekHold()
        playerPrepared = false
        pendingPlayWhenPrepared = false
        isPreparing = false
        exoPlayer?.release()
        exoPlayer = null
        mediaPlayer?.setOnPreparedListener(null)
        mediaPlayer?.setOnErrorListener(null)
        mediaPlayer?.setOnCompletionListener(null)
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun handleTrackCompleted() {
        pendingSkipRollback = null
        cancelPendingAutoNext()
        if (trackGapDelayMs <= 0L) {
            if (!playNextInternal(automatic = true)) {
                stopAtQueueEnd()
            }
            return
        }

        isPlaying = false
        updateExternalState()

        pendingAutoNextJob = scope.launch {
            delay(trackGapDelayMs)
            if (!playNextInternal(automatic = true)) {
                stopAtQueueEnd()
            }
        }
    }

    private fun stopAtQueueEnd() {
        lastPreparedTrackState = null
        pendingSkipRollback = null
        clearPendingTrackTarget()
        clearPendingSkippedTrack()
        cancelPendingAutoNext()
        stopPlayer()
        isPlaying = false
        clearSleepTimer()
        if (queue.isEmpty()) {
            currentIndex = -1
            currentTrackId = null
        } else {
            currentIndex = currentIndex.coerceIn(0, queue.lastIndex)
            currentTrackId = queue.getOrNull(currentIndex)?.id
        }
        updateExternalState()
    }

    private fun cancelPendingAutoNext() {
        pendingAutoNextJob?.cancel()
        pendingAutoNextJob = null
    }

    private fun captureSkipRollbackState(): SkipRollbackState? {
        if (!isPreparing && playerPrepared) {
            snapshotCurrentTrackState()?.let { return it }
        }
        lastPreparedTrackState?.let { stable ->
            if (stable.index in queue.indices && queue.getOrNull(stable.index)?.id == stable.trackId) {
                return stable
            }
        }
        return null
    }

    private fun snapshotCurrentTrackState(
        positionMs: Long = currentPositionMs(),
        shouldResumePlaying: Boolean = isPlaying || pendingPlayWhenPrepared
    ): SkipRollbackState? {
        val trackId = currentTrackId ?: return null
        if (currentIndex !in queue.indices) {
            return null
        }
        val queueSnapshot = queue.toList()
        if (queueSnapshot.getOrNull(currentIndex)?.id != trackId) {
            return null
        }
        return SkipRollbackState(
            queue = queueSnapshot,
            index = currentIndex,
            trackId = trackId,
            positionMs = positionMs.coerceAtLeast(0L),
            shouldResumePlaying = shouldResumePlaying,
            playbackOrder = playbackOrder.toList(),
            playbackOrderPosition = playbackOrderPosition
        )
    }

    private fun activeAnchorIndex(): Int {
        if (pendingTrackIndex in queue.indices) {
            return pendingTrackIndex
        }
        if (currentIndex in queue.indices) {
            return currentIndex
        }
        val stable = lastPreparedTrackState
        if (stable != null && stable.index in queue.indices && queue.getOrNull(stable.index)?.id == stable.trackId) {
            return stable.index
        }
        return -1
    }

    private fun rebuildPlaybackOrder(anchorIndex: Int = activeAnchorIndex()) {
        if (queue.isEmpty()) {
            playbackOrder = emptyList()
            playbackOrderPosition = -1
            return
        }

        val boundedAnchor = anchorIndex.takeIf { it in queue.indices } ?: 0
        playbackOrder = if (isShuffleEnabled && queue.size > 1) {
            buildList(queue.size) {
                add(boundedAnchor)
                addAll(queue.indices.filter { it != boundedAnchor }.shuffled(random))
            }
        } else {
            queue.indices.toList()
        }
        playbackOrderPosition = playbackOrder.indexOf(boundedAnchor).coerceAtLeast(0)
    }

    private fun currentOrderPosition(): Int {
        val anchorIndex = activeAnchorIndex()
        if (anchorIndex !in queue.indices) {
            return -1
        }
        if (playbackOrder.isEmpty() || playbackOrder.none { it in queue.indices }) {
            rebuildPlaybackOrder(anchorIndex)
        }
        val currentPosition = playbackOrder.indexOf(anchorIndex)
        if (currentPosition >= 0) {
            playbackOrderPosition = currentPosition
            return currentPosition
        }
        rebuildPlaybackOrder(anchorIndex)
        return playbackOrder.indexOf(anchorIndex).also { position ->
            playbackOrderPosition = position
        }
    }

    private fun currentOrderIndex(): Int {
        val position = currentOrderPosition()
        return if (position in playbackOrder.indices) playbackOrder[position] else -1
    }

    private fun resolveOrderedNextIndex(): Int? {
        if (queue.isEmpty()) {
            return null
        }

        val currentPosition = currentOrderPosition()
        if (currentPosition < 0) {
            return queue.indices.firstOrNull()
        }

        val nextPosition = currentPosition + 1
        if (nextPosition in playbackOrder.indices) {
            return playbackOrder[nextPosition]
        }
        return if (repeatMode == RepeatMode.Queue) playbackOrder.firstOrNull() else null
    }

    private fun resolveOrderedPreviousIndex(): Int? {
        if (queue.isEmpty()) {
            return null
        }

        val currentPosition = currentOrderPosition()
        if (currentPosition < 0) {
            return null
        }

        val previousPosition = currentPosition - 1
        if (previousPosition in playbackOrder.indices) {
            return playbackOrder[previousPosition]
        }
        return if (repeatMode == RepeatMode.Queue) playbackOrder.lastOrNull() else null
    }

    private fun restorePendingSkipRollback(): Boolean {
        val rollback = pendingSkipRollback ?: return false
        pendingSkipRollback = null
        clearPendingTrackTarget()
        clearPendingSkippedTrack()
        if (rollback.index !in rollback.queue.indices || rollback.queue[rollback.index].id != rollback.trackId) {
            return false
        }
        queue = rollback.queue
        queueCount = rollback.queue.size
        playbackOrder = rollback.playbackOrder.filter { it in rollback.queue.indices }
        playbackOrderPosition = rollback.playbackOrderPosition
        if (playbackOrder.isEmpty()) {
            rebuildPlaybackOrder(anchorIndex = rollback.index)
        }
        return playAt(
            index = rollback.index,
            shouldAutoPlay = rollback.shouldResumePlaying,
            initialSeekPositionMs = rollback.positionMs
        )
    }

    private fun isSameQueue(other: List<TrackItem>): Boolean {
        if (queue.size != other.size) {
            return false
        }
        for (index in queue.indices) {
            if (queue[index].id != other[index].id) {
                return false
            }
        }
        return true
    }

    private fun clearPendingTrackTarget() {
        pendingTrackIndex = -1
        pendingTrackId = null
    }

    private fun clearPendingSkippedTrack() {
        pendingSkippedTrackId = null
    }

    private fun stagePendingSkippedTrack(skippedTrackId: String?, targetTrackId: String?) {
        pendingSkippedTrackId = skippedTrackId?.takeIf { it.isNotBlank() && it != targetTrackId }
    }

    private fun dispatchPendingSkippedTrack(activeTrackId: String?) {
        val skippedTrackId = pendingSkippedTrackId
            ?.takeIf { it.isNotBlank() && it != activeTrackId }
            ?: run {
                pendingSkippedTrackId = null
                return
            }
        pendingSkippedTrackId = null
        onTrackSkipped(skippedTrackId)
    }

    private fun commitPreparedTrack(index: Int, trackId: String) {
        currentIndex = index
        currentTrackId = trackId
        clearPendingTrackTarget()
    }

    private fun updateExternalState() {
        updateMediaSession()
        updateNotification()
        PlaybackForegroundService.sync(appContext)
    }

    internal fun shouldRunInForegroundService(): Boolean {
        return currentTrack != null
    }

    internal fun notificationForForegroundService(): Notification? {
        val track = currentTrack ?: return null
        return buildPlaybackNotification(track)
    }

    private fun updateMediaSession() {
        val track = displayTrack ?: currentTrack
        if (track == null) {
            val state = PlaybackState.Builder()
                .setActions(
                    PlaybackState.ACTION_PLAY or
                        PlaybackState.ACTION_PLAY_PAUSE or
                        PlaybackState.ACTION_STOP
                )
                .setState(PlaybackState.STATE_STOPPED, 0L, 0f, SystemClock.elapsedRealtime())
                .build()
            mediaSession.setPlaybackState(state)
            mediaSession.setMetadata(null)
            mediaSession.isActive = false
            return
        }

        var actions = PlaybackState.ACTION_PLAY_PAUSE or
            PlaybackState.ACTION_PLAY or
            PlaybackState.ACTION_PAUSE or
            PlaybackState.ACTION_STOP or
            PlaybackState.ACTION_SEEK_TO
        if (canStep) {
            actions = actions or PlaybackState.ACTION_SKIP_TO_PREVIOUS or PlaybackState.ACTION_SKIP_TO_NEXT
        }

        val state = PlaybackState.Builder()
            .setActions(actions)
            .setState(
                if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED,
                currentPositionMs(),
                if (isPlaying) 1f else 0f,
                SystemClock.elapsedRealtime()
            )
            .build()

        val metadata = MediaMetadata.Builder().apply {
            putString(MediaMetadata.METADATA_KEY_TITLE, displayTitle(track))
            putString(MediaMetadata.METADATA_KEY_ARTIST, track.artist)
            putLong(MediaMetadata.METADATA_KEY_DURATION, durationMs())
            resolvedExternalArtworkBitmap(track)?.let { putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, it) }
        }.build()
        requestArtworkLoad(track)

        mediaSession.isActive = true
        mediaSession.setPlaybackState(state)
        mediaSession.setMetadata(metadata)
    }

    private fun updateNotification() {
        val track = displayTrack ?: currentTrack
        if (track == null) {
            notificationManager.cancel(NOTIFICATION_ID)
            return
        }

        val notification = buildPlaybackNotification(track)
        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Notification permission can be denied by user on Android 13+.
        }
    }

    private fun buildPlaybackNotification(track: TrackItem): Notification {
        val contentIntent = PendingIntent.getActivity(
            appContext,
            7002,
            Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val previousAction = Notification.Action.Builder(
            android.R.drawable.ic_media_previous,
            "Previous",
            actionPendingIntent(ACTION_PREVIOUS)
        ).build()
        val playPauseAction = Notification.Action.Builder(
            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
            if (isPlaying) "Pause" else "Play",
            actionPendingIntent(ACTION_PLAY_PAUSE)
        ).build()
        val nextAction = Notification.Action.Builder(
            android.R.drawable.ic_media_next,
            "Next",
            actionPendingIntent(ACTION_NEXT)
        ).build()

        return Notification.Builder(appContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.tab_note)
            .setContentTitle(displayTitle(track))
            .setContentText(track.artist.ifBlank { "Unknown Artist" })
            .setContentIntent(contentIntent)
            .setDeleteIntent(actionPendingIntent(ACTION_STOP))
            .setLargeIcon(resolvedExternalArtworkBitmap(track))
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setOngoing(isPlaying)
            .addAction(previousAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
    }

    private fun actionPendingIntent(action: String): PendingIntent {
        val intent = Intent(appContext, PlaybackActionReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            appContext,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun cachedArtworkBitmap(track: TrackItem): Bitmap? {
        return artworkCache[track.id]
    }

    private fun externalArtworkBitmap(track: TrackItem): Bitmap? {
        return squareArtworkBitmap(cachedArtworkBitmap(track))
    }

    private fun resolvedExternalArtworkBitmap(track: TrackItem): Bitmap? {
        externalArtworkBitmap(track)?.let { return it }
        val activeTrack = currentTrack
        if (activeTrack != null && activeTrack.id != track.id) {
            externalArtworkBitmap(activeTrack)?.let { return it }
        }
        val stableTrackId = lastPreparedTrackState?.trackId
        if (!stableTrackId.isNullOrBlank() && stableTrackId != track.id) {
            queue.firstOrNull { it.id == stableTrackId }?.let { stableTrack ->
                externalArtworkBitmap(stableTrack)?.let { return it }
            }
            lastPreparedTrackState?.queue?.firstOrNull { it.id == stableTrackId }?.let { stableTrack ->
                externalArtworkBitmap(stableTrack)?.let { return it }
            }
        }
        return null
    }

    private fun requestArtworkLoad(track: TrackItem) {
        if (artworkCache.containsKey(track.id) || !artworkLoadingTrackIds.add(track.id)) {
            return
        }
        scope.launch(Dispatchers.IO) {
            val resolved = resolveArtworkBitmap(track)
            launch(Dispatchers.Main.immediate) {
                artworkLoadingTrackIds.remove(track.id)
                artworkCache[track.id] = resolved
                if (currentTrackId == track.id) {
                    updateExternalState()
                }
            }
        }
    }

    private fun resolveArtworkBitmap(track: TrackItem): Bitmap? {
        val fileBitmap = track.artworkPath?.takeIf { it.isNotBlank() }?.let { path ->
            if (path.startsWith("http://", ignoreCase = true) ||
                path.startsWith("https://", ignoreCase = true)
            ) {
                decodeRemoteArtwork(path)
            } else {
                val artworkFile = File(path)
                if (artworkFile.exists()) {
                    BitmapFactory.decodeFile(path)
                } else {
                    null
                }
            }
        }

        return fileBitmap ?: decodeEmbeddedArtwork(track.filePath)
    }

    private fun decodeRemoteArtwork(url: String): Bitmap? {
        return runCatching {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = true
                connectTimeout = 12_000
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
            if (bytes.isEmpty()) {
                null
            } else {
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }.getOrNull()
    }

    private fun decodeEmbeddedArtwork(filePath: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            val bytes = retriever.embeddedPicture ?: return null
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
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

    private fun squareArtworkBitmap(bitmap: Bitmap?): Bitmap? {
        bitmap ?: return null
        val side = minOf(bitmap.width, bitmap.height)
        if (side <= 1) {
            return bitmap
        }
        if (bitmap.width == side && bitmap.height == side) {
            return bitmap
        }
        val offsetX = (bitmap.width - side) / 2
        val offsetY = (bitmap.height - side) / 2
        return runCatching {
            Bitmap.createBitmap(bitmap, offsetX, offsetY, side, side)
        }.getOrElse { bitmap }
    }

    private fun displayTitle(track: TrackItem): String {
        return track.title.ifBlank { "Unknown Track" }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Music playback controls"
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "ru.hippo.Sonora.action.PLAY_PAUSE"
        const val ACTION_NEXT = "ru.hippo.Sonora.action.NEXT"
        const val ACTION_PREVIOUS = "ru.hippo.Sonora.action.PREVIOUS"
        const val ACTION_STOP = "ru.hippo.Sonora.action.STOP"

        private const val NOTIFICATION_CHANNEL_ID = "sonora_playback"
        private const val REMOTE_STREAM_USER_AGENT = "Sonora-iOS/1.0"
        internal const val NOTIFICATION_ID = 2047
    }
}
