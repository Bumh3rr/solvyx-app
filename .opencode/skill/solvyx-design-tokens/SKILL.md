---
description: Design tokens específicos de Solvyx: paleta Teal, tipografía Nunito, shapes, spacing, sombras. Convenciones para mantener consistencia.
---

# Skill: Solvyx Design Tokens

Esta skill te entrega los design tokens específicos del proyecto Solvyx. Aplícala cuando necesites valores concretos de color, tipografía, spacing, shape o shadow que ya están definidos en el proyecto.

## Principios

1. **Tokens son la fuente única de verdad.** Nunca hardcodear valores numéricos.
2. **Tokens semánticos** preferidos sobre tokens literales. `MaterialTheme.colorScheme.primary` no `Color(0xFF0F766E)`.
3. **Documentar cada token nuevo** con nombre, valor y uso.
4. **No crear tokens nuevos si uno existente cubre el caso.**
5. **Light + Dark** desde el inicio. Cualquier token nuevo debe tener ambas variantes.

## Paleta Teal (colores primarios)

| Token | Valor Light | Valor Dark | Uso |
|---|---|---|---|
| `TealPrimary` | `#0F766E` | `#5BAF9E` | Botones primarios, íconos activos. |
| `TealDark` | `#0E5C56` | `#3A8C7E` | Headers, dark surfaces. |
| `TealMedium` | `#5B8C87` | `#7AB5AB` | Texto secundario. |
| `TealLight` | `#A8D5D0` | `#3F6E68` | Borders, íconos inactivos. |
| `TealLightest` | `#E8F5F3` | `#2A4A45` | Backgrounds sutiles. |

## Colores de soporte

| Token | Valor Light | Valor Dark | Uso |
|---|---|---|---|
| `BackgroundApp` | `#F8F6F1` | `#1A1A1A` | Background principal (crema). |
| `SurfaceWhite` | `#FFFFFF` | `#2A2A2A` | Cards, sheets. |
| `TextPrimary` | `#1F2937` | `#F5F5F5` | Texto principal. |
| `TextSecondary` | `#6B7280` | `#A1A1AA` | Texto secundario. |
| `Divider` | `#E5E7EB` | `#374151` | Dividers, borders sutiles. |
| `SOSRed` | `#E24B4A` | `#E86765` | Botones de crisis/SOS. |
| `WarningAmber` | `#D97706` | `#F59E0B` | Señales de alerta. |
| `SuccessGreen` | `#15803D` | `#4ADE80` | Confirmaciones, logros. |
| `InfoBlue` | `#1E40AF` | `#60A5FA` | Información neutral. |

## Tipografía Nunito

| Token | Weight | Tamaño | Uso |
|---|---|---|---|
| `displayLarge` | Bold | 32sp | Títulos de pantalla grandes. |
| `displayMedium` | Bold | 28sp | Títulos de sección. |
| `headlineMedium` | Bold | 24sp | Subtítulos importantes. |
| `titleLarge` | Bold | 20sp | Títulos de cards. |
| `titleMedium` | SemiBold | 16sp | Subtítulos de cards. |
| `bodyLarge` | Normal | 16sp | Texto principal. |
| `bodyMedium` | Normal | 14sp | Texto secundario. |
| `bodySmall` | Normal | 12sp | Texto auxiliar. |
| `labelLarge` | ExtraBold | 14sp | Botones. |

### Pesos de Nunito disponibles

```kotlin
FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold)
)
```

## Shapes

| Token | Valor | Uso |
|---|---|---|
| `extraSmall` | 4dp | Borders, tags. |
| `small` | 8dp | Chips, pequeños elementos. |
| `medium` | 12dp | Botones, inputs. |
| `large` | 16dp | Cards. |
| `extraLarge` | 24dp | Sheets, modales. |

### Shapes especiales

| Token | Valor | Uso |
|---|---|---|
| `roundedButton` | 50dp (pill) | Botones principales, FAB. |
| `roundedTopSheet` | top 16dp | Bottom sheets. |

## Spacing

| Token | Valor | Uso |
|---|---|---|
| `space.xs` | 4dp | Entre íconos y texto pequeños. |
| `space.sm` | 8dp | Padding interno en cards. |
| `space.md` | 12dp | Separación entre items. |
| `space.lg` | 16dp | Padding estándar. |
| `space.xl` | 24dp | Padding de pantalla. |
| `space.xxl` | 32dp | Separación entre secciones. |

### Implementación como objeto

