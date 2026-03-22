package perozzi.gib.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import perozzi.gib.domain.model.UserGoal
import perozzi.gib.domain.model.UserSettings
import perozzi.gib.domain.repository.SettingsRepository

private val Context.dataStore by preferencesDataStore(name = "gib_settings")

class SettingsDataStoreRepository(
    private val context: Context,
) : SettingsRepository {
    override val settings: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        UserSettings(
            goal = UserGoal.valueOf(preferences[GoalKey] ?: UserGoal.Maintain.name),
            baseCalorieTarget = preferences[BaseTargetKey] ?: 2200,
            lightExerciseAdjustment = preferences[LightAdjustmentKey] ?: 150,
            moderateExerciseAdjustment = preferences[ModerateAdjustmentKey] ?: 300,
            hardExerciseAdjustment = preferences[HardAdjustmentKey] ?: 500,
        )
    }

    override suspend fun update(settings: UserSettings) {
        context.dataStore.edit { preferences ->
            preferences[GoalKey] = settings.goal.name
            preferences[BaseTargetKey] = settings.baseCalorieTarget
            preferences[LightAdjustmentKey] = settings.lightExerciseAdjustment
            preferences[ModerateAdjustmentKey] = settings.moderateExerciseAdjustment
            preferences[HardAdjustmentKey] = settings.hardExerciseAdjustment
        }
    }

    private companion object {
        val GoalKey = stringPreferencesKey("goal")
        val BaseTargetKey = intPreferencesKey("base_target")
        val LightAdjustmentKey = intPreferencesKey("light_adjustment")
        val ModerateAdjustmentKey = intPreferencesKey("moderate_adjustment")
        val HardAdjustmentKey = intPreferencesKey("hard_adjustment")
    }
}
