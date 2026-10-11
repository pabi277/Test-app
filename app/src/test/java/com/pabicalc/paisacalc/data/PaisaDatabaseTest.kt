package com.pabicalc.paisacalc.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pabicalc.paisacalc.gst.GstCalculationMode
import com.pabicalc.paisacalc.gst.GstCalculator
import com.pabicalc.paisacalc.gst.GstSupplyType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PaisaDatabaseTest {

    @Test
    fun savedLoanAndGstHistorySurviveClosingAndReopeningTheDatabase() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "paisa-phase3-${UUID.randomUUID()}.db"
        val savedLoan = SavedLoanEntity(
            title = "Home loan · 8.5%",
            principalRupees = 1_000_000L,
            rateMicroPercent = 8_500_000L,
            tenureMonths = 240,
            prepaymentAmountRupees = 25_000L,
            prepaymentFromInstallment = 13,
            prepaymentFrequency = "QUARTERLY",
            prepaymentStrategy = "REDUCE_TENURE",
            savedAtEpochMillis = 1_760_000_000_000L,
        )
        val gstHistory = GstHistoryEntity(
            mode = "ADD",
            supplyType = "INTRA_STATE",
            rateMicroPercent = 18_000_000L,
            amountEnteredPaise = 100_000L,
            taxableValuePaise = 100_000L,
            taxAmountPaise = 18_000L,
            totalAmountPaise = 118_000L,
            cgstPaise = 9_000L,
            sgstPaise = 9_000L,
            igstPaise = 0L,
            createdAtEpochMillis = 1_760_000_000_100L,
        )

        try {
            val database = Room.databaseBuilder(context, PaisaDatabase::class.java, databaseName)
                .allowMainThreadQueries()
                .build()
            try {
                val repository = PaisaRepository(database)
                repository.saveLoan(savedLoan)
                repository.saveGstCalculation(
                    GstCalculator.calculate(
                        amountEnteredPaise = 100_000L,
                        rateMicroPercent = 18_000_000L,
                        mode = GstCalculationMode.ADD,
                        supplyType = GstSupplyType.INTRA_STATE,
                    ),
                    createdAtEpochMillis = gstHistory.createdAtEpochMillis,
                )
            } finally {
                database.close()
            }

            val reopened = Room.databaseBuilder(context, PaisaDatabase::class.java, databaseName)
                .allowMainThreadQueries()
                .build()
            try {
                assertEquals(
                    listOf(savedLoan.copy(id = 1L)),
                    reopened.savedLoanDao().observeAll().first(),
                )
                assertEquals(
                    listOf(gstHistory.copy(id = 1L)),
                    reopened.gstHistoryDao().observeRecent().first(),
                )
            } finally {
                reopened.close()
            }
        } finally {
            context.deleteDatabase(databaseName)
        }
    }
}
