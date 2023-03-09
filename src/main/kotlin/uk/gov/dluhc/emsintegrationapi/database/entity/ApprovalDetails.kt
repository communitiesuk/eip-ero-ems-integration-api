package uk.gov.dluhc.emsintegrationapi.database.entity

import java.time.Instant
import javax.persistence.Embeddable
import javax.validation.constraints.NotNull

@Embeddable
class ApprovalDetails(
    @NotNull
    val createdAt: Instant,
    @NotNull
    val gssCode: String,
    @NotNull
    val authorisedAt: Instant,
    @NotNull
    val authorisingStaffId: String,
    @NotNull
    val source: String
) {
    override fun hashCode() = javaClass.hashCode()
}
