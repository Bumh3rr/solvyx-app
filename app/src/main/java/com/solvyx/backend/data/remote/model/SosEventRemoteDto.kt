package com.solvyx.backend.data.remote.model

data class SosEventRemoteDto(
    val id: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val notifiedPhones: String = ""
) {
    companion object {
        const val SOS_EVENTS = "sos_events"
        const val DATE = "date"
        const val NOTIFIED_PHONES = "notified_phones"
        const val CONTACT_COUNT = "contact_count"
        const val CREATED_AT = "created_at"
    }
}