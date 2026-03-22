package perozzi.gib.domain.repository

import kotlinx.coroutines.flow.Flow
import perozzi.gib.domain.model.UserSettings

interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun update(settings: UserSettings)
}
