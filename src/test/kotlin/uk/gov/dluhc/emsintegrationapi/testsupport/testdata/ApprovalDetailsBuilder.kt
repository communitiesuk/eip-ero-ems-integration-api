package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import org.apache.commons.lang3.RandomStringUtils
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicantDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.ApprovalDetails
import java.time.Instant

fun buildApprovalDetailsEntity(
    createdAt: Instant = getPastDateTime(),
    gssCode: String = getRandomGssCode(),
    authorisedAt: Instant = getPastDateTime(5),
    authorisingStaffId: String = getRandomEmailAddress(),
    source: String = RandomStringUtils.random(5),
    applicantDetails: ApplicantDetails = buildApplicantDetailsEntity()
) = ApprovalDetails(
    createdAt = createdAt,
    gssCode = gssCode,
    authorisedAt = authorisedAt,
    authorisingStaffId = authorisingStaffId,
    source = source
)
