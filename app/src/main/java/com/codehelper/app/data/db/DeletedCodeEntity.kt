package com.codehelper.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_codes")
data class DeletedCodeEntity(
    @PrimaryKey
    val codeId: String,
    val deletedAt: Long = System.currentTimeMillis()
)
