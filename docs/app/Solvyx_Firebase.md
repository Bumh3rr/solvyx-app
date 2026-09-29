# Solvyx — Base de Datos Remota (Firebase)

> **Estado:** Borrador en evolución. Las colecciones, subcolecciones y campos pueden actualizarse conforme avanza la implementación. Esta página refleja el último acuerdo del equipo, no un esquema cerrado.

## Contexto

En la Etapa Regional, la persistencia se divide en dos partes: **Firebase** (esta página) y **Room** (SQLite local en Android). Firebase gestiona todos los datos que requieren conexión a internet; Room gestiona únicamente los datos necesarios para el funcionamiento offline.

Firebase cubre: perfil del usuario, bitácora emocional, resultados ASSIST, metas, logros y log de eventos SOS.

La autenticación se gestiona con **Firebase Auth**, soportando dos modalidades: registro con correo y contraseña, y modalidad anónima (Firebase Anonymous Auth). Los usuarios anónimos tienen un UID válido pero no tienen acceso a las funcionalidades de Firestore — solo pueden usar las funciones offline.

El UID de Firebase Auth es el identificador que vincula todos los documentos del usuario en Firestore, y es el mismo valor almacenado en el campo `server_id` de la tabla `users` en Room.

> **Acceso a Firestore centralizado (jul 2026):** `AuthRepository` y `AssistRepository` ya no tocan `FirebaseFirestore` directamente ni tienen strings de nombre de campo sueltos. El acceso vive en `backend/data/remote/datasource/`: `UserRemoteDataSource` (colección `users`) y `AssessmentRemoteDataSource` (subcolección `assist_results`), cada uno con sus propios DTOs (`UserRemoteDto`, `AssessmentResultRemoteDto` en `backend/data/remote/model/`) que centralizan los nombres de campo como constantes. Ver diccionario completo en `docs/superpowers/specs/2026-07-07-data-layer-english-restructure-design.md`.

---

## Pendientes

- [ ] Historial de conversación del agente Berto LLM — aún sin destino de persistencia definido (Firestore, API externa, o sin persistencia). No se documenta hasta confirmar la arquitectura del agente.
- [ ] Nuevas features aprobadas (`comodin_usado`, `comodin_reset_fecha` en `users`) — pendientes de agregar al esquema una vez que se confirme la implementación de la racha protegida.

---

## Estructura general

Todos los datos del usuario se anidan bajo `users/{uid}`, siguiendo el patrón `users/{uid}/subcoleccion/{docId}`. La única excepción es `logros_definicion`, que es una colección raíz compartida entre todos los usuarios.

| Ruta | Cardinalidad | Contenido |
| --- | --- | --- |
| `users/{uid}` | 1 doc / usuario | Perfil, racha y estado general |
| `users/{uid}/bitacora/{fecha}` | 1 doc / día | Registro emocional diario |
| `users/{uid}/assist_results/{autoId}` | N docs / usuario | Resultados ASSIST con detalle P2–P8, uno por intento |
| `users/{uid}/metas/{metaId}` | N docs / usuario | Metas activas e históricas |
| `users/{uid}/sos_eventos/{id}` | N docs / usuario | Log de auditoría de activaciones SOS |
| `users/{uid}/logros_usuario/{logroId}` | N docs / usuario | Logros desbloqueados |
| `logros_definicion/{logroId}` | Catálogo fijo | Definición de todos los logros posibles |

---

## Colecciones

### `users/{uid}`

Perfil del usuario. Un documento por usuario, identificado por su UID de Firebase Auth.

