package perozzi.gib.ui.trends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import perozzi.gib.ui.components.FrequencyPills
import perozzi.gib.ui.components.MetricCard
import perozzi.gib.ui.components.SectionCard
import perozzi.gib.ui.components.SimpleLineChart

@Composable
fun TrendsScreen(state: TrendsUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Trends", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "A quick read on how intake, weight, and activity have been moving.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard(
                    label = "Calories",
                    value = state.averageCalories,
                    modifier = Modifier.weight(1f),
                    supporting = "7-day average",
                )
                MetricCard(
                    label = "Weight",
                    value = state.averageWeight,
                    modifier = Modifier.weight(1f),
                    supporting = "Recent smoothed average",
                )
            }
        }
        item {
            SectionCard(
                title = "Actual vs recommended calories",
                subtitle = "Green is actual intake. Amber is the recommendation for each day.",
            ) {
                SimpleLineChart(
                    values = state.calorieValues,
                    baselineValues = state.recommendedValues,
                    valueLabel = "Actual",
                    baselineLabel = "Recommended",
                    yAxisFormatter = { "${it.toInt()}" },
                )
            }
        }
        item {
            SectionCard(
                title = "Weight trend",
                subtitle = "Smoothed from your logged weigh-ins so day-to-day noise matters less.",
            ) {
                SimpleLineChart(
                    values = state.weightValues,
                    valueLabel = "Weight",
                    yAxisFormatter = { "${"%.1f".format(it)} lb" },
                )
            }
        }
        item {
            SectionCard(
                title = "Exercise consistency",
                subtitle = "How often each activity level showed up in the current review window.",
            ) {
                FrequencyPills(state.exerciseCounts)
            }
        }
    }
}
