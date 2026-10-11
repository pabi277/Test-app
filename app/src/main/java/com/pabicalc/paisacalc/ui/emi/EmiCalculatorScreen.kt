package com.pabicalc.paisacalc.ui.emi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pabicalc.paisacalc.R
import com.pabicalc.paisacalc.engine.EmiEngine
import com.pabicalc.paisacalc.engine.LoanMonth
import com.pabicalc.paisacalc.engine.LoanSummary
import com.pabicalc.paisacalc.ui.theme.PaisaCalcTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Typing pause before recalculating: keeps slider drags and typing smooth. */
private const val RECALCULATION_DEBOUNCE_MS = 120L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiCalculatorScreen(modifier: Modifier = Modifier) {
    var amountText by rememberSaveable { mutableStateOf("") }
    var rateText by rememberSaveable { mutableStateOf("") }
    var tenureText by rememberSaveable { mutableStateOf("") }
    var tenureUnit by rememberSaveable { mutableStateOf(TenureUnit.YEARS) }
    var state by remember { mutableStateOf<EmiUiState>(EmiUiState.Empty) }
    // The last completed result stays on screen while the next one is being
    // computed, so typing and slider drags never blank out the numbers.
    var lastSummary by remember { mutableStateOf<LoanSummary?>(null) }

    val input = EmiInput(
        amountText = amountText,
        rateText = rateText,
        tenureText = tenureText,
        tenureUnit = tenureUnit,
    )
    val startMonth = remember { LoanMonth.now() }

    // Parse -> Loading -> compute off the main thread -> Ready. Re-running on
    // every input change cancels the previous pass, so a stale result can
    // never overwrite a fresh one.
    LaunchedEffect(input, startMonth) {
        when (val parsed = EmiInputParser.parse(input)) {
            is EmiParseResult.Valid -> {
                state = EmiUiState.Loading
                delay(RECALCULATION_DEBOUNCE_MS)
                val summary = withContext(Dispatchers.Default) {
                    EmiEngine.calculate(parsed.terms, startMonth)
                }
                lastSummary = summary
                state = EmiUiState.Ready(summary)
            }
            else -> {
                lastSummary = null
                state = parsed.toUiState()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.top_bar_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            amountText = ""
                            rateText = ""
                            tenureText = ""
                        },
                    ) {
                        Text(stringResource(R.string.action_reset))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { insets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .imePadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { PrivacyPill() }

            item {
                InputCard(
                    input = input,
                    issues = state.issuesByField().values.toList(),
                    onInputChange = { updated ->
                        amountText = updated.amountText
                        rateText = updated.rateText
                        tenureText = updated.tenureText
                        tenureUnit = updated.tenureUnit
                    },
                )
            }

            val loadingSummary = if (state is EmiUiState.Loading) lastSummary else null
            item { ResultSection(state = state, previousSummary = loadingSummary) }

            val summary = (state as? EmiUiState.Ready)?.summary ?: loadingSummary
            if (summary != null) {
                item { BreakdownCard(summary = summary) }
                item { ScheduleHeaderCard(summary = summary) }

                // Derived per recomposition: chunking at most 480 rows is free.
                val years = ScheduleGrouper.byLoanYear(summary)
                years.forEach { year ->
                    item(key = "year-${year.index}") { ScheduleYearHeader(year = year) }
                    items(items = year.installments, key = { it.number }) { installment ->
                        ScheduleRow(installment = installment)
                    }
                }

                if (summary.hasAdjustedFinalInstallment) {
                    item { FinalInstallmentNote(summary = summary) }
                }
                item { ScheduleEstimateNote() }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
private fun EmiCalculatorScreenPreview() {
    PaisaCalcTheme {
        EmiCalculatorScreen()
    }
}
