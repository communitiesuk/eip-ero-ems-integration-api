package uk.gov.dluhc.emsintegrationapi.mapper

import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.RetentionStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.database.entity.Type
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.messaging.models.RejectedReasonItem
import uk.gov.dluhc.emsintegrationapi.database.entity.RejectedReasonItem as RejectedReasonItemEntity

@Component
class PostalVoteApplicationMessageMapper(
    private val applicantDetailsMapper: ApplicantDetailsMapper,
    private val applicationDetailsMapper: ApplicationDetailsMapper,
    private val postalVoteDetailsMapper: PostalVoteDetailsMapper,
    private val primaryElectorDetailsMapper: PrimaryElectorDetailsMapper,
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
                primaryElectorDetails = primaryElectorDetailsMapper.mapToEntity(
                    it.applicationDetails.id,
                    it.primaryElectorDetails,
                ),
                createdBy = SourceSystem.POSTAL,
                retentionStatus = RetentionStatus.RETAIN,
                status = RecordStatus.RECEIVED,
                englishRejectionNotes = it.postalVoteDetails?.rejectedReasons?.englishReason?.notes,
                englishRejectedReasonItems = it.postalVoteDetails?.rejectedReasons?.englishReason?.reasonList?.map { mapRejectedReasonItemToEntity(it) }?.toSet(),
                welshRejectionNotes = it.postalVoteDetails?.rejectedReasons?.welshReason?.notes,
                welshRejectedReasonItems = it.postalVoteDetails?.rejectedReasons?.englishReason?.reasonList?.map { mapRejectedReasonItemToEntity(it) }?.toSet(),
            )
        }

    fun mapRejectedReasonItemToEntity(rejectedReasonItem: RejectedReasonItem): RejectedReasonItemEntity {
        return with(rejectedReasonItem) {
            RejectedReasonItemEntity(
                electorReason = electorReason,
                type = when (type) {
                    RejectedReasonItem.Type.IDENTITY_MINUS_NOT_MINUS_CONFIRMED -> Type.IDENTITY_NOT_CONFIRMED
                    RejectedReasonItem.Type.SIGNATURE_MINUS_IS_MINUS_NOT_MINUS_ACCEPTABLE -> Type.SIGNATURE_IS_NOT_ACCEPTABLE
                    RejectedReasonItem.Type.DOB_MINUS_NOT_MINUS_PROVIDED -> Type.DOB_NOT_PROVIDED
                    RejectedReasonItem.Type.FRAUDULENT_MINUS_APPLICATION -> Type.FRAUDULENT_APPLICATION
                    RejectedReasonItem.Type.NOT_MINUS_REGISTERED_MINUS_TO_MINUS_VOTE -> Type.NOT_REGISTERED_TO_VOTE
                    RejectedReasonItem.Type.NOT_MINUS_ELIGIBLE_MINUS_FOR_MINUS_RESERVED_MINUS_POLLS -> Type.NOT_ELIGIBLE_FOR_RESERVED_POLLS
                    RejectedReasonItem.Type.INCOMPLETE_MINUS_APPLICATION -> Type.INCOMPLETE_APPLICATION
                    else -> Type.OTHER_REJECT_REASON
                },
                includeInComms = includeInComms
            )
        }
    }
}
