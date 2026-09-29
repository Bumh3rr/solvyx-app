package com.solvyx.backend.data.remote.model

/** Field names of `users/{uid}/sos_events/{autoId}`: date and how many contacts were notified. */
object SosEventRemoteDto {
    const val SOS_EVENTS = "sos_events"
    const val DATE = "date"
    const val CONTACT_COUNT = "contact_count"
    const val CREATED_AT = "created_at"
}
