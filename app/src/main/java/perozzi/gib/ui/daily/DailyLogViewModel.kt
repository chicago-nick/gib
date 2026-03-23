package perozzi.gib.ui.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import perozzi.gib.AppContainer
import perozzi.gib.domain.model.DayEntry
import perozzi.gib.domain.model.ExerciseLevel
import perozzi.gib.domain.model.MealBucket
import perozzi.gib.domain.model.MealParts
import perozzi.gib.domain.model.UserSettings
import perozzi.gib.domain.repository.DayEntryRepository
import perozzi.gib.domain.repository.SettingsRepository
import perozzi.gib.domain.usecase.BehaviorCalculator

data class DailyLogUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val entry: DayEntry = DayEntry(LocalDate.now()),
    val settings: UserSettings = UserSettings(),
    val latestLoggedWeightLbs: Double? = null,
    val totalCalories: Int = 0,
    val recommendedCalories: Int = 0,
    val calorieDelta: Int = 0,
    val sevenDayAverage: Double? = null,
    val yesterdayAvailable: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class DailyLogViewModel(
    private val dayEntryRepository: DayEntryRepository,
    private val settingsRepository: SettingsRepository,
    initialEpochDay: Long?,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(initialEpochDay?.let(LocalDate::ofEpochDay) ?: LocalDate.now())

    val uiState: StateFlow<DailyLogUiState> = combine(
        selectedDate,
        selectedDate.flatMapLatest { dayEntryRepository.observeDay(it) },
        dayEntryRepository.observeRecentDays(90),
        settingsRepository.settings,
    ) { date, entry, recentEntries, settings ->
        val resolvedEntry = entry ?: DayEntry(date = date)
        val latestLoggedWeight = recentEntries.sortedByDescending { it.date }.firstOrNull { it.weight != null }?.weight
        val summary = BehaviorCalculator.daySummary(resolvedEntry, settings, latestLoggedWeight)
        DailyLogUiState(
            selectedDate = date,
            entry = resolvedEntry,
            settings = settings,
            latestLoggedWeightLbs = latestLoggedWeight,
            totalCalories = summary.totalCalories,
            recommendedCalories = summary.recommendedCalories,
            calorieDelta = summary.calorieDelta,
            sevenDayAverage = BehaviorCalculator.rollingCalorieAverage(recentEntries),
            yesterdayAvailable = recentEntries.any { it.date == date.minusDays(1) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DailyLogUiState())

    fun goToPreviousDay() {
        selectedDate.value = selectedDate.value.minusDays(1)
    }

    fun goToNextDay() {
        selectedDate.value = selectedDate.value.plusDays(1)
    }

    fun goToToday() {
        selectedDate.value = LocalDate.now()
    }

    fun addMealPart(bucket: MealBucket, calories: Int) {
        if (calories <= 0) return
        updateEntry { entry ->
            entry.copy(meals = entry.meals.updated(bucket, entry.meals.partsFor(bucket) + calories))
        }
    }

    fun removeMealPart(bucket: MealBucket, index: Int) {
        updateEntry { entry ->
            val updated = entry.meals.partsFor(bucket).toMutableList().also {
                if (index in it.indices) it.removeAt(index)
            }
            entry.copy(meals = entry.meals.updated(bucket, updated))
        }
    }

    fun copyYesterday() {
        viewModelScope.launch {
            dayEntryRepository.copyMeals(selectedDate.value.minusDays(1), selectedDate.value)
        }
    }

    fun copyBucketFromYesterday(bucket: MealBucket) {
        viewModelScope.launch {
            dayEntryRepository.copyMealBucket(selectedDate.value.minusDays(1), selectedDate.value, bucket)
        }
    }

    fun setExercise(level: ExerciseLevel) {
        updateEntry { it.copy(exerciseLevel = level) }
    }

    fun setWeight(text: String) {
        updateEntry { it.copy(weight = text.toDoubleOrNull()) }
    }

    private fun updateEntry(transform: (DayEntry) -> DayEntry) {
        viewModelScope.launch {
            val date = selectedDate.value
            val current = dayEntryRepository.getDay(date) ?: DayEntry(date = date, meals = MealParts())
            dayEntryRepository.upsert(transform(current))
        }
    }

    companion object {
        fun factory(container: AppContainer, initialEpochDay: Long?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DailyLogViewModel(
                        container.dayEntryRepository,
                        container.settingsRepository,
                        initialEpochDay,
                    ) as T
                }
            }
    }
}
