package com.jarvis.kotlin.network.toggl.response

import com.google.gson.annotations.SerializedName
import com.jarvis.kotlin.domain.toggl.TimeEntry

data class TimeEntryResponse(
        @SerializedName("data")
        val timeEntry: TimeEntry
) : BaseResponse()
