package com.atlas.agent.frame

import java.util.ArrayDeque

object FrameQueue {
    private const val MAX_QUEUE_SIZE = 30
    private const val FPS_WINDOW_MILLIS = 1_000L

    private val frames = ArrayDeque<Frame>()
    private val frameTimestamps = ArrayDeque<Long>()
    private var nextFrameId = 0L

    @Volatile
    var framesCaptured: Long = 0L
        private set

    @Synchronized
    fun push(jpegBytes: ByteArray): Frame {
        val now = System.currentTimeMillis()
        val frame = Frame(
            id = nextFrameId++,
            timestampMillis = now,
            jpegBytes = jpegBytes
        )

        frames.addLast(frame)
        while (frames.size > MAX_QUEUE_SIZE) {
            frames.removeFirst()
        }

        frameTimestamps.addLast(now)
        trimFrameWindow(now)

        framesCaptured += 1
        FrameBuffer.update(frame)

        return frame
    }

    @Synchronized
    fun latest(): Frame? {
        return FrameBuffer.latest()
    }

    @Synchronized
    fun currentFps(): Int {
        trimFrameWindow(System.currentTimeMillis())
        return frameTimestamps.size
    }

    @Synchronized
    fun clear() {
        frames.clear()
        frameTimestamps.clear()
        nextFrameId = 0L
        framesCaptured = 0L
        FrameBuffer.clear()
    }

    @Synchronized
    fun snapshot(): FrameStats {
        return FrameStats(
            framesCaptured = framesCaptured,
            currentFps = currentFps(),
            latestFrameSizeBytes = latest()?.sizeBytes
        )
    }

    private fun trimFrameWindow(now: Long) {
        while (
            frameTimestamps.isNotEmpty() &&
            now - frameTimestamps.first() > FPS_WINDOW_MILLIS
        ) {
            frameTimestamps.removeFirst()
        }
    }
}

data class FrameStats(
    val framesCaptured: Long,
    val currentFps: Int,
    val latestFrameSizeBytes: Int?
)
