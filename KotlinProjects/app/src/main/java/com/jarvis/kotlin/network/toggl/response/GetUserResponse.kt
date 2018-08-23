package com.jarvis.kotlin.network.toggl.response

import com.google.gson.annotations.SerializedName
import com.jarvis.kotlin.domain.toggl.User

data class GetUserResponse(
        @SerializedName("since")
        val profileCreationTime: Long,
        @SerializedName("data")
        val userData: User) : BaseResponse()
