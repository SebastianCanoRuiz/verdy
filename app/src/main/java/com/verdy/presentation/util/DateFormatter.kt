package com.verdy.presentation.util

import android.content.Context
import com.verdy.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateFormatter {
    private val DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy")

    fun formatRelative(date: LocalDate?, context: Context): String {
        if (date == null) return context.getString(R.string.never)
        val today = LocalDate.now()
        val daysDiff = ChronoUnit.DAYS.between(today, date)
        return when {
            daysDiff == 0L -> context.getString(R.string.today)
            daysDiff == 1L -> context.getString(R.string.tomorrow)
            daysDiff == -1L -> context.getString(R.string.yesterday)
            daysDiff > 1L -> context.getString(R.string.in_days, daysDiff.toInt())
            else -> context.getString(R.string.days_ago, (-daysDiff).toInt())
        }
    }

    fun format(date: LocalDate?): String =
        date?.format(DATE_FORMAT) ?: "—"
}
