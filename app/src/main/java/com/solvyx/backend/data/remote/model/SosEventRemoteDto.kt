package com.solvyx.backend.data.remote.model

data class SosEventRemoteDto(
    val id: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val notifiedPhones: String = ""
)