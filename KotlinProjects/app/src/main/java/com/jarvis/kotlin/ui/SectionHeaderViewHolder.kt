package com.jarvis.kotlin.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.jarvis.kotlin.R

class SectionHeaderViewHolder : RecyclerView.ViewHolder {
    val title: TextView

    constructor(view: View) : super(view) {
        title = view.findViewById(R.id.title)
    }
}

