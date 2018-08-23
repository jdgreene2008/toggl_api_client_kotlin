package com.jarvis.kotlin.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import com.jarvis.kotlin.R

class TogglTimeEntriesHeaderViewHolder : RecyclerView.ViewHolder {

    val btnCreate: Button
    val btnSaveAll: Button

    constructor(itemView: View) : super(itemView) {
        btnCreate = itemView.findViewById(R.id.btn_create)
        btnSaveAll = itemView.findViewById(R.id.btn_save_all)
    }

}

