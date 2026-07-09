package com.atlas.agent.streaming

data class FrameMetadata(
    val timestamp: Long,
    val width: Int,
    val height: Int,
    val frameSize: Int,
    val frameNumber: Long
)

data class FramePacket(
    val metadata: FrameMetadata,
    val payload: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FramePacket) return false

        return metadata == other.metadata && payload.contentEquals(other.payload)
    }

    override fun hashCode(): Int {
        var result = metadata.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}
