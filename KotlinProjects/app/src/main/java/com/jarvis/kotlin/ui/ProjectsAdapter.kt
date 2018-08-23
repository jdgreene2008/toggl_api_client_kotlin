package com.jarvis.kotlin.ui

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.jarvis.kotlin.R
import com.jarvis.kotlin.domain.toggl.Project

class ProjectsAdapter : ArrayAdapter<Project> {

    private val mData: List<Project>
    private val mContext: Context

    constructor(context: Context, layoutId: Int, textViewResId: Int, list: List<Project>) : super(context, layoutId, textViewResId, list) {
        mData = list
        mContext = context
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = super.getView(position, convertView, parent)
        view.findViewById<TextView>(R.id.text1).setText(mData[position].name)
        val projectColor = view.findViewById<View>(R.id.project_color)
        projectColor.setBackgroundColor(Color.parseColor(getProjectColor(mData[position].id)))
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = super.getDropDownView(position, convertView, parent)
        view.findViewById<TextView>(R.id.text1).setText(mData[position].name)
        val projectColor = view.findViewById<View>(R.id.project_color)
        projectColor.setBackgroundColor(Color.parseColor(getProjectColor(mData[position].id)))
        return view
    }


    fun getProjectAdapterPosition(projectId: Int): Int {
        for (project in mData) {
            if (project.id == projectId)
                return mData.indexOf(project)
        }

        return -1
    }


    private fun getProjectColor(id: Int): String? {
        for (project in mData) {
            if (project.id == id) {
                return project.hexColor
            }
        }

        return null
    }

}
