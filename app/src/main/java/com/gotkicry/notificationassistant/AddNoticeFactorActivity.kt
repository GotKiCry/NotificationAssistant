package com.gotkicry.notificationassistant

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.gotkicry.notificationassistant.database.Database
import com.gotkicry.notificationassistant.database.Notice
import com.gotkicry.notificationassistant.databinding.ActivityAddNoticeFactorBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.util.*

private const val TAG = "AddNoticeFactorActivity";
class AddNoticeFactorActivity : AppCompatActivity() {
    private lateinit var bind : ActivityAddNoticeFactorBinding
    private lateinit var database: Database
    private var tagTime : Long = 0
    private var mYear: Int = 0
    private var mDayofMonth: Int = 0
    private var mMonth: Int = 0
    private var id : Int? = null
    private var lastNoticeWay = 0
    var eventsID : Long? = null
    private var isAdd = true
    private lateinit var noticeFunction: NoticeFunction
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
        noticeFunction = NoticeFunction(this)
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
        eventsID = oneNotice.eventsID
        bind.factorEdittextTitle.setText(oneNotice.title)
        mYear = oneNotice.year
        mMonth = oneNotice.month
        mDayofMonth = oneNotice.dayofMonth
        lastNoticeWay = oneNotice.noticeWay
        bind.factorSpinner.setSelection(oneNotice.noticeWay)
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
            getString(R.string.title_add)
        }else{
            getString(R.string.title_update)
        }

        bind.barLayout.barButtonFinish.setOnClickListener {
            val hour = "${bind.factorTimePicker.hour}"
            val min = if(bind.factorTimePicker.minute < 10){
                "0${bind.factorTimePicker.minute}"
            }else{
                "${bind.factorTimePicker.minute}"
            }
            val noticeTime = "$hour:$min"
            val dayofMonth = this.mDayofMonth
            val month = this.mMonth
            val year = this.mYear
            val title = bind.factorEdittextTitle.text.toString()
            if(title.isEmpty()){
                Toast.makeText(this,getString(R.string.title_waring),Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val noticeWay = bind.factorSpinner.selectedItemPosition
            Log.d(TAG, "initBar: $noticeWay")
            var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm")
            val strings = "$year-$month-$dayofMonth ${bind.factorTimePicker.hour}:${bind.factorTimePicker.minute}"
            val date = simpleDateFormat.parse(strings).time
            if(Date().time > date){
                Toast.makeText(this,getString(R.string.addOrUpDate_Waring),Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            tagTime = noticeFunction.getTagTime(mYear, mMonth, mDayofMonth, hour.toInt(), min.toInt())
            if(noticeWay == 0){
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),1)
                    return@setOnClickListener
                }
            }
            Log.d(TAG, "initBar: 执行了一次")
            addOrUpdate(id,dayofMonth,month,year,title,noticeWay,noticeTime,date,eventsID)
            finish()
        }

    }

    private fun addOrUpdate(id : Int?,
        dayofMonth: Int,
        month: Int,
        year: Int,
        title: String,
        noticeWay: Int,
        noticeTime: String,
        date: Long,
        eventsID : Long?
    ){


        GlobalScope.launch(Dispatchers.IO) {
            val notice : Notice = Notice(id,dayofMonth,month,year,title,noticeWay,noticeTime,date,eventsID)
            setMission(notice)
        }
    }

    suspend fun setMission(notice: Notice){
        val noticeDao = database.getNoticeDao()
        val calendarFunction = CalendarFunction(this)
        Log.d(TAG, "setMission: isAdd : $isAdd")
        if(isAdd){
            if(notice.noticeWay == 0){
                notice.eventsID = calendarFunction.addCalendar(notice.title,notice.date)
            }
            noticeDao.addNewNotice(notice)
            canAddAlarmManager(noticeDao.getLastID(),notice,tagTime)
        }else{
            if(lastNoticeWay == notice.noticeWay){
                when(notice.noticeWay){
                    0-> calendarFunction.updateCalendar(notice.eventsID!!,notice.title,notice.date)
                    1-> return
                }
            }else{
                when(notice.noticeWay){
                    0-> {
                        notice.eventsID = calendarFunction.addCalendar(notice.title,notice.date)
                        noticeFunction.cancelAlarmManager(notice.id!!,lastNoticeWay)
                    }
                    1-> {
                        calendarFunction.delCalendar(notice.eventsID!!)
                        notice.eventsID = null
                        noticeFunction.cancelAlarmManager(notice.id!!,lastNoticeWay)
                    }
                }
            }
            noticeDao.updateNotice(notice)
            canAddAlarmManager(notice.id!!,notice,tagTime)
        }
    }

    private fun canAddAlarmManager(id : Int , notice: Notice , tagTime : Long){
        noticeFunction.cancelAlarmManager(id,notice.noticeWay)
        noticeFunction.addAlarmManager(id,tagTime,notice)
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