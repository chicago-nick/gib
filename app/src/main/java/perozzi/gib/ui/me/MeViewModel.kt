package perozzi.gib.ui.me

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import perozzi.gib.AppContainer
import perozzi.gib.domain.model.ExerciseLevel
import perozzi.gib.domain.model.UserGoal
import perozzi.gib.domain.model.UserSettings
import perozzi.gib.domain.repository.SettingsRepository

data class MeUiState(
    val settings: UserSettings = UserSettings(),
    val draftBaseTarget: String = "",
    val draftLight: String = "",
    val draftModerate: String = "",
    val draftHard: String = "",
)

class MeViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val draftSettings = MutableStateFlow<UserSettings?>(null)

    val uiState: StateFlow<MeUiState> = combine(
        settingsRepository.settings,
        draftSettings,
    ) { stored, draft ->
        val settings = draft ?: stored
        MeUiState(
            settings = settings,
            draftBaseTarget = settings.baseCalorieTarget.toString(),
            draftLight = settings.lightExerciseAdjustment.toString(),
            draftModerate = settings.moderateExerciseAdjustment.toString(),
            draftHard = settings.hardExerciseAdjustment.toString(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MeUiState())

    fun setGoal(goal: UserGoal) {
        updateDraft { it.copy(goal = goal) }
    }

    fun setBaseTarget(value: String) {
        value.toIntOrNull()?.let { parsed ->
            updateDraft { it.copy(baseCalorieTarget = parsed) }
        }
    }

    fun setExerciseAdjustment(level: ExerciseLevel, value: String) {
        val parsed = value.toIntOrNull() ?: return
        updateDraft {
            when (level) {
                ExerciseLevel.None -> it
                ExerciseLevel.Light -> it.copy(lightExerciseAdjustment = parsed)
                ExerciseLevel.Moderate -> it.copy(moderateExerciseAdjustment = parsed)
                ExerciseLevel.Hard -> it.copy(hardExerciseAdjustment = parsed)
            }
        }
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

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MeViewModel(container.settingsRepository) as T
            }
        }
    }
}
