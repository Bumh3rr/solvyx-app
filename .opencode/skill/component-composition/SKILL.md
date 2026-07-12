---
description: Composición de componentes en Solvyx. Cuándo extraer, cuándo dejar inline, props API, previews, slots API.
---

# Skill: Component Composition

Esta skill te entrega los patrones para componer y diseñar componentes reutilizables en Solvyx. Aplícala al crear o refactorizar Composables que se usan en múltiples pantallas.

## Principios

1. **Stateless por defecto.** Los componentes reciben estado, no lo mantienen.
2. **Props mínimos.** Solo lo necesario. No abstraer todo.
3. **Convención de nombres:** `Solvyx*` para componentes core; sin prefijo para internos.
4. **Previews obligatorias** con `SolvyxTheme`.
5. **Documentar con KDoc** props principales y comportamiento.
6. **Slots API** cuando hay contenido variable.

## Cuándo extraer un componente

| Caso | Acción |
|---|---|
| Mismo Composable aparece en ≥3 pantallas con misma estructura | **Extraer** a `ui/components/common/`. |
| Variante con un parámetro diferente | Usar variante de un componente existente. |
| Se usa 1-2 veces con estructura similar | Dejar inline o crear helper local. |
| Lógica no trivial con estado propio | Extraer. |
| Solo styling cosmético | Usar `Modifier` en sitio. |

## Anatomía de un componente core

### Header y KDoc

```kotlin
/**
 * Botón principal de Solvyx. Variantes: Primary, Secondary, Danger, Disabled.
 *
 * @param text Texto visible.
 * @param onClick Callback al tap.
 * @param modifier Modificadores externos.
 * @param enabled Si está habilitado.
 * @param variant Estilo visual.
 * @param icon Ícono opcional a la izquierda del texto.
 */
@Composable
fun SolvyxButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: SolvyxButtonVariant = SolvyxButtonVariant.Primary,
    icon: Painter? = null
) { /* ... */ }
```

### Props

**Reglas:**

1. **`text`, `onClick`, `modifier`** siempre primero (cuando aplica).
2. **`modifier`** siempre con default `Modifier`.
3. **Valores opcionales** al final.
4. **Callbacks tipados** (`(String) -> Unit`) en lugar de `Function<*>`.
5. **Enums para variantes** en lugar de `Boolean` flags.

### Modifier siempre último

```kotlin
@Composable
fun MiComponente(
    titulo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier  // último
) { /* ... */ }
```

### Previews

```kotlin
@Preview
@Composable
private fun SolvyxButtonPrimaryPreview() {
    SolvyxTheme {
        SolvyxButton(
            text = "Aceptar",
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun SolvyxButtonAllVariantsPreview() {
    SolvyxTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SolvyxButton("Primario", onClick = {}, variant = SolvyxButtonVariant.Primary)
            SolvyxButton("Secundario", onClick = {}, variant = SolvyxButtonVariant.Secondary)
            SolvyxButton("SOS", onClick = {}, variant = SolvyxButtonVariant.Danger)
            SolvyxButton("Deshabilitado", onClick = {}, enabled = false)
        }
    }
}
```

## Slots API

Cuando el componente tiene contenido variable, usa slots:

```kotlin
@Composable
fun SolvyxCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(SolvyxSpacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                leading?.invoke()
                if (title != null) {
                    Text(text = title, style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.weight(1f))
                trailing?.invoke()
            }
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            content()
        }
    }
}

// Uso:
SolvyxCard(
    title = "Mi ejercicio",
    subtitle = "3 minutos",
    leading = { Icon(Icons.Default.Timer, contentDescription = null) },
    trailing = { IconButton(onClick = {}) { Icon(Icons.Default.PlayArrow, contentDescription = null) } }
) {
    Text("Contenido del ejercicio...")
}
```

## Variantes

### Enum de variantes

```kotlin
enum class SolvyxCardVariant {
    Outlined,    // Solo borde
    Filled,      // Fondo surface
    Elevated     // Fondo + sombra
}
```

### Boolean flags solo si son 2

```kotlin
@Composable
fun SolvyxBadge(
    text: String,
    modifier: Modifier = Modifier,
    active: Boolean = false  // OK porque solo son 2 estados
)
```

