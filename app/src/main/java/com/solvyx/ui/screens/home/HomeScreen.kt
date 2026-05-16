package com.solvyx.ui.screens.home

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
import com.solvyx.ui.navigation.Routes

@Composable
fun HomeScreen(
    navController: NavController
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Inicio", style = MaterialTheme.typography.titleLarge)

            Button(onClick = { navController.navigate(Routes.DIAGNOSTICO) }) {
                Text("Ir a Diagnóstico")
            }

            Button(onClick = { navController.navigate("trees") }) {
                Text("Explorar árboles (demo)")
            }

            Button(onClick = { navController.navigate(Routes.ONBOARDING) }) {
                Text("Onboarding")
            }
        }
    }
}