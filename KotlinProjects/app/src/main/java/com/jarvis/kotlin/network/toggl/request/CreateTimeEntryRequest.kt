package com.jarvis.kotlin.network.toggl.request

import com.google.gson.annotations.SerializedName
import com.jarvis.kotlin.domain.toggl.TimeEntry

data class CreateTimeEntryRequest(
        @SerializedName("time_entry")
        val timeEntry: TimeEntry) : BaseRequest()
