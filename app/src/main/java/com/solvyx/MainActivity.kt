package com.solvyx

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.solvyx.backend.decisiontree.repository.DecisionTreeRepository
import com.solvyx.ui.navigation.SolvyxNavGraph
import com.solvyx.ui.theme.SolvyxappTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolvyxappTheme {
                val navController = rememberNavController()
                SolvyxNavGraph(navController = navController)
            }
        }
    }
}