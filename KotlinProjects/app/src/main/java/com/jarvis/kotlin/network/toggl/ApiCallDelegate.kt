package com.jarvis.kotlin.network.toggl

import com.jarvis.kotlin.network.toggl.response.TogglHttpResponse

interface ApiCallDelegate {
    fun onResponse(togglResponse: TogglHttpResponse)
}
