package perozzi.gib.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import perozzi.gib.ui.theme.Accent
import perozzi.gib.ui.theme.AccentStrong
import perozzi.gib.ui.theme.Card
import perozzi.gib.ui.theme.SoftLine
import perozzi.gib.ui.theme.Warning

@Composable
fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    supportingContent: (@Composable () -> Unit)? = null,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Card),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            Text(value, style = MaterialTheme.typography.headlineMedium)
            if (supportingContent != null) {
                supportingContent()
            } else supporting?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    subtitle: String? = null,
    titleStyle: TextStyle? = null,
    titleContent: @Composable (() -> Unit)? = null,
    headerContent: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Card),
    ) {
        Column(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = LinearEasing,
                    )
                )
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (titleContent != null) {
                        titleContent()
                    } else {
                        Text(
                            title,
                            style = titleStyle ?: MaterialTheme.typography.titleLarge
                        )
                    }
                    subtitle?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
                if (headerContent != null) {
                    headerContent()
                } else {
                    Spacer(modifier = Modifier.size(0.dp))
                }
            }
            content()
        }
    }
}

@Composable
fun PartChip(value: Int, onRemove: (() -> Unit)? = null) {
    AssistChip(
        onClick = onRemove ?: {},
        label = {
            Text(if (onRemove == null) value.toString() else "$value  x")
        },
    )
}

@Composable
fun SimpleLineChart(
    values: List<Double>,
    modifier: Modifier = Modifier,
    baselineValues: List<Double> = emptyList(),
    valueLabel: String = "Actual",
    baselineLabel: String = "Baseline",
    yAxisFormatter: (Double) -> String = { "%.0f".format(it) },
) {
    if (values.isEmpty()) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp)
                .border(1.dp, SoftLine, RoundedCornerShape(18.dp)),
            color = Card,
            shape = RoundedCornerShape(18.dp),
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Not enough data yet", style = MaterialTheme.typography.bodyMedium)
            }
        }
        return
    }

    val allValues = values + baselineValues
    val minValue = allValues.minOrNull() ?: 0.0
    val maxValue = allValues.maxOrNull() ?: (minValue + 1)
    val range = (maxValue - minValue).takeIf { it > 0.0 } ?: 1.0
    val midValue = minValue + (range / 2.0)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SoftLine, RoundedCornerShape(18.dp)),
        color = Card,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    yAxisFormatter(maxValue),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                if (baselineValues.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ChartLegend(color = AccentStrong, label = valueLabel)
                        ChartLegend(color = Warning.copy(alpha = 0.8f), label = baselineLabel)
                    }
                } else {
                    ChartLegend(color = AccentStrong, label = valueLabel)
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(190.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    fun points(series: List<Double>): List<Offset> =
                        series.mapIndexed { index, value ->
                            val x = if (series.size == 1) size.width / 2f else index.toFloat() / (series.lastIndex).coerceAtLeast(1) * size.width
                            val normalized = ((value - minValue) / range).toFloat()
                            val y = size.height - (normalized * size.height)
                            Offset(x, y)
                        }

                    val gridColor = SoftLine.copy(alpha = 0.8f)
                    val gridLevels = listOf(0f, 0.5f, 1f)
                    gridLevels.forEach { fraction ->
                        val y = size.height * fraction
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 2f,
                        )
                    }

                    baselineValues.takeIf { it.isNotEmpty() }?.let { baseline ->
                        val path = Path()
                        val points = points(baseline)
                        points.forEachIndexed { index, point ->
                            if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                        }
                        drawPath(path, Warning.copy(alpha = 0.8f), style = Stroke(width = 4f, cap = StrokeCap.Round))
                        points.forEach { point ->
                            drawCircle(
                                color = Warning.copy(alpha = 0.9f),
                                radius = 5f,
                                center = point,
                            )
                        }
                    }

                    val actualPath = Path()
                    val actualPoints = points(values)
                    actualPoints.forEachIndexed { index, point ->
                        if (index == 0) actualPath.moveTo(point.x, point.y) else actualPath.lineTo(point.x, point.y)
                    }
                    drawPath(actualPath, AccentStrong, style = Stroke(width = 5f, cap = StrokeCap.Round))
                    actualPoints.forEach { point ->
                        drawCircle(
                            color = AccentStrong,
                            radius = 5f,
                            center = point,
                        )
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    yAxisFormatter(midValue),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                )
                Text(
                    yAxisFormatter(minValue),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Earlier",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                )
                Text(
                    "Most recent",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                )
            }
        }
    }
}

@Composable
private fun ChartLegend(color: Color, label: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(99.dp))
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )
    }
}

@Composable
fun SimpleBarChart(
    values: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
) {
    if (values.isEmpty()) {
        Text("No data yet", style = MaterialTheme.typography.bodyMedium)
        return
    }
    val maxValue = values.maxOf { it.second }.coerceAtLeast(1)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        values.forEach { (label, value) ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                    Text(value.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(SoftLine, RoundedCornerShape(99.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(value / maxValue.toFloat())
                            .height(12.dp)
                            .background(Accent, RoundedCornerShape(99.dp))
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FrequencyPills(values: Map<String, Int>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEach { (label, count) ->
            Surface(
                shape = RoundedCornerShape(99.dp),
                color = Card,
                tonalElevation = 1.dp,
                modifier = Modifier.border(1.dp, SoftLine, RoundedCornerShape(99.dp))
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                    Text(count.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
