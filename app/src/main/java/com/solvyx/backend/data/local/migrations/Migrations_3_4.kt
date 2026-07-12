package com.solvyx.backend.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migración v3 → v4 del esquema Room de Solvyx.
 *
 * Cambios:
 * 1. Nueva tabla `leccion_progreso` para registrar el avance de lectura
 *    del usuario en las lecciones del catálogo educativo.
 *    PK = `slug` (string), `leida: Boolean`, `fechaLectura: Long?`.
 * 2. Nueva tabla `rutina_progreso` para registrar los check-ins de pasos
 *    completados de una rutina. Una fila por (paso, fecha, instancia);
 *    la deduplicación del día se hace en la query con un rango.
 *
 * Decisiones:
 * - Las dos tablas se crean con `CREATE TABLE IF NOT EXISTS` para que
 *   la migración sea idempotente ante retries.
 * - Los índices usan `IF NOT EXISTS` por la misma razón.
 * - NO se hace DROP ni ALTER de las tablas v3: la migración es
 *   puramente aditiva (preserva datos del usuario al 100%).
 * - `leccion_progreso.slug` es TEXT PRIMARY KEY; SQLite permite PRIMARY KEY
 *   sin autoincrement, no es `INTEGER` y por tanto no hay `ROWID` implícito.
 * - `rutina_progreso.id` es INTEGER PRIMARY KEY AUTOINCREMENT para tener
 *   ROWID estable y poder hacer upsert futuro si se requiere.
 */
val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ---------------------------------------------------------------
        // 1) CREATE TABLE leccion_progreso
        // ---------------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS leccion_progreso (
                slug TEXT NOT NULL PRIMARY KEY,
                leida INTEGER NOT NULL DEFAULT 0,
                fechaLectura INTEGER
            )
            """.trimIndent()
        )

        // ---------------------------------------------------------------
        // 2) CREATE TABLE rutina_progreso
        // ---------------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS rutina_progreso (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                rutinaPasoId INTEGER NOT NULL,
                fecha INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_rutina_progreso_paso ON rutina_progreso(rutinaPasoId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_rutina_progreso_fecha ON rutina_progreso(fecha)")
    }
}
