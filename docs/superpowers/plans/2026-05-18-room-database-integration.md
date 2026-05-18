# Room Database Integration — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace all hardcoded mock data in Solvyx ViewModels with real Room persistence across 7 entities, 7 DAOs, 7 repositories, and 8+ ViewModel refactors.

**Architecture:** Extend the existing `com.solvyx.backend.*` package. `AppDatabase` bumps to version 2 with `fallbackToDestructiveMigration()` (pre-release). All repositories are `@Singleton` injected via Hilt; ViewModels never call Room directly.

**Tech Stack:** Room KTX 2.x, Hilt, Kotlin Coroutines/Flow, KSP (not KAPT), `java.time` (requires `@RequiresApi(O)` guard, minSdk=24)

---

## File Map

### New files — entities + converters
| File | Package | Purpose |
|------|---------|---------|
| `UserEntity.kt` | `backend.data.local.entity` | Single-row user profile |
| `ContactoSosEntity.kt` | `backend.data.local.entity` | Up to 3 SOS contacts |
| `ResultadoAssistEntity.kt` | `backend.data.local.entity` | ASSIST quiz results (replaces `ResultadoEntity`) |
| `BitacoraEntity.kt` | `backend.data.local.entity` | Daily mood/consumption entries |
| `PlanEntity.kt` | `backend.data.local.entity` | Single-row daily plan state |
| `LogroEntity.kt` | `backend.data.local.entity` | 5 streak achievements (seeded on DB create) |
| `SosEventEntity.kt` | `backend.data.local.entity` | SOS activations log |
| `Converters.kt` | `backend.data.local.entity` | `List<String>` ↔ `"|||"`-delimited String |

### New files — DAOs
| File | Package |
|------|---------|
| `UserDao.kt` | `backend.data.local.dao` |
| `ContactoSosDao.kt` | `backend.data.local.dao` |
| `ResultadoAssistDao.kt` | `backend.data.local.dao` |
| `BitacoraDao.kt` | `backend.data.local.dao` |
| `PlanDao.kt` | `backend.data.local.dao` |
| `LogroDao.kt` | `backend.data.local.dao` |
| `SosEventDao.kt` | `backend.data.local.dao` |

### New files — repositories
| File | Package |
|------|---------|
| `UserRepository.kt` | `backend.repository` |
| `ContactoSosRepository.kt` | `backend.repository` |
| `AssistRepository.kt` | `backend.repository` |
| `BitacoraRepository.kt` | `backend.repository` |
| `PlanRepository.kt` | `backend.repository` |
| `AvancesRepository.kt` | `backend.repository` |
| `SosRepository.kt` | `backend.repository` |

### Modified files
| File | Change |
|------|--------|
| `AppDatabase.kt` | v2, all 7 entities, TypeConverters, seeding Callback |
| `AppModule.kt` | All DAOs + repositories wired |
| `DiagnosticoRepository.kt` | Remove `evaluarYGuardar` / `obtenerHistorial`; add `evaluar()` |
| `RegisterViewModel.kt` | Inject UserRepository |
| `RedApoyoViewModel.kt` | Inject ContactoSosRepository; drop `ContactoSOS` data class |
| `RedApoyoScreen.kt` | `ContactoSOS` → `ContactoSosEntity` in ContactCard |
| `DiagnosticoViewModel.kt` | Inject AssistRepository; route saves through it |
| `RegistroViewModel.kt` | Inject BitacoraRepository; real save + real fechas |
| `HistorialBitacoraScreen.kt` | Accept RegistroViewModel; replace RegistroMock with BitacoraEntity |
| `RegistroEmocionalScreen.kt` | Pass viewModel to HistorialBitacoraScreen; collect fechasConRegistro |
| `PlanViewModel.kt` | Inject PlanRepository |
| `AvancesViewModel.kt` | Inject AvancesRepository; drop all hardcoded arrays |
| `MisAvancesScreen.kt` | `viewModel.logros` → `viewModel.uiLogros`, collect StateFlows |
| `SosViewModel.kt` | Inject SosRepository; load phones from DB; log SOS events |
| `SosOverlayScreen.kt` | Drop `telefonos` parameter; call `viewModel.startCountdown()` |
| `PerfilViewModel.kt` | Inject UserRepository, AssistRepository, BitacoraRepository, ContactoSosRepository |

### Deleted files
- `DatabaseProvider.kt`
- `ResultadoEntity.kt` (replaced by `ResultadoAssistEntity.kt`)
- `ResultadoDao.kt` (replaced by `ResultadoAssistDao.kt`)

---

## Task 1 — Entities + TypeConverters

**Files:**
- Create: `app/src/main/java/com/solvyx/backend/data/local/entity/Converters.kt`
- Create: `app/src/main/java/com/solvyx/backend/data/local/entity/UserEntity.kt`
- Create: `app/src/main/java/com/solvyx/backend/data/local/entity/ContactoSosEntity.kt`
- Create: `app/src/main/java/com/solvyx/backend/data/local/entity/ResultadoAssistEntity.kt`
- Create: `app/src/main/java/com/solvyx/backend/data/local/entity/BitacoraEntity.kt`
- Create: `app/src/main/java/com/solvyx/backend/data/local/entity/PlanEntity.kt`
- Create: `app/src/main/java/com/solvyx/backend/data/local/entity/LogroEntity.kt`
- Create: `app/src/main/java/com/solvyx/backend/data/local/entity/SosEventEntity.kt`

- [ ] **Step 1: Create Converters.kt**

```kotlin
package com.solvyx.backend.data.local.entity

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromList(list: List<String>): String = list.joinToString("|||")

    @TypeConverter
    fun toList(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split("|||")
}
```

- [ ] **Step 2: Create UserEntity.kt**

```kotlin
package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val apodo: String = "",
    val email: String = "",
    val fechaRegistro: Long = System.currentTimeMillis(),
    val fechaNacimiento: String = "",
    val sustanciasJson: String = ""   // "|||"-delimited list of substance IDs
)
```

- [ ] **Step 3: Create ContactoSosEntity.kt**

```kotlin
package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contactos_sos")
data class ContactoSosEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String = "",
    val telefono: String = "",
    val orden: Int = 0
)
```

- [ ] **Step 4: Create ResultadoAssistEntity.kt**

```kotlin
package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resultados_assist")
data class ResultadoAssistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sustanciaId: String,
    val puntaje: Int,
    val nivel: String,
    val recomendacion: String,
    val fecha: Long = System.currentTimeMillis()
)
```

- [ ] **Step 5: Create BitacoraEntity.kt**

```kotlin
package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bitacora")
data class BitacoraEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: Long,
    val estadoAnimo: String,
    val consumio: Boolean,
    val sustancia: String? = null,
    val nota: String? = null
)
```

- [ ] **Step 6: Create PlanEntity.kt**

```kotlin
package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan")
data class PlanEntity(
    @PrimaryKey val id: Int = 1,
    val metaIndex: Int = 0,
    val metaLogradaHoy: Boolean = false,
    val fecha: Long = System.currentTimeMillis()
)
```

- [ ] **Step 7: Create LogroEntity.kt**

```kotlin
package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logros")
data class LogroEntity(
    @PrimaryKey val id: String,   // "racha_3", "racha_7", "racha_10", "racha_15", "racha_30"
    val unlocked: Boolean = false,
    val fechaUnlock: Long? = null
)
```

- [ ] **Step 8: Create SosEventEntity.kt**

