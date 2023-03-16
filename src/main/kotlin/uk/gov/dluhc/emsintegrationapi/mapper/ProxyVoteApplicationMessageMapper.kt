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
    private val approvalDetailsMapper: ApprovalDetailsMapper,
    private val proxyVoteDetailsMapper: ProxyVoteDetailsMapper
) {

    fun mapToEntity(proxyVoteApplicationMessage: ProxyVoteApplicationMessage?) =
        proxyVoteApplicationMessage?.let {
            ProxyVoteApplication(
                applicationId = it.approvalDetails.id,
                approvalDetails = approvalDetailsMapper.mapToApprovalDetails(it.approvalDetails)!!,
                applicantDetails = applicantDetailsMapper.mapToApplicantEntity(it.applicantDetails)!!,
                proxyVoteDetails = proxyVoteDetailsMapper.mapToProxyVoteDetailsEntity(it.proxyVoteDetails)!!,
                signatureBase64 = it.signatureBase64,
                createdBy = SourceSystem.POSTAL,
                retentionStatus = RetentionStatus.RETAIN,
                status = RecordStatus.RECEIVED
            )
        }
}
