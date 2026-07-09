package com.atlas.agent.device

import android.app.ActivityManager
import android.content.Context

object MemoryProvider {

    fun getRamInfo(context: Context): String {

        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE)
                    as ActivityManager

        val memoryInfo = ActivityManager.MemoryInfo()

        activityManager.getMemoryInfo(memoryInfo)

        val total =
            memoryInfo.totalMem / (1024 * 1024 * 1024.0)

        val available =
            memoryInfo.availMem / (1024 * 1024 * 1024.0)

        val used = total - available

        return String.format("%.1f / %.1f GB", used, total)

    }

}