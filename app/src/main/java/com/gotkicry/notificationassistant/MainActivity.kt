package com.gotkicry.notificationassistant

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.Space
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gotkicry.notificationassistant.backstage.KeepAliveService
import com.gotkicry.notificationassistant.database.Database
import com.gotkicry.notificationassistant.database.Notice
import com.gotkicry.notificationassistant.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding
    private lateinit var adapter: NoticeListAdapter
    private lateinit var list: MutableList<Notice>
    private var isFirstOpen = true
    private var topViewHeight = 0
    private lateinit var uiBroadcastReceiver: UIBroadcastReceiver
    private lateinit var calendarFunction: CalendarFunction
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        startService()
        loadData()
        initView()
    }

    private fun loadData() {
        calendarFunction = CalendarFunction(this)
        calendarFunction.cleanDatabase()
        //获取数据库
        val noticeViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoticeViewModel::class.java)
        noticeViewModel.getLiveDataNotice().observe(this, Observer {
            list.clear()
            list.addAll(it)
            adapter.notifyDataSetChanged()
            if (isFirstOpen) {
                GlobalScope.launch(Dispatchers.IO) {
                    getSystemCalendar()
                    activityInit(it)
                }
            }
        })
        uiBroadcastReceiver = UIBroadcastReceiver()
    }

    private suspend fun activityInit(it: List<Notice>) {
        val passedTime =
            Database.getDatabase(application)!!.getNoticeDao().getPassedTime(Date().time).size
        withContext(Dispatchers.Main) {
            if (it.size == passedTime) {
                Toast.makeText(this@MainActivity, "无提醒计划", Toast.LENGTH_SHORT).show()
                bind.mainListview.setSelection(passedTime - 1)
            } else {
                bind.mainListview.setSelection(passedTime)
            }
        }
        val noticeFunction = NoticeFunction(this)
        for (notice in it) {
            if (notice.date - System.currentTimeMillis() < 0) continue
            val year = notice.year
            val month = notice.month
            val dayOfMonth = notice.dayofMonth
            val time = notice.noticeTime.split(":")
            val hour = time[0].toInt()
            val minute = time[1].toInt()
            noticeFunction.addAlarmManager(
                notice.id!!,
                noticeFunction.getTagTime(year, month, dayOfMonth, hour, minute),
                notice
            )
        }
        isFirstOpen = false
    }

    private fun getSystemCalendar() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALENDAR), 1)
        } else {
            calendarFunction.getCalendar()
        }
    }

    private fun initView() {
        addSystemBarSpace()
        initListView()
        bind.barLayout.barButtonAdd.setOnClickListener {
            startActivity(Intent(this, AddNoticeFactorActivity::class.java))
        }
        initBarDate()


    }

    private fun initBarDate() {
        val instance = Calendar.getInstance()
        val date =
            "${resources.getStringArray(R.array.month_)[instance.get(Calendar.MONTH)]}${instance.get(
                Calendar.DAY_OF_MONTH
            )}${getString(R.string.day)}"
        bind.barLayout.barTextNowDate.text = date
    }

    private fun initListView() {
        list = arrayListOf()
        adapter = NoticeListAdapter(list, this)
        bind.mainListview.adapter = adapter

        val listViewFooterBar =
            LayoutInflater.from(this).inflate(R.layout.listview_footer, null, false)
        val space = listViewFooterBar.findViewById<Space>(R.id.space)
        val point: Point = Point()
        windowManager.defaultDisplay.getSize(point)
        val layoutParams = space.layoutParams
        layoutParams.height = point.y - topViewHeight - dip2px(this, 60f)
        space.layoutParams = layoutParams
        bind.mainListview.addFooterView(listViewFooterBar, null, false)
    }

    private fun addSystemBarSpace() {
        //获取状态栏高度
        val resources: Resources = this.resources
        val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        val height: Int = resources.getDimensionPixelSize(resourceId)

        val layoutParam = bind.statusView.layoutParams
        layoutParam.height = height
        topViewHeight = height + bind.layoutBar.layoutParams.height
        bind.statusView.layoutParams = layoutParam
    }

    private fun dip2px(context: Context, value: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            context.resources.displayMetrics
        ).toInt()
    }

    private fun startService() {
        startService(
            Intent(
                this,
                KeepAliveService::class.java
            )
        )
    }

    fun stopService() {
        stopService(
            Intent(
                this,
                KeepAliveService::class.java
            )
        )
    }

    inner class UIBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            adapter.notifyDataSetChanged()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions[0] == Manifest.permission.READ_CALENDAR || grantResults[0] == 0) {
            GlobalScope.launch {
                calendarFunction.getCalendar()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.main_sync_toast),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.gotkicry.main.UPDATE_UI")
        registerReceiver(uiBroadcastReceiver, intentFilter)
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(uiBroadcastReceiver)
    }

}