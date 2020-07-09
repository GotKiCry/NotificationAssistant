package com.gotkicry.notificationassistant.database

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Dao
interface  NoticeDao {
    @Insert
    fun addNewNotice(vararg notice: Notice)

    //按照时间排序
    @Query("SELECT * FROM Notice ORDER BY date")
    fun getAllNotice():LiveData<List<Notice>>

    @Query("SELECT date FROM Notice where date < :date")
    fun getPassedTime(date: Long) : List<Long>

    @Query("SELECT * FROM Notice WHERE id = :id")
    fun getOneNotice(id : Int) : Notice

    @Delete
    fun delNotice(notice: Notice)

    @Update
    fun updateNotice(notice: Notice)
}