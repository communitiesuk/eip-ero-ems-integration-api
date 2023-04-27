package uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions

import org.apache.commons.codec.binary.Base64
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import uk.gov.dluhc.emsintegrationapi.database.entity.Address
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicantDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteDetails
import uk.gov.dluhc.emsintegrationapi.mapper.InstantMapper
import uk.gov.dluhc.emsintegrationapi.models.PostalVote
import uk.gov.dluhc.emsintegrationapi.testsupport.haveNullValues
import uk.gov.dluhc.emsintegrationapi.testsupport.haveSameValues

class PostalVoteAssert(private val actual: PostalVote) :
    AbstractAssert<PostalVoteAssert, PostalVote>(actual, PostalVoteAssert::class.java) {
    private val instantMapper = InstantMapper()
    companion object {
        private val BALLOT_ADDRESS_FIELDS = arrayOf(
            "ballotproperty",
            "ballotstreet",
            "ballotpostcode",
            "ballotarea",
            "ballottown",
            "ballotlocality",
            "ballotuprn"
        )

        private val ADDRESS_ENTITY_FIELDS =
            arrayOf("property", "street", "postcode", "area", "town", "locality", "uprn")

        private val POSTAL_VOTE_FIELDS = arrayOf(
            "postalVoteUntilFurtherNotice",
            "postalVoteForSingleDate",
            "postalVoteStartDate",
            "postalVoteEndDate",
            "ballotAddressReason"
        )
        private val POSTAL_VOTE_ENTITY_FIELDS = arrayOf(
            "voteUntilFurtherNotice",
            "voteForSingleDate",
            "voteStartDate",
            "voteEndDate",
            "ballotAddressReason"
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

        fun assertThat(actual: PostalVote) = PostalVoteAssert(actual)
    }

    fun hasCorrectFieldsFromPostalApplication(postalVoteApplication: PostalVoteApplication) = validate {
        isNotNull
        with(postalVoteApplication) {
            Assertions.assertThat(actual.id).isEqualTo(this.applicationId)
            hasApplicationDetails(this)
            hasCorrectAddressFields(
                applicantDetails.registeredAddress,
                REGISTERED_ADDRESS_FIELDS
            )
            hasApplicantDetails(applicantDetails)
        }
    }

    private fun hasApplicationDetails(postalVoteApplication: PostalVoteApplication) =
        validate {
            with(postalVoteApplication.applicationDetails) {
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

    fun hasPostalVoteDetails(postalVoteDetails: PostalVoteDetails?) = validate {
        isNotNull
        haveSameValues(actual.detail, POSTAL_VOTE_FIELDS, postalVoteDetails, POSTAL_VOTE_ENTITY_FIELDS)
    }

    fun hasBallotAddress(ballotAddress: Address) = validate {
        isNotNull
        haveSameValues(
            actual.detail,
            BALLOT_ADDRESS_FIELDS,
            ballotAddress,
            ADDRESS_ENTITY_FIELDS
        )
    }

    fun doesNotHaveBallotAddress() = validate { haveNullValues(actual.detail, *BALLOT_ADDRESS_FIELDS) }

    fun doesNotHavePostalVoteDetails() = validate { haveNullValues(actual.detail, *POSTAL_VOTE_FIELDS) }

    fun hasSignature(base64Signature: String) =
        validate { Assertions.assertThat(actual.detail.signature).isEqualTo(Base64.decodeBase64(base64Signature)) }

    fun hasSignatureWaiver(waiverReason: String) = validate {
        Assertions.assertThat(actual.detail.signatureWaived).isTrue
        Assertions.assertThat(actual.detail.signatureWaivedReason).isEqualTo(waiverReason)
    }

    private fun hasCorrectAddressFields(address: Address?, addressFields: Array<String>) =
        validate { haveSameValues(actual.detail, addressFields, address, ADDRESS_ENTITY_FIELDS) }

    private fun validate(validationFunction: () -> Unit): PostalVoteAssert {
        validationFunction()
        return this
    }
}
