package com.pabicalc.paisacalc.ui.emi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pabicalc.paisacalc.R
import com.pabicalc.paisacalc.data.PaisaRepository
import com.pabicalc.paisacalc.data.SavedLoanEntity
import com.pabicalc.paisacalc.engine.EmiEngine
import com.pabicalc.paisacalc.engine.LoanMonth
import com.pabicalc.paisacalc.engine.LoanSummary
import com.pabicalc.paisacalc.engine.PrepaymentFrequency
import com.pabicalc.paisacalc.engine.PrepaymentPlan
import com.pabicalc.paisacalc.engine.PrepaymentStrategy
import com.pabicalc.paisacalc.format.IndianNumberFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Typing pause before recalculating: keeps slider drags and typing smooth. */
private const val RECALCULATION_DEBOUNCE_MS = 120L

/**
 * The prepayment editor survives rotation the same way the loan fields do:
 * save the raw text, rebuild the enums by name (tolerating a rename).
 */
private val PrepaymentInputSaver: Saver<PrepaymentInput, Any> = listSaver(
    save = { input ->
        listOf(input.amountText, input.fromInstallmentText, input.frequency.name, input.strategy.name)
    },
    restore = { saved ->
        PrepaymentInput(
            amountText = saved[0],
            fromInstallmentText = saved[1],
            frequency = PrepaymentFrequency.entries.firstOrNull { it.name == saved[2] }
                ?: PrepaymentFrequency.QUARTERLY,
            strategy = PrepaymentStrategy.entries.firstOrNull { it.name == saved[3] }
                ?: PrepaymentStrategy.REDUCE_TENURE,
        )
    },
)

/**
 * One tap to a complete, realistic scenario — the exact quarterly-prepayment
 * flow the phase-2 research asked for (₹10,00,000 at 8.5% for 20 years, then
 * ₹25,000 prepaid every quarter starting in year 2).
 */
