package com.solvyx.backend.sms

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SosSmsSender"
private const val SENT_ACTION_PREFIX = "com.solvyx.SOS_SMS_SENT"
private const val EXTRA_CONTACT_INDEX = "contact_index"
private const val SENT_CONFIRMATION_TIMEOUT_MS = 20_000L

/**
 * Sends the SOS text and reports who it really went out to. "Really" means the radio confirmed every
 * part through its sent PendingIntent: `sendTextMessage` returning proves nothing. A text too long
 * for one SMS (70 characters once it has an accent) was dropped by Android that way without any
 * error, and the app still said "Alerta enviada" (BUG-40).
 */
@Singleton
class SosSmsSender @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /** The phones that got the whole [message]; empty without the SMS permission or telephony. */
    suspend fun send(phones: List<String>, message: String): List<String> {
        if (phones.isEmpty() || !hasSmsPermission()) return emptyList()
        val smsManager = smsManager() ?: return emptyList()
        val parts = smsManager.divideMessage(message)
        val action = "$SENT_ACTION_PREFIX.${UUID.randomUUID()}"
        val results = Channel<SentResult>(Channel.UNLIMITED)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val contactIndex = intent.getIntExtra(EXTRA_CONTACT_INDEX, -1)
                results.trySend(SentResult(contactIndex, sent = resultCode == Activity.RESULT_OK))
            }
        }
        ContextCompat.registerReceiver(
            context, receiver, IntentFilter(action), ContextCompat.RECEIVER_NOT_EXPORTED
        )
        try {
            val queued = phones.indices.filter { index ->
                queue(smsManager, phones[index], parts, action, index)
            }
            val delivered = awaitDelivered(results, queued.size, parts.size)
            return delivered.map { phones[it] }
        } finally {
            context.unregisterReceiver(receiver)
        }
    }

    private fun queue(
        smsManager: SmsManager,
        phone: String,
        parts: ArrayList<String>,
        action: String,
        contactIndex: Int
    ): Boolean = try {
        val sentIntents = ArrayList(parts.indices.map { part ->
            sentIntent(action, contactIndex, requestCode = contactIndex * parts.size + part)
        })
        smsManager.sendMultipartTextMessage(phone, null, parts, sentIntents, null)
        true
    } catch (e: Exception) {
        // One bad number must not stop the rest of the contacts from being alerted.
        Log.w(TAG, "SMS to contact $contactIndex could not be queued", e)
        false
    }

    private fun sentIntent(action: String, contactIndex: Int, requestCode: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(action).setPackage(context.packageName).putExtra(EXTRA_CONTACT_INDEX, contactIndex),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

    /** Indexes of the contacts whose every part was confirmed sent before the timeout. */
    private suspend fun awaitDelivered(
        results: ReceiveChannel<SentResult>,
        queuedContacts: Int,
        partsPerContact: Int
    ): List<Int> {
        val confirmedParts = mutableMapOf<Int, Int>()
        var pending = queuedContacts * partsPerContact
        withTimeoutOrNull(SENT_CONFIRMATION_TIMEOUT_MS) {
            while (pending > 0) {
                val result = results.receive()
                pending--
                if (result.sent) confirmedParts.merge(result.contactIndex, 1, Int::plus)
                else Log.w(TAG, "SMS part to contact ${result.contactIndex} failed")
            }
        }
        return confirmedParts.filterValues { it == partsPerContact }.keys.sorted()
    }

    private fun smsManager(): SmsManager? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

    private fun hasSmsPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) ==
            PackageManager.PERMISSION_GRANTED

    private data class SentResult(val contactIndex: Int, val sent: Boolean)
}
