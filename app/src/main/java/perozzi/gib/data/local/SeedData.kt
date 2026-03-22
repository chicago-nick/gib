package perozzi.gib.data.local

import java.time.LocalDate
import perozzi.gib.domain.model.DayEntry
import perozzi.gib.domain.model.ExerciseLevel
import perozzi.gib.domain.model.MealParts
import perozzi.gib.domain.model.UserGoal
import perozzi.gib.domain.model.UserSettings
import perozzi.gib.domain.repository.DayEntryRepository
import perozzi.gib.domain.repository.SettingsRepository

object SeedData {
    suspend fun initialize(
        dayEntryRepository: DayEntryRepository,
        settingsRepository: SettingsRepository,
    ) {
        settingsRepository.update(
            UserSettings(
                goal = UserGoal.Cut,
                baseCalorieTarget = 2150,
                lightExerciseAdjustment = 125,
                moderateExerciseAdjustment = 275,
                hardExerciseAdjustment = 450,
            )
        )

        val today = LocalDate.now()

        val samples = listOf(
            DayEntry(today.minusDays(16), MealParts(listOf(260, 110), listOf(590), listOf(710), listOf(140)), false, ExerciseLevel.None, 204.4),
            DayEntry(today.minusDays(15), MealParts(listOf(290, 90), listOf(620), listOf(780), listOf(190)), true, ExerciseLevel.Light, null),
            DayEntry(today.minusDays(14), MealParts(listOf(270, 120, 70), listOf(610), listOf(730), listOf(160, 40)), false, ExerciseLevel.Moderate, 204.0),
            DayEntry(today.minusDays(13), MealParts(listOf(300, 100), listOf(650), listOf(760), listOf(180)), false, ExerciseLevel.Hard, null),
            DayEntry(today.minusDays(12), MealParts(listOf(280, 120), listOf(600, 90), listOf(720), listOf(170)), true, ExerciseLevel.None, 203.7),
            DayEntry(today.minusDays(11), MealParts(listOf(260, 110, 80), listOf(630), listOf(700), listOf(150, 50)), false, ExerciseLevel.Light, null),
            DayEntry(today.minusDays(10), MealParts(listOf(310, 95), listOf(640), listOf(810), listOf(160)), false, ExerciseLevel.Moderate, 203.3),
            DayEntry(today.minusDays(9), MealParts(listOf(275, 120), listOf(610, 100), listOf(740), listOf(210)), true, ExerciseLevel.Light, null),
            DayEntry(today.minusDays(8), MealParts(listOf(285, 105, 60), listOf(620), listOf(690), listOf(140, 70)), false, ExerciseLevel.Hard, 203.0),
            DayEntry(today.minusDays(7), MealParts(listOf(295, 115), listOf(630), listOf(750), listOf(180)), false, ExerciseLevel.Moderate, null),
            DayEntry(today.minusDays(6), MealParts(listOf(280, 120, 90), listOf(650), listOf(720), listOf(210)), false, ExerciseLevel.Light, 202.6),
            DayEntry(today.minusDays(5), MealParts(listOf(310, 95), listOf(640, 110), listOf(760), listOf(180)), true, ExerciseLevel.None, null),
            DayEntry(today.minusDays(4), MealParts(listOf(280, 120, 90, 90, 15), listOf(590), listOf(840), listOf(160)), false, ExerciseLevel.Hard, 201.8),
            DayEntry(today.minusDays(3), MealParts(listOf(290, 110), listOf(610), listOf(700), listOf(190, 70)), false, ExerciseLevel.Moderate, null),
            DayEntry(today.minusDays(2), MealParts(listOf(300, 90), listOf(620), listOf(680), listOf(210)), true, ExerciseLevel.Light, 201.3),
            DayEntry(today.minusDays(1), MealParts(listOf(280, 120, 90), listOf(610), listOf(730), listOf(170, 60)), false, ExerciseLevel.Moderate, null),
            DayEntry(today, MealParts(listOf(280, 120), listOf(540), emptyList(), emptyList()), false, ExerciseLevel.None, 201.0),
        )
        for (sample in samples) {
            if (dayEntryRepository.getDay(sample.date) == null) {
                dayEntryRepository.upsert(sample)
            }
        }
    }
}
