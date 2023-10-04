package uk.gov.dluhc.emsintegrationapi.mapper

import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.RetentionStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.database.entity.Type
import uk.gov.dluhc.emsintegrationapi.messaging.models.ProxyVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.messaging.models.RejectedReasonItem
import uk.gov.dluhc.emsintegrationapi.database.entity.RejectedReasonItem as RejectedReasonItemEntity

@Component
class ProxyVoteApplicationMessageMapper(
    private val applicantDetailsMapper: ApplicantDetailsMapper,
    private val applicationDetailsMapper: ApplicationDetailsMapper,
    private val proxyVoteDetailsMapper: ProxyVoteDetailsMapper
) {

    fun mapToEntity(proxyVoteApplicationMessage: ProxyVoteApplicationMessage) =
        proxyVoteApplicationMessage.let {
            ProxyVoteApplication(
                applicationId = it.applicationDetails.id,
                applicationDetails = applicationDetailsMapper.mapToApplicationDetails(it.applicationDetails),
                applicantDetails = applicantDetailsMapper.mapToApplicantEntity(
                    it.applicantDetails,
                    SourceSystem.PROXY
                ),
                proxyVoteDetails = proxyVoteDetailsMapper.mapToProxyVoteDetailsEntity(it.proxyVoteDetails),
                createdBy = SourceSystem.PROXY,
                retentionStatus = RetentionStatus.RETAIN,
                status = RecordStatus.RECEIVED,
                englishRejectionNotes = it.proxyVoteDetails.rejectedReasons?.englishReason?.notes,
                englishRejectedReasonItems = it.proxyVoteDetails.rejectedReasons?.englishReason?.reasonList?.map { mapRejectedReasonItemToEntity(it) }?.toSet(),
                welshRejectionNotes = it.proxyVoteDetails.rejectedReasons?.welshReason?.notes,
                welshRejectedReasonItems = it.proxyVoteDetails.rejectedReasons?.englishReason?.reasonList?.map { mapRejectedReasonItemToEntity(it) }?.toSet(),
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
                    RejectedReasonItem.Type.PROXY_MINUS_LIMITS -> Type.PROXY_LIMITS
                    RejectedReasonItem.Type.PROXY_MINUS_NOT_MINUS_REGISTERED_MINUS_TO_MINUS_VOTE -> Type.PROXY_NOT_REGISTERED_TO_VOTE
                    else -> Type.OTHER_REJECT_REASON
                },
                includeInComms = includeInComms
            )
        }
    }
}
