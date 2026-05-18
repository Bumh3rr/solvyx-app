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
| Permisos | `RECORD_AUDIO`, `SEND_SMS`, `INTERNET` |
| DB Local | Room |

**Mascota:** Berto — robot emocional con 7 estados visuales:
`berto_saludando`, `berto_preocupado`, `berto_tranquilo`,
`berto_feliz`, `berto_sentado_mirando_izquierda`,
`berto_mira_mariposa`, `berto_cabeza` (bottom nav).
Estado adicional offline: `berto_sin_internet`.

**Sustancias soportadas:** Alcohol · Vape · Cristal · Tabaco (NUNCA Cannabis)

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
    │   ├── common/        → SolvyxButton, SolvyxOutlinedButton, SolvyxTextField,
    │   │                    SolvyxBackButton, SolvyxStubTopBar, PageIndicator
    │   ├── dialog/        → SosConfirmationDialog
    │   ├── drawer/        → CustomDrawer, NavigationItemView,
    │   │                    model/{CustomDrawerState, NavigationItem}
    │   └── navigation/    → SolvyxBottomNavigationBar
    ├── decisiontree/      → TreesSelectionScreen, TreePlayScreen (no conectado a nav)
    ├── diagnostico/       → DiagnosticoNavGraph + 4 pantallas
    ├── navigation/        → Routes.kt, NavGraph.kt
    ├── screens/           → 14 módulos de pantallas
    └── theme/             → Color.kt, Type.kt, Theme.kt
```

**Flujo de datos:** UI → ViewModel → Repository → DAO → Room DB  
**Inyección:** Hilt con `@HiltViewModel` y `@Singleton`  
**Persistencia liviana:** DataStore Preferences (`onboarding_done`)

---

## Flujo de la Aplicación

```
[SPLASH]
    ↓ (1.5s, lee DataStore)
¿Onboarding completo?
    NO → [ONBOARDING] → [AUTH_CHOICE]
    SÍ → [AUTH_CHOICE]
         ↓
    ┌────┴────┐
[LOGIN]  [REGISTER]
             ↓
    [DIAGNOSTICO ASSIST]
             ↓
    [RED DE APOYO SETUP]
             ↓
         [HOME]
           ↓
  ┌────────┴──────────────┐
[Bottom Nav]           [Drawer]
Inicio|Plan|Berto|Avances  Rutina|Herramientas|Mi Cuenta
```

**Bottom Navigation Bar:**

| Posición | Tab | Icono |
|---|---|---|
| 1 | Inicio | ic_home |
| 2 | Plan | ic_plan |
| Centro (elevado) | Berto | berto_cabeza |
| 3 | Avances | ic_trophy |
| Flotante top-right | SOS | botón rojo |

**Drawer — secciones:**

| Sección | Items |
|---|---|
| Rutina | Inicio · Mi Plan · Registro diario · Mis Avances |
| Herramientas | Hablar con Berto · Guías de Primeros Auxilios · Directorio Profesional |
| Mi Cuenta | Mi Perfil · Cerrar sesión |
| Interno (sin entrada en drawer) | Red Apoyo |

---

### Flujo de Diagnóstico ASSIST

```
[Selección de Sustancia] (Alcohol / Vape / Cristal / Tabaco)
    ↓
[Cuestionario ASSIST] (6-7 preguntas por sustancia)
    ↓
[Resultado] → BAJO / MODERADO / ALTO + acciones sugeridas
    ↓ (botón Continuar)
[Red de Apoyo Setup] → [HOME]
    ↓ (o desde resultado: cualquier acción → HOME)
```

### Flujo de Guías de Primeros Auxilios

```
[Guías Hub]
    ├── Cómo sé si estoy en crisis   → GuiaCrisisId
    ├── Ansiedad y ataque de pánico  → GuiaPánico → EjercicioGuiado (5-4-3-2-1)
    ├── Craving muy intenso          → GuiaCravingIntenso
    ├── Consumí de más               → GuiaConsumiDeMás (tabs por sustancia)
    └── Estoy en crisis ahora mismo  → GuiaEstoyEnCrisis
```

### Flujo de Bitácora

```
[RegistroEmocional]
    ├── Seleccionar fecha (CalendarBottomSheet)
    ├── Estado de ánimo (5 emociones + nota libre)
    └── ¿Consumiste? → No / Sí → SustanciaBottomSheet
         ↓ Guardar
    [Dialog Exitoso: Berto + resumen Ánimo / Consumo]
         ↓ historial (icono top-right)
