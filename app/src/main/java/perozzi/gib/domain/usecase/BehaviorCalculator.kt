package perozzi.gib.domain.usecase

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import perozzi.gib.domain.model.ComputedDaySummary
import perozzi.gib.domain.model.DayEntry
import perozzi.gib.domain.model.ExerciseLevel
import perozzi.gib.domain.model.MealBucket
import perozzi.gib.domain.model.TrendPoint
import perozzi.gib.domain.model.UserSettings

object BehaviorCalculator {
    fun mealTotal(parts: List<Int>): Int = parts.sum()

    fun dailyCalories(entry: DayEntry): Int = MealBucket.entries.sumOf { mealTotal(entry.meals.partsFor(it)) }

    fun recommendedCalories(settings: UserSettings, exerciseLevel: ExerciseLevel): Int {
        return settings.baseCalorieTarget + settings.goal.calorieOffset + settings.adjustmentFor(exerciseLevel)
    }

    fun daySummary(entry: DayEntry, settings: UserSettings): ComputedDaySummary {
        val total = dailyCalories(entry)
        val recommended = recommendedCalories(settings, entry.exerciseLevel)
        return ComputedDaySummary(
            totalCalories = total,
            recommendedCalories = recommended,
            calorieDelta = total - recommended,
        )
    }

    fun rollingCalorieAverage(entries: List<DayEntry>, windowSize: Int = 7): Double? {
        if (entries.isEmpty()) return null
        val recent = entries.sortedBy { it.date }.takeLast(windowSize)
        return recent.map(::dailyCalories).average()
    }

    fun weeklyAlcoholTallies(entries: List<DayEntry>): List<Pair<String, Int>> {
        val weekFields = WeekFields.of(Locale.US)
        return entries
            .groupBy {
                val week = it.date.get(weekFields.weekOfWeekBasedYear())
                "${it.date.year}-W${week.toString().padStart(2, '0')}"
            }
            .toSortedMap()
            .map { (week, items) -> week to items.count { it.drankAlcohol } }
    }

    fun currentWeekAlcoholCount(entries: List<DayEntry>, today: LocalDate): Int {
        val startOfWeek = today.with(DayOfWeek.MONDAY)
        return entries.count { it.drankAlcohol && !it.date.isBefore(startOfWeek) && !it.date.isAfter(today) }
    }

    fun weightTrend(entries: List<DayEntry>, windowSize: Int = 5): List<TrendPoint> {
        val weighted = entries.sortedBy { it.date }.filter { it.weight != null }
        return weighted.mapIndexedNotNull { index, entry ->
            val window = weighted.subList(maxOf(0, index - windowSize + 1), index + 1).mapNotNull { it.weight }
            if (window.isEmpty()) null else TrendPoint(entry.date, window.average())
        }
    }

    fun calorieTrend(entries: List<DayEntry>): List<TrendPoint> =
        entries.sortedBy { it.date }.map { TrendPoint(it.date, dailyCalories(it).toDouble()) }

    fun recommendedTrend(entries: List<DayEntry>, settings: UserSettings): List<TrendPoint> =
        entries.sortedBy { it.date }.map { TrendPoint(it.date, recommendedCalories(settings, it.exerciseLevel).toDouble()) }

    fun exerciseFrequency(entries: List<DayEntry>): Map<ExerciseLevel, Int> {
        return ExerciseLevel.entries.associateWith { level -> entries.count { it.exerciseLevel == level } }
    }
}
