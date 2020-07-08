package com.gotkicry.notificationassistant.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface  NoticeDao {
    @Insert
    fun addNewNotice(vararg notice: Notice)

    @Query("SELECT * FROM Notice")
    fun getAllNotice():LiveData<List<Notice>>

    @Delete
    fun delNotice(notice: Notice)
}