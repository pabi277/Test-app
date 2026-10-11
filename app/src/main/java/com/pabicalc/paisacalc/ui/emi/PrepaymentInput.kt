package com.pabicalc.paisacalc.ui.emi

import com.pabicalc.paisacalc.engine.LoanTerms
import com.pabicalc.paisacalc.engine.Prepayment
import com.pabicalc.paisacalc.engine.PrepaymentFrequency
import com.pabicalc.paisacalc.engine.PrepaymentPlan
import com.pabicalc.paisacalc.engine.PrepaymentStrategy

/** Which prepayment field an issue belongs to. */
enum class PrepaymentField { AMOUNT, FROM_INSTALLMENT }

/**
 * What is wrong with a prepayment field. The parser returns these codes
 * (testable without Android); the UI maps each code to a message.
 */
enum class PrepaymentIssue(val field: PrepaymentField) {
    AMOUNT_INVALID(PrepaymentField.AMOUNT),
    FROM_INSTALLMENT_INVALID(PrepaymentField.FROM_INSTALLMENT),
}

/**
 * The raw text state of the prepayment editor, plus the two choices.
 *
 * A blank amount means "no prepayment" — the editor is always visible, but it
 * only changes the numbers once an amount is typed. The default start month is
 * installment 13 (the second year of a loan that started this month), which is
 * the "add payment amount & starting month" flow the store-review research
 * asked for.
 */
data class PrepaymentInput(
    val amountText: String = "",
    val fromInstallmentText: String = DEFAULT_FROM_INSTALLMENT.toString(),
    val frequency: PrepaymentFrequency = PrepaymentFrequency.QUARTERLY,
    val strategy: PrepaymentStrategy = PrepaymentStrategy.REDUCE_TENURE,
) {
    companion object {
        const val DEFAULT_FROM_INSTALLMENT = 13
    }
}

sealed interface PrepaymentParseResult {

    /** No amount typed: no plan, nothing to recalculate. */
    data object Inactive : PrepaymentParseResult

    /** The amount is typed but something is out of range. */
    data class Invalid(val issues: List<PrepaymentIssue>) : PrepaymentParseResult

    /** A usable plan; the engine will apply it. */
    data class Active(val plan: PrepaymentPlan) : PrepaymentParseResult
}

/** Keeps the prepayment fields typeable, with the engine's own caps. */
object PrepaymentInputSanitizer {

    fun amount(text: String): String = text.filter { it.isDigit() }.take(MAX_AMOUNT_DIGITS)

    fun installment(text: String): String = text.filter { it.isDigit() }.take(MAX_INSTALLMENT_DIGITS)

    /** 12 digits: enough to type past ₹1,00,00,00,000 and see the error. */
    private const val MAX_AMOUNT_DIGITS = 12
    private const val MAX_INSTALLMENT_DIGITS = 3
}

/**
 * Turns the editor's text into a [PrepaymentPlan], or says exactly why not.
 * Blank amount -> [PrepaymentParseResult.Inactive]; out-of-range values ->
 * [PrepaymentParseResult.Invalid]; anything else -> [PrepaymentParseResult.Active].
 */
object PrepaymentInputParser {

    fun parse(input: PrepaymentInput): PrepaymentParseResult {
        val amountDigits = input.amountText.filter { it.isDigit() }
        if (amountDigits.isEmpty()) return PrepaymentParseResult.Inactive

        val issues = ArrayList<PrepaymentIssue>(2)

        val amount = amountDigits.toLongOrNull()
        if (amount == null || amount < 1L || amount > LoanTerms.MAX_PRINCIPAL) {
            issues += PrepaymentIssue.AMOUNT_INVALID
        }

        val installment = input.fromInstallmentText.filter { it.isDigit() }.toIntOrNull()
        if (installment == null || installment < 1 || installment > LoanTerms.MAX_TENURE_MONTHS) {
            issues += PrepaymentIssue.FROM_INSTALLMENT_INVALID
        }

        if (issues.isNotEmpty()) return PrepaymentParseResult.Invalid(issues)

        return PrepaymentParseResult.Active(
            PrepaymentPlan(
                listOf(
                    Prepayment(
                        frequency = input.frequency,
                        fromInstallment = requireNotNull(installment),
                        amount = requireNotNull(amount),
                        strategy = input.strategy,
                    ),
                ),
            ),
        )
    }
}
