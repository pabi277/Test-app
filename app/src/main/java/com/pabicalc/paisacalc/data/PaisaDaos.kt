package com.pabicalc.paisacalc.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedLoanDao {

    @Query("SELECT * FROM saved_loans ORDER BY savedAtEpochMillis DESC, id DESC")
    fun observeAll(): Flow<List<SavedLoanEntity>>

    @Insert
    suspend fun insert(loan: SavedLoanEntity): Long

    @Query("DELETE FROM saved_loans WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface GstHistoryDao {

    @Query("SELECT * FROM gst_history ORDER BY createdAtEpochMillis DESC, id DESC LIMIT 50")
    fun observeRecent(): Flow<List<GstHistoryEntity>>

    @Insert
    suspend fun insert(calculation: GstHistoryEntity): Long

    @Query("DELETE FROM gst_history WHERE id = :id")
    suspend fun deleteById(id: Long)
}
