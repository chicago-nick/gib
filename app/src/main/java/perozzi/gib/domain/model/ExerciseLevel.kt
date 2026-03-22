package perozzi.gib.domain.model

enum class ExerciseLevel(val value: Int, val label: String) {
    None(0, "None"),
    Light(1, "Light"),
    Moderate(2, "Moderate"),
    Hard(3, "Hard");

    companion object {
        fun fromValue(value: Int): ExerciseLevel = entries.firstOrNull { it.value == value } ?: None
    }
}
