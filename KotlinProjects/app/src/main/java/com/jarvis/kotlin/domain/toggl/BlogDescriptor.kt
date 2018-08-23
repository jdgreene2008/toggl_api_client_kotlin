package com.jarvis.kotlin.domain.toggl

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class BlogDescriptor(
        @SerializedName("url")
        val url: String,
        @SerializedName("category")
        val category: String,
        @SerializedName("title")
        val title: String,
        @SerializedName("pub_date")
        val pubDate: String):Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString()) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(url)
                parcel.writeString(category)
                parcel.writeString(title)
                parcel.writeString(pubDate)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<BlogDescriptor> {
                override fun createFromParcel(parcel: Parcel): BlogDescriptor {
                        return BlogDescriptor(parcel)
                }

                override fun newArray(size: Int): Array<BlogDescriptor?> {
                        return arrayOfNulls(size)
                }
        }

}
