package uk.gov.dluhc.emsintegrationapi.database.entity

import java.time.Instant
import javax.persistence.Embeddable
import javax.validation.constraints.Size

@Embeddable
class ApprovalDetails(
    val createdAt: Instant,

    @field:Size(max = 9)
    val gssCode: String,

    val authorisedAt: Instant,

    @field:Size(max = 255)
    val authorisingStaffId: String,

    @field:Size(max = 50)
    val source: String
) {
    override fun hashCode() = javaClass.hashCode()
}
