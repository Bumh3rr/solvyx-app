package com.solvyx.ui.screens.firstaid.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxBackButton
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.components.common.SolvyxTopBar
import com.solvyx.ui.screens.firstaid.model.SosEmphasis
import com.solvyx.ui.theme.CrisisRed

/**
 * Shell shared by every guide: colored top bar, an optional fixed [header] (e.g. step progress),
 * scrollable content and a bottom bar that keeps "notify my support network" one tap away no
 * matter how far the user scrolled (the module has no bottom nav, so no floating SOS button).
 */
@Composable
fun GuideScaffold(
    title: String,
    onBack: () -> Unit,
    onSos: () -> Unit,
    sosEmphasis: SosEmphasis,
    headerColor: Color = MaterialTheme.colorScheme.primary,
    header: (@Composable () -> Unit)? = null,
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SolvyxTopBar(
            title = title,
            navigationButton = { SolvyxBackButton(onClick = onBack) },
            containerColor = headerColor
        )
        header?.invoke()
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
        SosActionBar(emphasis = sosEmphasis, onSos = onSos)
    }
}

@Composable
private fun SosActionBar(emphasis: SosEmphasis, onSos: () -> Unit) {
    val icon: @Composable () -> Unit = {
        Icon(
            painter = painterResource(R.drawable.ic_alert_triangle),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceDim)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            when (emphasis) {
                SosEmphasis.FILLED -> SolvyxButton(
                    text = "Avisar a mi red de apoyo",
                    onClick = onSos,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = CrisisRed,
                    leadingIcon = icon
                )
                SosEmphasis.OUTLINED -> SolvyxOutlinedButton(
                    text = "Avisar a mi red de apoyo",
                    onClick = onSos,
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = CrisisRed,
                    textColor = CrisisRed,
                    leadingIcon = icon
                )
            }
            Text(
                text = "Se enviará un SMS a tus contactos de confianza",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Closing reminder shown at the bottom of guides and the hub. */
@Composable
fun ProfessionalCareDisclaimer(modifier: Modifier = Modifier) {
    Text(
        text = "Solvyx no reemplaza la atención profesional. Es un puente para que llegues a " +
            "quien puede ayudarte.",
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}
