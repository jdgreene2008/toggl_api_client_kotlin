package com.jarvis.kotlin.ui

import android.os.Parcel
import android.os.Parcelable
import com.jarvis.kotlin.constants.TimeEntryState
import com.jarvis.kotlin.domain.toggl.TimeEntry

data class TimeEntryDisplayModel(val entry: TimeEntry,
                                 var state: TimeEntryState) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readParcelable(TimeEntry::class.java.classLoader),
            TimeEntryState.values()[parcel.readInt()]) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(entry, flags)
        parcel.writeInt(state!!.ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TimeEntryDisplayModel> {
        override fun createFromParcel(parcel: Parcel): TimeEntryDisplayModel {
            return TimeEntryDisplayModel(parcel)
        }

        override fun newArray(size: Int): Array<TimeEntryDisplayModel?> {
            return arrayOfNulls(size)
        }
    }
}
