package com.jarvis.kotlin.ui.widget

import android.content.Context
import android.support.v4.view.LayoutInflaterCompat
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import com.jarvis.kotlin.R
import com.jarvis.kotlin.constants.TimeEntryState
import com.jarvis.kotlin.domain.toggl.TimeEntry
import com.jarvis.kotlin.domain.toggl.User
import com.jarvis.kotlin.ui.TimeEntryDisplayModel
import kotlinx.android.synthetic.main.time_entry_actions.view.*

/**
 * TODO: document your custom view class.
 */
class TimeEntryActionsView : CardView {

    private var mTimeEntryDisplayModel: TimeEntryDisplayModel? = null
    private var mAdapterPosition: Int? = null
    private var mUser: User? = null
    var listener: OnTimeEntryOptionListener? = null

    // Used to dismiss the popup window after an item is clicked
    var popupWindowClickListener: View.OnClickListener? = null

    private val mInternalOnClickListener = OnClickListener { v ->
        Log.d(TimeEntryActionsView::class.java.name, "internalOnClickListener click")
        if (v == btn_delete) {
            listener?.onDeleteEntry(mTimeEntryDisplayModel!!, mAdapterPosition!!)
        } else if (v == btn_edit) {
            listener?.onEditEntry(mTimeEntryDisplayModel!!.entry, mAdapterPosition!!, mUser)
        } else {
            mTimeEntryDisplayModel?.entry?.createdWith = "android app"
            mTimeEntryDisplayModel?.entry?.stopTime = null
            listener?.onSaveEntry(mTimeEntryDisplayModel!!.entry, mAdapterPosition!!)
        }
        popupWindowClickListener?.onClick(v)
    }

    constructor(context: Context, user: User?,
                timeEntryDisplayModel: TimeEntryDisplayModel?,
                adapterPosition: Int?) : super(context) {
        mUser = user
        mTimeEntryDisplayModel = timeEntryDisplayModel
        mAdapterPosition = adapterPosition
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.time_entry_actions, this)
        btn_save.setOnClickListener(mInternalOnClickListener)
        btn_edit.setOnClickListener(mInternalOnClickListener)
        btn_delete.setOnClickListener(mInternalOnClickListener)
        if (mTimeEntryDisplayModel?.state == TimeEntryState.Saved) {
            btn_save.isEnabled = false
            btn_save.alpha = 0.5f
        }
    }


    companion object {
        private val TAG = TimeEntryActionsView::class.java.name
    }

    interface OnTimeEntryOptionListener {
        fun onSaveEntry(timeEntry: TimeEntry, adapterPosition: Int)

        fun onEditEntry(timeEntry: TimeEntry, adapterPosition: Int, user: User?)

        fun onDeleteEntry(timeEntryDisplayModel: TimeEntryDisplayModel, adapterPosition: Int)
    }
}
