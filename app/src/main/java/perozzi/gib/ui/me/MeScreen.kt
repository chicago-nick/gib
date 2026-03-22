package perozzi.gib.ui.me

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import perozzi.gib.domain.model.ExerciseLevel
import perozzi.gib.domain.model.UserGoal
import perozzi.gib.ui.components.SectionCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MeScreen(
    state: MeUiState,
    onGoalSelected: (UserGoal) -> Unit,
    onBaseTargetChanged: (String) -> Unit,
    onExerciseAdjustmentChanged: (ExerciseLevel, String) -> Unit,
    onSave: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("Me", style = MaterialTheme.typography.headlineMedium)
        }
        item {
            SectionCard(
                title = "Goal settings",
                subtitle = "Keep recommendation logic understandable and adjustable.",
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserGoal.entries.forEach { goal ->
                        FilterChip(
                            selected = state.settings.goal == goal,
                            onClick = { onGoalSelected(goal) },
                            label = { Text(goal.label) },
                        )
                    }
                }
                OutlinedTextField(
                    value = state.settings.baseCalorieTarget.toString(),
                    onValueChange = onBaseTargetChanged,
                    label = { Text("Base calorie target") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        item {
            SectionCard(
                title = "Exercise adjustments",
                subtitle = "Tune how much extra intake each effort level earns.",
            ) {
                OutlinedTextField(
                    value = state.settings.lightExerciseAdjustment.toString(),
                    onValueChange = { onExerciseAdjustmentChanged(ExerciseLevel.Light, it) },
                    label = { Text("Light adjustment") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.settings.moderateExerciseAdjustment.toString(),
                    onValueChange = { onExerciseAdjustmentChanged(ExerciseLevel.Moderate, it) },
                    label = { Text("Moderate adjustment") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.settings.hardExerciseAdjustment.toString(),
                    onValueChange = { onExerciseAdjustmentChanged(ExerciseLevel.Hard, it) },
                    label = { Text("Hard adjustment") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Button(onClick = onSave) { Text("Save settings") }
            }
        }
        item {
            SectionCard(
                title = "Future sync",
                subtitle = "Architecture note for the next phase.",
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Repository interfaces already sit above Android persistence.")
                    Text("Add auth and sync later by introducing a remote data source and a sync coordinator, not by rewriting screens.")
                }
            }
        }
    }
}
