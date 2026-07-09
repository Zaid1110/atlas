package com.atlas.agent

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import com.atlas.agent.device.DeviceInfoProvider
import com.atlas.agent.frame.FrameDispatcher
import com.atlas.agent.screen.ScreenCaptureManager
import com.atlas.agent.screen.ScreenCaptureState
import com.atlas.agent.service.AtlasForegroundService
import com.atlas.agent.ui.dashboard.DashboardScreen
import com.atlas.agent.ui.theme.AtlasAgentTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val deviceInfo = DeviceInfoProvider.getDeviceInfo(this)

        setContent {

            AtlasAgentTheme {

                var isAgentRunning by remember {
                    mutableStateOf(AtlasForegroundService.isRunning)
                }
                var screenCaptureState by remember {
                    mutableStateOf(ScreenCaptureManager.state)
                }
                var latestFrameSizeBytes by remember {
                    mutableStateOf(FrameDispatcher.stats().latestFrameSizeBytes)
                }
                var framesCaptured by remember {
                    mutableStateOf(FrameDispatcher.stats().framesCaptured)
                }
                var currentFps by remember {
                    mutableStateOf(FrameDispatcher.stats().currentFps)
                }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (granted) {
                        startAtlasService()
                        isAgentRunning = true
                    }
                }

                val screenCaptureLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                        ScreenCaptureManager.saveProjection(
                            context = this,
                            resultCode = result.resultCode,
                            data = result.data!!
                        )
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

                LaunchedEffect(screenCaptureState) {
                    while (screenCaptureState == ScreenCaptureState.Capturing) {
                        val stats = FrameDispatcher.stats()
                        latestFrameSizeBytes = stats.latestFrameSizeBytes
                        framesCaptured = stats.framesCaptured
                        currentFps = stats.currentFps
                        delay(250)
                    }
                }

                DashboardScreen(
                    deviceInfo = deviceInfo,
                    isAgentRunning = isAgentRunning,
                    onAgentButtonClick = {
                        if (isAgentRunning) {
                            stopAtlasService()
                            isAgentRunning = false
                        } else if (canPostNotifications()) {
                            startAtlasService()
                            isAgentRunning = true
                        } else {
                            notificationPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                    },
                    screenCaptureState = screenCaptureState,
                    framesCaptured = framesCaptured,
                    currentFps = currentFps,
                    latestFrameSizeBytes = latestFrameSizeBytes,
                    onScreenCaptureButtonClick = {
                        when (screenCaptureState) {
                            ScreenCaptureState.NotGranted -> {
                                screenCaptureLauncher.launch(
                                    ScreenCaptureManager.createPermissionIntent(this)
                                )
                            }
                            ScreenCaptureState.Ready -> {
                                ScreenCaptureManager.startCapture(this)
                                screenCaptureState = ScreenCaptureManager.state
                                val stats = FrameDispatcher.stats()
                                latestFrameSizeBytes = stats.latestFrameSizeBytes
                                framesCaptured = stats.framesCaptured
                                currentFps = stats.currentFps
                            }
                            ScreenCaptureState.Capturing -> {
                                ScreenCaptureManager.stopCapture()
                                screenCaptureState = ScreenCaptureManager.state
                                val stats = FrameDispatcher.stats()
                                latestFrameSizeBytes = stats.latestFrameSizeBytes
                                framesCaptured = stats.framesCaptured
                                currentFps = stats.currentFps
                            }
                        }
                    }
                )

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

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

}
