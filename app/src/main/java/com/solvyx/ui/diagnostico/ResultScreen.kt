package com.solvyx.ui.diagnostico

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.solvyx.backend.presentation.viewmodel.DiagnosticoViewModel

@Composable
fun ResultScreen(
    navController: NavController,
    viewModel: DiagnosticoViewModel = hiltViewModel()
) {

    val resultado by viewModel.resultado.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (resultado == null) {
                Text("Esperando resultado...", style = MaterialTheme.typography.bodyLarge)
                return@Column
            }

            Text("Resultado para: ${resultado!!.sustanciaId}", style = MaterialTheme.typography.titleLarge)
            Text("Puntaje: ${resultado!!.puntaje}", style = MaterialTheme.typography.titleMedium)
            Text("Nivel de riesgo: ${resultado!!.nivel.name}", style = MaterialTheme.typography.bodyLarge)
            Text("Recomendación: ${resultado!!.recomendacion}", style = MaterialTheme.typography.bodyLarge)

            Button(onClick = {
                // volver a inicio
                navController.navigate("selection") {
                    popUpTo("selection") { inclusive = false }
                }
            }) {
                Text("Inicio")
            }

            Button(onClick = { navController.navigate("history") }) {
                Text("Ver historial")
            }
        }
    }
}