package com.solvyx.ui.screens.directory

import com.solvyx.ui.screens.directory.data.DirectoryData
import com.solvyx.ui.screens.directory.model.DirectoryCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DirectorySearchTest {

    private val entries = DirectoryData.entries

    @Test
    fun `no filter returns every entry`() {
        assertEquals(entries, entries.filterBy(DirectoryFilter()))
    }

    @Test
    fun `search ignores accents and case`() {
        val results = entries.filterBy(DirectoryFilter(query = "PSICOLOGO"))
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.category == DirectoryCategory.PSYCHOLOGIST })
    }

    @Test
    fun `search matches the address`() {
        val results = entries.filterBy(DirectoryFilter(query = "burocratas"))
        assertEquals(listOf("clinica-salud-emocional"), results.map { it.id })
    }

    @Test
    fun `every query word has to match`() {
        val results = entries.filterBy(DirectoryFilter(query = "adicciones ansiedad"))
        assertEquals(listOf("psy-edgar"), results.map { it.id })
    }

    @Test
    fun `category and free filters combine`() {
        val results = entries.filterBy(
            DirectoryFilter(category = DirectoryCategory.PSYCHOLOGIST, onlyFree = true)
        )
        assertTrue(results.isEmpty())
    }

    @Test
    fun `only free keeps free entries`() {
        val results = entries.filterBy(DirectoryFilter(onlyFree = true))
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.isFree })
    }
}
