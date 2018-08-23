package com.jarvis.kotlin.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.jarvis.kotlin.R

class TogglEntryViewHolder : RecyclerView.ViewHolder {
    val statusIndicator: View?
    val description: TextView?
    val projectName: TextView?
    val duration: TextView?
    val startTime: TextView?
    val btnOptions: View?
    val btnSave: Button?
    val btnEdit: Button?
    val btnDelete: Button?
    val date: TextView?
    val titleDuration: TextView?
    val statusBar: View?
    val projectColor: View?

    constructor(view: View) : super(view) {
        statusIndicator = view.findViewById(R.id.status_indicator)
        description = view.findViewById(R.id.description)
        projectName = view.findViewById(R.id.project_name)
        duration = view.findViewById(R.id.duration)
        startTime = view.findViewById(R.id.start_time)
        btnSave = view.findViewById(R.id.btn_save)
        btnEdit = view.findViewById(R.id.btn_edit)
        btnDelete = view.findViewById(R.id.btn_delete)
        // Toggl Alternate
        btnOptions = view.findViewById(R.id.btn_show_options)
        date = view.findViewById(R.id.label_date)
        titleDuration = view.findViewById(R.id.title_duration)
        statusBar = view.findViewById(R.id.status_bar)
        projectColor = view.findViewById(R.id.project_color)
    }
}
