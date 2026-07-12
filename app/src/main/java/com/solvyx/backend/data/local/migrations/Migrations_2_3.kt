package com.solvyx.backend.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migración v2 → v3 del esquema Room de Solvyx.
 *
 * Cambios:
 * 1. Ampliación de la tabla `bitacora` con campos extendidos (todos nullable,
 *    más `updatedAt` NOT NULL DEFAULT 0 para compatibilidad con filas v2).
 * 2. Creación de las tablas nuevas para el módulo offline:
 *    - `ejercicios`
 *    - `guias_extendidas`
 *    - `lecciones`
 *    - `rutinas`
 *    - `rutina_pasos`
 *    - `prompts_journaling`
 *    - `journaling_entries`
 * 3. Creación de todos los índices declarados en las entities nuevas.
 *
 * Decisiones:
 * - NO se hace DROP de columnas: la migración es aditiva y preserva los datos
 *   del usuario al 100%.
 * - Las nuevas tablas se crean con `CREATE TABLE IF NOT EXISTS` para hacer la
 *   migración idempotente en escenarios de retry (ej. crash durante migrate).
 * - Los índices también usan `IF NOT EXISTS` por la misma razón.
 * - `updatedAt` para `bitacora` se añade como `INTEGER NOT NULL DEFAULT 0`.
 *   Las filas existentes quedan con `updatedAt = 0`; las nuevas se llenan
 *   desde Kotlin con `System.currentTimeMillis()`.
 * - El campo `activo` en cada tabla nueva queda con `activo = 1` por defecto
 *   (semánticamente `true`) gracias al DEFAULT.
 *
 * Notas de orden de operaciones:
 * - Las FKs (CASCADE de `rutina_pasos` → `rutinas`) requieren que la tabla
 *   padre exista antes de la hija, pero Room/SQLite NO enforza FKs hasta que
 *   se hace `PRAGMA foreign_keys = ON` (que Room activa al abrir). En este
 *   archivo solo creamos la estructura; la integridad se valida al primer uso.
 */
val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ---------------------------------------------------------------
        // 1) ALTER TABLE bitacora: añadir columnas extendidas
        // ---------------------------------------------------------------
        db.execSQL("ALTER TABLE bitacora ADD COLUMN suenoHoras INTEGER")
        db.execSQL("ALTER TABLE bitacora ADD COLUMN suenoCalidad INTEGER")
        db.execSQL("ALTER TABLE bitacora ADD COLUMN comio INTEGER")
        db.execSQL("ALTER TABLE bitacora ADD COLUMN calidadComida INTEGER")
        db.execSQL("ALTER TABLE bitacora ADD COLUMN actividadFisica TEXT")
        db.execSQL("ALTER TABLE bitacora ADD COLUMN contextoSocial TEXT")
        db.execSQL("ALTER TABLE bitacora ADD COLUMN detonantePrincipal TEXT")
        db.execSQL("ALTER TABLE bitacora ADD COLUMN nivelAnsiedad INTEGER")
        db.execSQL("ALTER TABLE bitacora ADD COLUMN tuvoCraving INTEGER")
        db.execSQL("ALTER TABLE bitacora ADD COLUMN ejercicioFisico INTEGER")
        db.execSQL("ALTER TABLE bitacora ADD COLUMN notaPrivada TEXT")
        db.execSQL("ALTER TABLE bitacora ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")

        // ---------------------------------------------------------------
        // 2) CREATE TABLE ejercicios
        // ---------------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ejercicios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                slug TEXT NOT NULL,
                nombre TEXT NOT NULL,
                tipo TEXT NOT NULL,
                duracionMinutos INTEGER NOT NULL,
                descripcionCorta TEXT NOT NULL,
                pasos TEXT NOT NULL,
                ttsText TEXT NOT NULL,
                iconAsset TEXT,
                orden INTEGER NOT NULL DEFAULT 0,
                activo INTEGER NOT NULL DEFAULT 1,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_ejercicios_slug ON ejercicios(slug)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_ejercicios_tipo ON ejercicios(tipo)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_ejercicios_orden ON ejercicios(orden)")

        // ---------------------------------------------------------------
        // 3) CREATE TABLE guias_extendidas
        // ---------------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS guias_extendidas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                slug TEXT NOT NULL,
                titulo TEXT NOT NULL,
                categoria TEXT NOT NULL,
                descripcionCorta TEXT NOT NULL,
                contenido TEXT NOT NULL,
                iconAsset TEXT,
                orden INTEGER NOT NULL DEFAULT 0,
                activo INTEGER NOT NULL DEFAULT 1,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_guias_extendidas_slug ON guias_extendidas(slug)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_guias_categoria ON guias_extendidas(categoria)")

        // ---------------------------------------------------------------
        // 4) CREATE TABLE lecciones
        // ---------------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS lecciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                slug TEXT NOT NULL,
                sustancia TEXT NOT NULL,
                tema TEXT NOT NULL,
                titulo TEXT NOT NULL,
                contenido TEXT NOT NULL,
                duracionLecturaMinutos INTEGER NOT NULL DEFAULT 0,
                orden INTEGER NOT NULL DEFAULT 0,
                activo INTEGER NOT NULL DEFAULT 1,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_lecciones_slug ON lecciones(slug)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_lecciones_sustancia ON lecciones(sustancia)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_lecciones_sustancia_tema ON lecciones(sustancia, tema)")

        // ---------------------------------------------------------------
        // 5) CREATE TABLE rutinas
        // ---------------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS rutinas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                slug TEXT NOT NULL,
                nombre TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                horaSugerida INTEGER NOT NULL DEFAULT 0,
                iconAsset TEXT,
                activo INTEGER NOT NULL DEFAULT 1,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_rutinas_slug ON rutinas(slug)")

        // ---------------------------------------------------------------
        // 6) CREATE TABLE rutina_pasos (hija con FK CASCADE)
        // ---------------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS rutina_pasos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                rutinaId INTEGER NOT NULL,
                orden INTEGER NOT NULL,
                titulo TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                duracionSegundos INTEGER NOT NULL DEFAULT 0,
                iconAsset TEXT,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY (rutinaId) REFERENCES rutinas(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_rutina_pasos_rutinaId ON rutina_pasos(rutinaId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_rutina_pasos_orden ON rutina_pasos(rutinaId, orden)")

        // ---------------------------------------------------------------
        // 7) CREATE TABLE prompts_journaling
        // ---------------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS prompts_journaling (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                categoria TEXT NOT NULL,
                texto TEXT NOT NULL,
                orden INTEGER NOT NULL DEFAULT 0,
                activo INTEGER NOT NULL DEFAULT 1,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_prompts_categoria ON prompts_journaling(categoria)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_prompts_orden ON prompts_journaling(orden)")

        // ---------------------------------------------------------------
        // 8) CREATE TABLE journaling_entries
        // ---------------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS journaling_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fecha INTEGER NOT NULL,
                promptId INTEGER,
                promptTexto TEXT,
                contenido TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_journaling_fecha ON journaling_entries(fecha)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_journaling_promptId ON journaling_entries(promptId)")
    }
}