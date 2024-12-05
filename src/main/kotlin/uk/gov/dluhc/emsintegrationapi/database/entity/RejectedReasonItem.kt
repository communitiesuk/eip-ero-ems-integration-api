package uk.gov.dluhc.emsintegrationapi.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.Size

@Embeddable
class RejectedReasonItem(
    @field:Size(max = 100)
    val electorReason: String?,

    @Column(name = "rejection_reasons", length = 100)
    val type: String?,

    val includeInComms: Boolean? = true,
) {
    override fun hashCode() = javaClass.hashCode()
}
