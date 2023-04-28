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

class ProxyVoteAssert(actual: ProxyVote) :
    AbstractAssert<ProxyVoteAssert, ProxyVote>(actual, ProxyVoteAssert::class.java) {
    private val instantMapper = InstantMapper()

    companion object {
        private val PROXY_ADDRESS_FIELDS = arrayOf(
            "proxyproperty",
            "proxystreet",
            "proxypostcode",
            "proxyarea",
            "proxytown",
            "proxylocality",
            "proxyuprn"
        )

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
        private val REGISTERED_ADDRESS_FIELDS =
            arrayOf("regproperty", "regstreet", "regpostcode", "regarea", "regtown", "reglocality", "reguprn")

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
                applicantDetails.registeredAddress,
                REGISTERED_ADDRESS_FIELDS
            )
            hasApplicantDetails(applicantDetails)
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
        haveSameValues(
            actual.detail,
            PROXY_ADDRESS_FIELDS,
            proxyDetails.proxyAddress,
            ADDRESS_ENTITY_FIELDS
        )
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

    private fun hasCorrectRegisteredAddress(address: Address?, addressFields: Array<String>) =
        validate { haveSameValues(actual.detail, addressFields, address, ADDRESS_ENTITY_FIELDS) }

    private fun validate(validationFunction: () -> Unit): ProxyVoteAssert {
        validationFunction()
        return this
    }
}
