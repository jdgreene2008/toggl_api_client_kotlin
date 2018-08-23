package com.jarvis.kotlin.ui.fragment

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jarvis.kotlin.R
import com.jarvis.kotlin.constants.TimeEntryState
import com.jarvis.kotlin.domain.toggl.TimeEntry
import com.jarvis.kotlin.domain.toggl.TimeEntryChangeResult
import com.jarvis.kotlin.domain.toggl.User
import com.jarvis.kotlin.ui.ListDecoration
import com.jarvis.kotlin.ui.AlternateTimeEntriesAdapter
import com.jarvis.kotlin.ui.TimeEntryDisplayModel
import com.jarvis.kotlin.ui.datamodels.TogglViewModel
import com.jarvis.kotlin.ui.widget.TimeEntryActionsView
import com.jarvis.kotlin.utils.DateUtils
import com.jarvis.kotlin.utils.ImageUtils
import kotlinx.android.synthetic.main.fragment_toggl.*
import java.util.*

private const val ARG_ADAPTER_POSITION = "adapter_position"
private const val ARG_CHANGE_RESULT = "change_result"
private const val ARG_TIME_ENTRIES = "time_entries"
private const val ARG_USER = "user"


class TimeEntriesFragment : Fragment() {
    private var listener: OnTimeEntryApiActionListener? = null
    private var mUser: User? = null
    private var mCurrentEdit: TimeEntryChangeResult? = null
    private var mTimeEntryAdapter: AlternateTimeEntriesAdapter? = null
    private var mAdapterPosition: Int? = null
    private var mTimeEntryData: ArrayList<TimeEntryDisplayModel>? = null
    private var mAwaitingFreshData: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView()")
        if (savedInstanceState != null) {
            mCurrentEdit = savedInstanceState.getParcelable(ARG_CHANGE_RESULT)
            mAdapterPosition = savedInstanceState.getInt(ARG_ADAPTER_POSITION, -1)
            mTimeEntryData = savedInstanceState.getParcelableArrayList(ARG_TIME_ENTRIES)
            mUser = savedInstanceState.getParcelable(ARG_USER)
        }
        return inflater.inflate(R.layout.fragment_toggl, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated()")
        super.onViewCreated(view, savedInstanceState)
        val viewModel = ViewModelProviders.of(this.activity!!).get(TogglViewModel::class.java)
        viewModel.userData.observe(this, Observer {
            mAwaitingFreshData = false
            if (it != null) {
                mUser = it
            }
            mTimeEntryData = null
            loadUi(mUser)
            viewModel.userData.removeObservers(this)
            viewModel.userData = MutableLiveData()
        })
        btn_save_all.setOnClickListener {
            mTimeEntryAdapter?.saveAll()
        }

        if (mUser != null) {
            loadUi(mUser)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(ARG_CHANGE_RESULT, mCurrentEdit)
        outState.putInt(ARG_ADAPTER_POSITION, mAdapterPosition ?: -1)
        outState.putParcelableArrayList(ARG_TIME_ENTRIES, mTimeEntryData)
        outState.putParcelable(ARG_USER, mUser)
        super.onSaveInstanceState(outState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnTimeEntryApiActionListener && context is TimeEntryActionsView.OnTimeEntryOptionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnTimeEntryActionListener,OnTimeEntryOptionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach()")
        listener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
    }

    private fun loadUi(user: User?) {
        Log.d(TAG, "loadUI()")
        empty_view.visibility = View.VISIBLE
        time_entry_list.visibility = View.GONE
        if (mTimeEntryData == null) {
            displayTimeEntries(user)
        } else {
            populateTimeEntryList(mTimeEntryData)
        }
        if (mCurrentEdit != null) {
            addChange(mAdapterPosition, mCurrentEdit!!)
        }
    }


    private fun displayTimeEntries(user: User?) {
        Log.d(TAG, "displayTimeEntries()")
        mTimeEntryData = arrayListOf()

        val start = DateUtils.getStartOfPayPeriod()
        val end = DateUtils.getEndOfPayPeriod()

        while (start.get(Calendar.DAY_OF_YEAR) <= end.get(Calendar.DAY_OF_YEAR)) {
            val sameDayEntries = user!!.timeEntries.filter {
                DateUtils.didOccurSameDay(DateUtils.parseApiDate(it.startTime!!)!!, start)
            }
            if (sameDayEntries.isNotEmpty()) {
                for (entry in sameDayEntries) {
                    mTimeEntryData!!.add(TimeEntryDisplayModel(entry, TimeEntryState.Saved))
                }
            } else if (start.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY && start.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
            && start.get(Calendar.DAY_OF_YEAR) <= Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
                val previousDayEntries = user.timeEntries.filter {
                    DateUtils.didOccurYesterday(DateUtils.parseApiDate(it.startTime!!)!!, start)
                }
                if (previousDayEntries.isNotEmpty()) {
                    for (entry in previousDayEntries) {
                        val copy = entry.copy()
                        val yesterday = DateUtils.parseApiDate(copy.startTime!!)

                        yesterday!!.add(Calendar.DAY_OF_YEAR, 1)
                        copy.startTime = DateUtils.getApiDateString(yesterday)
                        yesterday!!.add(Calendar.SECOND, copy.duration.toInt())
                        copy.stopTime = DateUtils.getApiDateString(yesterday)
                        copy.id = TimeEntry.NEW_TIME_ENTRY_ID
                        mTimeEntryData!!.add(TimeEntryDisplayModel(copy, TimeEntryState.Unsaved))
                    }
                }
            }

            start.add(Calendar.DAY_OF_YEAR, 1)
        }

        populateTimeEntryList(mTimeEntryData)
    }

    private fun populateTimeEntryList(timeEntries: ArrayList<TimeEntryDisplayModel>?) {
        Log.d(TAG, "populateTimeEntryList()")
        mTimeEntryAdapter = AlternateTimeEntriesAdapter(this.activity!!, timeEntries, mUser, context as OnTimeEntryApiActionListener)
        time_entry_list.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = mTimeEntryAdapter
            addItemDecoration(ListDecoration(ImageUtils.getDrawable(this.context, R.drawable.divider_transparent)))
        }

        setListVisibility()
    }

    private fun setListVisibility() {
        if (mTimeEntryData != null && mTimeEntryData?.size!! > 0) {
            time_entry_list.visibility = View.VISIBLE
            empty_view.visibility = View.GONE
        } else {
            time_entry_list.visibility = View.GONE
            empty_view.visibility = View.VISIBLE
        }
    }

    fun clearTimeEntries() {
        Log.d(TAG, "clearTimeEntries()")
        mTimeEntryData = null
        mAdapterPosition = null
        mAwaitingFreshData = true
    }

    fun createNewEntry() {
        mTimeEntryAdapter?.createNewEntry()
    }

    fun deleteTimeEntry(timeEntryModel: TimeEntryDisplayModel, adapterPosition: Int) {
        mTimeEntryAdapter?.deleteEntry(timeEntryModel, adapterPosition)
    }

    fun saveTimeEntry(entry: TimeEntry, adapterPosition: Int) {
        mTimeEntryAdapter?.saveEntry(entry, adapterPosition)
    }

    fun addChange(adapterPosition: Int?, changeResult: TimeEntryChangeResult) {
        Log.d(TAG, "addChange()")
        mCurrentEdit = changeResult
        mAdapterPosition = adapterPosition
        if (view != null) {
            updateTimeEntries(adapterPosition, changeResult)
        }
    }

    private fun updateTimeEntries(adapterPosition: Int?, changeResult: TimeEntryChangeResult) {
        Log.d(TAG, "updateTimeEntries()")
        val changeType = changeResult.changeType
        when (changeType) {
            TimeEntryChangeResult.ChangeType.CREATE, TimeEntryChangeResult.ChangeType.EDIT ->
                commitTimeEntryChanges(adapterPosition, changeResult.changedEntry)
            TimeEntryChangeResult.ChangeType.DELETE -> mTimeEntryAdapter?.removeAt(adapterPosition!!)
        }
        mCurrentEdit = null
        mAdapterPosition = null
    }

    private fun commitTimeEntryChanges(adapterPosition: Int?, timeEntry: TimeEntry) {
        Log.d(TAG, "commitTimeEntryChanges() :: position->" + adapterPosition)
        mTimeEntryAdapter?.commitSaveEntry(adapterPosition,
                TimeEntryDisplayModel(timeEntry, TimeEntryState.Saved))
    }


    interface OnTimeEntryApiActionListener {
        fun onCreateNew(user: User?)

        fun onSaveAll(timeEntries: MutableList<TimeEntry>?)

        fun onDelete(timeEntry: TimeEntry, adapterPosition: Int)

        fun onSave(timeEntry: TimeEntry, adapterPosition: Int)

        fun onEdit(timeEntry: TimeEntry, adapterPosition: Int, user: User?)

        fun onShowTimeEntryOptions(timeEntryModel: TimeEntryDisplayModel, adapterPosition: Int, user: User?,
                                   anchorView: View)
    }

    companion object {
        @JvmStatic
        fun newInstance() = TimeEntriesFragment()

        val TAG = TimeEntriesFragment::class.java.name
    }
}
