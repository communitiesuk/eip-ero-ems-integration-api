package uk.gov.dluhc.emsintegrationapi.database.entity

import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.validation.constraints.Size
import java.time.Instant

@Embeddable
class ApplicationDetails(
    val createdAt: Instant?,

    @field:Size(max = 9)
    val gssCode: String,

    val authorisedAt: Instant?,

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

    @Enumerated(EnumType.STRING)
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
