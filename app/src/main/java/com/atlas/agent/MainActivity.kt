package com.atlas.agent

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.atlas.agent.camera.CameraCaptureManager
import com.atlas.agent.device.DeviceInfoProvider
import com.atlas.agent.files.LocalFileStore
import com.atlas.agent.frame.FrameDispatcher
import com.atlas.agent.microphone.MicrophoneCaptureManager
import com.atlas.agent.permission.PermissionItem
import com.atlas.agent.permission.PermissionKey
import com.atlas.agent.permission.PermissionManager
import com.atlas.agent.screen.ScreenCaptureManager
import com.atlas.agent.screen.ScreenCaptureState
import com.atlas.agent.service.AtlasForegroundService
import com.atlas.agent.service.LocalDemoServer
import com.atlas.agent.streaming.StreamSession
import com.atlas.agent.ui.dashboard.DashboardScreen
import com.atlas.agent.ui.preview.PreviewScreen
import com.atlas.agent.ui.theme.AtlasAgentTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val deviceInfo = DeviceInfoProvider.getDeviceInfo(this)

        setContent {
            AtlasAgentTheme {
                var isAgentRunning by remember { mutableStateOf(AtlasForegroundService.isRunning) }
                var screenCaptureState by remember { mutableStateOf(ScreenCaptureManager.state) }
                var latestFrameSizeBytes by remember { mutableStateOf(FrameDispatcher.stats().latestFrameSizeBytes) }
                var framesCaptured by remember { mutableStateOf(FrameDispatcher.stats().framesCaptured) }
                var currentFps by remember { mutableStateOf(FrameDispatcher.stats().currentFps) }
                var streamStatistics by remember { mutableStateOf(StreamSession.currentStatistics()) }
                var showPreview by remember { mutableStateOf(false) }
                var previewBitmap by remember { mutableStateOf(null as android.graphics.Bitmap?) }
                var touchEvent by remember { mutableStateOf(LocalDemoServer.latestTouchEvent()) }
                var keyboardText by remember { mutableStateOf(LocalDemoServer.latestKeyboardText()) }
                var clipboardText by remember { mutableStateOf("") }
                var microphoneRecording by remember { mutableStateOf(MicrophoneCaptureManager.isRecording()) }
                var fileEntries by remember { mutableStateOf(LocalFileStore.listDownloads(this@MainActivity)) }
                var streamUrl by remember { mutableStateOf(LocalDemoServer.currentUrl()) }

                val runtimePermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { results ->
                    if (results.all { it.value }) {
                        startAtlasService()
                        isAgentRunning = true
                    }
                }

                val screenCaptureLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                        ScreenCaptureManager.saveProjection(
                            context = this@MainActivity,
                            resultCode = result.resultCode,
                            data = result.data!!
                        )
                        ScreenCaptureManager.startCapture(this@MainActivity)
                        screenCaptureState = ScreenCaptureManager.state
                        val stats = FrameDispatcher.stats()
                        latestFrameSizeBytes = stats.latestFrameSizeBytes
                        framesCaptured = stats.framesCaptured
                        currentFps = stats.currentFps
                    }
                }

                DisposableEffect(Unit) {
                    onDispose {
                        ScreenCaptureManager.stopCapture()
                    }
                }

                LaunchedEffect(Unit) {
                    StreamSession.statisticsFlow.collect { statistics ->
                        streamStatistics = statistics
                    }
                }

                LaunchedEffect(screenCaptureState) {
                    while (screenCaptureState == ScreenCaptureState.Capturing) {
                        val stats = FrameDispatcher.stats()
                        latestFrameSizeBytes = stats.latestFrameSizeBytes
                        framesCaptured = stats.framesCaptured
                        currentFps = stats.currentFps
                        delay(250)
                    }
                }

                LaunchedEffect(showPreview) {
                    while (showPreview) {
                        val latestBytes = ScreenCaptureManager.latestFrameBytes()
                        previewBitmap = latestBytes?.let {
                            BitmapFactory.decodeByteArray(it, 0, it.size)
                        }
                        delay(150)
                    }
                }

                fun refreshUiState() {
                    isAgentRunning = AtlasForegroundService.isRunning
                    screenCaptureState = ScreenCaptureManager.state
                    val stats = FrameDispatcher.stats()
                    latestFrameSizeBytes = stats.latestFrameSizeBytes
                    framesCaptured = stats.framesCaptured
                    currentFps = stats.currentFps
                    touchEvent = LocalDemoServer.latestTouchEvent()
                    keyboardText = LocalDemoServer.latestKeyboardText()
                    clipboardText = readClipboardText(this@MainActivity)
                    microphoneRecording = MicrophoneCaptureManager.isRecording()
                    fileEntries = LocalFileStore.listDownloads(this@MainActivity)
                    streamUrl = LocalDemoServer.currentUrl()
                }

                fun requestRequiredPermissions() {
                    val needed = mutableListOf<String>()
                    if (!hasPermission(Manifest.permission.CAMERA)) needed.add(Manifest.permission.CAMERA)
                    if (!hasPermission(Manifest.permission.RECORD_AUDIO)) needed.add(Manifest.permission.RECORD_AUDIO)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                        needed.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    if (needed.isNotEmpty()) {
                        runtimePermissionLauncher.launch(needed.toTypedArray())
                    } else {
                        startAtlasService()
                        isAgentRunning = true
                    }
                }

                fun handlePermissionAction(permission: PermissionItem) {
                    when (permission.key) {
                        PermissionKey.Camera -> runtimePermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                        PermissionKey.Microphone -> runtimePermissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                        PermissionKey.Notifications -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                runtimePermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                            }
                        }
                        PermissionKey.Accessibility -> {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                        PermissionKey.ScreenCapture -> {
                            screenCaptureLauncher.launch(ScreenCaptureManager.createPermissionIntent(this@MainActivity))
                        }
                        PermissionKey.BatteryOptimization -> {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${packageName}"))
                            startActivity(intent)
                        }
                        else -> {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${packageName}"))
                            startActivity(intent)
                        }
                    }
                    refreshUiState()
                }

                if (showPreview) {
                    PreviewScreen(bitmap = previewBitmap, onBack = { showPreview = false })
                } else {
                    DashboardScreen(
                        deviceInfo = deviceInfo,
                        isAgentRunning = isAgentRunning,
                        onAgentButtonClick = {
                            if (isAgentRunning) {
                                stopAtlasService()
                                isAgentRunning = false
                            } else {
                                requestRequiredPermissions()
                            }
                        },
                        screenCaptureState = screenCaptureState,
                        framesCaptured = framesCaptured,
                        currentFps = currentFps,
                        latestFrameSizeBytes = latestFrameSizeBytes,
                        streamStatistics = streamStatistics,
                        streamUrl = streamUrl,
                        serverRunning = LocalDemoServer.isRunning,
                        touchEvent = touchEvent,
                        keyboardText = keyboardText,
                        clipboardText = clipboardText,
                        microphoneRecording = microphoneRecording,
                        permissionItems = PermissionManager.getPermissions(this@MainActivity),
                        fileEntries = fileEntries,
                        onPreviewButtonClick = { showPreview = true },
                        onScreenCaptureButtonClick = {
                            when (screenCaptureState) {
                                ScreenCaptureState.NotGranted -> {
                                    screenCaptureLauncher.launch(ScreenCaptureManager.createPermissionIntent(this@MainActivity))
                                }
                                ScreenCaptureState.Ready -> {
                                    ScreenCaptureManager.startCapture(this@MainActivity)
                                    refreshUiState()
                                }
                                ScreenCaptureState.Capturing -> {
                                    ScreenCaptureManager.stopCapture()
                                    refreshUiState()
                                }
                            }
                        },
                        onCopyClipboardClick = {
                            writeClipboardText(this@MainActivity, "Atlas demo clipboard ${System.currentTimeMillis()}")
                            clipboardText = readClipboardText(this@MainActivity)
                        },
                        onPasteClipboardClick = {
                            clipboardText = readClipboardText(this@MainActivity)
                        },
                        onCapturePhotoClick = {
                            val photo = CameraCaptureManager.capturePhoto(this@MainActivity)
                            if (photo != null) {
                                fileEntries = LocalFileStore.listDownloads(this@MainActivity)
                            }
                        },
                        onMicToggleClick = {
                            if (MicrophoneCaptureManager.isRecording()) {
                                MicrophoneCaptureManager.stopCapture()
                            } else {
                                MicrophoneCaptureManager.startCapture()
                            }
                            microphoneRecording = MicrophoneCaptureManager.isRecording()
                        },
                        onDownloadFileClick = {
                            LocalFileStore.downloadFile(this@MainActivity)
                            fileEntries = LocalFileStore.listDownloads(this@MainActivity)
                        },
                        onUploadFileClick = {
                            LocalFileStore.uploadFile(this@MainActivity)
                            fileEntries = LocalFileStore.listDownloads(this@MainActivity)
                        },
                        onPermissionAction = ::handlePermissionAction
                    )
                }
            }
        }
    }

    private fun startAtlasService() {
        val intent = Intent(this, AtlasForegroundService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopAtlasService() {
        val intent = Intent(this, AtlasForegroundService::class.java)
        stopService(intent)
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun readClipboardText(context: Context): String {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        return manager?.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
    }

    private fun writeClipboardText(context: Context, value: String) {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("atlas", value)
        manager?.setPrimaryClip(clip)
    }
}
