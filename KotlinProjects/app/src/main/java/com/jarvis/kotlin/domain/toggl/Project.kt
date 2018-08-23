package com.jarvis.kotlin.domain.toggl

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Project(
        @SerializedName("id")
        val id: Int,
        @SerializedName("wid")
        val wid: Int,
        @SerializedName("name")
        val name: String,
        @SerializedName("billable")
        val billable: Boolean,
        @SerializedName("is_private")
        val private: Boolean,
        @SerializedName("active")
        val active: Boolean,
        @SerializedName("hex_color")
        val hexColor: String,
        var tasks: List<Task>?
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readString(),
            parcel.createTypedArrayList(Task.CREATOR)) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(wid)
        parcel.writeString(name)
        parcel.writeByte(if (billable) 1 else 0)
        parcel.writeByte(if (private) 1 else 0)
        parcel.writeByte(if (active) 1 else 0)
        parcel.writeString(hexColor)
        parcel.writeTypedList(tasks)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun findTaskById(taskId: Int): Task? {
        if (tasks != null) {
            for (task in tasks!!) {
                if (task.id == taskId) {
                    return task
                }
            }
        }

        return null
    }

    companion object CREATOR : Parcelable.Creator<Project> {
        override fun createFromParcel(parcel: Parcel): Project {
            return Project(parcel)
        }

        override fun newArray(size: Int): Array<Project?> {
            return arrayOfNulls(size)
        }
    }

}

