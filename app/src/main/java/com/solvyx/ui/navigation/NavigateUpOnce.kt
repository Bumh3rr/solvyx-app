package com.solvyx.ui.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

/**
 * `navigateUp()` only while [from] is still the screen on top. A second tap on "Listo" or a
 * second back press during the exit transition would otherwise pop the screen below as well, and
 * `navigateUp()` on the last destination relaunches the whole activity (the app "restarts").
 */
fun NavController.navigateUpFrom(from: NavBackStackEntry) {
    if (from.lifecycle.currentState == Lifecycle.State.RESUMED) navigateUp()
}