```kotlin
package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sos_events")
data class SosEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: Long = System.currentTimeMillis(),
    val telefonosEnviados: String = ""   // "|||"-delimited phone list
)
```

- [ ] **Step 9: Compile check**

```bash
cd /Users/bumh3r/Documents/GitHub/solvyx-app
./gradlew :app:compileDebugKotlin 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 10: Commit**

```bash
git add app/src/main/java/com/solvyx/backend/data/local/entity/
git commit -m "feat(db): add 7 Room entities and TypeConverters"
```

---

## Task 2 — DAOs

**Files:**
- Create: `app/src/main/java/com/solvyx/backend/data/local/dao/UserDao.kt`
- Create: `app/src/main/java/com/solvyx/backend/data/local/dao/ContactoSosDao.kt`
- Create: `app/src/main/java/com/solvyx/backend/data/local/dao/ResultadoAssistDao.kt`
- Create: `app/src/main/java/com/solvyx/backend/data/local/dao/BitacoraDao.kt`
- Create: `app/src/main/java/com/solvyx/backend/data/local/dao/PlanDao.kt`
- Create: `app/src/main/java/com/solvyx/backend/data/local/dao/LogroDao.kt`
- Create: `app/src/main/java/com/solvyx/backend/data/local/dao/SosEventDao.kt`

- [ ] **Step 1: Create UserDao.kt**

```kotlin
package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = 1")
    fun observar(): Flow<UserEntity?>
}
```

- [ ] **Step 2: Create ContactoSosDao.kt**

```kotlin
package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.ContactoSosEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoSosDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(contactos: List<ContactoSosEntity>)

    @Query("DELETE FROM contactos_sos")
    suspend fun deleteAll()

    @Query("SELECT * FROM contactos_sos ORDER BY orden ASC")
    fun observar(): Flow<List<ContactoSosEntity>>
}
```

- [ ] **Step 3: Create ResultadoAssistDao.kt**

```kotlin
package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.ResultadoAssistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResultadoAssistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(resultado: ResultadoAssistEntity)

    @Query("SELECT * FROM resultados_assist ORDER BY fecha DESC")
    fun observar(): Flow<List<ResultadoAssistEntity>>
}
```

- [ ] **Step 4: Create BitacoraDao.kt**

```kotlin
package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.BitacoraEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BitacoraDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(entry: BitacoraEntity)

    @Query("SELECT * FROM bitacora ORDER BY fecha DESC")
    fun observar(): Flow<List<BitacoraEntity>>

    @Query("SELECT fecha FROM bitacora")
    fun observarFechas(): Flow<List<Long>>
}
```

- [ ] **Step 5: Create PlanDao.kt**

```kotlin
package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solvyx.backend.data.local.entity.PlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(plan: PlanEntity)

    @Query("SELECT * FROM plan WHERE id = 1")
    fun observar(): Flow<PlanEntity?>
}
```

- [ ] **Step 6: Create LogroDao.kt**

```kotlin
package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.solvyx.backend.data.local.entity.LogroEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogroDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodos(logros: List<LogroEntity>)

    @Update
    suspend fun actualizar(logro: LogroEntity)

    @Query("SELECT * FROM logros ORDER BY id ASC")
    fun observar(): Flow<List<LogroEntity>>
}
```

- [ ] **Step 7: Create SosEventDao.kt**

```kotlin
package com.solvyx.backend.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.solvyx.backend.data.local.entity.SosEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SosEventDao {
    @Insert
    suspend fun insertar(event: SosEventEntity)

    @Query("SELECT * FROM sos_events ORDER BY fecha DESC")
    fun observar(): Flow<List<SosEventEntity>>
}
```

- [ ] **Step 8: Compile check**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL (DAOs compile but are not yet referenced by AppDatabase)

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/solvyx/backend/data/local/dao/
git commit -m "feat(db): add 7 Room DAOs"
```

---

## Task 3 — AppDatabase v2 + delete obsolete files

**Files:**
- Modify: `app/src/main/java/com/solvyx/backend/data/local/database/AppDatabase.kt`
- Delete: `app/src/main/java/com/solvyx/backend/data/local/database/DatabaseProvider.kt`
- Delete: `app/src/main/java/com/solvyx/backend/data/local/entity/ResultadoEntity.kt`
- Delete: `app/src/main/java/com/solvyx/backend/data/local/dao/ResultadoDao.kt`

- [ ] **Step 1: Replace AppDatabase.kt**

Full file content:

```kotlin
package com.solvyx.backend.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.solvyx.backend.data.local.dao.*
import com.solvyx.backend.data.local.entity.*

@TypeConverters(Converters::class)
@Database(
    entities = [
        UserEntity::class,
        ContactoSosEntity::class,
        ResultadoAssistEntity::class,
        BitacoraEntity::class,
        PlanEntity::class,
        LogroEntity::class,
        SosEventEntity::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contactoSosDao(): ContactoSosDao
    abstract fun resultadoAssistDao(): ResultadoAssistDao
    abstract fun bitacoraDao(): BitacoraDao
    abstract fun planDao(): PlanDao
    abstract fun logroDao(): LogroDao
    abstract fun sosEventDao(): SosEventDao

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
```

- [ ] **Step 2: Delete DatabaseProvider.kt**

```bash
rm app/src/main/java/com/solvyx/backend/data/local/database/DatabaseProvider.kt
```

- [ ] **Step 3: Delete ResultadoEntity.kt**

```bash
rm app/src/main/java/com/solvyx/backend/data/local/entity/ResultadoEntity.kt
```

- [ ] **Step 4: Delete ResultadoDao.kt**

```bash
rm app/src/main/java/com/solvyx/backend/data/local/dao/ResultadoDao.kt
```

- [ ] **Step 5: Check for remaining references to deleted types**

```bash
grep -rn "ResultadoDao\|ResultadoEntity\|DatabaseProvider" \
  app/src/main/java/com/solvyx/ --include="*.kt"
```

Any remaining references need manual removal before step 6. Likely found in: `AppModule.kt` and `DiagnosticoRepository.kt`.

- [ ] **Step 6: Compile check (will fail until AppModule and DiagnosticoRepository are updated in the next task)**

This step intentionally skipped — compile check happens after Task 4 + Task 5.

- [ ] **Step 7: Commit**

```bash
git add -u
git add app/src/main/java/com/solvyx/backend/data/local/database/AppDatabase.kt
git commit -m "feat(db): expand AppDatabase to v2 with 7 entities, remove legacy files"
```

---

## Task 4 — AppModule expansion

**Files:**
- Modify: `app/src/main/java/com/solvyx/di/AppModule.kt`

- [ ] **Step 1: Replace AppModule.kt with full version**

```kotlin
package com.solvyx.di

import android.content.Context
import androidx.room.Room
import com.solvyx.backend.data.local.dao.*
import com.solvyx.backend.data.local.database.AppDatabase
import com.solvyx.backend.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "solvyx_database")
            .fallbackToDestructiveMigration()
            .addCallback(AppDatabase.SEED_CALLBACK)
            .build()

    // ── DAOs ──────────────────────────────────────────────────────────────────
    @Provides @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides @Singleton
    fun provideContactoSosDao(db: AppDatabase): ContactoSosDao = db.contactoSosDao()

    @Provides @Singleton
    fun provideResultadoAssistDao(db: AppDatabase): ResultadoAssistDao = db.resultadoAssistDao()

    @Provides @Singleton
    fun provideBitacoraDao(db: AppDatabase): BitacoraDao = db.bitacoraDao()

    @Provides @Singleton
    fun providePlanDao(db: AppDatabase): PlanDao = db.planDao()

    @Provides @Singleton
    fun provideLogroDao(db: AppDatabase): LogroDao = db.logroDao()

    @Provides @Singleton
    fun provideSosEventDao(db: AppDatabase): SosEventDao = db.sosEventDao()
}
```

