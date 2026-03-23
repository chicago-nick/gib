package perozzi.gib.domain.usecase

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale
import perozzi.gib.domain.model.ComputedDaySummary
import perozzi.gib.domain.model.DayEntry
import perozzi.gib.domain.model.ExerciseLevel
import perozzi.gib.domain.model.MealBucket
import perozzi.gib.domain.model.TrendPoint
import perozzi.gib.domain.model.UserSettings

object BehaviorCalculator {
    private const val CaloriesPerPound = 3500.0

    fun mealTotal(parts: List<Int>): Int = parts.sum()

    fun dailyCalories(entry: DayEntry): Int = MealBucket.entries.sumOf { mealTotal(entry.meals.partsFor(it)) }

    fun baselineCaloriesOut(weightLbs: Double?, settings: UserSettings): Int? {
        val resolvedWeightLbs = weightLbs ?: return null
        val heightCm = settings.heightCm ?: return null
        val ageYears = settings.ageYears ?: return null
        val weightKg = resolvedWeightLbs * 0.45359237
        val bmr = (10 * weightKg) + (6.25 * heightCm) - (5 * ageYears) + settings.sex.bmrOffset
        return bmr.toInt()
    }

    fun requiredDailyCalorieAdjustment(
        currentWeightLbs: Double?,
        settings: UserSettings,
        today: LocalDate,
    ): Int? {
        val currentWeight = currentWeightLbs ?: return null
        val targetWeight = settings.targetWeightLbs ?: return null
        val targetDate = settings.targetDateEpochDay?.let(LocalDate::ofEpochDay) ?: return null
        val daysRemaining = ChronoUnit.DAYS.between(today, targetDate).toInt().coerceAtLeast(1)
        val poundsToLosePerDay = (currentWeight - targetWeight) / daysRemaining
        return (poundsToLosePerDay * CaloriesPerPound).toInt()
    }

    fun recommendedCalories(
        settings: UserSettings,
        exerciseLevel: ExerciseLevel,
        currentWeightLbs: Double?,
        today: LocalDate,
    ): Int {
        val baseline = baselineCaloriesOut(currentWeightLbs, settings) ?: 0
        val caloriesOut = (baseline * exerciseLevel.coefficient).toInt()
        val requiredAdjustment = requiredDailyCalorieAdjustment(currentWeightLbs, settings, today) ?: 0
        return caloriesOut - requiredAdjustment
    }

    fun daySummary(entry: DayEntry, settings: UserSettings, currentWeightLbs: Double?): ComputedDaySummary {
        val total = dailyCalories(entry)
        val recommended = recommendedCalories(settings, entry.exerciseLevel, currentWeightLbs, entry.date)
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
            .map { (week, items) -> week to items.sumOf { it.alcoholDrinks } }
    }

    fun currentWeekAlcoholCount(entries: List<DayEntry>, today: LocalDate): Int {
        val startOfWeek = today.with(DayOfWeek.MONDAY)
        return entries
            .filter { !it.date.isBefore(startOfWeek) && !it.date.isAfter(today) }
            .sumOf { it.alcoholDrinks }
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

    fun recommendedTrend(entries: List<DayEntry>, settings: UserSettings): List<TrendPoint> {
        var lastKnownWeight: Double? = null
        return entries.sortedBy { it.date }.map { entry ->
            val effectiveWeight = entry.weight ?: lastKnownWeight
            if (entry.weight != null) {
                lastKnownWeight = entry.weight
            }
            TrendPoint(
                entry.date,
                recommendedCalories(settings, entry.exerciseLevel, effectiveWeight, entry.date).toDouble()
            )
        }
    }

    fun exerciseFrequency(entries: List<DayEntry>): Map<ExerciseLevel, Int> {
        return ExerciseLevel.entries.associateWith { level -> entries.count { it.exerciseLevel == level } }
    }
}
