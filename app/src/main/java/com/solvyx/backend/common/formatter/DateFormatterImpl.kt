package com.solvyx.backend.common.formatter

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateFormatterImpl @Inject constructor() : DateFormatter {
    private val locale = Locale.forLanguageTag("es-MX")
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)
    private val zone = ZoneId.systemDefault()

    override fun parse(text: String?): Date? {
        if (text.isNullOrBlank()) return null
        val localDate = LocalDate.parse(text, formatter)
        return Date.from(localDate.atStartOfDay(zone).toInstant())
    }

    override fun format(date: Date?): String? {
        if (date == null) return null
        val localDate = Instant.ofEpochMilli(date.time).atZone(zone).toLocalDate()
        return localDate.format(formatter)
    }

    override fun format(long: Long?, pattern: String): String {
        if (long == null) return ""
        val localDate = Instant.ofEpochMilli(long).atZone(zone).toLocalDate()
        val customFormatter = DateTimeFormatter.ofPattern(pattern, locale)
        return localDate.format(customFormatter)
    }
}