package com.gotkicry.notificationassistant.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoticeDao {
    @Insert
    fun addNewNotice(vararg notice: Notice)

    //按照时间排序
    @Query("SELECT * FROM Notice ORDER BY date")
    fun getAllNotice(): LiveData<List<Notice>>

    @Query("SELECT date FROM Notice where date < :date")
    fun getPassedTime(date: Long): List<Long>

    @Query("SELECT * FROM Notice WHERE id = :id")
    fun getOneNotice(id: Int): Notice

    @Query("SELECT id FROM Notice ORDER BY id DESC limit 1")
    fun getLastID(): Int

    @Query("SELECT eventsID FROM Notice WHERE eventsID = :eventsID")
    fun queryHasSameEventsID(eventsID : Long) : List<Long>

    @Query("SELECT eventsID FROM Notice")
    fun getEventsID() : MutableList<Long>

    @Query("DELETE FROm Notice WHERE eventsID = :eventsID")
    fun delByEventsID(eventsID: Long)
    @Delete
    fun delNotice(notice: Notice)

    @Update
    fun updateNotice(notice: Notice)
}