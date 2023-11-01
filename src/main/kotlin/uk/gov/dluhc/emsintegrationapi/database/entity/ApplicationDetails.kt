package uk.gov.dluhc.emsintegrationapi.database.entity

import java.time.Instant
import javax.persistence.Embeddable
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.Size

@Embeddable
class ApplicationDetails(
    val createdAt: Instant,

    @field:Size(max = 9)
    val gssCode: String,

    val authorisedAt: Instant,

    @field:Size(max = 255)
    val authorisingStaffId: String,

    @field:Size(max = 50)
    val source: String,

    @Enumerated(EnumType.STRING)
    val applicationStatus: ApplicationStatus,

    val signatureBase64: String?,

    val signatureWaived: Boolean?,

    @field:Size(max = 500)
    val signatureWaivedReason: String?,

    var emsStatus: EmsStatus?,

    var emsMessage: String?,

    var emsDetails: String?
) {
    override fun hashCode() = javaClass.hashCode()

    enum class ApplicationStatus {
        APPROVED,
        REJECTED
    }

    enum class EmsStatus {
        SUCCESS,
        FAILURE
    }
}
