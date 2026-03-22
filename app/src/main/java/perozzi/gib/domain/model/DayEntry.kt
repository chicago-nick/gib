package perozzi.gib.domain.model

import java.time.LocalDate

data class DayEntry(
    val date: LocalDate,
    val meals: MealParts = MealParts(),
    val drankAlcohol: Boolean = false,
    val exerciseLevel: ExerciseLevel = ExerciseLevel.None,
    val weight: Double? = null,
)
