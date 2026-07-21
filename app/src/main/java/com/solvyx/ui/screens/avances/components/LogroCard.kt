package com.solvyx.ui.screens.avances.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.R
import com.solvyx.ui.screens.avances.AvancesViewModel
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

@Composable
fun LogroCard(logro: AvancesViewModel.UiLogro) {
    Card(
        modifier = Modifier.width(100.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = if (logro.unlocked)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceDim
        ),
        border   = BorderStroke(0.5.dp, TealLight)
    ) {
        Column(
            modifier             = Modifier.padding(12.dp),
            horizontalAlignment  = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier         = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (logro.unlocked) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter           = painterResource(logro.icon),
                        contentDescription = null,
                        tint              = if (logro.unlocked) Color.White
                                           else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier          = Modifier.size(22.dp)
                    )
                }
                if (!logro.unlocked) {
                    Box(
                        modifier         = Modifier
                            .size(16.dp)
                            .offset(x = 2.dp, y = 2.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceDim),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter           = painterResource(R.drawable.ic_lock),
                            contentDescription = null,
                            tint              = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier          = Modifier.size(10.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text      = logro.titulo,
                style     = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color     = if (logro.unlocked) TealDark
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines  = 2
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text      = logro.descripcion,
                style     = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines  = 2
            )
        }
    }
}
