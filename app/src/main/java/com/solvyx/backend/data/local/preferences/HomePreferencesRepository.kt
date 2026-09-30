package com.solvyx.backend.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.solvyx.solvyxDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Home choices kept on the device. */
@Singleton
class HomePreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /** Last day this account opened Home on this phone; null if Berto never introduced himself. */
    suspend fun lastHomeVisit(): LocalDate? =
        context.solvyxDataStore.data.first()[LAST_HOME_VISIT]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    suspend fun markHomeVisit(date: LocalDate) {
        context.solvyxDataStore.edit { it[LAST_HOME_VISIT] = date.toString() }
    }

    /** On sign-out or account deletion: whoever signs in next gets Berto's introduction. */
    suspend fun clear() {
        context.solvyxDataStore.edit { it.remove(LAST_HOME_VISIT) }
    }

    private companion object {
        val LAST_HOME_VISIT = stringPreferencesKey("home_last_visit_date")
    }
}
