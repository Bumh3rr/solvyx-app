---
description: Convenciones de Solvyx para diseñar entidades, DAOs, índices y type converters de Room. Carga al invocar backend-data-architect.
---

# Skill: Room Schema Design

Esta skill te entrega las convenciones del proyecto Solvyx para diseñar la capa de persistencia con Room. Aplícala siempre que crees o modifiques `@Entity`, `@Dao`, `@Database` o `Converters`.

## Convenciones del proyecto

### Anotación de Entity

```kotlin
@Entity(
    tableName = "ejercicios",
    indices = [
        Index(value = ["tipo"], name = "idx_ejercicios_tipo"),
        Index(value = ["orden"], name = "idx_ejercicios_orden")
    ]
)
data class EjercicioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val slug: String,                    // estable para sincronización, kebab-case
    val tipo: TipoEjercicio,             // enum
    val nombre: String,
    val duracionMinutos: Int,
    val pasos: String,                   // JSON serializado
    val activo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

### Reglas duras

1. **Primary key:** `Long` autogenerado para entidades internas. `String` (UUID o slug) si necesitas referencia estable externa.
2. **Slug estable:** cada entidad que pueda venir de un seed o sincronización tiene un campo `slug: String` único con `@Index(unique = true)`.
3. **Timestamps:** `createdAt` y `updatedAt` en milisegundos (`System.currentTimeMillis()`).
4. **Soft delete:** campo `activo: Boolean = true`. Nunca `DELETE FROM` directo en producción. Esto permite recuperar.
5. **Foreign keys:** declaradas con `@ForeignKey` en la entidad hija, con `onDelete = ForeignKey.CASCADE` cuando tiene sentido.
6. **Índices:** toda columna usada en `WHERE`, `ORDER BY` o `JOIN ON` debe tener índice. Nombres: `idx_<tabla>_<columna>`.
7. **Campos derivados denormalizados solo si la query los necesita.** Preferir JOINs.
8. **Datos clínicos sensibles:** considerar campo `cifrado: Boolean` y usar SQLCipher si la app lo decide más adelante. Por ahora, documentar con un comentario.

### Convención de DAO

```kotlin
@Dao
interface EjercicioDao {
    @Query("SELECT * FROM ejercicios WHERE activo = 1 ORDER BY orden ASC")
    fun observeActivos(): Flow<List<EjercicioEntity>>

    @Query("SELECT * FROM ejercicios WHERE slug = :slug LIMIT 1")
    suspend fun findBySlug(slug: String): EjercicioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: EjercicioEntity): Long

    @Query("UPDATE ejercicios SET activo = 0, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long = System.currentTimeMillis())
}
```

### Reglas de DAO

1. **Lecturas reactivas:** `fun` que devuelve `Flow<T>` o `Flow<List<T>>`.
2. **Lecturas one-shot:** `suspend fun` que devuelve `T?` o `List<T>`.
3. **Escrituras:** siempre `suspend fun`.
4. **One-to-many:** usa `@Transaction` en un método que orquesta las queries.
5. **Paginación:** si la tabla puede crecer mucho (>1000 rows), usa `PagingSource` con `@Query`.
6. **Conflict strategy:** `REPLACE` para upserts, `IGNORE` para inserts idempotentes.
7. **Nombres de queries claros:** `observeActivos()`, `findBySlug()`, `countByTipo()`. No `getData()` ni `query1()`.

### Type Converters

```kotlin
class Converters {
    @TypeConverter
    fun fromTipoEjercicio(value: TipoEjercicio): String = value.name

    @TypeConverter
    fun toTipoEjercicio(value: String): TipoEjimiento = TipoEjercicio.valueOf(value)

    @TypeConverter
    fun fromList(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toList(value: String): List<String> = Json.decodeFromString(value)
}
```

- Converters registrados en `@TypeConverters(Converters::class)` en la clase `AppDatabase`.
- JSON para listas y objetos complejos. No conviertas manualmente con `joinToString`.

## Patrones avanzados

### Full-text search (FTS) opcional

Si una entidad tiene búsqueda por texto (ejercicios, guías, lecciones), crea una tabla FTS4/FT5 mirror sincronizada con triggers. No usar `LIKE %x%` en producción.

### Migration test

```kotlin
@Test
fun migration_from_3_to_4_preserves_ejercicios() {
    val db = RoomDatabaseTestHelper.create<MyDb>("migration-test.db")
    db.close()
    
    // Aplicar migración manualmente
    val migrated = helper.runMigrationsAndValidate("test.db", 4, true, MIGRATION_3_4)
    
    // Verificar datos
}
```

### Auditoría

Cada cambio de schema documenta:
- Versión anterior y nueva.
- Tablas/columnas nuevas/modificadas/eliminadas.
- Si la migración es destructiva o no.
- Justificación si `fallbackToDestructiveMigration`.

## Anti-patrones prohibidos

1. `@Entity` sin `@PrimaryKey`.
2. `Flow<List<X>>` con `ORDER BY` sin índice.
3. `suspend fun` que devuelve `Unit` (debería devolver el ID insertado o affected rows).
4. `Entity` con campos `Mutable*` (siempre inmutables).
5. Queries con `SELECT *` (preferir columnas explícitas si la tabla tiene BLOBs).
6. `RawQuery` sin sanitización (riesgo de SQL injection, aunque Room lo mitigue en parte).
