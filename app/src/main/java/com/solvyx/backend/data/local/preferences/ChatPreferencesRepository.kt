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

    /** Si el usuario ya leyó el aviso de que, con internet, sus mensajes van al servidor. */
    val avisoPrivacidadVisto: Flow<Boolean> =
        context.solvyxDataStore.data.map { it[AVISO_PRIVACIDAD_VISTO] ?: false }

    suspend fun marcarAvisoPrivacidadVisto() {
        context.solvyxDataStore.edit { it[AVISO_PRIVACIDAD_VISTO] = true }
    }

    private companion object {
        val AVISO_PRIVACIDAD_VISTO = booleanPreferencesKey("chat_aviso_privacidad_visto")
    }
}
