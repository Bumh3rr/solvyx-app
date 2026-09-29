package com.solvyx.backend.sms

import java.util.Locale

private const val SOS_GREETING = "Hola, estoy en crisis y necesito apoyo."
private const val SOS_SIGNATURE = "Este mensaje fue enviado automáticamente por Solvyx."
private const val MAPS_URL = "https://maps.google.com/?q="

data class Coordinates(val latitude: Double, val longitude: Double)

/** The SOS text; with a [location] it adds a maps link so the contact can find the user. */
fun sosMessage(location: Coordinates?): String =
    if (location == null) "$SOS_GREETING $SOS_SIGNATURE"
    else "$SOS_GREETING Mi ubicación: ${mapsLink(location)} $SOS_SIGNATURE"

/** Always dot-decimal: with a comma-decimal locale "17,5,-99,5" would be a broken link. */
fun mapsLink(location: Coordinates): String =
    MAPS_URL + String.format(Locale.US, "%.5f,%.5f", location.latitude, location.longitude)
