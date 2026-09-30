# Solvyx — Base de Datos Local (Room)

> **Estado:** Borrador en evolución. Los atributos de las tablas pueden actualizarse, agregarse o eliminarse conforme avanza la implementación. Esta página refleja el último acuerdo, no un esquema cerrado.

---

## Contexto

En la Etapa Regional, la persistencia se divide en dos partes: **MySQL** (vía la API en Spring Boot 3) y **Room** (local, en Android). Esta página cubre solo Room.

Room almacena lo necesario para que la app funcione sin conexión a internet: SOS, Berto en modo árboles de decisión, la sesión activa del usuario, y el detalle de ASSIST más reciente como caché de lectura. Desde la incorporación de las tablas `journal`, `plan`, `achievements` y `sos_events`, bitácora, plan de metas, logros y el log de eventos SOS también se persisten localmente en Room — estas cuatro tablas aún **no** se sincronizan con Firestore (ver Pendientes en `Solvyx_Firebase.md`). El historial completo de ASSIST sí vive en Firestore (`assist_results`); `last_assist` en Room es solo una copia de lectura del resultado más reciente.

Room no tiene relaciones de llave foránea entre tablas. Cada dispositivo representa a un único usuario en sesión, por lo que tablas como `users`, `chat_session`, `last_assist` y `plan` son de fila única (el `id` siempre es 1), y no existe una columna `user_id` enlazando las demás tablas entre sí.

Nombre del archivo de base de datos: `solvyx_database`. Versión de esquema actual: 6 (`AppDatabase.kt`).

---

## Pendientes de nomenclatura (resueltos)

- `refresh_jwt` / `refresh_token` — resuelto: ninguno de los dos se implementa. Firebase Auth gestiona el token internamente; no se persiste manualmente en Room ni en ningún otro lado.
- `ultimo_assist` — resuelto en su momento: se confirmó este nombre en español como final (no se renombra a `assist_cache`/`contexto_riesgo`/`assist_snapshot`). Posteriormente, como parte del refactor de nombres a inglés (jul 2026), la tabla se renombró a `last_assist` — ver diccionario completo en `docs/superpowers/specs/2026-07-07-data-layer-english-restructure-design.md`.

---

## Tablas

### `users`

Sesión activa del usuario en el dispositivo. Fila única (`id` siempre 1). Entidad: `UserEntity`.

> **Nota:** `apodo`, `email` y las fechas de nacimiento/registro ya **no** viven en Room. El perfil completo se obtiene bajo demanda desde Firestore vía `AuthRepository.getProfile(): UserRemoteDto?` (ver `Solvyx_Firebase.md`). Room `users` ahora solo guarda lo mínimo para la sesión offline y el estado de sustancias.

| Campo | Tipo | Notas |
| --- | --- | --- |
| `id` | INTEGER | PK, fijo en 1 |
| `server_id` | TEXT | UID de Firebase Auth. Nulo hasta el primer registro/login exitoso. |
| `is_anonymous` | BOOLEAN | Controla qué funciones están disponibles sin cuenta |
| `substances_json` | JSON | Array de sustancias seleccionadas: alcohol, vape, cristal, cigarro |

---

### `sos_contacts` (ex `contactos_sos`)

Contactos de confianza para el envío de SMS de emergencia. Máximo 3 filas. Entidad: `SosContactEntity` (ex `ContactoSosEntity`).

| Campo | Tipo | Notas |
| --- | --- | --- |
| `id` | INTEGER | PK, autoincremental |
| `name` | VARCHAR(50) | |
| `phone` | VARCHAR(10) | Formato nacional mexicano. La longitud es documental, no se impone a nivel de SQLite. |
| `position` | INTEGER | Posición 0–2. El contacto en posición 0 es obligatorio para activar el SOS. *(no `order`: es palabra reservada en SQL)* |

---

### `chat_session`

