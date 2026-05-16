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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.solvyx.backend.presentation.viewmodel.DiagnosticoViewModel

@Composable
fun SubstanceSelectionScreen(
    navController: NavController,
    viewModel: DiagnosticoViewModel = hiltViewModel()
) {

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(text = "Selecciona una sustancia", style = MaterialTheme.typography.titleLarge)

            Button(onClick = {
                viewModel.cargarPreguntas("alcohol")
                navController.navigate("questions/alcohol")
            }) {
                Text("Alcohol")
            }

            Button(onClick = {
                viewModel.cargarPreguntas("cigarro")
                navController.navigate("questions/cigarro")
            }) {
                Text("Cigarro")
            }

            Button(onClick = {
                viewModel.cargarPreguntas("vape")
                navController.navigate("questions/vape")
            }) {
                Text("Vape")
            }

            Button(onClick = {
                viewModel.cargarPreguntas("cristal")
                navController.navigate("questions/cristal")
            }) {
                Text("Cristal")
            }

            Button(onClick = {
                navController.navigate("history")
            }) {
                Text("Ver historial")
            }
        }
    }
}