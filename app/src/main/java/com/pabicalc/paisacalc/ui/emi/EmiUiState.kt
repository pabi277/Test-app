package com.pabicalc.paisacalc.ui.emi

import com.pabicalc.paisacalc.engine.LoanSummary

/**
 * What the result area is showing right now.
 *
 * The four states are all reachable in the shipped build:
 * [Empty] before enough input, [Loading] while recalculating, [Invalid] for
 * out-of-range input, [Ready] once a summary exists.
 */
sealed interface EmiUiState {

    /** Nothing to calculate yet (blank fields). */
    data object Empty : EmiUiState

    /** Input is valid; the engine is running (debounced). */
    data object Loading : EmiUiState

    /** Input cannot be calculated; show the issues. */
    data class Invalid(val issues: List<FieldIssue>) : EmiUiState

    /** A calculated result. */
    data class Ready(val summary: LoanSummary) : EmiUiState
}

/**
 * The state a parse result implies before any calculation happens.
 * `Valid` maps to [EmiUiState.Loading] because the summary is still being
 * computed at that point.
 */
fun EmiParseResult.toUiState(): EmiUiState = when (this) {
    EmiParseResult.Incomplete -> EmiUiState.Empty
    is EmiParseResult.Invalid -> EmiUiState.Invalid(issues)
    is EmiParseResult.Valid -> EmiUiState.Loading
}

/** Per-field lookup for inline field errors. */
fun EmiUiState.issuesByField(): Map<EmiField, FieldIssue> =
    (this as? EmiUiState.Invalid)?.issues?.associateBy { it.field }.orEmpty()
