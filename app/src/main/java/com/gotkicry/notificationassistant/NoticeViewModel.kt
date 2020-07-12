package com.gotkicry.notificationassistant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.gotkicry.notificationassistant.database.Database
import com.gotkicry.notificationassistant.database.Notice

class NoticeViewModel constructor(application: Application) : AndroidViewModel(application) {
    private lateinit var database : Database
    private lateinit var livedataNotice : LiveData<List<Notice>>
    init {
        database = Database.getDatabase(application)!!
        livedataNotice = database.getNoticeDao().getAllNotice()
    }

    fun getLiveDataNotice():LiveData<List<Notice>>{
        return livedataNotice
    }
}