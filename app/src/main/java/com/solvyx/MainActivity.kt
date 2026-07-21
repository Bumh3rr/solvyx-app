package com.solvyx

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.solvyx.ui.navigation.ShortcutDestination
import com.solvyx.ui.navigation.SolvyxNavGraph
import com.solvyx.ui.theme.SolvyxappTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * Destino pedido por un App Shortcut, pendiente de consumir. Es estado y no se lee de
     * `intent.action` directo porque con `launchMode=singleTop` la app puede estar ya abierta:
     * en ese caso el shortcut llega por [onNewIntent], sin pasar por [onCreate].
     */
    private var pendingShortcut by mutableStateOf<ShortcutDestination?>(null)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingShortcut = ShortcutDestination.from(intent)
        setContent {
            SolvyxappTheme {
                val navController = rememberNavController()
                SolvyxNavGraph(
                    navController = navController,
                    pendingShortcut = pendingShortcut,
                    onShortcutHandled = { pendingShortcut = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingShortcut = ShortcutDestination.from(intent)
    }
}