| Campo                 | Tipo | Requerido | Descripción / regla de negocio |
|-----------------------| --- | --- | --- |
| `nickname`            | String | Sí | Nombre o alias del usuario. Máximo 30 caracteres. |
| `email`               | String | Condicional | Nulo si el usuario opera en modalidad anónima. |
| `birth_date`          | Timestamp | Sí | Se usa para calcular la edad del usuario. |
| `selected_substances` | Array | Sí | Valores: `alcohol`, `vape`, `cristal`, `cigarro`. |
| `assist_completed`    | Boolean | Sí | `false` por defecto. `true` al completar el ASSIST por primera vez. |
| `is_anonymous`        | Boolean | Sí | `true` si opera con Firebase Anonymous Auth. |
| `current_streak`      | Number | Calculado | Días consecutivos sin consumo. Solo `consumed=true` (bitácora) rompe la racha; los días sin registro son neutros. Se escribe, pero el cliente Android nunca lo lee de vuelta — hoy la racha mostrada en Mi Perfil se calcula en el propio cliente a partir de `journal` en Room. |
| `best_streak`         | Number | Calculado | Máxima racha histórica. Solo se sobreescribe si `current_streak` supera este valor. Mismo caso que arriba: se escribe, no se lee desde el cliente. |
| `comodin_usado`       | Boolean | Sí | `false` por defecto. `true` cuando el usuario usó su comodín del mes. *(pendiente de implementación — nombre aún no traducido, no forma parte del diccionario de renombrado porque el campo no existe todavía en `UserRemoteDto`/código)* |
| `comodin_reset_fecha` | Timestamp | Calculado | Fecha en que se resetea el comodín. Se calcula al primer día del siguiente mes. *(pendiente de implementación, mismo caso que arriba)* |
| `created_at`          | Timestamp | Auto | `FieldValue.serverTimestamp()` |

Campos y colección definidos como constantes en `UserRemoteDto` (`backend/data/remote/model/UserRemoteDto.kt`); la lectura/escritura real vive en `UserRemoteDataSource`.

---

### `users/{uid}/bitacora/{fecha}`

Registro emocional diario. Un documento por día, identificado por la fecha en formato `YYYY-MM-DD`. Si el usuario registra más de una vez en el mismo día, se sobreescribe (merge).

| Campo           | Tipo | Requerido | Descripción / regla de negocio |
|-----------------| --- | --- | --- |
| `fecha`         | String | Sí | Clave del documento. Formato `YYYY-MM-DD`. |
| `estado_animo`  | String | Sí | Valores: `triste`, `ansioso`, `neutral`, `bien`, `euforico`. |
| `nota_animo`    | String | No | Texto libre. Máximo 100 caracteres. |
| `consumio`      | Boolean | Sí | `true` si el usuario consumió alguna sustancia ese día. |
| `sustancia`     | String | Condicional | Solo si `consumio=true`. Valores: `alcohol`, `vape`, `cristal`, `cigarro`. |
| `cantidad_aprox` | String | No | Cantidad aproximada en texto libre. Ej: `2 cervezas`, `una dosis`. |
| `nota_contexto` | String | No | Contexto del consumo. Máximo 200 caracteres. |
| `meta_lograda`  | Boolean | No | **Heredado, ya no se escribe.** Lo usaba el "Lo logré hoy" del plan viejo; se sigue leyendo para días antiguos. Las metas viven en `metas`. |
| `creado_en`     | Timestamp | Auto | `FieldValue.serverTimestamp()` al guardar por primera vez. |
| `actualizado_en` | Timestamp | Auto | Se actualiza en cada operación merge sobre el documento. |

> **Regla de negocio:** El campo `nivel_ansiedad` fue evaluado y descartado deliberadamente. No debe reincorporarse.

---

### `users/{uid}/assist_results/{autoId}` (ex `assist_resultados`)

Resultados del cuestionario ASSIST. Un documento por cada intento completado (ID autogenerado) — un usuario puede repetir el cuestionario de la misma sustancia y conserva el historial completo. El campo `substance` identifica a cuál pertenece cada documento.

