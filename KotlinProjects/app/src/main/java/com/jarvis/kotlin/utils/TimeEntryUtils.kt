package com.jarvis.kotlin.utils

import android.text.TextUtils
import com.jarvis.kotlin.domain.toggl.TimeEntry
import java.util.*

class TimeEntryUtils {
    companion object {
        private val TAG = TimeEntryUtils@ this::class.java.name

        fun getTimeEntryDuplicatesForWeek(startEntry: TimeEntry): MutableList<TimeEntry> {
            val startTime = DateUtils.parseApiDate(startEntry.startTime!!)
            val duplicateEntries = mutableListOf<TimeEntry>()
            val startIndex = startTime!!.get(Calendar.DAY_OF_WEEK)

            if (startIndex > Calendar.SUNDAY && startIndex < Calendar.FRIDAY) {
                val begin = startIndex + 1
                for (i in begin..Calendar.FRIDAY) {
                    val nextEntry = startEntry.copy()
                    val nextEntryTime = DateUtils.parseApiDate(startEntry.startTime!!)

                    nextEntryTime!!.add(Calendar.DAY_OF_YEAR, i - startIndex)
                    nextEntry.startTime = DateUtils.getApiDateString(nextEntryTime)
                    if (!TextUtils.isEmpty(nextEntry.stopTime)) {
                        nextEntryTime.add(Calendar.SECOND, nextEntry.duration.toInt())
                        nextEntry.stopTime = DateUtils.getApiDateString(nextEntryTime)
                    }

                    duplicateEntries.add(nextEntry)
                }
            }

            return duplicateEntries
        }
    }
}
