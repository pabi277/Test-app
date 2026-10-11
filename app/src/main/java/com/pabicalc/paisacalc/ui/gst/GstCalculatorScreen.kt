@file:OptIn(ExperimentalMaterial3Api::class)

package com.pabicalc.paisacalc.ui.gst

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pabicalc.paisacalc.R
import com.pabicalc.paisacalc.data.GstHistoryEntity
import com.pabicalc.paisacalc.data.PaisaRepository
import com.pabicalc.paisacalc.format.IndianNumberFormat
import com.pabicalc.paisacalc.gst.GstCalculation
import com.pabicalc.paisacalc.gst.GstCalculationMode
import com.pabicalc.paisacalc.gst.GstCalculator
import com.pabicalc.paisacalc.gst.GstFieldIssue
import com.pabicalc.paisacalc.gst.GstInput
import com.pabicalc.paisacalc.gst.GstInputParser
import com.pabicalc.paisacalc.gst.GstInputSanitizer
import com.pabicalc.paisacalc.gst.GstParseResult
import com.pabicalc.paisacalc.gst.GstRateCatalog
import com.pabicalc.paisacalc.gst.GstRateCatalogParser
import com.pabicalc.paisacalc.gst.GstRatePreset
import com.pabicalc.paisacalc.gst.GstSupplyType
import com.pabicalc.paisacalc.ui.emi.PrivacyPill
import com.pabicalc.paisacalc.ui.emi.SectionCard
import kotlinx.coroutines.launch