private fun exampleInputs(): Pair<EmiInput, PrepaymentInput> = EmiInput(
    amountText = "1000000",
    rateText = "8.5",
    tenureText = "20",
    tenureUnit = TenureUnit.YEARS,
) to PrepaymentInput(
    amountText = "25000",
    fromInstallmentText = "13",
    frequency = PrepaymentFrequency.QUARTERLY,
    strategy = PrepaymentStrategy.REDUCE_TENURE,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiCalculatorScreen(
    repository: PaisaRepository,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var amountText by rememberSaveable { mutableStateOf("") }
    var rateText by rememberSaveable { mutableStateOf("") }
    var tenureText by rememberSaveable { mutableStateOf("") }
    var tenureUnit by rememberSaveable { mutableStateOf(TenureUnit.YEARS) }
    var prepaymentInput by rememberSaveable(stateSaver = PrepaymentInputSaver) {
        mutableStateOf(PrepaymentInput())
    }
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
    val parsedLoan = EmiInputParser.parse(input)
    val prepaymentParse = PrepaymentInputParser.parse(prepaymentInput)
    val savedLoans by repository.observeSavedLoans().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(R.string.saved_loan_saved_on_device)
    val loadedMessage = stringResource(R.string.saved_loan_loaded)
    val deletedMessage = stringResource(R.string.saved_loan_removed)
    val storageErrorMessage = stringResource(R.string.local_storage_error)
    val validTerms = (parsedLoan as? EmiParseResult.Valid)?.terms
    val generatedTitle = if (validTerms != null) {
        stringResource(
            R.string.saved_loan_title_format,
            IndianNumberFormat.compactRupees(validTerms.principal),
            DisplayFormatters.rateLabel(validTerms.rateMicroPercent),
            DisplayFormatters.tenureLabel(validTerms.tenureMonths),
        )
    } else {
        ""
    }
    val canSave = validTerms != null && prepaymentParse !is PrepaymentParseResult.Invalid

    // Parse -> Loading -> compute off the main thread -> Ready. Re-running on
    // every input change cancels the previous pass, so a stale result can
    // never overwrite a fresh one.
    LaunchedEffect(input, prepaymentInput, startMonth) {
        when (val parsed = EmiInputParser.parse(input)) {
            is EmiParseResult.Valid -> {
                state = EmiUiState.Loading
                delay(RECALCULATION_DEBOUNCE_MS)
                val plan = (PrepaymentInputParser.parse(prepaymentInput) as? PrepaymentParseResult.Active)
                    ?.plan
                    ?: PrepaymentPlan.NONE
                val summary = withContext(Dispatchers.Default) {
                    EmiEngine.calculate(parsed.terms, startMonth, plan)
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

    val saveCurrentLoan: () -> Unit = {
        val terms = validTerms
        if (terms != null) {
            val prepayment = (prepaymentParse as? PrepaymentParseResult.Active)
                ?.plan?.prepayments?.singleOrNull()
            val entity = SavedLoanEntity(
                title = generatedTitle,
                principalRupees = terms.principal,
                rateMicroPercent = terms.rateMicroPercent,
                tenureMonths = terms.tenureMonths,
                prepaymentAmountRupees = prepayment?.amount ?: 0L,
                prepaymentFromInstallment = prepayment?.fromInstallment
                    ?: PrepaymentInput.DEFAULT_FROM_INSTALLMENT,
                prepaymentFrequency = prepayment?.frequency?.name
                    ?: PrepaymentFrequency.QUARTERLY.name,
                prepaymentStrategy = prepayment?.strategy?.name
                    ?: PrepaymentStrategy.REDUCE_TENURE.name,
                savedAtEpochMillis = System.currentTimeMillis(),
            )
            coroutineScope.launch {
                try {
                    repository.saveLoan(entity)
                    snackbarHostState.showSnackbar(savedMessage)
                } catch (error: Exception) {
                    if (error is kotlinx.coroutines.CancellationException) throw error
                    snackbarHostState.showSnackbar(storageErrorMessage)
                }
            }
        }
    }
    val loadSavedLoan: (SavedLoanEntity) -> Unit = { saved ->
        val restored = saved.toInputs()
        amountText = restored.loan.amountText
        rateText = restored.loan.rateText
        tenureText = restored.loan.tenureText
        tenureUnit = restored.loan.tenureUnit
        prepaymentInput = restored.prepayment
        coroutineScope.launch { snackbarHostState.showSnackbar(loadedMessage) }
    }
    val deleteSavedLoan: (Long) -> Unit = { id ->
        coroutineScope.launch {
            try {
                repository.deleteLoan(id)
                snackbarHostState.showSnackbar(deletedMessage)
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                snackbarHostState.showSnackbar(storageErrorMessage)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            val (exampleLoan, examplePrepayment) = exampleInputs()
                            amountText = exampleLoan.amountText
                            rateText = exampleLoan.rateText
                            tenureText = exampleLoan.tenureText
                            tenureUnit = exampleLoan.tenureUnit
                            prepaymentInput = examplePrepayment
                        },
                    ) {
                        Text(stringResource(R.string.action_example))
                    }
                    TextButton(
                        onClick = {
                            amountText = ""
                            rateText = ""
                            tenureText = ""
                            prepaymentInput = PrepaymentInput()
                        },
                    ) {
                        Text(stringResource(R.string.action_reset))
                    }
                    TextButton(onClick = onToggleTheme) {
                        Text(
                            stringResource(
                                if (isDarkTheme) R.string.action_theme_light else R.string.action_theme_dark,
                            ),
                        )
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
                .consumeWindowInsets(insets)
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

            item {
                SavedLoansCard(
                    loans = savedLoans,
                    canSave = canSave,
                    onSave = saveCurrentLoan,
                    onLoad = loadSavedLoan,
                    onDelete = deleteSavedLoan,
                )
            }

            val tenureMonths = (parsedLoan as? EmiParseResult.Valid)?.terms?.tenureMonths
                ?: lastSummary?.terms?.tenureMonths
            item {
                PrepaymentCard(
                    input = prepaymentInput,
                    issues = (prepaymentParse as? PrepaymentParseResult.Invalid)?.issues.orEmpty(),
                    startMonth = startMonth,
                    tenureMonths = tenureMonths,
                    onInputChange = { prepaymentInput = it },
                )
            }

            val loadingSummary = if (state is EmiUiState.Loading) lastSummary else null
            item { ResultSection(state = state, previousSummary = loadingSummary) }

            val summary = (state as? EmiUiState.Ready)?.summary ?: loadingSummary
            if (summary != null) {
                if (summary.hasPrepayments) {
                    item { PrepaymentSavedCallout(summary = summary) }
                }
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
