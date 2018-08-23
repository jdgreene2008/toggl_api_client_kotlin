package com.jarvis.kotlin.domain.toggl

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class User(
        @SerializedName("api_token")
        val apiToken: String,
        @SerializedName("default_wid")
        val defaultWorkspaceId: String,
        @SerializedName("email")
        val email: String,
        @SerializedName("fullname")
        val fullname: String,
        @SerializedName("timeofday_format")
        val timeOfDayFormat: String,
        @SerializedName("beginning_of_week")
        val beginningOfWeek: Int,
        @SerializedName("language")
        val language: String,
        @SerializedName("image_url")
        val imageUrl: String,
        @SerializedName("timezone")
        val timezone: String,
        @SerializedName("new_blog_post")
        val newBlogPost: BlogDescriptor,
        @SerializedName("projects")
        val projects: List<Project>,
        var timeEntries: List<TimeEntry>,
        var futureTimeEntries: List<TimeEntry>
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readParcelable(BlogDescriptor::class.java.classLoader),
            parcel.createTypedArrayList(Project),
            parcel.createTypedArrayList(TimeEntry),
            parcel.createTypedArrayList(TimeEntry)) {
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(apiToken)
        dest?.writeString(defaultWorkspaceId)
        dest?.writeString(email)
        dest?.writeString(fullname)
        dest?.writeString(timeOfDayFormat)
        dest?.writeInt(beginningOfWeek)
        dest?.writeString(language)
        dest?.writeString(imageUrl)
        dest?.writeString(timezone)
        dest?.writeParcelable(newBlogPost, 0)
        dest?.writeTypedList(projects)
        dest?.writeTypedList(timeEntries)
        dest?.writeTypedList(futureTimeEntries)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }


}

