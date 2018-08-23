package com.jarvis.kotlin.network.toggl

import com.jarvis.kotlin.network.HttpMethod
import com.jarvis.kotlin.network.toggl.response.*

enum class Endpoint(val path: String, val httpMethod: HttpMethod, val responseClass: Class<out BaseResponse>?) {
    GetUserData("/me?with_related_data=true", HttpMethod.Get, GetUserResponse::class.java),
    CreateTimeEntry("/time_entries", HttpMethod.Post, TimeEntryResponse::class.java),
    DeleteTimeEntry("/time_entries", HttpMethod.Delete, null),
    GetTimeEntries("/time_entries", HttpMethod.Get, GetTimeEntriesResponse::class.java),
    UpdateTimeEntry("/time_entries", HttpMethod.Put, TimeEntryResponse::class.java),
    GetProjectTasks("/projects/{field}/tasks", HttpMethod.Get, GetProjectTasksResponse::class.java)
}
