package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.dto

import uk.gov.dluhc.emsintegrationapi.dto.VotingArrangementDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker.Companion.faker
import java.time.LocalDate

fun buildVotingArrangementDto(
    untilFurtherNotice: Boolean = false,
    forSingleDate: LocalDate? = faker.date().birthday().toLocalDateTime().toLocalDate(),
    startDate: LocalDate? = null,
    endDate: LocalDate? = null,
) = VotingArrangementDto(
    untilFurtherNotice = untilFurtherNotice,
    forSingleDate = forSingleDate,
    startDate = startDate,
    endDate = endDate,
)
