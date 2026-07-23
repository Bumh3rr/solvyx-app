// app/src/main/java/com/solvyx/backend/data/remote/datasource/JournalRemoteDataSource.kt
package com.solvyx.backend.data.remote.datasource

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.backend.data.remote.model.JournalRemoteDto
import com.solvyx.backend.data.remote.model.UserRemoteDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val zone = ZoneId.systemDefault()
    private fun dateId(date: LocalDate): String = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

    private fun journalCol(uid: String) =
        firestore.collection(UserRemoteDto.USERS).document(uid)
            .collection(JournalRemoteDto.JOURNAL)

    /**
     * Merge a nivel de campo: solo escribe los campos de registro no nulos + updated_at. Nunca
     * escribe meta_lograda (eso es setMetaLograda). Así el ánimo rápido no borra un consumed
     * previo y un registro completo no borra un meta_lograda previo.
     */
    suspend fun saveEntry(uid: String, entry: JournalEntry) {
        val data = buildMap<String, Any?> {
            put(JournalRemoteDto.DATE, entry.date.atStartOfDay(zone).toInstant().toEpochMilli())
            entry.mood?.let { put(JournalRemoteDto.MOOD, it) }
            entry.note?.let { put(JournalRemoteDto.NOTE, it) }
            entry.consumed?.let { put(JournalRemoteDto.CONSUMED, it) }
            entry.substance?.let { put(JournalRemoteDto.SUBSTANCE, it) }
            entry.cantidadAprox?.let { put(JournalRemoteDto.CANTIDAD_APROX, it) }
            entry.notaContexto?.let { put(JournalRemoteDto.NOTA_CONTEXTO, it) }
            put(JournalRemoteDto.UPDATED_AT, FieldValue.serverTimestamp())
            put(JournalRemoteDto.CREATED_AT, FieldValue.serverTimestamp())
        }
        journalCol(uid).document(dateId(entry.date)).set(data, SetOptions.merge()).await()
    }

    suspend fun getEntry(uid: String, date: LocalDate): JournalEntry? =
        journalCol(uid).document(dateId(date)).get().await().toJournalEntry()

    fun observeAll(uid: String): Flow<List<JournalEntry>> = callbackFlow {
        val registration = journalCol(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Firestore stops retrying permanent errors (permission denied, expired session)
                // on its own; without this the Flow would never emit again and Journey would be
                // stuck on "Loading" forever instead of falling back to an empty state.
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot.documents.mapNotNull { it.toJournalEntry() })
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun setMetaLograda(uid: String, date: LocalDate, value: Boolean) {
        journalCol(uid).document(dateId(date)).set(
            mapOf(
                JournalRemoteDto.META_LOGRADA to value,
                JournalRemoteDto.DATE to date.atStartOfDay(zone).toInstant().toEpochMilli(),
                JournalRemoteDto.UPDATED_AT to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
    }

    private fun DocumentSnapshot.toJournalEntry(): JournalEntry? {
        val dateMillis = getLong(JournalRemoteDto.DATE)
            ?: runCatching { LocalDate.parse(id).atStartOfDay(zone).toInstant().toEpochMilli() }.getOrNull()
            ?: return null
        return JournalEntry(
            date = Instant.ofEpochMilli(dateMillis).atZone(zone).toLocalDate(),
            mood = getString(JournalRemoteDto.MOOD),
            note = getString(JournalRemoteDto.NOTE),
            consumed = getBoolean(JournalRemoteDto.CONSUMED),
            substance = getString(JournalRemoteDto.SUBSTANCE),
            cantidadAprox = getString(JournalRemoteDto.CANTIDAD_APROX),
            notaContexto = getString(JournalRemoteDto.NOTA_CONTEXTO),
            metaLograda = getBoolean(JournalRemoteDto.META_LOGRADA) ?: false
        )
    }
}