[HistorialBitácora]
```

---

## Módulos

### 1. Auth
**Ubicación:** `screens/auth/`  
**Pantallas:** AuthChoice → Login / Register → ForgotPassword  
**Estado:** UI completa, Firebase pendiente

### 2. Onboarding
**Ubicación:** `screens/auth/onboarding/`  
**Propósito:** 4 páginas con Berto animado.  
**Estado:** DataStore escribe `onboarding_done` al completar

### 3. Diagnóstico ASSIST
**Ubicación:** `ui/diagnostico/`  
**Propósito:** Evaluación de riesgo de consumo (herramienta OMS ASSIST).  
**Scoring:** BAJO (0-10) / MODERADO (11-26) / ALTO (27+)  
**Persistencia:** Room DB (tabla `resultados`)  
**Acciones sugeridas en ResultScreen:**

| Nivel | Acción 1 | Acción 2 | Acción 3 |
|---|---|---|---|
| BAJO | Bitácora | Conoce a Berto | Mis Avances |
| MODERADO | Manejo del craving | Info por sustancia | Hablar con Berto |
| ALTO | Directorio Profesional | Botón SOS / Red Apoyo | Hablar con Berto |

### 4. Home / Inicio
**Ubicación:** `screens/home/`  
**Estado:** Datos mock (Alex, racha 5 días)

**Secciones:**
- Top bar: menú + "Solvyx" italic + campana con badge
- Hero: Berto saludando, "Hola, Alex", fecha, racha
- Selector de emociones: 5 íconos (Triste/Ansioso/Neutral/Bien/Eufórico) → toca → `AnimatedVisibility` muestra `EmocionSugerenciaCard` con acción contextual
- Herramientas rápidas: scroll horizontal con 4 cards (Respirar / Hablar con Berto / Estoy en crisis / Buscar ayuda)
- Accesos rápidos: grid 2×2 clickable (Mi Plan / Berto / Primeros Auxilios / Mi Registro)
- Actividad reciente: 3 entradas mock

### 5. Bitácora
**Ubicación:** `screens/bitacora/`  
**ViewModel:** `RegistroViewModel`  
**Campos del registro:** Fecha · Estado de ánimo · Nota (máx 100) · ¿Consumiste? · Sustancia  
**No incluye:** nivel de ansiedad (eliminado)

### 6. Berto (Chatbot)
**Ubicación:** `screens/chatbot/`  
**ViewModel:** `ChatViewModel`  
**Features:** SpeechRecognizer (es-MX), detección de keywords, delay simulado 1500ms, TTS simulado  
**Estado:** Respuestas predefinidas, sin IA real

### 7. Guías de Primeros Auxilios
**Ubicación:** `screens/guias/`  
**Propósito:** 5 guías de crisis y reducción de daños.  
**Feature especial:** Ejercicio 5-4-3-2-1 con TTS en español (accesible también como top-level route `EJERCICIO_GUIADO`)

### 8. Red de Apoyo
**Ubicación:** `screens/red/`  
**ViewModel:** `RedApoyoViewModel`  
**Modos:** Setup (post-registro) / Regular (desde drawer)  
**Contactos:** 1 obligatorio, hasta 3 totales

### 9. Mi Plan
**Ubicación:** `screens/plan/`  
**ViewModel:** `PlanViewModel`  
**Hub:** Meta del día (rotatoria) · Progreso semanal (7 días)  
**Herramientas:** Manejo del craving → `ManejoCravingScreen` · Info por sustancia → `InfoSustanciaScreen`  
**No incluye:** Mis Detonantes y Mis Metas (eliminados)

### 10. Mis Avances
**Ubicación:** `screens/avances/`  
**ViewModel:** `AvancesViewModel`  
**Secciones:** Racha + mejor racha · Milestone progress (7/15/30 días) · Tabs Semana/Mes · Gráfica "Mi bienestar" (solo bienestar, sin línea ansiedad) · Gráfica consumo · Insight de Berto · Carrusel de logros

### 11. Directorio Profesional
**Ubicación:** `screens/directorio/`  
**ViewModel:** `DirectorioViewModel`  
**Contenido:** Psicólogos · Clínicas · Instituciones (CIJ, DIF, etc.)  
**Estado:** Datos mock implementados

### 12. Mi Perfil
**Ubicación:** `screens/perfil/`  
**ViewModel:** `PerfilViewModel`  
**Hub:** Editar perfil (BottomSheet) · Editar sustancias de seguimiento · Reiniciar ASSIST · Editar Red de Apoyo · Privacidad · Acerca de Solvyx · Términos y Condiciones · Cerrar sesión  
**Sub-rutas internas:** `perfil_privacidad` · `perfil_acerca` · `perfil_terminos`

### 13. SOS Overlay
**Ubicación:** `screens/sos/`  
**Ruta:** Top-level `SOS_OVERLAY`  
**Contenido:** Contactos de confianza · llamar a Línea de la Vida (800 911 2000) · ir a Berto

### 14. Configuración (obsoleto)
**Ubicación:** `screens/configuracion/`  
**Estado:** Obsoleto — funcionalidad migrada a Mi Perfil. Pendiente de eliminación del proyecto.

---

## Vistas Detalladas

---

### SplashScreen

**Archivo:** `screens/splash/SplashScreen.kt`  
**ViewModel:** `SplashViewModel`

| Atributo | Detalle |
|---|---|
| Fondo | BackgroundApp (crema `#F8F6F1`) |
| Elementos | Texto "Solvyx" centrado, displayLarge |
| Duración | 1500ms automático |
| Lógica | Lee DataStore → navega a Onboarding o AuthChoice |

