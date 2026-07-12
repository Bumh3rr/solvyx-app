# Mapa de navegación de Solvyx

> Documento vivo. Cada vez que se agregue una pantalla o ruta nueva,
> actualizar este archivo. Mantiene al equipo y a los testers alineados
> sobre **dónde encontrar cada feature**.

Última actualización: 2026-07-12 (post Iteración A-D del rediseño UI/UX)

---

## Inicio de la app

```
[ Splash ]  (1.5s, lee DataStore)
    ↓
[ Onboarding ]  (4 páginas, si no se ha visto)
    ↓
[ AuthChoice ]  ←  Botón principal: "Crear cuenta"  (registro OBLIGATORIO)
    ↓
[ Register ]   (apodo, fecha nac, email, contraseña)
    ↓
[ Diagnostico ASSIST ]  (opcional)
    ↓
[ Red Apoyo Setup ]   (mín. 1 contacto, opcional)
    ↓
[ Home ]  ← aquí vive todo
```

### AuthChoice (registro obligatorio)
- **Botón principal:** "Crear cuenta" → navega a Register.
- **Enlace secundario:** "¿Ya tienes cuenta? Inicia sesión" → navega a Login.
- **Banner de privacidad:** "100% privado. Sin servidor. Tú controlas tus datos."

---

## Home (pantalla principal)

`Home` muestra:

```
[ Hola, Alex + racha ]

[ 5 selector de emoción ]   ← tap → Berto responde contextual

QUÉ NECESITAS AHORA  (4 cards horizontales)
  [ Respirar ]  [ Hablar con Berto ]  [ Estoy en crisis ]  [ Buscar ayuda ]

DESCUBRE SOLVYX  [NUEVO]  (6 cards en grid 2x3)
  [ Ejercicios ]  [ Psicoeducación ]
  [ Rutinas ]     [ Journaling ]
  [ Guías ext. ]  [ Insights ]
  [ Ver todo → ]   ← abre pantalla "Descubrir"

ACTIVIDAD RECIENTE  (3 últimas entradas de bitácora)
```

### ¿Dónde encuentro...?

| Quiero... | Dónde lo encuentro |
|---|---|
| **Registrar cómo me siento hoy** | Home → "Registro diario" (bottom nav) |
| **Respirar o hacer un ejercicio guiado** | Home → "Respirar" (acción rápida) **o** Drawer → "Ejercicios" |
| **Hablar con Berto** | Home → "Hablar con Berto" (acción rápida) **o** Bottom nav (centro) **o** Drawer |
| **Activar el botón SOS** | Cualquier pantalla → botón rojo flotante (top-right) |
| **Ver un directorio de profesionales** | Drawer → "Directorio Profesional" |
| **Configurar mi red de apoyo** | Drawer → "Mi Perfil" → "Red de Apoyo" |

---

## Drawer lateral (☰)

Se abre desde cualquier pantalla con el ícono ☰ arriba-izquierda.

```
TU PERFIL
  Hola, Alex 👋
  [click] → Mi Perfil

RUTINA · uso diario
  ▸ Inicio
  ▸ Mi Plan
  ▸ Registro diario
  ▸ Mis Avances
  ▸ Rutinas  [NUEVO]

HERRAMIENTAS · apoyo y recursos
  ▸ Hablar con Berto
  ▸ Ejercicios  [NUEVO]
  ▸ Guías de primeros auxilios  →  (submenú expandible)
      Crisis y cravings (5)
        • Cómo sé si estoy en crisis
        • Ansiedad y ataque de pánico
        • Craving muy intenso
        • Consumí de más
        • Estoy en crisis ahora mismo
      Extendidas (8)  [NUEVO]
        • Desregulación / flashback
        • Intoxicación: esperando que pase
        • Craving extremo (después de consumir)
        • Noche difícil (insomnio, madrugada)
        • Conflicto con familia
        • Violencia sexual reciente
        • Volver de fiesta
        • Después de consumir de nuevo
  ▸ Psicoeducación  [NUEVO]
  ▸ Journaling  [NUEVO]
  ▸ Insights de Berto  [NUEVO]
  ▸ Directorio Profesional
  ▸ Descubrir Solvyx  [NUEVO]

MI CUENTA
  ▸ Mi Perfil
  [ Cerrar sesión ]
```

