package com.jarvis.kotlin.network.toggl.response

import com.google.gson.annotations.SerializedName
import com.jarvis.kotlin.domain.toggl.Task

data class GetProjectTasksResponse(
        @SerializedName("data")
        val tasks: List<Task>
) : BaseResponse()
