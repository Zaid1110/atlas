package com.atlas.agent.device

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

object BatteryProvider {

    fun getBatteryLevel(context: Context): Int {

        val intent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        return intent?.getIntExtra(
            BatteryManager.EXTRA_LEVEL,
            -1
        ) ?: -1
    }

    fun isCharging(context: Context): Boolean {

        val intent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val status = intent?.getIntExtra(
            BatteryManager.EXTRA_STATUS,
            -1
        )

        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }
}