Note: Repositories use `@Inject constructor` and are annotated `@Singleton`, so they do NOT need `@Provides` entries — Hilt discovers them automatically.

- [ ] **Step 2: Compile check (will succeed once DiagnosticoRepository is fixed in Task 5)**

Proceed to Task 5, then run:

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -20
```

---

## Task 5 — Repositories + simplify DiagnosticoRepository

**Files:**
- Create: `app/src/main/java/com/solvyx/backend/repository/UserRepository.kt`
- Create: `app/src/main/java/com/solvyx/backend/repository/ContactoSosRepository.kt`
- Create: `app/src/main/java/com/solvyx/backend/repository/AssistRepository.kt`
- Create: `app/src/main/java/com/solvyx/backend/repository/BitacoraRepository.kt`
- Create: `app/src/main/java/com/solvyx/backend/repository/PlanRepository.kt`
- Create: `app/src/main/java/com/solvyx/backend/repository/AvancesRepository.kt`
- Create: `app/src/main/java/com/solvyx/backend/repository/SosRepository.kt`
- Modify: `app/src/main/java/com/solvyx/backend/repository/DiagnosticoRepository.kt`

- [ ] **Step 1: Create UserRepository.kt**

```kotlin
package com.solvyx.backend.repository

import com.solvyx.backend.data.local.dao.UserDao
import com.solvyx.backend.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val dao: UserDao) {
    fun observar(): Flow<UserEntity?> = dao.observar()
    suspend fun guardar(user: UserEntity) = dao.upsert(user)
}
```

- [ ] **Step 2: Create ContactoSosRepository.kt**

```kotlin
package com.solvyx.backend.repository

import com.solvyx.backend.data.local.dao.ContactoSosDao
import com.solvyx.backend.data.local.entity.ContactoSosEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactoSosRepository @Inject constructor(private val dao: ContactoSosDao) {
    fun observar(): Flow<List<ContactoSosEntity>> = dao.observar()

    suspend fun guardarTodos(contactos: List<ContactoSosEntity>) {
        dao.deleteAll()
        dao.upsertAll(contactos.mapIndexed { i, c -> c.copy(orden = i) })
    }
}
```

- [ ] **Step 3: Create AssistRepository.kt**

```kotlin
package com.solvyx.backend.repository

import com.solvyx.backend.data.local.dao.ResultadoAssistDao
import com.solvyx.backend.data.local.entity.ResultadoAssistEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistRepository @Inject constructor(private val dao: ResultadoAssistDao) {
    fun observar(): Flow<List<ResultadoAssistEntity>> = dao.observar()
    suspend fun guardar(resultado: ResultadoAssistEntity) = dao.insertar(resultado)
}
```

- [ ] **Step 4: Create BitacoraRepository.kt**

```kotlin
package com.solvyx.backend.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.solvyx.backend.data.local.dao.BitacoraDao
import com.solvyx.backend.data.local.entity.BitacoraEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BitacoraRepository @Inject constructor(private val dao: BitacoraDao) {
    fun observar(): Flow<List<BitacoraEntity>> = dao.observar()
    suspend fun guardar(entry: BitacoraEntity) = dao.insertar(entry)

    @RequiresApi(Build.VERSION_CODES.O)
    fun observarFechas(): Flow<Set<LocalDate>> =
        dao.observarFechas().map { millis ->
            millis.map {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            }.toSet()
        }
}
```

- [ ] **Step 5: Create PlanRepository.kt**

```kotlin
package com.solvyx.backend.repository

import com.solvyx.backend.data.local.dao.PlanDao
import com.solvyx.backend.data.local.entity.PlanEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlanRepository @Inject constructor(private val dao: PlanDao) {
    fun observar(): Flow<PlanEntity?> = dao.observar()
    suspend fun guardar(plan: PlanEntity) = dao.upsert(plan)
}
```

- [ ] **Step 6: Create AvancesRepository.kt**

```kotlin
package com.solvyx.backend.repository

import com.solvyx.backend.data.local.dao.BitacoraDao
import com.solvyx.backend.data.local.dao.LogroDao
import com.solvyx.backend.data.local.entity.BitacoraEntity
import com.solvyx.backend.data.local.entity.LogroEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvancesRepository @Inject constructor(
    private val bitacoraDao: BitacoraDao,
    private val logroDao: LogroDao
) {
    fun observarBitacora(): Flow<List<BitacoraEntity>> = bitacoraDao.observar()
    fun observarLogros(): Flow<List<LogroEntity>> = logroDao.observar()
    suspend fun desbloquearLogro(id: String) =
        logroDao.actualizar(
            LogroEntity(
                id = id,
                unlocked = true,
                fechaUnlock = System.currentTimeMillis()
            )
        )
}
```

- [ ] **Step 7: Create SosRepository.kt**

```kotlin
package com.solvyx.backend.repository

import com.solvyx.backend.data.local.dao.ContactoSosDao
import com.solvyx.backend.data.local.dao.SosEventDao
import com.solvyx.backend.data.local.entity.ContactoSosEntity
import com.solvyx.backend.data.local.entity.SosEventEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SosRepository @Inject constructor(
    private val contactoSosDao: ContactoSosDao,
    private val sosEventDao: SosEventDao
) {
    fun observarContactos(): Flow<List<ContactoSosEntity>> = contactoSosDao.observar()
    suspend fun registrarEvento(telefonos: List<String>) =
        sosEventDao.insertar(SosEventEntity(telefonosEnviados = telefonos.joinToString("|||")))
}
```

- [ ] **Step 8: Simplify DiagnosticoRepository.kt**

Replace the entire file. Remove `ResultadoDao` injection, remove `evaluarYGuardar`, remove `obtenerHistorial`. Add pure `evaluar()` function:

```kotlin
package com.solvyx.backend.repository

import com.solvyx.backend.models.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiagnosticoRepository @Inject constructor() {

    fun obtenerPreguntas(sustancia: String): List<Pregunta> {
        val preguntas = mutableListOf(
            Pregunta(id = 1, texto = "En los últimos 3 meses, ¿con qué frecuencia consumiste $sustancia?", opciones = FRECUENCIA_P2),
            Pregunta(id = 2, texto = "¿Con qué frecuencia sentiste deseo de consumir $sustancia?", opciones = FRECUENCIA_P3_P7),
            Pregunta(id = 3, texto = "¿El consumo de $sustancia causó problemas?", opciones = FRECUENCIA_P4),
            Pregunta(id = 4, texto = "¿Descuidaste responsabilidades por consumir $sustancia?", opciones = OPCIONES_P5_P6),
            Pregunta(id = 5, texto = "¿Alguien mostró preocupación por tu consumo?", opciones = OPCIONES_P5_P6),
            Pregunta(id = 6, texto = "¿Intentaste dejarlo y no pudiste?", opciones = FRECUENCIA_P3_P7)
        )
        if (sustancia.lowercase() == "cristal") {
            preguntas.add(Pregunta(id = 7, texto = "¿Has consumido cristal por vía inyectada?", opciones = OPCIONES_P8))
        }
        return preguntas
    }

    fun evaluar(sustanciaId: String, respuestas: List<Int>): ResultadoDiagnostico {
        val puntaje = respuestas.sum()
        val nivel = determinarNivel(puntaje)
        return ResultadoDiagnostico(
            sustanciaId = sustanciaId,
            puntaje = puntaje,
            nivel = nivel,
            recomendacion = generarRecomendacion(nivel)
        )
    }

    private fun determinarNivel(puntaje: Int): NivelRiesgo = when {
        puntaje <= 10 -> NivelRiesgo.BAJO
        puntaje <= 26 -> NivelRiesgo.MODERADO
        else -> NivelRiesgo.ALTO
    }

    private fun generarRecomendacion(nivel: NivelRiesgo): String = when (nivel) {
        NivelRiesgo.BAJO -> "Riesgo bajo."
        NivelRiesgo.MODERADO -> "Se recomienda seguimiento."
        NivelRiesgo.ALTO -> "Se recomienda atención profesional."
    }
}
```

- [ ] **Step 9: Compile check**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -30
```

