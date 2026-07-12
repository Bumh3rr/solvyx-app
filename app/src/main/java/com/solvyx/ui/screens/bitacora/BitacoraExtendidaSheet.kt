package com.solvyx.ui.screens.bitacora

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solvyx.R
import com.solvyx.backend.presentation.viewmodel.BitacoraExtendidaEffect
import com.solvyx.backend.presentation.viewmodel.BitacoraExtendidaViewModel
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * BottomSheet de **Bitácora extendida**.
 *
 * Se abre desde [RegistroEmocionalScreen] con un trigger (FAB o botón)
 * y monta un [ModalBottomSheet] con todos los campos opcionales:
 * sueño, comida, actividad física, contexto social, detonante, ansiedad,
 * craving, ejercicio físico y nota privada.
 *
 * Todos los campos son **opcionales**. El VM valida solo los básicos
 * (estado de ánimo + consumo) ya prefilled por el flow principal; este
 * sheet se enfoca en `onGuardar` que persiste el entry completo.
 *
 * Si el guardado tiene éxito, emite [BitacoraExtendidaEffect.Saved] y
 * cerramos el sheet automáticamente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitacoraExtendidaSheet(
    onDismiss: () -> Unit,
    viewModel: BitacoraExtendidaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effects.collectAsStateWithLifecycle(initialValue = null)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Cierra el sheet cuando el VM emite Saved.
    LaunchedEffect(effect) {
        when (effect) {
            is BitacoraExtendidaEffect.Saved -> onDismiss()
            is BitacoraExtendidaEffect.ShowMessage -> { /* host snackbar ausente */ }
            null -> Unit
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SolvyxSpacing.lg)
                .padding(bottom = SolvyxSpacing.xl)
        ) {
            // Header
            Text(
                text = stringResource(R.string.bitacora_extendida_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.bitacora_extendida_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(SolvyxSpacing.lg))
            HorizontalDivider(thickness = 0.5.dp)

            // Scrollable form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(top = SolvyxSpacing.md)
            ) {
                // Sueño
                SeccionLabel(stringResource(R.string.bitacora_extendida_sueno))
                val suenoA11y = stringResource(R.string.bitacora_extendida_sueno) +
                    ", " + stringResource(R.string.bitacora_extendida_horas)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = state.suenoHoras?.toString().orEmpty(),
                        onValueChange = { v ->
                            val n = v.toIntOrNull()
                            viewModel.setSuenoHoras(n)
                        },
                        modifier = Modifier
                            .width(96.dp)
                            .semantics { contentDescription = suenoA11y },
                        label = { Text(stringResource(R.string.bitacora_extendida_horas)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = bitacoraFieldColors()
                    )
                    Spacer(Modifier.width(SolvyxSpacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.bitacora_extendida_calidad),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Rating1to5(
                            value = state.suenoCalidad,
                            onChange = viewModel::setSuenoCalidad
                        )
                    }
                }

                Spacer(Modifier.height(SolvyxSpacing.lg))

                // Comida
                SeccionLabel(stringResource(R.string.bitacora_extendida_comida))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.bitacora_extendida_comio),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (state.comio == true) stringResource(R.string.bitacora_extendida_si)
                               else stringResource(R.string.bitacora_extendida_no),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(SolvyxSpacing.sm))
                    Switch(
                        checked = state.comio == true,
                        onCheckedChange = { viewModel.setComio(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                if (state.comio == true) {
                    Spacer(Modifier.height(SolvyxSpacing.sm))
                    Rating1to5(
                        value = state.calidadComida,
                        onChange = viewModel::setCalidadComida,
                        label = stringResource(R.string.bitacora_extendida_calidad)
                    )
                }

                Spacer(Modifier.height(SolvyxSpacing.lg))

                // Actividad física
                SeccionLabel(stringResource(R.string.bitacora_extendida_actividad))
                OpcionesRadio(
                    opciones = listOf(
                        "nada" to stringResource(R.string.bitacora_extendida_actividad_nada),
                        "ligera" to stringResource(R.string.bitacora_extendida_actividad_ligera),
                        "moderada" to stringResource(R.string.bitacora_extendida_actividad_moderada),
                        "intensa" to stringResource(R.string.bitacora_extendida_actividad_intensa)
                    ),
                    seleccionado = state.actividadFisica,
                    onSelect = viewModel::setActividadFisica
                )

                Spacer(Modifier.height(SolvyxSpacing.lg))

                // Contexto social
                SeccionLabel(stringResource(R.string.bitacora_extendida_contexto))
                OpcionesRadio(
                    opciones = listOf(
                        "solo" to stringResource(R.string.bitacora_extendida_contexto_solo),
                        "familia" to stringResource(R.string.bitacora_extendida_contexto_familia),
                        "amigos" to stringResource(R.string.bitacora_extendida_contexto_amigos),
                        "trabajo" to stringResource(R.string.bitacora_extendida_contexto_trabajo)
                    ),
                    seleccionado = state.contextoSocial,
                    onSelect = viewModel::setContextoSocial
                )

                Spacer(Modifier.height(SolvyxSpacing.lg))

                // Detonante principal
                SeccionLabel(stringResource(R.string.bitacora_extendida_detonante))
                OutlinedTextField(
                    value = state.detonantePrincipal.orEmpty(),
                    onValueChange = viewModel::setDetonantePrincipal,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            stringResource(R.string.bitacora_extendida_detonante_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = bitacoraFieldColors()
                )

                Spacer(Modifier.height(SolvyxSpacing.lg))

                // Nivel ansiedad (slider)
                SeccionLabel(
                    stringResource(
                        R.string.bitacora_extendida_ansiedad_label,
                        state.nivelAnsiedad ?: 0
                    )
                )
                Slider(
                    value = (state.nivelAnsiedad ?: 0).toFloat(),
                    onValueChange = { viewModel.setNivelAnsiedad(it.toInt()) },
                    valueRange = 0f..10f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(Modifier.height(SolvyxSpacing.lg))

                // Switches craving + ejercicio
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.bitacora_extendida_craving),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = state.tuvoCraving == true,
                        onCheckedChange = { viewModel.setTuvoCraving(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(Modifier.height(SolvyxSpacing.sm))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.bitacora_extendida_ejercicio),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = state.ejercicioFisico == true,
                        onCheckedChange = { viewModel.setEjercicioFisico(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(Modifier.height(SolvyxSpacing.lg))

                // Nota privada
                SeccionLabel(stringResource(R.string.bitacora_extendida_nota))
                OutlinedTextField(
                    value = state.notaPrivada.orEmpty(),
                    onValueChange = viewModel::setNotaPrivada,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = {
                        Text(
                            stringResource(R.string.bitacora_extendida_nota_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = bitacoraFieldColors(),
                    maxLines = 6
                )

                if (state.error != null) {
                    Spacer(Modifier.height(SolvyxSpacing.sm))
                    Text(
                        text = state.error.orEmpty(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(SolvyxSpacing.lg))
            }

            // CTA sticky
            SolvyxButton(
                text = if (state.guardando) stringResource(R.string.action_saving)
                       else stringResource(R.string.action_save),
                onClick = viewModel::onGuardar,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.guardando,
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_save),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            Spacer(Modifier.height(SolvyxSpacing.sm))
        }
    }
}

// ── Helpers ─────────────────────────────────────────────────────────────────

@Composable
private fun SeccionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .padding(bottom = SolvyxSpacing.sm)
            .semantics { heading() }
    )
}

/**
 * Selector de calificación 1..5.
 *
 * Cada opción es un círculo clickeable con touch target ≥ 48dp
 * (defaultMinSize) y anuncia la selección actual con `stateDescription`.
 * Antes era solo visual: el usuario no podía seleccionar nada. Ahora
 * también es interactivo.
 */
@Composable
private fun Rating1to5(
    value: Int?,
    onChange: (Int) -> Unit,
    label: String? = null
) {
    val selectedLabel = stringResource(R.string.state_selected)
    val notSelectedLabel = stringResource(R.string.state_not_selected)
    Column(modifier = Modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = SolvyxSpacing.xs)
            )
        }
        if (value != null) {
            Text(
                text = stringResource(R.string.bitacora_extendida_calidad_label, value),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = SolvyxSpacing.xs)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SolvyxSpacing.xs)
        ) {
            for (i in 1..5) {
                val selected = value == i
                val clickLabel = stringResource(
                    R.string.bitacora_extendida_calidad_seleccionar, i
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                        // defaultMinSize garantiza touch target ≥ 48dp.
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .clickable(
                            onClick = { onChange(i) },
                            onClickLabel = clickLabel
                        )
                        .semantics(mergeDescendants = true) {
                            stateDescription = if (selected) selectedLabel else notSelectedLabel
                            role = Role.RadioButton
                            contentDescription = clickLabel
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = i.toString(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Grupo de opciones con `RadioButton` visual. La fila completa es
 * tappable (touch target ≥ 48dp) para que el área clickeable no quede
 * reducida al círculo del radio. Cada fila anuncia el estado
 * (seleccionada / no seleccionada) vía `stateDescription`.
 */
@Composable
private fun OpcionesRadio(
    opciones: List<Pair<String, String>>,
    seleccionado: String?,
    onSelect: (String) -> Unit
) {
    val selectedLabel = stringResource(R.string.state_selected)
    val notSelectedLabel = stringResource(R.string.state_not_selected)
    Column(verticalArrangement = Arrangement.spacedBy(SolvyxSpacing.xs)) {
        opciones.forEach { (key, label) ->
            val selected = seleccionado == key
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // defaultMinSize: touch target accesible.
                    .defaultMinSize(minHeight = 48.dp)
                    .clickable(
                        onClick = { onSelect(key) },
                        onClickLabel = stringResource(R.string.bitacora_extendida_opcion_label, label)
                    )
                    .semantics(mergeDescendants = true) {
                        stateDescription = if (selected) selectedLabel else notSelectedLabel
                        role = Role.RadioButton
                    }
                    .padding(vertical = SolvyxSpacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected,
                    // onClick vacío: la fila completa ya maneja el tap.
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.outline
                    )
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun bitacoraFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceDim,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceDim,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
)

/**
 * Wrapper que muestra/oculta el [BitacoraExtendidaSheet] según `visible`.
 * Útil para conectarlo a un `remember { mutableStateOf(false) }` desde
 * el `RegistroEmocionalScreen`.
 */
@Composable
fun BitacoraExtendidaLauncher(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (visible) {
        BitacoraExtendidaSheet(onDismiss = onDismiss)
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun BitacoraExtendidaFormPreview() {
    SolvyxappTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(SolvyxSpacing.lg)
        ) {
            SeccionLabel(stringResource(R.string.bitacora_extendida_sueno))
            Rating1to5(value = 3, onChange = {})
            Spacer(Modifier.height(SolvyxSpacing.lg))
            SeccionLabel("Actividad física")
            OpcionesRadio(
                opciones = listOf("nada" to "Nada", "ligera" to "Ligera"),
                seleccionado = "ligera",
                onSelect = {}
            )
        }
    }
}
