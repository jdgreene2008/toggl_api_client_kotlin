package com.jarvis.kotlin.ui

import TogglProfileHeaderViewHolder
import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jarvis.kotlin.R
import com.jarvis.kotlin.constants.TimeEntryState
import com.jarvis.kotlin.domain.toggl.TimeEntry
import com.jarvis.kotlin.domain.toggl.User
import com.jarvis.kotlin.ui.fragment.TimeEntriesFragment
import com.jarvis.kotlin.utils.DateUtils
import com.jarvis.kotlin.utils.TogglUtils
import java.util.*

@Deprecated("Replaced with TimeEntryAdapterAlternate")
class TimeEntriesAdapter(context: Context, dataset: MutableList<TimeEntryDisplayModel>?, user: User?,
                         listener: TimeEntriesFragment.OnTimeEntryApiActionListener, sectioned: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mDataset: MutableList<TimeEntryDisplayModel>? = dataset
    private var mUser = user
    private var mContext: Context = context
    private var mTimeEntryActionListener = listener
    private var mSections = mutableListOf<TimeEntrySection>()
    private var mSectioned = sectioned

    init {
        if (mSectioned)
            calculateSections(mDataset)
    }

    constructor(context: Context, dataset: MutableList<TimeEntryDisplayModel>?, user: User?,
                listener: TimeEntriesFragment.OnTimeEntryApiActionListener) : this(context, dataset, user, listener, true)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            0 -> return TogglProfileHeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.toggl_profile_header,
                    parent, false))
            1 -> return TogglTimeEntriesHeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.toggl_time_entries_header,
                    parent, false))
            2 -> return TogglEntryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.toggl_time_entry, parent,
                    false))
            else -> return SectionHeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.section_title, parent,
                    false))
        }
    }

    override fun getItemCount(): Int {
        return if (mDataset == null || mDataset?.isEmpty()!!) {
            2
        } else {
            mDataset!!.size + 2 + mSections?.size
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)

        when (viewType) {
            0 -> populateProfileHeader(holder as TogglProfileHeaderViewHolder)
            1 -> setupTimeEntriesHeader(holder as TogglTimeEntriesHeaderViewHolder)
            2 -> {
                populateTimeEntryData(getTimeEntryPosition(position), holder as TogglEntryViewHolder)
            }
            3 -> {
                populateTimeEntrySectionHeader(getSectionForPosition(position)!!, holder as SectionHeaderViewHolder)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        when (position) {
            0 -> return VIEW_TYPE_PROFILE_HEADER
            1 -> return VIEW_TYPE_TIME_ENTRIES_HEADER
            else -> return getHeaderOrEntryViewType(position)
        }
    }

    private fun getHeaderOrEntryViewType(adapterPosition: Int): Int {
        if (isSectionHeader(adapterPosition))
            return VIEW_TYPE_SECTION_HEADER
        else return VIEW_TYPE_TIME_ENTRY
    }

    private fun isSectionHeader(adapterPosition: Int): Boolean {
        if (mSections == null || mSections.isEmpty()) {
            return false
        }

        for (section in mSections) {
            if (section.adapterPosition == adapterPosition) return true
        }

        return false
    }

    private fun sortDataset(dataset: MutableList<TimeEntryDisplayModel>?) {
        dataset!!.sortWith(Comparator<TimeEntryDisplayModel> { o1, o2 ->
            val o1Time = DateUtils.parseApiDate(o1!!.entry.startTime!!)
            val o2time = DateUtils.parseApiDate(o2!!.entry.startTime!!)
            -1 * (o1Time!!.compareTo(o2time))
        })
    }

    private fun calculateSections(dataset: MutableList<TimeEntryDisplayModel>?) {
        mSections = mutableListOf()
        sortDataset(dataset)
        val basePosition = 2
        var offset = 0
        var currentDay = -1
        if (dataset!!.isNotEmpty()) {
            for (item in dataset) {
                val startTime = item.entry.startTime
                Log.d(TAG, "api StartTime->" + startTime)
                val time = DateUtils.parseApiDate(item.entry.startTime!!)
                if (time!!.get(Calendar.DAY_OF_YEAR) != currentDay) {
                    mSections.add(createSection(time, basePosition + offset))
                    currentDay = time.get(Calendar.DAY_OF_YEAR)
                    offset++
                }
                offset++
            }
        }
    }

    private fun createSection(date: Calendar, position: Int): TimeEntrySection {
        return TimeEntrySection(DateUtils.getDisplayDate(date)!!, position)
    }

    private fun getSectionForPosition(adapterPosition: Int): TimeEntrySection? {
        if (mSections == null || mSections.isEmpty()) return null

        var foundSection: TimeEntrySection? = null
        for (section in mSections) {
            if (section.adapterPosition == adapterPosition) {
                return section
            }
        }

        return foundSection
    }

    private fun getTimeEntryPosition(adapterPosition: Int): Int {
        var sectionOffset = 2
        if (mSections == null || mSections.isEmpty()) {
        }
        for (section in mSections) {
            if (section.adapterPosition < adapterPosition) {
                sectionOffset++
            }
        }

        return adapterPosition - sectionOffset
    }


    private fun populateProfileHeader(holder: TogglProfileHeaderViewHolder) {
        holder.email.setText(mUser!!.email)
        holder.fullname.setText(mUser!!.fullname)
        holder.todaysDate.setText(DateUtils.getDisplayDateTime(Calendar.getInstance()))

        if (mUser!!.newBlogPost != null) {
            var blogText = mUser!!.newBlogPost.title
            blogText = blogText.plus(": " + mUser!!.newBlogPost.url)
            holder.blogPost.setText(blogText)
            holder.blogPost.setLinkTextColor(ContextCompat.getColor(mContext, android.R.color.holo_blue_dark))
            Linkify.addLinks(holder.blogPost, Linkify.ALL)
        }
    }

    private fun setupTimeEntriesHeader(holder: TogglTimeEntriesHeaderViewHolder) {
        holder.btnCreate.setOnClickListener {
            mTimeEntryActionListener?.onCreateNew(mUser)
        }
        holder.btnSaveAll.setOnClickListener {
            val unsavedChanges = getUnsavedChanges()
            if (unsavedChanges != null)
                mTimeEntryActionListener?.onSaveAll(unsavedChanges)
        }
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

    private fun populateTimeEntrySectionHeader(section: TimeEntrySection, holder: SectionHeaderViewHolder) {
        holder.title.setText(section.title)
    }

    private fun prepareUnsavedEntryForSave(entry: TimeEntry) {
        entry.createdWith = "android app"
        entry.stopTime = null
    }

    private fun populateTimeEntryData(dataListPosition: Int, holder: TogglEntryViewHolder) {
        val data = mDataset!![dataListPosition]
        val timeEntry = data.entry
        holder.description!!.setText(timeEntry.description)

        val task = TogglUtils.getTask(timeEntry.projectId, timeEntry.taskId, mUser!!)
        val projectName = TogglUtils.getUserProjectName(timeEntry.projectId, mUser)
        if (task != null) {
            holder.projectName!!.setText(String.format(mContext.getString(R.string.toggl_description_format), projectName, task.name))
        } else {
            holder.projectName!!.setText(projectName)
        }

        if (data.state == TimeEntryState.Saved) {
            holder.statusIndicator!!.visibility = View.INVISIBLE
        } else {
            holder.statusIndicator!!.visibility = View.VISIBLE
            holder.statusIndicator!!.setBackgroundColor(getStatusIndicatorColor(data))
        }
        holder.duration!!.setText(DateUtils.getDisplayDuration(timeEntry.duration))
        holder.startTime!!.setText(DateUtils.getDisplayDateTime(timeEntry.startTime))
        val color = TogglUtils.getUserProjectColor(timeEntry.projectId, mUser)
        if (color != null) {
            holder.projectName.setTextColor(Color.parseColor(color))
        }
        displayButtons(dataListPosition, holder)
    }

    private fun displayButtons(dataListPosition: Int, holder: TogglEntryViewHolder) {
        val data = mDataset!![dataListPosition]
        when (data?.state) {
            TimeEntryState.Unsaved -> {
                holder.btnSave!!.visibility = View.VISIBLE
                holder.btnEdit!!.visibility = View.VISIBLE
                holder.btnDelete!!.visibility = View.VISIBLE
            }
            TimeEntryState.Saved -> {
                holder.btnSave!!.visibility = View.GONE
                holder.btnEdit!!.visibility = View.VISIBLE
                holder.btnDelete!!.visibility = View.VISIBLE
            }
        }

        holder.btnEdit.setOnClickListener {
            mTimeEntryActionListener.onEdit(data.entry, dataListPosition, mUser)
        }
        holder.btnSave.setOnClickListener {
            prepareUnsavedEntryForSave(data.entry)
            mTimeEntryActionListener.onSave(data.entry, dataListPosition)
        }
        holder.btnDelete.setOnClickListener {
            if (data.state == TimeEntryState.Unsaved) {
                removeAt(dataListPosition)
            } else {
                mTimeEntryActionListener.onDelete(data.entry, dataListPosition)
            }
        }
    }

    private fun getStatusIndicatorColor(entry: TimeEntryDisplayModel?): Int {
        var colorId = 0
        when (entry?.state) {
            TimeEntryState.Unsaved -> {
                colorId = R.color.toggl_status_unsaved
            }
        }

        return ContextCompat.getColor(mContext, colorId)
    }

    fun removeAt(datasetPosition: Int) {
        mDataset?.removeAt(datasetPosition)
        calculateSections(mDataset)
        notifyDataSetChanged()
    }

    companion object {
        private val TAG = TimeEntriesAdapter::class.java.name
        // This position will force the dataset list to be re-sorted when a new time entry is added.
        val INVALID_POSITION = -1
        private val VIEW_TYPE_PROFILE_HEADER = 0
        private val VIEW_TYPE_TIME_ENTRIES_HEADER = 1
        private val VIEW_TYPE_TIME_ENTRY = 2
        private val VIEW_TYPE_SECTION_HEADER = 3
    }
}
