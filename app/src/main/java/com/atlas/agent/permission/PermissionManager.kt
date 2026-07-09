package com.atlas.agent.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.atlas.agent.screen.ScreenCaptureManager
import com.atlas.agent.screen.ScreenCaptureState

object PermissionManager {

    fun getPermissions(context: Context): List<PermissionItem> {
        return listOf(
            PermissionItem(
                key = PermissionKey.Camera,
                title = "Camera",
                state = stateFor(check(context, Manifest.permission.CAMERA)),
                action = PermissionAction.RequestRuntime
            ),
            PermissionItem(
                key = PermissionKey.Microphone,
                title = "Microphone",
                state = stateFor(check(context, Manifest.permission.RECORD_AUDIO)),
                action = PermissionAction.RequestRuntime
            ),
            PermissionItem(
                key = PermissionKey.Notifications,
                title = "Notifications",
                state = stateFor(isNotificationPermissionGranted(context)),
                action = PermissionAction.OpenSettings
            ),
            PermissionItem(
                key = PermissionKey.Accessibility,
                title = "Accessibility Service",
                state = stateFor(isAccessibilityEnabled(context)),
                action = PermissionAction.OpenSettings
            ),
            PermissionItem(
                key = PermissionKey.ScreenCapture,
                title = "Screen Capture",
                state = stateFor(ScreenCaptureManager.state != ScreenCaptureState.NotGranted),
                action = PermissionAction.OpenSettings
            ),
            PermissionItem(
                key = PermissionKey.Storage,
                title = "Storage",
                state = PermissionState.Granted,
                action = PermissionAction.OpenSettings
            ),
            PermissionItem(
                key = PermissionKey.Internet,
                title = "Internet",
                state = stateFor(check(context, Manifest.permission.INTERNET)),
                action = PermissionAction.OpenSettings
            ),
            PermissionItem(
                key = PermissionKey.Network,
                title = "Network",
                state = stateFor(check(context, Manifest.permission.ACCESS_NETWORK_STATE)),
                action = PermissionAction.OpenSettings
            )
        )
    }

    fun isAccessibilityEnabled(context: Context): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.split(':').any { service ->
            service.startsWith("${context.packageName}/", ignoreCase = true)
        }
    }

    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            true
        } else {
            check(context, Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun isNotificationListenerEnabled(context: Context): Boolean {
        return isNotificationPermissionGranted(context)
    }

    private fun check(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun stateFor(granted: Boolean): PermissionState {
        return if (granted) PermissionState.Granted else PermissionState.NotGranted
    }
}
