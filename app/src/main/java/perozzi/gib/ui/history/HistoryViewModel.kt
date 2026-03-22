package perozzi.gib.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import perozzi.gib.AppContainer
import perozzi.gib.domain.model.DayEntry
import perozzi.gib.domain.model.MealBucket
import perozzi.gib.domain.repository.DayEntryRepository
import perozzi.gib.domain.usecase.BehaviorCalculator

data class HistoryRowUiModel(
    val date: LocalDate,
    val totalCalories: Int,
    val drankAlcohol: Boolean,
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
) : ViewModel() {
    private val expandedDates = MutableStateFlow<Set<LocalDate>>(emptySet())

    val uiState: StateFlow<HistoryUiState> = combine(
        dayEntryRepository.observeRecentDays(90).map { entries ->
            entries.sortedByDescending { it.date }.map(::toRowModel)
        },
        expandedDates,
    ) { rows, expanded ->
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
                return HistoryViewModel(container.dayEntryRepository) as T
            }
        }
    }
}

private fun toRowModel(entry: DayEntry): HistoryRowUiModel = HistoryRowUiModel(
    date = entry.date,
    totalCalories = BehaviorCalculator.dailyCalories(entry),
    drankAlcohol = entry.drankAlcohol,
    exerciseLabel = entry.exerciseLevel.label,
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
