package com.atlas.agent.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.agent.device.DeviceInfo
import com.atlas.agent.screen.ScreenCaptureState

@Composable
fun DashboardScreen(
    deviceInfo: DeviceInfo,
    isAgentRunning: Boolean,
    onAgentButtonClick: () -> Unit,
    screenCaptureState: ScreenCaptureState,
    framesCaptured: Long,
    currentFps: Int,
    latestFrameSizeBytes: Int?,
    onScreenCaptureButtonClick: () -> Unit
) {

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "ATLAS",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Home Android"
            )

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Status : ${
                            if (isAgentRunning) "Running" else "Stopped"
                        }"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Battery : ${deviceInfo.battery}%")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Charging : ${
                            if (deviceInfo.charging) "Yes" else "No"
                        }"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Phone : ${deviceInfo.manufacturer} ${deviceInfo.model}"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Android : ${deviceInfo.androidVersion}"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Wi-Fi : Coming Soon")

                }

            }

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Screen Capture : ${
                            when (screenCaptureState) {
                                ScreenCaptureState.NotGranted -> "Not Granted"
                                ScreenCaptureState.Ready -> "Ready"
                                ScreenCaptureState.Capturing -> "Capturing..."
                            }
                        }"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Frames Captured : $framesCaptured")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Current FPS : $currentFps")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Latest Frame Size : ${
                            latestFrameSizeBytes?.let { it / 1024 } ?: 0
                        } KB"
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onAgentButtonClick
            ) {
                Text(
                    if (isAgentRunning) "Stop Agent" else "Start Agent"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onScreenCaptureButtonClick
            ) {
                Text(
                    if (screenCaptureState == ScreenCaptureState.Capturing) {
                        "Stop Capture"
                    } else {
                        "Start Capture"
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Version v0.1.0")

        }

    }

}
