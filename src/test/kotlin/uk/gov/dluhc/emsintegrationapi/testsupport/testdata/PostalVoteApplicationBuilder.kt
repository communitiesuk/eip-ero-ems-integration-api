package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicantDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicationDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplicationPrimaryElectorDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.RejectedReasonItem
import uk.gov.dluhc.emsintegrationapi.database.entity.RetentionStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteApplicationMessage
import java.time.Instant
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApplicantDetails as ApplicantDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApplicationDetails as ApplicationDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteDetails as PostalVoteDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.messaging.models.PrimaryElectorDetails as PrimaryElectorDetailsMessageDto

fun buildPostalVoteApplication(
    applicationId: String = getRandomAlphaNumeric(24),
    applicationDetails: ApplicationDetails = buildApplicationDetailsEntity(),
    applicantDetails: ApplicantDetails = buildApplicantDetailsEntity(),
    postalVoteDetails: PostalVoteDetails? = buildPostalVoteDetailsEntity(),
    primaryElectorDetails: PostalVoteApplicationPrimaryElectorDetails? = null,
    removalDateTime: Instant? = null,
    retentionStatus: RetentionStatus = RetentionStatus.RETAIN,
    createdBy: SourceSystem = SourceSystem.POSTAL,
    dateUpdated: Instant? = null,
    updatedBy: SourceSystem? = null,
    recordStatus: RecordStatus = RecordStatus.RECEIVED,
    englishRejectionNotes: String? = null,
    englishRejectedReasonItems: Set<RejectedReasonItem>? = emptySet(),
    welshRejectionNotes: String? = null,
    welshRejectedReasonItems: Set<RejectedReasonItem>? = emptySet()
) = PostalVoteApplication(
    applicationId = applicationId,
    applicationDetails = applicationDetails,
    applicantDetails = applicantDetails,
    postalVoteDetails = postalVoteDetails,
    primaryElectorDetails = primaryElectorDetails,
    removalDateTime = removalDateTime,
    retentionStatus = retentionStatus,
    createdBy = createdBy,
    dateUpdated = dateUpdated,
    updatedBy = updatedBy,
    status = recordStatus,
    englishRejectionNotes = englishRejectionNotes,
    englishRejectedReasonItems = englishRejectedReasonItems,
    welshRejectionNotes = welshRejectionNotes,
    welshRejectedReasonItems = welshRejectedReasonItems
)

fun buildPostalVoteApplicationMessage(
    applicationDetails: ApplicationDetailsMessageDto = buildApplicationDetailsMessageDto(),
    applicantDetails: ApplicantDetailsMessageDto = buildApplicantDetailsMessageDto(),
    postalVoteDetails: PostalVoteDetailsMessageDto = buildPostalVoteDetailsMessageDto(),
    primaryElectorDetails: PrimaryElectorDetailsMessageDto? = null,
) = PostalVoteApplicationMessage(
    applicationDetails = applicationDetails,
    applicantDetails = applicantDetails,
    postalVoteDetails = postalVoteDetails,
    primaryElectorDetails = primaryElectorDetails,
)
