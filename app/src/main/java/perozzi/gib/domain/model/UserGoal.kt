package perozzi.gib.domain.model

enum class UserGoal(val label: String, val calorieOffset: Int) {
    Cut("Cut", -300),
    Maintain("Maintain", 0),
    Gain("Gain", 250)
}
