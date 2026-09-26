package com.solvyx.ui.screens.directory.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.screens.directory.model.DirectoryCategory
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

/**
 * Calling a stranger is the hardest step for many young people. Berto lowers the barrier with a
 * ready-made opening line for this kind of provider and offers to rehearse it in the chat.
 */
@Composable
fun CallScriptCard(
    category: DirectoryCategory,
    onPracticeWithBerto: () -> Unit,
    modifier: Modifier = Modifier
) {
    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BertoPoseAnimation(
                    pose = BertoPose.RIGHT,
                    riveFileRes = R.raw.berto_poses,
                    modifier = Modifier.size(56.dp),
                    fallback = R.drawable.berto_dedo_der
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "¿Te pone nervioso llamar?",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = TealDark
                    )
                    Text(
                        text = "Es normal. No tienes que contar todo en la primera llamada. " +
                            "Puedes empezar así:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "\"${category.callScript}\"",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TealLight.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic
                ),
                color = TealDark
            )
            Spacer(Modifier.height(12.dp))
            SolvyxOutlinedButton(
                text = "Practicar con Berto",
                onClick = onPracticeWithBerto,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
