package com.gotkicry.notificationassistant.database

import android.app.Application
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase

@Database(entities = [Notice::class],version = 2)
abstract class Database : RoomDatabase(){
    abstract fun getNoticeDao() : NoticeDao
    companion object{
        private var databaseInstance : com.gotkicry.notificationassistant.database.Database? = null
        fun getDatabase(application: Application) : com.gotkicry.notificationassistant.database.Database?{
            if(databaseInstance == null){
                synchronized(Database::class.java){
                    if(databaseInstance == null){
                        databaseInstance = Room.databaseBuilder(
                            application,
                            com.gotkicry.notificationassistant.database.Database::class.java,
                            "notice.db"
                        ).build()
                    }
                }
            }
            return databaseInstance
        }

    }


}