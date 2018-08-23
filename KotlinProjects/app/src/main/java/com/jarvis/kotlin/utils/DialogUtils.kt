package com.jarvis.kotlin.utils

import android.content.Context
import android.support.annotation.DimenRes
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import com.jarvis.kotlin.R
import com.jarvis.kotlin.domain.toggl.User
import com.jarvis.kotlin.ui.TimeEntryDisplayModel
import com.jarvis.kotlin.ui.activity.TogglActivity
import com.jarvis.kotlin.ui.widget.TimeEntryActionsView
import com.jarvis.kotlin.ui.widget.TogglProfileSummaryView

class DialogUtils {


    companion object {

        private fun createPopupWindow(contentView: View, width: Int, height: Int): PopupWindow {
            val popupWindow = PopupWindow(contentView, width, height)
            popupWindow.isTouchable = true
            popupWindow.isOutsideTouchable = true
            popupWindow.elevation = 2.2f
            popupWindow.isFocusable = true
            return popupWindow
        }

        fun showUserDetailsPopup(context: Context, user: User, anchorView: View) {
            val userProfileView = TogglProfileSummaryView(context, user)
            anchorView.isSelected = true
            val popupWindow = createPopupWindow(userProfileView, context.resources.getDimensionPixelSize(R.dimen.user_profile_dialog_width),
                    context.resources.getDimensionPixelSize(R.dimen.user_profile_dialog_height))
            userProfileView.popupWindowClickListener = object : View.OnClickListener {
                override fun onClick(v: View?) {
                    popupWindow.dismiss()
                }
            }
            popupWindow.setOnDismissListener {
                anchorView.isSelected = false
            }

            val anchorCoords = intArrayOf(0, 0)
            anchorView.getLocationInWindow(anchorCoords)
            popupWindow.showAtLocation((context as AppCompatActivity).window.decorView, Gravity.TOP or Gravity.START,
                    anchorCoords[0] - context.resources.getDimensionPixelSize(R.dimen.user_profile_dialog_width) - 10,
                    2 * anchorCoords[1] + 10)

        }

        fun showTimeEntryActionsPopup(context: Context, timeEntryDisplayModel: TimeEntryDisplayModel,
                                      adapterPosition: Int, user: User?, anchorView: View) {
            val timeEntryActionView = TimeEntryActionsView(context, user, timeEntryDisplayModel, adapterPosition)
            timeEntryActionView.listener = context as TogglActivity

            val popupWindow = PopupWindow(timeEntryActionView,
                    context.resources.getDimensionPixelSize(R.dimen.time_entry_actions_width),
                    context.resources.getDimensionPixelSize(R.dimen.time_entry_actions_height)
            )
            popupWindow.isTouchable = true
            popupWindow.isOutsideTouchable = true
            popupWindow.elevation = 2.2f
            popupWindow.isFocusable = true
            popupWindow.setOnDismissListener {
                anchorView.isSelected = false
            }

            timeEntryActionView.popupWindowClickListener = object : View.OnClickListener {
                override fun onClick(v: View?) {
                    anchorView.isSelected = false
                    popupWindow.dismiss()
                }
            }

            val anchorCoords = intArrayOf(0, 0)
            anchorView.getLocationInWindow(anchorCoords)
            popupWindow.showAtLocation((context as AppCompatActivity).window.decorView, Gravity.TOP or Gravity.START,
                    anchorCoords[0] - context.resources.getDimensionPixelSize(R.dimen.time_entry_actions_width) - 15,
                    anchorCoords[1] + 15)

        }
    }


}
