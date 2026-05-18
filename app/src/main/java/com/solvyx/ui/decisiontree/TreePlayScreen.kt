package com.solvyx.ui.decisiontree

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.solvyx.backend.decisiontree.model.DecisionOption
import com.solvyx.backend.decisiontree.model.DecisionNode
import com.solvyx.backend.presentation.viewmodel.DecisionTreeViewModel

@Composable
fun TreePlayScreen(
    navController: NavController,
    treeId: String,
    viewModel: DecisionTreeViewModel
) {

    LaunchedEffect(treeId) {
        viewModel.iniciarArbol(treeId)
    }

    val nodoActual by viewModel.nodoActual.collectAsState()
    val respuestas by viewModel.respuestas.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Árbol: $treeId", style = MaterialTheme.typography.titleLarge)

            if (nodoActual == null) {
                Text("Cargando árbol...", style = MaterialTheme.typography.bodyLarge)
                return@Column
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = nodoActual!!.texto, style = MaterialTheme.typography.titleMedium)

                    if (!nodoActual!!.opciones.isNullOrEmpty()) {
                        LazyColumn {
                            items(nodoActual!!.opciones) { opcion: DecisionOption ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(onClick = { viewModel.responder(opcion.texto) }) {
                                        Text(opcion.texto)
                                    }
                                }
                            }
                        }
                    }

                    nodoActual!!.mensaje?.let { msg ->
                        Text(text = msg, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
                    }

                    if (nodoActual!!.esFinal) {
                        Button(onClick = { viewModel.reiniciar() }) {
                            Text("Reiniciar árbol")
                        }
                    }
                }
            }

            Text("Respuestas: ")
            if (respuestas.isNotEmpty()) {
                for (r in respuestas) {
                    Text("- $r", style = MaterialTheme.typography.bodySmall)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { navController.navigateUp() }) {
                    Text("Volver")
                }
            }
        }
    }
}