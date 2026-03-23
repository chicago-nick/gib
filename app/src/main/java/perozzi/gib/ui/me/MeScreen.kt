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
import perozzi.gib.domain.model.UserGoal
import perozzi.gib.domain.model.UserSex
import perozzi.gib.ui.components.SectionCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MeScreen(
    state: MeUiState,
    onGoalSelected: (UserGoal) -> Unit,
    onTargetWeightLbsChanged: (String) -> Unit,
    onTargetDateChanged: (String) -> Unit,
    onSexSelected: (UserSex) -> Unit,
    onHeightCmChanged: (String) -> Unit,
    onAgeYearsChanged: (String) -> Unit,
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
                title = "Goals",
                subtitle = "Set the weight you want and when you want to reach it.",
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
                    value = state.draftTargetWeightLbs,
                    onValueChange = { onTargetWeightLbsChanged(it.filter(Char::isDigit)) },
                    label = { Text("Target weight (lb)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = state.currentWeightLbs,
                        onValueChange = {},
                        label = { Text("Current weight (lb)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = false,
                    )
                    Text(
                        state.currentWeightSupporting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                OutlinedTextField(
                    value = state.draftTargetDate,
                    onValueChange = onTargetDateChanged,
                    label = { Text("Target date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        item {
            SectionCard(
                title = "BMR profile",
                subtitle = "Used for calories-out estimation when weight, height, and age are available.",
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserSex.entries.forEach { sex ->
                        FilterChip(
                            selected = state.settings.sex == sex,
                            onClick = { onSexSelected(sex) },
                            label = { Text(sex.label) },
                        )
                    }
                }
                OutlinedTextField(
                    value = state.draftHeightCm,
                    onValueChange = { onHeightCmChanged(it.filter(Char::isDigit)) },
                    label = { Text("Height (cm)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.draftAgeYears,
                    onValueChange = { onAgeYearsChanged(it.filter(Char::isDigit)) },
                    label = { Text("Age") },
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
