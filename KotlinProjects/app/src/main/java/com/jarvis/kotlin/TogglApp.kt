package com.jarvis.kotlin

import android.app.Application

class TogglApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: TogglApp
            private set
    }

}
