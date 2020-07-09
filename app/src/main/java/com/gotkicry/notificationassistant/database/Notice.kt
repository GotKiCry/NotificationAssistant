package com.gotkicry.notificationassistant.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.Year

@Entity
class Notice (
    @PrimaryKey(autoGenerate = true)var id : Int?,
    var dayofMonth : String,
    var month : String,
    var year: String,
    var title : String,
    var noticeWay : String,
    var noticeTime : String,
    var date: Long
){
}