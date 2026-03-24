package com.codehelper.app.data.model

data class PickupCode(
    val id: String,          // unique: smsId + code
    val code: String,        // the extracted pickup code
    val smsBody: String,     // full SMS body
    val sender: String,      // SMS sender address
    val timestamp: Long,     // SMS receive time
    val matchedRule: String, // which rule matched (e.g. "取件码")
    val isDeleted: Boolean = false
)