---

### OnboardingScreen

**Archivo:** `screens/auth/onboarding/OnboardingScreen.kt`  
**ViewModel:** `OnboardingViewModel`

| Páginas | Animaciones Berto | Indicador |
|---|---|---|
| 4 | FLOAT / BOUNCE / PULSE / WAVE | Dots animados (8dp → 28dp activo) |

| # | Título | Estado Berto | Fondo |
|---|---|---|---|
| 1 | Tu espacio seguro | WAVE | TealPrimary |
| 2 | Diagnóstico ASSIST · OMS | PULSE | TealDark |
| 3 | Botón SOS | BOUNCE | TealPrimary |
| 4 | 100% Privado · Sin internet | FLOAT | TealDark |

---

### AuthChoiceScreen

**Archivo:** `screens/auth/choice/AuthChoiceScreen.kt`

| Hero | Botones | Footer |
|---|---|---|
| 62% altura, TealPrimary, Berto con halos (230dp) | "Iniciar Sesión" (filled) + "Crear cuenta" (outlined) | Términos y Privacidad |

---

### LoginScreen / RegisterScreen / ForgotPasswordScreen

| Pantalla | Archivo | Campos principales |
|---|---|---|
| Login | `auth/login/LoginScreen.kt` | Email · Contraseña |
| Register | `auth/register/RegisterScreen.kt` | Apodo · Email · Fecha de nac. · Contraseña × 2 · Términos |
| ForgotPassword | `auth/forgot_password/ForgotPasswordScreen.kt` | Email |

**Éxito de Register:** navega a `DIAGNOSTICO` (popUpTo AUTH_CHOICE)

---

### MainScreen

**Archivo:** `screens/main/MainScreen.kt`

| Atributo | Detalle |
|---|---|
| Drawer | CustomDrawer (60% ancho), animación scale 0.9 + offset |
| Bottom Nav | Inicio · Plan · Berto (elevado) · Avances + SOS flotante |
| Fondo | Gradiente vertical TealPrimary → TealDark |
| Imagen deco | `ic_decorations_hero_3_drawer`, blur effect |
| Bottom bar visible en | Inicio · Plan · RegistroEmocional · Avances |

---

### InicioScreen (Home)

**Archivo:** `screens/home/InicioScreen.kt`

| Sección | Detalle |
|---|---|
| Top Bar | Menú + "Solvyx" italic + campana con badge |
| Hero | Berto saludando (130dp), "Hola, Alex", fecha, racha chip |
| Emociones | 5 íconos seleccionables → `AnimatedVisibility` muestra `EmocionSugerenciaCard` |
| Herramientas rápidas | Scroll horizontal: Respirar (→EjercicioGuiado) · Berto (→Chat) · Estoy en crisis (→SosDialog) · Buscar ayuda (→Directorio) |
| Accesos rápidos | Grid 2×2: Mi Plan · Berto · Primeros Auxilios · Mi Registro |
| Actividad reciente | Card con 3 entradas mock |

