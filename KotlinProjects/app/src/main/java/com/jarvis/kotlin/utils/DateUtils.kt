package com.jarvis.kotlin.utils

import android.util.Log
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateUtils {


    companion object {
        private val TAG = DateUtils@ this::class.java.name
        private val API_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX"
        private val DISPLAY_DATE_TIME_FORMAT = "MMM d, yyyy @ h:mm a"
        private val TIME_DURATION_FORMAT = "h:mm:ss"
        private val DISPLAY_DATE_FORMAT = "EEE, MMM d"
        private val DATE_PICKER_RANGE_FORMAT = "mm/dd/yyyy"
        private val MONTH_DAY_FORMAT = "M/dd"
        private val API_DATE_FORMAT_JODA_TIME = "yyyy-MM-dd'T'HH:mm:ssZZ"
        private val API_DATE_FORMATTER = SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault())
        private val DISPLAY_DATE_TIME_FORMATTER = SimpleDateFormat(DISPLAY_DATE_TIME_FORMAT, Locale.getDefault())
        private val DISPLAY_DURATION_FORMATTER = SimpleDateFormat(TIME_DURATION_FORMAT, Locale.getDefault())
        private val DISPLAY_DATE_FORMATTER = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
        private val DATE_PICKER_RANGE_FORMATTER = SimpleDateFormat(DATE_PICKER_RANGE_FORMAT, Locale.getDefault())
        private val MONTH_DAY_FORMATTER = SimpleDateFormat(MONTH_DAY_FORMAT, Locale.getDefault())
        private val API_DATE_FORMATTER_JODA = DateTimeFormat.forPattern(API_DATE_FORMAT_JODA_TIME)
        private const val TODAY = "Today"
        private const val YESTERDAY = "Yesterday"
        private const val TOMMOROW = "Tommorow"


        fun parseApiDate(input: String): Calendar? {
            try {
                val jodaDate = API_DATE_FORMATTER_JODA.parseDateTime(input)
                val calendar = Calendar.getInstance()
                calendar.time = jodaDate.toDate()
                return calendar
            } catch (e: ParseException) {
                return null
            }
        }

        fun didOccurSameDay(dateA: Calendar, dateB: Calendar): Boolean {
            return (dateA.get(Calendar.YEAR) == dateB.get(Calendar.YEAR))
                    && (dateA.get(Calendar.DAY_OF_YEAR) == dateB.get(Calendar.DAY_OF_YEAR))
        }

        fun didOccurYesterday(dateEarlier: Calendar, dateLatter: Calendar): Boolean {
            dateEarlier.add(Calendar.DAY_OF_YEAR, 1)
            return (dateEarlier.get(Calendar.DAY_OF_YEAR) == dateLatter.get(Calendar.DAY_OF_YEAR)
                    && dateEarlier.get(Calendar.YEAR) == dateLatter.get(Calendar.YEAR))
        }


        /**
         * Consumes a date string formatted as:
         * "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" and returns
         * a date string formatted as "MMM d, yyyy h:m a"
         */
        fun getDisplayDateTime(apiDateString: String?): String {
            if (apiDateString == null) return ""
            val date = parseApiDate(apiDateString)
            return DISPLAY_DATE_TIME_FORMATTER.format(date?.time)
        }

        fun getDisplayDateTime(date: Calendar): String? {
            return DISPLAY_DATE_TIME_FORMATTER.format(date.time)
        }

        fun getDisplayMonthDay(date: Calendar): String? {
            return MONTH_DAY_FORMATTER.format(date.time)
        }

        fun getApiDateString(date: Calendar): String? {
            return API_DATE_FORMATTER_JODA.print(DateTime(date.timeInMillis))
        }

        fun getDisplayDate(date: Calendar): String {
            val today = Calendar.getInstance()
            val tommorow = Calendar.getInstance()
            tommorow.add(Calendar.DAY_OF_YEAR, 1)
            val yesterday = Calendar.getInstance()
            yesterday.add(Calendar.DAY_OF_YEAR, -1)

            return when (date.get(Calendar.DAY_OF_YEAR)) {
                today.get(Calendar.DAY_OF_YEAR) -> TODAY
                yesterday.get(Calendar.DAY_OF_YEAR) -> YESTERDAY
                tommorow.get(Calendar.DAY_OF_YEAR) -> TOMMOROW
                else -> DISPLAY_DATE_FORMATTER.format(date.time)
            }
        }

        fun getDisplayDuration(seconds: Long): String? {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_YEAR, 0)
            calendar.set(Calendar.HOUR_OF_DAY, (seconds / 3600).toInt())
            calendar.set(Calendar.MINUTE, (seconds % 3600 / 60).toInt())
            calendar.set(Calendar.SECOND, ((seconds % 3600 % 60).toInt()))
            return DISPLAY_DURATION_FORMATTER.format(calendar.time)
        }

        fun getDurationComponents(seconds: Long): List<String>? {
            val durationString = getDisplayDuration(seconds)
            return durationString?.split(":")
        }


        /**
         * Start and end time for the time entries that were entered since Monday of the current week
         */
        fun getPayPeriod(): Pair<String?, String?> {
            val startTime = getStartOfPayPeriod()
            val endOfPeriod = getEndOfPayPeriod()
            return Pair(getApiDateString(startTime), getApiDateString(endOfPeriod))
        }

        fun getDatePickerRangeFormat(calendar: Calendar): String {
            return DATE_PICKER_RANGE_FORMATTER.format(calendar.time)
        }

        /**
         * Get start date of pay period.
         */
        fun getStartOfPayPeriod(): Calendar {
            val startTime = Calendar.getInstance()
            val currentDay = startTime.get(Calendar.DAY_OF_MONTH)
            startTime.set(Calendar.HOUR_OF_DAY, 0)
            startTime.set(Calendar.MINUTE, 0)
            startTime.set(Calendar.SECOND, 0)
            if (currentDay <= 15) {
                startTime.set(Calendar.DAY_OF_MONTH, 1)
            } else {
                startTime.set(Calendar.DAY_OF_MONTH, 16)
            }

            return startTime
        }


        /**
         * Get end date of work week.
         */
        fun getEndOfPayPeriod(): Calendar {
            val endTime = Calendar.getInstance()
            if (endTime.get(Calendar.DAY_OF_MONTH) <= 15) {
                endTime.set(Calendar.DAY_OF_MONTH, 15)
            } else {
                val maxDays = endTime.getActualMaximum(Calendar.DAY_OF_MONTH)
                endTime.set(Calendar.DAY_OF_MONTH, maxDays)
            }
            endTime.set(Calendar.HOUR_OF_DAY, 23)
            endTime.set(Calendar.MINUTE, 0)
            return endTime
        }

        /**
         * Get list of Calendar days for the pay period.
         */
        fun getPayPeriodDays(): MutableList<Calendar> {
            val payPeriodDays = mutableListOf<Calendar>()
            val start = getStartOfPayPeriod()
            val end = getEndOfPayPeriod()
            while (start.get(Calendar.DAY_OF_YEAR) <= end.get(Calendar.DAY_OF_YEAR)) {
                payPeriodDays.add(start.clone() as Calendar)
                start.add(Calendar.DAY_OF_YEAR, 1)
            }
            return payPeriodDays
        }


        /**
         * Start and end time for the time entries that were entered for the following day in the workweek
         * through Friday.
         */
        @Deprecated("Unused")
        fun getFutureTimeEntriesSearchRange(): Pair<String?, String?> {
            val startTime = Calendar.getInstance()
            val endTime = Calendar.getInstance()
            val currentDay = startTime.get(Calendar.DAY_OF_WEEK)

            if (currentDay > Calendar.SUNDAY && currentDay < Calendar.FRIDAY) {
                startTime.add(Calendar.DAY_OF_YEAR, 1)
                startTime.set(Calendar.HOUR_OF_DAY, 0)
                startTime.set(Calendar.MINUTE, 0)
                startTime.set(Calendar.SECOND, 0)

                endTime.add(Calendar.DAY_OF_YEAR, Calendar.FRIDAY - currentDay)
                endTime.set(Calendar.HOUR_OF_DAY, 23)
                endTime.set(Calendar.MINUTE, 59)
                endTime.set(Calendar.SECOND, 59)
            }

            return Pair(getApiDateString(startTime), getApiDateString(endTime))
        }
    }
}
