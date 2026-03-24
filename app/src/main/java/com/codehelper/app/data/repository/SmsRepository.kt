package com.codehelper.app.data.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import com.codehelper.app.data.model.ExtractionRule
import com.codehelper.app.data.model.PickupCode

class SmsRepository(private val context: Context) {

    fun readSms(hoursBack: Int): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()
        val cutoffTime = System.currentTimeMillis() - hoursBack * 3600_000L

        val cursor: Cursor? = context.contentResolver.query(
            Uri.parse("content://sms/inbox"),
            arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
            ),
            "${Telephony.Sms.DATE} >= ?",
            arrayOf(cutoffTime.toString()),
            "${Telephony.Sms.DATE} DESC"
        )

        cursor?.use {
            val idIdx = it.getColumnIndexOrThrow(Telephony.Sms._ID)
            val addressIdx = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIdx = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIdx = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

            while (it.moveToNext()) {
                messages.add(
                    SmsMessage(
                        id = it.getLong(idIdx),
                        address = it.getString(addressIdx) ?: "",
                        body = it.getString(bodyIdx) ?: "",
                        date = it.getLong(dateIdx)
                    )
                )
            }
        }

        return messages
    }

    fun extractCodes(messages: List<SmsMessage>, rules: List<ExtractionRule>): List<PickupCode> {
        val codes = mutableListOf<PickupCode>()
        val enabledRules = rules.filter { it.isEnabled }

        for (sms in messages) {
            for (rule in enabledRules) {
                try {
                    val regex = Regex(rule.toFullPattern())
                    val matches = regex.findAll(sms.body)
                    for (match in matches) {
                        val code = match.groupValues.getOrElse(1) { match.value }
                        val codeId = "${sms.id}_${code}"
                        codes.add(
                            PickupCode(
                                id = codeId,
                                code = code,
                                smsBody = sms.body,
                                sender = sms.address,
                                timestamp = sms.date,
                                matchedRule = rule.prefix
                            )
                        )
                    }
                } catch (_: Exception) {
                    // Skip invalid regex
                }
            }
        }

        // Deduplicate by code+timestamp
        return codes.distinctBy { it.id }.sortedByDescending { it.timestamp }
    }

    data class SmsMessage(
        val id: Long,
        val address: String,
        val body: String,
        val date: Long
    )
}
