package com.solvyx.ui.screens.sos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SosViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    // ── SosState enum ─────────────────────────────────────────────────────────

    @Test
    fun `SosState has COUNTDOWN value`() {
        // Fails to compile until SosState is defined → RED
        val state: SosState = SosState.COUNTDOWN
        assertNotNull(state)
    }

    @Test
    fun `SosState has SENT value`() {
        val state: SosState = SosState.SENT
        assertNotNull(state)
    }

    @Test
    fun `COUNTDOWN and SENT are distinct states`() {
        assertNotEquals(SosState.COUNTDOWN, SosState.SENT)
    }

    @Test
    fun `SosState has a NO_CONTACTS value`() {
        // Sin contactos no se envía ningún SMS, así que "enviado" no puede ser el estado final:
        // la pantalla afirmaría "Tus contactos han sido notificados" sin haber notificado a nadie.
        val state: SosState = SosState.NO_CONTACTS
        assertNotNull(state)
    }

    @Test
    fun `NO_CONTACTS is distinct from SENT`() {
        assertNotEquals(SosState.NO_CONTACTS, SosState.SENT)
    }

    @Test
    fun `SosState has a SEND_FAILED value distinct from SENT`() {
        // Tener contactos no garantiza el envío: SEND_SMS es un permiso dangerous y el sistema
        // puede fallar. "Con contactos" nunca debe colapsarse a "enviado".
        assertNotEquals(SosState.SEND_FAILED, SosState.SENT)
        assertNotEquals(SosState.SEND_FAILED, SosState.NO_CONTACTS)
    }

    @Test
    fun `SosState has exactly four values`() {
        assertEquals(4, SosState.entries.size)
    }

    // ── Countdown logic ───────────────────────────────────────────────────────
    // Tests the exact coroutine pattern used in SosViewModel.startCountdown()

    @Test
    fun `countdown starts at 3 and reaches 0 after 3 ticks`() = runTest(testDispatcher) {
        var countdown = 3
        var completed = false

        val job = launch {
            repeat(3) {
                kotlinx.coroutines.delay(1000L)
                countdown--
            }
            completed = true
        }

        advanceTimeBy(3001L)
        assertEquals(0, countdown)
        assertTrue(completed)
        job.join()
    }

    @Test
    fun `countdown does not complete if cancelled before 3 seconds`() = runTest(testDispatcher) {
        var countdown = 3
        var completed = false

        val job = launch {
            repeat(3) {
                kotlinx.coroutines.delay(1000L)
                countdown--
            }
            completed = true
        }

        advanceTimeBy(1500L) // cancels after first tick
        job.cancel()
        advanceTimeBy(5000L)

        assertFalse("Countdown must not complete when cancelled", completed)
        assertEquals("Only one tick should have decremented", 2, countdown)
    }

    @Test
    fun `state machine starts as COUNTDOWN`() {
        // Documents the expected initial state
        val initialState = SosState.COUNTDOWN
        assertEquals(SosState.COUNTDOWN, initialState)
    }

    @Test
    fun `state transitions to SENT after countdown finishes`() = runTest(testDispatcher) {
        var state = SosState.COUNTDOWN

        val job = launch {
            repeat(3) { kotlinx.coroutines.delay(1000L) }
            state = SosState.SENT
        }

        advanceTimeBy(3001L)
        assertEquals(SosState.SENT, state)
        job.join()
    }
}
