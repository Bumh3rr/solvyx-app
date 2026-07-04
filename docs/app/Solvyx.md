# Solvyx Regional

Contexto técnico para Claude Code. Lee este archivo antes de tocar cualquier parte del proyecto.

---

## Qué es este proyecto

Solvyx es una aplicación Android de reducción de daños enfocada en consumo de sustancias, dirigida a jóvenes de 15 a 24 años en Chilpancingo, Guerrero, México. Desarrollada para InnovaTec 2026 — Etapa Regional.

---

## Stack

| Capa | Tecnología |
| --- | --- |
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | MVVM + Hilt |
| Persistencia local | Room (SQLite) |
| Persistencia remota | Firebase Firestore |
| Autenticación | Firebase Auth (email/contraseña + anónimo) |
| Animaciones | Lottie Compose |
| SMS | Android SmsManager |
| Tipografía | Nunito (familia completa) |
| HTTP | Retrofit + OkHttp (instalado, en uso futuro para agentes) |
| Serialización | Gson |
| DI | Hilt con @HiltViewModel en todos los ViewModels |

---

## Estructura del proyecto

```
com.solvyx/
├── MainActivity.kt
├── SolvyxApp.kt                     ← @HiltAndroidApp
├── backend/
│   ├── data/local/
│   │   ├── dao/                     ← DAOs de Room
│   │   ├── database/                ← AppDatabase
│   │   └── entity/                  ← Entidades Room + TypeConverters
│   ├── decisiontree/
│   │   ├── engine/DecisionTreeEngine
│   │   ├── model/
│   │   ├── repository/DecisionTreeRepository
│   │   └── trees/                   ← 8 árboles (4 sustancias × craving + info)
│   ├── models/
│   ├── presentation/viewmodel/
│   └── repository/                  ← Repositorios
├── di/AppModule.kt
└── ui/
    ├── components/
    ├── navigation/                  ← Routes.kt, NavGraph.kt
    ├── screens/                     ← Módulos de pantallas
    └── theme/                       ← Color.kt, Type.kt, Theme.kt
```

---

## Base de datos local — Room

Nombre: `solvyx_database` · 4 entidades únicamente.

Room almacena **solo** lo necesario para funcionar sin internet. Todo lo demás va en Firestore.

### `users`
```kotlin
id: Int                // PK, siempre 1 — usuario único local
server_id: String?     // UID de Firebase Auth. Null si es anónimo.
apodo: String
email: String?         // Null si es anónimo
es_anonimo: Boolean
sustancias_json: String // JSON array: ["alcohol","vape","cristal","cigarro"]
```

### `contactos_sos`
```kotlin
id: Int                // PK, autoGenerate
nombre: String
telefono: String       // 10 dígitos, formato nacional mexicano
orden: Int             // 0-2. orden=0 es obligatorio para activar SOS
```

### `ultimo_assist`
```kotlin
id: Int                // PK, siempre 1 — solo el resultado más reciente
sustancia_id: String   // "alcohol" | "vape" | "cristal" | "cigarro"
puntaje: Int
nivel: String          // "BAJO" | "MODERADO" | "ALTO"
fecha: Long            // epoch ms
```

### `chat_session`
```kotlin
id: Int                // PK, autoGenerate
tree_id: String        // árbol activo: "alcohol_craving", "cristal_info", etc.
nodo_actual_id: String
timestamp: Long        // epoch ms
```

**Reglas de Room:**
- `ultimo_assist` es caché de solo lectura. Nunca se escribe hacia Firestore desde Room.
- `chat_session` es 100% local. Sin ninguna relación con Firebase.
- No hay FK entre tablas. Un solo usuario por dispositivo.
- No hay campo `nivel_ansiedad` en ninguna entidad. No lo agregues.
- `sustancias_json` nunca contiene "cannabis". Siempre usar "cigarro" para tabaco/cigarro.

---

## Base de datos remota — Firebase Firestore

Estructura: todo bajo `users/{uid}/subcoleccion/{docId}`.

### Colecciones activas

