package com.solvyx.ui.screens.red

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.R
import com.solvyx.backend.data.local.entity.ContactoSosEntity
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealLightest
import com.solvyx.ui.theme.TealMedium

@Composable
fun RedApoyoScreen(
    isSetupMode: Boolean,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    onFinishSetup: () -> Unit,
    viewModel: RedApoyoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary
    val background = MaterialTheme.colorScheme.background
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    LaunchedEffect(viewModel.savedSuccessfully) {
        if (viewModel.savedSuccessfully && !isSetupMode) {
            Toast.makeText(context, "Contactos guardados", Toast.LENGTH_SHORT).show()
            viewModel.resetSaved()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(background)) {

        Column(Modifier.fillMaxSize()) {

            // ── TOP BAR ─────────────────────────────────────────────────────
            if (isSetupMode) {
                Column(
                    Modifier.fillMaxWidth().background(primary).statusBarsPadding()
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_arrow_left),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            "Red de apoyo",
                            Modifier.weight(1f).padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "1 de 1",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TealLight
                        )
                    }
                    LinearProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = TealLightest,
                        trackColor = TealMedium
                    )
                }
            } else {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(primary)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            painterResource(R.drawable.ic_menu),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        "Mi Red de apoyo",
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.size(48.dp))
                }
            }

            // ── BERTO PEEK STRIP ─────────────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(background)
                    .drawBehind {
                        val sw = 1.dp.toPx()
                        drawLine(
                            color = Color(0xFFD9EBE3),
                            start = Offset(0f, size.height - sw / 2),
                            end = Offset(size.width, size.height - sw / 2),
                            strokeWidth = sw
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painterResource(R.drawable.berto_saludando),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.width(12.dp))
                SpeechBubble(
                    text = if (isSetupMode)
                        "Si presionas SOS, les avisaré por SMS."
                    else
                        "Puedes cambiar tus contactos cuando quieras."
                )
            }

            // ── SCROLLABLE CONTENT ──────────────────────────────────────────
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 22.dp, bottom = 120.dp)
            ) {
                if (isSetupMode) {
                    Column(
                        Modifier.fillMaxWidth().padding(bottom = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                            Box(
                                Modifier.fillMaxSize().clip(CircleShape)
                                    .background(TealLight.copy(alpha = 0.35f))
                            )
                            Icon(
                                painterResource(R.drawable.ic_people),
                                contentDescription = null,
                                tint = primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "¿Quién estará contigo?",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = TealDark,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Elige quién recibirá un SMS si presionas el botón de crisis.\nSolo ellos sabrán.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6E8682),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    viewModel.contactos.forEachIndexed { index, contacto ->
                        ContactCard(
                            index = index,
                            contacto = contacto,
                            isRequired = index == 0,
                            onContactoChange = { viewModel.setContacto(index, it) },
                            onRemove = { viewModel.removeContacto(index) }
                        )
                    }

                    if (viewModel.contactos.size < 3) {
                        val dashColor = primary
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawBehind {
                                    drawRoundRect(
                                        color = dashColor,
                                        style = Stroke(
                                            width = 1.5.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(
                                                floatArrayOf(8f, 6f), 0f
                                            )
                                        ),
                                        cornerRadius = CornerRadius(16.dp.toPx())
                                    )
                                }
                                .clickable { viewModel.addContacto() }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_add_user),
                                    contentDescription = null,
                                    tint = primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "Añadir otro contacto (opcional)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── FIXED BOTTOM BAR ────────────────────────────────────────────────
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .drawBehind {
                    val sw = 1.dp.toPx()
                    drawLine(
                        color = Color(0xFFD9EBE3),
                        start = Offset(0f, sw / 2),
                        end = Offset(size.width, sw / 2),
                        strokeWidth = sw
                    )
                }
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isSetupMode)
                    "Podrás cambiar estos contactos en cualquier momento"
                else
                    "El contacto principal es obligatorio para activar el SOS",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6E8682),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            val buttonEnabled = viewModel.canSave() && !viewModel.isSaving

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (buttonEnabled) primary else Color(0xFFB6D7CB))
                    .clickable(enabled = buttonEnabled) { viewModel.guardar() },
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.isSaving) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.5.dp
                        )
                        Text(
                            "Guardando…",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.White
                        )
                    }
                } else {
                    Text(
                        if (isSetupMode) "Guardar perfil y comenzar" else "Guardar cambios",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }

        // ── DONE OVERLAY (setup mode only) ───────────────────────────────────
        if (viewModel.savedSuccessfully && isSetupMode) {
            DoneOverlay(
                onEmpezar = {
                    viewModel.resetSaved()
                    onFinishSetup()
                }
            )
        }
    }
}

