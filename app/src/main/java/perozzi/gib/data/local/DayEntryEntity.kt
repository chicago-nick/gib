package perozzi.gib.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_entries")
data class DayEntryEntity(
    @PrimaryKey val dateEpochDay: Long,
    val breakfastParts: String,
    val lunchParts: String,
    val dinnerParts: String,
    val postDinnerParts: String,
    val exerciseLevel: Int,
    val weight: Double?,
)