Punto de avance del usuario dentro de los árboles de decisión de Berto. Fila única. Entidad: `ChatSessionEntity` (ya en inglés, fuera del alcance del diccionario de renombrado — el paquete de árboles de decisión de Berto está deliberadamente fuera de alcance de ese refactor).

| Campo | Tipo | Notas |
| --- | --- | --- |
| `id` | INTEGER | PK, autoincremental |
| `tree_id` | TEXT | Árbol activo, ej. `alcohol_craving` |
| `current_node_id` | TEXT | Nodo donde quedó la conversación |
| `timestamp` | INTEGER | epoch ms de la última interacción |

> **Nota de estado (jul 2026):** al revisar `AppDatabase.kt`, `ChatSessionEntity` no aparece en la lista `entities = [...]` ni tiene DAO propio — la tabla no se crea en el esquema activo (versión 6). No es parte de este refactor de nombres; se deja documentada la intención de diseño, pendiente de revisión aparte.

---

### `last_assist` (ex `ultimo_assist`)

Copia de solo lectura del resultado ASSIST más reciente, para que Berto tenga contexto de riesgo sin conexión. Fila única. Entidad: `LastAssistEntity` (ex `UltimoAssistEntity`). Los campos de resultado (`substance_id`, `score`, `level`, `date`) se sobrescriben por completo cada vez que se guarda un nuevo resultado en Firestore — no son origen de datos hacia Firestore, solo caché de lectura. `total_completed` es la excepción: es un contador acumulado que la app mantiene localmente (nunca se resetea, se incrementa en cada ASSIST completado) para mostrar el total de diagnósticos hechos incluso sin conexión o en modo anónimo, donde el historial completo de Firestore no está disponible.

| Campo | Tipo | Notas |
| --- | --- | --- |
| `id` | INTEGER | PK, fijo en 1 |
| `substance_id` | TEXT | alcohol, vape, cristal, cigarro |
| `score` | INTEGER | |
| `level` | TEXT | BAJO, MODERADO, ALTO |
| `date` | INTEGER | epoch ms |
| `total_completed` | INTEGER | Contador acumulado de ASSIST completados. Default 0. |

---

### `journal` (ex `bitacora`)

Registro emocional diario, local. Entidad: `JournalEntity` (ex `BitacoraEntity`). A diferencia de las demás tablas, **no** es de fila única: se agrega un registro por cada guardado. Aún no se sincroniza con Firestore (ver Pendientes en `Solvyx_Firebase.md`).

| Campo | Tipo | Notas |
| --- | --- | --- |
| `id` | INTEGER | PK, autoincremental |
| `date` | INTEGER | epoch ms del registro |
| `mood` | TEXT | Estado de ánimo registrado |
| `consumed` | BOOLEAN | `true` si el usuario consumió alguna sustancia ese día. Es el único campo que rompe la racha (ver regla de negocio en Perfil/Avances). |
| `substance` | TEXT | Nulo si `consumed=false`. Valores: alcohol, vape, cristal, cigarro |
| `note` | TEXT | Nota libre, opcional |

**DAO (`JournalDao`):** `insert(entry)` · `observe(): Flow<List<JournalEntity>>` (DESC por `date`) · `observeDates(): Flow<List<Long>>`

---

### `plan` — OBSOLETA

**Sin uso desde el rediseño de Mi plan (sept. 2026):** las metas viven en Firestore `metas` (ver `Solvyx_MiPlan.md`). La tabla, `PlanEntity` y `PlanDao` se quedan solo para no cambiar el esquema (ver el riesgo al final). Antes guardaba el índice del consejo del día.

| Campo | Tipo | Notas |
| --- | --- | --- |
| `id` | INTEGER | PK, fijo en 1 |
| `goal_index` | INTEGER | Índice de la meta activa |
| `goal_achieved_today` | BOOLEAN | `true` si la meta se marcó como lograda hoy |
| `date` | INTEGER | epoch ms |

---

### `achievements` (ex `logros`)

