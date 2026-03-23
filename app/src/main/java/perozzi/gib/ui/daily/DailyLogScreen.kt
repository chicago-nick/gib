package perozzi.gib.ui.daily

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs
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
    onAlcoholChanged: (String) -> Unit,
    onExerciseChanged: (ExerciseLevel) -> Unit,
    onWeightChanged: (String) -> Unit,
) {
    val inputs = remember { mutableStateMapOf<MealBucket, String>() }
    var expandedBucket by remember { mutableStateOf<MealBucket?>(null) }
    var showRecommendedWhyDialog by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
    val mealSnapshot = MealBucket.entries.joinToString("|") { bucket ->
        state.entry.meals.partsFor(bucket).joinToString(",")
    }

    LaunchedEffect(state.selectedDate, mealSnapshot) {
        expandedBucket = firstUnloggedMealBucket(state.entry.meals)
    }

    if (showRecommendedWhyDialog) {
        val currentWeight = state.latestLoggedWeightLbs
        val coefficient = state.entry.exerciseLevel.coefficient
        val baselineOut = BehaviorCalculator.baselineCaloriesOut(currentWeight, state.settings) ?: 0
        val exerciseAdjustedOut = (baselineOut * coefficient).toInt()
        val requiredAdjustment = BehaviorCalculator.requiredDailyCalorieAdjustment(
            currentWeightLbs = currentWeight,
            settings = state.settings,
            today = state.selectedDate,
        ) ?: 0
        val targetWeight = state.settings.targetWeightLbs
        val poundsRemaining = if (currentWeight != null && targetWeight != null) currentWeight - targetWeight else null
        val caloriesRemaining = poundsRemaining?.let { abs(it) * 3500 }
        val targetDate = state.settings.targetDateEpochDay?.let(LocalDate::ofEpochDay)
        val daysRemaining = targetDate?.let {
            ChronoUnit.DAYS.between(state.selectedDate, it).toInt().coerceAtLeast(1)
        }
        AlertDialog(
            onDismissRequest = { showRecommendedWhyDialog = false },
            confirmButton = {
                TextButton(onClick = { showRecommendedWhyDialog = false }) {
                    Text("Close")
                }
            },
            title = { Text("Why ${state.recommendedCalories}?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current weight: ${currentWeight?.toInt()?.toString() ?: "--"} lb")
                    Text("Target weight: ${targetWeight?.toInt()?.toString() ?: "--"} lb")
                    Text("Pounds remaining: ${poundsRemaining?.let { abs(it).toInt().toString() } ?: "--"} lb")
                    Text("Converted to calories: ${caloriesRemaining?.toInt()?.toString() ?: "--"}")
                    Text("Target date: ${targetDate?.toString() ?: "--"}")
                    Text("Days remaining: ${daysRemaining?.toString() ?: "--"}")
                    Text(
                        text = "Calorie loss needed per day: $requiredAdjustment",
                        fontWeight = FontWeight.Bold,
                    )
                    Text("What you'll burn today: $exerciseAdjustedOut")
                    Text("Calories in = calories out - daily loss goal = ${state.recommendedCalories}")
                }
            },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Daily Log", style = MaterialTheme.typography.headlineMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = onPreviousDay, modifier = Modifier.sizeIn(minWidth = 0.dp)) { Text("Prev") }
                        TextButton(onClick = onToday, modifier = Modifier.sizeIn(minWidth = 0.dp)) { Text("Today") }
                        TextButton(onClick = onNextDay, modifier = Modifier.sizeIn(minWidth = 0.dp)) { Text("Next") }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        state.selectedDate.format(dateFormatter),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard(
                    label = "Calories so far today",
                    value = state.totalCalories.toString(),
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    label = "Recommended",
                    value = state.recommendedCalories.toString(),
                    modifier = Modifier.weight(1f),
                    supportingContent = {
                        Text(
                            text = "why ${state.recommendedCalories}?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showRecommendedWhyDialog = true },
                        )
                    },
                )
            }
        }
        items(MealBucket.entries.size) { index ->
            val bucket = MealBucket.entries[index]
            val parts = state.entry.meals.partsFor(bucket)
            val currentInput = inputs[bucket].orEmpty()
            val isExpanded = expandedBucket == bucket
            SectionCard(
                title = "Log ${bucket.label}",
                subtitle = "Total ${BehaviorCalculator.mealTotal(parts)} calories",
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.clickable {
                    expandedBucket = if (isExpanded) null else bucket
                },
                headerContent = {
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse ${bucket.label}" else "Expand ${bucket.label}",
                        modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp)
                ) {
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn(animationSpec = tween(100, easing = LinearEasing)) +
                            expandVertically(animationSpec = tween(100, easing = LinearEasing)),
                        exit = fadeOut(animationSpec = tween(100, easing = LinearEasing)) +
                            shrinkVertically(animationSpec = tween(100, easing = LinearEasing)),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                )
                                Button(
                                    onClick = {
                                        currentInput.toIntOrNull()?.let { onAddMealPart(bucket, it) }
                                        inputs[bucket] = ""
                                    }
                                ) { Text("Add") }
                            }
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = false,
                                    onClick = { onCopyBucketFromYesterday(bucket) },
                                    label = { Text("Copy yesterday's meal") },
                                    enabled = state.yesterdayAvailable,
                                )
                                listOf(100).forEach { quickValue ->
                                    FilterChip(
                                        selected = false,
                                        onClick = { onQuickAdd(bucket, quickValue) },
                                        label = { Text("+$quickValue") },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionCard(
                title = "Other signals",
                subtitle = "Simple daily context. No extra ceremony.",
                contentPadding = PaddingValues(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Alcohol", style = MaterialTheme.typography.titleMedium)
                        Text("Number of drinks for the day", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                OutlinedTextField(
                    value = state.entry.alcoholDrinks.takeIf { it > 0 }?.toString().orEmpty(),
                    onValueChange = { onAlcoholChanged(it.filter(Char::isDigit)) },
                    label = { Text("Drinks") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Exercise level", style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExerciseLevel.entries.forEach { level ->
                            FilterChip(
                                selected = state.entry.exerciseLevel == level,
                                onClick = { onExerciseChanged(level) },
                                label = { Text(level.name) },
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

private fun firstUnloggedMealBucket(meals: perozzi.gib.domain.model.MealParts): MealBucket? =
    MealBucket.entries.firstOrNull { bucket -> meals.partsFor(bucket).isEmpty() }