Expected: BUILD SUCCESSFUL. If `DiagnosticoViewModel` errors (it still calls `repository.evaluarYGuardar`), fix it in Task 8 — for now just ensure entity/DAO/repository files compile.

- [ ] **Step 10: Commit**

```bash
git add app/src/main/java/com/solvyx/backend/repository/
git add app/src/main/java/com/solvyx/di/AppModule.kt
git commit -m "feat(db): add 7 repositories, simplify DiagnosticoRepository, expand AppModule"
```

---

## Task 6 — RegisterViewModel

**Files:**
- Modify: `app/src/main/java/com/solvyx/ui/screens/auth/register/RegisterViewModel.kt`

- [ ] **Step 1: Inject UserRepository and save user on register**

```kotlin
package com.solvyx.ui.screens.auth.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.entity.UserEntity
import com.solvyx.backend.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var nickname by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var birthdate by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set
    var acceptedTerms by mutableStateOf(false)
        private set

    fun onNicknameChange(value: String) { nickname = value }
    fun onEmailChange(value: String) { email = value }
    fun onBirthdateChange(value: String) { birthdate = value }
    fun onPasswordChange(value: String) { password = value }
    fun onConfirmPasswordChange(value: String) { confirmPassword = value }
    fun onTermsChange(value: Boolean) { acceptedTerms = value }

    fun register(onSuccess: () -> Unit) {
        viewModelScope.launch {
            userRepository.guardar(
                UserEntity(
                    apodo = nickname.trim(),
                    email = email.trim(),
                    fechaRegistro = System.currentTimeMillis(),
                    fechaNacimiento = birthdate.trim()
                )
            )
            onSuccess()
        }
    }
}
```

- [ ] **Step 2: Compile check + commit**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -10
git add app/src/main/java/com/solvyx/ui/screens/auth/register/RegisterViewModel.kt
git commit -m "feat(register): persist user to Room on registration"
```

---

## Task 7 — RedApoyoViewModel + RedApoyoScreen

**Files:**
- Modify: `app/src/main/java/com/solvyx/ui/screens/red/RedApoyoViewModel.kt`
- Modify: `app/src/main/java/com/solvyx/ui/screens/red/RedApoyoScreen.kt`

- [ ] **Step 1: Replace RedApoyoViewModel.kt**

Drop the `ContactoSOS` data class. Expose `ContactoSosEntity` directly.

```kotlin
package com.solvyx.ui.screens.red

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.entity.ContactoSosEntity
import com.solvyx.backend.repository.ContactoSosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RedApoyoViewModel @Inject constructor(
    private val repository: ContactoSosRepository
) : ViewModel() {

    var contactos by mutableStateOf(listOf(ContactoSosEntity()))
        private set

    var isSaving by mutableStateOf(false)
        private set

    var savedSuccessfully by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            repository.observar().collect { stored ->
                if (stored.isNotEmpty()) contactos = stored
            }
        }
    }

    fun phoneValido(telefono: String): Boolean =
        telefono.filter { it.isDigit() }.length >= 7

    fun canSave(): Boolean {
        val c0 = contactos.firstOrNull() ?: return false
        return c0.nombre.trim().length >= 2 && phoneValido(c0.telefono)
    }

    fun setContacto(index: Int, contacto: ContactoSosEntity) {
        contactos = contactos.toMutableList().also { it[index] = contacto }
    }

    fun addContacto() {
        if (contactos.size >= 3) return
        contactos = contactos + ContactoSosEntity()
    }

    fun removeContacto(index: Int) {
        if (index == 0) return
        contactos = contactos.filterIndexed { i, _ -> i != index }
    }

    fun guardar() {
        if (!canSave()) return
        viewModelScope.launch {
            isSaving = true
            repository.guardarTodos(contactos)
            isSaving = false
            savedSuccessfully = true
        }
    }

    fun resetSaved() {
        savedSuccessfully = false
    }
}
```

- [ ] **Step 2: Update RedApoyoScreen.kt — ContactCard signature**

Find in `RedApoyoScreen.kt`:

```kotlin
@Composable
private fun ContactCard(
    index: Int,
    contacto: ContactoSOS,
    isRequired: Boolean,
    onContactoChange: (ContactoSOS) -> Unit,
    onRemove: () -> Unit
)
```

Replace with:

```kotlin
@Composable
private fun ContactCard(
    index: Int,
    contacto: ContactoSosEntity,
    isRequired: Boolean,
    onContactoChange: (ContactoSosEntity) -> Unit,
    onRemove: () -> Unit
)
```

- [ ] **Step 3: Add import for ContactoSosEntity in RedApoyoScreen.kt**

Add at the top of the file (after the existing imports):

```kotlin
import com.solvyx.backend.data.local.entity.ContactoSosEntity
```

Remove the old import if present:
```kotlin
// remove: import com.solvyx.ui.screens.red.ContactoSOS  (if explicit)
```

- [ ] **Step 4: Compile check + commit**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -10
git add app/src/main/java/com/solvyx/ui/screens/red/
git commit -m "feat(red-apoyo): persist contacts to Room, drop ContactoSOS data class"
```

---

## Task 8 — DiagnosticoViewModel

**Files:**
- Modify: `app/src/main/java/com/solvyx/backend/presentation/viewmodel/DiagnosticoViewModel.kt`

- [ ] **Step 1: Replace DiagnosticoViewModel.kt**

Inject `AssistRepository` alongside `DiagnosticoRepository`. Route all saves through `AssistRepository`. Expose historial as `StateFlow` from `AssistRepository`.

