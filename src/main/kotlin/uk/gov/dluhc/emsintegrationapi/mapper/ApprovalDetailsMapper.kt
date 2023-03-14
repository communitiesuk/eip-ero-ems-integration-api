package uk.gov.dluhc.emsintegrationapi.mapper

import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApprovalDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.ApprovalDetails as ApprovalDetailsEntity

@Component
class ApprovalDetailsMapper(private val instantMapper: InstantMapper) {

    fun mapToApprovalDetails(approvalDetails: ApprovalDetails?) = approvalDetails?.let {
        ApprovalDetailsEntity(
            createdAt = instantMapper.toInstant(it.createdAt)!!,
            authorisingStaffId = it.authorisingStaffId,
            authorisedAt = instantMapper.toInstant(it.authorisedAt)!!,
            source = it.source,
            gssCode = it.gssCode
        )
    }
}
