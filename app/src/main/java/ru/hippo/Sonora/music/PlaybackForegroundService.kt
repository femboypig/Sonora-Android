package ru.hippo.Sonora.music

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat

class PlaybackForegroundService : Service() {

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        syncForegroundState()
        return START_NOT_STICKY
    }

    private fun syncForegroundState() {
        val controller = PlaybackRuntime.controller()
        val shouldRun = controller?.shouldRunInForegroundService() == true
        val notification = controller?.notificationForForegroundService()

        if (!shouldRun || notification == null) {
            stopSelfSafely()
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    PlaybackController.NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(PlaybackController.NOTIFICATION_ID, notification)
            }
            notificationManager.notify(PlaybackController.NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            stopSelfSafely()
        } catch (_: RuntimeException) {
            stopSelfSafely()
        }
    }

    private fun stopSelfSafely() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    companion object {
        fun sync(context: Context) {
            val appContext = context.applicationContext
            val shouldRun = PlaybackRuntime.controller()?.shouldRunInForegroundService() == true
            if (!shouldRun) {
                appContext.stopService(Intent(appContext, PlaybackForegroundService::class.java))
                return
            }

            val intent = Intent(appContext, PlaybackForegroundService::class.java)
            try {
                ContextCompat.startForegroundService(appContext, intent)
            } catch (_: RuntimeException) {
                appContext.startService(intent)
            }
        }
    }
}
