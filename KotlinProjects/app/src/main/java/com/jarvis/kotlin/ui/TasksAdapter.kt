package com.jarvis.kotlin.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.jarvis.kotlin.R
import com.jarvis.kotlin.domain.toggl.Task

class TasksAdapter(context: Context, layoutId: Int, list: List<Task>) :
        ArrayAdapter<Task>(context, layoutId, list) {

    private val mData: List<Task> = list
    private val mDummyTask = Task("<No Task>",0,0,
            0,0,0,false,"",0)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = super.getView(position, convertView, parent)
        if (position == 0) {
            view.findViewById<TextView>(android.R.id.text1).setText("<No Task>")
        } else {
            view.findViewById<TextView>(android.R.id.text1).setText(mData[position - 1].name)
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = super.getDropDownView(position, convertView, parent)
        if (position == 0) {
            view.findViewById<TextView>(android.R.id.text1).setText("<No Task>")
        } else {
            view.findViewById<TextView>(android.R.id.text1).setText(mData[position - 1].name)
        }
        return view
    }

    override fun getCount(): Int {
        return mData.size + 1
    }

    override fun getItem(position: Int): Task {
        if(position == 0){
            return mDummyTask
        }
        return super.getItem(position - 1)
    }

    fun getTaskAdapterPosition(taskId: Int): Int {
        for (task in mData) {
            if (task.id == taskId)
                return mData.indexOf(task)
        }

        return Task.INVALID_TASK_ID
    }
}
