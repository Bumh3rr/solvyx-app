package com.solvyx.ui.diagnostico

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.solvyx.backend.presentation.viewmodel.DiagnosticoViewModel

@Composable
fun QuestionsScreen(
    navController: NavController,
    sustanciaArg: String?,
    viewModel: DiagnosticoViewModel = hiltViewModel()
) {

    LaunchedEffect(sustanciaArg) {
        sustanciaArg?.let { viewModel.cargarPreguntas(it) }
    }

    val preguntas by viewModel.preguntas.collectAsState()

    // Local UI state for current question and answers (puntajes)
    var currentIndex by rememberSaveable { androidx.compose.runtime.mutableStateOf(0) }

    val answers = remember { mutableStateListOf<Int>() }

    // ensure answers list size matches preguntas
    if (answers.size < preguntas.size) {
        for (i in answers.size until preguntas.size) answers.add(0)
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (preguntas.isEmpty()) {
                Text("Cargando preguntas...", style = MaterialTheme.typography.bodyLarge)
                return@Column
            }

            val pregunta = preguntas[currentIndex]

            Text(text = "Pregunta ${currentIndex + 1} / ${preguntas.size}", style = MaterialTheme.typography.titleMedium)
            Text(text = pregunta.texto, style = MaterialTheme.typography.bodyLarge)

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(pregunta.opciones) { index, opcion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = answers[currentIndex] == opcion.puntaje,
                            onClick = { answers[currentIndex] = opcion.puntaje }
                        )

                        Text(text = opcion.texto, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = currentIndex > 0, onClick = { currentIndex = (currentIndex - 1).coerceAtLeast(0) }) {
                    Text("Anterior")
                }

                if (currentIndex < preguntas.lastIndex) {
                    Button(onClick = { currentIndex = (currentIndex + 1).coerceAtMost(preguntas.lastIndex) }) {
                        Text("Siguiente")
                    }
                } else {
                    Button(onClick = {
                        // enviar respuestas (lista de puntajes)
                        viewModel.evaluarRespuestas(answers.toList())
                        navController.navigate("result")
                    }) {
                        Text("Finalizar encuesta")
                    }
                }
            }
        }
    }
}