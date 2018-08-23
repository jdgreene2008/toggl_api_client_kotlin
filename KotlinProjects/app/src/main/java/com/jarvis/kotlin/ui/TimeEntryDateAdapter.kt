package com.jarvis.kotlin.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v4.widget.CompoundButtonCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import com.jarvis.kotlin.R
import com.jarvis.kotlin.utils.DateUtils
import java.util.*

class TimeEntryDateAdapter(context: Context, dataset: List<Calendar>?) :
        RecyclerView.Adapter<TimeEntryDateViewHolder>() {
    private var mDataset = dataset
    private val mContext = context
    private val mSelectedDates = mutableListOf<Calendar>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeEntryDateViewHolder {
        return TimeEntryDateViewHolder(LayoutInflater.from(parent.context
        ).inflate(R.layout.time_entry_date_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return if (mDataset == null) {
            0
        } else {
            mDataset!!.size
        }
    }

    override fun onBindViewHolder(holder: TimeEntryDateViewHolder, position: Int) {
        holder.checkBoxDate.setText(DateUtils.getDisplayDate(mDataset!![position]))
        holder.checkBoxDate.typeface = Typeface.DEFAULT_BOLD
        prepareCheckBox(position, holder.checkBoxDate)
    }

    private fun prepareCheckBox(position: Int, checkbox: CheckBox) {
        val statesChecked = intArrayOf(android.R.attr.state_checked)
        val statesNormal = intArrayOf()

        val states = arrayOf<IntArray>(statesChecked, statesNormal)
        val colors = intArrayOf(ContextCompat.getColor(mContext, R.color.red),
                ContextCompat.getColor(mContext, R.color.list_item_odd))
        val checkedItem = mDataset!![position]
        CompoundButtonCompat.setButtonTintList(checkbox, ColorStateList(states, colors))
        checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (!mSelectedDates.contains(checkedItem)) {
                    mSelectedDates.add(checkedItem)
                }
            } else {
                mSelectedDates.remove(checkedItem)
            }
        }

        checkbox.isChecked = mSelectedDates.contains(checkedItem)
    }

    fun updateData(dataset: List<Calendar>) {
        mDataset = dataset
        mSelectedDates.clear()
        notifyDataSetChanged()
    }

    fun getSelectedDates():MutableList<Calendar>{
        return mSelectedDates
    }
}

