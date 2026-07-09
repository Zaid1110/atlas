package com.atlas.agent.screen

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import java.io.ByteArrayOutputStream

object ImageConverter {

    fun imageToBitmap(image: Image): Bitmap {
        val plane = image.planes.first()
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val paddedWidth = image.width + rowPadding / pixelStride
        val paddedBitmap = Bitmap.createBitmap(
            paddedWidth,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        paddedBitmap.copyPixelsFromBuffer(buffer)

        if (paddedWidth == image.width) {
            return paddedBitmap
        }

        val croppedBitmap = Bitmap.createBitmap(
            paddedBitmap,
            0,
            0,
            image.width,
            image.height
        )
        paddedBitmap.recycle()
        return croppedBitmap
    }

    fun compressToJpeg(bitmap: Bitmap, quality: Int = 70): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }

    fun scaleBitmap(bitmap: Bitmap, scale: Float): Bitmap {
        if (scale >= 1f) return bitmap

        val matrix = Matrix().apply {
            postScale(scale, scale)
        }

        val scaledBitmap = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
        bitmap.recycle()
        return scaledBitmap
    }
}