**EmocionSugerenciaCard — mapeo:**

| Emoción | Sugerencia | Acción |
|---|---|---|
| Triste | Berto puede escucharte | Hablar con Berto |
| Ansioso | Prueba un ejercicio de respiración | Respirar ahora (→ EjercicioGuiado) |
| Neutral | Buen momento para registrar tu día | Ir al registro |
| Bien | Buen momento para registrar tu día | Ir al registro |
| Eufórico | Comparte este momento con tu red | Ver Red de Apoyo |

---

### RegistroEmocionalScreen (Bitácora)

**Archivo:** `screens/bitacora/RegistroEmocionalScreen.kt`  
**ViewModel:** `RegistroViewModel`

| Campo | Tipo | Valores |
|---|---|---|
| Fecha | CalendarBottomSheet | LocalDate, solo pasado |
| Estado de ánimo | 5 íconos seleccionables (52dp) | Triste · Ansioso · Neutral · Bien · Eufórico |
| Nota | TextArea, máx 100 chars | Texto libre |
| ¿Consumiste? | Dos botones (No = teal / Sí = rojo) | No / Sí → SustanciaBottomSheet |
| Sustancia | SustanciaBottomSheet | Alcohol · Cristal · Vape · Tabaco |
| Guardar | SolvyxButton sticky | → Dialog éxito |

**Dialog de éxito `RegistroExitosoDialog`:** Berto feliz + checkmark + resumen **Ánimo / Consumo** (2 columnas)

---

### HistorialBitacoraScreen

**Archivo:** `screens/bitacora/HistorialBitacoraScreen.kt`

| Sección | Detalle |
|---|---|
| Stats | 2 columnas: N° registros · Días sin consumo |
| Lista | LazyColumn de tarjetas cronológicas |
| Tarjeta | Fecha · badge consumo · ícono emoción (40dp) · nota truncada |

---

### BertoScreen (Chatbot)

**Archivo:** `screens/chatbot/BertoScreen.kt`  
**ViewModel:** `ChatViewModel`

| Sección | Detalle |
|---|---|
| Top Bar | Avatar Berto (color por estado), "En línea · Privado", dot pulsando |
| Mensajes | LazyColumn, burbujas usuario (primary) / Berto (surface) |
| Typing | Peek zone: ilustración + "Berto está escribiendo..." + 3 dots |
| Quick Replies | Chips bajo último mensaje |
| Input | SOS (rojo) + TextField + Micrófono + Enviar |

**Estados de Berto:**

| Estado | Visor | Trigger |
|---|---|---|
| TRANQUILO | BertoVisorCalm (verde claro) | Default |
| PREOCUPADO | BertoVisorWorried (amarillo) | Keywords ansiedad / tristeza |
| CELEBRANDO | BertoVisorCelebr (verde) | Keywords positivos / logros |
| CRISIS | BertoVisorCrisis (rojo) | Keywords crisis / SOS |

---

### GuiasHubScreen

**Archivo:** `screens/guias/screens/hub/GuiasHubScreen.kt`

| Card | Ruta interna |
|---|---|
| Cómo sé si estoy en crisis | `crisisId` |
| Ansiedad y ataque de pánico | `panic` |
| Craving muy intenso | `craving` |
| Consumí de más | `overuse` |
| Estoy en crisis ahora mismo | `crisis` |

**Footer de ayuda:** Línea de la Vida (800 911 2000) · SAPTEL (5552598121) · CIJ

---

### GuiaPanicoScreen + EjercicioGuiadoScreen

**Archivos:** `screens/guias/screens/panico/`  
**ViewModel:** `EjercicioGuiadoViewModel` (TTS)

**EjercicioGuiado — técnica 5-4-3-2-1:**

| Paso | Sentido | Burbujas |
|---|---|---|
| 1 | Ver | 5 objetos |
| 2 | Tocar | 4 texturas |
| 3 | Escuchar | 3 sonidos |
| 4 | Oler | 2 aromas |
| 5 | Saborear | 1 sabor |

**TTS:** voz femenina español, pitch 1.15, velocidad 0.85  
**Acceso:** desde GuiaPánico O como ruta top-level `EJERCICIO_GUIADO` (botón "Respirar" en InicioScreen)

---

