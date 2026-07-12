---
description: Accesibilidad Android (a11y) para Solvyx. TalkBack, contentDescription, focus traversal, touch targets, WCAG AA. Carga al invocar ui-accessibility-i18n-auditor.
---

# Skill: Accessibility Android

Esta skill te entrega las convenciones para implementar accesibilidad en Android aplicado a Solvyx. Aplícala cada vez que crees o modifiques un Composable.

## Principios

1. **Toda imagen significativa** necesita `contentDescription`.
2. **Imágenes decorativas** necesitan `contentDescription = null` explícito.
3. **Touch targets ≥ 48dp x 48dp.**
4. **Contraste WCAG AA:** 4.5:1 texto, 3:1 texto grande/botones.
5. **Soporte de font scaling hasta 200%.**
6. **Anuncio de estado** (selected, expanded, disabled, etc.).
7. **Navegación lógica** con focus traversal.

## contentDescription

### Imagen significativa

```kotlin
Image(
    painter = painterResource(R.drawable.berto_saludando),
    contentDescription = "Berto saludando"
)
```

### Imagen decorativa

```kotlin
Image(
    painter = painterResource(R.drawable.decoracion_hero),
    contentDescription = null  // explícito, no null implícito
)
```

### Ícono en botón

```kotlin
IconButton(onClick = { /* ... */ }) {
    Icon(
        painter = painterResource(R.drawable.ic_close),
        contentDescription = "Cerrar"
    )
}
```

### Ícono decorativo junto a texto

Si el ícono está junto a texto que ya describe la acción, el ícono es decorativo:

```kotlin
Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
        painter = painterResource(R.drawable.ic_check),
        contentDescription = null  // decorativo, el texto ya describe
    )
    Spacer(Modifier.width(8.dp))
    Text("Completado")
}
```

### Imagen con información en texto cercano

Si el texto cercano ya describe lo que la imagen muestra, la imagen es decorativa:

```kotlin
Column {
    Image(
        painter = painterResource(R.drawable.illustration_crisis),
        contentDescription = null
    )
    Text(
        text = "Estoy en crisis. Aquí te acompañamos.",
        style = MaterialTheme.typography.bodyLarge
    )
}
```

## Touch targets

### Mínimo 48dp x 48dp

```kotlin
@Composable
fun MyIconButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)  // explícito
    ) {
        Icon(painter = icon, contentDescription = contentDescription)
    }
}
```

O usando `minimumInteractiveComponentSize()`:

```kotlin
IconButton(onClick = onClick) {
    Icon(
        painter = icon,
        contentDescription = contentDescription,
        modifier = Modifier.size(24.dp)
    )
}
```

`IconButton` ya aplica `minimumInteractiveComponentSize` por defecto.

### Clickable Modifier con target pequeño

```kotlin
Box(
    modifier = Modifier
        .size(24.dp)
        .clickable(
            onClickLabel = "Reproducir audio",  // label para TalkBack
            onClick = { /* ... */ }
        )
)
```

## Focus traversal

### Orden lógico

El focus order debe coincidir con la lectura visual (top → bottom, left → right). Compose lo maneja automáticamente si los Composables están en orden en el código.

### `Modifier.semantics`

Para agrupar elementos relacionados:

```kotlin
Row(
    modifier = Modifier.semantics(mergeDescendants = true) {
        contentDescription = "Mensaje de ${usuario.nombre}, ${usuario.fecha}"
    }
) {
    Image(painter = painterResource(R.drawable.avatar), contentDescription = null)
    Column {
        Text(usuario.nombre)
        Text(usuario.fecha)
    }
}
```

### Anuncio de estado

```kotlin
Checkbox(
    checked = checked,
    onCheckedChange = { /* ... */ },
    modifier = Modifier.semantics {
        stateDescription = if (checked) "Seleccionado" else "No seleccionado"
    }
)
```

### Headings (para navegación rápida con TalkBack)

```kotlin
Text(
    text = "Mis ejercicios",
    style = MaterialTheme.typography.headlineMedium,
    modifier = Modifier.semantics { heading() }
)
```

Esto permite a TalkBack saltar entre encabezados.

## Contraste

### Verificar pares

| Elemento | Color texto | Color fondo | Ratio mínimo |
|---|---|---|---|
| Texto principal | `onSurface` | `surface` | 4.5:1 |
| Texto secundario | `onSurfaceVariant` | `surface` | 4.5:1 |
| Botón primario | `onPrimary` | `primary` | 4.5:1 |
| Botón SOS | blanco | `SOSRed` | 4.5:1 |
| Texto sobre hero | blanco | `primary` | 4.5:1 |

### Herramientas

- **WebAIM Contrast Checker:** webaim.org/resources/contrastchecker
- **Android Studio:** Layout Inspector → muestra los colores.
- **Manual:** calcular ratio L1/L2 con WCAG formula.

### Solución para problemas comunes

