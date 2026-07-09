package com.atlas.agent.device

import android.content.Context
import android.os.Build

object DeviceInfoProvider {

    fun getDeviceInfo(
        context: Context
    ): DeviceInfo {

        return DeviceInfo(

            battery = BatteryProvider.getBatteryLevel(context),

            charging = BatteryProvider.isCharging(context),

            manufacturer = Build.MANUFACTURER,

            model = Build.MODEL,

            androidVersion = Build.VERSION.RELEASE,

            wifiName = WifiProvider.getWifiName(context),

            ram = MemoryProvider.getRamInfo(context),

            storage = StorageProvider.getStorageInfo(),

            cpu = CpuProvider.getCpuCores()

        )

    }

}