package com.solvyx.ui.navigation

import android.content.Intent

/**
 * Destinos alcanzables desde un App Shortcut (mantener presionado el ícono de la app).
 *
 * Ambos funcionan **sin sesión y sin red**, que es lo que permite saltarse el flujo normal de
 * arranque en vez de pasar por el splash y el enrutado de auth:
 * - Berto (`ChatViewModel`) solo depende de `DecisionTreeRepository`, en memoria.
 * - SOS (`SosViewModel`) solo lee los contactos de Room.
 *
 * Los IDs de acción deben coincidir con `res/xml/shortcuts.xml`.
 */
enum class ShortcutDestination(val action: String, val route: String) {
    Sos(action = "com.solvyx.action.SHORTCUT_SOS", route = Routes.SOS_OVERLAY),
    Berto(action = "com.solvyx.action.SHORTCUT_BERTO", route = Routes.CHAT);

    companion object {
        fun from(intent: Intent?): ShortcutDestination? {
            val action = intent?.action ?: return null
            return entries.firstOrNull { it.action == action }
        }
    }
}