```kotlin
package com.solvyx.backend.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.entity.ResultadoAssistEntity
import com.solvyx.backend.models.Pregunta
import com.solvyx.backend.models.ResultadoDiagnostico
import com.solvyx.backend.repository.AssistRepository
import com.solvyx.backend.repository.DiagnosticoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiagnosticoViewModel @Inject constructor(
    private val repository: DiagnosticoRepository,
    private val assistRepository: AssistRepository
) : ViewModel() {

    private val _preguntas = MutableStateFlow<List<Pregunta>>(emptyList())
    val preguntas: StateFlow<List<Pregunta>> = _preguntas.asStateFlow()

    private val _resultado = MutableStateFlow<ResultadoDiagnostico?>(null)
    val resultado: StateFlow<ResultadoDiagnostico?> = _resultado.asStateFlow()

    val historial: StateFlow<List<ResultadoAssistEntity>> =
        assistRepository.observar()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var sustanciasSeleccionadas by mutableStateOf<List<String>>(emptyList())
        private set
    var sustanciaActualIndex by mutableStateOf(0)
        private set
    var answersMap by mutableStateOf<Map<String, List<Int>>>(emptyMap())
        private set

    private val _resultados = MutableStateFlow<List<ResultadoDiagnostico>>(emptyList())
    val resultados: StateFlow<List<ResultadoDiagnostico>> = _resultados.asStateFlow()

    val sustanciaActual: String get() = sustanciasSeleccionadas.getOrElse(sustanciaActualIndex) { "" }
    val totalSustancias: Int get() = sustanciasSeleccionadas.size
    val esUltimaSustancia: Boolean get() = sustanciaActualIndex >= sustanciasSeleccionadas.lastIndex
    fun canContinue(): Boolean = sustanciasSeleccionadas.isNotEmpty()

    fun toggleSustancia(id: String) {
        sustanciasSeleccionadas = if (sustanciasSeleccionadas.contains(id))
            sustanciasSeleccionadas - id
        else
            sustanciasSeleccionadas + id
    }

    fun iniciarCuestionario() {
        sustanciaActualIndex = 0
        answersMap = emptyMap()
        _resultados.value = emptyList()
        cargarPreguntas(sustanciaActual)
    }

    fun guardarYAvanzar(answers: List<Int>): Boolean {
        val sustanciaGuardada = sustanciaActual
        answersMap = answersMap + (sustanciaGuardada to answers)
        viewModelScope.launch {
            val resultado = repository.evaluar(sustanciaGuardada, answers)
            _resultados.value = _resultados.value + resultado
            assistRepository.guardar(
                ResultadoAssistEntity(
                    sustanciaId = resultado.sustanciaId,
                    puntaje = resultado.puntaje,
                    nivel = resultado.nivel.name,
                    recomendacion = resultado.recomendacion
                )
            )
        }
        return if (esUltimaSustancia) false
        else { sustanciaActualIndex++; cargarPreguntas(sustanciaActual); true }
    }

    fun cargarPreguntas(sustancia: String) {
        _preguntas.value = repository.obtenerPreguntas(sustancia)
    }

    fun evaluarRespuestas(respuestas: List<Int>) {
        viewModelScope.launch {
            val resultado = repository.evaluar(sustanciaActual, respuestas)
            _resultado.value = resultado
            assistRepository.guardar(
                ResultadoAssistEntity(
                    sustanciaId = resultado.sustanciaId,
                    puntaje = resultado.puntaje,
                    nivel = resultado.nivel.name,
                    recomendacion = resultado.recomendacion
                )
            )
        }
    }
}
```

- [ ] **Step 2: Update HistoryScreen.kt if it references ResultadoEntity**

```bash
grep -n "ResultadoEntity\|obtenerHistorial\|cargarHistorial" \
  app/src/main/java/com/solvyx/ui/diagnostico/HistoryScreen.kt
```

If `ResultadoEntity` is referenced, replace with `ResultadoAssistEntity` and update the import:
```kotlin
// old:
import com.solvyx.backend.data.local.entity.ResultadoEntity
// new:
import com.solvyx.backend.data.local.entity.ResultadoAssistEntity
```

- [ ] **Step 3: Compile check + commit**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -10
git add app/src/main/java/com/solvyx/backend/presentation/viewmodel/DiagnosticoViewModel.kt
git add app/src/main/java/com/solvyx/ui/diagnostico/
git commit -m "feat(diagnostico): route ASSIST results through AssistRepository"
```

---

## Task 9 — RegistroViewModel + HistorialBitacoraScreen

**Files:**
- Modify: `app/src/main/java/com/solvyx/ui/screens/bitacora/RegistroViewModel.kt`
- Modify: `app/src/main/java/com/solvyx/ui/screens/bitacora/HistorialBitacoraScreen.kt`
- Modify: `app/src/main/java/com/solvyx/ui/screens/bitacora/RegistroEmocionalScreen.kt`

- [ ] **Step 1: Replace RegistroViewModel.kt**

```kotlin
package com.solvyx.ui.screens.bitacora

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.entity.BitacoraEntity
import com.solvyx.backend.repository.BitacoraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val repository: BitacoraRepository
) : ViewModel() {

    var fechaSeleccionada by mutableStateOf(LocalDate.now())
        private set
    var estadoAnimo by mutableStateOf<String?>(null)
        private set
    var notaAnimo by mutableStateOf("")
        private set
    var consumo by mutableStateOf<Boolean?>(null)
        private set
    var sustanciaSeleccionada by mutableStateOf<String?>(null)
        private set
    var showCalendar by mutableStateOf(false)
        private set
    var showSustanciaSheet by mutableStateOf(false)
        private set
    var isSaved by mutableStateOf(false)
        private set

    val historial: StateFlow<List<BitacoraEntity>> =
        repository.observar()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val fechasConRegistro: StateFlow<Set<LocalDate>> =
        repository.observarFechas()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    fun canSave(): Boolean =
        estadoAnimo != null && consumo != null &&
        (consumo == false || sustanciaSeleccionada != null)

    fun setFecha(fecha: LocalDate) { fechaSeleccionada = fecha }
    fun updateEstadoAnimo(estado: String) { estadoAnimo = estado }
    fun updateNotaAnimo(nota: String) { if (nota.length <= 100) notaAnimo = nota }
    fun updateConsumo(value: Boolean) {
        consumo = value
        if (!value) sustanciaSeleccionada = null
        if (value) showSustanciaSheet = true
    }
    fun setSustancia(s: String) { sustanciaSeleccionada = s }
    fun toggleCalendar() { showCalendar = !showCalendar }
    fun toggleSustanciaSheet() { showSustanciaSheet = !showSustanciaSheet }

    fun guardarRegistro() {
        if (!canSave()) return
        viewModelScope.launch {
            repository.guardar(
                BitacoraEntity(
                    fecha = fechaSeleccionada
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli(),
                    estadoAnimo = estadoAnimo!!,
                    consumio = consumo!!,
                    sustancia = sustanciaSeleccionada,
                    nota = notaAnimo.ifBlank { null }
                )
            )
            isSaved = true
        }
    }

    fun resetForm() {
        estadoAnimo = null
        notaAnimo = ""
        consumo = null
        sustanciaSeleccionada = null
        isSaved = false
    }
}
```

- [ ] **Step 2: Update RegistroEmocionalScreen.kt — collect fechasConRegistro and pass viewModel to historial**

Find the lines in `RegistroEmocionalScreen.kt` that:
1. Pass `viewModel.fechasConRegistro` to `CalendarBottomSheet`
2. Launch `HistorialBitacoraScreen(onBack = ...)`

Add `collectAsState` import at top:
```kotlin
import androidx.compose.runtime.collectAsState
```

Change how `fechasConRegistro` is consumed (find the `CalendarBottomSheet` call):
```kotlin
// old:
fechasConRegistro = viewModel.fechasConRegistro,

// new:
fechasConRegistro = viewModel.fechasConRegistro.collectAsState().value,
```

Change the `HistorialBitacoraScreen` call (inside `if (showHistorial)` block):
```kotlin
// old:
HistorialBitacoraScreen(onBack = { showHistorial = false })

