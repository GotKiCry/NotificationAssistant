package com.gotkicry.notificationassistant

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.gotkicry.notificationassistant.database.Database
import com.gotkicry.notificationassistant.database.Notice
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class NoticeListAdapter constructor(private var list: List<Notice>, private var context: Context) :
    BaseAdapter() {
    val noticeFunction: NoticeFunction = NoticeFunction(context)

    class ViewHolder(var view: View) {
        val item_Date_Day: TextView = view.findViewById(R.id.item_date_day)
        val item_Date_Month: TextView = view.findViewById(R.id.item_date_month)
        val item_Detail_time: TextView = view.findViewById(R.id.item_detail_time)
        val item_Notice_Way: TextView = view.findViewById(R.id.item_notice_way)
        val item_Title: TextView = view.findViewById(R.id.item_title)
        var isOldTime = false
        fun setFinishColor(context: Context) {
            val color = ContextCompat.getColor(context, R.color.item_finish)
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.item_finish_backgroud))
            item_Date_Day.setTextColor(color)
            item_Date_Month.setTextColor(color)
            item_Detail_time.setTextColor(color)
            item_Notice_Way.setTextColor(color)
            item_Title.setTextColor(color)
        }

        fun setNotFinishColor(context: Context) {
            val black = ContextCompat.getColor(context, R.color.item_not_finish_black)
            val gray = ContextCompat.getColor(context, R.color.item_not_finish_gray)
            view.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.item_not_finish_backgroud
                )
            )
            item_Date_Day.setTextColor(black)
            item_Date_Month.setTextColor(gray)
            item_Detail_time.setTextColor(gray)
            item_Notice_Way.setTextColor(gray)
            item_Title.setTextColor(black)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: ViewHolder
        val view: View?
        val notice = list[position]
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_thing_info, parent, false)
            viewHolder = ViewHolder(view)
            viewHolder.item_Title.tag = position
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        viewHolder.item_Date_Day.text = notice.dayofMonth.toString()
        viewHolder.item_Date_Month.text =
            "${context.getString(R.string.day)}  ${context.resources.getStringArray(R.array.month_)[(notice.month).toInt() - 1]}"
        viewHolder.item_Detail_time.text = notice.noticeTime
        viewHolder.item_Notice_Way.text = when (notice.noticeWay) {
            0 -> context.resources.getStringArray(R.array.factor_array)[0]
            1 -> context.resources.getStringArray(R.array.factor_array)[1]
            else -> "Error"
        }
        viewHolder.item_Title.text = notice.title
        //弹出菜单 待替换PopupWindow
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.item_pop_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.pop_update -> {
                    val intent = Intent(context, AddNoticeFactorActivity::class.java)
                    intent.putExtra("isAdd", false)
                    intent.putExtra("noticeId", notice.id)
                    context.startActivity(intent)
                }
                R.id.pop_del -> {
                    GlobalScope.launch {
                        Database.getDatabase((context as MainActivity).application)!!.getNoticeDao()
                            .delNotice(notice)
                        NoticeFunction(context).cancelAlarmManager(notice.id!!, notice.noticeWay)
                        if (notice.noticeWay == 0) {
                            CalendarFunction(context).delCalendar(notice.eventsID!!)
                        }
                    }
                }
                else -> Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            }
            true
        }

        view?.setOnLongClickListener {
            popupMenu.show()

            true
        }

        changeColor(viewHolder, notice)

        return view as View
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }

    private fun changeColor(viewHolder: ViewHolder, notice: Notice) {
        val result = Date().time - notice.date
        viewHolder.isOldTime = result > 0
        if (viewHolder.isOldTime) {
            viewHolder.setFinishColor(context)
        } else {
            viewHolder.setNotFinishColor(context)
        }
    }
}