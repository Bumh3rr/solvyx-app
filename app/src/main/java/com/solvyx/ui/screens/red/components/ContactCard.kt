package com.solvyx.ui.screens.red.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.data.local.entity.SosContactEntity
import com.solvyx.backend.validation.Validadores
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.normalizeMexicanPhone
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealMedium
import com.solvyx.ui.theme.WarnAmber
import com.solvyx.ui.theme.WarnAmberDark

private const val PhoneLength = 10
private const val ChipTintAlpha = 0.45f
private const val PickButtonTintAlpha = 0.08f

/**
 * One SOS contact: name + phone, typed or picked from the address book. The first contact is
 * required; the others can be removed. A phone that's started but incomplete says how many digits
 * are missing instead of just disabling "Guardar".
 */
@Composable
fun ContactCard(
    index: Int,
    contact: SosContactEntity,
    onChange: (SosContactEntity) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRequired = index == 0
    val pickContact = rememberPhoneContactPicker { picked ->
        onChange(
            contact.copy(
                name = Validadores.filtrarNombre(picked.name).ifBlank { contact.name },
                phone = Validadores.filtrarTelefono(normalizeMexicanPhone(picked.phone))
            )
        )
    }
    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isRequired) "Contacto principal" else "Contacto ${index + 1}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = TealDark
                )
                Spacer(Modifier.width(8.dp))
                RequirementPill(isRequired)
                Spacer(Modifier.weight(1f))
                if (!isRequired) {
                    Icon(
                        painter = painterResource(R.drawable.ic_circle_x),
                        contentDescription = "Quitar contacto",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(role = Role.Button, onClick = onRemove)
                            .padding(4.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            PickFromContactsButton(onClick = pickContact)
            Spacer(Modifier.height(10.dp))
            ContactInputField(
                icon = R.drawable.ic_person,
                placeholder = "Nombre",
                value = contact.name,
                onValueChange = { onChange(contact.copy(name = Validadores.filtrarNombre(it))) },
                keyboardType = KeyboardType.Text
            )
            Spacer(Modifier.height(10.dp))
            ContactInputField(
                icon = R.drawable.ic_phone,
                placeholder = "Teléfono a 10 dígitos",
                value = contact.phone,
                onValueChange = { onChange(contact.copy(phone = Validadores.filtrarTelefono(it))) },
                keyboardType = KeyboardType.Phone
            )
            val missingDigits = PhoneLength - contact.phone.length
            if (contact.phone.isNotEmpty() && missingDigits > 0) {
                Text(
                    text = if (missingDigits == 1) "Falta 1 dígito" else "Faltan $missingDigits dígitos",
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = WarnAmberDark
                )
            }
        }
    }
}

@Composable
private fun RequirementPill(isRequired: Boolean) {
    Text(
        text = if (isRequired) "Obligatorio" else "Opcional",
        modifier = Modifier
            .background(if (isRequired) WarnAmber else TealLight.copy(alpha = ChipTintAlpha), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = if (isRequired) WarnAmberDark else TealDark
    )
}

@Composable
private fun PickFromContactsButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = PickButtonTintAlpha))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_people),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Elegir de mis contactos",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ContactInputField(
    @DrawableRes icon: Int,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val primary = MaterialTheme.colorScheme.primary
    val border by animateColorAsState(if (focused) primary else TealLight, label = "contactFieldBorder")
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(MaterialTheme.colorScheme.background, shape)
            .border(if (focused) 2.dp else 1.dp, border, shape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (focused) primary else TealMedium,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            interactionSource = interaction,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = TealDark),
            cursorBrush = SolidColor(primary),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    inner()
                }
            }
        )
    }
}
