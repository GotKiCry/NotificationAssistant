package com.gotkicry.notificationassistant.backstage

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.gotkicry.notificationassistant.R
import com.gotkicry.notificationassistant.database.Database
import com.gotkicry.notificationassistant.database.Notice
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotificationBroadcastReceiver : BroadcastReceiver() {
    private var id = 0
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "com.gotkicry.UPDATE_UI" -> {
                sendBroadcast(context)
            }
            "com.gotkicry.notificationassistant" -> {
                GlobalScope.launch {
                    val singleData =
                        getSingleData(context as Application, intent!!.getIntExtra("id", 0))
                    showNotification(context, singleData)
                    sendBroadcast(context)
                }
            }
        }
    }

    private fun sendBroadcast(context: Context?) {
        val intent = Intent("com.gotkicry.main.UPDATE_UI")
        context?.sendBroadcast(intent)
    }

    private fun getSingleData(application: Application, id: Int): Notice {
        return Database.getDatabase(application)!!
            .getNoticeDao().getOneNotice(id)
    }

    private fun showNotification(context: Context?, notice: Notice) {
        val channel_ID = "1"
        val channel_Name = "Notice_"

        val notificationManager =
            context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channel_ID,
                channel_Name,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notification = NotificationCompat.Builder(context, channel_ID)
            .setContentTitle(notice.title)
            .setContentText("${context.getString(R.string.notification_text)} ${notice.noticeTime}")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.mipmap.ic_launcher
                )
            )
            .build()
        notification.flags = Notification.FLAG_AUTO_CANCEL
        notificationManager.notify(id++, notification)
        val currentTimeMillis = System.currentTimeMillis()
        Log.d("TAG", "Now Time: $currentTimeMillis")

    }
}