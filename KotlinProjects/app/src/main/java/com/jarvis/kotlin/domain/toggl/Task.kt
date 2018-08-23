package com.jarvis.kotlin.domain.toggl

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Task(val name: String,
                val id: Int = INVALID_TASK_ID,
                @SerializedName("pid")
                val projectId: Int,
                @SerializedName("wid")
                val workspaceId: Int,
                @SerializedName("uid")
                val userId: Int,
                @SerializedName("estimated_seconds")
                val estimatedSeconds: Int,
                val active: Boolean,
                val at: String,
                @SerializedName("tracked_seconds")
                val trackedSeconds: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readByte() != 0.toByte(),
            parcel.readString(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(id)
        parcel.writeInt(projectId)
        parcel.writeInt(workspaceId)
        parcel.writeInt(userId)
        parcel.writeInt(estimatedSeconds)
        parcel.writeByte(if (active) 1 else 0)
        parcel.writeString(at)
        parcel.writeInt(trackedSeconds)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Task> {
        val INVALID_TASK_ID = -1
        override fun createFromParcel(parcel: Parcel): Task {
            return Task(parcel)
        }

        override fun newArray(size: Int): Array<Task?> {
            return arrayOfNulls(size)
        }
    }

}

