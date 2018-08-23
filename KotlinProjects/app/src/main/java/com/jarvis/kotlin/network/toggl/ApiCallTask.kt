package com.jarvis.kotlin.network.toggl

import android.os.AsyncTask
import com.jarvis.kotlin.network.toggl.response.TogglHttpResponse

abstract class ApiCallTask(delegate: ApiCallDelegate) : AsyncTask<Void, Void, TogglHttpResponse>() {

    private var mDelegate = delegate

    override fun onPostExecute(result: TogglHttpResponse) {
        mDelegate.onResponse(result!!)
    }
}
