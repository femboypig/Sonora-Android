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
import android.os.PowerManager
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
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
    private val onTrackSkipped: (String) -> Unit = {}
) {

    var currentTrackId: String? by mutableStateOf(null)
        private set

    var isPlaying: Boolean by mutableStateOf(false)
        private set

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

    private val appContext = context.applicationContext
    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val mediaSession = MediaSession(appContext, "SonoraPlayback")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val random = Random(SystemClock.elapsedRealtime())
    private val artworkCache = mutableMapOf<String, Bitmap?>()
    private val shuffleHistory = ArrayDeque<Int>()
    private val shuffleBag = ArrayDeque<Int>()

    private var mediaPlayer: MediaPlayer? = null
    private var queue: List<TrackItem> = emptyList()
    private var currentIndex: Int = -1
    private var sleepTimerJob: Job? = null

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
        val position = mediaPlayer?.currentPosition ?: 0
        return position.toLong().coerceAtLeast(0L)
    }

    fun durationMs(): Long {
        val playerDuration = mediaPlayer?.duration?.toLong() ?: 0L
        if (playerDuration > 0L) {
            return playerDuration
        }
        return currentTrack?.durationMs ?: 0L
    }

    fun seekTo(positionMs: Long) {
        val player = mediaPlayer ?: return
        val duration = player.duration
        if (duration <= 0) {
            return
        }
        val clamped = positionMs.coerceIn(0L, duration.toLong())
        player.seekTo(clamped.toInt())
        updateExternalState()
    }

    fun predictedNextTrackForSkip(): TrackItem? {
        val nextIndex = predictedNextIndex() ?: return null
        return queue.getOrNull(nextIndex)
    }

    fun playOrToggleFromQueue(queue: List<TrackItem>, targetTrackId: String) {
        if (queue.isEmpty() || targetTrackId.isBlank()) {
            stop()
            return
        }

        if (currentTrackId == targetTrackId && isSameQueue(queue)) {
            togglePlayPause()
            return
        }

        val index = queue.indexOfFirst { it.id == targetTrackId }
        if (index < 0) {
            return
        }

        val shouldCountSkip = isPlaying && currentTrackId != null && currentTrackId != targetTrackId
        if (shouldCountSkip) {
            recordSkipCurrentTrack()
        }

        this.queue = queue
        queueCount = queue.size
        resetShuffleState()
        playAt(index)
    }

    fun isQueueMatching(queue: List<TrackItem>): Boolean {
        return isSameQueue(queue)
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (isPlaying) {
            player.pause()
            isPlaying = false
        } else {
            player.start()
            isPlaying = true
        }
        updateExternalState()
    }

    fun toggleShuffleEnabled() {
        isShuffleEnabled = !isShuffleEnabled
        resetShuffleState()
        updateExternalState()
    }

    fun cycleRepeatMode() {
        repeatMode = when (repeatMode) {
            RepeatMode.None -> RepeatMode.Queue
            RepeatMode.Queue -> RepeatMode.Track
            RepeatMode.Track -> RepeatMode.None
        }
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
        recordSkipCurrentTrack()
        return playNextInternal(automatic = false)
    }

    fun playPreviousFromUser(): Boolean {
        recordSkipCurrentTrack()
        return playPreviousInternal()
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
        stopPlayer()
        queue = emptyList()
        queueCount = 0
        currentIndex = -1
        currentTrackId = null
        isPlaying = false
        clearSleepTimer()
        resetShuffleState()
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
            stop()
            return false
        }

        if (isShuffleEnabled && currentIndex >= 0 && nextIndex != currentIndex) {
            shuffleHistory.addLast(currentIndex)
        }

        return playAt(nextIndex)
    }

    private fun playPreviousInternal(): Boolean {
        if (queue.isEmpty()) {
            return false
        }

        if (isShuffleEnabled && shuffleHistory.isNotEmpty()) {
            val previous = shuffleHistory.removeLast()
            if (currentIndex >= 0 && currentIndex != previous) {
                shuffleBag.addFirst(currentIndex)
            }
            return playAt(previous)
        }

        val previousIndex = currentIndex - 1
        if (previousIndex >= 0) {
            return playAt(previousIndex)
        }

        if (repeatMode == RepeatMode.Queue && queue.isNotEmpty()) {
            return playAt(queue.lastIndex)
        }

        return false
    }

    private fun resolveNextIndex(automatic: Boolean): Int? {
        if (queue.isEmpty()) {
            return null
        }

        if (currentIndex < 0) {
            return 0
        }

        if (automatic && repeatMode == RepeatMode.Track) {
            return currentIndex
        }

        if (isShuffleEnabled) {
            return nextShuffleIndex()
        }

        val next = currentIndex + 1
        if (next <= queue.lastIndex) {
            return next
        }

        return if (repeatMode == RepeatMode.Queue) 0 else null
    }

    private fun predictedNextIndex(): Int? {
        if (queue.isEmpty()) {
            return null
        }

        if (currentIndex < 0) {
            return 0
        }

        if (isShuffleEnabled) {
            if (queue.size == 1) {
                return if (repeatMode == RepeatMode.None) null else currentIndex
            }
            if (shuffleBag.isNotEmpty()) {
                return shuffleBag.firstOrNull()
            }
            val fallback = queue.indices.firstOrNull { it != currentIndex }
            if (repeatMode == RepeatMode.Queue) {
                return fallback ?: currentIndex
            }
            return fallback
        }

        val next = currentIndex + 1
        if (next <= queue.lastIndex) {
            return next
        }
        return if (repeatMode == RepeatMode.Queue) 0 else null
    }

    private fun nextShuffleIndex(): Int? {
        if (queue.isEmpty()) {
            return null
        }

        if (queue.size == 1) {
            return if (repeatMode == RepeatMode.None) null else 0
        }

        if (shuffleBag.isEmpty()) {
            refillShuffleBag(excluding = currentIndex)
        }

        val next = shuffleBag.removeFirstOrNull()
        if (next != null) {
            return next
        }

        if (repeatMode == RepeatMode.Queue) {
            refillShuffleBag(excluding = currentIndex)
            return shuffleBag.removeFirstOrNull()
        }

        return null
    }

    private fun refillShuffleBag(excluding: Int) {
        val candidates = queue.indices.filter { it != excluding }.shuffled(random)
        shuffleBag.clear()
        shuffleBag.addAll(candidates)
    }

    private fun resetShuffleState() {
        shuffleHistory.clear()
        shuffleBag.clear()
        if (isShuffleEnabled && queue.size > 1) {
            refillShuffleBag(excluding = currentIndex)
        }
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

    private fun playAt(index: Int): Boolean {
        val track = queue.getOrNull(index) ?: return false

        stopPlayer()
        val player = try {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setWakeMode(appContext, PowerManager.PARTIAL_WAKE_LOCK)
                setDataSource(track.filePath)
                setOnCompletionListener {
                    if (!playNextInternal(automatic = true)) {
                        this@PlaybackController.stop()
                    }
                }
                prepare()
                start()
            }
        } catch (_: Exception) {
            null
        }

        if (player == null) {
            currentIndex = -1
            currentTrackId = null
            isPlaying = false
            updateExternalState()
            return false
        }

        mediaPlayer = player
        currentIndex = index
        currentTrackId = track.id
        isPlaying = true
        if (isShuffleEnabled && queue.size > 1) {
            refillShuffleBag(excluding = currentIndex)
        }
        onTrackPlayed(track.id)
        updateExternalState()
        return true
    }

    private fun stopPlayer() {
        mediaPlayer?.setOnCompletionListener(null)
        mediaPlayer?.release()
        mediaPlayer = null
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

    private fun recordSkipCurrentTrack() {
        val trackId = currentTrackId ?: return
        onTrackSkipped(trackId)
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
        val track = currentTrack
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
            loadArtworkBitmap(track)?.let { putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, it) }
        }.build()

        mediaSession.isActive = true
        mediaSession.setPlaybackState(state)
        mediaSession.setMetadata(metadata)
    }

    private fun updateNotification() {
        val track = currentTrack
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
            .setLargeIcon(loadArtworkBitmap(track))
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

    private fun loadArtworkBitmap(track: TrackItem): Bitmap? {
        artworkCache[track.id]?.let { return it }

        val fileBitmap = track.artworkPath?.takeIf { it.isNotBlank() }?.let { path ->
            val artworkFile = File(path)
            if (artworkFile.exists()) {
                BitmapFactory.decodeFile(path)
            } else {
                null
            }
        }

        val resolved = fileBitmap ?: decodeEmbeddedArtwork(track.filePath)
        artworkCache[track.id] = resolved
        return resolved
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
        internal const val NOTIFICATION_ID = 2047
    }
}