### GuiaCrisisIdScreen / GuiaCravingIntensoScreen / GuiaConsumiDeMasScreen / GuiaEstoyEnCrisisScreen

| Pantalla | Característica clave |
|---|---|
| GuiaCrisisId | Señales físicas / emocionales / conductuales / cuándo llamar al 911 |
| GuiaCravingIntenso | Plan de 4 pasos + prácticas de reducción de daños |
| GuiaConsumiDeMás | Tabs por sustancia: Alcohol · Cristal · Vape · Tabaco |
| GuiaEstoyEnCrisis | Speech bubble Berto + CTA rojo prominente "Avisar a mi red" |

---

### RedApoyoScreen

**Archivo:** `screens/red/RedApoyoScreen.kt`  
**ViewModel:** `RedApoyoViewModel`

| Modo | Top Bar | CTA |
|---|---|---|
| Setup | Back + "1 de 1" | "Guardar perfil y comenzar" → HOME |
| Regular | Drawer | "Guardar cambios" |

Máximo 3 contactos (nombre ≥ 2 chars, teléfono ≥ 7 dígitos).  
**Done Overlay (solo setup):** Berto + "¡Listo!" + botón "Empezar"

---

### SosOverlayScreen

**Archivo:** `screens/sos/SosOverlayScreen.kt`  
**Ruta:** Top-level `Routes.SOS_OVERLAY`

Muestra contactos de confianza + botón llamar a Línea de la Vida (`800 911 2000`) + botón "Hablar con Berto" → navega a CHAT (popUpTo SOS_OVERLAY).

---

### DiagnosticoNavGraph

**Archivos:** `ui/diagnostico/`

| Pantalla | Ruta interna | Descripción |
|---|---|---|
| SubstanceSelectionScreen | `selection` | Grid 2×2 de sustancias |
| QuestionsScreen | `questions` | 6-7 preguntas ASSIST, progress bar, slide transitions |
| ResultScreen | `result` | Puntaje · badge nivel · recomendación · acciones sugeridas |
| HistoryScreen | `history` | Resultados pasados (Room Flow, DESC) |

Todas las acciones de `ResultScreen` navegan a `onNavigateToHome` (→ HOME, popUpTo DIAGNOSTICO).

---

### MiPlanHubScreen

**Archivo:** `screens/plan/MiPlanHubScreen.kt`  
**ViewModel:** `PlanViewModel`

| Sección | Detalle |
|---|---|
| Meta del día | Texto rotatorio + "Lo logré hoy" + "Ver otra" |
| Progreso semanal | 7 círculos L-M-X-J-V-S-D con check/vacío |
| Herramientas | Manejo del craving (→ ManejoCravingScreen) · Info por sustancia (→ InfoSustanciaScreen) |

---

### MisAvancesScreen

**Archivo:** `screens/avances/MisAvancesScreen.kt`  
**ViewModel:** `AvancesViewModel`

| Sección | Detalle |
|---|---|
| Hero | Racha actual · "días sin consumo" · mejor racha chip |
| Milestone card | Progress bar + marcadores 7/15/30 días |
| Tabs | Semana / Mes |
| Gráfica "Mi bienestar" | `FeelingsChart` — línea bienestar sólida (sin línea ansiedad) |
| Gráfica consumo | `ConsumptionChart` — barras de días de consumo |
| Insight Berto | BorderCard con observación contextual |
| Logros | LazyRow horizontal: 5 logros, 3 desbloqueados |

---

### MiPerfilScreen

**Archivo:** `screens/perfil/MiPerfilScreen.kt`  
**ViewModel:** `PerfilViewModel`  
**NavGraph:** `PerfilNavGraph` (rutas: `perfil_main` · `perfil_privacidad` · `perfil_acerca` · `perfil_terminos`)

| Sección | Detalle |
|---|---|
| Header | Avatar con iniciales (Canvas) · apodo · sustancias de seguimiento (FlowRow chips) |
| Mi progreso | Racha · días seguimiento · registros mes (3 stats) |
| Mi cuenta | "Editar perfil" (BottomSheet) · "Mis sustancias" (BottomSheet) |
| Herramientas | "Repetir diagnóstico ASSIST" · "Editar red de apoyo" |
| Información | Privacidad y datos · Acerca de Solvyx · Términos y condiciones |
| Sesión | Cerrar sesión (con `LogoutConfirmDialog`) |

---

