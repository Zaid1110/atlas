package com.atlas.agent.frame

object FrameBuffer {
    @Volatile
    private var latestFrame: Frame? = null

    fun update(frame: Frame) {
        latestFrame = frame
    }

    fun latest(): Frame? {
        return latestFrame
    }

    fun clear() {
        latestFrame = null
    }
}
