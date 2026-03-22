package perozzi.gib.ui.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter
import perozzi.gib.domain.model.ExerciseLevel
import perozzi.gib.domain.model.MealBucket
import perozzi.gib.domain.usecase.BehaviorCalculator
import perozzi.gib.ui.components.MetricCard
import perozzi.gib.ui.components.PartChip
import perozzi.gib.ui.components.SectionCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyLogScreen(
    state: DailyLogUiState,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onAddMealPart: (MealBucket, Int) -> Unit,
    onQuickAdd: (MealBucket, Int) -> Unit,
    onRemoveMealPart: (MealBucket, Int) -> Unit,
    onCopyYesterday: () -> Unit,
    onCopyBucketFromYesterday: (MealBucket) -> Unit,
    onAlcoholChanged: (Boolean) -> Unit,
    onExerciseChanged: (ExerciseLevel) -> Unit,
    onWeightChanged: (String) -> Unit,
) {
    val inputs = remember { mutableStateMapOf<MealBucket, String>() }
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("GIB", style = MaterialTheme.typography.headlineMedium)
                Text("Results come from ordinary repeated actions.", style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onPreviousDay) { Text("Prev") }
                    TextButton(onClick = onToday) { Text("Today") }
                    TextButton(onClick = onNextDay) { Text("Next") }
                    Text(
                        state.selectedDate.format(dateFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard(
                    label = "Daily calories",
                    value = state.totalCalories.toString(),
                    supporting = "7-day avg ${state.sevenDayAverage?.let { "%.0f".format(it) } ?: "--"}",
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    label = "Recommended",
                    value = state.recommendedCalories.toString(),
                    supporting = if (state.calorieDelta == 0) "On plan" else "${if (state.calorieDelta > 0) "+" else ""}${state.calorieDelta}",
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            SectionCard(
                title = "Daily shortcuts",
                subtitle = "Keep repeated days fast. Copy, then adjust.",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onCopyYesterday, enabled = state.yesterdayAvailable) {
                        Text("Copy yesterday")
                    }
                    Text(
                        if (state.yesterdayAvailable) "Copies all four meal buckets into this day."
                        else "No yesterday entry available yet.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        items(MealBucket.entries.size) { index ->
            val bucket = MealBucket.entries[index]
            val parts = state.entry.meals.partsFor(bucket)
            val currentInput = inputs[bucket].orEmpty()
            SectionCard(
                title = bucket.label,
                subtitle = "Total ${BehaviorCalculator.mealTotal(parts)} calories",
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    parts.forEachIndexed { partIndex, value ->
                        PartChip(value = value) { onRemoveMealPart(bucket, partIndex) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = { inputs[bucket] = it.filter(Char::isDigit) },
                        label = { Text("Add part") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    Button(
                        onClick = {
                            currentInput.toIntOrNull()?.let { onAddMealPart(bucket, it) }
                            inputs[bucket] = ""
                        }
                    ) { Text("Add") }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(80, 120, 200, 300).forEach { quickValue ->
                        FilterChip(
                            selected = false,
                            onClick = { onQuickAdd(bucket, quickValue) },
                            label = { Text("+$quickValue") },
                        )
                    }
                    FilterChip(
                        selected = false,
                        onClick = { onCopyBucketFromYesterday(bucket) },
                        label = { Text("Copy yesterday meal") },
                        enabled = state.yesterdayAvailable,
                    )
                }
            }
        }
        item {
            SectionCard(
                title = "Other signals",
                subtitle = "Simple daily context. No extra ceremony.",
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Alcohol", style = MaterialTheme.typography.titleMedium)
                        Text("Binary yes/no for weekly tallies", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(checked = state.entry.drankAlcohol, onCheckedChange = onAlcoholChanged)
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Exercise level", style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExerciseLevel.entries.forEach { level ->
                            FilterChip(
                                selected = state.entry.exerciseLevel == level,
                                onClick = { onExerciseChanged(level) },
                                label = { Text(level.label) },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = state.entry.weight?.toString().orEmpty(),
                    onValueChange = onWeightChanged,
                    label = { Text("Weight (lb)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        item {
            Text(
                "The app stays manual on purpose: fast entry, clear totals, no food database friction.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
