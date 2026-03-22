package perozzi.gib.domain.model

data class MealParts(
    val breakfast: List<Int> = emptyList(),
    val lunch: List<Int> = emptyList(),
    val dinner: List<Int> = emptyList(),
    val postDinner: List<Int> = emptyList(),
) {
    fun partsFor(bucket: MealBucket): List<Int> = when (bucket) {
        MealBucket.Breakfast -> breakfast
        MealBucket.Lunch -> lunch
        MealBucket.Dinner -> dinner
        MealBucket.PostDinner -> postDinner
    }

    fun updated(bucket: MealBucket, parts: List<Int>): MealParts = when (bucket) {
        MealBucket.Breakfast -> copy(breakfast = parts)
        MealBucket.Lunch -> copy(lunch = parts)
        MealBucket.Dinner -> copy(dinner = parts)
        MealBucket.PostDinner -> copy(postDinner = parts)
    }
}
