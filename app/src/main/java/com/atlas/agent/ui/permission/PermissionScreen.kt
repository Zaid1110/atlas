package com.atlas.agent.ui.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.atlas.agent.permission.PermissionKey
import com.atlas.agent.permission.PermissionManager
import com.atlas.agent.permission.PermissionState

@Composable
fun PermissionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var permissions by remember { mutableStateOf(PermissionManager.getPermissions(context)) }

    fun refreshPermissions() {
        permissions = PermissionManager.getPermissions(context)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshPermissions()
    }

    val microphoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshPermissions()
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshPermissions()
    }

    LaunchedEffect(Unit) {
        refreshPermissions()
    }

    BackHandler(onBack = onBack)

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Permissions",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Button(onClick = onBack) {
                    Text("Back")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = permissions,
                    key = { it.key.name }
                ) { permission ->
                    PermissionCard(
                        permission = permission,
                        onActionClick = {
                            when (it.key) {
                                PermissionKey.Camera -> {
                                    if (it.state == PermissionState.Granted) {
                                        openAppSettings(context)
                                    } else {
                                        cameraLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                                PermissionKey.Microphone -> {
                                    if (it.state == PermissionState.Granted) {
                                        openAppSettings(context)
                                    } else {
                                        microphoneLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                                PermissionKey.Accessibility -> openAccessibilitySettings(context)
                                PermissionKey.Notifications -> {
                                    if (it.state == PermissionState.Granted) {
                                        openNotificationSettings(context)
                                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        openAppSettings(context)
                                    }
                                }
                                PermissionKey.ScreenCapture,
                                PermissionKey.BatteryOptimization,
                                PermissionKey.Storage,
                                PermissionKey.Internet,
                                PermissionKey.Network -> openAppSettings(context)
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun openAccessibilitySettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    context.startActivity(intent)
}

private fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        "package:${context.packageName}".toUri()
    )
    context.startActivity(intent)
}
