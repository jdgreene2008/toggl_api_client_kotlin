package com.jarvis.kotlin.ui.fragment


import android.app.DatePickerDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast

import com.jarvis.kotlin.R
import com.jarvis.kotlin.domain.toggl.Project
import com.jarvis.kotlin.domain.toggl.Task
import com.jarvis.kotlin.domain.toggl.TimeEntry
import com.jarvis.kotlin.domain.toggl.User
import com.jarvis.kotlin.ui.ProjectsAdapter
import com.jarvis.kotlin.ui.TasksAdapter
import com.jarvis.kotlin.ui.TimeEntriesAdapter
import com.jarvis.kotlin.ui.TimeEntryDateAdapter
import com.jarvis.kotlin.utils.DateUtils
import kotlinx.android.synthetic.main.fragment_edit_time_entry.*
import java.util.*
import kotlin.collections.HashSet

private const val ARG_USER = "user"
private const val ARG_TIME_ENTRY = "time_entry"
private const val ARG_ADAPTER_POSITION = "adapter_position"


class EditTimeEntryFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var mUser: User? = null
    private var mTimeEntry: TimeEntry? = null
    private var mProjects: List<Project>? = listOf()
    private var mTaskId: Int = INVALID_TASK_ID
    private var mProjectsAdapterPosition: Int? = null
    private var mListener: TimeEntryListener? = null
    private var mDatePickerTime: Calendar? = null
    private var mStartDateModified: Boolean = false
    private var mDateAdapter: TimeEntryDateAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mTimeEntry = it.getParcelable(ARG_TIME_ENTRY)
            mUser = it.getParcelable(ARG_USER)
            if (it.containsKey(ARG_ADAPTER_POSITION)) {
                mProjectsAdapterPosition = it.getInt(ARG_ADAPTER_POSITION)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_time_entry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    private fun setupUi() {
        btn_cancel.setOnClickListener {
            mListener?.onCancel()
        }
        btn_edit_date.setOnClickListener {
            showDatePickerDialog()
        }

        showMultiDateSelectionView(isNewTimeEntry(mTimeEntry))
        btn_save.setOnClickListener {
            if (isNewTimeEntry(mTimeEntry) || mStartDateModified) {
                createNewTimeEntry()
            } else {
                editExistingTimeEntry()
            }
        }

        displayUserData(mUser)

        if (mTimeEntry != null) {
            displayTimeEntryData(mTimeEntry)
        } else {
            start_time.setText(DateUtils.getDisplayDate(Calendar.getInstance()))
        }
    }

    private fun displayUserData(user: User?) {
        mProjects = user?.projects
        projects.apply {
            adapter = ProjectsAdapter(context!!, R.layout.projects_spinner_item, R.id.text1, mProjects!!)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Unused
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    handleProjectSelected(mProjects!![position])
                }

            }
        }
        if (mTimeEntry != null) {
            projects.setSelection((projects.adapter as ProjectsAdapter).getProjectAdapterPosition(mTimeEntry!!.projectId))
        }
        if (user?.timeEntries != null && user!!.timeEntries.isNotEmpty()) {
            populateAutoCompleteEntries(user!!)
        }
    }

    private fun populateAutoCompleteEntries(user: User) {
        val uniqueDescriptions: MutableSet<String> = mutableSetOf()
        for (entry in user.timeEntries) {
            if (!TextUtils.isEmpty(entry.description)) {
                uniqueDescriptions.add(entry.description!!)
            }
        }
        val descriptionsAdapter: ArrayAdapter<String> = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item,
                uniqueDescriptions.filter {
                    true
                })
        description.setAdapter(descriptionsAdapter)
        description.threshold = 3
    }

    private fun showMultiDateSelectionView(show: Boolean) {
        if (!show) {
            divider_date_multi_select.visibility = View.GONE
            label_apply_multiple_days.visibility = View.GONE
            list_dates_multi_select.visibility = View.GONE
        } else {
            mDateAdapter = TimeEntryDateAdapter(context!!, getMultiSelectVisibleDays())
            val manager = LinearLayoutManager(context!!, RecyclerView.HORIZONTAL, false)
            list_dates_multi_select.apply {
                adapter = mDateAdapter
                layoutManager = manager
            }
        }
    }

    private fun refreshMultiSelectVisibleDays() {
        if (list_dates_multi_select.visibility == View.VISIBLE) {
            mDateAdapter!!.updateData(getMultiSelectVisibleDays())
        }
    }

    private fun getMultiSelectVisibleDays(): List<Calendar> {
        val multiSelectDays = DateUtils.getPayPeriodDays()
        val dayOfYear = if (mDatePickerTime != null) {
            mDatePickerTime!!.get(Calendar.DAY_OF_YEAR)
        } else if (mTimeEntry != null && !TextUtils.isEmpty(mTimeEntry!!.startTime)) {
            DateUtils.parseApiDate(mTimeEntry!!.startTime!!)!!.get(Calendar.DAY_OF_YEAR)
        } else {
            Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        }

        return multiSelectDays.filter {
            it.get(Calendar.DAY_OF_YEAR) != dayOfYear
        }
    }

    private fun handleProjectSelected(project: Project) {
        mTaskId = INVALID_TASK_ID
        if (project.tasks == null || project.tasks!!.isEmpty()) {
            lbl_tasks.visibility = View.GONE
            tasks.visibility = View.GONE
            divider_tasks.visibility = View.GONE
        } else {
            lbl_tasks.visibility = View.VISIBLE
            tasks.visibility = View.VISIBLE
            divider_tasks.visibility = View.VISIBLE
            tasks.apply {
                adapter = TasksAdapter(context!!, android.R.layout.simple_spinner_item, project.tasks!!)
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Unused
                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (position > 0) {
                            mTaskId = (adapter as TasksAdapter).getItem(position).id
                        } else {
                            mTaskId = Task.INVALID_TASK_ID
                        }
                    }

                }
            }
            val tasksAdapter = tasks.adapter as TasksAdapter
            tasksAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            if (mTimeEntry != null) {
                val taskIndex = tasksAdapter.getTaskAdapterPosition(mTimeEntry!!.taskId)
                if (taskIndex != Task.INVALID_TASK_ID) {
                    tasks.setSelection(taskIndex + 1)
                }
            }
        }
    }

    private fun displayTimeEntryData(timeEntry: TimeEntry?) {
        val allMinutes = context?.resources?.getStringArray(R.array.entries_minutes_seconds)?.asList()
        val allHours = context?.resources?.getStringArray(R.array.entries_hours)?.asList()

        description.setText(timeEntry?.description)
        val durationComponents = DateUtils.getDurationComponents(timeEntry?.duration!!)
        start_time.setText(DateUtils.getDisplayDate(DateUtils.parseApiDate(mTimeEntry!!.startTime!!)!!))

        if (durationComponents != null) {
            hrs.setSelection(allHours?.indexOf(durationComponents[0])!!)
            minutes.setSelection(allMinutes?.indexOf(durationComponents[1])!!)
        }
    }

    private fun isNewTimeEntry(entry: TimeEntry?): Boolean {
        return entry == null || entry.id == TimeEntry.NEW_TIME_ENTRY_ID
    }

    private fun showDatePickerDialog() {
        if (mDatePickerTime == null) {
            if (mTimeEntry == null) {
                mDatePickerTime = Calendar.getInstance()
            } else {
                mDatePickerTime = DateUtils.parseApiDate(mTimeEntry!!.startTime!!)
            }
        }

        val dateDialog = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            mDatePickerTime!!.set(Calendar.YEAR, year)
            mDatePickerTime!!.set(Calendar.MONTH, month)
            mDatePickerTime!!.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            mStartDateModified = (mTimeEntry == null ||
                    !DateUtils.didOccurSameDay(mDatePickerTime!!, DateUtils.parseApiDate(mTimeEntry!!.startTime!!)!!))
            start_time.setText(DateUtils.getDisplayDate(mDatePickerTime!!))
            refreshMultiSelectVisibleDays()
        }, mDatePickerTime!!.get(Calendar.YEAR),
                mDatePickerTime!!.get(Calendar.MONTH),
                mDatePickerTime!!.get(Calendar.DAY_OF_MONTH))
        dateDialog.datePicker.minDate = DateUtils.getStartOfPayPeriod().timeInMillis
        dateDialog.datePicker.maxDate = DateUtils.getEndOfPayPeriod().timeInMillis
        dateDialog.show()
    }

    private fun createNewTimeEntry() {
        if (mTimeEntry == null) {
            mTimeEntry = TimeEntry.Builder().build()
        } else if (mStartDateModified) {
            mTimeEntry = mTimeEntry!!.copy()
        }
        populateTimeEntryDetails(mTimeEntry!!)

        if (mDateAdapter != null && mDateAdapter!!.getSelectedDates().size > 0) {
            mListener?.onRequestCreateMultiple(getMultiSelectionTimeEntries(mTimeEntry!!))
        } else {
            mListener?.onRequestCreateTimeEntry(mTimeEntry!!, getAdapterPosition(), false)
        }
    }

    private fun getMultiSelectionTimeEntries(baseEntry: TimeEntry): MutableList<TimeEntry>? {
        val selectedEntries = mutableListOf<TimeEntry>(baseEntry)

        for (date in mDateAdapter!!.getSelectedDates()) {
            val copy = baseEntry.copy()
            val apiDate = DateUtils.parseApiDate(baseEntry.startTime!!)
            apiDate!!.set(Calendar.DAY_OF_YEAR, date.get(Calendar.DAY_OF_YEAR))
            copy.startTime = DateUtils.getApiDateString(apiDate)
            apiDate.add(Calendar.SECOND, copy.duration.toInt())
            copy.stopTime = DateUtils.getApiDateString(apiDate)
            selectedEntries.add(copy)
        }

        return selectedEntries
    }

    private fun getAdapterPosition(): Int? {
        return if (mStartDateModified) {
            TimeEntriesAdapter.INVALID_POSITION
        } else {
            mProjectsAdapterPosition
        }
    }

    private fun editExistingTimeEntry() {
        populateTimeEntryDetails(mTimeEntry!!)
        mListener?.onRequestUpdateTimeEntry(mTimeEntry, getAdapterPosition()!!, false)
    }

    private fun populateTimeEntryDetails(timeEntry: TimeEntry) {
        if (projects.selectedItem == null) {
            Toast.makeText(context, "Project must be specified in order to complete this action", Toast.LENGTH_SHORT).show()
            return
        }
        val project = projects.selectedItem as Project
        timeEntry.projectId = project.id
        timeEntry.duration = getDuration()

        if (TextUtils.isEmpty(timeEntry.startTime)) {
            timeEntry.startTime = if (mStartDateModified) {
                DateUtils.getApiDateString(mDatePickerTime!!)
            } else {
                DateUtils.getApiDateString(Calendar.getInstance())
            }
        } else {
            var startTime = if (mStartDateModified) {
                mDatePickerTime
            } else {
                DateUtils.parseApiDate(timeEntry.startTime!!)
            }
            timeEntry.startTime = DateUtils.getApiDateString(startTime!!)
            startTime.add(Calendar.SECOND, getDuration().toInt())
            timeEntry.stopTime = DateUtils.getApiDateString(startTime)
        }

        timeEntry.billable = project.billable
        timeEntry.description = description.getText().toString()
        timeEntry.createdWith = "android app"
        timeEntry.workspaceId = project.wid
        if (mTaskId != INVALID_TASK_ID) {
            timeEntry.taskId = mTaskId
        }
    }

    private fun getDuration(): Long {
        val durationHours: Long = (hrs.selectedItem as String).toLong(10)
        val durationMinutes: Long = (minutes.selectedItem as String).toLong(10)
        return durationHours * 3600 + durationMinutes * 60
    }


    companion object {
        val TAG = EditTimeEntryFragment::class.java.name
        private val INVALID_TASK_ID = -1
        @JvmStatic
        fun newInstance(user: User?, listener: TimeEntryListener?) =
                EditTimeEntryFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_USER, user)
                    }
                    mListener = listener
                }

        fun newInstance(timeEntry: TimeEntry?, adapterPosition: Int, user: User?, listener: TimeEntryListener) =
                EditTimeEntryFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_USER, user)
                        putInt(ARG_ADAPTER_POSITION, adapterPosition)
                        putParcelable(ARG_TIME_ENTRY, timeEntry)
                    }
                    mListener = listener
                }
    }

    interface TimeEntryListener {
        fun onCancel()

        fun onRequestCreateTimeEntry(timeEntry: TimeEntry, adapterPosition: Int?, replicate: Boolean?)

        fun onRequestUpdateTimeEntry(timeEntry: TimeEntry?, adapterPosition: Int, replicate: Boolean?)

        fun onRequestCreateMultiple(timeEntries: MutableList<TimeEntry>?)
    }
}
