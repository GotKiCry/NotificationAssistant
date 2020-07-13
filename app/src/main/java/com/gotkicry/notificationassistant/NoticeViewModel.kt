package com.gotkicry.notificationassistant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.gotkicry.notificationassistant.database.Database
import com.gotkicry.notificationassistant.database.Notice

class NoticeViewModel constructor(application: Application) : AndroidViewModel(application) {
    private var database: Database = Database.getDatabase(application)!!
    private lateinit var livedataNotice: LiveData<List<Notice>>

    init {
        livedataNotice = database.getNoticeDao().getAllNotice()
    }

    fun getLiveDataNotice(): LiveData<List<Notice>> {
        return livedataNotice
    }
}