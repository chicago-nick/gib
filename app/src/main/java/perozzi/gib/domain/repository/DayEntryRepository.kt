package perozzi.gib.domain.repository

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import perozzi.gib.domain.model.DayEntry
import perozzi.gib.domain.model.MealBucket

interface DayEntryRepository {
    fun observeDay(date: LocalDate): Flow<DayEntry?>
    fun observeRecentDays(limit: Int = 90): Flow<List<DayEntry>>
    fun observeDateRange(start: LocalDate, end: LocalDate): Flow<List<DayEntry>>
    suspend fun getDay(date: LocalDate): DayEntry?
    suspend fun upsert(dayEntry: DayEntry)
    suspend fun copyMeals(from: LocalDate, to: LocalDate)
    suspend fun copyMealBucket(from: LocalDate, to: LocalDate, bucket: MealBucket)
}
