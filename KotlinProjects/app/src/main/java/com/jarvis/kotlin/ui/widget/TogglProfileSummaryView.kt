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
import com.jarvis.kotlin.ui.activity.TogglActivity
import com.jarvis.kotlin.utils.DateUtils
import kotlinx.android.synthetic.main.toggl_user_profile_header.view.*
import java.util.*

/**
 * TODO: document your custom view class.
 */
class TogglProfileSummaryView : CardView {

    private lateinit var mUser: User

    // Used to dismiss the popup window after an item is clicked
    var popupWindowClickListener: View.OnClickListener? = null

    constructor(context: Context, user: User) : super(context) {
        mUser = user
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.toggl_user_profile_header, this)
        btn_logout.setOnClickListener {
            (context as TogglActivity).logout()
            popupWindowClickListener?.onClick(it)
        }

        label_username.setText(mUser.fullname)
        label_email.setText(mUser.email)
        blog_post.setText(mUser.newBlogPost?.title)
        blog_url.setText(mUser.newBlogPost?.url)
        date.setText(DateUtils.getDisplayDateTime(Calendar.getInstance()))
    }


    companion object {
        private val TAG = TimeEntryActionsView::class.java.name
    }
}
