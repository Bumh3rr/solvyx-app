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

/** SOS choices kept on the device. */
@Singleton
class SosPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Whether the user chose to add a maps link with their position to the SOS text. Opt-in: the
     * location permission alone is not the decision, the switch in "Mi red de apoyo" is.
     */
    val shareLocation: Flow<Boolean> =
        context.solvyxDataStore.data.map { it[SHARE_LOCATION] ?: false }

    suspend fun setShareLocation(enabled: Boolean) {
        context.solvyxDataStore.edit { it[SHARE_LOCATION] = enabled }
    }

    /** After deleting the account: whoever uses the phone next decides again. */
    suspend fun clear() {
        context.solvyxDataStore.edit { it.remove(SHARE_LOCATION) }
    }

    private companion object {
        val SHARE_LOCATION = booleanPreferencesKey("sos_share_location")
    }
}