private const val GST_RATE_ASSET = "gst-rate-slabs.txt"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GstCalculatorScreen(
    repository: PaisaRepository,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val catalog = remember {
        context.assets.open(GST_RATE_ASSET).bufferedReader().use { reader ->
            GstRateCatalogParser.parse(reader.readText())
        }
    }
    var amountText by rememberSaveable { mutableStateOf("") }
    var mode by rememberSaveable { mutableStateOf(GstCalculationMode.ADD) }
    var supplyType by rememberSaveable { mutableStateOf(GstSupplyType.INTRA_STATE) }
    var selectedRateId by rememberSaveable { mutableStateOf<String?>("standard-18") }
    var customRateText by rememberSaveable { mutableStateOf("") }

    val selectedPreset = if (selectedRateId == null) {
        null
    } else {
        catalog.presets.firstOrNull { it.id == selectedRateId }
            ?: catalog.presets.firstOrNull { it.id == "standard-18" }
    }
    val effectiveRateId = if (selectedRateId == null) null else selectedPreset?.id
    val rateText = selectedPreset?.rateText ?: customRateText
    val parsed = GstInputParser.parse(
        GstInput(
            amountText = amountText,
            rateText = rateText,
            mode = mode,
            supplyType = supplyType,
        ),
    )
    val calculation = (parsed as? GstParseResult.Valid)?.calculation
    val history by repository.observeGstHistory().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(R.string.gst_history_saved)
    val deletedMessage = stringResource(R.string.gst_history_removed)
    val storageErrorMessage = stringResource(R.string.local_storage_error)

    val saveCalculation: (GstCalculation) -> Unit = { result ->
        coroutineScope.launch {
            try {
                repository.saveGstCalculation(result)
                snackbarHostState.showSnackbar(savedMessage)
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                snackbarHostState.showSnackbar(storageErrorMessage)
            }
        }
    }
    val deleteHistory: (Long) -> Unit = { id ->
        coroutineScope.launch {
            try {
                repository.deleteGstHistory(id)
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
                            text = stringResource(R.string.gst_top_bar_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { PrivacyPill() }
                item {
                    GstInputCard(
                        amountText = amountText,
                        mode = mode,
                        supplyType = supplyType,
                        catalog = catalog,
                        selectedRateId = effectiveRateId,
                        customRateText = customRateText,
                        parseResult = parsed,
                        onAmountChange = { amountText = GstInputSanitizer.amount(it) },
                        onModeChange = { mode = it },
                        onSupplyTypeChange = { supplyType = it },
                        onPresetSelected = { selectedRateId = it },
                        onCustomSelected = { selectedRateId = null },
                        onCustomRateChange = { customRateText = GstInputSanitizer.rate(it) },
                    )
                }
                if (calculation != null) {
                    item {
                        GstResultCard(
                            calculation = calculation,
                            onSave = { saveCalculation(calculation) },
                        )
                    }
                }
                item {
                    GstHistoryCard(
                        history = history,
                        onDelete = deleteHistory,
                    )
                }
            }
        }
    }
}

@Composable
private fun GstInputCard(
    amountText: String,
    mode: GstCalculationMode,
    supplyType: GstSupplyType,
    catalog: GstRateCatalog,
    selectedRateId: String?,
    customRateText: String,
    parseResult: GstParseResult,
    onAmountChange: (String) -> Unit,
    onModeChange: (GstCalculationMode) -> Unit,
    onSupplyTypeChange: (GstSupplyType) -> Unit,
    onPresetSelected: (String) -> Unit,
    onCustomSelected: () -> Unit,
    onCustomRateChange: (String) -> Unit,
) {
    val issues = (parseResult as? GstParseResult.Invalid)?.issues.orEmpty()
    val amountError = issues.any {
        it == GstFieldIssue.AMOUNT_NOT_POSITIVE || it == GstFieldIssue.AMOUNT_TOO_LARGE
    }
    val rateError = GstFieldIssue.RATE_INVALID in issues

    SectionCard {
        Text(
            text = stringResource(R.string.gst_calculator_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.gst_calculator_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = mode == GstCalculationMode.ADD,
                onClick = { onModeChange(GstCalculationMode.ADD) },
                label = { Text(stringResource(R.string.gst_add_mode)) },
            )
            FilterChip(
                selected = mode == GstCalculationMode.REMOVE,
                onClick = { onModeChange(GstCalculationMode.REMOVE) },
                label = { Text(stringResource(R.string.gst_remove_mode)) },
            )
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = onAmountChange,
            label = {
                Text(
                    stringResource(
                        if (mode == GstCalculationMode.ADD) {
                            R.string.gst_amount_before_label
                        } else {
                            R.string.gst_amount_including_label
                        },
                    ),
                )
            },
            prefix = { Text(stringResource(R.string.currency_symbol)) },
            placeholder = { Text(stringResource(R.string.gst_amount_placeholder)) },
            supportingText = if (amountError) {
                {
                    Text(
                        stringResource(
                            if (GstFieldIssue.AMOUNT_TOO_LARGE in issues) {
                                R.string.gst_amount_too_large
                            } else {
                                R.string.gst_amount_invalid
                            },
                        ),
                    )
                }
            } else {
                { Text(stringResource(R.string.gst_amount_helper)) }
            },
            isError = amountError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = stringResource(R.string.gst_supply_type_label),
            style = MaterialTheme.typography.labelLarge,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = supplyType == GstSupplyType.INTRA_STATE,
                onClick = { onSupplyTypeChange(GstSupplyType.INTRA_STATE) },
                label = { Text(stringResource(R.string.gst_intra_state)) },
            )
            FilterChip(
                selected = supplyType == GstSupplyType.INTER_STATE,
                onClick = { onSupplyTypeChange(GstSupplyType.INTER_STATE) },
                label = { Text(stringResource(R.string.gst_inter_state)) },
            )
        }

        Text(
            text = stringResource(R.string.gst_rate_label),
            style = MaterialTheme.typography.labelLarge,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            catalog.presets.forEach { preset ->
                FilterChip(
                    selected = selectedRateId == preset.id,
                    onClick = { onPresetSelected(preset.id) },
                    label = { Text(preset.label) },
                )
            }
            FilterChip(
                selected = selectedRateId == null,
                onClick = onCustomSelected,
                label = { Text(stringResource(R.string.gst_custom_rate)) },
            )
        }
        if (selectedRateId == null) {
            OutlinedTextField(
                value = customRateText,
                onValueChange = onCustomRateChange,
                label = { Text(stringResource(R.string.gst_custom_rate_label)) },
                suffix = { Text("%") },
                supportingText = if (rateError) {
                    { Text(stringResource(R.string.gst_rate_invalid)) }
                } else {
                    { Text(stringResource(R.string.gst_custom_rate_helper)) }
                },
                isError = rateError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Text(
            text = stringResource(R.string.gst_rate_asset_as_of, catalog.effectiveFrom),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.gst_rate_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GstResultCard(
    calculation: GstCalculation,
    onSave: () -> Unit,
) {
    SectionCard(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        borderColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = stringResource(
                if (calculation.mode == GstCalculationMode.ADD) {
                    R.string.gst_result_added_title
                } else {
                    R.string.gst_result_removed_title
                },
            ),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        GstValueRow(
            label = stringResource(
                if (calculation.mode == GstCalculationMode.ADD) {
                    R.string.gst_taxable_value
                } else {
                    R.string.gst_amount_entered
                },
            ),
            value = IndianNumberFormat.rupeesAndPaise(
                if (calculation.mode == GstCalculationMode.ADD) {
                    calculation.taxableValuePaise
                } else {
                    calculation.amountEnteredPaise
                },
            ),
        )
        if (calculation.mode == GstCalculationMode.REMOVE) {
            GstValueRow(
                label = stringResource(R.string.gst_taxable_value),
                value = IndianNumberFormat.rupeesAndPaise(calculation.taxableValuePaise),
            )
        }
        GstValueRow(
            label = stringResource(
                R.string.gst_tax_amount_at_rate,
                GstCalculator.rateText(calculation.rateMicroPercent),
            ),
            value = IndianNumberFormat.rupeesAndPaise(calculation.taxAmountPaise),
        )
        when (calculation.supplyType) {
            GstSupplyType.INTRA_STATE -> {
                GstValueRow(
                    label = stringResource(
                        R.string.gst_cgst_at_rate,
                        GstCalculator.rateText(calculation.rateMicroPercent / 2L),
                    ),
                    value = IndianNumberFormat.rupeesAndPaise(calculation.cgstPaise),
                )
                GstValueRow(
                    label = stringResource(
                        R.string.gst_sgst_at_rate,
                        GstCalculator.rateText(calculation.rateMicroPercent / 2L),
                    ),
                    value = IndianNumberFormat.rupeesAndPaise(calculation.sgstPaise),
                )
            }
            GstSupplyType.INTER_STATE -> GstValueRow(
                label = stringResource(
                    R.string.gst_igst_at_rate,
                    GstCalculator.rateText(calculation.rateMicroPercent),
                ),
                value = IndianNumberFormat.rupeesAndPaise(calculation.igstPaise),
            )
        }
        GstValueRow(
            label = stringResource(
                if (calculation.mode == GstCalculationMode.ADD) {
                    R.string.gst_total_including
                } else {
                    R.string.gst_total_entered
                },
            ),
            value = IndianNumberFormat.rupeesAndPaise(calculation.totalAmountPaise),
            emphasize = true,
        )
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.gst_save_to_history))
        }
    }
}

@Composable
private fun GstValueRow(label: String, value: String, emphasize: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = if (emphasize) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Text(
            text = value,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun GstHistoryCard(
    history: List<GstHistoryEntity>,
    onDelete: (Long) -> Unit,
) {
    SectionCard {
        Text(
            text = stringResource(R.string.gst_history_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.gst_history_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (history.isEmpty()) {
            Text(
                text = stringResource(R.string.gst_history_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            history.forEach { row ->
                val mode = when (row.mode) {
                    GstCalculationMode.ADD.name -> stringResource(R.string.gst_add_mode)
                    else -> stringResource(R.string.gst_remove_mode)
                }
                val historyRateText = GstCalculator.rateText(row.rateMicroPercent)
                val title = stringResource(
                    R.string.gst_history_item_title,
                    mode,
                    historyRateText,
                )
                val removeDescription = stringResource(
                    R.string.remove_gst_history_accessibility,
                    mode,
                    historyRateText,
                )
                val detail = stringResource(
                    R.string.gst_history_item_detail,
                    IndianNumberFormat.rupeesAndPaise(row.amountEnteredPaise),
                    IndianNumberFormat.rupeesAndPaise(row.taxAmountPaise),
                    IndianNumberFormat.rupeesAndPaise(row.totalAmountPaise),
                )
                val split = if (row.supplyType == GstSupplyType.INTER_STATE.name) {
                    stringResource(
                        R.string.gst_history_igst,
                        IndianNumberFormat.rupeesAndPaise(row.igstPaise),
                    )
                } else {
                    stringResource(
                        R.string.gst_history_cgst_sgst,
                        IndianNumberFormat.rupeesAndPaise(row.cgstPaise),
                        IndianNumberFormat.rupeesAndPaise(row.sgstPaise),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.labelLarge)
                        Text(
                            detail,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            split,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(
                        onClick = { onDelete(row.id) },
                        modifier = Modifier.semantics { contentDescription = removeDescription },
                    ) {
                        Text(stringResource(R.string.action_remove))
                    }
                }
            }
        }
    }
}
