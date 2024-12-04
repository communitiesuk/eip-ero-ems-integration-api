package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity

import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus.PENDING
import uk.gov.dluhc.emsintegrationapi.database.entity.PersonalDetail
import uk.gov.dluhc.emsintegrationapi.database.entity.RegisterCheck
import uk.gov.dluhc.emsintegrationapi.database.entity.RegisterCheckMatch
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceType
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceType.VOTER_CARD
import java.time.Instant
import java.util.UUID

fun buildRegisterCheck(
    correlationId: UUID = UUID.randomUUID(),
    sourceReference: String = UUID.randomUUID().toString(),
    sourceCorrelationId: UUID = UUID.randomUUID(),
    sourceType: SourceType = VOTER_CARD,
    gssCode: String = "E09000021",
    status: CheckStatus = PENDING,
    matchCount: Int = 0,
    registerCheckMatches: MutableList<RegisterCheckMatch> = mutableListOf(),
    personalDetail: PersonalDetail = buildPersonalDetail(),
    emsElectorId: String = randomAlphanumeric(30),
    historicalSearch: Boolean = false,
    historicalSearchEarliestDate: Instant? = Instant.now(),
    createdBy: String = "system",
) = RegisterCheck(
    id = UUID.randomUUID(),
    correlationId = correlationId,
    sourceReference = sourceReference,
    sourceCorrelationId = sourceCorrelationId,
    sourceType = sourceType,
    gssCode = gssCode,
    status = status,
    matchCount = matchCount,
    registerCheckMatches = registerCheckMatches,
    personalDetail = personalDetail,
    emsElectorId = emsElectorId,
    historicalSearch = historicalSearch,
    historicalSearchEarliestDate = historicalSearchEarliestDate,
    createdBy = createdBy,
)
