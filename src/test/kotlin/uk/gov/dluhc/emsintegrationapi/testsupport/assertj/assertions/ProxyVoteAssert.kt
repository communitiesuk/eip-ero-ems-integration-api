package uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions

import org.apache.commons.codec.binary.Base64
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import uk.gov.dluhc.emsintegrationapi.database.entity.Address
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicantDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteDetails
import uk.gov.dluhc.emsintegrationapi.mapper.InstantMapper
import uk.gov.dluhc.emsintegrationapi.models.ProxyVote
import uk.gov.dluhc.emsintegrationapi.testsupport.haveSameValues
import uk.gov.dluhc.emsintegrationapi.models.Address as AddressModel

class ProxyVoteAssert(actual: ProxyVote) :
    AbstractAssert<ProxyVoteAssert, ProxyVote>(actual, ProxyVoteAssert::class.java) {
    private val instantMapper = InstantMapper()

    companion object {
        private val ADDRESS_ENTITY_FIELDS =
            arrayOf("property", "street", "postcode", "area", "town", "locality", "uprn")

        private val PROXY_VOTE_FIELDS = arrayOf(
            "proxyVoteUntilFurtherNotice",
            "proxyVoteForSingleDate",
            "proxyVoteStartDate",
            "proxyVoteEndDate",
            "proxyreason",
            "proxyfamilyrelationship",
        )
        private val PROXY_VOTE_ENTITY_FIELDS = arrayOf(
            "voteUntilFurtherNotice",
            "voteForSingleDate",
            "voteStartDate",
            "voteEndDate",
            "proxyReason",
            "proxyFamilyRelationship"
        )

        private val APPLICANT_FIELDS =
            arrayOf("refNum", "ip", "emsElectorId", "fn", "ln", "mn", "dob", "phone", "email")
        private val APPLICANT_ENTITY_FIELDS = arrayOf(
            "referenceNumber",
            "ipAddress",
            "emsElectorId",
            "firstName",
            "surname",
            "middleNames",
            "dob",
            "phone",
            "email"
        )

        private val APPLICATION_DETAILS_FIELDS_WITHOUT_DATE_FIELDS =
            arrayOf("gssCode", "source", "authorisingStaffId")

        fun assertThat(actual: ProxyVote) = ProxyVoteAssert(actual)
    }

    fun hasCorrectFieldsFromProxyApplication(proxyVoteApplication: ProxyVoteApplication) = validate {
        isNotNull
        with(proxyVoteApplication) {
            Assertions.assertThat(actual.id).isEqualTo(this.applicationId)
            hasProxyDetails(this.proxyVoteDetails)
            hasApplicationDetails(this)
            hasCorrectRegisteredAddress(
                actual.detail.registeredAddress,
                applicantDetails.registeredAddress
            )
            hasApplicantDetails(applicantDetails)
        }
    }

    fun hasRejectedReasons(proxyVoteApplication: ProxyVoteApplication) {
        with(actual.detail.rejectedReasons) {
            Assertions.assertThat(this?.englishReason?.notes).isEqualTo(proxyVoteApplication.englishRejectionNotes)
            Assertions.assertThat(this?.englishReason?.reasons).isEqualTo(proxyVoteApplication.englishRejectionReasons?.toList())
            Assertions.assertThat(this?.welshReason?.notes).isEqualTo(proxyVoteApplication.welshRejectionNotes)
            Assertions.assertThat(this?.welshReason?.reasons).isEqualTo(proxyVoteApplication.welshRejectionReasons?.toList())
        }
    }

    fun hasNoRejectedReasons() {
        with(actual.detail.rejectedReasons) {
            Assertions.assertThat(this?.englishReason?.notes).isNull()
            Assertions.assertThat(this?.englishReason?.reasons).isEmpty()
            Assertions.assertThat(this?.welshReason?.notes).isNull()
            Assertions.assertThat(this?.welshReason?.reasons).isEmpty()
        }
    }

    private fun hasApplicationDetails(proxyVoteApplication: ProxyVoteApplication) =
        validate {
            with(proxyVoteApplication.applicationDetails) {
                haveSameValues(
                    actual,
                    APPLICATION_DETAILS_FIELDS_WITHOUT_DATE_FIELDS,
                    this
                )
                Assertions.assertThat(actual.createdAt)
                    .isEqualTo(instantMapper.toOffsetDateTime(this.createdAt))
                Assertions.assertThat(actual.authorisedAt)
                    .isEqualTo(instantMapper.toOffsetDateTime(this.authorisedAt))
            }
        }

    private fun hasApplicantDetails(applicantDetails: ApplicantDetails) = validate {
        haveSameValues(actual.detail, APPLICANT_FIELDS, applicantDetails, APPLICANT_ENTITY_FIELDS)
        Assertions.assertThat(actual.detail.lang.name).isEqualTo(applicantDetails.language.name)
    }

    private fun hasProxyDetails(proxyDetails: ProxyVoteDetails) = validate {
        haveSameValues(actual.detail, PROXY_VOTE_FIELDS, proxyDetails, PROXY_VOTE_ENTITY_FIELDS)
        haveSameValues(actual.detail.proxyAddress, ADDRESS_ENTITY_FIELDS, proxyDetails.proxyAddress)
    }

    fun hasSignature(base64Signature: String) =
        validate { Assertions.assertThat(actual.detail.signature).isEqualTo(Base64.decodeBase64(base64Signature)) }

    fun signatureWaived() = validate {
        Assertions.assertThat(actual.detail.signatureWaived).isTrue
    }

    fun hasSignatureWaiverReason(waiverReason: String) = validate {
        Assertions.assertThat(actual.detail.signatureWaivedReason).isEqualTo(waiverReason)
    }

    fun hasNoSignature() =
        validate { Assertions.assertThat(actual.detail.signature).isNull() }

    fun hasNoSignatureWaiver() = validate {
        Assertions.assertThat(actual.detail.signatureWaived).isNull()
        Assertions.assertThat(actual.detail.signatureWaivedReason).isNull()
    }

    fun hasNoEmsElectorId() = validate { Assertions.assertThat(actual.detail.emsElectorId).isNull() }

    private fun hasCorrectRegisteredAddress(addressModel: AddressModel, addressEntity: Address?) =
        validate { haveSameValues(addressModel, ADDRESS_ENTITY_FIELDS, addressEntity) }

    private fun validate(validationFunction: () -> Unit): ProxyVoteAssert {
        validationFunction()
        return this
    }
}