Logros desbloqueados localmente. Entidad: `AchievementEntity` (ex `LogroEntity`). Aún no se sincroniza con Firestore (la colección `logros_definicion`/`logros_usuario` de Firestore documentada en `Solvyx_Firebase.md` sigue siendo el esquema acordado, no implementado).

| Campo | Tipo | Notas |
| --- | --- | --- |
| `id` | TEXT | PK. Coincide con el ID de logro (ej. `racha_7`) |
| `unlocked` | BOOLEAN | Default `false` |
| `unlock_date` | INTEGER | epoch ms, nulo hasta desbloquearse |

**DAO (`AchievementDao`):** `insertAll(list)` · `update(achievement)` · `observe(): Flow<List<AchievementEntity>>`

> **Nota:** `AppDatabase.SEED_CALLBACK` siembra 5 filas al crear la DB vía `execSQL` crudo: `racha_3`, `racha_7`, `racha_10`, `racha_15`, `racha_30` (columnas `id, unlocked, unlockDate` — nombres de columna literales en camelCase, sin `@ColumnInfo`, a diferencia de la convención snake_case usada en esta página para documentar). Este set de semilla (incluye `racha_10`) difiere del catálogo `logros_definicion` documentado en `Solvyx_Firebase.md` (que no incluye `racha_10` pero sí logros de otros tipos: `metas_completadas_*`, `constancia_bitacora_7`, etc.) — discrepancia observada, no resuelta en este cambio de documentación.

---

### `sos_events`

Log de auditoría local de activaciones del botón SOS. Entidad: `SosEventEntity` (sin cambio de nombre). Aún no se sincroniza con Firestore.

| Campo | Tipo | Notas |
| --- | --- | --- |
| `id` | INTEGER | PK, autoincremental |
| `date` | INTEGER | epoch ms de la activación |
| `notified_phones` | TEXT | Teléfonos notificados, unidos con separador `\|\|\|` (`SosRepository.registerEvent`) |

**DAO (`SosEventDao`):** `insert(event)` · `observe(): Flow<List<SosEventEntity>>` (DESC por `date`)

---

## Resumen

| Tabla | Cardinalidad | Origen de los datos |
| --- | --- | --- |
| `users` | 0–1 fila | UID de Firebase Auth (login/registro) + sustancias elegidas por el usuario |
| `sos_contacts` | 0–3 filas | Ingreso directo del usuario |
| `chat_session` | 0–1 fila | Generado por la app *(actualmente no registrada en `AppDatabase`, ver nota arriba)* |
| `last_assist` | 0–1 fila | Copia local de un resultado ASSIST guardado en Firestore |
| `journal` | N filas | Ingreso directo del usuario. Sin sincronizar a Firestore todavía |
| `plan` | 0–1 fila | **Obsoleta**: ya nadie la escribe ni la lee |
| `achievements` | Filas sembradas + actualizadas | Semilla local (`SEED_CALLBACK`) + `ProgressRepository.unlockAchievement()` |
| `sos_events` | N filas | Generado automáticamente al activarse el SOS |

---

## ⚠️ Riesgo: `fallbackToDestructiveMigration`

`AppModule` crea la base con `.fallbackToDestructiveMigration()`. Si alguien sube la `version` de `AppDatabase` (o cambia una entidad) **sin escribir una migración**, Room **borra todas las tablas** al abrir la app. Eso incluye `sos_contacts`, que **solo existen en el teléfono** (sin copia en Firestore): el usuario perdería sus contactos de emergencia sin aviso.

**Recomendación para el equipo:**
1. Quitar `.fallbackToDestructiveMigration()` y activar `exportSchema = true` (con `room.schemaLocation`) para versionar el esquema.
2. Escribir una `Migration` por cada cambio de esquema (y un test con `MigrationTestHelper`).
3. Mientras tanto, no cambiar el esquema; por eso la tabla `plan` obsoleta no se borró.
