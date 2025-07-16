package com.apcs.worknestapp.domain.logic

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormater {
    fun format(date: Date, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat("dd MMMM, yyyy", locale)
        return formatter.format(date)
    }

    fun format(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        return format(Date(timestamp), locale)
    }

    fun format(timestamp: Timestamp, locale: Locale = Locale.getDefault()): String {
        return format(timestamp.toDate(), locale)
    }
}
