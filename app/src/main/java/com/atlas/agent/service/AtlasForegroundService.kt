package com.atlas.agent.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder

class AtlasForegroundService : Service() {

    companion object {
        var isRunning: Boolean = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        NotificationHelper.createChannel(this)
        LocalDemoServer.initialize(this)
        LocalDemoServer.startServer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAsForegroundService()
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        LocalDemoServer.stopServer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startAsForegroundService() {
        val notification = NotificationHelper.createForegroundNotification(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationHelper.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NotificationHelper.NOTIFICATION_ID, notification)
        }
    }
}
