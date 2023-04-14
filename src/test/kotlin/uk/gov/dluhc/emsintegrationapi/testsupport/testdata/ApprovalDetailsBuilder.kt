package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import uk.gov.dluhc.emsintegrationapi.database.entity.ApprovalDetails
import java.time.Instant
import java.time.ZoneOffset
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApprovalDetails as ApprovalDetailsMessageDto

const val GSS_CODE1 = "E12345678"
const val GSS_CODE2 = "E12345679"
fun buildApprovalDetailsEntity(
    createdAt: Instant = getPastDateTime(),
    gssCode: String = getRandomGssCode(),
    authorisedAt: Instant = getPastDateTime(5),
    authorisingStaffId: String = getRandomEmailAddress(),
    source: String = getRandomString(10),
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
    source: String = getRandomString(10),
) = ApprovalDetailsMessageDto(
    id = applicationId,
    createdAt = createdAt.atOffset(ZoneOffset.UTC),
    gssCode = gssCode,
    authorisedAt = authorisedAt.atOffset(ZoneOffset.UTC),
    authorisingStaffId = authorisingStaffId,
    source = source
)
