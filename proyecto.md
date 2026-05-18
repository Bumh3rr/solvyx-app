# Solvyx — Documentación del Proyecto

> Aplicación Android de apoyo para personas con consumo de sustancias. Construida en Kotlin + Jetpack Compose con arquitectura MVVM y Hilt.

---

## Tabla de Contenidos

1. [Visión General](#visión-general)
2. [Arquitectura](#arquitectura)
3. [Flujo de la Aplicación](#flujo-de-la-aplicación)
4. [Módulos](#módulos)
5. [Vistas Detalladas](#vistas-detalladas)
6. [Sistema de Diseño](#sistema-de-diseño)
7. [Modelos de Datos](#modelos-de-datos)
8. [Navegación](#navegación)
9. [Componentes Compartidos](#componentes-compartidos)

---

## Visión General

| Atributo | Valor |
|---|---|
| Package | `com.solvyx` |
| minSdk | 24 (Android 7.0) |
| targetSdk | 36 |
| Lenguaje | Kotlin |
| UI Framework | Jetpack Compose + Material 3 |
| Arquitectura | MVVM + Hilt DI |
| Tipografía | Nunito (familia completa) |
| Permisos | `RECORD_AUDIO` |
| DB Local | Room |

**Mascota:** Berto — robot emocional con 5 estados visuales (saludando, preocupado, tranquilo, feliz, sentado).

---

## Arquitectura

```
com.solvyx/
├── backend/
│   ├── data/local/
│   │   ├── dao/          → ResultadoDao
│   │   ├── database/     → AppDatabase, DatabaseProvider
│   │   └── entity/       → ResultadoEntity
│   ├── models/           → Pregunta, Sustancia, ResultadoDiagnostico, NivelRiesgo
│   ├── presentation/viewmodel/ → DiagnosticoViewModel
│   └── repository/       → DiagnosticoRepository
├── di/
│   └── AppModule.kt      → Hilt module (DB, DAO, Repository)
└── ui/
    ├── components/
    │   ├── common/        → SolvyxButton, TextField, BackButton, TopBar, PageIndicator
    │   ├── drawer/        → CustomDrawer, NavigationItem, CustomDrawerState
    │   └── SolvyxBottomNavigationBar, SosConfirmationDialog
    ├── diagnostico/       → DiagnosticoNavGraph + 4 pantallas
    ├── navigation/        → Routes.kt, NavGraph.kt
    ├── screens/           → 15+ módulos de pantallas
    └── theme/             → Color.kt, Type.kt, Theme.kt
```

**Flujo de datos:** UI → ViewModel → Repository → DAO → Room DB  
**Inyección:** Hilt con `@HiltViewModel` y `@Singleton`  
**Persistencia liviana:** DataStore Preferences (onboarding_done)

---

## Flujo de la Aplicación

```
[SPLASH]
    ↓ (1.5s)
¿Onboarding completo?
    NO → [ONBOARDING] → [AUTH_CHOICE]
    SÍ → [AUTH_CHOICE]
         ↓
    ┌────┴────┐
[LOGIN]  [REGISTER]
    │         ↓
    │    [DIAGNOSTICO ASSIST]
    │         ↓
    │    [RED DE APOYO SETUP]
    └────────↓
         [HOME / MAIN]
              ↓
    ┌─────────┴──────────┐
[Bottom Nav]         [Drawer]
 Inicio | Registro | Berto   Plan | Guías | Red | Config | Logout
```

### Flujo de Diagnóstico ASSIST (post-registro)

```
[Selección de Sustancia]
    ↓ (sustancia elegida)
[Cuestionario ASSIST] (6-7 preguntas por sustancia)
    ↓ (respuestas evaluadas)
[Resultado] → NivelRiesgo: BAJO / MODERADO / ALTO
    ↓
[Historial] (opcional)
    ↓
[Red de Apoyo Setup] → [HOME]
```

### Flujo de Guías de Primeros Auxilios

```
[Guías Hub]
    ├── Cómo sé si estoy en crisis   → [GuiaCrisisId]
    ├── Ansiedad y ataque de pánico  → [GuiaPánico] → [Ejercicio 5-4-3-2-1]
    ├── Craving muy intenso          → [GuiaCraving]
    ├── Consumí de más               → [GuiaConsumiDeMás] (tabs por sustancia)
    └── Estoy en crisis ahora mismo  → [GuiaEstoyEnCrisis]
```

### Flujo de Bitácora

```
[RegistroEmocional]
    ├── Seleccionar fecha (CalendarBottomSheet)
    ├── Estado de ánimo (5 emociones + nota)
    ├── ¿Consumiste? → SustanciaBottomSheet
    ├── Nivel de ansiedad (slider 1-10)
    └── Guardar → [Dialog Exitoso]
         ↓
[HistorialBitácora] → Tarjetas cronológicas
```

---

## Módulos

### 1. Auth
**Ubicación:** `screens/auth/`  
**Propósito:** Registro, login y recuperación de contraseña.  
**Pantallas:** AuthChoice → Login / Register → ForgotPassword  
**Estado:** UI completa, backend pendiente de implementar

### 2. Onboarding
**Ubicación:** `screens/onboarding/`  
**Propósito:** Presentación de 4 páginas con Berto animado.  
**Estado:** DataStore marca `onboarding_done` al completar

### 3. Diagnóstico ASSIST
**Ubicación:** `ui/diagnostico/`  
**Propósito:** Evaluación de riesgo de consumo (herramienta OMS ASSIST).  
**Sustancias:** Alcohol, Cigarro, Vape, Cristal  
**Scoring:** BAJO (0-10) / MODERADO (11-26) / ALTO (27+)  
**Persistencia:** Room DB (tabla `resultados`)

### 4. Home / Inicio
**Ubicación:** `screens/home/`  
**Propósito:** Dashboard principal con racha, meta del día, emociones y accesos rápidos.  
**Estado:** Datos mock (Alex, 5 días de racha)

### 5. Bitácora
**Ubicación:** `screens/bitacora/`  
**Propósito:** Registro diario de estado emocional y consumo.  
**Persistencia:** RegistroViewModel (sin persistencia real aún)

### 6. Berto (Chatbot)
**Ubicación:** `screens/chatbot/`  
**Propósito:** Asistente conversacional con 4 estados emocionales.  
**Features:** Voz (SpeechRecognizer), detección de keywords, TTS simulado  
**Estado:** Respuestas simuladas, sin IA real

### 7. Guías de Primeros Auxilios
**Ubicación:** `screens/guias/`  
**Propósito:** 5 guías de crisis y reducción de daños.  
**Feature especial:** Ejercicio 5-4-3-2-1 con TTS en español

### 8. Red de Apoyo
**Ubicación:** `screens/red/`  
**Propósito:** Gestión de contactos SOS (máx. 3 contactos).  
**Modos:** Setup (post-registro) / Edición (desde drawer)

### 9. Plan de Reducción
**Ubicación:** `screens/plan/`  
**Estado:** Placeholder (sin implementar)

### 10. Configuración
**Ubicación:** `screens/configuracion/`  
**Estado:** Placeholder (sin implementar)

---

## Vistas Detalladas

---

### SplashScreen

**Archivo:** `screens/splash/SplashScreen.kt`  
**ViewModel:** `SplashViewModel`

| Atributo | Detalle |
|---|---|
| Fondo | BackgroundApp (crema) |
| Elementos | Texto "Solvyx" centrado, displayLarge |
| Duración | 1500ms automático |
| Lógica | Lee DataStore → navega a Onboarding o AuthChoice |

---

### OnboardingScreen

**Archivo:** `screens/onboarding/OnboardingScreen.kt`  
**ViewModel:** `OnboardingViewModel`

| Atributo | Detalle |
|---|---|
| Páginas | 4 |
| Animaciones Berto | FLOAT, BOUNCE, PULSE, WAVE |
| Indicador | Dots animados (8dp → 28dp activo) |
| Botones | "Siguiente" / "Comenzar ahora" + "Saltar" |

**Páginas:**

| # | Título | Estado Berto | Fondo |
|---|---|---|---|
| 1 | Tu espacio seguro | WAVE | TealPrimary |
| 2 | Diagnóstico ASSIST · OMS | PULSE | TealDark |
| 3 | Botón SOS | BOUNCE | TealPrimary |
| 4 | 100% Privado · Sin internet | FLOAT | TealDark |

---

### AuthChoiceScreen

**Archivo:** `screens/auth/choice/AuthChoiceScreen.kt`

| Atributo | Detalle |
|---|---|
| Hero | 62% altura, fondo TealPrimary, Berto con halos (230dp) |
| Botones | "Iniciar Sesión" (filled) + "Crear cuenta" (outlined) |
| Footer | Términos y Privacidad (texto anotado con links) |

---

### LoginScreen

**Archivo:** `screens/auth/login/LoginScreen.kt`  
**ViewModel:** `LoginViewModel`

| Campo | Tipo | Icono |
|---|---|---|
| Email | SolvyxTextField, teclado email | ic_email |
| Contraseña | SolvyxTextField, enmascarado | ic_lock |

**Elementos adicionales:**
- Berto asomándose (90dp, offset y=-25dp)
- Link "¿Olvidaste tu contraseña?" → ForgotPassword
- Link "¿No tienes cuenta?" → Register
- Copyright "2026 Solvyx ®"

---

### RegisterScreen

**Archivo:** `screens/auth/register/RegisterScreen.kt`  
**ViewModel:** `RegisterViewModel`

| Campo | Tipo | Icono |
|---|---|---|
| Apodo | SolvyxTextField | ic_person |
| Email | SolvyxTextField, email | ic_email |
| Fecha de nacimiento | SolvyxTextField | ic_birthday |
| Contraseña | SolvyxTextField, enmascarado | ic_lock |
| Confirmar contraseña | SolvyxTextField, enmascarado | ic_lock |
| Términos | Checkbox con links anotados | — |

**Navegación al éxito:** → DIAGNOSTICO (popUpTo AUTH_CHOICE)

---

### ForgotPasswordScreen

**Archivo:** `screens/auth/forgot_password/ForgotPasswordScreen.kt`  
**ViewModel:** `ForgotPasswordViewModel`

| Atributo | Detalle |
|---|---|
| Hero | 52% altura, TealDark, Berto preocupado con halos |
| Campo | Email |
| Botón | "Enviar enlace de recuperación" (ic_send) |
| Secundario | "Volver a iniciar sesión" |

---

### MainScreen

**Archivo:** `screens/main/MainScreen.kt`

Pantalla raíz del área autenticada. No tiene contenido propio — es el contenedor.

| Atributo | Detalle |
|---|---|
| Drawer | CustomDrawer (63% ancho), animación scale + offset |
| Bottom Nav | 3 tabs + botón SOS flotante |
| Fondo | Degradado TealPrimary → TealDark |
| Efecto | Blur 20dp en imagen hero decorativa |
| Rutas internas | Inicio, Plan, Registro, Guías, Red, Config |

**Items del Drawer:**

| Item | Icono | Ruta |
|---|---|---|
| Inicio | ic_home | InicioScreen |
| Mi Plan | ic_plan | PlanReduccionScreen |
| Registro | ic_calendar | RegistroEmocionalScreen |
| Primeros Auxilios | ic_guide | GuiasNavGraph |
| Red de Apoyo | ic_people | RedApoyoScreen |
| Configuración | ic_settings | ConfiguracionScreen |
| Berto | ic_chat | BertoScreen |
| Cerrar sesión | ic_logout | → AUTH_CHOICE |

---

### InicioScreen (Home)

**Archivo:** `screens/home/InicioScreen.kt`

| Sección | Detalle |
|---|---|
| Top Bar | Menú + "Solvyx" italic + campana con badge |
| Hero | Berto saludando (130dp), "Hola, Alex", fecha actual |
| Racha | "5 días de racha" chip (ic_flame_2, rojo) |
| Meta del Día | Card primary: objetivo semanal, 7 dots de progreso, "Logrado" |
| Emociones | 5 opciones: Triste, Ansioso, Neutral, Bien, Eufórico |
| Accesos rápidos | Grid 2x2: Mi Plan / Hablar con Berto / Primeros Auxilios / Mi Registro |
| Actividad reciente | Card con 3 entradas mock |

**Estado local:** `emocionSeleccionada: String?`

---

### RegistroEmocionalScreen (Bitácora)

**Archivo:** `screens/bitacora/RegistroEmocionalScreen.kt`  
**ViewModel:** `RegistroViewModel`

| Campo | Tipo | Valores |
|---|---|---|
| Fecha | CalendarBottomSheet | LocalDate, solo pasado |
| Estado de ánimo | 5 íconos seleccionables (52dp) | Triste, Ansioso, Neutral, Bien, Eufórico |
| Nota | TextArea, máx 100 chars | Texto libre |
| ¿Consumiste? | Dos botones | No / Sí → SustanciaBottomSheet |
| Sustancia | SustanciaBottomSheet | Alcohol, Cristal, Vape, Tabaco |
| Ansiedad | Slider 1-10, haptic por paso | 1-10, codificado por color |
| Guardar | SolvyxButton sticky | → Dialog éxito |

**Dialog de éxito:** Berto feliz + checkmark + resumen de registro

---

### HistorialBitacoraScreen

**Archivo:** `screens/bitacora/HistorialBitacoraScreen.kt`

| Sección | Detalle |
|---|---|
| Stats | 3 columnas: N° registros, días sin consumo, ansiedad media |
| Lista | LazyColumn de tarjetas cronológicas |
| Tarjeta | Fecha, badge consumo, emoción (40dp círculo), nota truncada, nivel ansiedad |

---

### BertoScreen (Chatbot)

**Archivo:** `screens/chatbot/BertoScreen.kt`  
**ViewModel:** `ChatViewModel`

| Sección | Detalle |
|---|---|
| Top Bar | Avatar Berto (color por estado), "En línea · Privado", dot verde pulsando, menú 3 puntos |
| Mensajes | LazyColumn, burbujas usuario (primary) / Berto (surface) |
| Typing | Berto peek zone: ilustración + "Berto está escribiendo..." + 3 dots |
| Quick Replies | Chips bajo último mensaje de Berto |
| Input | SOS (rojo) + TextField + Micrófono + Enviar |
| Capabilities | BottomSheet con 4 capacidades |

**Estados de Berto:**

| Estado | Color visor | Trigger |
|---|---|---|
| TRANQUILO | BertoVisorCalm (verde claro) | Default |
| PREOCUPADO | BertoVisorWorried (amarillo) | Keywords ansiedad |
| CELEBRANDO | BertoVisorCelebr (verde) | Keywords positivos |
| CRISIS | BertoVisorCrisis (rojo) | Keywords crisis |

**Features:** SpeechRecognizer (es-MX), detección keywords, delay simulado 1500ms

---

### GuiasHubScreen

**Archivo:** `screens/guias/screens/hub/GuiasHubScreen.kt`

| Card | Icono | Color | Ruta |
|---|---|---|---|
| Cómo sé si estoy en crisis | ic_info | Primary | crisisId |
| Ansiedad y ataque de pánico | ic_brain | Primary | panic |
| Craving muy intenso | ic_flame | Teal Medium | craving |
| Consumí de más | ic_alert_triangle | Naranja/Amarillo | overuse |
| Estoy en crisis ahora mismo | ic_sos | Rojo/Rosa | crisis |

**Footer:** Línea de la Vida (800 290 0024), SAPTEL (5552598121), CIJ

---

### GuiaCrisisIdScreen

**Archivo:** `screens/guias/screens/GuiaCrisisIdScreen.kt`

| Sección | Contenido |
|---|---|
| Hero | Berto preocupado lateral |
| Señales físicas | 6 síntomas (card primary) |
| Señales emocionales | 5 síntomas (card naranja) |
| Señales conductuales | 4 síntomas (card secondary) |
| Cuándo llamar al 911 | 5 señales críticas (card rojo) |
| CTA | "Avisar a mi red de apoyo" (rojo) |

---

### GuiaPanicoScreen + EjercicioGuiadoScreen

**Archivos:** `screens/guias/screens/panico/`  
**ViewModel:** `EjercicioGuiadoViewModel` (TTS)

**GuiaPánico:**
- "¿Pánico o emergencia cardíaca?" (BorderCard)
- 4 pasos inmediatos
- Botón → EjercicioGuiado

**EjercicioGuiado (5-4-3-2-1):**

| Paso | Sentido | Burbujas interactivas |
|---|---|---|
| 1 | Ver | 5 objetos |
| 2 | Tocar | 4 texturas |
| 3 | Escuchar | 3 sonidos |
| 4 | Oler | 2 aromas |
| 5 | Saborear | 1 sabor |

**TTS:** voz femenina español, pitch 1.15, velocidad 0.85  
**Animación:** círculos de respiración en fondo, arco de progreso Canvas

---

### GuiaCravingIntensoScreen

**Archivo:** `screens/guias/screens/GuiaCravingIntensoScreen.kt`

| Sección | Contenido |
|---|---|
| Hero | "Las ganas van a pasar." |
| ¿Qué es el craving? | Explicación (teal bg) |
| Plan de acción | 4 pasos numerados |
| Si decides consumir | 4 prácticas de reducción de daños (shields) |
| CTA | "Avisar a mi red de apoyo" (rojo outlined) |

---

### GuiaConsumiDeMasScreen

**Archivo:** `screens/guias/screens/GuiaConsumiDeMasScreen.kt`

**Tabs:** Alcohol / Cristal / Vape / Tabaco (contenido específico por sustancia)

| Sección | Contenido |
|---|---|
| Hero | Mensaje de auto-compasión, TealDark |
| Señales de alerta | Síntomas según sustancia (yellow bg) |
| Cuídate ahora | 5 pasos de cuidado (primary) |
| Cuándo llamar al 911 | 6 señales de emergencia (rojo) |
| CTA | Botón rojo prominente + Líneas de ayuda |

---

### GuiaEstoyEnCrisisScreen

**Archivo:** `screens/guias/screens/GuiaEstoyEnCrisisScreen.kt`

| Sección | Contenido |
|---|---|
| Hero | Speech bubble Berto: "Que estés leyendo esto ya es un acto de valentía..." |
| Haz esto ahora | 4 acciones inmediatas |
| CTA principal | "Avisar a mi red de apoyo ahora" (rojo, prominente) |
| Secundario | "¿Hablar con Berto?" (card clickable) |
| Apoyo | "Lo que sientes tiene nombre" (texto de sostén) |
| Footer | Líneas de ayuda |

---

### RedApoyoScreen

**Archivo:** `screens/red/RedApoyoScreen.kt`  
**ViewModel:** `RedApoyoViewModel`

**Dos modos:**

| Modo | Top Bar | CTA |
|---|---|---|
| Setup | Back + progreso "1 de 1" | "Guardar perfil y comenzar" |
| Regular | Drawer | "Guardar cambios" |

**Por contacto:**

| Campo | Tipo | Validación |
|---|---|---|
| Nombre | SolvyxTextField (50dp) | ≥ 2 chars |
| Teléfono | SolvyxTextField numérico | ≥ 7 dígitos |

**Límites:** 1 contacto obligatorio, hasta 3 totales  
**Done Overlay (solo setup):** Berto saludando + "¡Listo!" + "Empezar" → HOME

---

### DiagnosticoNavGraph

**Archivos:** `ui/diagnostico/`

#### SubstanceSelectionScreen
- Grid de tarjetas por sustancia (Alcohol, Cigarro, Vape, Cristal)
- Cada tarjeta: icono de sustancia, nombre, selección visual

#### QuestionsScreen
- 6-7 preguntas por sustancia (cuestionario ASSIST OMS)
- Progress bar animada
- Slide transitions entre preguntas
- Dot indicator de progreso

#### ResultScreen
- Puntaje final
- Badge de nivel: BAJO (verde) / MODERADO (naranja) / ALTO (rojo)
- Recomendación personalizada por nivel
- CTA → Historial o siguiente sustancia

#### HistoryScreen
- Lista de resultados pasados con timestamp
- Nombre de sustancia, puntaje, nivel de riesgo
- Ordenados por fecha DESC (Room Flow)

---

### PlanReduccionScreen

**Archivo:** `screens/plan/PlanReduccionScreen.kt`  
**Estado:** Placeholder (Box vacío)

---

### ConfiguracionScreen

**Archivo:** `screens/configuracion/ConfiguracionScreen.kt`  
**Estado:** Placeholder (Box vacío)

---

## Sistema de Diseño

### Paleta de Colores

| Token | Hex | Uso |
|---|---|---|
| TealPrimary | `#1D9E75` | Botones principales, headers, activos |
| TealMedium | `#5DCAA5` | Textos secundarios, bordes |
| TealLight | `#9FE1CB` | Bordes suaves, placeholders |
| TealLightest | `#E1F5EE` | Fondos de cards, chips activos |
| TealDark | `#085041` | Texto principal, outlines Berto |
| BackgroundApp | `#F8F6F1` | Fondo general de la app |
| CrisisRed | `#E24B4A` | SOS, emergencias, errores |
| CrisisRedLight | `#fde8e8` | Fondos de secciones de crisis |
| CrisisRedDark | `#991b1b` | Texto sobre fondos rojos |
| WarnAmber | `#fef9c3` | Advertencias de ansiedad |
| TextMuted | `#888780` | Textos secundarios / deshabilitados |
| CardBorder | `#D1FAE5` | Bordes de tarjetas |
| White | `#FFFFFF` | — |

**Estados de Berto (visor):**

| Estado | Color |
|---|---|
| Calm | `#E1F5EE` |
| Celebrating | `#d4f7e0` |
| Worried | `#fef9c3` |
| Crisis | `#fde8e8` |

---

### Tipografía (Nunito)

| Estilo | Tamaño | Peso | Uso |
|---|---|---|---|
| displayLarge | 56sp | Black | Números grandes, splash |
| headlineLarge | 28sp | Bold | Títulos de sección |
| headlineMedium | 22sp | Bold | Subtítulos |
| headlineSmall | 18sp | Bold | Cabeceras de cards |
| titleLarge | 16sp | SemiBold | Etiquetas de navegación |
| titleMedium | 14sp | SemiBold | Items de lista |
| titleSmall | 13sp | SemiBold | Badges, chips |
| bodyLarge | 15sp | Normal | Texto de contenido |
| bodyMedium | 13sp | Normal | Cuerpo secundario |
| bodySmall | 12sp | Normal | Notas, meta-info |
| labelLarge | 14sp | ExtraBold | Botones |
| labelMedium | 12sp | ExtraBold | Tags |
| labelSmall | 10sp | ExtraBold | Etiquetas mínimas |

---

### Shapes

| Uso | Radio |
|---|---|
| Pills / botones | 50dp |
| Modales / Drawers | 28dp |
| Cards principales | 20dp |
| Cards estándar | 14-16dp |
| Chips / badges | 10-12dp |

---

### Iconografía

78 íconos SVG propios en `res/drawable/`:

**Berto:** `berto_saludando`, `berto_preocupado`, `berto_tranquilo`, `berto_feliz`, `berto_sentado_mirando_izquierda`, `berto_mira_mariposa`, `berto_sin_internet`

**Sustancias:** `ic_bottle` (alcohol), `ic_gem` (cristal), `ic_vape`, `ic_cigarette`

**Emociones:** `ic_face_anxious`, `ic_face_happy`, `ic_face_neutral`, `ic_face_sad`, `ic_face_tired`, `ic_face_euphoric`

**UI General:** `ic_home`, `ic_menu`, `ic_settings`, `ic_chat`, `ic_bell`, `ic_calendar`, `ic_heart`, `ic_brain`, `ic_mic`, `ic_send`, `ic_shield`, `ic_sos`, `ic_phone`, etc.

---

## Modelos de Datos

```kotlin
// Sustancias disponibles
data class Sustancia(val id: String, val nombre: String)
val SUSTANCIAS = listOf("alcohol", "cigarro", "vape", "cristal")

// Pregunta del cuestionario ASSIST
data class Pregunta(val id: Int, val texto: String, val opciones: List<Opcion>)
data class Opcion(val texto: String, val puntaje: Int)

// Nivel de riesgo
enum class NivelRiesgo { BAJO, MODERADO, ALTO }
// BAJO: 0-10 / MODERADO: 11-26 / ALTO: 27+

// Resultado de diagnóstico
data class ResultadoDiagnostico(
    val sustanciaId: String,
    val puntaje: Int,
    val nivel: NivelRiesgo,
    val recomendacion: String
)

// Entidad de base de datos
@Entity(tableName = "resultados")
data class ResultadoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sustanciaId: String,
    val puntaje: Int,
    val nivel: String,
    val recomendacion: String,
    val fecha: Long = System.currentTimeMillis()
)

// Contacto SOS
data class ContactoSOS(val nombre: String, val telefono: String)
```

---

## Navegación

### Routes.kt

```kotlin
object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val AUTH_CHOICE = "auth_choice"
    const val LOGIN = "login"
    const val FORGOT_PASSWORD = "forgot_password"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CHAT = "chat"
    const val DIAGNOSTICO = "diagnostico"
    const val RED_APOYO_SETUP = "red_apoyo_setup"
}
```

### Rutas internas (MainScreen)

```
inicio / planReduccion / registroEmocional /
guiasPrimerosAuxilios / redApoyo / configuracion
```

### Rutas de Guías (GuiasNavGraph)

```
guiasHub / crisisId / panic / craving / overuse / crisis
```

---

## Componentes Compartidos

| Componente | Descripción |
|---|---|
| `SolvyxButton` | Botón primario, 56dp altura, 28dp radius |
| `SolvyxOutlinedButton` | Variante outlined, borde 1.5dp |
| `SolvyxTextButton` | Solo texto |
| `SolvyxTextField` | OutlinedTextField con icono leading, soporte password |
| `SolvyxBackButton` | IconButton con flecha atrás |
| `SolvyxStubTopBar` | TopBar genérico |
| `PageIndicator` | Dots animados de paginación |
| `CustomDrawer` | Drawer personalizado 63% ancho |
| `SolvyxBottomNavigationBar` | 3 tabs + SOS flotante |
| `SosConfirmationDialog` | Dialog de confirmación de SOS |
| `GuiaTopBar` | Top bar para guías (back/menu) |
| `HeroSideBerto` | Hero section con Berto lateral |
| `GuiaPanel` | Panel blanco scrollable (-24dp overlap) |
| `BorderCard` | Card con borde izquierdo coloreado |
| `DotRow` | Punto de lista con dot de color |
| `StepRow` | Paso numerado con badge círculo |
| `HelpLineRow` | Fila clickable de número de ayuda |

---

## Dependencias Clave

| Librería | Uso |
|---|---|
| Jetpack Compose + Material3 | UI framework |
| Navigation Compose | Navegación declarativa |
| Hilt | Inyección de dependencias |
| Room | Base de datos local |
| DataStore Preferences | Persistencia liviana |
| Retrofit + OkHttp | HTTP (pendiente backend) |
| Coroutines + Lifecycle | Async + ViewModel |
| Accompanist Pager | Onboarding horizontal pager |
| Lottie Compose | Animaciones Lottie |
| Haze | Efecto blur |

---

*Última actualización: Mayo 2026*
