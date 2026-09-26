package com.solvyx.ui.components.common

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneFormatTest {

    @Test
    fun `toll free numbers group as 3-3-4`() {
        assertEquals("800 911 2000", formatMexicanPhone("8009112000"))
    }

    @Test
    fun `mexico city numbers group as 2-4-4`() {
        assertEquals("55 5259 8121", formatMexicanPhone("5552598121"))
    }

    @Test
    fun `guerrero numbers group as 3-3-4`() {
        assertEquals("747 494 9445", formatMexicanPhone("7474949445"))
    }

    @Test
    fun `numbers that are not 10 digits are returned untouched`() {
        assertEquals("911", formatMexicanPhone("911"))
        assertEquals("747-494", formatMexicanPhone("747-494"))
    }
}
