package com.jiri.perspective.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jiri.perspective.R
import com.jiri.perspective.ui.viewmodel.CategorySpending
import com.jiri.perspective.ui.viewmodel.OverviewInsight
import com.jiri.perspective.ui.viewmodel.OverviewInsightType
import com.jiri.perspective.ui.viewmodel.OverviewUiState
import com.jiri.perspective.ui.viewmodel.OverviewViewModel
import com.jiri.perspective.ui.viewmodel.UsageChartItem

@Composable
fun OverviewScreen(
    // Screen dostane OverviewViewModel, který už má připravený celý uiState.
    viewModel: OverviewViewModel,
    modifier: Modifier = Modifier
) {
    // Odebíráme uiState z ViewModelu jako Compose state.
    // Když se data změní, screen se automaticky překreslí.
    val uiState by viewModel.uiState.collectAsState()

    // Celý screen je jednoduchý svislý seznam karet.
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = stringResource(R.string.overview),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            // Horní souhrnná karta.
            OverviewSummaryCard(uiState = uiState)
        }

        item {
            // Karta s insighty.
            OverviewInsightsCard(uiState = uiState)
        }

        item {
            // Karta s category chartem.
            CategoryPieChartCard(categorySpending = uiState.categorySpending)
        }

        item {
            // Karta s usage rankingem.
            UsageRankingCard(usageRanking = uiState.usageRanking)
        }
    }
}

@Composable
fun OverviewSummaryCard(
    uiState: OverviewUiState
) {
    // Souhrnná karta s počtem aktivních subscriptions,
    // měsíční cenou a celkovým usage count.
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = stringResource(R.string.overview_basic_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverviewCompactStat(
                    label = stringResource(R.string.active),
                    value = uiState.activeSubscriptions.toString(),
                    modifier = Modifier.weight(1f)
                )

                OverviewCompactStat(
                    label = stringResource(R.string.monthly),
                    value = stringResource(
                        R.string.value_monthly_czk_precise,
                        uiState.totalActivePrice
                    ),
                    modifier = Modifier.weight(1f)
                )

                OverviewCompactStat(
                    label = stringResource(R.string.uses),
                    value = uiState.totalUsageCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun OverviewCompactStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    // Malý blok jedné statistiky: label + value.
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun OverviewInsightsCard(
    uiState: OverviewUiState
) {
    // Karta, která zobrazuje jednotlivé insighty.
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.overview_insights_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            uiState.worstValueInsight?.let {
                InsightItem(
                    insight = it,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            uiState.mostUsedInsight?.let {
                InsightItem(
                    insight = it,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            uiState.unusedSubscriptionsInsight?.let {
                InsightItem(
                    insight = it,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
fun InsightItem(
    insight: OverviewInsight,
    modifier: Modifier = Modifier
) {
    // Podle typu insightu určíme jeho title.
    val title = when (insight.type) {
        OverviewInsightType.WORST_VALUE -> stringResource(R.string.insight_worst_value_title)
        OverviewInsightType.MOST_USED -> stringResource(R.string.insight_most_used_title)
        OverviewInsightType.UNUSED_SUBSCRIPTIONS -> stringResource(R.string.insight_unused_title)
    }

    // Podle typu insightu složíme jeho subtitle.
    val subtitle = when (insight.type) {
        OverviewInsightType.WORST_VALUE -> {
            val price = insight.price ?: 0.0
            val currency = insight.currency ?: ""
            stringResource(R.string.insight_worst_value_subtitle, price, currency)
        }

        OverviewInsightType.MOST_USED -> {
            stringResource(R.string.insight_most_used_subtitle, insight.count ?: 0)
        }

        OverviewInsightType.UNUSED_SUBSCRIPTIONS -> {
            insight.exampleName?.let {
                stringResource(R.string.insight_unused_subtitle_example, it)
            } ?: stringResource(R.string.insight_unused_subtitle_all_used)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // primaryText je hlavní hodnota insightu,
            // například název subscription nebo počet nepoužívaných items.
            Text(
                text = insight.primaryText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun CategoryPieChartCard(
    categorySpending: List<CategorySpending>
) {
    // Jemná paleta barev pro jednotlivé výseče grafu.
    val chartColors = listOf(
        Color(0xFF8E97FD),
        Color(0xFF7CC6FE),
        Color(0xFFA5D6A7),
        Color(0xFFFFCC80),
        Color(0xFFF48FB1),
        Color(0xFFB39DDB)
    )
    val otherCategoryLabel = stringResource(R.string.category_other)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.overview_category_chart_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.overview_category_chart_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Pokud nejsou data, místo grafu zobrazíme jednoduchou informaci.
            if (categorySpending.isEmpty()) {
                Text(
                    text = stringResource(R.string.overview_no_chart_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else {
                val total = categorySpending.sumOf { it.totalPrice }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Canvas kreslí vlastní donut / pie chart.
                    Canvas(
                        modifier = Modifier.size(230.dp)
                    ) {
                        var startAngle = -90f

                        categorySpending.forEachIndexed { index, item ->
                            val sweepAngle = ((item.totalPrice / total) * 360f).toFloat()

                            drawArc(
                                color = chartColors[index % chartColors.size],
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                // Stroke kreslí jen obrys oblouku, ne vyplněný kruh.
                                style = Stroke(width = 34f, cap = StrokeCap.Butt),
                                size = Size(size.width, size.height)
                            )

                            startAngle += sweepAngle
                        }
                    }

                    // Uprostřed donut chartu je vnitřní kruh s celkovou sumou.
                    Surface(
                        modifier = Modifier.size(124.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.overview_total),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.value_whole_number, total),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                text = stringResource(R.string.overview_czk_per_month),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Legenda pod grafem: category, procenta a cena.
                Column(modifier = Modifier.padding(top = 18.dp)) {
                    categorySpending.forEachIndexed { index, item ->
                        val percentage =
                            if (total > 0.0) (item.totalPrice / total) * 100.0 else 0.0

                        val categoryLabel = item.category.ifBlank { otherCategoryLabel }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Barevná tečka odpovídající výseči grafu.
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            color = chartColors[index % chartColors.size],
                                            shape = CircleShape
                                        )
                                )

                                Text(
                                    text = categoryLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 10.dp)
                                )
                            }

                            Text(
                                text = stringResource(R.string.value_percent, percentage),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Text(
                                text = stringResource(R.string.value_monthly_czk, item.totalPrice),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsageRankingCard(
    usageRanking: List<UsageChartItem>
) {
    val fillColor = Color(0xFF8E97FD)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.overview_ranking_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.overview_ranking_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (usageRanking.isEmpty()) {
                Text(
                    text = stringResource(R.string.overview_no_chart_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else {
                // Nejvyšší usage count použijeme jako 100 % šířku progress baru
                val maxUsage = usageRanking.maxOf { it.usageCount }.coerceAtLeast(1)

                Column(modifier = Modifier.padding(top = 14.dp)) {
                    usageRanking.forEach { item ->
                        val progress = item.usageCount.toFloat() / maxUsage.toFloat()

                        Column(
                            modifier = Modifier.padding(bottom = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )

                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = stringResource(R.string.value_usage_count, item.usageCount),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 5.dp
                                        )
                                    )
                                }
                            }

                            // Jednoduchý horizontální progress bar.
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(trackColor)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress)
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(fillColor)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}