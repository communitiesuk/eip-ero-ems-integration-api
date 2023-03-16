package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicantDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.ApprovalDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.RetentionStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.messaging.models.ProxyVoteApplicationMessage
import java.time.Instant
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApplicantDetails as ApplicantDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApprovalDetails as ApprovalDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.messaging.models.ProxyVoteDetails as ProxyVoteDetailsMessageDto

fun buildProxyVoteApplication(
    applicationId: String = getRandomAlphaNumeric(24),
    approvalDetails: ApprovalDetails = buildApprovalDetailsEntity(),
    applicantDetails: ApplicantDetails = buildApplicantDetailsEntity(),
    proxyVoteDetails: ProxyVoteDetails = buildProxyVoteDetailsEntity(),
    signatureBase64: String = getRandomAlphaNumeric(20),
    removalDateTime: Instant? = null,
    retentionStatus: RetentionStatus = RetentionStatus.RETAIN,
    createdBy: SourceSystem = SourceSystem.POSTAL,
    dateUpdated: Instant? = null,
    updatedBy: SourceSystem? = null,
    recordStatus: RecordStatus = RecordStatus.RECEIVED
) = ProxyVoteApplication(
    applicationId = applicationId,
    approvalDetails = approvalDetails,
    applicantDetails = applicantDetails,
    proxyVoteDetails = proxyVoteDetails,
    signatureBase64 = signatureBase64,
    removalDateTime = removalDateTime,
    retentionStatus = retentionStatus,
    createdBy = createdBy,
    dateUpdated = dateUpdated,
    updatedBy = updatedBy,
    status = recordStatus
)

fun buildProxyVoteApplicationMessageDto(
    applicationId: String = getRandomAlphaNumeric(24),
    approvalDetails: ApprovalDetailsMessageDto = buildApprovalDetailsMessageDto(applicationId = applicationId),
    applicantDetails: ApplicantDetailsMessageDto = buildApplicantDetailsMessageDto(),
    proxyVoteDetails: ProxyVoteDetailsMessageDto = buildProxyVoteDetailsMessageDto(),
    signatureBase64: String = getRandomAlphaNumeric(20),
) = ProxyVoteApplicationMessage(
    approvalDetails = approvalDetails,
    applicantDetails = applicantDetails,
    proxyVoteDetails = proxyVoteDetails,
    signatureBase64 = signatureBase64,
)