```kotlin
// Si onSurfaceVariant no alcanza contraste:
Text(
    text = "Texto secundario",
    color = MaterialTheme.colorScheme.onSurface  // usar onSurface normal
)

// Si un color semitransparente falla:
Text(
    text = "Importante",
    color = MaterialTheme.colorScheme.error  // más fuerte
)
```

## Soporte de font scaling

### `sp` no `dp` para texto

```kotlin
Text(
    text = "Hola",
    fontSize = 16.sp  // escala con fontSize del usuario
)

Text(
    text = "Hola",
    fontSize = 16.dp  // MAL: no escala
)
```

### `maxLines` y `overflow`

Con font scaling alto, los textos pueden no caber:

```kotlin
Text(
    text = "Texto largo que podría no caber",
    maxLines = 2,
    overflow = TextOverflow.Ellipsis
)
```

### Evitar tamaños fijos para textos

```kotlin
// MAL: rompe con fontScale 2.0
Box(modifier = Modifier.height(40.dp)) {
    Text("Nombre del usuario", fontSize = 14.sp)
}

// BIEN: usa wrapContentHeight
Text("Nombre del usuario", fontSize = 14.sp, modifier = Modifier.wrapContentHeight())
```

### Soporte hasta 200%

Probar con `Settings → Display → Font size → Largest` o emulador con `fontScale = 2f`.

```kotlin
@Preview(fontScale = 2f)
@Composable
private fun PantallaFontScale2Preview() {
    SolvyxTheme {
        PantallaScreen()
    }
}
```

## Acciones accesibles

### `onClickLabel`

Da contexto a TalkBack sobre qué hace el click:

```kotlin
IconButton(
    onClick = { /* play */ }
) {
    Icon(
        painter = painterResource(R.drawable.ic_play),
        contentDescription = null,  // contenido en onClickLabel
        modifier = Modifier.semantics {
            onClick(label = "Reproducir ejercicio") {
                true  // consumido
            }
        }
    )
}
```

O más simple:

```kotlin
Box(
    modifier = Modifier.clickable(
        onClickLabel = "Reproducir ejercicio",
        onClick = { /* ... */ }
    )
) {
    Icon(painter = painterResource(R.drawable.ic_play), contentDescription = null)
}
```

## Roles semánticos

Compose tiene roles semánticos para describir la función:

```kotlin
Switch(
    checked = activado,
    onCheckedChange = { /* ... */ },
    modifier = Modifier.semantics { role = Role.Switch }
)
```

Roles disponibles: `Button`, `Checkbox`, `Switch`, `RadioButton`, `Tab`, `Image`, `DropdownMenu`, etc.

## Anuncios (snackbar)

Para mensajes importantes que TalkBack debe leer:

```kotlin
val snackbarHostState = remember { SnackbarHostState() }

LaunchedEffect(mensaje) {
    if (mensaje.isNotBlank()) {
        snackbarHostState.showSnackbar(
            message = mensaje,
            // TalkBack leerá esto automáticamente
        )
    }
}
```

Para anuncios fuera de snackbar:

```kotlin
val context = LocalContext.current
LaunchedEffect(anuncio) {
    val vibrator = context.getSystemService<Vibrator>()
    vibrator?.vibrate(100)
    // Para Compose, usa TalkBack announcement:
    // (no hay API directa, pero accessibilityEvent se puede enviar via View)
}
```

## Testing

### Compose UI Test con TalkBack

```kotlin
@OptIn(ExperimentalTestApi::class)
class AccessibilityTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun every_image_has_content_description() {
        composeTestRule.setContent {
            SolvyxTheme { PantallaScreen() }
        }
        
        composeTestRule.onAllNodes(isImage())
            .assertAll(hasContentDescription())
    }
    
    @Test
    fun clickable_nodes_meet_minimum_size() {
        composeTestRule.setContent { SolvyxTheme { PantallaScreen() } }
        
        composeTestRule.onAllNodes(hasClickAction())
            .assertAll(hasMinimumTouchTargetSize(48.dp))
    }
}
```

### TalkBack manual en emulador

1. Habilitar TalkBack: Settings → Accessibility → TalkBack → On.
2. Explorar la pantalla con swipe.
3. Verificar que cada elemento anuncia algo útil.
4. Verificar que el orden tiene sentido.

## Anti-patrones prohibidos

1. **`Image` sin `contentDescription`** (ni null ni texto).
2. **Click target < 48dp sin wrap en `clickable` con label.**
3. **Texto con `dp` en lugar de `sp`.**
4. **`maxLines = 1` sin `overflow` definido.**
5. **Colores hardcoded** que no verifican contraste.
6. **Estado de UI no anunciado** (selected/expanded).
7. **Decorar elementos como focusables** sin razón.
8. **Sin `onClickLabel`** en `clickable`.
9. **Color como único indicador** (ej. error solo en rojo).
10. **Layouts que rompen con fontScale 2.0.** Probar siempre.