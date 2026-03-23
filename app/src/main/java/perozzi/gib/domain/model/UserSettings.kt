package perozzi.gib.domain.model

data class UserSettings(
    val goal: UserGoal = UserGoal.Maintain,
    val targetWeightLbs: Double? = null,
    val targetDateEpochDay: Long? = null,
    val sex: UserSex = UserSex.Male,
    val heightCm: Int? = null,
    val ageYears: Int? = null,
)
