package com.solvyx.ui.components.common

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneNormalizeTest {

    @Test
    fun `plain 10 digit numbers are kept`() {
        assertEquals("7471234567", normalizeMexicanPhone("7471234567"))
    }

    @Test
    fun `formatting characters are dropped`() {
        assertEquals("7471234567", normalizeMexicanPhone("(747) 123-4567"))
    }

    @Test
    fun `country code is dropped`() {
        assertEquals("7471234567", normalizeMexicanPhone("+52 747 123 4567"))
    }

    @Test
    fun `country code with old mobile prefix is dropped`() {
        assertEquals("7471234567", normalizeMexicanPhone("+52 1 747 123 4567"))
    }

    @Test
    fun `short numbers stay short so validation can reject them`() {
        assertEquals("911", normalizeMexicanPhone("911"))
    }
}
