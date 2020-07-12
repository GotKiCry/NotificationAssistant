package com.gotkicry.notificationassistant

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.AlarmClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

private const val TAG = "NoticeFunctionActivity";
class NoticeFunction constructor(var context: Context) {

    /**
     * 闹钟指定日期无法实现  暂时搁浅该功能
     */
    fun addAlarmClock(){
        var intent = Intent(AlarmClock.ACTION_SET_ALARM)
        intent.putExtra(AlarmClock.EXTRA_MESSAGE,"测试")
        intent.putExtra(AlarmClock.EXTRA_HOUR,5)
        intent.putExtra(AlarmClock.EXTRA_MINUTES,30)
        intent.putExtra(AlarmClock.EXTRA_SKIP_UI,true)
        val arrayList : ArrayList<Int> = arrayListOf(Calendar.MONDAY, Calendar.THURSDAY)
        intent.putExtra(AlarmClock.EXTRA_DAYS,arrayList )
        context.startActivity(intent)
    }

    fun addAlarmManager(id: Int,tagTime : Long,noticeWay : Int){
        val intent = Intent()
        when(noticeWay){
            0->intent.action = "com.gotkicry.UPDATE_UI"
            1->intent.action = "com.gotkicry.notificationassistant"
        }
        intent.putExtra("id",id)
        val pendingIntent = PendingIntent.getBroadcast(context,id,intent,PendingIntent.FLAG_CANCEL_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+tagTime,pendingIntent)
    }

    fun cancelAlarmManager(id: Int,noticeWay : Int){
        val intent = Intent()
        when(noticeWay){
            0->intent.action = "com.gotkicry.UPDATE_UI"
            1->intent.action = "com.gotkicry.notificationassistant"
        }
        val pendingIntent = PendingIntent.getBroadcast(context,id,intent,PendingIntent.FLAG_CANCEL_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    fun getTagTime(tagYear :Int, tagMonth :Int, tagDayOfMonth: Int, tagHour : Int, tagMinute :Int) : Long{
        val calendar = Calendar.getInstance()
        var year = if(tagYear > calendar.get(Calendar.YEAR)){
            tagYear - calendar.get(Calendar.YEAR)
        }else{
            0
        }
        var month = if(tagMonth < (calendar.get(Calendar.MONTH)+1)){
            year -= 1
            tagMonth - (calendar.get(Calendar.MONTH)+1)
        }else{
            tagMonth - (calendar.get(Calendar.MONTH)+1)
        }
        var dayofMonth =  if(tagDayOfMonth < calendar.get(Calendar.DAY_OF_MONTH)){
            month -= 1
            tagDayOfMonth - calendar.get(Calendar.DAY_OF_MONTH)
        }else{
            tagDayOfMonth - calendar.get(Calendar.DAY_OF_MONTH)
        }
        var hour =  if(tagHour < calendar.get(Calendar.HOUR_OF_DAY)){
            dayofMonth -= 1
            tagHour - calendar.get(Calendar.HOUR_OF_DAY)
        }else{
            tagHour - calendar.get(Calendar.HOUR_OF_DAY)
        }
        var minute =  if(tagMinute < calendar.get(Calendar.MINUTE)){
            hour -= 1
            tagMinute - calendar.get(Calendar.MINUTE)
        }else{
            tagMinute - calendar.get(Calendar.MINUTE)
        }
        Log.d(TAG, "getTagTime: $year  $month  $dayofMonth  $hour  $minute")
        return year.toLong()*1000*3600*24*365+
                month.toLong()*1000*3600*24*30+
                dayofMonth.toLong()*1000*3600*24+
                hour.toLong()*1000*3600+
                (minute.toLong()-1)*1000*60+
                (60-calendar.get(Calendar.SECOND))*1000
    }

    fun getPermission(vararg permissions : String){
        for(permission in permissions){
            val checkSelfPermission = ContextCompat.checkSelfPermission(context,
                permission)
            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((context as AppCompatActivity), arrayOf(permission),1)
            }else{
                Log.d(TAG, "getPermission: 已授权")

            }
        }
    }
}