package com.atlas.agent.permission

data class PermissionItem(
    val key: PermissionKey,
    val title: String,
    val state: PermissionState,
    val action: PermissionAction
)

enum class PermissionKey {
    Camera,
    Microphone,
    Notifications,
    Accessibility,
    ScreenCapture,
    BatteryOptimization,
    Storage,
    Internet,
    Network
}

enum class PermissionAction {
    RequestRuntime,
    OpenSettings
}
