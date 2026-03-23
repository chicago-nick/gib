package perozzi.gib.domain.model

enum class ExerciseLevel(val score: Int, val coefficient: Double) {
    None(0, 1.08),
    Light(1, 1.20),
    Moderate(2, 1.30),
    Hard(3, 1.45),
    Intense(4, 1.6);

    companion object {
        fun fromScore(score: Int): ExerciseLevel = entries.firstOrNull { it.score == score } ?: None
    }
}
