package com.gotkicry.notificationassistant

import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract.Events
import android.provider.CalendarContract.Reminders
import android.util.Log
import android.widget.Toast
import com.gotkicry.notificationassistant.database.Database
import com.gotkicry.notificationassistant.database.Notice
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class CalendarFunction constructor(var context: Context) {
    private val CALENDAR_ENENT_URL = "content://com.android.calendar/events"
    private val CALENDAR_REMINDER_URL = "content://com.android.calendar/reminders"


    fun addCalendar(title: String, time: Long): Long {
        val CALENDAR_ID = "1"
        val calendar = Calendar.getInstance()
        calendar.time = Date(time)
        val contentProvider = context.contentResolver
        val eventsValue = ContentValues()
        eventsValue.put(Events.TITLE, title)
        eventsValue.put(Events.CALENDAR_ID, CALENDAR_ID)
        eventsValue.put(Events.DTSTART, time)
        eventsValue.put(Events.DTEND, time)
        eventsValue.put(Events.HAS_ALARM, 1)
        eventsValue.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        val uri = contentProvider.insert(Uri.parse(CALENDAR_ENENT_URL), eventsValue)
        val eventsID = uri?.lastPathSegment!!.toLong()
        //添加提醒
        val reminderValues = ContentValues()
        reminderValues.put(Reminders.METHOD, 1)
        reminderValues.put(Reminders.MINUTES, 0)
        reminderValues.put(Reminders.EVENT_ID, eventsID)
        contentProvider.insert(Uri.parse(CALENDAR_REMINDER_URL), reminderValues)
        return eventsID
    }

    fun delCalendar(eventsID: Long) {
        val contentResolver = context.contentResolver
        val deleteUri = ContentUris.withAppendedId(Uri.parse(CALENDAR_ENENT_URL), eventsID)
        contentResolver.delete(deleteUri, null, null)
    }

    fun updateCalendar(eventsID: Long, title: String, time: Long) {
        val contentResolver = context.contentResolver
        val values = ContentValues()
        values.put(Events.TITLE, title)
        values.put(Events.DTSTART, time)
        values.put(Events.DTEND, time)
        val updateUri = ContentUris.withAppendedId(Uri.parse(CALENDAR_ENENT_URL), eventsID)
        contentResolver.update(updateUri, values, null, null)
    }

    fun getCalendar() {
        val query = context.contentResolver.query(
            Uri.parse(CALENDAR_ENENT_URL),
            null,
            null,
            null,
            null
        )
        val noticeDao = Database.getDatabase((context as Activity).application)!!.getNoticeDao()
        while (query!!.moveToNext()) {
            val eventsID = query.getString(query.getColumnIndex("_id")).toLong()
            if (noticeDao.queryHasSameEventsID(eventsID).size == 1) continue
            val title = query.getString(query.getColumnIndex("title"))
            val time = query.getString(query.getColumnIndex("dtstart")).toLong()
            val calendar = Calendar.getInstance()
            calendar.time = Date(time)
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = if (calendar.get(Calendar.HOUR_OF_DAY) < 10) {
                "0${calendar.get(Calendar.HOUR_OF_DAY)}"
            } else {
                "${calendar.get(Calendar.HOUR_OF_DAY)}"
            }
            val minute = if (calendar.get(Calendar.MINUTE) < 10) {
                "0${calendar.get(Calendar.MINUTE)}"
            } else {
                "${calendar.get(Calendar.MINUTE)}"
            }
            val notice =
                Notice(null, dayOfMonth, month, year, title, 0, "$hour:$minute", time, eventsID)
            noticeDao.addNewNotice(notice)
        }
        query.close()

    }

    fun cleanDatabase() {
        val cursor = context.contentResolver.query(
            Uri.parse(CALENDAR_ENENT_URL),
            arrayOf(Events._ID), null, null, null
        )
        val noticeDao = Database.getDatabase((context as Activity).application)?.getNoticeDao()
        GlobalScope.launch {
            val eventsID = noticeDao?.getEventsID()
            while (cursor!!.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex("_id")).toLong()
                if (eventsID?.contains(id)!!) {
                    eventsID.remove(id)
                }
            }
            Log.d("TAG", "cleanDatabase: ${eventsID!!.size}")
            for (id : Long? in eventsID) {
                Log.d("TAG", "cleanDatabase: $id")
                if(id == null) continue
                noticeDao.delByEventsID(id!!)
            }
            cursor.close()
        }
    }
}