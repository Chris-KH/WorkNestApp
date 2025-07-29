package com.apcs.worknestapp.domain.logic

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormater {
    fun format(
        date: Date,
        formatString: String = "dd MMMM, yyyy",
        locale: Locale = Locale.getDefault(),
    ): String {
        val formatter = SimpleDateFormat(formatString, locale)
        return formatter.format(date)
    }

    fun format(
        timestamp: Long,
        formatString: String = "dd MMMM, yyyy",
        locale: Locale = Locale.getDefault(),
    ): String {
        return format(
            date = Date(timestamp),
            formatString = formatString,
            locale = locale,
        )
    }

    fun format(
        timestamp: Timestamp,
        formatString: String = "dd MMMM, yyyy",
        locale: Locale = Locale.getDefault(),
    ): String {
        return format(
            timestamp.toDate(),
            formatString = formatString,
            locale = locale,
        )
    }
}
