package perozzi.gib.domain.model

data class UserSettings(
    val goal: UserGoal = UserGoal.Maintain,
    val baseCalorieTarget: Int = 2200,
    val lightExerciseAdjustment: Int = 150,
    val moderateExerciseAdjustment: Int = 300,
    val hardExerciseAdjustment: Int = 500,
) {
    fun adjustmentFor(level: ExerciseLevel): Int = when (level) {
        ExerciseLevel.None -> 0
        ExerciseLevel.Light -> lightExerciseAdjustment
        ExerciseLevel.Moderate -> moderateExerciseAdjustment
        ExerciseLevel.Hard -> hardExerciseAdjustment
    }
}
