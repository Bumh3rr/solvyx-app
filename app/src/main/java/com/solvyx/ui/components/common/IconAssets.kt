package com.solvyx.ui.components.common

import androidx.annotation.DrawableRes
import com.solvyx.R

/**
 * Mapea el `iconAsset` lógico de los modelos de dominio
 * (`Ejercicio.iconAsset`, `Rutina.iconAsset`, `GuiaExtendida.iconAsset`)
 * al `R.drawable.*` real.
 *
 * Si el `iconAsset` es `null` o desconocido, devuelve `null` para que el
 * llamador decida si renderizar un placeholder, un ícono genérico, u
 * omitir el ícono. **Nunca** devuelve un valor inventado.
 */
@DrawableRes
fun mapIconAsset(iconAsset: String?): Int? = when (iconAsset) {
    // Ejercicios
    "ic_wind" -> R.drawable.ic_wind
    "ic_activity" -> R.drawable.ic_activity
    "ic_heart_pulse" -> R.drawable.ic_heart_pulse
    "ic_face_neutral" -> R.drawable.ic_face_neutral
    "ic_face_happy" -> R.drawable.ic_face_happy
    "ic_face_sad" -> R.drawable.ic_face_sad
    "ic_face_anxious" -> R.drawable.ic_face_anxious
    "ic_brain" -> R.drawable.ic_brain
    "ic_droplet" -> R.drawable.ic_droplet
    "ic_flame" -> R.drawable.ic_flame
    "ic_clipboard" -> R.drawable.ic_clipboard
    "ic_target" -> R.drawable.ic_target
    "ic_zap" -> R.drawable.ic_zap
    "ic_shield" -> R.drawable.ic_shield
    "ic_bell" -> R.drawable.ic_bell
    "ic_calendar" -> R.drawable.ic_calendar
    "ic_clock" -> R.drawable.ic_clock
    // Sustancias (también reusables en guías)
    "ic_bottle" -> R.drawable.ic_bottle
    "ic_vape" -> R.drawable.ic_vape
    "ic_gem" -> R.drawable.ic_gem
    "ic_cigarette" -> R.drawable.ic_cigarette
    // Estados / misceláneos
    "ic_heart" -> R.drawable.ic_heart
    "ic_people" -> R.drawable.ic_people
    "ic_alert_triangle" -> R.drawable.ic_alert_triangle
    "ic_alert_circle" -> R.drawable.ic_alert_circle
    "ic_alert_octagon" -> R.drawable.ic_alert_octagon
    "ic_info" -> R.drawable.ic_info
    "ic_info_circle" -> R.drawable.ic_info_circle
    "ic_history" -> R.drawable.ic_history
    "ic_trophy" -> R.drawable.ic_trophy
    "ic_check" -> R.drawable.ic_check
    "ic_check_circle" -> R.drawable.ic_check_circle
    "ic_chevron_right" -> R.drawable.ic_chevron_right
    else -> null
}
