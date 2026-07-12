---
description: Font scaling en Compose para Solvyx. Soporte hasta 200%, prevención de overflow, maxLines, layout adaptativo.
---

# Skill: Font Scaling

Esta skill te entrega los patrones para soportar font scaling (tamaño de letra del usuario) en Solvyx sin romper layouts. Aplícala cada vez que crees pantallas con texto.

## Principios

1. **Texto siempre en `sp`,** nunca `dp`.
2. **Probar con fontScale 1.0, 1.3 y 2.0.**
3. **`maxLines` + `overflow` en textos largos** dentro de cards o filas.
4. **Layouts flexibles** que se adapten al alto del texto.
5. **Sin tamaños fijos para textos** en altura (`Modifier.height(40.dp)` con texto adentro).
6. **Botones pueden crecer** en alto cuando el texto crece.
7. **Scroll vertical** cuando el contenido es largo.

## ¿Por qué importa?

Android permite al usuario configurar fontScale de 0.85 a 2.0 (aprox). Esto afecta todo texto en `sp`. Si un layout tiene tamaños fijos, el texto se desborda o se corta.

### Configuración del usuario

Settings → Display → Font size → Small / Default / Large / Largest.

### Programáticamente

```kotlin
@Composable
fun PreviewFontScale() {
    CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
        Text("Texto escalado al 200%")
    }
}
```

## Reglas de oro

### 1. `sp` siempre para texto

```kotlin
Text(
    text = "Hola",
    fontSize = 16.sp  // escala con fontScale del usuario
)

Text(
    text = "Hola",
    fontSize = 16.dp  // MAL: no escala, layout rompe
)
```

### 2. `wrapContentHeight` para contenedores con texto

```kotlin
// MAL: si el texto crece, se corta
Box(modifier = Modifier.height(48.dp)) {
    Text("Texto")
}

// BIEN: se adapta al texto
Box(modifier = Modifier.wrapContentHeight()) {
    Text("Texto")
}
```

### 3. `maxLines` + `overflow` en textos dentro de cards/filas

```kotlin
Text(
    text = "Descripción larga del producto que podría no caber en una línea",
    maxLines = 2,
    overflow = TextOverflow.Ellipsis
)
```

### 4. Botones con alto flexible

```kotlin
SolvyxButton(
    text = "Registrar",
    onClick = {},
    modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp)  // mínimo, pero puede crecer
)
```

NO uses `.height(56.dp)` fijo en botones; usa `.heightIn(min = 56.dp)`.

### 5. Scroll vertical cuando hay mucho contenido

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
) {
    // contenido largo
}
```

## Casos específicos

### Cards con título y descripción

```kotlin
@Composable
fun TarjetaConDescripcion(
    titulo: String,
    descripcion: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
```

Con fontScale alto, el texto ocupa más líneas pero el card crece naturalmente.

### Bottom navigation

```kotlin
@Composable
fun BottomNavItem(label: String, icon: ImageVector, selected: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
```

Con fontScale alto, el label puede no caber. Usar `maxLines = 1` con ellipsis es aceptable aquí.

### Dialog con texto largo

```kotlin
@Composable
fun ConfirmDialog(
    titulo: String,
    mensaje: String,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(titulo) },
        text = {
            Text(
                mensaje,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            SolvyxButton(text = "Confirmar", onClick = onConfirm)
        }
    )
}
```

`AlertDialog` se adapta al texto automáticamente.

### Hero con Berto

```kotlin
@Composable
fun HeroConBerto() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.berto_saludando),
            contentDescription = null,
            modifier = Modifier.size(130.dp)  // fijo OK para ícono
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Hola, Alex",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }
}
```

El ícono de Berto puede tener tamaño fijo. El texto se adapta.

### Lists con texto variable

```kotlin
LazyColumn {
    items(items, key = { it.id }) { item ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(item.icon),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
```

Con fontScale alto, el texto crece verticalmente. El Row se adapta porque `weight(1f)` ocupa el espacio restante.

## Previews con fontScale

```kotlin
@Preview(name = "100%", showBackground = true)
@Composable
private fun PreviewNormal() {
    SolvyxTheme {
        PantallaScreen()
    }
}

@Preview(name = "130%", showBackground = true, fontScale = 1.3f)
@Composable
private fun PreviewGrande() {
    SolvyxTheme {
        PantallaScreen()
    }
}

@Preview(name = "200%", showBackground = true, fontScale = 2f)
@Composable
private fun PreviewMuyGrande() {
    SolvyxTheme {
        PantallaScreen()
    }
}
```

## Testing

### Manual en emulador

1. Abrir la app.
2. Settings → Display → Font size → Largest.
3. Volver a la app. Verificar:
   - ¿El texto se ve completo?
   - ¿Los botones son clickeables?
   - ¿Los cards no se rompen?
   - ¿El scroll funciona?

### Compose UI Test

```kotlin
@OptIn(ExperimentalTestApi::class)
class FontScalingTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun text_does_not_overflow_at_200_percent() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 2f)) {
                SolvyxTheme {
                    PantallaConTextoLargo()
                }
            }
        }
        
        composeTestRule.onNodeWithText("Texto largo esperado")
            .assertIsDisplayed()  // no se corta
    }
}
```

## Errores comunes

| Error | Solución |
|---|---|
| Texto cortado en card | `maxLines = N` + `overflow = TextOverflow.Ellipsis`. |
| Botón muy chico para el texto | `heightIn(min = 56.dp)` en lugar de `height(56.dp)`. |
| `dp` para fontSize | Cambiar a `sp`. |
| Layout que no crece con texto | `wrapContentHeight()` o `heightIn(min)`. |
| Texto que rompe card | Aplicar `maxLines` o reestructurar layout. |
| Bottom nav item que se desborda | `maxLines = 1` + ellipsis. |
| Título que se sale del header | `maxLines = 2` en lugar de 1. |

## Anti-patrones prohibidos

1. **Texto con `dp` en lugar de `sp`.**
2. **`Modifier.height()` fijo en contenedores con texto.**
3. **`maxLines = 1` sin `overflow`.**
4. **Layouts que no se adaptan** al alto del texto.
5. **Cards con texto sin scroll** cuando el contenido es largo.
6. **No probar con fontScale >1.3.**
7. **`FontSize(16.dp)` en lugar de `fontSize = 16.sp`.**
8. **Asumir que fontScale = 1.0 siempre.** Usuario puede cambiar.