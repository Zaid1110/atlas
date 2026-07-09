package com.atlas.agent.screen

import android.media.Image

class FrameProcessor {
    fun process(image: Image): ByteArray {
        val bitmap = ImageConverter.imageToBitmap(image)
        val jpeg = ImageConverter.compressToJpeg(bitmap)
        bitmap.recycle()
        return jpeg
    }

    fun reset() {
    }
}
