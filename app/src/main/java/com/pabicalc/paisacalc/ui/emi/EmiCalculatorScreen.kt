package com.pabicalc.paisacalc.ui.emi

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.pabicalc.paisacalc.export.LoanScheduleImageRenderer
import com.pabicalc.paisacalc.export.LoanSchedulePdfRenderer
import com.pabicalc.paisacalc.format.IndianNumberFormat
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Typing pause before recalculating: keeps slider drags and typing smooth. */
private const val RECALCULATION_DEBOUNCE_MS = 120L

private suspend fun writeCachedExport(
    context: Context,
    extension: String,
    render: (OutputStream) -> Unit,
): File = withContext(Dispatchers.IO) {
    val directory = File(context.cacheDir, "exports")
    check(directory.isDirectory || directory.mkdirs()) { "Could not create export cache" }
    val file = File.createTempFile("loan-schedule-", ".$extension", directory)
    FileOutputStream(file).use { output -> render(output) }
    file
}

private fun shareCachedExport(
    context: Context,
    file: File,
    mimeType: String,
    chooserTitle: String,
) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newUri(context.contentResolver, chooserTitle, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(
        Intent.createChooser(sendIntent, chooserTitle)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
    )
}

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
    var moreActionsExpanded by remember { mutableStateOf(false) }
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
    val pdfSavedMessage = stringResource(R.string.export_pdf_saved)
    val exportErrorMessage = stringResource(R.string.export_failed)
    val shareChooserTitle = stringResource(R.string.export_chooser_title)
    val moreOptionsDescription = stringResource(R.string.action_more_options)
    val context = LocalContext.current
    var pendingPdfSummary by remember { mutableStateOf<LoanSummary?>(null) }
    val pdfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri ->
        val summaryToExport = pendingPdfSummary
        pendingPdfSummary = null
        if (uri != null && summaryToExport != null) {
            coroutineScope.launch {
                val saved = runCatching {
                    withContext(Dispatchers.IO) {
                        val output = context.contentResolver.openOutputStream(uri)
                            ?: error("The document provider did not open the PDF output stream")
                        output.use { LoanSchedulePdfRenderer.write(summaryToExport, it) }
                    }
                }
                snackbarHostState.showSnackbar(
                    if (saved.isSuccess) pdfSavedMessage else exportErrorMessage,
                )
            }
        }
    }
    val shareScheduleImage: (LoanSummary) -> Unit = { summaryToExport ->
        coroutineScope.launch {
            try {
                val imageFile = writeCachedExport(context, "png") { output ->
                    LoanScheduleImageRenderer.writePng(summaryToExport, output)
                }
                shareCachedExport(context, imageFile, "image/png", shareChooserTitle)
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                snackbarHostState.showSnackbar(exportErrorMessage)
            }
        }
    }
    val shareSchedulePdf: (LoanSummary) -> Unit = { summaryToExport ->
        coroutineScope.launch {
            try {
                val pdfFile = writeCachedExport(context, "pdf") { output ->
                    LoanSchedulePdfRenderer.write(summaryToExport, output)
                }
                shareCachedExport(context, pdfFile, "application/pdf", shareChooserTitle)
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                snackbarHostState.showSnackbar(exportErrorMessage)
            }
        }
    }
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
                    Box {
                        IconButton(onClick = { moreActionsExpanded = true }) {
                            Text(
                                text = "⋮",
                                modifier = Modifier.semantics {
                                    contentDescription = moreOptionsDescription
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        DropdownMenu(
                            expanded = moreActionsExpanded,
                            onDismissRequest = { moreActionsExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_reset)) },
                                onClick = {
                                    amountText = ""
                                    rateText = ""
                                    tenureText = ""
                                    prepaymentInput = PrepaymentInput()
                                    moreActionsExpanded = false
                                },
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(
                                            if (isDarkTheme) R.string.action_theme_light else R.string.action_theme_dark,
                                        ),
                                    )
                                },
                                onClick = {
                                    moreActionsExpanded = false
                                    onToggleTheme()
                                },
                            )
                        }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .consumeWindowInsets(insets),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
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
                    item {
                        ExportActionsCard(
                            summary = summary,
                            onSavePdf = {
                                pendingPdfSummary = summary
                                pdfExportLauncher.launch("PaisaCalc-loan-schedule.pdf")
                            },
                            onSharePdf = { shareSchedulePdf(summary) },
                            onShareImage = { shareScheduleImage(summary) },
                        )
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
}
