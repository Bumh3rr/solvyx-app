package com.solvyx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.solvyx.ui.diagnostico.DiagnosticoNavGraph
import com.solvyx.ui.theme.SolvyxappTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolvyxappTheme {
                Surface {
                    DiagnosticoNavGraph()
                }
            }
        }
    }
}
