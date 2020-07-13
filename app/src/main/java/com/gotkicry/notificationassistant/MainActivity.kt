package com.gotkicry.notificationassistant

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.Space
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import kotlin.math.log


private const val TAG = "MainActivity";
class MainActivity : AppCompatActivity() {
    private lateinit var bind : ActivityMainBinding
    private lateinit var adapter: NoticeListAdapter
    private lateinit var list : MutableList<Notice>
    private var isFirstOpen = true
    private var topViewHeight = 0
    private lateinit var uiBroadcastReceiver: UIBroadcastReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        startService()
        loadData()
        initView()
    }

    private fun loadData() {
        //获取数据库
        val noticeViewModel = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(NoticeViewModel::class.java)
        noticeViewModel.getLiveDataNotice().observe(this, Observer { it ->
            list.clear()
            list.addAll(it)
            adapter.notifyDataSetChanged()
            it.forEach { Log.d(TAG, "loadData: $it") }
            if(isFirstOpen){
                GlobalScope.launch {
                    val passedTime = Database.getDatabase(application)!!.getNoticeDao().getPassedTime(Date().time).size
                    withContext(Dispatchers.Main){
                        if(it.size == passedTime){
                            Toast.makeText(this@MainActivity,"无提醒计划",Toast.LENGTH_SHORT).show()
                            bind.mainListview.setSelection(passedTime - 1)
                        }else{
                            bind.mainListview.setSelection(passedTime)
                        }

                    }
                }
                val noticeFunction = NoticeFunction(this)
                for(notice in it){
                    if(notice.date - System.currentTimeMillis() < 0) continue
                    val year = notice.year.toInt()
                    val month = notice.month.toInt()
                    val dayOfMonth = notice.dayofMonth.toInt()
                    val time = notice.noticeTime.split(":")
                    val hour = time[0].toInt()
                    val minute = time[1].toInt()
                    noticeFunction.addAlarmManager(notice.id!!,noticeFunction.getTagTime(year,month,dayOfMonth,hour,minute),notice)
                }

                isFirstOpen = false
            }

        })
        uiBroadcastReceiver = UIBroadcastReceiver()
    }

    private fun initView() {
        addSystemBarSpace()
        initListView()
        bind.barLayout.barButtonAdd.setOnClickListener {
            startActivity(Intent(this,AddNoticeFactorActivity::class.java))
        }
        initBarDate()


    }

    private fun initBarDate() {
        val instance = Calendar.getInstance()
        val date = "${resources.getStringArray(R.array.month_)[instance.get(Calendar.MONTH)]}${instance.get(Calendar.DAY_OF_MONTH)}${getString(R.string.day)}"
        Log.d(TAG, "initBarDate: $date")
        bind.barLayout.barTextNowDate.text = date
    }

    private fun initListView() {
        list = arrayListOf()
        adapter = NoticeListAdapter(list,this)
        bind.mainListview.adapter = adapter

        val listViewFooterBar = LayoutInflater.from(this).inflate(R.layout.listview_footer,null,false)
        val space = listViewFooterBar.findViewById<Space>(R.id.space)
        var point : Point = Point()
        windowManager.defaultDisplay.getSize(point)
        val layoutParams = space.layoutParams
        layoutParams.height = point.y - topViewHeight - dip2px(this,60f)
        space.layoutParams = layoutParams
        bind.mainListview.addFooterView(listViewFooterBar,null,false)
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
    fun startService(){
        startService(Intent(this,
            KeepAliveService::class.java))
    }
    fun stopService(){
        stopService(Intent(this,
            KeepAliveService::class.java))
    }

    inner class UIBroadcastReceiver() : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive: -------------------------------------")
            adapter.notifyDataSetChanged()
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.gotkicry.main.UPDATE_UI")
        registerReceiver(uiBroadcastReceiver,intentFilter)
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