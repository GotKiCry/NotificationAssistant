package com.gotkicry.notificationassistant

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract.Events
import android.provider.CalendarContract.Reminders
import androidx.core.app.ActivityCompat
import com.gotkicry.notificationassistant.database.Database
import com.gotkicry.notificationassistant.database.Notice
import java.util.*

class CalendarFunction constructor(var  context: Context){
    val CALENDAR_URL = "content://com.android.calendar"
    val CALENDAR_ENENT_URL = "content://com.android.calendar/events"
    val CALENDAR_REMINDER_URL = "content://com.android.calendar/reminders"


    fun addCalendar(title : String,time : Long) : Long{
        val CALENDAR_ID = "1"
        val calendar = Calendar.getInstance()
        calendar.time = Date(time)
        val contentProvider = context.contentResolver
        val eventsValue = ContentValues()
        eventsValue.put(Events.TITLE,title)
        eventsValue.put(Events.CALENDAR_ID,CALENDAR_ID)
        eventsValue.put(Events.DTSTART,time)
        eventsValue.put(Events.DTEND,time)
        eventsValue.put(Events.HAS_ALARM,1)
        eventsValue.put(Events.EVENT_TIMEZONE,TimeZone.getDefault().id)
        val uri = contentProvider.insert(Uri.parse(CALENDAR_ENENT_URL),eventsValue)
        val eventsID = uri?.lastPathSegment!!.toLong()
        //添加提醒
        val reminderValues = ContentValues()
        reminderValues.put(Reminders.METHOD,1)
        reminderValues.put(Reminders.MINUTES,0)
        reminderValues.put(Reminders.EVENT_ID,eventsID)
        contentProvider.insert(Uri.parse(CALENDAR_REMINDER_URL),reminderValues)
        return eventsID
    }

    fun delCalendar(eventsID : Long){
        val contentResolver = context.contentResolver
        val deleteUri = ContentUris.withAppendedId(Uri.parse(CALENDAR_ENENT_URL),eventsID)
        contentResolver.delete(deleteUri,null,null)
    }

    fun updateCalendar(eventsID: Long,title : String,time : Long){
        val contentResolver = context.contentResolver
        val values = ContentValues()
        values.put(Events.TITLE,title)
        values.put(Events.DTSTART,time)
        values.put(Events.DTEND,time)
        val updateUri = ContentUris.withAppendedId(Uri.parse(CALENDAR_ENENT_URL), eventsID)
        contentResolver.update(updateUri,values,null,null)
    }
}