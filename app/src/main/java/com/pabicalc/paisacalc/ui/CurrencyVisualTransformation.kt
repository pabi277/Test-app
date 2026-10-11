package com.pabicalc.paisacalc.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.pabicalc.paisacalc.format.IndianCurrencyCaret
import com.pabicalc.paisacalc.format.IndianNumberFormat

/**
 * Shows a digits-only field as Indian-grouped currency while typing:
 * the stored value stays `2500000`, the user sees `25,00,000`.
 *
 * The cursor math lives in [IndianCurrencyCaret] (pure, unit-tested); this
 * class is only the Compose face of it.
 */
class IndianCurrencyVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val grouped = IndianNumberFormat.group(raw)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                IndianCurrencyCaret.toTransformedOffset(raw.length, offset)
                    .coerceIn(0, grouped.length)

            override fun transformedToOriginal(offset: Int): Int =
                IndianCurrencyCaret.toRawOffset(grouped, offset)
        }
        return TransformedText(AnnotatedString(grouped), offsetMapping)
    }
}
