package com.solvyx.ui.screens.directory

import com.solvyx.ui.screens.directory.model.DirectoryCategory
import com.solvyx.ui.screens.directory.model.DirectoryEntry
import java.text.Normalizer

private val DiacriticsRegex = "\\p{Mn}+".toRegex()

/** Lowercase without accents, so "psicologo" finds "Psicólogo". */
internal fun String.normalizedForSearch(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD).replace(DiacriticsRegex, "").lowercase()

private fun DirectoryEntry.searchableText(): String =
    listOfNotNull(name, description, specialty, address, category.singularLabel, *tags.toTypedArray())
        .joinToString(" ")
        .normalizedForSearch()

/** What the user asked the directory to show. `category == null` means "Todos". */
data class DirectoryFilter(
    val query: String = "",
    val category: DirectoryCategory? = null,
    val onlyFree: Boolean = false
)

/**
 * Entries matching [filter]: every word of the query must appear somewhere in the entry
 * (name, description, specialty, address, category or tags), accent- and case-insensitive.
 */
fun List<DirectoryEntry>.filterBy(filter: DirectoryFilter): List<DirectoryEntry> {
    val words = filter.query.normalizedForSearch().split(" ").filter { it.isNotBlank() }
    return filter { entry ->
        (filter.category == null || entry.category == filter.category) &&
            (!filter.onlyFree || entry.isFree) &&
            entry.searchableText().let { text -> words.all { it in text } }
    }
}
