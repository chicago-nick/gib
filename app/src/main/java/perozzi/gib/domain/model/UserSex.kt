package perozzi.gib.domain.model

enum class UserSex(val label: String, val bmrOffset: Int) {
    Male("Male", 5),
    Female("Female", -161),
}
