package com.atlas.agent.streaming

enum class StreamState {
    Idle,
    Streaming;

    val label: String
        get() = name
}

data class StreamStatistics(
    val state: StreamState = StreamState.Idle,
    val fps: Int = 0,
    val averageFrameSizeBytes: Long = 0L,
    val framesSent: Long = 0L,
    val droppedFrames: Long = 0L
)
