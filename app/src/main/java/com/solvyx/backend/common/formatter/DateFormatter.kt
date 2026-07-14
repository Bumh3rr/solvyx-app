package com.solvyx.backend.common.formatter

import java.util.Date

interface DateFormatter {
    fun parse(text: String?): Date?
    fun format(date: Date?): String?
    fun format(long: Long?, pattern: String): String
}