## Defaults sensibles

```kotlin
@Composable
fun SolvyxButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: SolvyxButtonVariant = SolvyxButtonVariant.Primary,
    icon: Painter? = null  // opcional, default null
) { /* ... */ }
```

## Composición sobre herencia

NO uses clases para componentes:

```kotlin
// Mal
class MiBoton : View() { /* ... */ }

// Bien
@Composable
fun MiBoton(/* ... */) { /* ... */ }
```

## Wrapper vs custom

### Cuándo hacer wrapper de Material 3

- Si necesitas un comportamiento consistente del proyecto (variantes específicas).
- Si el componente se usa en ≥3 lugares con misma configuración.

```kotlin
// Wrapper de Button (Material 3) con defaults del proyecto
@Composable
fun SolvyxButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: SolvyxButtonVariant = SolvyxButtonVariant.Primary
) {
    val containerColor = when (variant) {
        SolvyxButtonVariant.Primary -> MaterialTheme.colorScheme.primary
        SolvyxButtonVariant.Secondary -> MaterialTheme.colorScheme.secondary
        SolvyxButtonVariant.Danger -> SolvyxExtraColors.SOSRed
        SolvyxButtonVariant.Disabled -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(SolvyxSpacing.touchTarget.comfortable),
        shape = RoundedCornerShape(SolvyxShapes.roundedButton),  // pill
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
```

### Cuándo hacer componente custom

- Si Material 3 no tiene nada similar.
- Si la interacción es muy específica del proyecto (ej. `BertoStateIndicator`).

## Organización

```
ui/components/
├── common/
│   ├── SolvyxButton.kt
│   ├── SolvyxOutlinedButton.kt
│   ├── SolvyxTextField.kt
│   ├── SolvyxCard.kt
│   ├── SolvyxBackButton.kt
│   ├── SolvyxStubTopBar.kt
│   ├── SolvyxBottomNavigationBar.kt
│   └── PageIndicator.kt
├── dialog/
│   └── SosConfirmationDialog.kt
├── drawer/
│   ├── CustomDrawer.kt
│   └── NavigationItemView.kt
└── navigation/
    └── SolvyxBottomNavigationBar.kt
```

## Documentación interna

Al inicio de cada archivo de componente, deja un comentario breve:

```kotlin
// ui/components/common/SolvyxButton.kt
//
// Botón principal de Solvyx. Usar en CTAs, formularios, y acciones primarias.
// Variantes: Primary (default), Secondary, Danger (solo SOS/crisis), Disabled.
//
// Ejemplo:
//   SolvyxButton("Registrar", onClick = { vm.guardar() })
//
```

## Testing

```kotlin
@OptIn(ExperimentalTestApi::class)
class SolvyxButtonTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun button_is_clickable_when_enabled() {
        var clicked = false
        composeTestRule.setContent {
            SolvyxTheme {
                SolvyxButton("Test", onClick = { clicked = true })
            }
        }
        
        composeTestRule.onNodeWithText("Test").performClick()
        
        assertTrue(clicked)
    }
    
    @Test
    fun button_is_not_clickable_when_disabled() {
        var clicked = false
        composeTestRule.setContent {
            SolvyxTheme {
                SolvyxButton("Test", onClick = { clicked = true }, enabled = false)
            }
        }
        
        composeTestRule.onNodeWithText("Test").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Test").performClick()
        
        assertFalse(clicked)
    }
}
```

## Anti-patrones prohibidos

1. **Componentes con estado mutable interno** que otros componentes necesitan leer.
2. **Componentes que toman el ViewModel como parámetro.**
3. **Componentes con demasiadas props** (>10). Dividir o usar slots.
4. **Hardcodear colores, fuentes, shapes** dentro de un componente core.
5. **Nombres genéricos** (`MyCard`, `CustomButton`). Usar prefijo `Solvyx*` para core.
6. **Componentes sin `@Preview`.**
7. **Componentes sin KDoc.**
8. **Duplicar lógica** entre componentes. Extraer helper.
9. **Composición por copy-paste.** Extraer.
10. **Componentes públicos sin `Solvyx*` prefix.**