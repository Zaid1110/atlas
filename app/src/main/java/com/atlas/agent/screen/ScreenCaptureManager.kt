package com.atlas.agent.screen

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import androidx.core.content.getSystemService
import com.atlas.agent.frame.FrameDispatcher

object ScreenCaptureManager {
    private const val VIRTUAL_DISPLAY_NAME = "AtlasScreenCapture"

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var captureThread: HandlerThread? = null
    private var captureHandler: Handler? = null
    private val frameProcessor = FrameProcessor(targetFramesPerSecond = 10)

    @Volatile
    var state: ScreenCaptureState = ScreenCaptureState.NotGranted
        private set

    @Volatile
    var latestFrame: ByteArray? = null
        private set

    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            stopCapture()
            mediaProjection = null
            state = ScreenCaptureState.NotGranted
        }
    }

    fun createPermissionIntent(context: Context): Intent {
        val manager = context.getSystemService<MediaProjectionManager>()
            ?: error("MediaProjectionManager is unavailable")
        return manager.createScreenCaptureIntent()
    }

    fun saveProjection(context: Context, resultCode: Int, data: Intent) {
        releaseProjection()

        val manager = context.getSystemService<MediaProjectionManager>()
            ?: error("MediaProjectionManager is unavailable")
        val projection = manager.getMediaProjection(resultCode, data) ?: return
        projection.registerCallback(projectionCallback, null)
        mediaProjection = projection
        state = ScreenCaptureState.Ready
    }

    fun startCapture(context: Context) {
        val projection = mediaProjection ?: return
        if (state == ScreenCaptureState.Capturing) return

        val metrics = context.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val densityDpi = metrics.densityDpi

        val thread = HandlerThread("AtlasScreenCaptureThread").also { it.start() }
        val handler = Handler(thread.looper)
        captureThread = thread
        captureHandler = handler

        imageReader = ImageReader.newInstance(
            width,
            height,
            android.graphics.PixelFormat.RGBA_8888,
            2
        ).also { reader ->
            reader.setOnImageAvailableListener(
                { availableReader ->
                    val image = availableReader.acquireLatestImage() ?: return@setOnImageAvailableListener
                    image.use {
                        frameProcessor.process(it)?.let { jpeg ->
                            latestFrame = jpeg
                            FrameDispatcher.dispatch(jpeg)
                        }
                    }
                },
                handler
            )
        }

        val surface = imageReader?.surface ?: return

        virtualDisplay = projection.createVirtualDisplay(
            VIRTUAL_DISPLAY_NAME,
            width,
            height,
            densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surface,
            null,
            handler
        )

        state = ScreenCaptureState.Capturing
    }

    fun stopCapture() {
        virtualDisplay?.release()
        virtualDisplay = null

        imageReader?.close()
        imageReader = null

        frameProcessor.reset()

        captureThread?.quitSafely()
        captureThread = null
        captureHandler = null

        if (mediaProjection != null) {
            state = ScreenCaptureState.Ready
        }
    }

    fun releaseProjection() {
        stopCapture()
        mediaProjection?.unregisterCallback(projectionCallback)
        mediaProjection?.stop()
        mediaProjection = null
        latestFrame = null
        FrameDispatcher.reset()
        state = ScreenCaptureState.NotGranted
    }
}