### DirectorioRootScreen

**Archivo:** `screens/directorio/DirectorioRootScreen.kt`  
**ViewModel:** `DirectorioViewModel`

Directorio de recursos profesionales: psicólogos · clínicas · instituciones (CIJ, DIF, CJM, Consejo Ciudadano).  
**Datos:** hardcodeados en `DirectorioViewModel`. 11 entradas reales de Chilpancingo, Guerrero (1 CIJ, 2 clínicas, 4 psicólogos, 4 líneas de apoyo). Cada entrada incluye nombre, teléfono, dirección, horario, coordenadas (lat/lng) y, cuando aplica, un `mapEmbedUrl` de Google Maps para el WebView del detalle.

---

## Sistema de Diseño

### Paleta de Colores

| Token | Hex | Uso |
|---|---|---|
| TealPrimary | `#1D9E75` | Botones principales, headers, activos |
| TealMedium | `#5DCAA5` | Textos secundarios, ejes de gráficas |
| TealLight | `#9FE1CB` | Bordes suaves, placeholders |
| TealLightest | `#E1F5EE` | Fondos de cards, chips activos, guías de gráfica |
| TealDark | `#085041` | Texto principal, outlines Berto |
| BackgroundApp | `#F8F6F1` | Fondo general |
| CrisisRed | `#E24B4A` | SOS, emergencias, botón Sí-consumo |
| CrisisRedLight | `#fde8e8` | Fondo secciones de riesgo ALTO |
| WarnAmber | `#d97706` | Nivel MODERADO, advertencias |
| WarnAmberLight | `#fef9c3` | Fondo nivel MODERADO |
| TextMuted | `#888780` | Textos deshabilitados |
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
| displayLarge | 56sp | Black | Números grandes, racha |
| headlineLarge | 28sp | Bold | Títulos de sección |
| headlineMedium | 22sp | Bold | Subtítulos |
| headlineSmall | 18sp | Bold | Cabeceras de cards |
| titleLarge | 16sp | SemiBold | Top bars |
| titleMedium | 14sp | SemiBold | Items de lista |
| titleSmall | 13sp | SemiBold | Labels de sección |
| bodyLarge | 15sp | Normal | Texto de contenido |
| bodyMedium | 13sp | Normal | Cuerpo secundario |
| bodySmall | 12sp | Normal | Notas, meta-info |
| labelLarge | 14sp | ExtraBold | Botones |
| labelMedium | 12sp | ExtraBold | Tags, chips |
| labelSmall | 10sp | ExtraBold | Etiquetas mínimas |

---

### Shapes

| Uso | Radio |
|---|---|
| Pills / botones / chips | 50dp |
| Modales / Drawers / Dialogs | 28dp |
| Cards principales | 20dp |
| Cards estándar / herramientas | 14-16dp |
| Badges / etiquetas | 10-12dp |

---

### Iconografía

Aprox. 80+ íconos SVG propios en `res/drawable/`:

**Berto:** `berto_saludando`, `berto_preocupado`, `berto_tranquilo`, `berto_feliz`, `berto_sentado_mirando_izquierda`, `berto_mira_mariposa`, `berto_cabeza`, `berto_sin_internet`

**Sustancias:** `ic_bottle` (alcohol), `ic_gem` (cristal), `ic_vape`, `ic_cigarette` (tabaco)

**Emociones:** `ic_face_anxious`, `ic_face_happy`, `ic_face_neutral`, `ic_face_sad`, `ic_face_tired`, `ic_face_euphoric`

**UI General:** `ic_home`, `ic_menu`, `ic_settings`, `ic_chat`, `ic_bell`, `ic_calendar`, `ic_heart`, `ic_brain`, `ic_mic`, `ic_send`, `ic_shield`, `ic_sos`, `ic_phone`, `ic_plan`, `ic_trophy`, `ic_flame`, `ic_flag`, `ic_building`, `ic_people`, `ic_trending_up`, `ic_share`, `ic_history`, `ic_save`, `ic_lock`, `ic_wind`, `ic_alert_triangle`, `ic_check`, `ic_check_circle`, `ic_chevron_right`, `ic_chevron_down`, `ic_info`, `ic_activity`, `ic_alert_circle`, `ic_circle_x`, `ic_gem`, etc.

---

## Modelos de Datos

