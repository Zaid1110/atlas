package com.atlas.agent.device

import android.os.Environment
import android.os.StatFs

object StorageProvider {

    fun getStorageInfo(): String {

        val stat =
            StatFs(Environment.getDataDirectory().path)

        val total =
            stat.totalBytes / (1024 * 1024 * 1024.0)

        val available =
            stat.availableBytes / (1024 * 1024 * 1024.0)

        val used = total - available

        return String.format("%.1f / %.1f GB", used, total)

    }

}