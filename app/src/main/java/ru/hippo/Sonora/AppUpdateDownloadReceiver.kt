package ru.hippo.Sonora

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

private const val APP_UPDATE_NOTIFICATION_CHANNEL_ID = "sonora_app_updates"
private const val APP_UPDATE_NOTIFICATION_ID = 2042

class AppUpdateDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            return
        }
        val completedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (completedDownloadId <= 0L) {
            return
        }
        val snapshot = readAndroidAppUpdateDownloadSnapshot(context, completedDownloadId) ?: return
        if (snapshot.status != DownloadManager.STATUS_SUCCESSFUL) {
            return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                APP_UPDATE_NOTIFICATION_CHANNEL_ID,
                "App updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Sonora app update downloads"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, APP_UPDATE_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Sonora ${snapshot.release.versionName} downloaded")
            .setContentText("Open Sonora to continue installation.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(APP_UPDATE_NOTIFICATION_ID, notification)
    }
}