```kotlin
// Sustancias disponibles (NUNCA Cannabis)
val SUSTANCIAS = listOf("alcohol", "vape", "cristal", "cigarro")

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

// Entidad Room
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

// Mock de registro de bitácora (sin persistencia real)
data class RegistroMock(
    val fecha: String,
    val estadoAnimo: String,
    val consumio: Boolean,
    val sustancia: String?,
    val nota: String?
)
```

---

## Navegación

### Routes.kt

```kotlin
object Routes {
    const val SPLASH           = "splash"
    const val ONBOARDING       = "onboarding"
    const val AUTH_CHOICE      = "auth_choice"
    const val LOGIN            = "login"
    const val FORGOT_PASSWORD  = "forgot_password"
    const val REGISTER         = "register"
    const val HOME             = "home"
    const val CHAT             = "chat"           // soporta ?source=drawer
    const val DIAGNOSTICO      = "diagnostico"
    const val RED_APOYO_SETUP  = "red_apoyo_setup"
    const val SOS_OVERLAY      = "sos_overlay"
    const val EJERCICIO_GUIADO = "ejercicio_guiado"
}
```

### Rutas internas de MainScreen (NavigationItem)

```
Inicio / Plan / RegistroEmocional / Avances /
GuiasPrimerosAuxilios / RedApoyo / Directorio / MiPerfil /
Berto (navega fuera) / CerrarSesion (navega fuera)
```

### Rutas de GuiasNavGraph

```
guiasHub → crisisId / panic → ejercicioGuiado / craving / overuse / crisis
```

### Rutas de PlanNavGraph

```
planHub → manejo_craving / info_sustancia
```

### Rutas de PerfilNavGraph

```
perfil_main → perfil_privacidad / perfil_acerca / perfil_terminos
```

### Rutas de DiagnosticoNavGraph

```
selection → questions → result → history
```

---

## Componentes Compartidos

| Componente | Ubicación | Descripción |
|---|---|---|
| `SolvyxButton` | `common/` | Botón primario, 56dp altura, 28dp radius |
| `SolvyxOutlinedButton` | `common/` | Variante outlined, borde 1.5dp; soporta `buttonColor`/`textColor` custom |
| `SolvyxTextButton` | `common/` | Solo texto |
| `SolvyxTextField` | `common/` | OutlinedTextField con icono leading, soporte password |
| `SolvyxBackButton` | `common/` | IconButton con flecha atrás |
| `SolvyxStubTopBar` | `common/` | TopBar genérico |
| `PageIndicator` | `common/` | Dots animados de paginación |
| `CustomDrawer` | `drawer/` | Drawer 60% ancho, 3 secciones |
| `SolvyxBottomNavigationBar` | `navigation/` | 4 slots + Berto elevado + SOS flotante |
| `SosConfirmationDialog` | `dialog/` | Dialog de confirmación antes de abrir SOS |
| `GuiaTopBar` | `guias/components/` | Top bar para guías (back o menú) |
| `HeroSideBerto` | `guias/components/` | Hero section con Berto lateral |
| `GuiaPanel` | `guias/components/` | Panel blanco scrollable con overlap (-24dp) |
| `BorderCard` | `guias/components/` | Card con borde izquierdo coloreado |
| `CardLabel` | `guias/components/` | Label de sección con ícono |
| `DotRow` | `guias/components/` | Punto de lista con dot de color |
| `StepRow` | `guias/components/` | Paso numerado con badge círculo |
| `HelpLineRow` | `guias/components/` | Fila clickable de número de ayuda |

---

## Líneas de Ayuda (constantes en código)

| Servicio | Número |
|---|---|
| Línea de la Vida (CONADIC) | **800 911 2000** |
| SAPTEL | 55 5259 8121 |
| CIJ | (según directorio) |

---

## Dependencias Clave

| Librería | Uso |
|---|---|
| Jetpack Compose + Material3 | UI framework |
| Navigation Compose | Navegación declarativa |
| Hilt | Inyección de dependencias |
| Room | Base de datos local |
| DataStore Preferences | Persistencia liviana (onboarding) |
| Retrofit + OkHttp | HTTP (pendiente backend) |
| Coroutines + Lifecycle | Async + ViewModel |
| Accompanist Pager | Onboarding horizontal pager |
| Lottie Compose | Animaciones Lottie |
| Haze | Efecto blur decorativo |

---

*Última actualización: Mayo 2026*
