package uk.gov.dluhc.emsintegrationapi.mapper

import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApplicationDetails
import uk.gov.dluhc.emsintegrationapi.util.toBase64String
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicationDetails as ApprovalDetailsEntity

@Component
class ApplicationDetailsMapper(private val instantMapper: InstantMapper) {

    fun mapToApplicationDetails(applicationDetails: ApplicationDetails) = applicationDetails.let {
        ApprovalDetailsEntity(
            createdAt = instantMapper.toInstant(it.createdAt),
            authorisingStaffId = it.authorisingStaffId,
            authorisedAt = instantMapper.toInstant(it.authorisedAt),
            source = it.source,
            gssCode = it.gssCode,
            signatureBase64 = applicationDetails.signature?.toBase64String(),
            signatureWaived = it.signatureWaived,
            signatureWaivedReason = it.signatureWaivedReason,
            applicationStatus = ApprovalDetailsEntity.ApplicationStatus.valueOf(it.applicationStatus.name)
        )
    }
}
