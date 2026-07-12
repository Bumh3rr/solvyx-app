package com.solvyx.ui.screens.journaling

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solvyx.R
import com.solvyx.backend.presentation.viewmodel.JournalingEditorEffect
import com.solvyx.backend.presentation.viewmodel.JournalingEditorViewModel
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.theme.SolvyxSpacing
import com.solvyx.ui.theme.SolvyxappTheme

/**
 * Editor de entradas de **journaling**.
 *
 * - Si el VM trae `promptTexto` precargado, pintamos la cabecera con la
 *   pregunta en un card "prompt" arriba.
 * - En cualquier caso, hay un `OutlinedTextField` multilínea para escribir.
 * - Botones: **Cancelar** (X en top bar) y **Guardar** (sticky al fondo
 *   y también como acción del top bar).
 */
@Composable
fun JournalingEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: JournalingEditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effects.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(effect) {
        when (effect) {
            JournalingEditorEffect.Cerrar -> onNavigateBack()
            is JournalingEditorEffect.ShowMessage -> { /* no-op */ }
            null -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EditorTopBar(
            guardando = state.guardando,
            onCancelar = viewModel::onCancelar,
            onGuardar = viewModel::onGuardar
        )

        if (state.guardando) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
        } else {
            EditorContent(
                promptTexto = state.promptTexto,
                contenido = state.contenido,
                onContenidoChange = viewModel::onContenidoChange,
                onGuardar = viewModel::onGuardar,
                onCancelar = viewModel::onCancelar,
                error = state.error
            )
        }
    }
}

@Composable
private fun EditorTopBar(
    guardando: Boolean,
    onCancelar: () -> Unit,
    onGuardar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = SolvyxSpacing.xs, vertical = SolvyxSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCancelar, modifier = Modifier.size(48.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_circle_x),
                contentDescription = stringResource(R.string.action_cancel),
                tint = Color.White
            )
        }
        Text(
            text = stringResource(R.string.journaling_editor_title),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        TextButton(
            onClick = onGuardar,
            enabled = !guardando,
            // Anuncia a TalkBack que el botón está deshabilitado mientras
            // se está guardando (cumple WCAG 4.1.2).
            modifier = Modifier.semantics { role = Role.Button }
        ) {
            Text(
                text = if (guardando) stringResource(R.string.action_saving)
                       else stringResource(R.string.action_save),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }
    }
}

@Composable
private fun EditorContent(
    promptTexto: String?,
    contenido: String,
    onContenidoChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
    error: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SolvyxSpacing.lg)
            .padding(top = SolvyxSpacing.md, bottom = SolvyxSpacing.lg)
    ) {
        // Prompt opcional (card cabecera)
        if (!promptTexto.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(SolvyxSpacing.md)
            ) {
                Text(
                    text = promptTexto,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.height(SolvyxSpacing.md))
        }

        // TextField multi-línea (no usamos SolvyxTextField porque ese es single-line).
        OutlinedTextField(
            value = contenido,
            onValueChange = onContenidoChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
            placeholder = {
                Text(
                    text = if (promptTexto.isNullOrBlank())
                        stringResource(R.string.journaling_editor_placeholder_libre)
                    else
                        stringResource(R.string.journaling_editor_placeholder_prompt),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceDim,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceDim,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            maxLines = 10,
            textStyle = MaterialTheme.typography.bodyLarge
        )

        // Contador + error
        Spacer(Modifier.height(SolvyxSpacing.sm))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.journaling_editor_chars, contenido.length),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (error != null) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(Modifier.height(SolvyxSpacing.md))
        Text(
            text = stringResource(R.string.journaling_editor_privacidad_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.weight(1f))

        // Botones inferiores
        SolvyxButton(
            text = stringResource(R.string.action_save),
            onClick = onGuardar,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_save),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            },
            enabled = contenido.isNotBlank()
        )
        Spacer(Modifier.height(SolvyxSpacing.sm))
        SolvyxOutlinedButton(
            text = stringResource(R.string.action_cancel),
            onClick = onCancelar,
            modifier = Modifier.fillMaxWidth(),
            borderColor = MaterialTheme.colorScheme.outline,
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun EditorContentConPromptPreview() {
    SolvyxappTheme {
        EditorContent(
            promptTexto = "Hoy lo mejor fue…",
            contenido = "Hoy terminé mi rutina matutina completa y me sentí con energía.",
            onContenidoChange = {},
            onGuardar = {},
            onCancelar = {},
            error = null
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditorContentLibrePreview() {
    SolvyxappTheme {
        EditorContent(
            promptTexto = null,
            contenido = "",
            onContenidoChange = {},
            onGuardar = {},
            onCancelar = {},
            error = null
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditorContentConErrorPreview() {
    SolvyxappTheme {
        EditorContent(
            promptTexto = null,
            contenido = "",
            onContenidoChange = {},
            onGuardar = {},
            onCancelar = {},
            error = "Escribe algo antes de guardar."
        )
    }
}
