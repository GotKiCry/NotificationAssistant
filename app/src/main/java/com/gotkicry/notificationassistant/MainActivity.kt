package com.gotkicry.notificationassistant

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.room.RoomDatabase
import com.gotkicry.notificationassistant.database.Database
import com.gotkicry.notificationassistant.database.Notice
import com.gotkicry.notificationassistant.database.NoticeDao
import com.gotkicry.notificationassistant.databinding.ActivityMainBinding
import kotlinx.coroutines.*

private const val TAG = "MainActivity";
class MainActivity : AppCompatActivity() {
    private lateinit var bind : ActivityMainBinding
    private lateinit var adapter: NoticeListAdapter
    private lateinit var list : MutableList<Notice>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        loadData()
        initView()
    }


    private fun initView() {
        addSystemBarSpace()
        initListView()
        bind.barLayout.barButtonAdd.setOnClickListener {
            startActivityForResult(Intent(this,AddNoticeFactorActivity::class.java),0)
        }
    }

    private fun initListView() {
        list = arrayListOf()
        adapter = NoticeListAdapter(list,this)
        bind.mainListview.adapter = adapter
    }

    private fun loadData() {
        //获取数据库
        val noticeViewModel = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(NoticeViewModel::class.java)
        noticeViewModel.getLiveDataNotice().observe(this, Observer {
            list.clear()
            list.addAll(it)
            adapter.notifyDataSetChanged()
        })
    }


    private fun addSystemBarSpace() {
        //获取状态栏高度
        val resources: Resources = this.resources
        val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        val height: Int = resources.getDimensionPixelSize(resourceId)

        val layoutParam = bind.statusView.layoutParams
        layoutParam.height = height
        bind.statusView.layoutParams = layoutParam
    }

}