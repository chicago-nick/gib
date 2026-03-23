package perozzi.gib.data.local

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import perozzi.gib.domain.model.DayEntry
import perozzi.gib.domain.model.ExerciseLevel
import perozzi.gib.domain.model.MealBucket
import perozzi.gib.domain.model.MealParts
import perozzi.gib.domain.repository.DayEntryRepository

class RoomDayEntryRepository(
    private val dao: DayEntryDao,
) : DayEntryRepository {
    override fun observeDay(date: LocalDate): Flow<DayEntry?> =
        dao.observeByDate(date.toEpochDay()).map { it?.toDomain() }

    override fun observeRecentDays(limit: Int): Flow<List<DayEntry>> =
        dao.observeRecent(limit).map { items -> items.map { it.toDomain() } }

    override fun observeDateRange(start: LocalDate, end: LocalDate): Flow<List<DayEntry>> =
        dao.observeDateRange(start.toEpochDay(), end.toEpochDay()).map { items -> items.map { it.toDomain() } }

    override suspend fun getDay(date: LocalDate): DayEntry? = dao.getByDate(date.toEpochDay())?.toDomain()

    override suspend fun upsert(dayEntry: DayEntry) {
        dao.upsert(dayEntry.toEntity())
    }

    override suspend fun copyMeals(from: LocalDate, to: LocalDate) {
        val source = getDay(from) ?: return
        val target = getDay(to) ?: DayEntry(date = to)
        upsert(target.copy(meals = source.meals))
    }

    override suspend fun copyMealBucket(from: LocalDate, to: LocalDate, bucket: MealBucket) {
        val source = getDay(from) ?: return
        val target = getDay(to) ?: DayEntry(date = to)
        val updatedMeals = target.meals.updated(bucket, source.meals.partsFor(bucket))
        upsert(target.copy(meals = updatedMeals))
    }
}

private fun DayEntryEntity.toDomain(): DayEntry = DayEntry(
    date = LocalDate.ofEpochDay(dateEpochDay),
    meals = MealParts(
        breakfast = breakfastParts.toPartList(),
        lunch = lunchParts.toPartList(),
        dinner = dinnerParts.toPartList(),
        postDinner = postDinnerParts.toPartList(),
    ),
    alcoholDrinks = alcoholDrinks,
    exerciseLevel = ExerciseLevel.fromScore(exerciseLevel),
    weight = weight,
)

private fun DayEntry.toEntity(): DayEntryEntity = DayEntryEntity(
    dateEpochDay = date.toEpochDay(),
    breakfastParts = meals.breakfast.joinToString(","),
    lunchParts = meals.lunch.joinToString(","),
    dinnerParts = meals.dinner.joinToString(","),
    postDinnerParts = meals.postDinner.joinToString(","),
    alcoholDrinks = alcoholDrinks,
    exerciseLevel = exerciseLevel.score,
    weight = weight,
)

private fun String.toPartList(): List<Int> = split(",")
    .mapNotNull { item -> item.trim().takeIf { it.isNotEmpty() }?.toIntOrNull() }
