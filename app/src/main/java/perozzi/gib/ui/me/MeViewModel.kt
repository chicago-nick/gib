package perozzi.gib.ui.me

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
import kotlinx.coroutines.launch
import perozzi.gib.AppContainer
import perozzi.gib.domain.repository.DayEntryRepository
import perozzi.gib.domain.model.UserGoal
import perozzi.gib.domain.model.UserSettings
import perozzi.gib.domain.model.UserSex
import perozzi.gib.domain.repository.SettingsRepository

data class MeUiState(
    val settings: UserSettings = UserSettings(),
    val draftTargetWeightLbs: String = "",
    val draftTargetDate: String = "",
    val draftHeightCm: String = "",
    val draftAgeYears: String = "",
    val currentWeightLbs: String = "",
    val currentWeightSupporting: String = "",
)

class MeViewModel(
    private val settingsRepository: SettingsRepository,
    private val dayEntryRepository: DayEntryRepository,
) : ViewModel() {
    private val draftSettings = MutableStateFlow<UserSettings?>(null)
    private val draftTargetDateText = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MeUiState> = combine(
        settingsRepository.settings,
        dayEntryRepository.observeRecentDays(90).map { entries ->
            entries.sortedByDescending { it.date }.firstOrNull { it.weight != null }?.weight
        },
        draftSettings,
        draftTargetDateText,
    ) { stored, latestWeight, draft, targetDateText ->
        val settings = draft ?: stored
        MeUiState(
            settings = settings,
            draftTargetWeightLbs = settings.targetWeightLbs?.toInt()?.toString().orEmpty(),
            draftTargetDate = targetDateText ?: settings.targetDateEpochDay?.let(LocalDate::ofEpochDay)?.toString().orEmpty(),
            draftHeightCm = settings.heightCm?.toString().orEmpty(),
            draftAgeYears = settings.ageYears?.toString().orEmpty(),
            currentWeightLbs = latestWeight?.toInt()?.toString().orEmpty(),
            currentWeightSupporting = if (latestWeight == null) {
                "Log your weight in the 'Daily Log' tab to get this"
            } else {
                "Based on your most recent weight log"
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MeUiState())

    fun setGoal(goal: UserGoal) {
        updateDraftAndPersist { it.copy(goal = goal) }
    }

    fun setTargetWeightLbs(value: String) {
        updateDraftAndPersist { it.copy(targetWeightLbs = value.toDoubleOrNull()) }
    }

    fun setTargetDate(value: String) {
        draftTargetDateText.value = value
        runCatching { LocalDate.parse(value).toEpochDay() }.getOrNull()?.let { epochDay ->
            updateDraftAndPersist {
                it.copy(targetDateEpochDay = epochDay)
            }
        }
    }

    fun setSex(sex: UserSex) {
        updateDraftAndPersist { it.copy(sex = sex) }
    }

    fun setHeightCm(value: String) {
        updateDraftAndPersist { it.copy(heightCm = value.toIntOrNull()) }
    }

    fun setAgeYears(value: String) {
        updateDraftAndPersist { it.copy(ageYears = value.toIntOrNull()) }
    }

    fun save() {
        viewModelScope.launch {
            settingsRepository.update(draftSettings.value ?: uiState.value.settings)
        }
    }

    private fun updateDraft(transform: (UserSettings) -> UserSettings) {
        val current = draftSettings.value ?: uiState.value.settings
        draftSettings.value = transform(current)
    }

    private fun updateDraftAndPersist(transform: (UserSettings) -> UserSettings) {
        val updated = transform(draftSettings.value ?: uiState.value.settings)
        draftSettings.value = updated
        viewModelScope.launch {
            settingsRepository.update(updated)
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MeViewModel(container.settingsRepository, container.dayEntryRepository) as T
            }
        }
    }
}
