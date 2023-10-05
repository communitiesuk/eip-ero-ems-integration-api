package uk.gov.dluhc.emsintegrationapi.database.entity

import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.Size

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
