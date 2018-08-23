package com.jarvis.kotlin.utils

import android.content.Context
import android.net.ConnectivityManager

class NetworkUtils {

    companion object {
        fun isNetworkConnected(context: Context): Boolean {
            val info = (context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
            return if (info != null) {
                info!!.isConnected
            } else {
                false
            }
        }
    }
}