| Campo | Tipo | Requerido | Descripción / regla de negocio |
| --- | --- | --- | --- |
| `substance` | String | Sí | ID de la sustancia: `alcohol`, `vape`, `cristal`, `cigarro`. |
| `p2_frequency` | Number | Sí | Respuesta P2. Valores: 0, 2, 3, 4, 6. |
| `p3_craving` | Number | Sí | Respuesta P3. Valores: 0, 3, 4, 5, 6. *(ya estaba en inglés, sin cambio)* |
| `p4_problems` | Number | Sí | Respuesta P4. Valores: 0, 4, 5, 6, 7. |
| `p5_obligations` | Number | Sí | Respuesta P5. Valores: 0, 3, 6. |
| `p6_concern` | Number | Sí | Respuesta P6. Valores: 0, 3, 6. |
| `p7_attempts` | Number | Sí | Respuesta P7. Valores: 0, 3, 6. |
| `p8_injected` | Number | Condicional | Solo presente si `substance = cristal`. Valores: 0, 2, 3. |
| `total_score` | Number | Calculado | Suma de P2+P3+P4+P5+P6+P7. Nunca incluye P8. Calculado en el cliente antes de guardar. |
| `risk_level` | String | Calculado | `BAJO` (0–10) · `MODERADO` (11–26) · `ALTO` (27+). |
| `recommendation` | String | Calculado | Texto de recomendación según `risk_level`. |
| `date` | Timestamp | Auto | `FieldValue.serverTimestamp()` |

Campos y colección definidos como constantes en `AssessmentResultRemoteDto` (`backend/data/remote/model/AssessmentResultRemoteDto.kt`); la lectura/escritura real vive en `AssessmentRemoteDataSource`.

---

### `users/{uid}/metas/{metaId}`

Metas de reducción de daños. Pueden ser creadas manualmente por el usuario o sugeridas por Berto. Máximo 3 metas activas simultáneas.

| Campo | Tipo | Requerido | Descripción / regla de negocio |
| --- | --- | --- | --- |
| `tipo` | String | Sí | `sin_consumo`, `reducir_frecuencia`, `tecnicas_regulacion`. |
| `origen` | String | Sí | `usuario` (creada manualmente) o `sugerida_berto` (propuesta por el agente). |
| `sustancia` | String | No | Nulo = cualquier sustancia (solo en metas `sin_consumo` creadas por el usuario). |
| `titulo` | String | Sí | Descripción breve. La genera la app (ej. `5 días sin vapear`); el usuario no escribe texto libre. |
| `objetivo` | Number | Sí | `sin_consumo`: días (ej. `5`). `reducir_frecuencia`: semanas a cumplir (ej. `3`). |
| `progreso_actual` | Number | Sí | Días o semanas cumplidos. Se calcula en el teléfono desde la bitácora. |
| `unidad` | String | Sí | `dias` (`sin_consumo`) o `semanas` (`reducir_frecuencia`). |
| `limite_semanal` | Number | Condicional | Solo en `reducir_frecuencia`: máximo de días de consumo por semana (ej. `2`). |
| `fecha_inicio` | Timestamp | Sí | Fecha de creación de la meta. |
| `fecha_limite` | Timestamp | No | Nulo si la meta no tiene plazo fijo. |
| `activa` | Boolean | Sí | `true` = activa. `false` = archivada o completada. |
| `completada` | Boolean | Sí | `false` por defecto. `true` cuando `progreso_actual >= objetivo`. |
| `completada_en` | Timestamp | Condicional | Solo existe si `completada=true`. |
| `creado_en` | Timestamp | Auto | `FieldValue.serverTimestamp()` |

**Cálculo del progreso** (`GoalProgressCalculator`). Nada se reinicia: un día con consumo simplemente no suma.

- Solo cuentan los días en que el usuario respondió si consumió (`consumed` no nulo). El ánimo rápido de Inicio y los días sin registro son neutros.
- `sin_consumo`: +1 por día registrado sin consumir esa sustancia (no tienen que ser seguidos).
- `reducir_frecuencia`: bloques de 7 días desde `fecha_inicio`. Una semana suma +1 cuando ya terminó, tiene al menos 4 días registrados y los días de consumo no pasan de `limite_semanal`.

---

### `users/{uid}/sos_eventos/{id}`

Log de auditoría de cada activación del botón SOS. Se crea un documento por cada evento en que la cuenta regresiva termina sin que el usuario cancele.

