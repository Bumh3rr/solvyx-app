package com.solvyx.ui.screens.avances

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxSegmentedControl
import com.solvyx.ui.components.common.SolvyxSegmentedDefaults
import com.solvyx.ui.screens.avances.components.CuentaRequeridaState
import com.solvyx.ui.screens.avances.components.DiaDetalleSheet
import com.solvyx.ui.screens.avances.tabs.LogrosTab
import com.solvyx.ui.screens.avances.tabs.ProgresoTab
import com.solvyx.ui.screens.avances.tabs.RegistroHoyResumen
import com.solvyx.ui.screens.avances.tabs.RegistroWizard
import com.solvyx.ui.screens.bitacora.RegistroViewModel

@Composable
fun AvancesScreen(
    onOpenDrawer: () -> Unit,
    onCrearCuenta: () -> Unit
) {
    val avancesVM: AvancesViewModel = hiltViewModel()
    val registroVM: RegistroViewModel = hiltViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    painter = painterResource(R.drawable.ic_menu),
                    contentDescription = "Menú",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "Mi camino",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.size(48.dp))
        }

        if (avancesVM.isAnonymous) {
            CuentaRequeridaState(onCrearCuenta = onCrearCuenta)
            return@Column
        }

        val registroCompleto by registroVM.registroCompletoHoy.collectAsState()

        // Tab inicial fijado una sola vez, cuando llega el primer valor real de registroCompletoHoy.
        var selectedTab by rememberSaveable { mutableIntStateOf(-1) }
        LaunchedEffect(registroCompleto) {
            if (selectedTab == -1) selectedTab = defaultTabIndex(registroCompleto)
        }
        val tab = if (selectedTab == -1) TAB_HOY else selectedTab

        // "Editar" abre el wizard aunque el registro de hoy esté completo; se resetea al guardar.
        var editando by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(registroVM.isSaved) {
            if (registroVM.isSaved) editando = false
        }

        SolvyxSegmentedControl(
            options = listOf("Hoy", "Progreso", "Logros"),
            selectedIndex = tab,
            onSelect = { selectedTab = it },
            colors = SolvyxSegmentedDefaults.onPrimary(),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp)
        )

        when (tab) {
            TAB_HOY -> {
                val hoy = registroVM.historial.collectAsState().value
                    .firstOrNull { it.date == registroVM.fecha }
                if (registroCompleto && !editando && hoy != null) {
                    RegistroHoyResumen(
                        entry = hoy,
                        onEditar = { registroVM.cargarRegistroDeHoy(); editando = true },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    RegistroWizard(viewModel = registroVM, modifier = Modifier.weight(1f))
                }
            }
            TAB_PROGRESO -> ProgresoTab(viewModel = avancesVM, modifier = Modifier.weight(1f))
            TAB_LOGROS -> LogrosTab(logros = avancesVM.uiLogros, modifier = Modifier.weight(1f))
        }
    }

    avancesVM.selectedDay?.let { entry ->
        DiaDetalleSheet(entry = entry, onDismiss = { avancesVM.dismissDayDetail() })
    }
}