> **Nota:** Los items marcados `[NUEVO]` muestran un badge naranja
> la primera vez. Se ocultan al primer tap.

---

## Bottom navigation (visible en Inicio, Plan, Registro, Avances)

```
[ Inicio (casa) ]   [ Plan (target) ]   [ Berto (centro) ]   [ Avances (trofeo) ]   [ SOS flotante ]
```

---

## Mapa completo de pantallas

| Pantalla | Ruta | Dónde la encuentro |
|---|---|---|
| **Splash** | `splash` | Automático al abrir |
| **Onboarding** | `onboarding` | Tras splash si no se ha visto |
| **AuthChoice** | `auth_choice` | Post-onboarding |
| **Login** | `login` | AuthChoice → enlace "¿Ya tienes cuenta?" |
| **Register** | `register` | AuthChoice → "Crear cuenta" |
| **ForgotPassword** | `forgot_password` | Login → enlace |
| **Home** | `home` | Tras registro exitoso |
| **Inicio** (Home screen) | (dentro de Home) | Home o Drawer |
| **Mi Plan** | (dentro de Home) | Drawer → "Mi Plan" o Bottom nav |
| **Registro diario** | (dentro de Home) | Drawer o Bottom nav |
| **Mis Avances** | (dentro de Home) | Drawer o Bottom nav |
| **Rutinas** | `rutinas` | Drawer → "Rutinas" o Home → Descubre → "Rutinas" |
| **Rutina detalle** | `rutinas/{slug}` | Rutinas → tap en una rutina |
| **Hablar con Berto (Chat)** | `chat?source=` | Drawer, Bottom nav, o acción rápida |
| **Ejercicios** | `ejercicios` | Drawer → "Ejercicios" o Home → Descubre |
| **Ejercicio detalle** | `ejercicios/{slug}` | Ejercicios → tap en uno |
| **Ejercicio activo (TTS)** | `ejercicios/{slug}/activo` | Ejercicio detalle → "Iniciar" |
| **Guías de primeros auxilios** | (dentro de Home) | Drawer → submenú "Guías" |
| **Guía detalle (original)** | (dentro de Home) | Submenú → tap en una de 5 |
| **Guías extendidas** | `guias-extendidas` | Drawer → submenú "Extendidas" o Home → Descubre |
| **Guía detalle (extendida)** | `guias-extendidas/{slug}` | Submenú Ext. → tap en una de 8 |
| **Psicoeducación** | `lecciones` | Drawer → "Psicoeducación" o Home → Descubre |
| **Lección detalle** | `lecciones/{sustancia}/{slug}` | Psicoeducación → tap en una |
| **Journaling** | `journaling` | Drawer → "Journaling" o Home → Descubre |
| **Journaling editor** | `journaling/editor?promptSlug=...` | Journaling → tap en prompt o FAB |
| **Insights de Berto** | `insights` | Drawer → "Insights de Berto" o Home → Descubre |
| **Descubrir Solvyx** (hub) | `descubrir` | Home → "Ver todo →" o Drawer |
| **Directorio Profesional** | (dentro de Home) | Drawer → "Directorio Profesional" |
| **Mi Perfil** | (dentro de Home) | Drawer → "Mi Perfil" o tap en avatar |
| **Red de Apoyo** | (dentro de Home) | Drawer → "Mi Red de apoyo" o Mi Perfil |
| **SOS Overlay** | `sos_overlay` | Botón SOS flotante |
| **Diagnostico ASSIST** | `diagnostico` | Post-registro o Mi Perfil → "Reiniciar ASSIST" |
| **Red Apoyo Setup** | `red_apoyo_setup` | Post-ASSIST o Mi Perfil |
| **Cerrar sesión** | (acción) | Drawer → "Cerrar sesión" abajo |

---

## Features nuevas — guía rápida

Esta sección es para testers y nuevos miembros del equipo.

### 🆕 Ejercicios de regulación
- **Drawer** → HERRAMIENTAS → "Ejercicios" (badge NUEVO)
- **Home** → DESCUBRE SOLVYX → "Ejercicios"
- **Descubrir** (hub) → "Regular tu día" → "Ejercicios"
- Pantalla: grid 2×3 de 6 ejercicios. Toca uno → detalle → "Iniciar" (TTS guiado).

