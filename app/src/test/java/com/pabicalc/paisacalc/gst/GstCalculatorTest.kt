package com.pabicalc.paisacalc.gst

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GstCalculatorTest {

    @Test
    fun addIntraStateGstSplitsTaxEvenly() {
        val result = GstCalculator.calculate(
            amountEnteredPaise = 100_000L, // ₹1,000.00
            rateMicroPercent = 18_000_000L,
            mode = GstCalculationMode.ADD,
            supplyType = GstSupplyType.INTRA_STATE,
        )

        assertEquals(100_000L, result.taxableValuePaise)
        assertEquals(18_000L, result.taxAmountPaise)
        assertEquals(118_000L, result.totalAmountPaise)
        assertEquals(9_000L, result.cgstPaise)
        assertEquals(9_000L, result.sgstPaise)
        assertEquals(0L, result.igstPaise)
    }

    @Test
    fun addInterStateGstIsOneIgstAmount() {
        val result = GstCalculator.calculate(
            amountEnteredPaise = 100_000L,
            rateMicroPercent = 5_000_000L,
            mode = GstCalculationMode.ADD,
            supplyType = GstSupplyType.INTER_STATE,
        )

        assertEquals(5_000L, result.taxAmountPaise)
        assertEquals(5_000L, result.igstPaise)
        assertEquals(0L, result.cgstPaise)
        assertEquals(0L, result.sgstPaise)
    }

    @Test
    fun removeGstFromTaxInclusivePriceInBothSupplyTypes() {
        val intra = GstCalculator.calculate(
            amountEnteredPaise = 118_000L,
            rateMicroPercent = 18_000_000L,
            mode = GstCalculationMode.REMOVE,
            supplyType = GstSupplyType.INTRA_STATE,
        )
        val inter = GstCalculator.calculate(
            amountEnteredPaise = 105_000L,
            rateMicroPercent = 5_000_000L,
            mode = GstCalculationMode.REMOVE,
            supplyType = GstSupplyType.INTER_STATE,
        )

        assertEquals(100_000L, intra.taxableValuePaise)
        assertEquals(18_000L, intra.taxAmountPaise)
        assertEquals(118_000L, intra.totalAmountPaise)
        assertEquals(9_000L, intra.cgstPaise)
        assertEquals(9_000L, intra.sgstPaise)

        assertEquals(100_000L, inter.taxableValuePaise)
        assertEquals(5_000L, inter.taxAmountPaise)
        assertEquals(105_000L, inter.totalAmountPaise)
        assertEquals(5_000L, inter.igstPaise)
    }

    @Test
    fun paiseRoundingIsHalfUpAndSplitAlwaysReconciles() {
        val result = GstCalculator.calculate(
            amountEnteredPaise = 11L, // 11 paise x 5% = 0.55 paise
            rateMicroPercent = 5_000_000L,
            mode = GstCalculationMode.ADD,
            supplyType = GstSupplyType.INTRA_STATE,
        )

        assertEquals(1L, result.taxAmountPaise)
        assertEquals(result.taxAmountPaise, result.cgstPaise + result.sgstPaise)
        assertEquals(1L, result.cgstPaise)
        assertEquals(0L, result.sgstPaise)
    }

    @Test
    fun zeroRateDoesNotChangeEitherCalculationDirection() {
        val added = GstCalculator.calculate(
            amountEnteredPaise = 12_345L,
            rateMicroPercent = 0L,
            mode = GstCalculationMode.ADD,
            supplyType = GstSupplyType.INTRA_STATE,
        )
        val removed = GstCalculator.calculate(
            amountEnteredPaise = 12_345L,
            rateMicroPercent = 0L,
            mode = GstCalculationMode.REMOVE,
            supplyType = GstSupplyType.INTER_STATE,
        )

        assertEquals(12_345L, added.totalAmountPaise)
        assertEquals(0L, added.taxAmountPaise)
        assertEquals(12_345L, removed.taxableValuePaise)
        assertEquals(0L, removed.taxAmountPaise)
    }

    @Test
    fun inputAndRateCapsAreEnforced() {
        assertThrows(IllegalArgumentException::class.java) {
            GstCalculator.calculate(
                amountEnteredPaise = GstCalculator.MAX_AMOUNT_PAISE + 1L,
                rateMicroPercent = 0L,
                mode = GstCalculationMode.ADD,
                supplyType = GstSupplyType.INTER_STATE,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            GstCalculator.calculate(
                amountEnteredPaise = 100L,
                rateMicroPercent = GstCalculator.MAX_RATE_MICRO_PERCENT + 1L,
                mode = GstCalculationMode.ADD,
                supplyType = GstSupplyType.INTER_STATE,
            )
        }
    }
}
