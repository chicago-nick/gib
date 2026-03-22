package perozzi.gib.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import perozzi.gib.ui.components.SectionCard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onToggleExpanded: (LocalDate) -> Unit,
    onExpandAll: () -> Unit,
    onCollapseAll: () -> Unit,
    onEditDay: (LocalDate) -> Unit,
) {
    val formatter = DateTimeFormatter.ofPattern("MMM d")
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
        stickyHeader {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Date   Cal   Alc   Ex   Wt", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        items(state.rows, key = { it.date.toEpochDay() }) { row ->
            val isExpanded = row.date in state.expandedDates
            val menuExpanded = remember { mutableStateOf(false) }
            val interactionSource = remember { MutableInteractionSource() }
            val chevronRotation = animateFloatAsState(
                targetValue = if (isExpanded) 180f else 0f,
                animationSpec = tween(durationMillis = 100, easing = LinearEasing),
                label = "historyRowChevronRotation",
            )
            SectionCard(
                title = "${row.date.format(formatter)}   ${row.totalCalories}   ${if (row.drankAlcohol) "Yes" else "No"}   ${row.exerciseLabel.take(4)}   ${row.weight}",
                titleStyle = MaterialTheme.typography.titleMedium,
                modifier = Modifier.combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onToggleExpanded(row.date) },
                    onLongClick = { menuExpanded.value = true },
                ),
                headerContent = {
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse row" else "Expand row",
                        modifier = Modifier.rotate(chevronRotation.value),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
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
                        row.mealDetails.forEach { (meal, detail) ->
                            Text("$meal: $detail", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
