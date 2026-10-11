package com.pabicalc.paisacalc.gst

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GstRateCatalogParserTest {

    @Test
    fun loadsTheCheckedInDatedPresetList() {
        val catalog = GstRateCatalogParser.parse(
            """
            # version=2025-09-22-v1
            # effective_from=2025-09-22
            # source=https://example.test/official
            # note=Verify current rates.
            id|rate_percent|label|note
            nil|0|Nil|Nil or exempt
            special-025|0.25|0.25%|Specified goods
            standard-18|18|18%|Common rate
            demerit-40|40|40%|Specified goods
            """.trimIndent(),
        )

        assertEquals("2025-09-22-v1", catalog.version)
        assertEquals("2025-09-22", catalog.effectiveFrom)
        assertEquals(4, catalog.presets.size)
        assertEquals(250_000L, catalog.presets[1].rateMicroPercent)
        assertEquals("18%", catalog.presets[2].label)
        assertTrue(catalog.note.contains("Verify"))
    }

    @Test
    fun malformedRowsAndDuplicateIdsAreRejected() {
        val missingField = """
            # version=1
            # effective_from=2025-09-22
            # source=https://example.test
            id|rate_percent|label|note
            one|18|18%
        """.trimIndent()
        val duplicateIds = """
            # version=1
            # effective_from=2025-09-22
            # source=https://example.test
            id|rate_percent|label|note
            same|5|5%|one
            same|18|18%|two
        """.trimIndent()

        assertTrue(runCatching { GstRateCatalogParser.parse(missingField) }.isFailure)
        assertTrue(runCatching { GstRateCatalogParser.parse(duplicateIds) }.isFailure)
    }
}
