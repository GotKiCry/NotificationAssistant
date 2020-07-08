package com.gotkicry.notificationassistant

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.gotkicry.notificationassistant.database.Database
import com.gotkicry.notificationassistant.database.Notice
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val TAG = "NoticeListAdapter";
class NoticeListAdapter constructor(private var list : List<Notice>, private var context : Context) : BaseAdapter() {

    class ViewHolder(view: View){
        val item_Date_Day : TextView = view.findViewById(R.id.item_date_day)
        val item_Date_Month : TextView = view.findViewById(R.id.item_date_month)
        val item_Detail_time : TextView = view.findViewById(R.id.item_detail_time)
        val item_Notice_Way : TextView = view.findViewById(R.id.item_notice_way)
        val item_Title : TextView = view.findViewById(R.id.item_title)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder : ViewHolder
        val view : View?
        if(convertView == null){
            view = LayoutInflater.from(context).inflate(R.layout.item_thing_info,parent,false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        }else{
            view = convertView
            viewHolder = view.tag as ViewHolder
        }
        val notice = list[position]
        viewHolder.item_Date_Day.text = "${notice.dayofMonth}"
        viewHolder.item_Date_Month.text = "日  ${notice.month}月"
        viewHolder.item_Detail_time.text = "${notice.noticeTime}"
        viewHolder.item_Notice_Way.text = "${notice.noticeWay}"
        viewHolder.item_Title.text = "${notice.title}"
        view?.setOnLongClickListener {
            GlobalScope.launch {
                val database = Database.getDatabase((context as Activity).application)
                database!!.getNoticeDao().delNotice(notice)
            }
            true
        }
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
}