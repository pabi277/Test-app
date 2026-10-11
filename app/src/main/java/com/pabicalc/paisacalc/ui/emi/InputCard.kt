@file:OptIn(ExperimentalMaterial3Api::class)

package com.pabicalc.paisacalc.ui.emi

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pabicalc.paisacalc.R
import com.pabicalc.paisacalc.format.IndianNumberFormat
import com.pabicalc.paisacalc.ui.IndianCurrencyVisualTransformation
import kotlin.math.roundToInt

private const val AMOUNT_SLIDER_MIN = 100_000L        // ₹1 L
private const val AMOUNT_SLIDER_MAX = 20_000_000L     // ₹2 Cr
private const val AMOUNT_SLIDER_STEP = 25_000L
private const val AMOUNT_SLIDER_STEPS = ((AMOUNT_SLIDER_MAX - AMOUNT_SLIDER_MIN) / AMOUNT_SLIDER_STEP - 1).toInt()

private const val RATE_SLIDER_MIN = 1f
private const val RATE_SLIDER_MAX = 24f
private const val RATE_SLIDER_STEPS = 459             // 1% .. 24% in 0.05% steps

private const val TENURE_SLIDER_MIN_YEARS = 1
private const val TENURE_SLIDER_MAX_YEARS = 30
private const val TENURE_SLIDER_STEPS = TENURE_SLIDER_MAX_YEARS - TENURE_SLIDER_MIN_YEARS - 1

private const val DEFAULT_RATE_PERCENT = 8.5f
private const val DEFAULT_TENURE_YEARS = 20

private val QUICK_AMOUNTS = listOf(500_000L, 1_000_000L, 2_500_000L, 5_000_000L)

/**
 * The three loan inputs — amount, rate, tenure — each as a field plus a slider,
 * so a whole calculation is one thumb-drag away. Fields keep digits-only text;
 * the amount field *displays* Indian grouping via the visual transformation.
 */
