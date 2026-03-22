package perozzi.gib.domain.model

data class ComputedDaySummary(
    val totalCalories: Int,
    val recommendedCalories: Int,
    val calorieDelta: Int,
)
