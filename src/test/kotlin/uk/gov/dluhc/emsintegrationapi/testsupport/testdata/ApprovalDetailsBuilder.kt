package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicationDetails
import java.time.Instant
import java.time.ZoneOffset
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApplicationDetails as ApplicationDetailsMessageDto

const val GSS_CODE = "E12345678"
const val GSS_CODE2 = "E12345679"
fun buildApplicationDetailsEntity(
    createdAt: Instant = getPastDateTime(),
    gssCode: String = getRandomGssCode(),
    authorisedAt: Instant = getPastDateTime(5),
    authorisingStaffId: String = getRandomEmailAddress(),
    source: String = getRandomString(10),
    applicationStatus: ApplicationDetails.ApplicationStatus = ApplicationDetails.ApplicationStatus.APPROVED,
    signatureBase64: String? = null,
    signatureWaived: Boolean? = null,
    signatureWaivedReason: String? = null,
) = ApplicationDetails(
    createdAt = createdAt,
    gssCode = gssCode,
    authorisedAt = authorisedAt,
    authorisingStaffId = authorisingStaffId,
    source = source,
    signatureBase64 = signatureBase64,
    signatureWaivedReason = signatureWaivedReason,
    signatureWaived = signatureWaived,
    applicationStatus = applicationStatus
)

fun buildApplicationDetailsMessageDto(
    applicationId: String = getRandomAlphaNumeric(24),
    createdAt: Instant = getPastDateTime(),
    gssCode: String = getRandomGssCode(),
    authorisedAt: Instant = getPastDateTime(5),
    authorisingStaffId: String = getRandomEmailAddress(),
    source: String = getRandomString(10),
    applicationStatus: ApplicationDetailsMessageDto.ApplicationStatus = ApplicationDetailsMessageDto.ApplicationStatus.APPROVED,
    signature: ByteArray? = null,
    signatureWaived: Boolean? = null,
    signatureWaivedReason: String? = null,
) = ApplicationDetailsMessageDto(
    id = applicationId,
    createdAt = createdAt.atOffset(ZoneOffset.UTC),
    gssCode = gssCode,
    authorisedAt = authorisedAt.atOffset(ZoneOffset.UTC),
    authorisingStaffId = authorisingStaffId,
    source = source,
    applicationStatus = applicationStatus,
    signatureWaived = signatureWaived,
    signatureWaivedReason = signatureWaivedReason,
    signature = signature
)