### 🆕 Guías extendidas (8 adicionales)
- **Drawer** → HERRAMIENTAS → "Guías de primeros auxilios" → expandir submenú → sección "Extendidas (8)"
- **Home** → DESCUBRE SOLVYX → "Guías ext."
- **Descubrir** → "Momentos difíciles" → "Guías extendidas"
- 8 guías: Desregulación, Intoxicación, Craving extremo, Noche difícil, Conflicto familia, Violencia sexual, Volver de fiesta, Después de consumir.

### 🆕 Psicoeducación (24 lecciones)
- **Drawer** → HERRAMIENTAS → "Psicoeducación" (badge NUEVO)
- **Home** → DESCUBRE SOLVYX → "Psicoeducación"
- **Descubrir** → "Entender" → "Psicoeducación"
- Tabs por sustancia (Alcohol, Vape, Cristal, Tabaco) × 6 temas = 24 lecciones.

### 🆕 Journaling
- **Drawer** → HERRAMIENTAS → "Journaling" (badge NUEVO)
- **Home** → DESCUBRE SOLVYX → "Journaling"
- **Descubrir** → "Expresarte" → "Journaling"
- Tabs por categoría (Gratitud, Dificultad, Curiosidad, Emociones, Cravings, Planes). 30+ prompts. FAB "+" para entrada libre.

### 🆕 Rutinas
- **Drawer** → RUTINA → "Rutinas" (badge NUEVO)
- **Home** → DESCUBRE SOLVYX → "Rutinas"
- **Descubrir** → "Regular tu día" → "Rutinas"
- 2 rutinas: Matutina (8AM) y Nocturna (10PM). Pasos tildables con progreso diario.

### 🆕 Insights de Berto
- **Drawer** → HERRAMIENTAS → "Insights de Berto" (badge NUEVO)
- **Home** → DESCUBRE SOLVYX → "Insights"
- **Descubrir** → "Entender" → "Insights de Berto"
- 7 reglas determinísticas. Banner con insight actual. Botón "Evaluar ahora".

### 🆕 Descubrir Solvyx (hub)
- **Drawer** → HERRAMIENTAS → "Descubrir Solvyx" (badge NUEVO, abajo)
- **Home** → "Ver todo →" en sección DESCUBRE SOLVYX
- Hub con 4 categorías: Regular tu día, Entender, Expresarte, Momentos difíciles.

### 🆕 Bitácora extendida
- **Drawer** → RUTINA → "Registro diario"
- En el top bar, tap en el ícono de detalles/clipboard → abre BottomSheet con 9 campos opcionales.

---

## Cambios desde el rediseño UI/UX (2026-07-12)

| Antes | Ahora |
|---|---|
| Drawer con 11 items sin agrupar | Drawer con 3 secciones (RUTINA, HERRAMIENTAS, MI CUENTA) |
| "Guías de primeros auxilios" y "Guías Extendidas" como 2 items | Submenú expandible con 5 originales + 8 extendidas |
| Sin badges de "NUEVO" | Badge naranja en los 6 items nuevos, oculto al primer tap |
| Home sin sección "Descubre" | Sección "DESCUBRE SOLVYX" con grid 2×3 de cards |
| Sin hub de features nuevas | Pantalla "Descubrir Solvyx" con 4 categorías |
| AuthChoice con "Iniciar Sesión" + "Crear cuenta" | "Crear cuenta" como botón principal único, "Iniciar sesión" como enlace |
| Registro sin validación robusta | Validación: apodo, email regex, contraseña ≥8, edad 13-25, términos |

---

## Mantenimiento

Al agregar una pantalla nueva:

1. **Crear la ruta** en `Routes.kt` con `SolvyxRoutes.X`.
2. **Registrar** en `NavGraph.kt` con `composable(...)`.
3. **Si es top-level** (accesible desde drawer), añadir `NavigationItem` con `isNew = true` y agregarlo a `isHerramientas()` o `isRutina()`.
4. **Si es sección de Descubrir**, agregar card en `DescubrirScreen.kt`.
5. **Actualizar este documento** (`docs/navigation-map.md`).
6. **Si tiene copy clínico nuevo**, validar con `psicologo-solvyx`.
