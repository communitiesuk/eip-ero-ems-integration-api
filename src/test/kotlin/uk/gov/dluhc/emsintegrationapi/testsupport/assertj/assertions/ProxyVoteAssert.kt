package uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import uk.gov.dluhc.emsintegrationapi.database.entity.Address
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicantDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteDetails
import uk.gov.dluhc.emsintegrationapi.mapper.InstantMapper
import uk.gov.dluhc.emsintegrationapi.models.ProxyVote
import uk.gov.dluhc.emsintegrationapi.testsupport.haveSameValues

class ProxyVoteAssert(private val actual: ProxyVote) :
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
            "proxyreason",
            "proxyreason",
            "proxyreason"
        )
        private val PROXY_VOTE_ENTITY_FIELDS = arrayOf(
            "voteUntilFurtherNotice",
            "voteForSingleDate",
            "voteStartDate",
            "voteEndDate",
            "proxyReason"
        )
        private val REGISTERED_ADDRESS_FIELDS =
            arrayOf("regproperty", "regstreet", "regpostcode", "regarea", "regtown", "reglocality", "reguprn")

        private val APPLICANT_FIELDS =
            arrayOf("refNum", "ip", "lang", "emsElectorId", "fn", "ln", "mn", "dob", "phone", "email")
        private val APPLICANT_ENTITY_FIELDS = arrayOf(
            "referenceNumber",
            "ipAddress",
            "language",
            "emsElectorId",
            "firstName",
            "surname",
            "middleNames",
            "dob",
            "phone",
            "email"
        )

        private val APPROVAL_FIELDS_WITHOUT_DATE_FIELDS =
            arrayOf("gssCode", "source", "authorisingStaffId")

        fun assertThat(actual: ProxyVote) = ProxyVoteAssert(actual)
    }

    fun hasCorrectFieldsFromProxyApplication(proxyVoteApplication: ProxyVoteApplication) = validate {
        isNotNull
        with(proxyVoteApplication) {
            Assertions.assertThat(actual.id).isEqualTo(this.applicationId)
            hasProxyDetails(this.proxyVoteDetails)
            hasApprovalDetails(this)
            hasCorrectAddressFields(
                applicantDetails.registeredAddress,
                REGISTERED_ADDRESS_FIELDS
            )
            hasApplicantDetails(applicantDetails)
        }
    }

    private fun hasApprovalDetails(proxyVoteApplication: ProxyVoteApplication) =
        validate {
            with(proxyVoteApplication.approvalDetails) {
                haveSameValues(
                    actual,
                    APPROVAL_FIELDS_WITHOUT_DATE_FIELDS,
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
    }

    fun hasPostalVoteDetails(postalVoteDetails: PostalVoteDetails?) = validate {

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

    private fun hasCorrectAddressFields(address: Address?, addressFields: Array<String>) =
        validate { haveSameValues(actual.detail, addressFields, address, ADDRESS_ENTITY_FIELDS) }

    private fun validate(validationFunction: () -> Unit): ProxyVoteAssert {
        validationFunction()
        return this
    }
}
