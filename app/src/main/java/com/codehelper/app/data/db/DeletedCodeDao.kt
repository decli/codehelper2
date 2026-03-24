package com.codehelper.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DeletedCodeDao {
    @Query("SELECT codeId FROM deleted_codes")
    fun getAllDeletedIds(): Flow<List<String>>

    @Query("SELECT codeId FROM deleted_codes")
    suspend fun getAllDeletedIdsOnce(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markDeleted(entity: DeletedCodeEntity)

    @Query("DELETE FROM deleted_codes")
    suspend fun clearAll()
}
