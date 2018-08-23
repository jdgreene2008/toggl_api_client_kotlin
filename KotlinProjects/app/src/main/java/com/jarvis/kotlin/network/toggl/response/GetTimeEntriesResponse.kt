package com.jarvis.kotlin.network.toggl.response

import com.google.gson.annotations.SerializedName
import com.jarvis.kotlin.domain.toggl.TimeEntry

data class GetTimeEntriesResponse(
        @SerializedName("data")
        val timeEntries: List<TimeEntry>
) : BaseResponse()
