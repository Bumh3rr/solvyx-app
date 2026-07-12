---
description: Dark mode en Solvyx. Definición de ColorScheme dark, transiciones, contrastes, edge cases.
---

# Skill: Dark Mode

Esta skill te entrega las convenciones para implementar y mantener dark mode en Solvyx. Aplícala al crear o modificar colores, componentes, o temas.

## Principios

1. **Light + Dark desde el inicio.** Nunca agregar un color pensando solo en light.
2. **Contraste WCAG AA mínimo en ambos modos:** 4.5:1 texto normal, 3:1 texto grande.
3. **No invertir mecánicamente colores light.** Dark mode no es "lo mismo pero al revés".
4. **`isSystemInDarkTheme()` por defecto.** Respetar preferencia del usuario.
5. **Override manual solo en pantallas específicas** (ej. modo lectura nocturna).
6. **Estados de carga y error también con variantes dark.**

## Verificar contraste

| Elemento | Light ratio | Dark ratio |
|---|---|---|
| Texto principal sobre fondo | ≥4.5:1 | ≥4.5:1 |
| Texto secundario sobre fondo | ≥4.5:1 | ≥4.5:1 |
| Texto grande sobre fondo | ≥3:1 | ≥3:1 |
| Botones | ≥4.5:1 (label vs fondo) | ≥4.5:1 |
| Borders | ≥3:1 | ≥3:1 |

Usa herramientas online como WebAIM Contrast Checker para validar pares específicos.

## ColorScheme dark

### Plantilla

```kotlin
private val SolvyxDarkColorScheme = darkColorScheme(
    primary = TealLight,                    // invertido del light
    onPrimary = TealDark,
    primaryContainer = TealDark,
    onPrimaryContainer = TealLightest,
    
    secondary = TealMedium,
    onSecondary = TealDark,
    secondaryContainer = TealDark,
    onSecondaryContainer = TealLight,
    
    tertiary = WarningAmberDark,
    onTertiary = Color.Black,
    
    background = BackgroundAppDark,         // #1A1A1A
    onBackground = TextPrimaryDark,         // #F5F5F5
    
    surface = SurfaceDark,                  // #2A2A2A
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    
    error = ErrorRedDark,
    onError = Color.White,
    errorContainer = ErrorContainerDark,
    onErrorContainer = ErrorLight,
    
    outline = TealMedium,
    outlineVariant = TealDark
)
```

### Inversión inteligente

| Token | Light | Dark | Lógica |
|---|---|---|---|
| `primary` | TealPrimary (#0F766E) | TealLight (#A8D5D0) | Color más claro en dark para destacar sobre fondo oscuro. |
| `background` | Crema claro | Casi negro | Contraste invertido. |
| `surface` | Blanco | Gris muy oscuro | Para cards. |
| `textPrimary` | Casi negro | Casi blanco | Texto principal. |
| `outline` | Gris medio | Gris medio | Similar, solo ajusta para contraste. |

### Colores que NO se invierten

| Color | Light | Dark | Razón |
|---|---|---|---|
| `SOSRed` | `#E24B4A` | `#E86765` | Rojo ligeramente más claro en dark para no quemar. |
| `WarningAmber` | `#D97706` | `#F59E0B` | Similar. |
| `SuccessGreen` | `#15803D` | `#4ADE80` | Verde más vibrante en dark. |

## Theme wrapper con detección automática

```kotlin
@Composable
fun SolvyxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> SolvyxDarkColorScheme
        else -> SolvyxLightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = SolvyxTypography,
        shapes = SolvyxShapes,
        content = content
    )
}
```

## Toggle manual desde Mi Perfil

```kotlin
enum class ThemeMode {
    SYSTEM,    // sigue el sistema
    LIGHT,     // forzar light
    DARK       // forzar dark
}

@Composable
fun SolvyxTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    
    MaterialTheme(
        colorScheme = if (darkTheme) SolvyxDarkColorScheme else SolvyxLightColorScheme,
        content = content
    )
}
```

## Status bar y navigation bar

Adaptar al modo:

```kotlin
@Composable
fun SolvyxTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            
            if (isSystemInDarkTheme()) {
                window.statusBarColor = Color.Transparent.toArgb()
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            } else {
                window.statusBarColor = Color.Transparent.toArgb()
                controller.isAppearanceLightStatusBars = true
                controller.isAppearanceLightNavigationBars = true
            }
        }
    }
    
    MaterialTheme(/* ... */)
}
```

## Previews para ambos modos

```kotlin
@Preview(name = "Light", showBackground = true, widthDp = 360)
@Composable
private fun SolvyxButtonLightPreview() {
    SolvyxTheme(darkTheme = false) {
        SolvyxButton("Test", onClick = {})
    }
}

@Preview(
    name = "Dark",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SolvyxButtonDarkPreview() {
    SolvyxTheme(darkTheme = true) {
        SolvyxButton("Test", onClick = {})
    }
}
```

## Imágenes y assets

### Berto

Los assets de Berto (`berto_*.png`) tienen fondo transparente. Funcionan en ambos modos sin cambios.

### Imágenes decorativas

Si una imagen tiene fondo blanco o negro, **considera crear variante dark**:

```
res/
├── drawable-mdpi/
│   ├── decoration_hero.png        (light)
│   └── decoration_hero_dark.png   (dark)
```

En Compose:

```kotlin
val isDark = isSystemInDarkTheme()
val painter = painterResource(
    if (isDark) R.drawable.decoration_hero_dark
    else R.drawable.decoration_hero
)
Image(painter = painter, contentDescription = null)
```

## Sombras y elevación

En dark mode, las sombras son menos visibles. Compensar con:

1. **Bordes sutiles** (`outlineVariant`) en cards.
2. **Tonos más claros** en `surfaceVariant` vs `surface`.
3. **Tonal elevation** en Material 3 (ya soportado).

```kotlin
// Light: card con sombra sutil
Card(
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
) { /* ... */ }

// Dark: card con borde en lugar de sombra
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
) { /* ... */ }
```

## Errores comunes

| Error | Solución |
|---|---|
| Color que solo funciona en light | Agregar variante dark. |
| `Color.Black` o `Color.White` hardcoded | Usar `MaterialTheme.colorScheme.surface` o `onSurface`. |
| Texto blanco sobre fondo blanco en dark | Verificar `onSurface` y `onBackground` en dark scheme. |
| Status bar negro en dark mode | Configurar `controller.isAppearanceLightStatusBars = false`. |
| Imágenes con fondo fijo | Crear variantes light/dark. |
| Bordes invisibles en dark | Aumentar opacidad o cambiar a `outlineVariant`. |

## Testing

```kotlin
@OptIn(ExperimentalTestApi::class)
class DarkModeTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun text_is_visible_in_dark_mode() {
        composeTestRule.setContent {
            SolvyxTheme(darkTheme = true) {
                Text("Hello", color = MaterialTheme.colorScheme.onSurface)
            }
        }
        
        composeTestRule.onNodeWithText("Hello").assertIsDisplayed()
    }
}
```

## Anti-patrones prohibidos

1. **Colores solo definidos para light.**
2. **`Color.Black` o `Color.White` directos en componentes.**
3. **Inversión mecánica de todos los colores.** Algunos no deben invertirse.
4. **Sombras altas en dark.** Casi no se ven.
5. **Sin previews dark.**
6. **Asumir que el usuario prefiere un modo fijo.** Ofrecer SYSTEM como default.
7. **Imágenes con fondo fijo** sin variantes.
8. **Status bar mal configurado** en dark mode.
9. **Componentes que ignoran `MaterialTheme.colorScheme`** y usan colores hardcoded.