package com.solvyx.ui.decisiontree

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
import androidx.navigation.NavController
import com.solvyx.backend.presentation.viewmodel.DecisionTreeViewModel

@Composable
fun TreesSelectionScreen(
    navController: NavController,
    viewModel: DecisionTreeViewModel
) {

    val trees = listOf(
        "alcohol_craving" to "Alcohol - Craving",
        "alcohol_info" to "Alcohol - Información",
        "cristal_craving" to "Cristal - Craving",
        "cristal_info" to "Cristal - Información"
    )

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Árboles de decisión (demo)", style = MaterialTheme.typography.titleLarge)

            for ((id, label) in trees) {
                Button(onClick = { navController.navigate("trees/$id") }) {
                    Text(label)
                }
            }

            Button(onClick = { navController.navigateUp() }) {
                Text("Volver")
            }
        }
    }
}