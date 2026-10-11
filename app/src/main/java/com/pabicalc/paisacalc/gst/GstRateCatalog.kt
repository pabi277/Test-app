package com.pabicalc.paisacalc.gst

/** A quick-pick rate from the dated, offline GST asset. */
data class GstRatePreset(
    val id: String,
    /** Decimal percentage text, e.g. "0.25" or "18". */
    val rateText: String,
    val label: String,
    val note: String,
) {
    val rateMicroPercent: Long
        get() = requireNotNull(GstCalculator.parseRateMicroPercent(rateText))
}

/**
 * A versioned convenience list, not an HSN-wise or legally authoritative
 * classification of goods and services.
 */
data class GstRateCatalog(
    val version: String,
    val effectiveFrom: String,
    val source: String,
    val note: String,
    val presets: List<GstRatePreset>,
)

/** Small pipe-delimited asset parser; deliberately independent of Android. */
object GstRateCatalogParser {

    fun parse(assetText: String): GstRateCatalog {
        val metadata = LinkedHashMap<String, String>()
        val rates = ArrayList<GstRatePreset>()
        var sawHeader = false

        assetText.lineSequence().forEachIndexed { index, rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEachIndexed
            if (line.startsWith("#")) {
                val content = line.removePrefix("#").trim()
                val separator = content.indexOf('=')
                if (separator > 0) {
                    metadata[content.substring(0, separator).trim()] =
                        content.substring(separator + 1).trim()
                }
                return@forEachIndexed
            }
            if (!sawHeader) {
                require(line == HEADER) { "Unexpected GST rate asset header on line ${index + 1}" }
                sawHeader = true
                return@forEachIndexed
            }

            val fields = line.split('|')
            require(fields.size == 4) { "Malformed GST rate asset row on line ${index + 1}" }
            val (id, rateText, label, note) = fields.map { it.trim() }
            require(id.isNotEmpty() && label.isNotEmpty()) {
                "GST rate asset row ${index + 1} needs an id and label"
            }
            val rate = GstCalculator.parseRateMicroPercent(rateText)
            require(rate != null && rate in 0..GstCalculator.MAX_RATE_MICRO_PERCENT) {
                "Invalid GST rate '$rateText' on line ${index + 1}"
            }
            rates += GstRatePreset(id = id, rateText = rateText, label = label, note = note)
        }

        require(sawHeader) { "GST rate asset header is missing" }
        require(rates.isNotEmpty()) { "GST rate asset has no presets" }
        require(rates.map { it.id }.distinct().size == rates.size) { "GST rate preset ids must be unique" }
        return GstRateCatalog(
            version = requireNotNull(metadata["version"]) { "GST rate asset version is missing" },
            effectiveFrom = requireNotNull(metadata["effective_from"]) {
                "GST rate asset effective date is missing"
            },
            source = requireNotNull(metadata["source"]) { "GST rate asset source is missing" },
            note = metadata["note"].orEmpty(),
            presets = rates,
        )
    }

    private const val HEADER = "id|rate_percent|label|note"
}
