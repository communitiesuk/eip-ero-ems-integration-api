package uk.gov.dluhc.emsintegrationapi.mapper

import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.RetentionStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteApplicationMessage

@Component
class PostalVoteApplicationMessageMapper(
    private val applicantDetailsMapper: ApplicantDetailsMapper,
    private val applicationDetailsMapper: ApplicationDetailsMapper,
    private val postalVoteDetailsMapper: PostalVoteDetailsMapper
) {

    fun mapToEntity(postalVoteApplicationMessage: PostalVoteApplicationMessage) =
        postalVoteApplicationMessage.let {
            PostalVoteApplication(
                applicationId = it.applicationDetails.id,
                applicationDetails = applicationDetailsMapper.mapToApplicationDetails(it.applicationDetails),
                applicantDetails = applicantDetailsMapper.mapToApplicantEntity(
                    it.applicantDetails,
                    SourceSystem.POSTAL
                ),
                postalVoteDetails = postalVoteDetailsMapper.mapToPostVoteDetailsEntity(it.postalVoteDetails),
                createdBy = SourceSystem.POSTAL,
                retentionStatus = RetentionStatus.RETAIN,
                status = RecordStatus.RECEIVED,
                englishRejectionNotes = it.postalVoteDetails?.rejectedReasons?.englishReason?.notes,
                welshRejectionNotes = it.postalVoteDetails?.rejectedReasons?.welshReason?.notes,
            )
        }
}
