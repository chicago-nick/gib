package perozzi.gib.ui.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import perozzi.gib.AppContainer
import perozzi.gib.domain.model.UserSettings
import perozzi.gib.domain.repository.DayEntryRepository
import perozzi.gib.domain.repository.SettingsRepository
import perozzi.gib.domain.usecase.BehaviorCalculator

data class TrendsUiState(
    val calorieValues: List<Double> = emptyList(),
    val recommendedValues: List<Double> = emptyList(),
    val weightValues: List<Double> = emptyList(),
    val weeklyAlcohol: List<Pair<String, Int>> = emptyList(),
    val exerciseCounts: Map<String, Int> = emptyMap(),
    val averageCalories: String = "--",
    val currentWeekAlcohol: Int = 0,
    val averageWeight: String = "--",
)

class TrendsViewModel(
    dayEntryRepository: DayEntryRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiState: StateFlow<TrendsUiState> = combine(
        dayEntryRepository.observeRecentDays(30),
        settingsRepository.settings,
    ) { entries, settings ->
        toTrendsUiState(entries, settings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrendsUiState())

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TrendsViewModel(container.dayEntryRepository, container.settingsRepository) as T
            }
        }
    }
}

private fun toTrendsUiState(
    entries: List<perozzi.gib.domain.model.DayEntry>,
    settings: UserSettings,
): TrendsUiState {
    val calorieTrend = BehaviorCalculator.calorieTrend(entries).map { it.value }
    val recommendedTrend = BehaviorCalculator.recommendedTrend(entries, settings).map { it.value }
    val weightTrend = BehaviorCalculator.weightTrend(entries).map { it.value }
    return TrendsUiState(
        calorieValues = calorieTrend,
        recommendedValues = recommendedTrend,
        weightValues = weightTrend,
        weeklyAlcohol = BehaviorCalculator.weeklyAlcoholTallies(entries).takeLast(6),
        exerciseCounts = BehaviorCalculator.exerciseFrequency(entries).mapKeys { it.key.name },
        averageCalories = BehaviorCalculator.rollingCalorieAverage(entries)?.let { "%.0f".format(it) } ?: "--",
        currentWeekAlcohol = BehaviorCalculator.currentWeekAlcoholCount(entries, LocalDate.now()),
        averageWeight = weightTrend.takeLast(5).takeIf { it.isNotEmpty() }?.average()?.let { "%.1f".format(it) } ?: "--",
    )
}
