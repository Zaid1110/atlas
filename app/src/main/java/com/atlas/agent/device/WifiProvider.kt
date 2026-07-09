package com.atlas.agent.device

import android.content.Context
import android.net.ConnectivityManager

object WifiProvider {

    fun getWifiName(context: Context): String {

        return try {

            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE)
                        as ConnectivityManager

            val network = connectivityManager.activeNetwork

            if (network == null) {
                "Disconnected"
            } else {
                "Connected"
            }

        } catch (e: Exception) {

            "Unknown"

        }

    }

}