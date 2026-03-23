package perozzi.gib.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import perozzi.gib.ui.components.SectionCard
import perozzi.gib.ui.theme.AccentStrong
import perozzi.gib.ui.theme.Warning

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onToggleExpanded: (LocalDate) -> Unit,
    onExpandAll: () -> Unit,
    onCollapseAll: () -> Unit,
    onEditDay: (LocalDate) -> Unit,
) {
    val formatter = DateTimeFormatter.ofPattern("M/d")
    val horizontalScrollState = rememberScrollState()
    var visibleColumns by remember {
        mutableStateOf(
            HistoryColumn.entries.toSet() - setOf(
                HistoryColumn.CaloriesIn,
                HistoryColumn.CaloriesOut,
                HistoryColumn.Exercise,
            )
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("History", style = MaterialTheme.typography.headlineMedium)
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = onExpandAll,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Expand all")
                }
                TextButton(
                    onClick = onCollapseAll,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Collapse all")
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Columns", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HistoryColumn.entries.forEach { column ->
                        val isVisible = column in visibleColumns
                        val canToggleOff = visibleColumns.size > 1 || !isVisible
                        FilterChip(
                            selected = isVisible,
                            onClick = {
                                visibleColumns = if (isVisible) {
                                    if (canToggleOff) visibleColumns - column else visibleColumns
                                } else {
                                    visibleColumns + column
                                }
                            },
                            enabled = canToggleOff,
                            label = { Text(column.label) },
                            leadingIcon = if (isVisible) {
                                {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                    )
                                }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }
        stickyHeader {
            Card(modifier = Modifier.fillMaxWidth()) {
                HistorySummaryRow(
                    values = mapOf(
                        HistoryColumn.Date to HistoryCellUiModel("Date"),
                        HistoryColumn.CaloriesIn to HistoryCellUiModel("C In"),
                        HistoryColumn.CaloriesOut to HistoryCellUiModel("C Out"),
                        HistoryColumn.Net to HistoryCellUiModel("C Net"),
                        HistoryColumn.Exercise to HistoryCellUiModel("Activity"),
                        HistoryColumn.Weight to HistoryCellUiModel("Lbs"),
                    ),
                    visibleColumns = visibleColumns,
                    modifier = Modifier
                        .horizontalScroll(horizontalScrollState)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        items(state.rows, key = { it.date.toEpochDay() }) { row ->
            val isExpanded = row.date in state.expandedDates
            val menuExpanded = remember { mutableStateOf(false) }
            val interactionSource = remember { MutableInteractionSource() }
            SectionCard(
                title = "",
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                titleStyle = MaterialTheme.typography.titleMedium,
                titleContent = {
                    HistorySummaryRow(
                        values = mapOf(
                            HistoryColumn.Date to HistoryCellUiModel(row.date.format(formatter)),
                            HistoryColumn.CaloriesIn to HistoryCellUiModel(row.caloriesIn.toString()),
                            HistoryColumn.CaloriesOut to HistoryCellUiModel(row.caloriesOut.toString()),
                            HistoryColumn.Net to HistoryCellUiModel(
                                text = row.net.text,
                                color = if (row.net.isFavorable) AccentStrong else Warning,
                                fontWeight = FontWeight.Bold,
                            ),
                            HistoryColumn.Exercise to HistoryCellUiModel(row.exerciseLabel),
                            HistoryColumn.Weight to HistoryCellUiModel(row.weight.ifBlank { "-" }),
                        ),
                        visibleColumns = visibleColumns,
                        modifier = Modifier.horizontalScroll(horizontalScrollState),
                        style = MaterialTheme.typography.titleSmall,
                    )
                },
                modifier = Modifier.combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onToggleExpanded(row.date) },
                    onLongClick = { menuExpanded.value = true },
                ),
            ) {
                DropdownMenu(expanded = menuExpanded.value, onDismissRequest = { menuExpanded.value = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit day") },
                        onClick = {
                            menuExpanded.value = false
                            onEditDay(row.date)
                        },
                    )
                }
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = tween(100, easing = LinearEasing)) +
                        expandVertically(animationSpec = tween(100, easing = LinearEasing)),
                    exit = fadeOut(animationSpec = tween(100, easing = LinearEasing)) +
                        shrinkVertically(animationSpec = tween(100, easing = LinearEasing)),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Calories in: ${row.caloriesIn}", style = MaterialTheme.typography.bodyMedium)
                        Text("Calories out: ${row.caloriesOut}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Net calories: ${row.net.text}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (row.net.isFavorable) AccentStrong else Warning,
                            fontWeight = FontWeight.Medium,
                        )
                        row.mealDetails.forEach { (meal, detail) ->
                            Text("$meal: $detail", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

private enum class HistoryColumn(
    val label: String,
    val width: Dp,
    val textAlign: TextAlign,
) {
    Date("Date", 40.dp, TextAlign.End),
    CaloriesIn("C In", 44.dp, TextAlign.Start),
    CaloriesOut("C Out", 44.dp, TextAlign.Start),
    Net("C Net", 44.dp, TextAlign.Start),
    Exercise("Activity", 80.dp, TextAlign.Start),
    Weight("Lbs", 60.dp, TextAlign.Start),
}

private val HistoryColumnSpacing = 8.dp

private data class HistoryCellUiModel(
    val text: String,
    val color: Color = Color.Unspecified,
    val fontWeight: FontWeight? = null,
)

@Composable
private fun HistorySummaryRow(
    values: Map<HistoryColumn, HistoryCellUiModel>,
    visibleColumns: Set<HistoryColumn>,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
) {
    val orderedColumns = HistoryColumn.entries.filter { it in visibleColumns }
    val totalWidth = orderedColumns.fold(0.dp) { total, column -> total + column.width } +
        HistoryColumnSpacing * (orderedColumns.size - 1).coerceAtLeast(0)
    Row(
        modifier = modifier.requiredWidth(totalWidth),
        horizontalArrangement = Arrangement.spacedBy(HistoryColumnSpacing),
    ) {
        orderedColumns.forEach { column ->
            val cell = values[column] ?: HistoryCellUiModel("")
            HistoryCell(
                text = cell.text,
                width = column.width,
                style = style,
                textAlign = column.textAlign,
                color = cell.color,
                fontWeight = cell.fontWeight,
            )
        }
    }
}

@Composable
private fun HistoryCell(
    text: String,
    width: Dp,
    style: androidx.compose.ui.text.TextStyle,
    textAlign: TextAlign,
    color: Color,
    fontWeight: FontWeight?,
) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        style = style,
        color = color,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = 1,
    )
}
