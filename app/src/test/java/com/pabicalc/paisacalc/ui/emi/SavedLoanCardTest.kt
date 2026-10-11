package com.pabicalc.paisacalc.ui.emi

import com.pabicalc.paisacalc.data.SavedLoanEntity
import com.pabicalc.paisacalc.engine.PrepaymentFrequency
import com.pabicalc.paisacalc.engine.PrepaymentStrategy
import org.junit.Assert.assertEquals
import org.junit.Test

class SavedLoanCardTest {

    @Test
    fun aSavedLoanRestoresItsInputsAndPrepayment() {
        val saved = SavedLoanEntity(
            id = 7,
            title = "Home loan",
            principalRupees = 1_000_000L,
            rateMicroPercent = 8_500_000L,
            tenureMonths = 240,
            prepaymentAmountRupees = 25_000L,
            prepaymentFromInstallment = 13,
            prepaymentFrequency = "QUARTERLY",
            prepaymentStrategy = "REDUCE_TENURE",
            savedAtEpochMillis = 1L,
        )

        val restored = saved.toInputs()

        assertEquals(EmiInput("1000000", "8.5", "20", TenureUnit.YEARS), restored.loan)
        assertEquals("25000", restored.prepayment.amountText)
        assertEquals("13", restored.prepayment.fromInstallmentText)
        assertEquals(PrepaymentFrequency.QUARTERLY, restored.prepayment.frequency)
        assertEquals(PrepaymentStrategy.REDUCE_TENURE, restored.prepayment.strategy)
    }

    @Test
    fun aLoanWithoutPrepaymentRestoresTheBlankEditor() {
        val saved = SavedLoanEntity(
            title = "Car loan",
            principalRupees = 500_000L,
            rateMicroPercent = 12_000_000L,
            tenureMonths = 37,
            savedAtEpochMillis = 1L,
        )

        val restored = saved.toInputs()

        assertEquals("37", restored.loan.tenureText)
        assertEquals(TenureUnit.MONTHS, restored.loan.tenureUnit)
        assertEquals(PrepaymentInput(), restored.prepayment)
    }
}
