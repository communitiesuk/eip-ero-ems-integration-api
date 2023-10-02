package uk.gov.dluhc.emsintegrationapi.database.entity

import com.fasterxml.jackson.annotation.JsonProperty
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
enum class Type(val value: kotlin.String) {

    @JsonProperty("identity-not-confirmed") IDENTITY_NOT_CONFIRMED("identity-not-confirmed"),
    @JsonProperty("signature-is-not-acceptable") SIGNATURE_IS_NOT_ACCEPTABLE("signature-is-not-acceptable"),
    @JsonProperty("fraudulent-application") FRAUDULENT_APPLICATION("fraudulent-application"),
    @JsonProperty("not-registered-to-vote") NOT_REGISTERED_TO_VOTE("not-registered-to-vote"),
    @JsonProperty("not-eligible-for-reserved-polls") NOT_ELIGIBLE_FOR_RESERVED_POLLS("not-eligible-for-reserved-polls"),
    @JsonProperty("dob-not-provided") DOB_NOT_PROVIDED("dob-not-provided"),
    @JsonProperty("incomplete-application") INCOMPLETE_APPLICATION("incomplete-application"),
    @JsonProperty("proxy-not-registered-to-vote") PROXY_NOT_REGISTERED_TO_VOTE("proxy-not-registered-to-vote"),
    @JsonProperty("proxy-limits") PROXY_LIMITS("proxy-limits"),
    @JsonProperty("other-reject-reason") OTHER_REJECT_REASON("other-reject-reason")
}