| Campo | Tipo | Requerido | Descripción / regla de negocio |
| --- | --- | --- | --- |
| `fecha` | Timestamp | Sí | Fecha y hora exacta de la activación. |
| `contactos_notificados` | Array\<String\> | Sí | Lista de teléfonos a los que se envió el SMS. |
| `cantidad_contactos` | Number | Calculado | Longitud del array. Valor: 1, 2 o 3. |
| `sms_enviado` | Boolean | Sí | `true` si SmsManager confirmó el envío. |
| `pantalla_origen` | String | No | Pantalla desde donde se activó el SOS. |
| `cancelado` | Boolean | Sí | `true` si el usuario canceló durante la cuenta regresiva. |
| `creado_en` | Timestamp | Auto | `FieldValue.serverTimestamp()` |

---

### `logros_definicion/{logroId}`

Catálogo de todos los logros posibles. Colección raíz compartida entre todos los usuarios — gestionada directamente por el equipo de desarrollo, no por los usuarios.

| Campo | Tipo | Requerido | Descripción / regla de negocio |
| --- | --- | --- | --- |
| `id` | String | Sí | Identificador único. Coincide con el ID del documento. |
| `tipo` | String | Sí | `racha`, `metas_completadas`, `constancia_bitacora`, `assist_completado`, `uso_herramientas`. |
| `titulo` | String | Sí | Nombre visible del logro en la UI. |
| `descripcion` | String | Sí | Texto explicativo de la condición para desbloquearlo. |
| `condicion` | Number | Sí | Valor numérico objetivo. Ej: `7` (días de racha), `5` (metas completadas). |
| `icono_id` | String | Sí | Identificador del recurso visual en Android. |

**Logros iniciales definidos:**

| ID | Tipo | Condición |
| --- | --- | --- |
| `racha_3` | `racha` | 3 días consecutivos sin consumo |
| `racha_7` | `racha` | 7 días consecutivos sin consumo |
| `racha_15` | `racha` | 15 días consecutivos sin consumo |
| `racha_30` | `racha` | 30 días consecutivos sin consumo |
| `metas_completadas_1` | `metas_completadas` | Completar 1 meta |
| `metas_completadas_5` | `metas_completadas` | Completar 5 metas |
| `metas_completadas_10` | `metas_completadas` | Completar 10 metas |
| `constancia_bitacora_7` | `constancia_bitacora` | Registrar 7 días seguidos en la bitácora |
| `assist_completado` | `assist_completado` | Completar el ASSIST por primera vez |
| `uso_herramientas_berto` | `uso_herramientas` | Iniciar 5 conversaciones con Berto |
| `uso_herramientas_5421` | `uso_herramientas` | Usar la técnica 5-4-3-2-1 por primera vez |

---

### `users/{uid}/logros_usuario/{logroId}`

Logros desbloqueados por el usuario. El ID del documento coincide con el ID en `logros_definicion`. Solo existen documentos para logros ya desbloqueados — su ausencia indica que el logro aún no fue obtenido.

| Campo | Tipo | Requerido | Descripción / regla de negocio |
| --- | --- | --- | --- |
| `logro_id` | String | Sí | Referencia al ID del logro en `logros_definicion`. |
| `desbloqueado` | Boolean | Sí | `true` cuando el usuario cumplió la condición. |
| `fecha_unlock` | Timestamp | Auto | `FieldValue.serverTimestamp()` al momento del desbloqueo. |

---

## Resumen

| Tabla | Cardinalidad | Origen de los datos |
| --- | --- | --- |
| `users/{uid}` | 1 doc / usuario | Firebase Auth + ingreso del usuario |
| `users/{uid}/bitacora/{fecha}` | 1 doc / día | Ingreso del usuario |
| `users/{uid}/assist_results/{autoId}` | N docs / usuario | Calculado en cliente, guardado en Firestore |
| `users/{uid}/metas/{metaId}` | N docs / usuario | Ingreso del usuario o sugerencia de Berto |
| `users/{uid}/sos_eventos/{id}` | N docs / usuario | Generado automáticamente por la app |
| `logros_definicion/{logroId}` | Catálogo fijo | Gestionado por el equipo de desarrollo |
| `users/{uid}/logros_usuario/{logroId}` | N docs / usuario | Generado automáticamente por la app |
