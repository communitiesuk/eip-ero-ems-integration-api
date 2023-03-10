package uk.gov.dluhc.emsintegrationapi.database.entity

import java.time.Instant
import javax.persistence.Embeddable
import javax.validation.constraints.NotNull

@Embeddable
class ApprovalDetails(
    @field:NotNull
    val createdAt: Instant,
    @field:NotNull
    val gssCode: String,
    @field:NotNull
    val authorisedAt: Instant,
    @field:NotNull
    val authorisingStaffId: String,
    @field:NotNull
    val source: String
) {
    override fun hashCode() = javaClass.hashCode()
}
