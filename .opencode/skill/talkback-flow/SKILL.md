---
description: TalkBack flow en Solvyx. Navegación por gestos, focus order, anuncios de estado, headings, saltos rápidos, roles semánticos.
---

# Skill: TalkBack Flow

Esta skill te entrega los patrones para diseñar flujos TalkBack eficientes en Solvyx. Aplícala al crear o modificar pantallas, especialmente las críticas (crisis, ASSIST, bitácora, Berto).

## Principios

1. **Focus order = lectura visual.** Top → bottom, left → right.
2. **Headings navegables.** TalkBack permite saltar entre `heading()`.
3. **Landmarks para secciones.** `Role.Button`, `Role.Tab`, etc.
4. **Anuncios de estado claros.** "Seleccionado", "Expandido", "No disponible".
5. **Acciones explícitas.** Cada elemento clickeable anuncia su acción.
6. **No información solo por color.** Siempre texto o ícono con label.

## Gestos básicos de TalkBack

| Gesto | Acción |
|---|---|
| Swipe right | Siguiente elemento. |
| Swipe left | Elemento anterior. |
| Doble tap | Activar elemento. |
| Swipe up + right | Siguiente heading. |
| Swipe down + left | Anterior heading. |
| Swipe up + left | Siguiente landmark. |
| Swipe down + right | Anterior landmark. |

## Headings

Usa `semantics { heading() }` en títulos de sección para que TalkBack pueda saltar:

```kotlin
Text(
    text = "Ejercicios de respiración",
    style = MaterialTheme.typography.headlineMedium,
    modifier = Modifier.semantics { heading() }
)

Text(
    text = "Ejercicios de grounding",
    style = MaterialTheme.typography.headlineMedium,
    modifier = Modifier.semantics { heading() }
)
```

El usuario con TalkBack puede saltar entre estos títulos sin leer todo lo intermedio.

### Cuándo marcar como heading

- Títulos de pantalla (`Scaffold topBar` automáticamente).
- Títulos de sección principales.
- Títulos de cards importantes.
- Títulos de grupos de elementos.

NO marcar como heading:
- Etiquetas de inputs.
- Texto dentro de cards que no es un título.
- Botones (tienen su propio role).

## Landmarks

Roles semánticos para identificar tipos de elementos:

```kotlin
@Composable
fun MiBoton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.semantics { role = Role.Button }
    ) {
        Text("Aceptar")
    }
}
```

Compose aplica `Role.Button` automáticamente a `Button`, `Role.Checkbox` a `Checkbox`, etc.

### Roles disponibles

| Role | Uso |
|---|---|
| `Role.Button` | Acciones. |
| `Role.Checkbox` | Selección múltiple. |
| `Role.RadioButton` | Selección única. |
| `Role.Switch` | Toggle. |
| `Role.Tab` | Pestañas. |
| `Role.TabRow` | Contenedor de tabs. |
| `Role.DropdownMenu` | Menú desplegable. |
| `Role.Image` | Imagen significativa. |
| `Role.Slider` | Slider. |

## Focus traversal

### Orden automático vs manual

Compose calcula el orden de focus automáticamente según la posición en el layout. Generalmente coincide con la lectura visual.

### Cambiar el orden manualmente

```kotlin
@Composable
fun PantallaConFocusCustom() {
    // Primero el botón de pánico
    SolvyxButton(
        text = "Estoy en crisis",
        onClick = { /* ... */ },
        modifier = Modifier.semantics {
            // Primer foco al abrir la pantalla
            inTraversalIndex = 0f
        }
    )
    
    // Luego el contenido normal
    LazyColumn { /* ... */ }
}
```

Úsalo solo cuando el orden automático no coincida con la lectura lógica.

## Anuncios de estado

### `stateDescription`

```kotlin
@Composable
fun MiSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        modifier = Modifier.semantics {
            stateDescription = if (checked) "Activado" else "Desactivado"
        }
    )
}
```

### `toggleableStateDescription`

Para Checkbox y Switch:

```kotlin
Checkbox(
    checked = checked,
    onCheckedChange = onChange,
    modifier = Modifier.semantics {
        toggleableStateDescription = if (checked) "Seleccionado" else "No seleccionado"
    }
)
```

### `expanded` para acordeones

```kotlin
IconButton(
    onClick = { expanded = !expanded },
    modifier = Modifier.semantics {
        expanded = expanded
    }
) { /* ... */ }
```

## Acciones personalizadas

### `onClick` con label

```kotlin
@Composable
fun PlayButton(onPlay: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(
                onClickLabel = "Reproducir ejercicio",
                onClick = onPlay
            )
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_play),
            contentDescription = null
        )
    }
}
```

### Múltiples acciones en un nodo

```kotlin
@Composable
fun MensajeBurbuja(mensaje: ChatMessage) {
    Row(
        modifier = Modifier
            .semantics {
                // Anuncia el contenido
                contentDescription = "Mensaje de ${mensaje.autor}: ${mensaje.texto}"
            }
            .clickable(
                onClickLabel = "Leer mensaje en voz alta",
                onClick = { /* TTS */ }
            )
            .semantics {
                // Acción personalizada de TalkBack
                customActions = listOf(
                    CustomAccessibilityAction("Copiar") { /* ... */ ; true },
                    CustomAccessibilityAction("Compartir") { /* ... */ ; true }
                )
            }
    ) {
        Text(mensaje.texto)
    }
}
```

