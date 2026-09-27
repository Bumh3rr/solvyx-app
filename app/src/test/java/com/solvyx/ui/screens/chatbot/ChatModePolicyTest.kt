package com.solvyx.ui.screens.chatbot

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatModePolicyTest {

    private var ahora = 1_000_000L
    private val policy = ChatModePolicy(esperaMs = 60_000L, reloj = { ahora })

    @Test
    fun `uses the API when connected and there was no failure`() {
        assertTrue(policy.debeUsarApi(conectado = true))
    }

    @Test
    fun `uses the tree when there is no network`() {
        assertFalse(policy.debeUsarApi(conectado = false))
    }

    @Test
    fun `uses the tree during the 60 s after a failure`() {
        policy.registrarFallo()
        ahora += 59_999L

        assertTrue(policy.enEspera)
        assertFalse(policy.debeUsarApi(conectado = true))
    }

    @Test
    fun `goes back to the API once the 60 s have passed`() {
        policy.registrarFallo()
        ahora += 60_000L

        assertFalse(policy.enEspera)
        assertTrue(policy.debeUsarApi(conectado = true))
    }

    @Test
    fun `retry ignores the wait but still needs network`() {
        policy.registrarFallo()

        assertTrue(policy.debeUsarApi(conectado = true, esReintento = true))
        assertFalse(policy.debeUsarApi(conectado = false, esReintento = true))
    }

    @Test
    fun `a success clears the wait`() {
        policy.registrarFallo()
        policy.registrarExito()

        assertTrue(policy.debeUsarApi(conectado = true))
    }
}
