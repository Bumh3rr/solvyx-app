package com.solvyx.ui.components.haze

import androidx.compose.runtime.compositionLocalOf
import dev.chrisbanes.haze.HazeState

/**
 * Instancia compartida entre el contenido detrás del bottom nav (fuente del blur, vía
 * [dev.chrisbanes.haze.haze]) y la barra misma (el "child" que renderiza el blur, vía
 * [dev.chrisbanes.haze.hazeChild]). El default es una instancia nueva sin usar, para que
 * composables compartidos (como `GuiaPanel`) sigan funcionando sin blur en pantallas que
 * no viven detrás del bottom nav, sin necesitar un parámetro `hasBottomNav` aparte.
 */
val LocalHazeState = compositionLocalOf { HazeState() }