```kotlin
object SolvyxSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

// Uso:
Column(
    verticalArrangement = Arrangement.spacedBy(SolvyxSpacing.md)
)
```

## Touch targets

| Token | Valor | Uso |
|---|---|---|
| `touchTarget.min` | 48dp x 48dp | Mínimo accesible. |
| `touchTarget.comfortable` | 56dp x 56dp | Botones principales. |
| `touchTarget.large` | 64dp x 64dp | CTA principal (SOS). |

## Elevación / Sombras

| Token | Valor | Uso |
|---|---|---|
| `elevation.none` | 0dp | Sin sombra (sobre surface). |
| `elevation.low` | 1dp | Cards normales. |
| `elevation.medium` | 4dp | FAB, dialogs. |
| `elevation.high` | 8dp | Sheets expandidos. |

## Tamaños de íconos

| Token | Valor | Uso |
|---|---|---|
| `iconSize.sm` | 16dp | Íconos en línea con texto pequeño. |
| `iconSize.md` | 24dp | Íconos estándar. |
| `iconSize.lg` | 32dp | Íconos en cards destacadas. |
| `iconSize.xl` | 48dp | Íconos en estados vacíos. |

## Tamaños de avatar

| Token | Valor | Uso |
|---|---|---|
| `avatar.sm` | 32dp | Avatar de usuario pequeño. |
| `avatar.md` | 48dp | Avatar estándar. |
| `avatar.lg` | 92dp | Berto en hero. |
| `avatar.xl` | 130dp | Berto saludando en Home. |

## Tamaños de Berto

| Token | Valor | Contexto |
|---|---|---|
| `berto.bottomNav` | 48dp | En el bottom nav elevado. |
| `berto.hero` | 92dp | En hero de guías. |
| `berto.home` | 130dp | En home. |
| `berto.guiaCard` | 44dp | En card de "Hablar con Berto". |
| `berto.emptyState` | 120-140dp | En estados vacíos. |
| `berto.dialog` | 80dp | En diálogos. |

## Tokens de estado emocional (Berto)

Berto cambia según estado emocional. Estos son los assets y cuándo usarlos:

| Asset | Estado | Cuándo |
|---|---|---|
| `berto_saludando` | Saludando | Onboarding, Home principal. |
| `berto_tranquilo` | Tranquilo | Default, estados calmos. |
| `berto_feliz` | Feliz | Celebración, logro. |
| `berto_preocupado` | Preocupado | Craving, ansiedad. |
| `berto_mira_mariposa` | Curioso | Discover, exploración. |
| `berto_sentado_mirando_izquierda` | Reflexivo | Reflexión, journaling. |
| `berto_cabeza` | Cabeza (ícono) | Bottom nav. |
| `berto_sin_internet` | Sin red | Estado offline. |

## Tokens de tiempo

| Token | Valor | Uso |
|---|---|---|
| `animation.short` | 150ms | Transiciones de íconos. |
| `animation.medium` | 300ms | Cambios de estado. |
| `animation.long` | 500ms | Entrada de pantalla. |
| `debounce.search` | 300ms | Búsqueda. |
| `debounce.insight` | 72h | Insights automáticos. |

## Cómo extender los tokens

Cuando necesites un token nuevo:

1. **Verifica si uno existente cubre el caso.** Revisa esta skill primero.
2. **Si es verdaderamente nuevo, agrégalo a:**
   - `Color.kt` (con variantes light y dark).
   - `Theme.kt` (si va en `ColorScheme`).
   - Esta skill (documentar).
3. **Comunica al equipo.** El skill es la documentación viva.
4. **Usa el nuevo token** en todos los lugares que apliquen.

## Cuándo NO crear tokens nuevos

- Si solo lo usas una vez, hardcodear con `Color(0xFF...)` y comentario está bien temporalmente.
- Si coincide con un Material 3 colorScheme, usa ese.
- Si es un valor numérico único sin significado semántico (ej. 13dp), está bien hardcodear.

## Anti-patrones prohibidos

1. **`Color(0xFF...)` en pantallas.** Usar tokens.
2. **`16.dp` literal en componentes core.** Usar tokens de spacing.
3. **`RoundedCornerShape(12.dp)` literal.** Usar `MaterialTheme.shapes.medium`.
4. **Crear tokens sin documentarlos.**
5. **Tokens sin variante dark.**
6. **Reutilizar tokens con significados distintos.** Un token = un significado.
7. **Modificar tokens existentes sin avisar.** Rompe todos los usos.