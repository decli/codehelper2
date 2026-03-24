package com.codehelper.app.data.model

data class ExtractionRule(
    val id: String = java.util.UUID.randomUUID().toString(),
    val prefix: String,     // Chinese prefix like "取件码"
    val pattern: String,    // regex pattern like "[a-zA-Z0-9-]+"
    val isEnabled: Boolean = true
) {
    fun toFullPattern(): String {
        return "$prefix\\s{0,2}[:：]?\\s{0,2}($pattern)"
    }

    fun toDisplayString(): String = "$prefix$pattern"
}
