# SolvyxApp — Documentación Técnica Completa

> App Android de apoyo para personas con consumo de sustancias.  
> Stack: Kotlin · Jetpack Compose · MVVM · Hilt · Room

---

## Tabla de Contenidos

1. [Visión General](#1-visión-general)
2. [Arquitectura](#2-arquitectura)
3. [Estructura de Paquetes](#3-estructura-de-paquetes)
4. [Módulos y Pantallas](#4-módulos-y-pantallas)
5. [Base de Datos Local (Room)](#5-base-de-datos-local-room)
6. [Repositorios](#6-repositorios)
7. [ViewModels](#7-viewmodels)
8. [Árboles de Decisión (Berto)](#8-árboles-de-decisión-berto)
9. [Navegación](#9-navegación)
10. [Sistema de Diseño](#10-sistema-de-diseño)
11. [Componentes Compartidos](#11-componentes-compartidos)
12. [Dependencias](#12-dependencias)
13. [Estado Actual y Pendientes](#13-estado-actual-y-pendientes)

---

## 1. Visión General

| Atributo | Valor |
|---|---|
| Package | `com.solvyx` |
| Application ID | `com.solvyx` |
| minSdk | 24 (Android 7.0) |
| targetSdk | 36 |
| compileSdk | 36 |
| versionCode | 1 |
| versionName | 1.0 |
| Lenguaje | Kotlin |
| UI Framework | Jetpack Compose + Material 3 |
| Arquitectura | MVVM + Hilt DI |
| Persistencia | Room (SQLite local) + DataStore Preferences |
| Tipografía | Nunito (familia completa) |
| Permisos | `RECORD_AUDIO`, `SEND_SMS`, `INTERNET` |

**Mascota:** Berto — robot emocional con 7 expresiones visuales: `berto_saludando`, `berto_preocupado`, `berto_tranquilo`, `berto_feliz`, `berto_sentado_mirando_izquierda`, `berto_mira_mariposa`, `berto_cabeza` (bottom nav), más `berto_sin_internet` para estado offline.

**Sustancias soportadas:** Alcohol · Vape · Cristal · Tabaco — **NUNCA Cannabis**

**Firebase:** No integrado actualmente. Toda la persistencia es local (Room). Auth es UI-only (sin backend real).

---

## 2. Arquitectura

### Patrón general

```
UI (Composables)
    ↕
ViewModel (@HiltViewModel)
    ↕
Repository (@Singleton)
    ↕
DAO (Room)
    ↕
AppDatabase (SQLite)
```

### Inyección de dependencias

- Hilt con `@HiltViewModel` en todos los ViewModels
- `AppModule.kt` provee: `AppDatabase`, todos los DAOs, DataStore
- KSP (en lugar de KAPT) para procesamiento de anotaciones
- Todos los repositorios son `@Singleton`

### Persistencia liviana

- `DataStore Preferences` almacena `onboarding_done` (Boolean)
- `SplashViewModel` lee este valor para decidir el flujo de entrada

---

## 3. Estructura de Paquetes

```
com.solvyx/
├── MainActivity.kt
├── SolvyxApp.kt                   ← @HiltAndroidApp
│
├── backend/
│   ├── data/
│   │   └── local/
│   │       ├── dao/               ← 7 DAOs
│   │       │   ├── BitacoraDao
│   │       │   ├── ContactoSosDao
│   │       │   ├── LogroDao
│   │       │   ├── PlanDao
│   │       │   ├── ResultadoAssistDao
│   │       │   ├── SosEventDao
│   │       │   └── UserDao
│   │       ├── database/
│   │       │   └── AppDatabase    ← Room v2, 7 entidades
│   │       └── entity/            ← 7 entidades + Converters
│   │
│   ├── decisiontree/
│   │   ├── engine/DecisionTreeEngine
│   │   ├── model/                 ← DecisionNode, DecisionOption, DecisionTree, NodeType
│   │   ├── repository/DecisionTreeRepository
│   │   └── trees/                 ← 8 árboles (4 sustancias × craving + info)
│   │
│   ├── models/                    ← Pregunta, Sustancia, ResultadoDiagnostico, NivelRiesgo, OpcionesPreguntas
│   ├── presentation/viewmodel/    ← DiagnosticoViewModel, DecisionTreeViewModel
│   └── repository/                ← 8 repositorios
│
├── di/
│   └── AppModule.kt
│
└── ui/
    ├── components/
    │   ├── common/                ← Botones, TextField, BackButton, PageIndicator
    │   ├── dialog/                ← SosConfirmationDialog
    │   ├── drawer/                ← CustomDrawer, NavigationItemView, models
    │   └── navigation/            ← SolvyxBottomNavigationBar
    │
    ├── decisiontree/              ← TreesSelectionScreen, TreePlayScreen
    ├── diagnostico/               ← DiagnosticoNavGraph + 4 pantallas
    ├── navigation/                ← Routes.kt, NavGraph.kt
    ├── screens/                   ← 14 módulos (ver sección 4)
    └── theme/                     ← Color.kt, Type.kt, Theme.kt
```

---

## 4. Módulos y Pantallas

### Módulo 1 — Splash

| Archivo | `screens/splash/SplashScreen.kt` |
|---|---|
| ViewModel | `SplashViewModel` |
| Duración | 1500 ms automático |
| Lógica | Lee DataStore `onboarding_done` → navega a Onboarding o AuthChoice |
| UI | Fondo `BackgroundApp` (#F8F6F1), texto "Solvyx" centrado, displayLarge |

---

### Módulo 2 — Onboarding

| Archivo | `screens/auth/onboarding/OnboardingScreen.kt` |
|---|---|
| ViewModel | `OnboardingViewModel` |
| Páginas | 4 |
| Indicador | Dots animados (8dp → 28dp activo) |
| Persistencia | Escribe `onboarding_done = true` en DataStore al completar |

| # | Título | Estado Berto | Fondo |
|---|---|---|---|
| 1 | Tu espacio seguro | WAVE | TealPrimary |
| 2 | Diagnóstico ASSIST · OMS | PULSE | TealDark |
| 3 | Botón SOS | BOUNCE | TealPrimary |
| 4 | 100% Privado · Sin internet | FLOAT | TealDark |

---

### Módulo 3 — Auth

**Pantallas:** `AuthChoiceScreen` → `LoginScreen` / `RegisterScreen` / `ForgotPasswordScreen`

**AuthChoiceScreen** (`screens/auth/choice/AuthChoiceScreen.kt`)

| Sección | Detalle |
|---|---|
| Hero | 62% altura, fondo TealPrimary, Berto con halos (230dp) |
| Botones | "Iniciar Sesión" (filled) · "Crear cuenta" (outlined) |
| Footer | Links Términos y Privacidad |

**LoginScreen** (`screens/auth/login/`)
- ViewModel: `LoginViewModel`
- Campos: Email · Contraseña (toggle visibilidad)

**RegisterScreen** (`screens/auth/register/`)
- ViewModel: `RegisterViewModel`
- Campos: Apodo · Email · Fecha de nacimiento · Contraseña · Confirmar contraseña · Checkbox Términos
- Éxito: navega a `DIAGNOSTICO` (popUpTo AUTH_CHOICE inclusive)

**ForgotPasswordScreen** (`screens/auth/forgot_password/`)
- ViewModel: `ForgotPasswordViewModel`
- Campo: Email

> **Estado:** UI completamente implementada. Backend (Firebase Auth) pendiente de integración.

---

### Módulo 4 — Diagnóstico ASSIST

**Ubicación:** `ui/diagnostico/` · **NavGraph propio:** `DiagnosticoNavGraph`

| Pantalla | Ruta interna | Descripción |
|---|---|---|
| `SubstanceSelectionScreen` | `selection` | Grid 2×2 — Alcohol, Vape, Cristal, Tabaco |
| `QuestionsScreen` | `questions` | 6–7 preguntas ASSIST, progress bar, slide transitions |
| `ResultScreen` | `result` | Puntaje · badge nivel · recomendación · acciones sugeridas |
| `HistoryScreen` | `history` | Historial de resultados pasados (Room Flow, orden DESC) |

**ViewModel:** `DiagnosticoViewModel`  
**Persistencia:** Room → tabla `resultados_assist`

**Scoring ASSIST (OMS):**

| Nivel | Rango | Color |
|---|---|---|
| BAJO | 0–10 | TealPrimary |
| MODERADO | 11–26 | WarnAmber |
| ALTO | 27+ | CrisisRed |

**Acciones sugeridas en ResultScreen:**

| Nivel | Acción 1 | Acción 2 | Acción 3 |
|---|---|---|---|
| BAJO | Bitácora | Conoce a Berto | Mis Avances |
| MODERADO | Manejo del craving | Info por sustancia | Hablar con Berto |
| ALTO | Directorio Profesional | Botón SOS / Red Apoyo | Hablar con Berto |

---

### Módulo 5 — MainScreen (shell principal)

**Archivo:** `screens/main/MainScreen.kt`

| Atributo | Detalle |
|---|---|
| Drawer | `CustomDrawer` — 60% ancho, animación scale 0.9 + offset horizontal |
| Bottom Nav | `SolvyxBottomNavigationBar` — 4 tabs + Berto elevado + SOS flotante |
| Fondo | Gradiente vertical TealPrimary → TealDark |
| Imagen deco | `ic_decorations_hero_3_drawer`, efecto blur (Haze) |
| Bottom bar visible en | Inicio · Plan · RegistroEmocional · Avances |

**Bottom Navigation:**

| Posición | Tab | Icono |
|---|---|---|
| 1 | Inicio | `ic_home` |
| 2 | Mi Plan | `ic_plan` |
| Centro (elevado) | Berto | `berto_cabeza` |
| 3 | Mis Avances | `ic_trophy` |
| Flotante top-right | SOS | botón rojo |

**Drawer — secciones:**

| Sección | Items |
|---|---|
| Rutina | Inicio · Mi Plan · Registro diario · Mis Avances |
| Herramientas | Hablar con Berto · Guías de Primeros Auxilios · Directorio Profesional |
| Mi Cuenta | Mi Perfil · Cerrar sesión |

---

### Módulo 6 — Inicio (Home)

**Archivo:** `screens/home/InicioScreen.kt`  
**ViewModel:** `InicioViewModel`

| Sección | Detalle |
|---|---|
| Top Bar | Icono menú + "Solvyx" italic + campana con badge |
| Hero | Berto saludando (130dp) · "Hola, Alex" · fecha actual · chip racha |
| Selector de emociones | 5 íconos seleccionables (Triste / Ansioso / Neutral / Bien / Eufórico) → `AnimatedVisibility` muestra `EmocionSugerenciaCard` |
| Herramientas rápidas | Scroll horizontal: Respirar · Berto · Estoy en crisis · Buscar ayuda |
| Accesos rápidos | Grid 2×2: Mi Plan · Berto · Primeros Auxilios · Mi Registro |
| Actividad reciente | Card con 3 entradas mock |

**EmocionSugerenciaCard — mapeo:**

| Emoción | Sugerencia | Destino |
|---|---|---|
| Triste | Berto puede escucharte | → Chat |
| Ansioso | Prueba un ejercicio de respiración | → EjercicioGuiado |
| Neutral | Buen momento para registrar tu día | → RegistroEmocional |
| Bien | Buen momento para registrar tu día | → RegistroEmocional |
| Eufórico | Comparte este momento con tu red | → RedApoyo |

---

### Módulo 7 — Bitácora

**Archivos:** `screens/bitacora/`  
**ViewModel:** `RegistroViewModel`  
**Persistencia:** Room → tabla `bitacora`

**RegistroEmocionalScreen:**

| Campo | Tipo | Valores / Restricciones |
|---|---|---|
| Fecha | `CalendarBottomSheet` | `LocalDate`, solo fechas pasadas |
| Estado de ánimo | 5 íconos seleccionables (52dp) | Triste · Ansioso · Neutral · Bien · Eufórico |
| Nota | TextArea | Texto libre, máx 100 caracteres |
| ¿Consumiste? | Dos botones | No (teal) / Sí (rojo) → abre `SustanciaBottomSheet` |
| Sustancia | `SustanciaBottomSheet` | Alcohol · Cristal · Vape · Tabaco |
| Guardar | `SolvyxButton` sticky | → `RegistroExitosoDialog` |

**RegistroExitosoDialog:** Berto feliz + checkmark + resumen en 2 columnas (Ánimo / Consumo)

**HistorialBitacoraScreen:**

| Sección | Detalle |
|---|---|
| Stats | 2 columnas: N° registros totales · días sin consumo |
| Lista | `LazyColumn` cronológica (DESC) |
| Tarjeta | Fecha · badge consumo · ícono emoción (40dp) · nota truncada |

---

### Módulo 8 — Berto (Chatbot)

**Archivo:** `screens/chatbot/BertoScreen.kt`  
**ViewModel:** `ChatViewModel`

| Sección | Detalle |
|---|---|
| Top Bar | Avatar Berto (color dinámico por estado) · "En línea · Privado" · dot pulsando |
| Mensajes | `LazyColumn` — burbujas usuario (TealPrimary) / Berto (surface) |
| Typing | Peek zone: ilustración + "Berto está escribiendo..." + 3 dots animados |
| Quick Replies | Chips bajo último mensaje de Berto |
| Input | Botón SOS (rojo) · TextField · Micrófono · Enviar |
| TTS | Voz femenina español, pitch 1.15, rate 0.85 — botón mute/unmute |

**Estados de Berto:**

| Estado | Visor | Trigger |
|---|---|---|
| `TRANQUILO` | BertoVisorCalm (verde claro) | Default, árboles de info |
| `PREOCUPADO` | BertoVisorWorried (amarillo) | Keywords ansiedad/craving, árboles `_craving` |
| `CELEBRANDO` | BertoVisorCelebr (verde) | Keywords positivos / logros |
| `CRISIS` | BertoVisorCrisis (rojo) | Keywords suicidio/emergencia/crisis |

**Keywords de detección:**

| Categoría | Palabras clave |
|---|---|
| Crisis | suicidio, hacerme daño, quiero morir, no puedo más, crisis, emergencia, socorro |
| Ansiedad | ansiedad, ansioso, angustia, miedo, pánico, nervioso, estresado, craving, ganas de consumir |
| Positivo | logré, gracias, mejor, bien, lo conseguí, racha |

**Flujo de árbol de decisiones:**
1. Inicio → Menú principal (8 opciones de árbol)
2. Usuario selecciona → carga árbol correspondiente del `DecisionTreeRepository`
3. Nodos tipo `MESSAGE` presentan texto + quick replies
4. Al llegar a nodo `esFinal = true` → oferta de volver al menú
5. Texto libre fuera de opciones → respuesta guía + quick replies del nodo actual

**SpeechRecognizer:** locale `es-MX`, delay simulado 1500 ms entre mensaje y respuesta

---

### Módulo 9 — Guías de Primeros Auxilios

**Ubicación:** `screens/guias/`  
**NavGraph propio:** `GuiasNavGraph`

**GuiasHubScreen** (`screens/guias/screens/hub/GuiasHubScreen.kt`)

| Card | Ruta interna |
|---|---|
| ¿Cómo sé si estoy en crisis? | `crisisId` |
| Ansiedad y ataque de pánico | `panic` |
| Craving muy intenso | `craving` |
| Consumí de más | `overuse` |
| Estoy en crisis ahora mismo | `crisis` |

**Footer de ayuda:** Línea de la Vida (800 911 2000) · SAPTEL (55 5259 8121) · CIJ

**GuiaPanicoScreen + EjercicioGuiadoScreen** (`screens/guias/screens/panico/`)
- ViewModel: `EjercicioGuiadoViewModel` (maneja TTS + pasos)
- Técnica 5-4-3-2-1: 5 Ver · 4 Tocar · 3 Escuchar · 2 Oler · 1 Saborear
- TTS: voz femenina español, pitch 1.15, rate 0.85
- Acceso doble: desde `GuiaPánico` O como ruta top-level `EJERCICIO_GUIADO` (botón "Respirar" de InicioScreen)

**GuiaConsumiDeMasScreen:** Tabs por sustancia (Alcohol · Cristal · Vape · Tabaco)

**GuiaEstoyEnCrisisScreen:** Speech bubble Berto + CTA rojo "Avisar a mi red"

**GuiaCrisisIdScreen:** Señales físicas / emocionales / conductuales / cuándo llamar al 911

**GuiaCravingIntensoScreen:** Plan de 4 pasos + prácticas de reducción de daños

---

### Módulo 10 — Red de Apoyo

**Archivo:** `screens/red/RedApoyoScreen.kt`  
**ViewModel:** `RedApoyoViewModel`  
**Persistencia:** Room → tabla `contactos_sos`

| Modo | Activación | Top Bar | CTA Principal |
|---|---|---|---|
| Setup | Post-registro (primera vez) | Back + "1 de 1" | "Guardar perfil y comenzar" → HOME |
| Regular | Drawer → Mi Cuenta | Drawer | "Guardar cambios" |

- Mínimo 1 contacto obligatorio
- Máximo 3 contactos
- Validación: nombre ≥ 2 chars, teléfono ≥ 7 dígitos
- Done Overlay (solo setup): Berto + "¡Listo!" + botón "Empezar"

---

### Módulo 11 — Mi Plan

**Archivos:** `screens/plan/`  
**ViewModel:** `PlanViewModel`  
**Persistencia:** Room → tabla `plan`  
**NavGraph propio:** `PlanNavGraph`

**MiPlanHubScreen:**

| Sección | Detalle |
|---|---|
| Meta del día | Texto rotatorio + botones "Lo logré hoy" · "Ver otra meta" |
| Progreso semanal | 7 círculos L-M-X-J-V-S-D con estado check/vacío |
| Herramientas | Manejo del craving (→ ManejoCravingScreen) · Info por sustancia (→ InfoSustanciaScreen) |

**ManejoCravingScreen:** estrategias y técnicas de manejo del craving por sustancia  
**InfoSustanciaScreen:** información por sustancia seleccionada

---

### Módulo 12 — Mis Avances

**Archivo:** `screens/avances/MisAvancesScreen.kt`  
**ViewModel:** `AvancesViewModel`  
**Persistencia:** lee de Room → `bitacora` + `logros`

| Sección | Detalle |
|---|---|
| Hero | Racha actual · "días sin consumo" · chip mejor racha |
| Milestone card | Progress bar continuo + marcadores en 7 / 15 / 30 días |
| Tabs | Semana / Mes |
| `FeelingsChart` | Gráfica de bienestar — línea sólida (sin línea de ansiedad) |
| `ConsumptionChart` | Gráfica de barras — días de consumo en el periodo |
| Insight de Berto | `BorderCard` con observación contextual |
| Logros | `LazyRow` horizontal: 5 logros (IDs: racha_3, racha_7, racha_10, racha_15, racha_30) — 3 desbloqueados en mock |

---

### Módulo 13 — Directorio Profesional

**Archivo:** `screens/directorio/DirectorioRootScreen.kt`  
**ViewModel:** `DirectorioViewModel`

- 11 entradas reales de Chilpancingo, Guerrero
- Categorías: 1 CIJ · 2 Clínicas · 4 Psicólogos · 4 Líneas de apoyo
- Cada entrada: nombre, teléfono, dirección, horario, coordenadas (lat/lng), `mapEmbedUrl` (Google Maps WebView)
- Datos hardcodeados en el ViewModel

---

### Módulo 14 — Mi Perfil

**Archivo:** `screens/perfil/MiPerfilScreen.kt`  
**ViewModel:** `PerfilViewModel`  
**NavGraph propio:** `PerfilNavGraph`

**Rutas internas:**

```
perfil_main → perfil_privacidad
            → perfil_acerca
            → perfil_terminos
```

**Secciones de MiPerfilScreen:**

| Sección | Componentes |
|---|---|
| Header | Avatar con iniciales (Canvas) · apodo · chips de sustancias (FlowRow) |
| Mi progreso | 3 stats: Racha · días en seguimiento · registros del mes |
| Mi cuenta | "Editar perfil" (BottomSheet) · "Mis sustancias" (BottomSheet) |
| Herramientas | "Repetir diagnóstico ASSIST" · "Editar red de apoyo" |
| Información | Privacidad y datos · Acerca de Solvyx · Términos y condiciones |
| Sesión | "Cerrar sesión" rojo → `LogoutConfirmDialog` |

**Sub-pantallas:** `PrivacidadDatosScreen`, `AcercaDeSolvyxScreen`, `TerminosCondicionesScreen` (stubs con `GuiaTopBar` + back)

---

### Módulo 15 — SOS Overlay

**Archivo:** `screens/sos/SosOverlayScreen.kt`  
**ViewModel:** `SosViewModel`  
**Ruta:** top-level `Routes.SOS_OVERLAY`

- Lista de contactos de confianza (de Room `contactos_sos`)
- Botón llamar a Línea de la Vida (`800 911 2000`)
- Botón "Hablar con Berto" → navega a `CHAT` (popUpTo SOS_OVERLAY)
- Test en: `SosViewModelTest.kt`

---

### Módulo 16 — Árboles de Decisión (UI)

**Archivos:** `ui/decisiontree/`

- `TreesSelectionScreen`: selector de árboles disponibles (no conectado al nav principal)
- `TreePlayScreen`: reproduce cualquier árbol con UI de chat simplificada

> Estos componentes existen pero no están integrados al nav principal; el chatbot de Berto consume los árboles internamente vía `ChatViewModel`.

---

### Módulo obsoleto — Configuración

**Archivo:** `screens/configuracion/ConfiguracionScreen.kt`  
**Estado:** Obsoleto — funcionalidad migrada a Mi Perfil. Pendiente eliminación.

---

## 5. Base de Datos Local (Room)

**Nombre del archivo:** `solvyx_database`  
**Clase:** `AppDatabase` (versión 2)  
**Ubicación:** `backend/data/local/database/AppDatabase.kt`

### Tablas

#### `users`

```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,    // siempre ID=1 (usuario único)
    val apodo: String = "",
    val email: String = "",
    val fechaRegistro: Long = System.currentTimeMillis(),
    val fechaNacimiento: String = "",
    val sustanciasJson: String = "" // JSON list de sustancias activas
)
```

**DAO (`UserDao`):**
- `upsert(user)` — `INSERT OR REPLACE`
- `observar()` — `Flow<UserEntity?>` filtrando `id = 1`

---

#### `contactos_sos`

```kotlin
@Entity(tableName = "contactos_sos")
data class ContactoSosEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String = "",
    val telefono: String = "",
    val orden: Int = 0              // posición en la lista (0-based)
)
```

**DAO (`ContactoSosDao`):**
- `upsertAll(contactos)` — reemplaza la lista completa
- `deleteAll()` — borra antes del upsert
- `observar()` — `Flow<List<ContactoSosEntity>>` orden ASC por `orden`

---

#### `resultados_assist`

```kotlin
@Entity(tableName = "resultados_assist")
data class ResultadoAssistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sustanciaId: String,        // "alcohol" | "vape" | "cristal" | "cigarro"
    val puntaje: Int,
    val nivel: String,              // "BAJO" | "MODERADO" | "ALTO"
    val recomendacion: String,
    val fecha: Long = System.currentTimeMillis()
)
```

**DAO (`ResultadoAssistDao`):**
- `insertar(resultado)` — `INSERT OR REPLACE`
- `observar()` — `Flow<List<ResultadoAssistEntity>>` orden DESC por `fecha`

---

#### `bitacora`

```kotlin
@Entity(tableName = "bitacora")
data class BitacoraEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: Long,                // epoch ms de la fecha del registro
    val estadoAnimo: String,        // "Triste" | "Ansioso" | "Neutral" | "Bien" | "Eufórico"
    val consumio: Boolean,
    val sustancia: String? = null,  // null si consumio = false
    val nota: String? = null        // texto libre, máx 100 chars
)
```

**DAO (`BitacoraDao`):**
- `insertar(entry)` — `INSERT OR REPLACE`
- `observar()` — `Flow<List<BitacoraEntity>>` orden DESC
- `observarFechas()` — `Flow<List<Long>>` solo fechas (para cómputo de racha)

---

#### `plan`

```kotlin
@Entity(tableName = "plan")
data class PlanEntity(
    @PrimaryKey val id: Int = 1,    // siempre ID=1 (plan único)
    val metaIndex: Int = 0,         // índice en la lista de metas rotatorias
    val metaLogradaHoy: Boolean = false,
    val fecha: Long = System.currentTimeMillis()
)
```

**DAO (`PlanDao`):** upsert + observe

---

#### `logros`

```kotlin
@Entity(tableName = "logros")
data class LogroEntity(
    @PrimaryKey val id: String,     // "racha_3" | "racha_7" | "racha_10" | "racha_15" | "racha_30"
    val unlocked: Boolean = false,
    val fechaUnlock: Long? = null
)
```

**Seed automático** en `AppDatabase.SEED_CALLBACK.onCreate`: inserta los 5 logros con `unlocked = false`.

**DAO (`LogroDao`):**
- `insertarTodos(logros)` — `INSERT OR IGNORE` (no sobreescribe)
- `actualizar(logro)` — `UPDATE` al desbloquear
- `observar()` — `Flow<List<LogroEntity>>` orden ASC por `id`

---

#### `sos_events`

```kotlin
@Entity(tableName = "sos_events")
data class SosEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: Long = System.currentTimeMillis(),
    val telefonosEnviados: String = "" // separados por "|||"
)
```

**DAO (`SosEventDao`):**
- `insertar(event)` — `INSERT`
- `observar()` — `Flow<List<SosEventEntity>>` orden DESC

---

## 6. Repositorios

| Repositorio | DAOs que usa | Responsabilidad |
|---|---|---|
| `UserRepository` | `UserDao` | CRUD del perfil de usuario único |
| `ContactoSosRepository` | `ContactoSosDao` | Guardar/observar contactos SOS (delete-all + upsert) |
| `AssistRepository` | `ResultadoAssistDao` | Insertar y observar resultados ASSIST |
| `BitacoraRepository` | `BitacoraDao` | Insertar y observar registros de bitácora |
| `AvancesRepository` | `BitacoraDao` + `LogroDao` | Observar bitácora, desbloquear logros |
| `PlanRepository` | `PlanDao` | Persistir estado de la meta diaria |
| `SosRepository` | `ContactoSosDao` + `SosEventDao` | Observar contactos + registrar evento SOS |
| `DiagnosticoRepository` | `ResultadoAssistDao` | (legacy, coexiste con AssistRepository) |
| `DecisionTreeRepository` | — (in-memory) | Provee los 8 árboles de decisión en memoria |

---

## 7. ViewModels

| ViewModel | Módulo | Repositorios / Deps |
|---|---|---|
| `SplashViewModel` | Splash | DataStore |
| `OnboardingViewModel` | Onboarding | DataStore |
| `LoginViewModel` | Auth | — (UI-only) |
| `RegisterViewModel` | Auth | — (UI-only) |
| `ForgotPasswordViewModel` | Auth | — (UI-only) |
| `DiagnosticoViewModel` | Diagnóstico | `DiagnosticoRepository` / `AssistRepository` |
| `InicioViewModel` | Home | — (mock data) |
| `RegistroViewModel` | Bitácora | `BitacoraRepository` |
| `ChatViewModel` | Chatbot | `DecisionTreeRepository`, `Context` (TTS) |
| `EjercicioGuiadoViewModel` | Guías/Pánico | `Context` (TTS) |
| `RedApoyoViewModel` | Red de Apoyo | `ContactoSosRepository` |
| `PlanViewModel` | Mi Plan | `PlanRepository` |
| `AvancesViewModel` | Mis Avances | `AvancesRepository` |
| `DirectorioViewModel` | Directorio | — (datos hardcodeados) |
| `PerfilViewModel` | Mi Perfil | `UserRepository`, `AssistRepository` |
| `SosViewModel` | SOS Overlay | `SosRepository` |
| `DecisionTreeViewModel` | Árbol UI (standalone) | `DecisionTreeRepository` |

---

## 8. Árboles de Decisión (Berto)

### Modelo de datos

```kotlin
data class DecisionTree(
    val id: String,
    val nombre: String,
    val nodoInicialId: String,
    val nodos: Map<String, DecisionNode>
)

data class DecisionNode(
    val id: String,
    val texto: String,              // mensaje principal
    val tipo: NodeType,
    val opciones: List<DecisionOption> = emptyList(),
    val mensaje: String? = null,    // texto extra breve
    val porQue: String? = null,     // explicación profunda
    val recomendaciones: List<String> = emptyList(),
    val bertoState: String = "TRANQUILO",
    val delayMs: Long = 1000L,
    val esFinal: Boolean = false
)

data class DecisionOption(
    val texto: String,              // label del quick reply
    val siguienteNodoId: String     // ID del siguiente nodo o del árbol a cargar
)
```

### Árboles disponibles (8)

| ID | Sustancia | Tipo | Archivo |
|---|---|---|---|
| `alcohol_craving` | Alcohol | Manejo de ansiedad/craving | `AlcoholCravingTree.kt` |
| `alcohol_info` | Alcohol | Información y daños | `AlcoholInfoTree.kt` |
| `cristal_craving` | Cristal | Manejo de ansiedad/craving | `CristalCravingTree.kt` |
| `cristal_info` | Cristal | Información y daños | `CristalInfoTree.kt` |
| `vape_craving` | Vape | Manejo de ansiedad/craving | `VapeCravingTree.kt` |
| `vape_info` | Vape | Información y daños | `VapeInfoTree.kt` |
| `cigarro_craving` | Cigarro/Tabaco | Manejo de ansiedad/craving | `CigarroCravingTree.kt` |
| `cigarro_info` | Cigarro/Tabaco | Información y daños | `CigarroInfoTree.kt` |

### Menú principal virtual

Al iniciar Berto se construye un `DecisionTree` en memoria con ID `menu_principal_virtual` que presenta las 8 opciones como quick replies. Al seleccionar una, `ChatViewModel` carga el árbol real del repositorio.

---

## 9. Navegación

### Routes.kt — Rutas top-level

```kotlin
object Routes {
    const val SPLASH           = "splash"
    const val ONBOARDING       = "onboarding"
    const val AUTH_CHOICE      = "auth_choice"
    const val LOGIN            = "login"
    const val FORGOT_PASSWORD  = "forgot_password"
    const val REGISTER         = "register"
    const val HOME             = "home"             // envuelve MainScreen
    const val CHAT             = "chat"             // soporta ?source=drawer
    const val DIAGNOSTICO      = "diagnostico"
    const val RED_APOYO_SETUP  = "red_apoyo_setup"
    const val SOS_OVERLAY      = "sos_overlay"
    const val EJERCICIO_GUIADO = "ejercicio_guiado"
}
```

### Flujo principal

```
SPLASH
  └─ onboarding_done?
       NO → ONBOARDING → AUTH_CHOICE
       SÍ → AUTH_CHOICE
              ↓
         LOGIN / REGISTER
              ↓ (Register exitoso)
         DIAGNOSTICO
              ↓ (Continuar desde resultado)
         RED_APOYO_SETUP
              ↓
           HOME (MainScreen)
```

### Rutas internas de MainScreen (NavigationItem)

```
Inicio · Plan · RegistroEmocional · Avances
GuiasPrimerosAuxilios · RedApoyo · Directorio · MiPerfil
(Berto y CerrarSesion navegan a rutas top-level)
```

### Sub-NavGraphs

| NavGraph | Rutas internas |
|---|---|
| `DiagnosticoNavGraph` | selection → questions → result → history |
| `GuiasNavGraph` | guiasHub → crisisId / panic → ejercicioGuiado / craving / overuse / crisis |
| `PlanNavGraph` | planHub → manejo_craving / info_sustancia |
| `PerfilNavGraph` | perfil_main → perfil_privacidad / perfil_acerca / perfil_terminos |

---

## 10. Sistema de Diseño

### Paleta de colores

| Token | Hex | Uso principal |
|---|---|---|
| `TealPrimary` | `#1D9E75` | Botones primarios, headers, elementos activos |
| `TealMedium` | `#5DCAA5` | Textos secundarios, ejes de gráficas |
| `TealLight` | `#9FE1CB` | Bordes suaves, placeholders |
| `TealLightest` | `#E1F5EE` | Fondos de cards, chips activos, guías de gráfica |
| `TealDark` | `#085041` | Texto principal, outlines de Berto |
| `BackgroundApp` | `#F8F6F1` | Fondo general de la app |
| `CrisisRed` | `#E24B4A` | SOS, emergencias, botón "Sí consumí" |
| `CrisisRedLight` | `#fde8e8` | Fondo secciones riesgo ALTO |
| `WarnAmber` | `#d97706` | Nivel MODERADO, advertencias |
| `WarnAmberLight` | `#fef9c3` | Fondo nivel MODERADO |
| `TextMuted` | `#888780` | Textos deshabilitados / secundarios |
| `White` | `#FFFFFF` | — |

**Visores de Berto:**

| Estado | Color fondo |
|---|---|
| Calm | `#E1F5EE` |
| Celebrating | `#d4f7e0` |
| Worried | `#fef9c3` |
| Crisis | `#fde8e8` |

### Tipografía (Nunito)

| Estilo | Tamaño | Peso | Uso |
|---|---|---|---|
| `displayLarge` | 56sp | Black | Números grandes, racha |
| `headlineLarge` | 28sp | Bold | Títulos de sección |
| `headlineMedium` | 22sp | Bold | Subtítulos |
| `headlineSmall` | 18sp | Bold | Cabeceras de cards |
| `titleLarge` | 16sp | SemiBold | Top bars |
| `titleMedium` | 14sp | SemiBold | Items de lista |
| `titleSmall` | 13sp | SemiBold | Labels de sección |
| `bodyLarge` | 15sp | Normal | Texto de contenido |
| `bodyMedium` | 13sp | Normal | Cuerpo secundario |
| `bodySmall` | 12sp | Normal | Notas, meta-info |
| `labelLarge` | 14sp | ExtraBold | Botones |
| `labelMedium` | 12sp | ExtraBold | Tags, chips |
| `labelSmall` | 10sp | ExtraBold | Etiquetas mínimas |

### Shapes (border radius)

| Uso | Radio |
|---|---|
| Pills / botones / chips | 50dp |
| Modales / Drawers / Dialogs | 28dp |
| Cards principales | 20dp |
| Cards estándar / herramientas | 14–16dp |
| Badges / etiquetas | 10–12dp |

### Iconografía

Aproximadamente 80+ SVG propios en `res/drawable/`.

**Berto:** `berto_saludando` · `berto_preocupado` · `berto_tranquilo` · `berto_feliz` · `berto_sentado_mirando_izquierda` · `berto_mira_mariposa` · `berto_cabeza` · `berto_sin_internet`

**Sustancias:** `ic_bottle` (alcohol) · `ic_gem` (cristal) · `ic_vape` · `ic_cigarette` (tabaco)

**Emociones:** `ic_face_anxious` · `ic_face_happy` · `ic_face_neutral` · `ic_face_sad` · `ic_face_tired` · `ic_face_euphoric`

**UI General:** `ic_home` · `ic_menu` · `ic_settings` · `ic_chat` · `ic_bell` · `ic_calendar` · `ic_heart` · `ic_brain` · `ic_mic` · `ic_send` · `ic_shield` · `ic_sos` · `ic_phone` · `ic_plan` · `ic_trophy` · `ic_flame` · `ic_flag` · `ic_building` · `ic_people` · `ic_trending_up` · `ic_share` · `ic_history` · `ic_save` · `ic_lock` · `ic_wind` · `ic_alert_triangle` · `ic_check` · `ic_check_circle` · `ic_chevron_right` · `ic_chevron_down` · `ic_info` · `ic_activity` · `ic_alert_circle` · `ic_circle_x` · `ic_pencil` · `ic_clipboard` · `ic_refresh`

---

## 11. Componentes Compartidos

### Componentes comunes (`ui/components/common/`)

| Componente | Descripción |
|---|---|
| `SolvyxButton` | Botón primario filled, 56dp altura, 28dp radius |
| `SolvyxOutlinedButton` | Variante outlined, borde 1.5dp, acepta `borderColor: Color?` y `textColor: Color?` opcionales |
| `SolvyxTextButton` | Solo texto, sin borde ni relleno |
| `SolvyxTextField` | `OutlinedTextField` con ícono leading, soporte para modo contraseña |
| `SolvyxBackButton` | `IconButton` con flecha atrás |
| `SolvyxStubTopBar` | TopBar genérico reutilizable |
| `PageIndicator` | Dots animados de paginación (8dp → 28dp activo) |

### Drawer (`ui/components/drawer/`)

| Componente | Descripción |
|---|---|
| `CustomDrawer` | Drawer 60% ancho, 3 secciones, animación scale + offset |
| `NavigationItemView` | Item individual del drawer |
| `CustomDrawerState` | Enum: `Opened` / `Closed` |
| `NavigationItem` | Sealed class con todos los destinos del drawer |

### Navegación (`ui/components/navigation/`)

| Componente | Descripción |
|---|---|
| `SolvyxBottomNavigationBar` | 4 tabs + Berto elevado en centro + SOS flotante top-right |

### Dialogs (`ui/components/dialog/`)

| Componente | Descripción |
|---|---|
| `SosConfirmationDialog` | Dialog de confirmación antes de abrir SOS Overlay |

### Componentes de Guías (`ui/screens/guias/components/`)

| Componente | Descripción |
|---|---|
| `GuiaTopBar` | Top bar para guías (back o menú hamburguesa) |
| `HeroSideBerto` | Sección hero con Berto lateral + fondo TealPrimary |
| `GuiaPanel` | Panel blanco scrollable con overlap de -24dp sobre el hero |
| `BorderCard` | Card con borde izquierdo coloreado |
| `CardLabel` | Label de sección con ícono |
| `DotRow` | Punto de lista con dot de color |
| `StepRow` | Paso numerado con badge círculo |
| `HelpLineRow` | Fila clickable de número de ayuda |

---

## 12. Dependencias

### Producción

| Librería | Versión | Uso |
|---|---|---|
| Jetpack Compose BOM | 2025.08.00 | UI framework |
| Material 3 | 1.3.2 | Design system |
| Navigation Compose | 2.9.3 | Navegación declarativa |
| Hilt Android | 2.51.1 | Inyección de dependencias |
| Hilt Navigation Compose | 1.2.0 | `hiltViewModel()` en Composables |
| Room Runtime + KTX | 2.6.1 | Base de datos local SQLite |
| DataStore Preferences | 1.0.0 | Persistencia de onboarding flag |
| Retrofit | 2.9.0 | HTTP client (pendiente de uso) |
| OkHttp | 4.12.0 | HTTP logging/interceptors (pendiente) |
| Gson | 2.10.1 | Serialización JSON |
| Kotlin Coroutines | 1.7.3 | Async / Flows |
| Lifecycle Runtime Compose | 2.7.0 | `collectAsStateWithLifecycle` |
| Lifecycle ViewModel KTX | 2.8.0 | `viewModelScope` |
| Accompanist Pager | 0.36.0 | Paginación horizontal (Onboarding) |
| Lottie Compose | 6.6.7 | Animaciones Lottie |
| Haze | 0.4.1 | Efecto blur decorativo en drawer |
| KSP | 2.0.21-1.0.28 | Procesamiento de anotaciones (Hilt, Room) |

### Testing

| Librería | Uso |
|---|---|
| JUnit 4.13.2 | Unit tests |
| Kotlinx Coroutines Test 1.7.3 | Testing de coroutines/flows |
| Espresso Core 3.7.0 | UI tests |

---

## 13. Estado Actual y Pendientes

### Completado

- UI completa de todos los módulos (Auth, Onboarding, Diagnóstico, Home, Bitácora, Chatbot, Guías, Red de Apoyo, Plan, Avances, Directorio, Perfil, SOS)
- Room DB con 7 tablas operativas
- Bitácora con persistencia real (Room)
- Red de Apoyo con persistencia real (Room)
- Resultados ASSIST con persistencia real (Room)
- Árboles de decisión (8 árboles completos en memoria)
- TTS en Berto y EjercicioGuiado (voz femenina española)
- SpeechRecognizer (es-MX) en Berto
- Logros con seed automático y desbloqueo por racha

### Pendiente

| Área | Estado | Notas |
|---|---|---|
| Firebase Auth | Pendiente | Retrofit + OkHttp instalados, sin implementar |
| Firebase Firestore | Pendiente | Sin `google-services.json` en el repo |
| Persistencia Home/InicioViewModel | Mock | Nombre "Alex", racha 5 días hardcodeados |
| IA real en Berto | Mock | Respuestas basadas en árboles de decisión, sin LLM |
| Plan de Reducción | Placeholder | `ManejoCravingScreen` e `InfoSustanciaScreen` son stubs |
| HistorialBitácora | Real | Lee Room, pero algunos stats son parcialmente mock |
| Diseño final Árboles de Decisión | Pendiente | `TreesSelectionScreen` + `TreePlayScreen` sin conectar al nav |
| `ConfiguracionScreen` | Obsoleto | Pendiente eliminar del proyecto |

### Líneas de ayuda (constantes)

| Servicio | Número |
|---|---|
| Línea de la Vida (CONADIC) | **800 911 2000** |
| SAPTEL | 55 5259 8121 |
| CIJ | Según entradas del Directorio |

---

*Última actualización: Junio 2026*
