package com.atlas.agent.camera

import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object CameraCaptureManager {
    private const val TAG = "CameraCaptureManager"
    private const val CAPTURE_TIMEOUT_SECONDS = 5L

    private var latestPhotoBytes: ByteArray? = null
    private var lastCameraId: String? = null

    fun latestPhotoBytes(): ByteArray? = latestPhotoBytes?.copyOf()

    fun lastCameraId(): String? = lastCameraId

    fun enumerateCameras(context: Context): List<String> {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return emptyList()
        return manager.cameraIdList.filter { cameraId ->
            val chars = manager.getCameraCharacteristics(cameraId)
            chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT ||
                chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        }
    }

    @Suppress("DEPRECATION")
    fun capturePhoto(context: Context): ByteArray? {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return null
        val cameraId = selectCamera(manager) ?: return null
        lastCameraId = cameraId

        val latch = CountDownLatch(1)
        var capturedBytes: ByteArray? = null
        val handlerThread = HandlerThread("AtlasCameraThread").also { it.start() }
        val handler = Handler(handlerThread.looper)

        val reader = ImageReader.newInstance(1280, 720, android.graphics.ImageFormat.JPEG, 2)
        reader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
            image.use { img ->
                val buffer = img.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                capturedBytes = bytes
                latch.countDown()
            }
        }, handler)

        val stateCallback = object : CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) {
                val sessionCallback = object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        val requestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                            addTarget(reader.surface)
                            set(CaptureRequest.JPEG_ORIENTATION, 90)
                        }
                        session.capture(requestBuilder.build(), object : CameraCaptureSession.CaptureCallback() {}, handler)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        latch.countDown()
                    }
                }
                device.createCaptureSession(listOf(reader.surface), sessionCallback, handler)
            }

            override fun onDisconnected(device: CameraDevice) {
                latch.countDown()
            }

            override fun onError(device: CameraDevice, error: Int) {
                latch.countDown()
            }
        }

        manager.openCamera(cameraId, stateCallback, handler)
        latch.await(CAPTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        reader.close()
        handlerThread.quitSafely()

        latestPhotoBytes = capturedBytes
        return capturedBytes
    }

    private fun selectCamera(manager: CameraManager): String? {
        return manager.cameraIdList.firstOrNull { cameraId ->
            val chars = manager.getCameraCharacteristics(cameraId)
            chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        } ?: manager.cameraIdList.firstOrNull()
    }
}
