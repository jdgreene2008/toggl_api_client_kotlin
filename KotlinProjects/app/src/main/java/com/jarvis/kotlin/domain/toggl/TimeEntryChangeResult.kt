package com.jarvis.kotlin.domain.toggl

import android.os.Parcel
import android.os.Parcelable

data class TimeEntryChangeResult(val changedEntry: TimeEntry, val changeType: ChangeType) : Parcelable {


    constructor(parcel: Parcel) : this(
            parcel.readParcelable(TimeEntry::class.java.classLoader),
            ChangeType.values()[parcel.readInt()]) {
    }

    enum class ChangeType {
        DELETE,
        CREATE,
        EDIT
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(changedEntry, flags)
        parcel.writeInt(changeType.ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TimeEntryChangeResult> {
        override fun createFromParcel(parcel: Parcel): TimeEntryChangeResult {
            return TimeEntryChangeResult(parcel)
        }

        override fun newArray(size: Int): Array<TimeEntryChangeResult?> {
            return arrayOfNulls(size)
        }
    }


}
