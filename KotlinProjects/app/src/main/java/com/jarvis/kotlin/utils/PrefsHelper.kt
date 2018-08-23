package com.jarvis.kotlin.utils

import android.content.Context
import android.content.SharedPreferences

class PrefsHelper(context: Context) {
    private val mContext = context

    private val preferences: SharedPreferences

    private val editor: SharedPreferences.Editor

    init {
        preferences = mContext.getSharedPreferences("toggl_user", Context.MODE_PRIVATE)
        editor = preferences.edit()
    }

    fun saveApiToken(apiToken: String) {
        editor.putString("api_token", apiToken).apply()
    }

    fun clearApiToken() {
        editor.remove("api_token").apply()
    }

    fun getApiToken(): String {
        return preferences.getString("api_token", "")
    }

    fun saveUsername(username: String) {
        editor.putString("user", username).apply()
    }

    fun getUsername(): String {
        return preferences.getString("user", "")
    }
}