```
users/{uid}
users/{uid}/bitacora/{YYYY-MM-DD}
users/{uid}/assist_resultados/{sustanciaId}
users/{uid}/metas/{metaId}
users/{uid}/sos_eventos/{id}
users/{uid}/logros_usuario/{logroId}
logros_definicion/{logroId}            ← colección raíz, catálogo compartido
```

### `users/{uid}` — campos clave
```
apodo, email, fecha_nacimiento
sustancias_seleccionadas: Array        // ["alcohol","vape","cristal","cigarro"]
assist_completado: Boolean
es_anonimo: Boolean
racha_actual: Number                   // calculado desde bitácora
mejor_racha: Number
comodin_usado: Boolean                 // racha protegida — pendiente de implementar
comodin_reset_fecha: Timestamp         // se resetea el primer día del mes siguiente
creado_en: Timestamp
```

### `bitacora/{YYYY-MM-DD}` — campos clave
```
fecha: String                          // YYYY-MM-DD (también el ID del doc)
estado_animo: String                   // "triste"|"ansioso"|"neutral"|"bien"|"euforico"
consumio: Boolean
sustancia: String?                     // solo si consumio=true
nota_animo: String?                    // máx 100 chars
nota_contexto: String?                 // máx 200 chars
meta_lograda: Boolean?
creado_en, actualizado_en: Timestamp
```

> No existe campo `nivel_ansiedad` en ningún documento de Firestore. No lo agregues.

### `metas/{metaId}` — campos clave
```
tipo: String           // "sin_consumo"|"reducir_frecuencia"|"tecnicas_regulacion"
origen: String         // "usuario" | "sugerida_berto"
sustancia: String?
titulo: String
objetivo: Number
progreso_actual: Number
unidad: String         // "dias"|"veces_semana"|"tecnicas_dia"
activa: Boolean
completada: Boolean
completada_en: Timestamp?
creado_en: Timestamp
```

### `logros_definicion/{logroId}` — catálogo
```
id, tipo, titulo, descripcion, condicion: Number, icono_id: String
```
Tipos: `racha` · `metas_completadas` · `constancia_bitacora` · `assist_completado` · `uso_herramientas`

### `logros_usuario/{logroId}`
Solo existen documentos para logros desbloqueados. Ausencia = no obtenido.
```
logro_id: String
desbloqueado: Boolean
fecha_unlock: Timestamp
```

---

## Autenticación

- Firebase Auth con email/contraseña y modalidad anónima.
- El UID de Firebase es el `server_id` en Room `users`.
- Usuarios anónimos: tienen UID válido pero no acceden a Firestore — solo funciones offline.
- JWT: Firebase lo gestiona internamente. No se guarda en Room ni en ningún lado manual.

---

## Qué funciona sin internet (offline)

| Funcionalidad | Fuente de datos |
| --- | --- |
| Botón SOS + SMS | Room `contactos_sos` |
| Berto — árboles de decisión | In-memory + Room `chat_session` |
| Guías de primeros auxilios | Hardcodeado en Android |
| Técnica 5-4-3-2-1 | Hardcodeado en Android |
| Contexto de riesgo para Berto | Room `ultimo_assist` |

Todo lo demás requiere internet.

---

## Qué requiere internet

Bitácora · ASSIST · Metas · Logros · Avances · Directorio (mapas) · Berto LLM (cuando se implemente)

---

## Sistema de diseño

### Colores principales
```kotlin
TealPrimary   = Color(0xFF1D9E75)   // botones primarios, headers
TealDark      = Color(0xFF085041)   // texto principal
BackgroundApp = Color(0xFFF8F6F1)   // fondo general
CrisisRed     = Color(0xFFE24B4A)   // SOS, emergencias
WarnAmber     = Color(0xFFD97706)   // nivel MODERADO
```

### Tipografía
Nunito en toda la app. Nunca cambiar la familia tipográfica.

### Shapes
- Pills / botones: 50dp
- Modales / Drawers: 28dp
- Cards principales: 20dp
- Cards estándar: 14–16dp

