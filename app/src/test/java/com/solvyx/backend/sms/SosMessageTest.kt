package com.solvyx.backend.sms

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class SosMessageTest {

    private val defaultLocale = Locale.getDefault()

    @After
    fun restoreLocale() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `without a location the message has no link`() {
        val message = sosMessage(location = null)

        assertFalse(message.contains("maps"))
        assertTrue(message.startsWith("Hola, estoy en crisis y necesito apoyo."))
    }

    @Test
    fun `with a location the message carries a maps link to it`() {
        val message = sosMessage(Coordinates(latitude = 17.5512345, longitude = -99.5001234))

        assertTrue(message.contains("https://maps.google.com/?q=17.55123,-99.50012"))
    }

    @Test
    fun `coordinates use a dot even on a phone set to a comma-decimal language`() {
        Locale.setDefault(Locale.GERMANY)

        val link = mapsLink(Coordinates(latitude = 17.5, longitude = -99.5))

        assertEquals("https://maps.google.com/?q=17.50000,-99.50000", link)
    }
}