// new:
HistorialBitacoraScreen(viewModel = viewModel, onBack = { showHistorial = false })
```

- [ ] **Step 3: Replace HistorialBitacoraScreen.kt**

Remove `RegistroMock` data class. Accept `RegistroViewModel`. Map `BitacoraEntity` to display. Compute stats from real data.

```kotlin
package com.solvyx.ui.screens.bitacora

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.data.local.entity.BitacoraEntity
import com.solvyx.ui.components.common.SolvyxBackButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistorialBitacoraScreen(
    viewModel: RegistroViewModel,
    onBack: () -> Unit
) {
    val registros by viewModel.historial.collectAsState()
    val totalRegistros = registros.size
    val sinConsumo = registros.count { !it.consumio }
    val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "MX"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SolvyxBackButton(onClick = onBack)
            Text(
                "Historial de Registros",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.size(48.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ResumenStatItem(totalRegistros.toString(), "Registros")
            ResumenStatItem(sinConsumo.toString(), "Sin consumo")
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(registros) { registro ->
                HistorialRegistroCard(
                    registro = registro,
                    fechaLabel = dateFormat.format(Date(registro.fecha))
                        .replaceFirstChar { it.uppercase() }
                )
            }
        }
    }
}

@Composable
private fun ResumenStatItem(valor: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f), textAlign = TextAlign.Center)
    }
}

