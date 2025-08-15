package com.apcs.worknestapp.domain.logic

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateFormater {
    fun format(
        date: Date,
        formatString: String = "dd MMMM, yyyy",
        locale: Locale = Locale.getDefault(),
    ): String = SimpleDateFormat(formatString, locale).format(date)

    fun format(
        timestamp: Timestamp,
        formatString: String = "dd MMMM, yyyy",
        locale: Locale = Locale.getDefault(),
    ): String = format(timestamp.toDate(), formatString = formatString, locale = locale)

    fun formatConversationTime(date: Date, locale: Locale = Locale.getDefault()): String {
        val dayFormat = SimpleDateFormat("HH:mm", locale)
        val weekDayFormat = SimpleDateFormat("EEE", locale)
        val monthDayFormat = SimpleDateFormat("MMM dd", locale)
        val fullDateFormat = SimpleDateFormat("MMM dd, yyyy", locale)

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { time = date }

        return when {
            isSameDay(now, target) -> dayFormat.format(date)
            isSameWeek(now, target) -> weekDayFormat.format(date)
            isSameYear(now, target) -> monthDayFormat.format(date)
            else -> fullDateFormat.format(date)
        }
    }

    fun formatConversationTime(timestamp: Timestamp, locale: Locale = Locale.getDefault()): String {
        return formatConversationTime(timestamp.toDate(), locale)
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameWeek(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
    }

    private fun isSameYear(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }
}
