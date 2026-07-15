package com.solvyx.backend.data.remote.model

data class JournalRemoteDto(
    val date: Long,
    val mood: String,
    val consumed: Boolean,
    val substance: String? = null,
    val note: String? = null
) {
    companion object {
        const val JOURNAL = "journal"
        const val DATE = "date"
        const val MOOD = "mood"
        const val CONSUMED = "consumed"
        const val SUBSTANCE = "substance"
        const val NOTE = "note"
        const val CREATED_AT = "created_at"
    }
}