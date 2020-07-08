package com.gotkicry.notificationassistant

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import com.gotkicry.notificationassistant.database.Database
import com.gotkicry.notificationassistant.database.Notice
import com.gotkicry.notificationassistant.databinding.ActivityAddNoticeFactorBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

private const val TAG = "AddNoticeFactorActivity";
class AddNoticeFactorActivity : AppCompatActivity() {
    private lateinit var bind : ActivityAddNoticeFactorBinding
    private lateinit var database: Database

    private var mYear: Int = 0
    private var mDayofMonth: Int = 0
    private var mMonth: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityAddNoticeFactorBinding.inflate(layoutInflater)
        setContentView(bind.root)
        init()
    }

    private fun init() {
        //初始化数据库连接
        database = Database.getDatabase(application)!!

        val calendar = Calendar.getInstance()
        mYear = calendar.get(Calendar.YEAR)
        mMonth = calendar.get(Calendar.MONTH)+1
        mDayofMonth = calendar.get(Calendar.DAY_OF_MONTH)
        addSystemBarSpace()
        initBar()
        initMainView()
    }

    private fun initMainView() {
        bind.factorTextDate.text = "$mYear.$mMonth.$mDayofMonth"
        bind.factorTextDate.setOnClickListener {
            DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener
                { view, year, month, dayOfMonth ->
                    this.mDayofMonth = dayOfMonth
                    this.mMonth = month+1
                    this.mYear = year
                    bind.factorTextDate.text = "$mYear.$mMonth.$mDayofMonth"
                }
                ,mYear,mMonth-1,mDayofMonth).show()

        }
    }

    private fun initBar() {
        bind.barLayout.barButtonBack.setOnClickListener {
            setResult(0)
            finish()
        }
        bind.barLayout.barTextTitle.text = if(intent.getBooleanExtra("isAdd",true)){
            "添加"
        }else{
            "修改"
        }

        bind.barLayout.barButtonFinish.setOnClickListener {
            setResult(1)
            val noticeTime = "${bind.factorTimePicker.hour}:${bind.factorTimePicker.minute}"
            val dayofMonth = this.mDayofMonth.toString()
            val month = this.mMonth.toString()
            val year = this.mYear.toString()
            val title = bind.factorEdittextTitle.text.toString()
            val noticeWay = bind.factorSpinner.selectedItem.toString()
            addNotice(dayofMonth,month,year,title,noticeWay,noticeTime)
            finish()
        }

    }

    private fun addNotice(
        dayofMonth : String,
        month : String,
        year : String,
        title : String,
        noticeWay : String,
        noticeTime : String
    ){
        val noticeDao = database.getNoticeDao()
        val notice : Notice = Notice(null,dayofMonth,month,year,title,noticeWay,noticeTime)
        GlobalScope.launch(Dispatchers.IO) {
            noticeDao.addNewNotice(notice)
        }
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