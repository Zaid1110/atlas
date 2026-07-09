package com.atlas.agent.device

data class DeviceInfo(

    val battery: Int,

    val charging: Boolean,

    val manufacturer: String,

    val model: String,

    val androidVersion: String,

    val wifiName: String,

    val ram: String,

    val storage: String,

    val cpu: String

)