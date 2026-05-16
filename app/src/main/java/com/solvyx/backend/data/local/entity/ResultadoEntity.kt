package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resultados")

data class ResultadoEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val sustanciaId: String,

    val puntaje: Int,

    val nivel: String,

    val recomendacion: String,

    val fecha: Long = System.currentTimeMillis()
)