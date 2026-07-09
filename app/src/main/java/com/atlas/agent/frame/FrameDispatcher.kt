package com.atlas.agent.frame

object FrameDispatcher {

    fun dispatch(jpegBytes: ByteArray): Frame {
        return FrameQueue.push(jpegBytes)
    }

    fun latestFrame(): Frame? {
        return FrameQueue.latest()
    }

    fun stats(): FrameStats {
        return FrameQueue.snapshot()
    }

    fun reset() {
        FrameQueue.clear()
    }
}
