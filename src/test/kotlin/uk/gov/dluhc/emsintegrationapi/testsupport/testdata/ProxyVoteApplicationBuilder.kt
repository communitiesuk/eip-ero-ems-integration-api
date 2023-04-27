package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicantDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicationDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.RetentionStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.messaging.models.ProxyVoteApplicationMessage
import java.time.Instant
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApplicantDetails as ApplicantDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApplicationDetails as ApplicationDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.messaging.models.ProxyVoteDetails as ProxyVoteDetailsMessageDto

fun buildProxyVoteApplication(
    applicationId: String = getIerDsApplicationId(),
    applicationDetails: ApplicationDetails = buildApplicationDetailsEntity(),
    applicantDetails: ApplicantDetails = buildApplicantDetailsEntity(),
    proxyVoteDetails: ProxyVoteDetails = buildProxyVoteDetailsEntity(),
    removalDateTime: Instant? = null,
    retentionStatus: RetentionStatus = RetentionStatus.RETAIN,
    createdBy: SourceSystem = SourceSystem.POSTAL,
    dateUpdated: Instant? = null,
    updatedBy: SourceSystem? = null,
    recordStatus: RecordStatus = RecordStatus.RECEIVED
) = ProxyVoteApplication(
    applicationId = applicationId,
    applicationDetails = applicationDetails,
    applicantDetails = applicantDetails,
    proxyVoteDetails = proxyVoteDetails,
    removalDateTime = removalDateTime,
    retentionStatus = retentionStatus,
    createdBy = createdBy,
    dateUpdated = dateUpdated,
    updatedBy = updatedBy,
    status = recordStatus
)

fun buildProxyVoteApplicationMessageDto(
    applicationDetails: ApplicationDetailsMessageDto = buildApplicationDetailsMessageDto(),
    applicantDetails: ApplicantDetailsMessageDto = buildApplicantDetailsMessageDto(),
    proxyVoteDetails: ProxyVoteDetailsMessageDto = buildProxyVoteDetailsMessageDto(),
) = ProxyVoteApplicationMessage(
    applicationDetails = applicationDetails,
    applicantDetails = applicantDetails,
    proxyVoteDetails = proxyVoteDetails,
)