// ── Private composables ──────────────────────────────────────────────────────

@Composable
private fun SpeechBubble(text: String) {
    Box(modifier = Modifier.wrapContentSize()) {
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .widthIn(max = 220.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.5.dp, Color(0xFFD9EBE3), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = TealDark
            )
        }
        Canvas(
            modifier = Modifier
                .size(width = 9.dp, height = 14.dp)
                .align(Alignment.CenterStart)
        ) {
            drawPath(
                Path().apply {
                    moveTo(size.width, 0f)
                    lineTo(0f, size.height / 2f)
                    lineTo(size.width, size.height)
                    close()
                },
                color = Color(0xFFD9EBE3)
            )
            drawPath(
                Path().apply {
                    moveTo(size.width, 1.5f)
                    lineTo(1.5f, size.height / 2f)
                    lineTo(size.width, size.height - 1.5f)
                    close()
                },
                color = Color.White
            )
        }
    }
}

@Composable
private fun ContactCard(
    index: Int,
    contacto: ContactoSosEntity,
    isRequired: Boolean,
    onContactoChange: (ContactoSosEntity) -> Unit,
    onRemove: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Color(0xFFD9EBE3))
    ) {
        Column(Modifier.padding(16.dp)) {

            // Header row
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(6.dp).clip(CircleShape).background(primary)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (index == 0) "Contacto principal" else "Contacto ${index + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = TealDark
                    )
                }

                Spacer(Modifier.width(6.dp))

                if (isRequired) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFFFCE7E0))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Obligatorio",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            ),
                            color = Color(0xFFC0573D)
                        )
                    }
                } else {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Opcional",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = Color(0xFF6E8682)
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                if (!isRequired) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onRemove() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_circle_x),
                            contentDescription = null,
                            tint = Color(0xFF6E8682),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            ContactInputField(
                iconRes = R.drawable.ic_person,
                placeholder = "Nombre completo",
                value = contacto.nombre,
                onValueChange = { onContactoChange(contacto.copy(nombre = it)) },
                keyboardType = KeyboardType.Text
            )

            Spacer(Modifier.height(10.dp))

            ContactInputField(
                iconRes = R.drawable.ic_phone,
                placeholder = "Número de teléfono",
                value = contacto.telefono,
                onValueChange = { onContactoChange(contacto.copy(telefono = it)) },
                keyboardType = KeyboardType.Phone
            )
        }
    }
}

@Composable
private fun ContactInputField(
    iconRes: Int,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    var isFocused by remember { mutableStateOf(false) }
    val primary = MaterialTheme.colorScheme.primary
    val background = MaterialTheme.colorScheme.background

    val borderColor = if (isFocused) primary else Color.Transparent
    val iconColor = if (isFocused) primary else Color(0xFF7BB39E)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(iconRes),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { isFocused = it.isFocused },
            textStyle = TextStyle(
                fontSize = 15.sp,
                color = Color(0xFF0B3B30)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            placeholder,
                            style = TextStyle(
                                fontSize = 15.sp,
                                color = Color(0xFF9DB4AD)
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun DoneOverlay(onEmpezar: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF082820).copy(alpha = 0.55f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) +
                    fadeIn(tween(250))
        ) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painterResource(R.drawable.berto_saludando),
                        contentDescription = null,
                        modifier = Modifier.height(130.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "¡Listo!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontStyle = FontStyle.Italic
                        ),
                        color = TealDark
                    )
                    Text(
                        "Tu perfil está configurado. Berto te acompaña desde ahora.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6E8682),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp, bottom = 22.dp)
                    )
                    Button(
                        onClick = onEmpezar,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primary)
                    ) {
                        Text(
                            "Empezar",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            painterResource(R.drawable.ic_arrow_right),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
