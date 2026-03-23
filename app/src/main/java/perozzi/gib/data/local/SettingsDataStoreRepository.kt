package perozzi.gib.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import perozzi.gib.domain.model.UserGoal
import perozzi.gib.domain.model.UserSex
import perozzi.gib.domain.model.UserSettings
import perozzi.gib.domain.repository.SettingsRepository

private val Context.dataStore by preferencesDataStore(name = "gib_settings")

class SettingsDataStoreRepository(
    private val context: Context,
) : SettingsRepository {
    override val settings: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        UserSettings(
            goal = UserGoal.valueOf(preferences[GoalKey] ?: UserGoal.Maintain.name),
            targetWeightLbs = preferences[TargetWeightLbsKey],
            targetDateEpochDay = preferences[TargetDateEpochDayKey],
            sex = UserSex.valueOf(preferences[SexKey] ?: UserSex.Male.name),
            heightCm = preferences[HeightCmKey],
            ageYears = preferences[AgeYearsKey],
        )
    }

    override suspend fun update(settings: UserSettings) {
        context.dataStore.edit { preferences ->
            preferences[GoalKey] = settings.goal.name
            settings.targetWeightLbs?.let { preferences[TargetWeightLbsKey] = it } ?: preferences.remove(TargetWeightLbsKey)
            settings.targetDateEpochDay?.let { preferences[TargetDateEpochDayKey] = it } ?: preferences.remove(TargetDateEpochDayKey)
            preferences[SexKey] = settings.sex.name
            settings.heightCm?.let { preferences[HeightCmKey] = it } ?: preferences.remove(HeightCmKey)
            settings.ageYears?.let { preferences[AgeYearsKey] = it } ?: preferences.remove(AgeYearsKey)
        }
    }

    private companion object {
        val GoalKey = stringPreferencesKey("goal")
        val TargetWeightLbsKey = doublePreferencesKey("target_weight_lbs")
        val TargetDateEpochDayKey = longPreferencesKey("target_date_epoch_day")
        val SexKey = stringPreferencesKey("sex")
        val HeightCmKey = intPreferencesKey("height_cm")
        val AgeYearsKey = intPreferencesKey("age_years")
    }
}
