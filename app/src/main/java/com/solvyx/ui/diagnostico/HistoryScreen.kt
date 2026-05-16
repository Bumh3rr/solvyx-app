package com.solvyx.ui.diagnostico

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.solvyx.backend.data.local.entity.ResultadoEntity
import com.solvyx.backend.presentation.viewmodel.DiagnosticoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: DiagnosticoViewModel = hiltViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.cargarHistorial()
    }

    val historial by viewModel.historial.collectAsState()

    Scaffold { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            if (historial.isEmpty()) {
                Text("No hay historial", style = MaterialTheme.typography.bodyLarge)
                return@Column
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(historial) { resultado ->
                    ResultadoRow(resultado)
                }
            }
        }
    }
}

@Composable
private fun ResultadoRow(resultado: ResultadoEntity) {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val fecha = sdf.format(Date(resultado.fecha))

    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Sustancia: ${resultado.sustanciaId}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Puntaje: ${resultado.puntaje}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Nivel: ${resultado.nivel}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Fecha: $fecha", style = MaterialTheme.typography.bodySmall)
        }
    }
}