@file:OptIn(ExperimentalMaterial3Api::class)

package com.pabicalc.paisacalc.ui.emi

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pabicalc.paisacalc.R
import com.pabicalc.paisacalc.engine.LoanSummary
import com.pabicalc.paisacalc.format.IndianNumberFormat

/**
 * The result area: exactly one of the four [EmiUiState]s is on screen.
 */
@Composable
fun ResultSection(
    state: EmiUiState,
    previousSummary: LoanSummary? = null,
    modifier: Modifier = Modifier,
) {
    when (state) {
        EmiUiState.Empty -> MessageCard(
            glyph = "₹",
            title = stringResource(R.string.empty_title),
            body = stringResource(R.string.empty_body),
            modifier = modifier,
        )

        // Stale-while-revalidate: while the next result is computed the last
        // one stays visible under a "Calculating…" strip.
        EmiUiState.Loading -> Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LoadingCard()
            if (previousSummary != null) {
                HeroEmiCard(previousSummary)
            }
        }

        is EmiUiState.Invalid -> InvalidCard(state.issues, modifier = modifier)

        is EmiUiState.Ready -> HeroEmiCard(state.summary, modifier = modifier)
    }
}

@Composable
private fun MessageCard(glyph: String, title: String, body: String, modifier: Modifier = Modifier) {
    SectionCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlyphBadge(glyph = glyph, container = MaterialTheme.colorScheme.primaryContainer, content = MaterialTheme.colorScheme.primary)
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LoadingCard(modifier: Modifier = Modifier) {
    SectionCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Text(
                text = stringResource(R.string.loading_title),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun InvalidCard(issues: List<FieldIssue>, modifier: Modifier = Modifier) {
    SectionCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        borderColor = MaterialTheme.colorScheme.errorContainer,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlyphBadge(
                glyph = "!",
                container = MaterialTheme.colorScheme.error,
                content = MaterialTheme.colorScheme.onError,
            )
            Text(
                text = stringResource(R.string.error_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
        issues.forEach { issue ->
            Text(
                text = "•  " + stringResource(issue.messageRes()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
private fun HeroEmiCard(summary: LoanSummary, modifier: Modifier = Modifier) {
    SectionCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        borderColor = MaterialTheme.colorScheme.primary,
    ) {
        val onPrimary = MaterialTheme.colorScheme.onPrimary
        Text(
            text = stringResource(R.string.hero_emi_label),
            style = MaterialTheme.typography.labelMedium,
            color = onPrimary,
        )
        Text(
            text = IndianNumberFormat.rupees(summary.monthlyEmi),
            style = MaterialTheme.typography.displaySmall,
            color = onPrimary,
        )
        Text(
            text = stringResource(
                R.string.hero_terms,
                DisplayFormatters.tenureLabel(summary.terms.tenureMonths),
                DisplayFormatters.rateLabel(summary.terms.rateMicroPercent),
            ),
            style = MaterialTheme.typography.labelLarge,
            color = onPrimary,
        )
        HorizontalDivider(color = onPrimary.copy(alpha = 0.25f))
        Text(
            text = stringResource(
                R.string.hero_installments,
                summary.startMonth.shortLabel(),
                summary.endMonth.shortLabel(),
            ),
            style = MaterialTheme.typography.labelSmall,
            color = onPrimary,
        )
    }
}

/** Principal-vs-interest donut plus the three headline totals. */
@Composable
fun BreakdownCard(summary: LoanSummary, modifier: Modifier = Modifier) {
    SectionCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.breakdown_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.breakdown_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            InterestDonut(summary = summary, modifier = Modifier.padding(vertical = 12.dp))
        }
        LegendRow(
            dotColor = MaterialTheme.colorScheme.primary,
            label = stringResource(R.string.legend_principal),
            value = IndianNumberFormat.rupees(summary.principal),
            share = DisplayFormatters.percent(summary.principalShare),
        )
        LegendRow(
            dotColor = MaterialTheme.colorScheme.tertiary,
            label = stringResource(R.string.legend_interest),
            value = IndianNumberFormat.rupees(summary.totalInterest),
            share = DisplayFormatters.percent(summary.interestShare),
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        SummaryRow(stringResource(R.string.summary_loan_amount), IndianNumberFormat.rupees(summary.principal))
        SummaryRow(stringResource(R.string.summary_total_interest), IndianNumberFormat.rupees(summary.totalInterest))
        if (summary.hasPrepayments) {
            SummaryRow(
                stringResource(R.string.prepayment_breakdown_total),
                IndianNumberFormat.rupees(summary.totalPrepaid),
            )
            SummaryRow(
                stringResource(R.string.prepayment_breakdown_saved),
                IndianNumberFormat.rupees(summary.interestSaved),
            )
        }
        SummaryRow(
            label = stringResource(R.string.summary_total_payable),
            value = IndianNumberFormat.rupees(summary.totalPayable),
            emphasized = true,
        )
    }
}

@Composable
private fun InterestDonut(summary: LoanSummary, modifier: Modifier = Modifier) {
    val principalColor = MaterialTheme.colorScheme.primary
    val interestColor = MaterialTheme.colorScheme.tertiary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val interestShare = summary.interestShare.toFloat()

    // Draw the arcs on first show: the chart "fills up" once per calculation.
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }
    val progress by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "donutProgress",
    )

    val description = stringResource(
        R.string.donut_content_description,
        DisplayFormatters.percent(summary.principalShare),
        DisplayFormatters.percent(summary.interestShare),
    )

    Box(
        modifier = modifier
            .size(176.dp)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 22.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            val gap = 2.5f

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke),
            )

            val principalSweep = 360f * (1f - interestShare) * progress
            if (principalSweep > gap) {
                drawArc(
                    color = principalColor,
                    startAngle = -90f,
                    sweepAngle = principalSweep - gap,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }

            val interestSweep = 360f * interestShare * progress - gap
            if (interestSweep > 0f) {
                drawArc(
                    color = interestColor,
                    startAngle = -90f + principalSweep,
                    sweepAngle = interestSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = DisplayFormatters.percent(summary.interestShare),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(R.string.donut_center_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LegendRow(dotColor: Color, label: String, value: String, share: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = dotColor, shape = CircleShape),
        )
        Spacer(Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.amount_with_share, value, share),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String, emphasized: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (emphasized) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasized) FontWeight.SemiBold else null,
        )
    }
}

@Composable
private fun GlyphBadge(glyph: String, container: Color, content: Color) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(color = container, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = glyph,
            style = MaterialTheme.typography.titleMedium,
            color = content,
        )
    }
}
