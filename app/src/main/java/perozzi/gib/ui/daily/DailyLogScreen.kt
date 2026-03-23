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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.text.style.TextAlign
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

private enum class DailySection {
    Calories,
    Activity,
    Weight,
}

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
    onExerciseChanged: (ExerciseLevel) -> Unit,
    onWeightChanged: (String) -> Unit,
) {
    val inputs = remember { mutableStateMapOf<MealBucket, String>() }
    val expandedBuckets = remember { mutableStateMapOf<MealBucket, Boolean>() }
    val expandedSections = remember {
        mutableStateMapOf(
            DailySection.Calories to false,
            DailySection.Activity to false,
            DailySection.Weight to false,
        )
    }
    var showRecommendedWhyDialog by remember { mutableStateOf(false) }
    var showActivityWhyDialog by remember { mutableStateOf(false) }
    var showManualTrackingDialog by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

    LaunchedEffect(state.selectedDate) {
        expandedBuckets.clear()
        firstUnloggedMealBucket(state.entry.meals)?.let { bucket ->
            expandedBuckets[bucket] = true
        }
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
                    Text("Days until then: ${daysRemaining?.toString() ?: "--"}")
                    Text(
                        text = "Calorie loss needed per day: ${
                            caloriesRemaining?.toInt()?.toString() ?: "--"
                        } ÷ ${daysRemaining?.toString() ?: "--"} = $requiredAdjustment",
                        fontWeight = FontWeight.Bold,
                    )
                    Text("Calories you're expected to burn today (based on your MBR and activity level): $exerciseAdjustedOut")
                    Text(
                        "Recommended calories in = $exerciseAdjustedOut - $requiredAdjustment = ${state.recommendedCalories}",
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
        )
    }

    if (showActivityWhyDialog) {
        val currentWeight = state.latestLoggedWeightLbs
        val coefficient = state.entry.exerciseLevel.coefficient
        val baselineOut = BehaviorCalculator.baselineCaloriesOut(currentWeight, state.settings) ?: 0
        val activityAdjustedOut = (baselineOut * coefficient).toInt()
        AlertDialog(
            onDismissRequest = { showActivityWhyDialog = false },
            confirmButton = {
                TextButton(onClick = { showActivityWhyDialog = false }) {
                    Text("Close")
                }
            },
            title = { Text("Why does this matter?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("The more active your day is, the more calories you burn (and the more calories you can eat).")
                    Text("Note how your recommended caloric intake changes when you change your answer here.")
                    Text("Today's activity level: ${state.entry.exerciseLevel.name}")
                    Text("Activity coefficient: $coefficient")
                    Text("Baseline calorie burn before activity (MBR): $baselineOut")
                    Text("Burn after activity multiplier: $activityAdjustedOut")
                    Text("That adjusted burn feeds into today's recommended calories: ${state.recommendedCalories}")
                }
            },
        )
    }

    if (showManualTrackingDialog) {
        AlertDialog(
            onDismissRequest = { showManualTrackingDialog = false },
            confirmButton = {
                TextButton(onClick = { showManualTrackingDialog = false }) {
                    Text("Close")
                }
            },
            title = { Text("Why track manually?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tracking calories manually is the crux of this entire journey. It is boring and forces you to be aware of the calories you're eating.")
                    Text("Tracking calories helps teach which foods are calorically light and which are calorically dense. It can also encourage routines in this regard.")
                    Text("It is no coincidence that the more filling-per-calorie foods tend to be conventionally healthier eating options.")
                    Text("If you are eating food that is hard to track, such as a plate at a restaurant, I recommend taking a picture and asking your preferred AI agent to estimate it for you.")
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
            val recommendationDelta = state.totalCalories - state.recommendedCalories
            val deltaSuffix = if (abs(recommendationDelta) <= 100 && recommendationDelta != 0) {
                " (pretty close)"
            } else {
                ""
            }
            val caloriesSoFarSupporting = when {
                recommendationDelta < 0 -> "${abs(recommendationDelta)} under recommended today$deltaSuffix"
                recommendationDelta > 0 -> "${recommendationDelta} over recommended today$deltaSuffix"
                else -> "Right on recommended today"
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard(
                    label = "Calories so far today",
                    value = state.totalCalories.toString(),
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    label = "Recommended for you",
                    value = state.recommendedCalories.toString(),
                    modifier = Modifier.weight(1f),
                    supportingContent = {
                        Text(
                            text = "Why ${state.recommendedCalories}?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showRecommendedWhyDialog = true },
                        )
                    },
                )
            }
        }
        item {
            val recommendationDelta = state.totalCalories - state.recommendedCalories
            val day = if (state.selectedDate == LocalDate.now()) {
                "today"
            } else {
                "on ${state.selectedDate.format(DateTimeFormatter.ofPattern("M/d"))}"
            }
            val prefix = if (day == "today") "So far y" else "Y"
            val caloriesSoFarSupporting = when {
                recommendationDelta < -200 -> "${prefix}ou're ${abs(recommendationDelta)} calories under recommended $day."
                recommendationDelta > 200 -> "${prefix}ou're ${recommendationDelta} calories over recommended $day."
                else -> "${prefix}ou're right around recommended $day."
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = caloriesSoFarSupporting,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
        item {
            val isExpanded = expandedSections[DailySection.Calories] == true
            val cardInteractionSource = remember { MutableInteractionSource() }
            SectionCard(
                title = "Log Calories",
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.clickable(
                    interactionSource = cardInteractionSource,
                    indication = null,
                ) {
                    expandedSections[DailySection.Calories] = !isExpanded
                },
                headerContent = {
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse Log Calories" else "Expand Log Calories",
                        modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            ) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = tween(100, easing = LinearEasing)) +
                        expandVertically(animationSpec = tween(100, easing = LinearEasing)),
                    exit = fadeOut(animationSpec = tween(100, easing = LinearEasing)) +
                        shrinkVertically(animationSpec = tween(100, easing = LinearEasing)),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MealBucket.entries.forEach { bucket ->
                            val parts = state.entry.meals.partsFor(bucket)
                            val currentInput = inputs[bucket].orEmpty()
                            val isMealExpanded = expandedBuckets[bucket] == true
                            MealSection(
                                bucket = bucket,
                                parts = parts,
                                currentInput = currentInput,
                                isExpanded = isMealExpanded,
                                yesterdayAvailable = state.yesterdayAvailable,
                                onToggleExpanded = {
                                    if (isMealExpanded) {
                                        expandedBuckets.remove(bucket)
                                    } else {
                                        expandedBuckets[bucket] = true
                                    }
                                },
                                onInputChanged = { inputs[bucket] = it.filter(Char::isDigit) },
                                onAddPart = {
                                    currentInput.toIntOrNull()?.let { onAddMealPart(bucket, it) }
                                    inputs[bucket] = ""
                                    expandedBuckets[bucket] = true
                                    nextMealBucketAfter(bucket, state.entry.meals)?.let { nextBucket ->
                                        expandedBuckets[nextBucket] = true
                                    }
                                },
                                onRemovePart = { index -> onRemoveMealPart(bucket, index) },
                                onCopyYesterday = {
                                    onCopyBucketFromYesterday(bucket)
                                    expandedBuckets[bucket] = true
                                    nextMealBucketAfter(bucket, state.entry.meals)?.let { nextBucket ->
                                        expandedBuckets[nextBucket] = true
                                    }
                                },
                                onQuickAdd = {
                                    onQuickAdd(bucket, 100)
                                    expandedBuckets[bucket] = true
                                    nextMealBucketAfter(bucket, state.entry.meals)?.let { nextBucket ->
                                        expandedBuckets[nextBucket] = true
                                    }
                                },
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = "Why track manually?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { showManualTrackingDialog = true },
                            )
                        }
                    }
                }
            }
        }
        item {
            val isExpanded = expandedSections[DailySection.Activity] == true
            val cardInteractionSource = remember { MutableInteractionSource() }
            SectionCard(
                title = "Log Activity Level today",
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.clickable(
                    interactionSource = cardInteractionSource,
                    indication = null,
                ) {
                    expandedSections[DailySection.Activity] = !isExpanded
                },
                headerContent = {
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse Log Activity Level today" else "Expand Log Activity Level today",
                        modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            ) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = tween(100, easing = LinearEasing)) +
                        expandVertically(animationSpec = tween(100, easing = LinearEasing)),
                    exit = fadeOut(animationSpec = tween(100, easing = LinearEasing)) +
                        shrinkVertically(animationSpec = tween(100, easing = LinearEasing)),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "What is this?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showActivityWhyDialog = true },
                        )
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
                }
            }
        }
        item {
            val isExpanded = expandedSections[DailySection.Weight] == true
            val cardInteractionSource = remember { MutableInteractionSource() }
            SectionCard(
                title = "Log Weight",
                subtitle = "You don't really need to log weight every day. Try to at least log weekly.",
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.clickable(
                    interactionSource = cardInteractionSource,
                    indication = null,
                ) {
                    expandedSections[DailySection.Weight] = !isExpanded
                },
                headerContent = {
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse Log Weight" else "Expand Log Weight",
                        modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            ) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = tween(100, easing = LinearEasing)) +
                        expandVertically(animationSpec = tween(100, easing = LinearEasing)),
                    exit = fadeOut(animationSpec = tween(100, easing = LinearEasing)) +
                        shrinkVertically(animationSpec = tween(100, easing = LinearEasing)),
                ) {
                    OutlinedTextField(
                        value = state.entry.weight?.toString().orEmpty(),
                        onValueChange = onWeightChanged,
                        label = { Text("Weight (lb)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MealSection(
    bucket: MealBucket,
    parts: List<Int>,
    currentInput: String,
    isExpanded: Boolean,
    yesterdayAvailable: Boolean,
    onToggleExpanded: () -> Unit,
    onInputChanged: (String) -> Unit,
    onAddPart: () -> Unit,
    onRemovePart: (Int) -> Unit,
    onCopyYesterday: () -> Unit,
    onQuickAdd: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpanded),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Log ${bucket.label}", style = MaterialTheme.typography.titleMedium)
                Text(
                    "= ${BehaviorCalculator.mealTotal(parts)} calories",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = if (isExpanded) "Collapse ${bucket.label}" else "Expand ${bucket.label}",
                modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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
                        PartChip(value = value) { onRemovePart(partIndex) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = onInputChanged,
                        label = { Text("Add part") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Button(onClick = onAddPart) { Text("Add") }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = false,
                        onClick = onCopyYesterday,
                        label = { Text("Copy yesterday's meal") },
                        enabled = yesterdayAvailable,
                    )
                    FilterChip(
                        selected = false,
                        onClick = onQuickAdd,
                        label = { Text("+100") },
                    )
                }
            }
        }
    }
}

private fun firstUnloggedMealBucket(meals: perozzi.gib.domain.model.MealParts): MealBucket? =
    MealBucket.entries.firstOrNull { bucket -> meals.partsFor(bucket).isEmpty() }

private fun nextMealBucketAfter(
    current: MealBucket,
    meals: perozzi.gib.domain.model.MealParts,
): MealBucket? = MealBucket.entries
    .dropWhile { it != current }
    .drop(1)
    .firstOrNull { bucket -> meals.partsFor(bucket).isEmpty() }
