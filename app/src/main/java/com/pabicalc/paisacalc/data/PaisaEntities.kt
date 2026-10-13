package com.pabicalc.paisacalc.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A reusable loan input set, stored only in the app's private Room database. */
@Entity(tableName = "saved_loans")
data class SavedLoanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val principalRupees: Long,
    val rateMicroPercent: Long,
    val tenureMonths: Int,
    /** Zero means no active prepayment. The current editor stores one plan. */
    val prepaymentAmountRupees: Long = 0,
    val prepaymentFromInstallment: Int = 13,
    val prepaymentFrequency: String = "QUARTERLY",
    val prepaymentStrategy: String = "REDUCE_TENURE",
    val savedAtEpochMillis: Long,
)

/** A saved GST calculation history row; all monetary columns are in paise. */
@Entity(tableName = "gst_history")
data class GstHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mode: String,
    val supplyType: String,
    val rateMicroPercent: Long,
    val amountEnteredPaise: Long,
    val taxableValuePaise: Long,
    val taxAmountPaise: Long,
    val totalAmountPaise: Long,
    val cgstPaise: Long,
    val sgstPaise: Long,
    val igstPaise: Long,
    val createdAtEpochMillis: Long,
)
