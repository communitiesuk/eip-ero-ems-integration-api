package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import org.apache.commons.lang3.RandomStringUtils
import uk.gov.dluhc.emsintegrationapi.database.entity.ApprovalDetails
import java.time.Instant
import java.time.ZoneOffset
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApprovalDetails as ApprovalDetailsMessageDto

fun buildApprovalDetailsEntity(
    createdAt: Instant = getPastDateTime(),
    gssCode: String = getRandomGssCode(),
    authorisedAt: Instant = getPastDateTime(5),
    authorisingStaffId: String = getRandomEmailAddress(),
    source: String = RandomStringUtils.random(5),
) = ApprovalDetails(
    createdAt = createdAt,
    gssCode = gssCode,
    authorisedAt = authorisedAt,
    authorisingStaffId = authorisingStaffId,
    source = source
)

fun buildApprovalDetailsMessageDto(
    applicationId: String = getRandomAlphaNumeric(24),
    createdAt: Instant = getPastDateTime(),
    gssCode: String = getRandomGssCode(),
    authorisedAt: Instant = getPastDateTime(5),
    authorisingStaffId: String = getRandomEmailAddress(),
    source: String = RandomStringUtils.random(5),
) = ApprovalDetailsMessageDto(
    id = applicationId,
    createdAt = createdAt.atOffset(ZoneOffset.UTC),
    gssCode = gssCode,
    authorisedAt = authorisedAt.atOffset(ZoneOffset.UTC),
    authorisingStaffId = authorisingStaffId,
    source = source
)
