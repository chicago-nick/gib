package perozzi.gib.domain.model

import java.time.LocalDate

data class TrendPoint(
    val date: LocalDate,
    val value: Double,
)
