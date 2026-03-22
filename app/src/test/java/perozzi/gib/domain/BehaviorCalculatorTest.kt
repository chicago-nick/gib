package perozzi.gib.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import perozzi.gib.domain.model.DayEntry
import perozzi.gib.domain.model.ExerciseLevel
import perozzi.gib.domain.model.MealParts
import perozzi.gib.domain.model.UserGoal
import perozzi.gib.domain.model.UserSettings
import perozzi.gib.domain.usecase.BehaviorCalculator

class BehaviorCalculatorTest {
    @Test
    fun mealAndDayTotals_areSummedFromParts() {
        val entry = DayEntry(
            date = LocalDate.of(2026, 3, 22),
            meals = MealParts(
                breakfast = listOf(280, 120, 90, 90, 15),
                lunch = listOf(600),
                dinner = listOf(700),
                postDinner = listOf(180),
            )
        )

        assertEquals(595, BehaviorCalculator.mealTotal(entry.meals.breakfast))
        assertEquals(2075, BehaviorCalculator.dailyCalories(entry))
    }

    @Test
    fun recommendedCalories_includeGoalAndExerciseAdjustment() {
        val settings = UserSettings(
            goal = UserGoal.Cut,
            baseCalorieTarget = 2200,
            lightExerciseAdjustment = 100,
            moderateExerciseAdjustment = 250,
            hardExerciseAdjustment = 400,
        )

        assertEquals(2150, BehaviorCalculator.recommendedCalories(settings, ExerciseLevel.Moderate))
    }

    @Test
    fun rollingAverage_usesRecentWindow() {
        val start = LocalDate.of(2026, 3, 1)
        val entries = (0..9).map { offset ->
            DayEntry(
                date = start.plusDays(offset.toLong()),
                meals = MealParts(breakfast = listOf(100 + offset), lunch = listOf(200), dinner = listOf(300), postDinner = listOf(400))
            )
        }

        assertEquals(1006.0, BehaviorCalculator.rollingCalorieAverage(entries, 7)!!, 0.01)
    }

    @Test
    fun weightTrend_returnsSmoothedValues() {
        val start = LocalDate.of(2026, 3, 1)
        val entries = listOf(203.0, 202.5, 202.0, 201.5, 201.0).mapIndexed { index, value ->
            DayEntry(date = start.plusDays(index.toLong()), weight = value)
        }

        val trend = BehaviorCalculator.weightTrend(entries, 3)

        assertNotNull(trend)
        assertEquals(5, trend.size)
        assertEquals(201.5, trend.last().value, 0.01)
    }
}