@Composable
fun InputCard(
    input: EmiInput,
    issues: List<FieldIssue>,
    onInputChange: (EmiInput) -> Unit,
    modifier: Modifier = Modifier,
) {
    val issueByField = issues.associateBy { it.field }
    val amountIssue = issueByField[EmiField.AMOUNT]
    val rateIssue = issueByField[EmiField.RATE]
    val tenureIssue = issueByField[EmiField.TENURE]

    val amount = IndianNumberFormat.parseGrouped(input.amountText)
    val rateMicroPercent = EmiInputParser.rateMicroPercent(input.rateText)
    val tenureValue = input.tenureText.toLongOrNull()
    val amountSliderLabel = stringResource(R.string.slider_loan_amount_description)
    val rateSliderLabel = stringResource(R.string.slider_interest_rate_description)
    val tenureSliderLabel = stringResource(R.string.slider_tenure_description)
    val amountSliderState = IndianNumberFormat.rupees(amount ?: AMOUNT_SLIDER_MIN)
    val rateSliderState = DisplayFormatters.rateLabel(
        rateMicroPercent ?: (DEFAULT_RATE_PERCENT * 1_000_000f).toLong(),
    )
    val tenureSliderState = if (input.tenureUnit == TenureUnit.YEARS) {
        "${tenureValue ?: DEFAULT_TENURE_YEARS.toLong()} years"
    } else {
        "${tenureValue ?: DEFAULT_TENURE_YEARS * 12L} months"
    }

    SectionCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.input_card_title),
            style = MaterialTheme.typography.titleMedium,
        )

        // ---- Loan amount -------------------------------------------------
        OutlinedTextField(
            value = input.amountText,
            onValueChange = { onInputChange(input.copy(amountText = EmiInputSanitizer.amount(it))) },
            label = { Text(stringResource(R.string.field_amount_label)) },
            prefix = { Text(stringResource(R.string.currency_symbol)) },
            singleLine = true,
            isError = amountIssue != null,
            supportingText = if (amountIssue != null) {
                { Text(stringResource(amountIssue.messageRes())) }
            } else {
                null
            },
            visualTransformation = IndianCurrencyVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        Slider(
            value = (amount ?: AMOUNT_SLIDER_MIN).toFloat()
                .coerceIn(AMOUNT_SLIDER_MIN.toFloat(), AMOUNT_SLIDER_MAX.toFloat()),
            onValueChange = { onInputChange(input.copy(amountText = it.toLong().toString())) },
            valueRange = AMOUNT_SLIDER_MIN.toFloat()..AMOUNT_SLIDER_MAX.toFloat(),
            steps = AMOUNT_SLIDER_STEPS,
            modifier = Modifier.semantics {
                contentDescription = amountSliderLabel
                stateDescription = amountSliderState
            },
        )
        SliderEnds(
            startText = IndianNumberFormat.compactRupees(AMOUNT_SLIDER_MIN),
            endText = IndianNumberFormat.compactRupees(AMOUNT_SLIDER_MAX),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QUICK_AMOUNTS.forEach { quickAmount ->
                FilterChip(
                    selected = amount == quickAmount,
                    onClick = { onInputChange(input.copy(amountText = quickAmount.toString())) },
                    label = { Text(IndianNumberFormat.compactRupees(quickAmount)) },
                )
            }
        }

        // ---- Interest rate -----------------------------------------------
        OutlinedTextField(
            value = input.rateText,
            onValueChange = { onInputChange(input.copy(rateText = EmiInputSanitizer.rate(it))) },
            label = { Text(stringResource(R.string.field_rate_label)) },
            suffix = { Text("%") },
            singleLine = true,
            isError = rateIssue != null,
            supportingText = if (rateIssue != null) {
                { Text(stringResource(rateIssue.messageRes())) }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        Slider(
            value = (rateMicroPercent?.let { it / 1_000_000.0 } ?: DEFAULT_RATE_PERCENT.toDouble())
                .toFloat()
                .coerceIn(RATE_SLIDER_MIN, RATE_SLIDER_MAX),
            onValueChange = {
                onInputChange(input.copy(rateText = DisplayFormatters.sliderRateText(it)))
            },
            valueRange = RATE_SLIDER_MIN..RATE_SLIDER_MAX,
            steps = RATE_SLIDER_STEPS,
            modifier = Modifier.semantics {
                contentDescription = rateSliderLabel
                stateDescription = rateSliderState
            },
        )
        SliderEnds(
            startText = DisplayFormatters.rateLabel((RATE_SLIDER_MIN * 1_000_000).toLong()),
            endText = DisplayFormatters.rateLabel((RATE_SLIDER_MAX * 1_000_000).toLong()),
        )

        // ---- Tenure ------------------------------------------------------
        OutlinedTextField(
            value = input.tenureText,
            onValueChange = { onInputChange(input.copy(tenureText = EmiInputSanitizer.tenure(it))) },
            label = { Text(stringResource(R.string.field_tenure_label)) },
            suffix = {
                Text(
                    stringResource(
                        if (input.tenureUnit == TenureUnit.YEARS) {
                            R.string.tenure_suffix_years
                        } else {
                            R.string.tenure_suffix_months
                        },
                    ),
                )
            },
            singleLine = true,
            isError = tenureIssue != null,
            supportingText = if (tenureIssue != null) {
                { Text(stringResource(tenureIssue.messageRes())) }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = input.tenureUnit == TenureUnit.YEARS,
                onClick = { onInputChange(withTenureUnit(input, TenureUnit.YEARS)) },
                label = { Text(stringResource(R.string.tenure_unit_years)) },
            )
            FilterChip(
                selected = input.tenureUnit == TenureUnit.MONTHS,
                onClick = { onInputChange(withTenureUnit(input, TenureUnit.MONTHS)) },
                label = { Text(stringResource(R.string.tenure_unit_months)) },
            )
        }
        Slider(
            value = tenureInYears(input.tenureUnit, tenureValue)
                .coerceIn(TENURE_SLIDER_MIN_YEARS.toFloat(), TENURE_SLIDER_MAX_YEARS.toFloat()),
            onValueChange = { onInputChange(withTenureYears(input, it.roundToInt())) },
            valueRange = TENURE_SLIDER_MIN_YEARS.toFloat()..TENURE_SLIDER_MAX_YEARS.toFloat(),
            steps = TENURE_SLIDER_STEPS,
            modifier = Modifier.semantics {
                contentDescription = tenureSliderLabel
                stateDescription = tenureSliderState
            },
        )
        SliderEnds(
            startText = "$TENURE_SLIDER_MIN_YEARS yr",
            endText = "$TENURE_SLIDER_MAX_YEARS yrs",
        )
    }
}

/**
 * Switching the tenure unit converts the current value so the loan does not
 * silently change meaning ("20" years becomes "240" months).
 */
private fun withTenureUnit(input: EmiInput, unit: TenureUnit): EmiInput {
    if (unit == input.tenureUnit) return input
    val value = input.tenureText.toLongOrNull() ?: return input.copy(tenureUnit = unit)
    val converted = if (unit == TenureUnit.MONTHS) {
        (value * 12L).toString()
    } else {
        when {
            value <= 0L -> "0"
            else -> (value / 12.0).roundToInt().coerceAtLeast(1).toString()
        }
    }
    return input.copy(tenureText = EmiInputSanitizer.tenure(converted), tenureUnit = unit)
}

/** Sets the tenure from a years slider position, respecting the active unit. */
private fun withTenureYears(input: EmiInput, years: Int): EmiInput {
    val text = if (input.tenureUnit == TenureUnit.YEARS) years.toString() else (years * 12).toString()
    return input.copy(tenureText = EmiInputSanitizer.tenure(text))
}

private fun tenureInYears(unit: TenureUnit, tenureValue: Long?): Float {
    val value = tenureValue ?: return DEFAULT_TENURE_YEARS.toFloat()
    return if (unit == TenureUnit.YEARS) value.toFloat() else value / 12f
}

@Composable
private fun SliderEnds(startText: String, endText: String) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = startText, style = MaterialTheme.typography.labelSmall, color = muted)
        Spacer(Modifier.weight(1f))
        Text(text = endText, style = MaterialTheme.typography.labelSmall, color = muted)
    }
}

/** Maps an issue code to the message shown under the field. */
fun FieldIssue.messageRes(): Int = when (this) {
    FieldIssue.AMOUNT_NOT_POSITIVE -> R.string.issue_amount_not_positive
    FieldIssue.AMOUNT_TOO_LARGE -> R.string.issue_amount_too_large
    FieldIssue.RATE_INVALID -> R.string.issue_rate_invalid
    FieldIssue.TENURE_NOT_POSITIVE -> R.string.issue_tenure_not_positive
    FieldIssue.TENURE_TOO_LONG -> R.string.issue_tenure_too_long
}

/** The offline/privacy promise, stated once at the top of the screen. */
@Composable
fun PrivacyPill(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(top = 4.dp),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "●",
                modifier = Modifier.clearAndSetSemantics { },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.privacy_pill),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
