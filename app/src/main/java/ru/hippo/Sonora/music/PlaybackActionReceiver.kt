package ru.hippo.Sonora.music

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PlaybackActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        val controller = PlaybackRuntime.controller() ?: return
        controller.handleExternalAction(action)
    }
}
