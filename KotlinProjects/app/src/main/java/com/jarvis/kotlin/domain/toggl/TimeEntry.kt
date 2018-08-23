package com.jarvis.kotlin.domain.toggl

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class TimeEntry(
        @SerializedName("description")
        var description: String?,
        @SerializedName("wid")
        var workspaceId: Int,
        @SerializedName("pid")
        var projectId: Int,
        @SerializedName("id")
        var id: Int = NEW_TIME_ENTRY_ID,
        @SerializedName("tid")
        var taskId: Int,
        @SerializedName("billable")
        var billable: Boolean,
        @SerializedName("start")
        var startTime: String?,
        @SerializedName("stop")
        var stopTime: String?,
        @SerializedName("duration")
        var duration: Long,
        @SerializedName("created_with")
        var createdWith: String?,
        @SerializedName("tags")
        var tags: List<String>?,
        @SerializedName("duronly")
        var duronly: Boolean,
        @SerializedName("at")
        var at: String?) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readByte() != 0.toByte(),
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readString(),
            parcel.createStringArrayList(),
            parcel.readByte() != 0.toByte(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(description)
        parcel.writeInt(workspaceId)
        parcel.writeInt(projectId)
        parcel.writeInt(id)
        parcel.writeInt(taskId)
        parcel.writeByte(if (billable) 1 else 0)
        parcel.writeString(startTime)
        parcel.writeString(stopTime)
        parcel.writeLong(duration)
        parcel.writeString(createdWith)
        parcel.writeStringList(tags)
        parcel.writeByte(if (duronly) 1 else 0)
        parcel.writeString(at)
    }

    override fun describeContents(): Int {
        return 0
    }

     class Builder {
        var description: String? = null
        var workspaceId: Int = 0
        var projectId: Int = 0
        var id: Int = NEW_TIME_ENTRY_ID
        var taskId: Int = 0
        var billable: Boolean = false
        var startTime: String? = null
        var stopTime: String? = null
        var duration: Long = 0
        var createdWith: String? = null
        val tags: List<String>? = null
        var duronly: Boolean = false
        var at: String? = null

        fun build(): TimeEntry {
            return TimeEntry(description,
                    workspaceId,
                    projectId,
                    id,
                    taskId,
                    billable,
                    startTime,
                    stopTime,
                    duration,
                    createdWith,
                    tags,
                    duronly,
                    at)
        }

    }

    companion object CREATOR : Parcelable.Creator<TimeEntry> {
        val NEW_TIME_ENTRY_ID = -1
        override fun createFromParcel(parcel: Parcel): TimeEntry {
            return TimeEntry(parcel)
        }

        override fun newArray(size: Int): Array<TimeEntry?> {
            return arrayOfNulls(size)
        }
    }

}
