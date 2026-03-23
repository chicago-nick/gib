package perozzi.gib

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import perozzi.gib.data.local.GibDatabase
import perozzi.gib.data.local.MIGRATION_1_2
import perozzi.gib.data.local.RoomDayEntryRepository
import perozzi.gib.data.local.SeedData
import perozzi.gib.data.local.SettingsDataStoreRepository
import perozzi.gib.domain.repository.DayEntryRepository
import perozzi.gib.domain.repository.SettingsRepository

class AppContainer(context: Context) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database = Room.databaseBuilder(
        context,
        GibDatabase::class.java,
        "gib.db"
    ).addMigrations(MIGRATION_1_2)
        .fallbackToDestructiveMigration(false)
        .build()

    val dayEntryRepository: DayEntryRepository = RoomDayEntryRepository(database.dayEntryDao())
    val settingsRepository: SettingsRepository = SettingsDataStoreRepository(context)

    init {
        appScope.launch {
            SeedData.initialize(dayEntryRepository, settingsRepository)
        }
    }
}
