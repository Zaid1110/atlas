package com.atlas.agent.screen

import android.media.Image

class FrameProcessor(
    private val targetFramesPerSecond: Int = 10
) {
    private val frameIntervalMillis = 1_000L / targetFramesPerSecond
    private var lastFrameTimeMillis = 0L

    fun process(image: Image): ByteArray? {
        val now = System.currentTimeMillis()
        if (now - lastFrameTimeMillis < frameIntervalMillis) {
            return null
        }

        lastFrameTimeMillis = now

        val bitmap = ImageConverter.imageToBitmap(image)
        val scaledBitmap = ImageConverter.scaleBitmap(bitmap, scale = 0.5f)
        val jpeg = ImageConverter.compressToJpeg(scaledBitmap)
        scaledBitmap.recycle()
        return jpeg
    }

    fun reset() {
        lastFrameTimeMillis = 0L
    }
}
