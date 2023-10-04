package uk.gov.dluhc.emsintegrationapi.database.entity

import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.Size

@Embeddable
class RejectedReasonItem(
    @field:Size(max = 100)
    val electorReason: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "rejection_reasons", length = 100)
    val type: Type,

    val includeInComms: Boolean? = true,
) {
    override fun hashCode() = javaClass.hashCode()
}

/**
 * Enum indicating if an application is approved or rejected.
 * Values:
 * IDENTITY_NOT_CONFIRMED
 * SIGNATURE_IS_NOT_ACCEPTABLE
 * FRAUDULENT_APPLICATION
 * NOT_REGISTERED_TO_VOTE
 * NOT_ELIGIBLE_FOR_RESERVED_POLLS
 * DOB_NOT_PROVIDED
 * INCOMPLETE_APPLICATION
 * PROXY_NOT_REGISTERED_TO_VOTE
 * PROXY_LIMITS
 * OTHER_REJECT_REASON
 */
enum class Type {

    IDENTITY_NOT_CONFIRMED,
    SIGNATURE_IS_NOT_ACCEPTABLE,
    FRAUDULENT_APPLICATION,
    NOT_REGISTERED_TO_VOTE,
    NOT_ELIGIBLE_FOR_RESERVED_POLLS,
    DOB_NOT_PROVIDED,
    INCOMPLETE_APPLICATION,
    PROXY_NOT_REGISTERED_TO_VOTE,
    PROXY_LIMITS,
    OTHER_REJECT_REASON
}
