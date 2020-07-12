package com.gotkicry.notificationassistant.backstage

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log

class KeepAliveService : Service() {
    private var receiver: NotificationBroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.gotkicry.notificationassistant")
        intentFilter.addAction("com.gotkicry.UPDATE_UI")
        receiver =
            NotificationBroadcastReceiver()
        application.registerReceiver(receiver,intentFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(receiver == null){
            val intentFilter = IntentFilter()
            intentFilter.addAction("com.gotkicry.notificationassistant")
            intentFilter.addAction("com.gotkicry.UPDATE_UI")
            receiver =
                NotificationBroadcastReceiver()
            registerReceiver(receiver,intentFilter)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        application.unregisterReceiver(receiver)
    }
}