@Composable
private fun HistorialRegistroCard(registro: BitacoraEntity, fechaLabel: String) {
    val faceIcons = mapOf(
        "triste" to R.drawable.ic_face_sad, "ansioso" to R.drawable.ic_face_anxious,
        "neutral" to R.drawable.ic_face_neutral, "bien" to R.drawable.ic_face_happy,
        "euforico" to R.drawable.ic_face_euphoric
    )
    val faceLabels = mapOf(
        "triste" to "Triste", "ansioso" to "Ansioso",
        "neutral" to "Neutral", "bien" to "Bien", "euforico" to "Eufórico"
    )
    val sosRed = Color(0xFFE24B4A)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceDim),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    fechaLabel,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (registro.consumio) {
                    Box(Modifier.clip(RoundedCornerShape(50.dp)).background(sosRed.copy(alpha = 0.10f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("Consumo: ${registro.sustancia?.replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = sosRed)
                    }
                } else {
                    Box(Modifier.clip(RoundedCornerShape(50.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("Sin consumo", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), thickness = 0.5.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                    Icon(painter = painterResource(faceIcons[registro.estadoAnimo] ?: R.drawable.ic_face_neutral), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(faceLabels[registro.estadoAnimo] ?: "", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                    if (registro.nota != null) {
                        Text(registro.nota, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 4: Compile check + commit**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -10
git add app/src/main/java/com/solvyx/ui/screens/bitacora/
git commit -m "feat(bitacora): persist entries to Room, historial reads real DB data"
```

---

## Task 10 — PlanViewModel

**Files:**
- Modify: `app/src/main/java/com/solvyx/ui/screens/plan/PlanViewModel.kt`

- [ ] **Step 1: Replace PlanViewModel.kt**

```kotlin
package com.solvyx.ui.screens.plan

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.entity.PlanEntity
import com.solvyx.backend.repository.PlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val repository: PlanRepository
) : ViewModel() {

    var metaIndex by mutableStateOf(0)
        private set
    var metaLogradaHoy by mutableStateOf(false)
        private set
    var showSosDialog by mutableStateOf(false)
        private set

    val metasList = listOf(
        "Antes de consumir, toma agua y come algo primero.",
        "Si sientes ganas de consumir, espera 15 minutos antes de decidir.",
        "Habla con alguien de confianza antes de consumir.",
        "Reduce la dosis a la mitad respecto a la última vez."
    )

    val metaActual get() = metasList[metaIndex]

    init {
        viewModelScope.launch {
            repository.observar().collect { plan ->
                plan?.let {
                    metaIndex = it.metaIndex
                    metaLogradaHoy = it.metaLogradaHoy
                }
            }
        }
    }

    private fun persistir() {
        viewModelScope.launch {
            repository.guardar(PlanEntity(metaIndex = metaIndex, metaLogradaHoy = metaLogradaHoy))
        }
    }

    fun toggleMetaLograda() { metaLogradaHoy = !metaLogradaHoy; persistir() }
    fun siguienteMeta() { metaIndex = (metaIndex + 1) % metasList.size; metaLogradaHoy = false; persistir() }
    fun abrirSosDialog() { showSosDialog = true }
    fun cerrarSosDialog() { showSosDialog = false }
}
```

- [ ] **Step 2: Compile check + commit**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -10
git add app/src/main/java/com/solvyx/ui/screens/plan/PlanViewModel.kt
git commit -m "feat(plan): persist daily plan state to Room"
```

---

## Task 11 — AvancesViewModel + MisAvancesScreen

**Files:**
- Modify: `app/src/main/java/com/solvyx/ui/screens/avances/AvancesViewModel.kt`
- Modify: `app/src/main/java/com/solvyx/ui/screens/avances/MisAvancesScreen.kt`

- [ ] **Step 1: Replace AvancesViewModel.kt**

```kotlin
package com.solvyx.ui.screens.avances

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.R
import com.solvyx.backend.data.local.entity.BitacoraEntity
import com.solvyx.backend.data.local.entity.LogroEntity
import com.solvyx.backend.repository.AvancesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class AvancesViewModel @Inject constructor(
    private val repository: AvancesRepository
) : ViewModel() {

    var selectedTab by mutableStateOf(0)
        private set

    fun selectTab(index: Int) { selectedTab = index }

    val milestoneDays = listOf(7, 15, 30)

    val bitacora: StateFlow<List<BitacoraEntity>> =
        repository.observarBitacora()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val logros: StateFlow<List<LogroEntity>> =
        repository.observarLogros()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val racha: Int get() = calcularRacha(bitacora.value)
    val mejorRacha: Int get() = calcularMejorRacha(bitacora.value)
    val proximoLogro: Int get() = milestoneDays.firstOrNull { it > racha } ?: 30
    val milestoneProgress: Float get() = if (proximoLogro == 0) 1f else racha.toFloat() / proximoLogro

    private fun epochToLocalDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

    private fun emocionABienestar(estado: String): Float = when (estado) {
        "triste" -> 2f; "ansioso" -> 3f; "neutral" -> 5f; "bien" -> 7f; "euforico" -> 9f
        else -> 5f
    }

    private fun bitacoraForDay(entries: List<BitacoraEntity>, date: LocalDate): BitacoraEntity? =
        entries.firstOrNull { epochToLocalDate(it.fecha) == date }

    val feelingsDataSemana: List<Float> get() {
        val hoy = LocalDate.now()
        return (6 downTo 0).map { d ->
            bitacoraForDay(bitacora.value, hoy.minusDays(d.toLong()))
                ?.let { emocionABienestar(it.estadoAnimo) } ?: 5f
        }
    }

    val feelingsDataMes: List<Float> get() {
        val hoy = LocalDate.now()
        return (27 downTo 0).map { d ->
            bitacoraForDay(bitacora.value, hoy.minusDays(d.toLong()))
                ?.let { emocionABienestar(it.estadoAnimo) } ?: 5f
        }
    }

    val consumoSemana: List<Float> get() {
        val hoy = LocalDate.now()
        return (6 downTo 0).map { d ->
            val e = bitacoraForDay(bitacora.value, hoy.minusDays(d.toLong()))
            when { e == null || !e.consumio -> 0f; e.sustancia == "cristal" -> 2f; else -> 1f }
        }
    }

    val consumoMes: List<Float> get() {
        val hoy = LocalDate.now()
        return (27 downTo 0).map { d ->
            val e = bitacoraForDay(bitacora.value, hoy.minusDays(d.toLong()))
            when { e == null || !e.consumio -> 0f; e.sustancia == "cristal" -> 2f; else -> 1f }
        }
    }

    data class UiLogro(val icon: Int, val titulo: String, val descripcion: String, val unlocked: Boolean)

    val uiLogros: List<UiLogro> get() = logros.value.map { entity ->
        val days = entity.id.removePrefix("racha_").toIntOrNull() ?: 0
        UiLogro(
            icon = when (days) { 3 -> R.drawable.ic_flame; 7 -> R.drawable.ic_trophy; 10 -> R.drawable.ic_brain; 15 -> R.drawable.ic_flag; else -> R.drawable.ic_gem },
            titulo = when (days) { 3 -> "Primera racha"; 7 -> "Primera semana"; 10 -> "Mente clara"; 15 -> "2 semanas"; else -> "Un mes" },
            descripcion = "$days días consecutivos",
            unlocked = entity.unlocked
        )
    }

    val labelsSemana = listOf("L", "M", "X", "J", "V", "S", "D")
    val labelsMes = (1..28).map { it.toString() }

    init {
        viewModelScope.launch {
            combine(bitacora, logros) { bit, lgr ->
                val r = calcularRacha(bit)
                listOf(3, 7, 10, 15, 30).forEach { dias ->
                    if (r >= dias) {
                        val logro = lgr.find { it.id == "racha_$dias" }
                        if (logro != null && !logro.unlocked) repository.desbloquearLogro("racha_$dias")
                    }
                }
            }.collect()
        }
    }

    private fun calcularRacha(entries: List<BitacoraEntity>): Int {
        var racha = 0
        var fecha = LocalDate.now()
        while (true) {
            val e = entries.firstOrNull { epochToLocalDate(it.fecha) == fecha }
            if (e == null || e.consumio) break
            racha++
            fecha = fecha.minusDays(1)
        }
        return racha
    }

    private fun calcularMejorRacha(entries: List<BitacoraEntity>): Int {
        var mejor = 0
        var actual = 0
        entries.sortedBy { it.fecha }.forEach { e ->
            if (!e.consumio) { actual++; if (actual > mejor) mejor = actual }
            else actual = 0
        }
        return mejor
    }
}
```

- [ ] **Step 2: Update MisAvancesScreen.kt — replace `viewModel.logros` with `viewModel.uiLogros`**

Find in `MisAvancesScreen.kt`:

```kotlin
items(viewModel.logros) { logro ->
    LogroCard(logro = logro)
}
```

Replace with:

```kotlin
items(viewModel.uiLogros) { logro ->
    LogroCard(logro = logro)
}
```

Find the `LogroCard` signature:

```kotlin
private fun LogroCard(logro: AvancesViewModel.Logro)
```

Replace with:

```kotlin
private fun LogroCard(logro: AvancesViewModel.UiLogro)
```

- [ ] **Step 3: Compile check + commit**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -10
git add app/src/main/java/com/solvyx/ui/screens/avances/
git commit -m "feat(avances): derive charts and logros from real DB data"
```

---

## Task 12 — SosViewModel + SosOverlayScreen

**Files:**
- Modify: `app/src/main/java/com/solvyx/ui/screens/sos/SosViewModel.kt`
- Modify: `app/src/main/java/com/solvyx/ui/screens/sos/SosOverlayScreen.kt`

- [ ] **Step 1: Inject SosRepository in SosViewModel.kt**

Add `private val sosRepository: SosRepository` to the constructor and update `startCountdown`:

```kotlin
@HiltViewModel
class SosViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val sosRepository: SosRepository
) : ViewModel() {

    // (all existing state fields unchanged: sosState, countdown, countdownJob, tts, mainHandler)

    fun startCountdown() {
        countdownJob?.cancel()
        countdown = 3
        countdownJob = viewModelScope.launch {
            val contactos = kotlinx.coroutines.flow.first(sosRepository.observarContactos())
            val telefonos = contactos.map { it.telefono }
            sendSmsBackground(telefonos)
            if (telefonos.isNotEmpty()) sosRepository.registrarEvento(telefonos)
            repeat(3) { delay(1000L); countdown-- }
            sosState = SosState.SENT
            initTts()
        }
    }

    fun cancel() { countdownJob?.cancel(); tts?.stop() }

    // (sendSmsBackground, initTts, speakInitialGuide, speakPhase, speak, stopTts, onCleared — all unchanged)
}
```

Add the import at the top of the file:
```kotlin
import com.solvyx.backend.repository.SosRepository
import kotlinx.coroutines.flow.first
```

Note: `sendSmsBackground` signature changes from `fun sendSmsBackground(phones: List<String>)` — no change needed in body, just the caller.

- [ ] **Step 2: Update SosOverlayScreen.kt — remove telefonos param from startCountdown call**

Find in `SosOverlayScreen.kt`:

```kotlin
viewModel.startCountdown(telefonos)
```

Replace with:

```kotlin
viewModel.startCountdown()
```

Also check the composable signature that has `telefonos: List<String>` — if this was passed by the caller for display purposes only (contact names for the `contactos` list), keep that param; only the `telefonos` forward to ViewModel is removed.

- [ ] **Step 3: Compile check + commit**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -10
git add app/src/main/java/com/solvyx/ui/screens/sos/
git commit -m "feat(sos): load contact phones from DB, log SOS events to Room"
```

---

## Task 13 — PerfilViewModel

**Files:**
- Modify: `app/src/main/java/com/solvyx/ui/screens/perfil/PerfilViewModel.kt`

- [ ] **Step 1: Replace PerfilViewModel.kt**

```kotlin
package com.solvyx.ui.screens.perfil

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.data.local.entity.BitacoraEntity
import com.solvyx.backend.data.local.entity.UserEntity
import com.solvyx.backend.repository.AssistRepository
import com.solvyx.backend.repository.BitacoraRepository
import com.solvyx.backend.repository.ContactoSosRepository
import com.solvyx.backend.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val assistRepository: AssistRepository,
    private val bitacoraRepository: BitacoraRepository,
    private val contactoSosRepository: ContactoSosRepository
) : ViewModel() {

    val user: StateFlow<UserEntity?> =
        userRepository.observar().stateIn(viewModelScope, SharingStarted.Lazily, null)

    val rachaActual: StateFlow<Int> =
        bitacoraRepository.observar()
            .map { entries -> calcularRacha(entries) }
            .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val mejorRacha: StateFlow<Int> =
        bitacoraRepository.observar()
            .map { entries -> calcularMejorRacha(entries) }
            .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val diagnosticosCompletados: StateFlow<Int> =
        assistRepository.observar()
            .map { it.size }
            .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val ultimoAssist =
        assistRepository.observar()
            .map { it.firstOrNull() }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val cantidadContactos: StateFlow<Int> =
        contactoSosRepository.observar()
            .map { it.size }
            .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // ── Derived properties from StateFlows ────────────────────────────────────

    val apodo: String get() = user.value?.apodo?.ifBlank { "—" } ?: "—"

    val fechaRegistro: String get() {
        val ts = user.value?.fechaRegistro ?: return "—"
        return SimpleDateFormat("MMMM yyyy", Locale("es", "MX")).format(Date(ts))
            .replaceFirstChar { it.uppercase() }
    }

    val fechaNacimiento: String get() = user.value?.fechaNacimiento ?: ""

    val sustanciasSeleccionadas: Set<String> get() {
        val json = user.value?.sustanciasJson ?: return emptySet()
        return if (json.isBlank()) emptySet() else json.split("|||").toSet()
    }

    val nivelRiesgo: String get() = ultimoAssist.value?.nivel ?: "BAJO"
    val puntajeAssist: Int get() = ultimoAssist.value?.puntaje ?: 0

    val fechaUltimoAssist: String get() {
        val ts = ultimoAssist.value?.fecha ?: return "—"
        return SimpleDateFormat("d 'de' MMMM yyyy", Locale("es", "MX")).format(Date(ts))
    }

    val notificacionesActivas: Boolean = true  // not persisted in this iteration

    // ── Edit state ────────────────────────────────────────────────────────────

    var showEditarPerfil by mutableStateOf(false)
        private set
    var showLogoutDialog by mutableStateOf(false)
        private set
    var showEditarSustancias by mutableStateOf(false)
        private set
    var apodoEditando by mutableStateOf("")
        private set
    var fechaNacimientoEditando by mutableStateOf("")
        private set

    fun abrirEditarPerfil() { apodoEditando = apodo; fechaNacimientoEditando = fechaNacimiento; showEditarPerfil = true }
    fun cerrarEditarPerfil() { showEditarPerfil = false }
    fun onApodoChange(v: String) { if (v.length <= 30) apodoEditando = v }
    fun onFechaNacimientoChange(v: String) { fechaNacimientoEditando = v }
    fun guardarPerfil() {
        viewModelScope.launch {
            val current = user.value ?: UserEntity()
            userRepository.guardar(current.copy(
                apodo = apodoEditando.trim().ifBlank { current.apodo },
                fechaNacimiento = fechaNacimientoEditando
            ))
            showEditarPerfil = false
        }
    }

    fun toggleSustancia(id: String) {
        viewModelScope.launch {
            val current = user.value ?: UserEntity()
            val set = sustanciasSeleccionadas.toMutableSet().also {
                if (id in it) it.remove(id) else it.add(id)
            }
            userRepository.guardar(current.copy(sustanciasJson = set.joinToString("|||")))
        }
    }

    fun abrirEditarSustancias() { showEditarSustancias = true }
    fun cerrarEditarSustancias() { showEditarSustancias = false }
    fun abrirLogoutDialog() { showLogoutDialog = true }
    fun cerrarLogoutDialog() { showLogoutDialog = false }

    fun progresoRiesgo(): Float = when (nivelRiesgo) {
        "BAJO" -> puntajeAssist / 27f * 0.40f
        "MODERADO" -> puntajeAssist / 27f * 0.75f
        "ALTO" -> 1f
        else -> 0f
    }

    fun colorNivel(): Color = when (nivelRiesgo) {
        "BAJO" -> Color(0xFF065F46); "MODERADO" -> Color(0xFFd97706); else -> Color(0xFFE24B4A)
    }

    fun bgColorNivel(): Color = when (nivelRiesgo) {
        "BAJO" -> Color(0xFFD1FAE5); "MODERADO" -> Color(0xFFfef9c3); else -> Color(0xFFfde8e8)
    }

    private fun calcularRacha(entries: List<BitacoraEntity>): Int {
        var racha = 0
        var fecha = LocalDate.now()
        while (true) {
            val e = entries.firstOrNull {
                Instant.ofEpochMilli(it.fecha).atZone(ZoneId.systemDefault()).toLocalDate() == fecha
            }
            if (e == null || e.consumio) break
            racha++; fecha = fecha.minusDays(1)
        }
        return racha
    }

    private fun calcularMejorRacha(entries: List<BitacoraEntity>): Int {
        var mejor = 0; var actual = 0
        entries.sortedBy { it.fecha }.forEach { e ->
            if (!e.consumio) { actual++; if (actual > mejor) mejor = actual } else actual = 0
        }
        return mejor
    }
}
```

- [ ] **Step 2: Update MiPerfilScreen.kt — collect StateFlows for racha/mejorRacha/diagnosticos/contactos**

Find in `MiPerfilScreen.kt` all direct references to `viewModel.rachaActual`, `viewModel.mejorRacha`, `viewModel.diagnosticosCompletados`, `viewModel.cantidadContactos`. These are now `StateFlow<Int>` instead of `Int`.

Add at the top of `MiPerfilScreen` composable:
```kotlin
val rachaActual by viewModel.rachaActual.collectAsState()
val mejorRacha by viewModel.mejorRacha.collectAsState()
val diagnosticosCompletados by viewModel.diagnosticosCompletados.collectAsState()
val cantidadContactos by viewModel.cantidadContactos.collectAsState()
```

Replace all direct `viewModel.rachaActual` → `rachaActual`, etc. in the composable body.

Add import if missing:
```kotlin
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
```

- [ ] **Step 3: Full compile check**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -30
```

Expected: BUILD SUCCESSFUL with zero errors.

- [ ] **Step 4: Final commit**

```bash
git add app/src/main/java/com/solvyx/ui/screens/perfil/
git commit -m "feat(perfil): load user profile, racha, and assist data from Room"
```

---

## Self-Review Checklist

**Spec coverage:**
- [x] 7 entities (UserEntity, ContactoSosEntity, ResultadoAssistEntity, BitacoraEntity, PlanEntity, LogroEntity, SosEventEntity)
- [x] TypeConverters (`"|||"` separator)
- [x] 7 DAOs with Flow queries
- [x] AppDatabase v2, `fallbackToDestructiveMigration()`, SEED_CALLBACK (5 logros)
- [x] 7 repositories (`@Singleton @Inject`)
- [x] AppModule wires all DAOs
- [x] DiagnosticoRepository: `evaluarYGuardar` removed → `evaluar()` added
- [x] RegisterViewModel saves UserEntity
- [x] RedApoyoViewModel: ContactoSOS dropped, ContactoSosEntity exposed
- [x] DiagnosticoViewModel: injects AssistRepository, routes saves through it
- [x] RegistroViewModel: real BitacoraRepository, real `guardarRegistro()`, real `fechasConRegistro`
- [x] HistorialBitacoraScreen: receives RegistroViewModel, renders BitacoraEntity list
- [x] PlanViewModel: persists metaIndex + metaLogradaHoy via PlanRepository
- [x] AvancesViewModel: computes racha/charts/logros from real DB; auto-unlocks logros
- [x] MisAvancesScreen: `viewModel.logros` → `viewModel.uiLogros`
- [x] SosViewModel: loads phones from ContactoSos, logs SOS events
- [x] PerfilViewModel: UserRepository + AssistRepository + BitacoraRepository + ContactoSosRepository
- [x] DatabaseProvider.kt deleted
- [x] ResultadoEntity.kt deleted
- [x] ResultadoDao.kt deleted

**Constraints honored:**
- No Firebase, no cannabis, no anxiety fields, no Detonantes/Metas entities
- No blocking calls on main thread (all DB ops in `viewModelScope.launch`)
- No manual singletons outside Hilt
- `@RequiresApi(Build.VERSION_CODES.O)` on all classes using `java.time.*`

**Placeholder scan:** None found.

**Type consistency:**
- `ContactoSosEntity` used consistently (ViewModel + Screen)
- `ResultadoAssistEntity` used in AssistRepository, DiagnosticoViewModel, PerfilViewModel
- `BitacoraEntity` used consistently in Bitacora stack + AvancesViewModel + PerfilViewModel
- `LogroEntity` → `UiLogro` mapping in AvancesViewModel; `LogroCard` updated to `UiLogro`
- `StateFlow<Set<LocalDate>>` in RegistroViewModel; collected with `collectAsState()` in Screen
