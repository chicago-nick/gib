package perozzi.gib.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DayEntryDao {
    @Query("SELECT * FROM day_entries WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    fun observeByDate(dateEpochDay: Long): Flow<DayEntryEntity?>

    @Query("SELECT * FROM day_entries WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY dateEpochDay ASC")
    fun observeDateRange(startEpochDay: Long, endEpochDay: Long): Flow<List<DayEntryEntity>>

    @Query("SELECT * FROM day_entries ORDER BY dateEpochDay DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<DayEntryEntity>>

    @Query("SELECT * FROM day_entries WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getByDate(dateEpochDay: Long): DayEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DayEntryEntity)

    @Query("SELECT COUNT(*) FROM day_entries")
    suspend fun count(): Int
}
