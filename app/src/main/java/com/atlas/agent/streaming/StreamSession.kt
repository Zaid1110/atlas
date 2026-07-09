package com.atlas.agent.streaming

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.ArrayDeque

object StreamSession {
    private const val FPS_WINDOW_MILLIS = 1_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val frameTimestamps = ArrayDeque<Long>()

    private val _statistics = MutableStateFlow(StreamStatistics())
    val statisticsFlow: StateFlow<StreamStatistics> = _statistics.asStateFlow()

    private var isActive = false
    private var frameNumber = 0L

    fun start() {
        if (isActive) return

        isActive = true
        frameTimestamps.clear()
        frameNumber = 0L
        updateState(StreamState.Streaming)
    }

    fun stop() {
        if (!isActive) return

        isActive = false
        frameTimestamps.clear()
        updateState(StreamState.Idle)
    }

    fun currentStatistics(): StreamStatistics = _statistics.value

    fun acceptFrame(jpegFrame: ByteArray, width: Int, height: Int) {
        scope.launch {
            if (!isActive) {
                val current = _statistics.value
                _statistics.value = current.copy(
                    droppedFrames = current.droppedFrames + 1
                )
                return@launch
            }

            val timestampMillis = System.currentTimeMillis()
            frameNumber += 1
            val packet = FrameEncoder.encodeFrame(
                jpegFrame = jpegFrame,
                timestampMillis = timestampMillis,
                width = width,
                height = height,
                frameNumber = frameNumber
            )

            val current = _statistics.value
            val framesSent = current.framesSent + 1
            val averageSize = if (current.framesSent == 0L) {
                packet.metadata.frameSize.toLong()
            } else {
                ((current.averageFrameSizeBytes * current.framesSent) + packet.metadata.frameSize) / framesSent
            }

            frameTimestamps.addLast(timestampMillis)
            while (frameTimestamps.isNotEmpty() && timestampMillis - frameTimestamps.first() > FPS_WINDOW_MILLIS) {
                frameTimestamps.removeFirst()
            }

            val fps = frameTimestamps.size
            _statistics.value = current.copy(
                state = StreamState.Streaming,
                fps = fps,
                averageFrameSizeBytes = averageSize,
                framesSent = framesSent
            )
        }
    }

    private fun updateState(state: StreamState) {
        val current = _statistics.value
        _statistics.value = current.copy(state = state)
    }
}
