package uk.gov.dluhc.emsintegrationapi.mapper

import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.RetentionStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.messaging.models.ProxyVoteApplicationMessage

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
                englishRejectionReasons = it.proxyVoteDetails.rejectedReasons?.englishReason?.reasons?.toSet(),
                welshRejectionNotes = it.proxyVoteDetails.rejectedReasons?.welshReason?.notes,
                welshRejectionReasons = it.proxyVoteDetails.rejectedReasons?.welshReason?.reasons?.toSet()
            )
        }
}
