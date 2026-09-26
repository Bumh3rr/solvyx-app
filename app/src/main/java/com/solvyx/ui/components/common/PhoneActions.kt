package com.solvyx.ui.components.common

import android.content.Context
import android.content.Intent
import android.net.Uri

private const val MexicanPhoneLength = 10
private const val TollFreePrefix = "800"

/** Metro areas with a 2-digit area code; every other Mexican area code has 3 digits. */
private val TwoDigitAreaCodes = setOf("55", "33", "81")

/**
 * Groups a 10-digit Mexican number the way people read it aloud:
 * "800 911 2000", "55 5259 8121", "747 494 9445". Anything else is returned untouched.
 */
fun formatMexicanPhone(digits: String): String {
    if (digits.length != MexicanPhoneLength || !digits.all(Char::isDigit)) return digits
    return when {
        digits.startsWith(TollFreePrefix) || digits.take(2) !in TwoDigitAreaCodes ->
            "${digits.substring(0, 3)} ${digits.substring(3, 6)} ${digits.substring(6)}"
        else -> "${digits.substring(0, 2)} ${digits.substring(2, 6)} ${digits.substring(6)}"
    }
}

/** Opens the dialer pre-filled with [number]; the user still confirms the call themselves. */
fun Context.openDialer(number: String) {
    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
}

/**
 * Turn-by-turn directions in Google Maps when installed, otherwise the maps website —
 * so it works on any phone.
 */
fun Context.openDirections(latitude: Double, longitude: Double) {
    val navigation = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$latitude,$longitude"))
        .setPackage("com.google.android.apps.maps")
    val intent = if (navigation.resolveActivity(packageManager) != null) {
        navigation
    } else {
        Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=$latitude,$longitude"))
    }
    startActivity(intent)
}