## Pantallas críticas en Solvyx

### GuiaEstoyEnCrisisScreen

Esta pantalla es la más sensible. TalkBack debe:

1. **Anunciar el título "Estoy en crisis"** (heading) primero.
2. **Anunciar el botón SOS prominente** con acción "Activar mi red de apoyo".
3. **Anunciar las líneas de ayuda** con números correctos.
4. **Permitir acceder al ejercicio 5-4-3-2-1** rápido.

```kotlin
Scaffold(
    topBar = {
        GuiaTopBar(title = "Estoy en crisis", onBack = onBack)
    }
) { padding ->
    Column(
        modifier = Modifier
            .padding(padding)
            .semantics {
                // El contenedor no es focusable
                containerDescription = null
            }
    ) {
        CrisisHero()  // Berto preocupado con speech bubble
        
        // El SOS es lo más importante
        Button(
            onClick = onSos,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    // Anuncio prioritario al entrar a la pantalla
                    inTraversalIndex = 1f
                    contentDescription = "Botón de emergencia. Activa tu red de apoyo ahora."
                }
        ) {
            Text("Avisar a mi red de apoyo ahora")
        }
        
        // Líneas de ayuda como landmarks
        HelpLineRow("Línea de la Vida", "800 911 2000")
        HelpLineRow("SAPTEL", "555 259 8121")
    }
}
```

### BertoScreen (chat)

El chat debe anunciar cada mensaje nuevo y permitir escuchar el contenido.

```kotlin
LazyColumn {
    items(messages, key = { it.id }) { message ->
        ChatBubble(
            message = message,
            modifier = Modifier.semantics {
                contentDescription = if (message.isFromBerto) {
                    "Berto dice: ${message.content}"
                } else {
                    "Tú dijiste: ${message.content}"
                }
            }
        )
    }
}
```

### ASSIST pantallas

Las preguntas ASSIST son largas. TalkBack debe leerlas completas.

```kotlin
@Composable
fun PreguntaScreen(pregunta: Pregunta) {
    Column {
        Text(
            text = pregunta.texto,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() }
        )
        
        // Opciones con role RadioButton
        pregunta.opciones.forEach { opcion ->
            Row(
                modifier = Modifier
                    .selectable(
                        selected = (opcion == seleccionada),
                        onClick = { /* ... */ },
                        role = Role.RadioButton
                    )
                    .semantics {
                        stateDescription = if (opcion == seleccionada) "Seleccionado" else "No seleccionado"
                    }
            ) {
                RadioButton(selected = opcion == seleccionada, onClick = null)
                Spacer(Modifier.width(8.dp))
                Text(opcion.texto)
            }
        }
    }
}
```

## Snackbar y anuncios efímeros

Snackbar se anuncia automáticamente por TalkBack. Para anuncios manuales:

```kotlin
@Composable
fun AnnounceOnChange(mensaje: String) {
    val context = LocalContext.current
    LaunchedEffect(mensaje) {
        // Enviar evento de accesibilidad
        val view = LocalView.current
        view.announceForAccessibility(mensaje)
    }
}
```

## Testing

### Habilitar TalkBack en tests

```kotlin
@OptIn(ExperimentalTestApi::class)
class TalkBackFlowTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun first_focus_is_sos_button() {
        composeTestRule.setContent {
            SolvyxTheme { GuiaEstoyEnCrisisScreen(/* ... */) }
        }
        
        composeTestRule.onAllNodesWithText("Avisar a mi red de apoyo ahora")
            .onFirst()
            .assertIsDisplayed()
    }
    
    @Test
    fun headings_are_marked() {
        composeTestRule.setContent {
            SolvyxTheme { EjerciciosScreen(/* ... */) }
        }
        
        composeTestRule.onAllNodes(hasAnySetTextStyle().and(hasClickAction().not()))
            .assertAll(hasContentDescription())  // o algo más específico
    }
}
```

### Manual

1. Habilitar TalkBack en emulador.
2. En la pantalla crítica, verificar:
   - ¿El foco inicial es el elemento más importante? (ej. botón SOS).
   - ¿Los headings son navegables con swipe up+right?
   - ¿El orden de lectura coincide con la lectura visual?
   - ¿Cada elemento clickeable anuncia su acción al recibir foco?
   - ¿Los estados (selected, expanded) se anuncian correctamente?

## Anti-patrones prohibidos

1. **Sin `heading()` en títulos principales.** TalkBack no puede saltar.
2. **Botones sin `onClickLabel`.** Acción ambigua.
3. **Estados no anunciados.** Usuario no sabe si algo está activo.
4. **Focus order incoherente** con lectura visual.
5. **Información solo por color.** TalkBack no ve color.
6. **Anuncios duplicados o contradictorios.**
7. **Focus automático al abrir la pantalla** sin razón.
8. **Imágenes sin `contentDescription` ni `null` explícito.**
9. **Datos numéricos no anunciados** (ej. "800 911 2000" se lee como un número enorme, mejor "Línea de la Vida, 800 911 2000").
10. **No probar con TalkBack real** antes de mergear.