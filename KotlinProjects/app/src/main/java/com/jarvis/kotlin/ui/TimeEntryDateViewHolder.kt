package com.jarvis.kotlin.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import com.jarvis.kotlin.R

class TimeEntryDateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val checkBoxDate: CheckBox = view.findViewById(R.id.check_date)
}
