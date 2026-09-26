package com.solvyx.ui.screens.red.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/** A phone number the user picked from their address book. */
data class PickedContact(val name: String, val phone: String)

/**
 * Lets the user choose one phone number from their contacts. Uses the system picker, which grants
 * temporary read access to the chosen entry only — no `READ_CONTACTS` permission needed.
 * Returns the function that opens the picker.
 */
@Composable
fun rememberPhoneContactPicker(onPicked: (PickedContact) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        readPickedPhone(context, uri)?.let(onPicked)
    }
    return {
        try {
            launcher.launch(Intent(Intent.ACTION_PICK, Phone.CONTENT_URI))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No encontramos una app de contactos en tu teléfono.", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun readPickedPhone(context: Context, uri: Uri): PickedContact? = runCatching {
    context.contentResolver.query(uri, arrayOf(Phone.DISPLAY_NAME, Phone.NUMBER), null, null, null)
        ?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            PickedContact(name = cursor.getString(0).orEmpty(), phone = cursor.getString(1).orEmpty())
        }
}.getOrNull()
