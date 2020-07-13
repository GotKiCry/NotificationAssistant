package com.gotkicry.notificationassistant.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.Year

@Entity
class Notice (
    @PrimaryKey(autoGenerate = true)var id : Int?,
    var dayofMonth : Int,
    var month : Int,
    var year: Int,
    var title : String,
    var noticeWay : Int,
    var noticeTime : String,
    var date: Long,
    var eventsID : Long?
){
    override fun toString(): String {
        return "Notice(id=$id, dayofMonth=$dayofMonth, month=$month, year=$year, title='$title', noticeWay=$noticeWay, noticeTime='$noticeTime', date=$date, eventsID=$eventsID)"
    }
}