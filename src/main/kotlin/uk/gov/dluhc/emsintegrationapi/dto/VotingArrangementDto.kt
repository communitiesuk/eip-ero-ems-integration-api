package uk.gov.dluhc.emsintegrationapi.dto

import java.time.LocalDate

data class VotingArrangementDto(
    val untilFurtherNotice: Boolean,
    val forSingleDate: LocalDate?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
)
