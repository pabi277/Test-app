package com.pabicalc.paisacalc

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Phase 0 smoke test: proves the unit-test pipeline runs on CI.
 * The real EMI-engine test suite lands in phase 1.
 */
class SmokeTest {

    @Test
    fun arithmeticPipelineWorks() {
        assertEquals(4, 2 + 2)
    }
}
