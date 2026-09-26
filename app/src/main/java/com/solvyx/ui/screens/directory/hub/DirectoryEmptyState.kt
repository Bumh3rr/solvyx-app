package com.solvyx.ui.screens.directory.hub

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.theme.TealDark

/** No match for the current search/filters: say so plainly and offer a one-tap way back. */
@Composable
fun DirectoryEmptyState(onClearFilters: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BertoPoseAnimation(
            pose = BertoPose.CENTER_IDLE,
            riveFileRes = R.raw.berto_poses,
            modifier = Modifier.size(96.dp),
            fallback = R.drawable.berto_pregunta
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "No encontré a nadie con esa búsqueda",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = TealDark,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Prueba con otra palabra o quita los filtros.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        SolvyxOutlinedButton(text = "Quitar filtros", onClick = onClearFilters)
    }
}