### Berto — estados
| Estado | Trigger |
| --- | --- |
| `TRANQUILO` | Default |
| `PREOCUPADO` | Keywords ansiedad / craving |
| `CELEBRANDO` | Keywords positivos / logros |
| `CRISIS` | Keywords suicidio / emergencia |

### Reglas de UI
- Sin emojis en la UI.
- Sin bottom nav en pantallas secundarias.
- `BorderCard` usa layout `Row`.

---

## Navegación

### Rutas top-level principales
```kotlin
"splash" → SplashScreen
"onboarding" → OnboardingScreen
"auth_choice" → AuthChoiceScreen
"login" → LoginScreen
"register" → RegisterScreen
"diagnostico" → DiagnosticoNavGraph
"red_apoyo_setup" → RedApoyoScreen (modo setup)
"home" → MainScreen (shell principal)
"chat" → BertoScreen
"sos_overlay" → SosOverlayScreen
"ejercicio_guiado" → EjercicioGuiadoScreen
```

### Sub-NavGraphs
- `DiagnosticoNavGraph`: selection → questions → result → history
- `GuiasNavGraph`: guiasHub → crisisId / panic / craving / overuse / crisis
- `PlanNavGraph`: planHub → manejo_craving / info_sustancia
- `PerfilNavGraph`: perfil_main → privacidad / acerca / terminos

---

## Features pendientes de implementar

### 1. Modo "Hoy me siento así"
Selector de emoción reactivo en `InicioScreen` que registra directamente en Firestore `bitacora/{fecha}` sin navegar a la pantalla de registro. Una pregunta, 5 opciones, sin fricción.

### 2. Mensajes de Berto basados en contexto real
Notificaciones personalizadas que leen datos de Firestore antes de enviarse. Requiere lógica condicional en `FirebaseMessaging` o Cloud Function. Agregar campo `ultima_notificacion_en` a `users/{uid}` para evitar spam.

### 3. Acceso directo de crisis (App Shortcut)
Android App Shortcut que aparece al mantener presionado el ícono de la app. Dos opciones: "SOS" y "Hablar con Berto". Navega directamente a `sos_overlay` o `chat` sin pasar por el flujo normal. Implementar con `shortcuts.xml` en `res/xml/`.

### 4. Racha protegida (comodín del mes)
Si el usuario lleva 10+ días de racha y consume, Berto ofrece usar el comodín del mes — la racha no se resetea pero el día queda marcado. Un comodín por mes calendario.

Campos en Firestore `users/{uid}`:
- `comodin_usado: Boolean` — false por defecto
- `comodin_reset_fecha: Timestamp` — primer día del mes siguiente

Reglas de negocio:
- Solo disponible si `racha_actual >= 10`
- Solo un comodín por mes — `comodin_usado` vuelve a `false` cuando se alcanza `comodin_reset_fecha`
- Si el usuario no activa el comodín, la racha se resetea normalmente
- El día con comodín se marca visualmente diferente en el calendario (no como día limpio, no como día de consumo)

---

## Reglas de negocio globales — nunca violar

- La racha se calcula hacia atrás desde hoy. Los días sin registro son **neutros** — no rompen la racha. Solo `consumio=true` rompe la racha.
- Las sustancias válidas son exactamente: `alcohol`, `vape`, `cristal`, `cigarro`. Nunca `cannabis`, nunca `tabaco` como ID.
- No existe `nivel_ansiedad` en ninguna capa — ni Room, ni Firestore, ni UI.
- El directorio profesional está hardcodeado en Android con datos reales de Chilpancingo. No es mock. No moverlo a Firestore.
- Línea de la Vida: `800 911 2000`. SAPTEL: `55 5259 8121`. Estos números no cambian.
- Máximo 3 contactos SOS por usuario. El primero (orden=0) es obligatorio.
- Máximo 3 metas activas simultáneas por usuario.

---

## Pendientes de decisión (no implementar hasta confirmar)

- Historial de conversación Berto LLM — destino de persistencia sin definir.
- Widget de home screen (Glance API) — en evaluación. Por ahora se implementa solo el App Shortcut.
- Arquitectura del agente Berto LLM — API propia vs API key externa (DeepSeek).
