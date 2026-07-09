package com.atlas.agent.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.agent.device.DeviceInfo
import com.atlas.agent.files.LocalFileStore
import com.atlas.agent.permission.PermissionItem
import com.atlas.agent.permission.PermissionState
import com.atlas.agent.screen.ScreenCaptureState
import com.atlas.agent.streaming.StreamStatistics

@Composable
fun DashboardScreen(
    deviceInfo: DeviceInfo,
    isAgentRunning: Boolean,
    onAgentButtonClick: () -> Unit,
    screenCaptureState: ScreenCaptureState,
    framesCaptured: Long,
    currentFps: Int,
    latestFrameSizeBytes: Int?,
    streamStatistics: StreamStatistics,
    streamUrl: String,
    serverRunning: Boolean,
    touchEvent: String,
    keyboardText: String,
    clipboardText: String,
    microphoneRecording: Boolean,
    permissionItems: List<PermissionItem>,
    fileEntries: List<LocalFileStore.LocalFileEntry>,
    onPreviewButtonClick: () -> Unit,
    onScreenCaptureButtonClick: () -> Unit,
    onCopyClipboardClick: () -> Unit,
    onPasteClipboardClick: () -> Unit,
    onCapturePhotoClick: () -> Unit,
    onMicToggleClick: () -> Unit,
    onDownloadFileClick: () -> Unit,
    onUploadFileClick: () -> Unit,
    onPermissionAction: (PermissionItem) -> Unit
) {
    val scrollState = rememberScrollState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "ATLAS", fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Remote Agent Demo")
            Spacer(modifier = Modifier.height(24.dp))

            DashboardCard(title = "Service") {
                Text("State : ${if (isAgentRunning) "Running" else "Stopped"}")
                Spacer(modifier = Modifier.height(6.dp))
                Text("Server : ${if (serverRunning) "Online" else "Offline"}")
                Spacer(modifier = Modifier.height(6.dp))
                Text("Stream : $streamUrl")
            }

            Spacer(modifier = Modifier.height(12.dp))

            DashboardCard(title = "Device") {
                Text("Battery : ${deviceInfo.battery}%")
                Spacer(modifier = Modifier.height(6.dp))
                Text("Charging : ${if (deviceInfo.charging) "Yes" else "No"}")
                Spacer(modifier = Modifier.height(6.dp))
                Text("Phone : ${deviceInfo.manufacturer} ${deviceInfo.model}")
                Spacer(modifier = Modifier.height(6.dp))
                Text("Android : ${deviceInfo.androidVersion}")
                Spacer(modifier = Modifier.height(6.dp))
                Text("Wi-Fi : ${deviceInfo.wifiName}")
                Spacer(modifier = Modifier.height(6.dp))
                Text("RAM : ${deviceInfo.ram}")
                Spacer(modifier = Modifier.height(6.dp))
                Text("Storage : ${deviceInfo.storage}")
                Spacer(modifier = Modifier.height(6.dp))
                Text("CPU : ${deviceInfo.cpu}")
            }

            Spacer(modifier = Modifier.height(12.dp))

            DashboardCard(title = "Permissions") {
                permissionItems.forEach { permission ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(permission.title)
                        Text(
                            text = if (permission.state == PermissionState.Granted) "Granted" else "Pending",
                            color = if (permission.state == PermissionState.Granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            DashboardCard(title = "Screen Capture") {
                Text("Status : ${when (screenCaptureState) { ScreenCaptureState.NotGranted -> "Ready"; ScreenCaptureState.Ready -> "Ready"; ScreenCaptureState.Capturing -> "Capturing" }}")
                Spacer(modifier = Modifier.height(6.dp))
                Text("FPS : $currentFps")
                Spacer(modifier = Modifier.height(6.dp))
                Text("Frames : $framesCaptured")
                Spacer(modifier = Modifier.height(6.dp))
                Text("Last Frame : ${latestFrameSizeBytes?.let { it / 1024 } ?: 0} KB")
            }

            Spacer(modifier = Modifier.height(12.dp))

            DashboardCard(title = "Streaming") {
                Text("State : ${streamStatistics.state.label}")
                Spacer(modifier = Modifier.height(6.dp))
                Text("FPS : ${streamStatistics.fps}")
                Spacer(modifier = Modifier.height(6.dp))
                Text("Frames : ${streamStatistics.framesSent}")
                Spacer(modifier = Modifier.height(6.dp))
                Text("Avg Size : ${streamStatistics.averageFrameSizeBytes / 1024} KB")
                Spacer(modifier = Modifier.height(6.dp))
                Text("Touch : $touchEvent")
                Spacer(modifier = Modifier.height(4.dp))
                Text("Keyboard : $keyboardText")
                Spacer(modifier = Modifier.height(4.dp))
                Text("Clipboard : $clipboardText")
            }

            Spacer(modifier = Modifier.height(12.dp))

            DashboardCard(title = "Controls") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onAgentButtonClick, modifier = Modifier.weight(1f)) {
                        Text(if (isAgentRunning) "Stop Agent" else "Start Agent")
                    }
                    Button(onClick = onPreviewButtonClick, modifier = Modifier.weight(1f)) {
                        Text("Preview")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onScreenCaptureButtonClick, modifier = Modifier.weight(1f)) {
                        Text(if (screenCaptureState == ScreenCaptureState.Capturing) "Stop Capture" else "Start Capture")
                    }
                    Button(onClick = onCapturePhotoClick, modifier = Modifier.weight(1f)) {
                        Text("Capture Photo")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onCopyClipboardClick, modifier = Modifier.weight(1f)) {
                        Text("Copy")
                    }
                    Button(onClick = onPasteClipboardClick, modifier = Modifier.weight(1f)) {
                        Text("Paste")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onMicToggleClick, modifier = Modifier.weight(1f)) {
                        Text(if (microphoneRecording) "Stop Mic" else "Start Mic")
                    }
                    Button(onClick = onDownloadFileClick, modifier = Modifier.weight(1f)) {
                        Text("Download File")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onUploadFileClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Upload File")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            DashboardCard(title = "Files") {
                if (fileEntries.isEmpty()) {
                    Text("No files in Downloads yet")
                } else {
                    fileEntries.forEach { file ->
                        Text("• ${file.name} (${file.sizeBytes} bytes)")
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Version v0.1.0")
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DashboardCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
