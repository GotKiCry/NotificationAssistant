package com.gotkicry.notificationassistant

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.Space
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gotkicry.notificationassistant.database.Database
import com.gotkicry.notificationassistant.database.Notice
import com.gotkicry.notificationassistant.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


private const val TAG = "MainActivity";
class MainActivity : AppCompatActivity() {
    private lateinit var bind : ActivityMainBinding
    private lateinit var adapter: NoticeListAdapter
    private lateinit var list : MutableList<Notice>
    private var isFirstOpen = true
    private var topViewHeight = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        loadData()
        initView()
    }

    private fun loadData() {
        //获取数据库
        val noticeViewModel = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(NoticeViewModel::class.java)
        noticeViewModel.getLiveDataNotice().observe(this, Observer {
            list.clear()
            list.addAll(it)
            adapter.notifyDataSetChanged()
            if(isFirstOpen){
                GlobalScope.launch {
                    val passedTime = Database.getDatabase(application)!!.getNoticeDao().getPassedTime(Date().time).size
                    Log.d(TAG, "loadData: $passedTime")
                    withContext(Dispatchers.Main){
                        bind.mainListview.setSelection(passedTime)
                    }
                }
                isFirstOpen = false
            }
        })
    }

    private fun initView() {
        addSystemBarSpace()
        initListView()
        bind.barLayout.barButtonAdd.setOnClickListener {
            startActivityForResult(Intent(this,AddNoticeFactorActivity::class.java),0)
        }
        initBarDate()
        val listViewFooterBar = LayoutInflater.from(this).inflate(R.layout.listview_footer,null,false)
        val space = listViewFooterBar.findViewById<Space>(R.id.space)
        var point : Point = Point()
        windowManager.defaultDisplay.getSize(point)
        val layoutParams = space.layoutParams
        layoutParams.height = point.y - topViewHeight - dip2px(this,60f)
        space.layoutParams = layoutParams
        bind.mainListview.addFooterView(listViewFooterBar,null,false)

    }

    private fun initBarDate() {
        val instance = Calendar.getInstance()
        val date = "${instance.get(Calendar.MONTH)+1}${getString(R.string.month)}${instance.get(Calendar.DAY_OF_MONTH)}${getString(R.string.day)}"
        Log.d(TAG, "initBarDate: $date")
        bind.barLayout.barTextNowDate.text = date
    }

    private fun initListView() {
        list = arrayListOf()
        adapter = NoticeListAdapter(list,this)
        bind.mainListview.adapter = adapter
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

    fun dip2px(context: Context, value: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            context.resources.displayMetrics
        ).toInt()
    }

}