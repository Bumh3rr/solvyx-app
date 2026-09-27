package com.solvyx.backend.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.solvyx.solvyxDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Preferencias del chat de Berto guardadas en el dispositivo. */
@Singleton
class ChatPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Whether the user accepted that their chat messages go to our server and an external AI.
     * Until then Berto only answers with the local decision trees and nothing is sent.
     */
    val aiConsentGranted: Flow<Boolean> =
        context.solvyxDataStore.data.map { it[AI_CONSENT_GRANTED] ?: false }

    suspend fun grantAiConsent() {
        context.solvyxDataStore.edit { it[AI_CONSENT_GRANTED] = true }
    }

    private companion object {
        // New key on purpose: the old "notice seen" flag was not consent and its text was inaccurate.
        val AI_CONSENT_GRANTED = booleanPreferencesKey("chat_ai_consent_granted")
    }
}
