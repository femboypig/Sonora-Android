package ru.hippo.Sonora.music

import java.lang.ref.WeakReference

object PlaybackRuntime {
    @Volatile
    private var controllerRef: WeakReference<PlaybackController>? = null

    fun attach(controller: PlaybackController) {
        controllerRef = WeakReference(controller)
    }

    fun detach(controller: PlaybackController) {
        val current = controllerRef?.get()
        if (current === controller) {
            controllerRef = null
        }
    }

    fun controller(): PlaybackController? {
        return controllerRef?.get()
    }
}
