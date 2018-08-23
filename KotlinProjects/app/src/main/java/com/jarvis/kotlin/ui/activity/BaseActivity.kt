package com.jarvis.kotlin.ui.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.jarvis.kotlin.utils.PrefsHelper
import java.util.*

abstract class BaseActivity : AppCompatActivity() {

    private var mUserInteractionTimer: Timer? = null

    override fun onUserInteraction() {
        super.onUserInteraction()
        if (mUserInteractionTimer != null) {
            mUserInteractionTimer?.cancel()
            setupUserInteractionMonitor()
        }
    }

    protected fun setupUserInteractionMonitor() {
        mUserInteractionTimer = Timer()
        mUserInteractionTimer!!.schedule(getTimerTask(), TIMEOUT_PERIOD)
    }

    private fun getTimerTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                logout()
            }
        }
    }

    fun logout() {
        Log.d(TAG, "logout()")
        if (mUserInteractionTimer != null) {
            mUserInteractionTimer!!.cancel()
        }
        PrefsHelper(this@BaseActivity).clearApiToken()
        val restartIntent = Intent(this@BaseActivity, TogglActivity::class.java)
        restartIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(restartIntent)
    }

    companion object {
        private val TAG = BaseActivity::class.java.name
        private val TIMEOUT_PERIOD: Long = 480000
    }

}
