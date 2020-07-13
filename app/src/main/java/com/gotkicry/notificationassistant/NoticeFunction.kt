package com.gotkicry.notificationassistant

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import com.gotkicry.notificationassistant.database.Notice
import java.util.*

class NoticeFunction constructor(var context: Context) {

    /**
     * 闹钟指定日期无法实现  暂时搁浅该功能
     */
    fun addAlarmClock() {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM)
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, "测试")
        intent.putExtra(AlarmClock.EXTRA_HOUR, 5)
        intent.putExtra(AlarmClock.EXTRA_MINUTES, 30)
        intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        val arrayList: ArrayList<Int> = arrayListOf(Calendar.MONDAY, Calendar.THURSDAY)
        intent.putExtra(AlarmClock.EXTRA_DAYS, arrayList)
        context.startActivity(intent)
    }

    fun addAlarmManager(id: Int, tagTime: Long, notice: Notice) {
        val intent = Intent()
        when (notice.noticeWay) {
            0 -> intent.action = "com.gotkicry.UPDATE_UI"
            1 -> intent.action = "com.gotkicry.notificationassistant"
        }
        intent.putExtra("id", id)
        val pendingIntent =
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + tagTime,
            pendingIntent
        )
    }

    fun cancelAlarmManager(id: Int, noticeWay: Int) {
        val intent = Intent()
        when (noticeWay) {
            0 -> intent.action = "com.gotkicry.UPDATE_UI"
            1 -> intent.action = "com.gotkicry.notificationassistant"
        }
        val pendingIntent =
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    fun getTagTime(
        tagYear: Int,
        tagMonth: Int,
        tagDayOfMonth: Int,
        tagHour: Int,
        tagMinute: Int
    ): Long {
        val calendar = Calendar.getInstance()
        var year = if (tagYear > calendar.get(Calendar.YEAR)) {
            tagYear - calendar.get(Calendar.YEAR)
        } else {
            0
        }
        var month = if (tagMonth < (calendar.get(Calendar.MONTH) + 1)) {
            year -= 1
            tagMonth - (calendar.get(Calendar.MONTH) + 1) + 12
        } else {
            tagMonth - (calendar.get(Calendar.MONTH) + 1)
        }
        var dayofMonth = if (tagDayOfMonth < calendar.get(Calendar.DAY_OF_MONTH)) {
            month -= 1
            tagDayOfMonth - calendar.get(Calendar.DAY_OF_MONTH) + 30
        } else {
            tagDayOfMonth - calendar.get(Calendar.DAY_OF_MONTH)
        }
        var hour = if (tagHour < calendar.get(Calendar.HOUR_OF_DAY)) {
            dayofMonth -= 1
            tagHour - calendar.get(Calendar.HOUR_OF_DAY) + 24
        } else {
            tagHour - calendar.get(Calendar.HOUR_OF_DAY)
        }
        val minute = if (tagMinute < calendar.get(Calendar.MINUTE)) {
            hour -= 1
            tagMinute - calendar.get(Calendar.MINUTE) + 60
        } else {
            tagMinute - calendar.get(Calendar.MINUTE)
        }
        return year.toLong() * 1000 * 3600 * 24 * 365 +
                month.toLong() * 1000 * 3600 * 24 * 30 +
                dayofMonth.toLong() * 1000 * 3600 * 24 +
                hour.toLong() * 1000 * 3600 +
                (minute.toLong() - 1) * 1000 * 60 +
                (60 - calendar.get(Calendar.SECOND)) * 1000
    }
}