package perozzi.gib.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import perozzi.gib.AppContainer
import perozzi.gib.domain.model.DayEntry
import perozzi.gib.domain.model.ExerciseLevel
import perozzi.gib.domain.model.MealBucket
import perozzi.gib.domain.model.UserGoal
import perozzi.gib.domain.model.UserSettings
import perozzi.gib.domain.repository.DayEntryRepository
import perozzi.gib.domain.repository.SettingsRepository
import perozzi.gib.domain.usecase.BehaviorCalculator

data class HistoryNetUiModel(
    val text: String,
    val isFavorable: Boolean,
)

data class HistoryRowUiModel(
    val date: LocalDate,
    val caloriesIn: Int,
    val caloriesOut: Int,
    val net: HistoryNetUiModel,
    val alcoholDrinks: Int,
    val exerciseLabel: String,
    val weight: String,
    val mealDetails: List<Pair<String, String>>,
)

data class HistoryUiState(
    val rows: List<HistoryRowUiModel> = emptyList(),
    val expandedDates: Set<LocalDate> = emptySet(),
)

class HistoryViewModel(
    private val dayEntryRepository: DayEntryRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val expandedDates = MutableStateFlow<Set<LocalDate>>(emptySet())

    val uiState: StateFlow<HistoryUiState> = combine(
        dayEntryRepository.observeRecentDays(90),
        settingsRepository.settings,
        expandedDates,
    ) { entries, settings, expanded ->
        val rows = toRowModels(entries, settings)
        HistoryUiState(rows = rows, expandedDates = expanded)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun toggleExpanded(date: LocalDate) {
        expandedDates.value = expandedDates.value.toMutableSet().also { dates ->
            if (date in dates) dates.remove(date) else dates.add(date)
        }
    }

    fun expandAll() {
        expandedDates.value = uiState.value.rows.map { it.date }.toSet()
    }

    fun collapseAll() {
        expandedDates.value = emptySet()
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HistoryViewModel(container.dayEntryRepository, container.settingsRepository) as T
            }
        }
    }
}

private fun toRowModels(entries: List<DayEntry>, settings: UserSettings): List<HistoryRowUiModel> {
    var lastKnownWeight: Double? = null
    return entries
        .sortedBy { it.date }
        .map { entry ->
            val effectiveWeight = entry.weight ?: lastKnownWeight
            if (entry.weight != null) {
                lastKnownWeight = entry.weight
            }
            toRowModel(entry, effectiveWeight, settings)
        }
        .sortedByDescending { it.date }
}

private fun toRowModel(
    entry: DayEntry,
    effectiveWeight: Double?,
    settings: UserSettings,
): HistoryRowUiModel {
    val caloriesIn = BehaviorCalculator.dailyCalories(entry)
    val caloriesOut = calculateCaloriesOut(
        effectiveWeight = effectiveWeight,
        exerciseLevel = entry.exerciseLevel,
        settings = settings,
    )
    val netValue = caloriesIn - caloriesOut
    return HistoryRowUiModel(
        date = entry.date,
        caloriesIn = caloriesIn,
        caloriesOut = caloriesOut,
        net = HistoryNetUiModel(
            text = netValue.toSignedString(),
            isFavorable = isNetFavorable(netValue, settings.goal),
        ),
        alcoholDrinks = entry.alcoholDrinks,
        exerciseLabel = entry.exerciseLevel.name,
        weight = entry.weight?.let { "%.1f".format(it) } ?: "--",
        mealDetails = MealBucket.entries.map { bucket ->
            val parts = entry.meals.partsFor(bucket)
            val detail = if (parts.isEmpty()) {
                "0"
            } else {
                "${BehaviorCalculator.mealTotal(parts)}  [${parts.joinToString(" + ")}]"
            }
            bucket.label to detail
        },
    )
}

private fun calculateCaloriesOut(
    effectiveWeight: Double?,
    exerciseLevel: ExerciseLevel,
    settings: UserSettings,
): Int {
    val baseBurn = BehaviorCalculator.baselineCaloriesOut(
        weightLbs = effectiveWeight,
        settings = settings,
    ) ?: 0
    return (baseBurn * exerciseLevel.coefficient).toInt()
}

private fun isNetFavorable(netValue: Int, goal: UserGoal): Boolean = when (goal) {
    UserGoal.Cut -> netValue <= 0
    UserGoal.Gain -> netValue >= 0
    UserGoal.Maintain -> kotlin.math.abs(netValue) <= 150
}

private fun Int.toSignedString(): String = if (this > 0) "+$this" else toString()
