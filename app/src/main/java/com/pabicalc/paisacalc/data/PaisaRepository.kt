package com.pabicalc.paisacalc.data

import com.pabicalc.paisacalc.gst.GstCalculation
import kotlinx.coroutines.flow.Flow

/** A narrow local-only API used by the EMI and GST screens. */
class PaisaRepository(private val database: PaisaDatabase) {

    fun observeSavedLoans(): Flow<List<SavedLoanEntity>> = database.savedLoanDao().observeAll()

    suspend fun saveLoan(loan: SavedLoanEntity): Long = database.savedLoanDao().insert(loan)

    suspend fun deleteLoan(id: Long) = database.savedLoanDao().deleteById(id)

    fun observeGstHistory(): Flow<List<GstHistoryEntity>> = database.gstHistoryDao().observeRecent()

    suspend fun saveGstCalculation(
        calculation: GstCalculation,
        createdAtEpochMillis: Long = System.currentTimeMillis(),
    ): Long = database.gstHistoryDao().insert(
        GstHistoryEntity(
            mode = calculation.mode.name,
            supplyType = calculation.supplyType.name,
            rateMicroPercent = calculation.rateMicroPercent,
            amountEnteredPaise = calculation.amountEnteredPaise,
            taxableValuePaise = calculation.taxableValuePaise,
            taxAmountPaise = calculation.taxAmountPaise,
            totalAmountPaise = calculation.totalAmountPaise,
            cgstPaise = calculation.cgstPaise,
            sgstPaise = calculation.sgstPaise,
            igstPaise = calculation.igstPaise,
            createdAtEpochMillis = createdAtEpochMillis,
        ),
    )

    suspend fun deleteGstHistory(id: Long) = database.gstHistoryDao().deleteById(id)
}
