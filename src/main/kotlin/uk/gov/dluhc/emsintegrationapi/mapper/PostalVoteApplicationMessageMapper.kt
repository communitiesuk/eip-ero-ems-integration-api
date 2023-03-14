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
    private val approvalDetailsMapper: ApprovalDetailsMapper,
    private val postalVoteDetailsMapper: PostalVoteDetailsMapper
) {

    fun mapToEntity(postalVoteApplicationMessage: PostalVoteApplicationMessage?) =
        postalVoteApplicationMessage?.let {
            PostalVoteApplication(
                applicationId = it.approvalDetails.id,
                approvalDetails = approvalDetailsMapper.mapToApprovalDetails(it.approvalDetails)!!,
                applicantDetails = applicantDetailsMapper.mapToApplicantEntity(it.applicantDetails)!!,
                postalVoteDetails = postalVoteDetailsMapper.mapToPostVoteDetailsEntity(it.postalVoteDetails),
                signatureBase64 = postalVoteApplicationMessage.signatureBase64,
                createdBy = SourceSystem.POSTAL,
                retentionStatus = RetentionStatus.RETAIN,
                status = RecordStatus.RECEIVED
            )
        }
}
