---
description: Diseño y aplicación de migraciones Room para Solvyx. Versionado de schema, validación sin pérdida de datos, manejo de cambios destructivos.
---

# Skill: Room Migrations

Esta skill te entrega las reglas para diseñar y aplicar migraciones Room en Solvyx sin perder datos de usuario. Aplícala siempre que cambies la versión de `@Database`.

## Principios

1. **Nunca destruir datos del usuario sin una decisión explícita y documentada.**
2. **Cada cambio de schema es una migración, no un reset.** La única excepción justificada es un cambio de versión mayor donde el modelo de datos ya no es compatible.
3. **Las migraciones son testeadas antes de mergear.** Test unitario obligatorio.
4. **Las migraciones son idempotentes.** Pueden correr dos veces sin romper nada (Room las marca como corridas, pero en testing se simula reinstalación).

## Cuándo aumentar la versión

| Cambio | Versión |
|---|---|
| Añadir columna con default o nullable | Sí, migración |
| Añadir tabla | Sí, migración |
| Renombrar columna | Sí, migración con `ALTER TABLE ... RENAME COLUMN` |
| Cambiar tipo de columna (int → long) | Sí, migración explícita |
| Añadir índice | Sí, migración |
| Eliminar columna | Sí, migración; documentar impacto |
| Eliminar tabla | Sí, migración; documentar impacto |

## Plantilla de migración aditiva

```kotlin
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            ALTER TABLE bitacora_entries
            ADD COLUMN suenoHoras INTEGER
        """.trimIndent())
        
        db.execSQL("""
            ALTER TABLE bitacora_entries
            ADD COLUMN comida TEXT
        """.trimIndent())
        
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_bitacora_suenoHoras
            ON bitacora_entries(sueñoHoras)
        """.trimIndent())
    }
}
```

## Plantilla de migración con rename de tabla

```kotlin
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE old_table RENAME TO new_table")
    }
}
```

## Plantilla de migración con copia de datos

```kotlin
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Crear nueva tabla
        db.execSQL("""
            CREATE TABLE new_bitacora_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                fecha INTEGER NOT NULL,
                animo TEXT NOT NULL,
                -- ... nuevos campos
                notaPrivadaCifrada TEXT
            )
        """.trimIndent())
        
        // 2. Copiar datos
        db.execSQL("""
            INSERT INTO new_bitacora_entries (id, fecha, animo, /* ... */)
            SELECT id, fecha, animo, /* ... */ FROM bitacora_entries
        """.trimIndent())
        
        // 3. Eliminar vieja
        db.execSQL("DROP TABLE bitacora_entries")
        
        // 4. Renombrar
        db.execSQL("ALTER TABLE new_bitacora_entries RENAME TO bitacora_entries")
        
        // 5. Recrear índices
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_bitacora_fecha ON bitacora_entries(fecha)")
    }
}
```

## Registro en AppDatabase

```kotlin
@Database(
    entities = [ /* ... */ ],
    version = 7,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    // ...
    
    companion object {
        val MIGRATIONS = arrayOf(
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7
        )
    }
}

// En AppModule.kt:
Room.databaseBuilder(context, AppDatabase::class.java, "solvyx.db")
    .addMigrations(*AppDatabase.MIGRATIONS)
    // NO usar fallbackToDestructiveMigration en producción.
    // Si es estrictamente necesario, documentar:
    // .fallbackToDestructiveMigrationOnDowngrade()
    .build()
```

## Exportación de schema para tests

En `app/build.gradle.kts`:

```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```

Esto exporta el JSON del schema en cada versión. Los tests de migración lo comparan.

## Test de migración (AndroidX Room Testing)

```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )
    
    @Test
    fun migrate_4_to_5_preserves_data() {
        // 1. Crear BD en versión 4 con datos
        helper.createDatabase("test.db", 4).use { db ->
            db.execSQL("""
                INSERT INTO bitacora_entries (fecha, animo, consumo, sustancia)
                VALUES (?, ?, ?, ?)
            """.trimIndent(), arrayOf(123L, "bien", 0, null))
        }
        
        // 2. Aplicar migración a versión 5
        helper.runMigrationsAndValidate("test.db", 5, true, MIGRATION_4_5).use { db ->
            val cursor = db.query("SELECT fecha, animo, suenoHoras FROM bitacora_entries")
            cursor.moveToFirst()
            assertEquals(123L, cursor.getLong(0))
            assertEquals("bien", cursor.getString(1))
            assertNull(cursor.getString(2))  // nuevo campo, null por default
        }
    }
}
```

## Cuándo SÍ usar `fallbackToDestructiveMigration`

Solo en estos casos, con justificación documentada:

1. Cambio mayor de modelo donde los datos existentes no son compatibles (ej. rediseño completo de bitácora).
2. Beta temprana donde los datos no son producción todavía.
3. Después de pedir permiso explícito al usuario y ofrecer backup.

## Anti-patrones prohibidos

1. `fallbackToDestructiveMigration()` sin comentario de justificación.
2. Migración sin test.
3. `DROP TABLE` sin migración previa (rompe histórico de datos).
4. Migrations que usan `db.execSQL()` con strings concatenados (riesgo de SQL injection).
5. Cambiar el nombre de una columna sin migración (cambia el schema sin migrar).
6. Asumir que `Room` reescribe el schema automáticamente — eso es `fallbackToDestructiveMigration`, no una migración.

## Checklist antes de mergear una migración

- [ ] Versión incrementada en `@Database(version = N)`.
- [ ] `Migration` class escrita en archivo versionado (ej. `Migrations_4_5.kt`).
- [ ] Registrada en `AppDatabase.MIGRATIONS`.
- [ ] Test de migración pasa.
- [ ] Schema JSON exportado y commiteado.
- [ ] Documentado en CHANGELOG del proyecto.
