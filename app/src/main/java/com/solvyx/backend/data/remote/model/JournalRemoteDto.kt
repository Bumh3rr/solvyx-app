package com.solvyx.backend.data.remote.model

data class JournalRemoteDto(
    val date: Long,
    val mood: String,
    val consumed: Boolean,
    val substance: String? = null,
    val note: String? = null
)