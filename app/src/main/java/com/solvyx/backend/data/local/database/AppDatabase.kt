package com.solvyx.backend.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.solvyx.backend.data.local.dao.BitacoraDao
import com.solvyx.backend.data.local.dao.ContactoSosDao
import com.solvyx.backend.data.local.dao.EjercicioDao
import com.solvyx.backend.data.local.dao.GuiaExtendidaDao
import com.solvyx.backend.data.local.dao.JournalingDao
import com.solvyx.backend.data.local.dao.LeccionDao
import com.solvyx.backend.data.local.dao.LeccionProgresoDao
import com.solvyx.backend.data.local.dao.LogroDao
import com.solvyx.backend.data.local.dao.PlanDao
import com.solvyx.backend.data.local.dao.PromptJournalingDao
import com.solvyx.backend.data.local.dao.ResultadoAssistDao
import com.solvyx.backend.data.local.dao.RutinaDao
import com.solvyx.backend.data.local.dao.RutinaProgresoDao
import com.solvyx.backend.data.local.dao.SosEventDao
import com.solvyx.backend.data.local.dao.UserDao
import com.solvyx.backend.data.local.entity.BitacoraEntity
import com.solvyx.backend.data.local.entity.ContactoSosEntity
import com.solvyx.backend.data.local.entity.Converters
import com.solvyx.backend.data.local.entity.EjercicioEntity
import com.solvyx.backend.data.local.entity.GuiaExtendidaEntity
import com.solvyx.backend.data.local.entity.JournalingEntryEntity
import com.solvyx.backend.data.local.entity.LeccionEntity
import com.solvyx.backend.data.local.entity.LeccionProgresoEntity
import com.solvyx.backend.data.local.entity.LogroEntity
import com.solvyx.backend.data.local.entity.PlanEntity
import com.solvyx.backend.data.local.entity.PromptJournalingEntity
import com.solvyx.backend.data.local.entity.ResultadoAssistEntity
import com.solvyx.backend.data.local.entity.RutinaEntity
import com.solvyx.backend.data.local.entity.RutinaPasoEntity
import com.solvyx.backend.data.local.entity.RutinaProgresoEntity
import com.solvyx.backend.data.local.entity.SosEventEntity
import com.solvyx.backend.data.local.entity.UserEntity
import com.solvyx.backend.data.local.migrations.MIGRATION_2_3
import com.solvyx.backend.data.local.migrations.MIGRATION_3_4

@TypeConverters(Converters::class)
@Database(
    entities = [
        // Catálogo / identidad
        UserEntity::class,
        ContactoSosEntity::class,
        // Resultados y eventos
        ResultadoAssistEntity::class,
        BitacoraEntity::class,
        SosEventEntity::class,
        // Gamificación
        PlanEntity::class,
        LogroEntity::class,
        // Módulo offline (v3)
        EjercicioEntity::class,
        GuiaExtendidaEntity::class,
        LeccionEntity::class,
        RutinaEntity::class,
        RutinaPasoEntity::class,
        PromptJournalingEntity::class,
        JournalingEntryEntity::class,
        // Progreso del usuario (v4)
        LeccionProgresoEntity::class,
        RutinaProgresoEntity::class
    ],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contactoSosDao(): ContactoSosDao
    abstract fun resultadoAssistDao(): ResultadoAssistDao
    abstract fun bitacoraDao(): BitacoraDao
    abstract fun planDao(): PlanDao
    abstract fun logroDao(): LogroDao
    abstract fun sosEventDao(): SosEventDao

    // DAOs del módulo offline (v3)
    abstract fun ejercicioDao(): EjercicioDao
    abstract fun guiaExtendidaDao(): GuiaExtendidaDao
    abstract fun leccionDao(): LeccionDao
    abstract fun rutinaDao(): RutinaDao
    abstract fun promptJournalingDao(): PromptJournalingDao
    abstract fun journalingDao(): JournalingDao

    // DAOs de progreso del usuario (v4)
    abstract fun leccionProgresoDao(): LeccionProgresoDao
    abstract fun rutinaProgresoDao(): RutinaProgresoDao

    companion object {
        val SEED_CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                listOf("racha_3", "racha_7", "racha_10", "racha_15", "racha_30").forEach { id ->
                    db.execSQL(
                        "INSERT INTO logros (id, unlocked, fechaUnlock) VALUES ('$id', 0, NULL)"
                    )
                }
            }
        }
    }
}
