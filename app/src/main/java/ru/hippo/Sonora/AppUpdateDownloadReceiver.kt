package ru.hippo.Sonora

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

private const val APP_UPDATE_NOTIFICATION_CHANNEL_ID = "sonora_app_updates"
private const val APP_UPDATE_NOTIFICATION_ID = 2042

internal fun cancelAndroidAppUpdateReadyNotification(context: Context) {
    NotificationManagerCompat.from(context).cancel(APP_UPDATE_NOTIFICATION_ID)
}

internal fun showAndroidAppUpdateReadyNotification(
    context: Context,
    release: AndroidAppUpdateRelease
): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        return false
    }

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            APP_UPDATE_NOTIFICATION_CHANNEL_ID,
            "App updates",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Sonora app update downloads"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val openIntent = Intent(context, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        putExtra("open_app_update_install", true)
    }
    val openPendingIntent = PendingIntent.getActivity(
        context,
        0,
        openIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, APP_UPDATE_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Sonora ${release.versionName} downloaded")
        .setContentText("Return to Sonora to continue installation.")
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_STATUS)
        .setContentIntent(openPendingIntent)
        .build()

    NotificationManagerCompat.from(context).notify(APP_UPDATE_NOTIFICATION_ID, notification)
    return true
}

internal fun showAndroidAppUpdateReadyNotification(
    context: Context,
    snapshot: AndroidAppUpdateDownloadSnapshot
): Boolean = showAndroidAppUpdateReadyNotification(context, snapshot.release)

class AppUpdateDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            return
        }
        val completedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (completedDownloadId <= 0L) {
            return
        }
        val storedRelease = readStoredAndroidAppUpdateRelease(context, completedDownloadId)
        if (storedRelease != null) {
            showAndroidAppUpdateReadyNotification(context, storedRelease)
            return
        }
        val snapshot = readAndroidAppUpdateDownloadSnapshot(context, completedDownloadId) ?: return
        if (snapshot.status == DownloadManager.STATUS_SUCCESSFUL) {
            showAndroidAppUpdateReadyNotification(context, snapshot)
        }
    }
}
