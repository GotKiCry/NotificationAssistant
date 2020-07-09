package com.gotkicry.notificationassistant

import android.app.DatePickerDialog
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.gotkicry.notificationassistant.database.Database
import com.gotkicry.notificationassistant.database.Notice
import com.gotkicry.notificationassistant.databinding.ActivityAddNoticeFactorBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "AddNoticeFactorActivity";
class AddNoticeFactorActivity : AppCompatActivity() {
    private lateinit var bind : ActivityAddNoticeFactorBinding
    private lateinit var database: Database

    private var mYear: Int = 0
    private var mDayofMonth: Int = 0
    private var mMonth: Int = 0
    private var id : Int? = null
    private var isAdd = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityAddNoticeFactorBinding.inflate(layoutInflater)
        setContentView(bind.root)

        isAdd = intent.getBooleanExtra("isAdd", true)
        if(isAdd){
            addNewNotice()
        }else{
            updateNotice()
        }
        init()
    }

    private fun init() {
        //初始化数据库连接
        database = Database.getDatabase(application)!!
        addSystemBarSpace()
        initBar()
    }

    private fun addNewNotice() {
        val calendar = Calendar.getInstance()
        mYear = calendar.get(Calendar.YEAR)
        mMonth = calendar.get(Calendar.MONTH)+1
        mDayofMonth = calendar.get(Calendar.DAY_OF_MONTH)
        initMainView()
    }

    private fun updateNotice() {
        var oneNotice : Notice
        GlobalScope.launch {
            oneNotice = database.getNoticeDao().getOneNotice(intent.getIntExtra("noticeId", 1))
            initUIdata(oneNotice)
        }
    }

    private fun initUIdata(oneNotice : Notice) {
        id = oneNotice.id
        bind.factorEdittextTitle.setText(oneNotice.title)
        mYear = oneNotice.year.toInt()
        mMonth = oneNotice.month.toInt()
        mDayofMonth = oneNotice.dayofMonth.toInt()
        if(oneNotice.noticeWay=="日历"){
            bind.factorSpinner.setSelection(0)
        }else{
            bind.factorSpinner.setSelection(1)
        }
        val split = oneNotice.noticeTime.split(":")
        bind.factorTimePicker.hour = split[0].toInt()
        bind.factorTimePicker.minute = split[1].toInt()
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
            finish()
        }
        bind.barLayout.barTextTitle.text = if(isAdd){
            "添加"
        }else{
            "修改"
        }

        bind.barLayout.barButtonFinish.setOnClickListener {
            val hour = "${bind.factorTimePicker.hour}"
            val min = if(bind.factorTimePicker.minute < 10){
                "0${bind.factorTimePicker.minute}"
            }else{
                "${bind.factorTimePicker.minute}"
            }
            val noticeTime = "$hour:$min"
            val dayofMonth = this.mDayofMonth.toString()
            val month = this.mMonth.toString()
            val year = this.mYear.toString()
            val title = bind.factorEdittextTitle.text.toString()
            if(title.isEmpty()){
                Toast.makeText(this,getString(R.string.title_waring),Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val noticeWay = bind.factorSpinner.selectedItem.toString()
            var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm")
            val strings = "$year-$month-$dayofMonth ${bind.factorTimePicker.hour}:${bind.factorTimePicker.minute}"
            val date = simpleDateFormat.parse(strings).time
            if(Date().time > date){
                Toast.makeText(this,getString(R.string.addOrUpDate_Waring),Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            addOrUpdate(id,dayofMonth,month,year,title,noticeWay,noticeTime,date)
            finish()
        }

    }

    private fun addOrUpdate(id : Int?,
        dayofMonth: String,
        month: String,
        year: String,
        title: String,
        noticeWay: String,
        noticeTime: String,
        date: Long
    ){
        val noticeDao = database.getNoticeDao()
        val notice : Notice = Notice(id,dayofMonth,month,year,title,noticeWay,noticeTime,date)
        GlobalScope.launch(Dispatchers.IO) {
            if(isAdd){
                noticeDao.addNewNotice(notice)
            }else{
                noticeDao.updateNotice(notice)
            }
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