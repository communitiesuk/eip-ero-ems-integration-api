package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity

import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus.PENDING
import uk.gov.dluhc.emsintegrationapi.database.entity.PersonalDetail
import uk.gov.dluhc.emsintegrationapi.database.entity.RegisterCheck
import uk.gov.dluhc.emsintegrationapi.database.entity.RegisterCheckMatch
import java.time.Instant
import java.util.UUID

fun buildRegisterCheck(
    persisted: Boolean = false,
    correlationId: UUID = UUID.randomUUID(),
    sourceReference: String = UUID.randomUUID().toString(),
    sourceCorrelationId: UUID = UUID.randomUUID(),
    gssCode: String = "E09000021",
    status: CheckStatus = PENDING,
    matchCount: Int = 0,
    matchResultSentAt: Instant? = null,
    registerCheckMatches: MutableList<RegisterCheckMatch> = mutableListOf(),
    personalDetail: PersonalDetail = buildPersonalDetail(),
    emsElectorId: String = randomAlphanumeric(30),
    historicalSearch: Boolean = false,
    historicalSearchEarliestDate: Instant? = Instant.now(),
    createdBy: String = "system",
) = RegisterCheck(
    id = if (persisted) UUID.randomUUID() else null,
    correlationId = correlationId,
    sourceReference = sourceReference,
    sourceCorrelationId = sourceCorrelationId,
    gssCode = gssCode,
    status = status,
    matchCount = matchCount,
    matchResultSentAt = matchResultSentAt,
    registerCheckMatches = registerCheckMatches,
    personalDetail = personalDetail,
    emsElectorId = emsElectorId,
    historicalSearch = historicalSearch,
    historicalSearchEarliestDate = historicalSearchEarliestDate,
    createdBy = createdBy,
).apply {
    if (!persisted) {
        stripIdsForIntegrationTests()
    }
}

fun RegisterCheck.stripIdsForIntegrationTests() {
    id = null
    registerCheckMatches.forEach { it.stripIdsForIntegrationTests() }
}
