package com.jarvis.kotlin.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v4.widget.CompoundButtonCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jarvis.kotlin.R
import com.jarvis.kotlin.constants.TimeEntryState
import com.jarvis.kotlin.domain.toggl.TimeEntry
import com.jarvis.kotlin.domain.toggl.User
import com.jarvis.kotlin.ui.fragment.TimeEntriesFragment
import com.jarvis.kotlin.utils.DateUtils
import com.jarvis.kotlin.utils.TextHelper
import com.jarvis.kotlin.utils.TogglUtils
import java.util.*

class AlternateTimeEntriesAdapter(context: Context, dataset: MutableList<TimeEntryDisplayModel>?, user: User?,
                                  listener: TimeEntriesFragment.OnTimeEntryApiActionListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mDataset: MutableList<TimeEntryDisplayModel>? = dataset
    private var mUser = user
    private var mContext: Context = context
    private var mTimeEntryActionListener = listener

    init {
        sortDataset(mDataset)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TogglEntryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.alternate_toggl_time_entry, parent,
                false))
    }

    override fun getItemCount(): Int {
        return if (mDataset == null || mDataset?.isEmpty()!!) {
            0
        } else {
            mDataset!!.size
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        populateTimeEntryData(position, holder as TogglEntryViewHolder)
    }


    private fun sortDataset(dataset: MutableList<TimeEntryDisplayModel>?) {
        dataset!!.sortWith(Comparator<TimeEntryDisplayModel> { o1, o2 ->
            val o1Time = DateUtils.parseApiDate(o1!!.entry.startTime!!)
            val o2time = DateUtils.parseApiDate(o2!!.entry.startTime!!)
            -1 * (o1Time!!.compareTo(o2time))
        })
    }

    fun saveAll() {
        val unsavedChanges = getUnsavedChanges()
        if (unsavedChanges != null)
            mTimeEntryActionListener?.onSaveAll(unsavedChanges)
    }

    private fun getUnsavedChanges(): MutableList<TimeEntry>? {
        if (mDataset != null) {
            val filteredData = mDataset?.filter {
                it.state == TimeEntryState.Unsaved
            }
            if (filteredData != null && !filteredData.isEmpty()) {
                val unsavedTimeEntries = mutableListOf<TimeEntry>()
                for (result in filteredData) {
                    prepareUnsavedEntryForSave(result.entry)
                    unsavedTimeEntries.add(result.entry)
                }
                return unsavedTimeEntries
            }
        }

        return null
    }

    private fun prepareUnsavedEntryForSave(entry: TimeEntry) {
        entry.createdWith = "android app"
        entry.stopTime = null
    }

    private fun populateTimeEntryData(dataListPosition: Int, holder: TogglEntryViewHolder) {
        val data = mDataset!![dataListPosition]
        val timeEntry = data.entry
        holder.description!!.setText(timeEntry.description)

        val projectName = TogglUtils.getUserProjectName(timeEntry.projectId, mUser)
        val task = TogglUtils.getTask(timeEntry.projectId, timeEntry.taskId, mUser!!)
        if (task != null && projectName != null) {
            holder.projectName!!.setText(TextHelper.applyBoldSpan(
                    projectName + ": " + task.name, projectName?.length + 1))
        } else {
            holder.projectName!!.setText(projectName)
        }
        if (data.state == TimeEntryState.Saved) {
            holder.statusBar!!.visibility = View.INVISIBLE
        } else {
            holder.statusBar!!.visibility = View.VISIBLE
            holder.statusBar.setBackgroundColor(getStatusIndicatorColor(data))
        }
        holder.duration!!.setText(DateUtils.getDisplayDuration(timeEntry.duration))
        holder.duration!!.setTextColor(getStatusIndicatorColor(data))
        holder.titleDuration!!.setTextColor(getStatusIndicatorColor(data))
        holder.titleDuration!!.setText(DateUtils.getDisplayDuration(timeEntry.duration))
        holder.date!!.setText(DateUtils.getDisplayDate(DateUtils.parseApiDate(timeEntry.startTime!!)!!))
        val color = TogglUtils.getUserProjectColor(timeEntry.projectId, mUser)
        if (color != null) {
            holder.projectColor!!.setBackgroundColor(Color.parseColor(color))
        }

        val statesSelected = intArrayOf(android.R.attr.state_selected)
        val statesNormal = intArrayOf()
        val states = arrayOf<IntArray>(statesSelected, statesNormal)
        val colors = intArrayOf(ContextCompat.getColor(mContext, R.color.toggl_action_selected),
                ContextCompat.getColor(mContext, R.color.toggl_action_unselected))
        ViewCompat.setBackgroundTintList(holder.btnOptions, ColorStateList(states, colors))
        ViewCompat.setBackgroundTintMode(holder.btnOptions, PorterDuff.Mode.SRC_ATOP)

        holder.btnOptions!!.setOnClickListener {
            it.isSelected = true
            mTimeEntryActionListener.onShowTimeEntryOptions(data, dataListPosition, mUser, it)
        }
    }

    private fun getStatusIndicatorColor(entry: TimeEntryDisplayModel?): Int {
        var colorId = 0
        when (entry?.state) {
            TimeEntryState.Unsaved -> {
                colorId = R.color.toggl_status_unsaved
            }
            TimeEntryState.Saved -> {
                colorId = R.color.toggl_status_saved
            }
        }

        return ContextCompat.getColor(mContext, colorId)
    }

    fun createNewEntry() {
        mTimeEntryActionListener.onCreateNew(mUser)
    }


    fun deleteEntry(timeEntryModel: TimeEntryDisplayModel, adapterPosition: Int) {
        if (timeEntryModel.state == TimeEntryState.Unsaved) {
            removeAt(adapterPosition)
        } else {
            mTimeEntryActionListener.onDelete(timeEntryModel.entry, adapterPosition)
        }
    }


    /**
     * Request that a time entry be saved.
     */
    fun saveEntry(entry: TimeEntry, adapterPosition: Int) {
        prepareUnsavedEntryForSave(entry)
        mTimeEntryActionListener.onSave(entry, adapterPosition)
    }

    fun commitSaveEntry(datasetPosition: Int?, timeEntryDisplayModel: TimeEntryDisplayModel) {
        if (mDataset == null) mDataset = mutableListOf()

        if (datasetPosition != null && datasetPosition >= 0 && datasetPosition < mDataset!!.size) {
            mDataset!!.removeAt(datasetPosition)
            mDataset!!.add(datasetPosition, timeEntryDisplayModel)
        } else {
            mDataset!!.add(timeEntryDisplayModel)
        }
        sortDataset(mDataset)
        notifyDataSetChanged()
    }

    fun removeAt(datasetPosition: Int) {
        mDataset?.removeAt(datasetPosition)
        notifyDataSetChanged()
    }

    companion object {
        private val TAG = TimeEntriesAdapter::class.java.name
        // This position will force the dataset list to be re-sorted when a new time entry is added.
        val INVALID_POSITION = -1
    }
}
