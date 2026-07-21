package com.solvyx.backend.data.remote.model

data class JournalRemoteDto(
    val date: Long,
    val mood: String,
    val consumed: Boolean,
    val substance: String? = null,
    val note: String? = null,
    val cantidadAprox: String? = null,
    val notaContexto: String? = null,
    val metaLograda: Boolean = false
) {
    companion object {
        const val JOURNAL = "journal"
        const val DATE = "date"
        const val MOOD = "mood"
        const val CONSUMED = "consumed"
        const val SUBSTANCE = "substance"
        const val NOTE = "note"
        const val CANTIDAD_APROX = "cantidad_aprox"
        const val NOTA_CONTEXTO = "nota_contexto"
        const val META_LOGRADA = "meta_lograda"
        const val CREATED_AT = "created_at"
        const val UPDATED_AT = "updated_at"
    }
}