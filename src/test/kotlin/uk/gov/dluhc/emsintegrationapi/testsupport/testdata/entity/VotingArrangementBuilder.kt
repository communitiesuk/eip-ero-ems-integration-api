package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity

import uk.gov.dluhc.emsintegrationapi.database.entity.VotingArrangement
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker.Companion.faker
import java.time.LocalDate

fun buildVotingArrangement(
    untilFurtherNotice: Boolean = false,
    forSingleDate: LocalDate? = faker.date().birthday().toLocalDateTime().toLocalDate(),
    startDate: LocalDate? = null,
    endDate: LocalDate? = null,
) = VotingArrangement(
    untilFurtherNotice = untilFurtherNotice,
    forSingleDate = forSingleDate,
    startDate = startDate,
    endDate = endDate,
)
