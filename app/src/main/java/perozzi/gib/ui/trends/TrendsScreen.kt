package perozzi.gib.ui.trends

import androidx.compose.foundation.layout.Arrangement
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
import perozzi.gib.ui.components.SimpleBarChart
import perozzi.gib.ui.components.SimpleLineChart

@Composable
fun TrendsScreen(state: TrendsUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("Trends", style = MaterialTheme.typography.headlineMedium)
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
                    label = "Alcohol",
                    value = state.currentWeekAlcohol.toString(),
                    modifier = Modifier.weight(1f),
                    supporting = "Current week",
                )
            }
        }
        item {
            SectionCard(
                title = "Actual vs recommended calories",
                subtitle = "Actual intake in green, recommendation in amber.",
            ) {
                SimpleLineChart(values = state.calorieValues, baselineValues = state.recommendedValues)
            }
        }
        item {
            SectionCard(
                title = "Weight trend",
                subtitle = "Smoothed rolling average from logged weigh-ins.",
            ) {
                MetricCard(
                    label = "Weight",
                    value = state.averageWeight,
                    supporting = "Recent smoothed average",
                )
                SimpleLineChart(values = state.weightValues)
            }
        }
        item {
            SectionCard(
                title = "Alcohol by week",
                subtitle = "Binary counts keep this intentionally lightweight.",
            ) {
                SimpleBarChart(values = state.weeklyAlcohol)
            }
        }
        item {
            SectionCard(
                title = "Exercise consistency",
                subtitle = "Frequency across the current review window.",
            ) {
                FrequencyPills(state.exerciseCounts)
            }
        }
    }
}
