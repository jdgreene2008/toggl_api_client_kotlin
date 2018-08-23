package com.jarvis.kotlin.ui.fragment

import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.*

class TimeEntryOptionsDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val window = dialog.window
        val layoutParams: WindowManager.LayoutParams = window.attributes
        layoutParams.apply {
            gravity = Gravity.BOTTOM
            flags = flags and (WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
        window.attributes = layoutParams
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
