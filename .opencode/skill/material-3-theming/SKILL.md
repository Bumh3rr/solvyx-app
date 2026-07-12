---
description: Material 3 theming para Solvyx. ColorScheme, Typography, Shapes, dark mode, tokens, dynamic color.
---

# Skill: Material 3 Theming

Esta skill te entrega las convenciones para usar Material 3 theming en Solvyx. Aplícala al crear o modificar el theme, colores, tipografía y shapes del proyecto.

## Principios

1. **Material 3 siempre.** Migrar lo que quede de Material 2.
2. **Color tokens semánticos.** `primary`, `secondary`, `surface`, etc. NUNCA colores raw en componentes.
3. **Light + Dark desde el inicio.** Cualquier color nuevo debe funcionar en ambos modos.
4. **Tipografía Nunito.** No usar otra fuente.
5. **Shapes consistentes.** `RoundedCornerShape` con valores fijos del proyecto.
6. **`SolvyxTheme` wrapper.** Todo Composable vive dentro de este theme.
7. **Dynamic color opcional** para Android 12+. Detrás de un feature flag.

## Setup

```kotlin
implementation("androidx.compose.material3:material3:1.3.0")
```

## ColorScheme

### Light

```kotlin
private val SolvyxLightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealLightest,
    onPrimaryContainer = TealDark,
    
    secondary = TealMedium,
    onSecondary = Color.White,
    secondaryContainer = TealLight,
    onSecondaryContainer = TealDark,
    
    tertiary = WarningAmber,
    onTertiary = Color.White,
    
    background = BackgroundApp,
    onBackground = TextPrimary,
    
    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundApp,
    onSurfaceVariant = TextSecondary,
    
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorDark,
    
    outline = TealLight,
    outlineVariant = TealLightest
)
```

### Dark

```kotlin
private val SolvyxDarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = TealDark,
    primaryContainer = TealDark,
    onPrimaryContainer = TealLightest,
    
    secondary = TealMedium,
    onSecondary = TealDark,
    secondaryContainer = TealDark,
    onSecondaryContainer = TealLight,
    
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    
    error = ErrorRedDark,
    onError = Color.White,
    errorContainer = ErrorDark,
    onErrorContainer = ErrorLight,
    
    outline = TealMedium,
    outlineVariant = TealDark
)
```

### Theme wrapper

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

## Typography

### Definición

```kotlin
val SolvyxTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp
    )
)
```

### Uso

```kotlin
Text(
    text = "Título",
    style = MaterialTheme.typography.headlineMedium  // no FontSize(24.sp)
)
```

## Shapes

```kotlin
val SolvyxShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)
```

### Uso

```kotlin
Card(
    shape = MaterialTheme.shapes.large,  // 16dp
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
) { /* ... */ }
```

## Semantic colors

### Cuándo usar cada rol

| Color | Uso |
|---|---|
| `primary` | Acciones primarias (botón "Aceptar", FAB activo). |
| `onPrimary` | Texto/iconos sobre `primary`. |
| `primaryContainer` | Fondo de elementos primarios (cards destacadas). |
| `onPrimaryContainer` | Texto/iconos sobre `primaryContainer`. |
| `secondary` | Acciones secundarias. |
| `tertiary` | Acciones terciarias o informativas. |
| `error` | Mensajes de error, validación. |
| `errorContainer` | Fondo de cards de error. |
| `surface` | Fondo de cards, sheets. |
| `surfaceVariant` | Fondo sutil para elementos secundarios. |
| `background` | Fondo de pantalla. |
| `outline` | Borders, dividers. |

## Dynamic Color (Android 12+)

```kotlin
@Composable
fun SolvyxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,  // default ON, usuario puede desactivar
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
    
    MaterialTheme(colorScheme = colorScheme, /* ... */)
}
```

**Consideración:** dynamic color reemplaza el TealPrimary con el color de acento del sistema del usuario. Esto puede romper la identidad visual. Considera poner un toggle en Mi Perfil: "Usar colores del sistema (Android 12+)".

## Status bar y navigation bar

```kotlin
@Composable
fun SolvyxTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = MaterialTheme.colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    
    MaterialTheme(/* ... */, content = content)
}
```

## Previews

```kotlin
@Preview(showBackground = true)
@Composable
private fun SolvyxThemeLightPreview() {
    SolvyxTheme(darkTheme = false) {
        Surface {
            Text("Light Theme", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SolvyxThemeDarkPreview() {
    SolvyxTheme(darkTheme = true) {
        Surface {
            Text("Dark Theme", style = MaterialTheme.typography.headlineMedium)
        }
    }
}
```

## Tokens custom (Solvyx-specific)

Si necesitas tokens que no encajan en Material 3 (ej. colores de estado emocional):

```kotlin
object SolvyxExtraColors {
    val SOSRed = Color(0xFFE24B4A)
    val WarningAmber = Color(0xFFD97706)
    val SuccessGreen = Color(0xFF15803D)
    val InfoBlue = Color(0xFF1E40AF)
    
    // Para cada uno, versión light y dark:
    val SOSRedDark = Color(0xFFE86765)
    val WarningAmberDark = Color(0xFFF59E0B)
}
```

Uso via `CompositionLocal`:

```kotlin
val LocalSolvyxColors = staticCompositionLocalOf { SolvyxExtraColors }

val MaterialTheme.solvyxColors: SolvyxExtraColors
    @Composable
    @ReadOnlyComposable
    get() = LocalSolvyxColors.current

// Uso:
Text("Estoy en crisis", color = MaterialTheme.solvyxColors.SOSRed)
```

## Anti-patrones prohibidos

1. **`Color(0xFF...)` en Composables.** Usar `MaterialTheme.colorScheme.*`.
2. **`FontSize(16.sp)` en Composables.** Usar `MaterialTheme.typography.*`.
3. **Material 2 (`androidx.compose.material.*`).** Migrar a Material 3.
4. **Color hardcoded pensando solo en un modo.** Verificar light y dark.
5. **Cambiar `MaterialTheme.colorScheme.primary` directamente.** Solo vía `ColorScheme` constructor.
6. **Olvidar `SolvyxTheme` en `@Preview`.** Pierdes contexto.
7. **`dynamicColor` siempre ON sin opción.** Ofrecer toggle.
8. **Shapes hardcoded `RoundedCornerShape(12.dp)`.** Usar `MaterialTheme.shapes.medium`.