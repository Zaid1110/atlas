package com.atlas.agent.streaming

object FrameEncoder {

    fun encodeFrame(
        jpegFrame: ByteArray,
        timestampMillis: Long,
        width: Int,
        height: Int,
        frameNumber: Long
    ): FramePacket {
        val metadata = FrameMetadata(
            timestamp = timestampMillis,
            width = width,
            height = height,
            frameSize = jpegFrame.size,
            frameNumber = frameNumber
        )

        return FramePacket(
            metadata = metadata,
            payload = jpegFrame.copyOf()
        )
    }
}
