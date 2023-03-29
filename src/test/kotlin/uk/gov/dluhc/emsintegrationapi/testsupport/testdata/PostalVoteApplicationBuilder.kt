package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicantDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.ApprovalDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.RetentionStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteApplicationMessage
import java.time.Instant
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApplicantDetails as ApplicantDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApprovalDetails as ApprovalDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteDetails as PostalVoteDetailsMessageDto

fun buildPostalVoteApplication(
    applicationId: String = getRandomAlphaNumeric(24),
    approvalDetails: ApprovalDetails = buildApprovalDetailsEntity(),
    applicantDetails: ApplicantDetails = buildApplicantDetailsEntity(),
    postalVoteDetails: PostalVoteDetails = buildPostalVoteDetailsEntity(),
    signatureBase64: String = getRandomAlphaNumeric(20),
    removalDateTime: Instant? = null,
    retentionStatus: RetentionStatus = RetentionStatus.RETAIN,
    createdBy: SourceSystem = SourceSystem.POSTAL,
    dateUpdated: Instant? = null,
    updatedBy: SourceSystem? = null,
    recordStatus: RecordStatus = RecordStatus.RECEIVED
) = PostalVoteApplication(
    applicationId = applicationId,
    approvalDetails = approvalDetails,
    applicantDetails = applicantDetails,
    postalVoteDetails = postalVoteDetails,
    signatureBase64 = signatureBase64,
    removalDateTime = removalDateTime,
    retentionStatus = retentionStatus,
    createdBy = createdBy,
    dateUpdated = dateUpdated,
    updatedBy = updatedBy,
    status = recordStatus
)

fun buildPostalVoteApplicationMessage(
    applicationId: String = getIerDsApplicationId(),
    approvalDetails: ApprovalDetailsMessageDto = buildApprovalDetailsMessageDto(applicationId = applicationId),
    applicantDetails: ApplicantDetailsMessageDto = buildApplicantDetailsMessageDto(),
    postalVoteDetails: PostalVoteDetailsMessageDto = buildPostalVoteDetailsMessageDto(),
    signatureBase64: String = getRandomAlphaNumeric(20),
) = PostalVoteApplicationMessage(
    approvalDetails = approvalDetails,
    applicantDetails = applicantDetails,
    postalVoteDetails = postalVoteDetails,
    signatureBase64 = signatureBase64,
)
