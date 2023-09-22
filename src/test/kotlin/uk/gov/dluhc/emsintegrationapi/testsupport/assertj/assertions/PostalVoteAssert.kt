package uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions

import org.apache.commons.codec.binary.Base64
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import uk.gov.dluhc.emsintegrationapi.database.entity.Address
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicantDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.BfpoAddress
import uk.gov.dluhc.emsintegrationapi.database.entity.OverseasAddress
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplicationPrimaryElectorDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteDetails
import uk.gov.dluhc.emsintegrationapi.mapper.InstantMapper
import uk.gov.dluhc.emsintegrationapi.models.PostalVote
import uk.gov.dluhc.emsintegrationapi.testsupport.haveNullValues
import uk.gov.dluhc.emsintegrationapi.testsupport.haveSameValues
import uk.gov.dluhc.emsintegrationapi.models.Address as AddressModel

class PostalVoteAssert(actual: PostalVote) :
    AbstractAssert<PostalVoteAssert, PostalVote>(actual, PostalVoteAssert::class.java) {
    private val instantMapper = InstantMapper()

    companion object {
        private val BALLOT_BFPO_ADDRESS_FIELDS = arrayOf(
            "bfpoNumber",
            "addressLine1",
            "addressLine2",
            "addressLine3",
            "addressLine4",
            "addressLine5"
        )

        private val BALLOT_OVERSEAS_ADDRESS_FIELDS = arrayOf(
            "addressLine1",
            "addressLine2",
            "addressLine3",
            "addressLine4",
            "addressLine5",
            "country"
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

        private val APPLICANT_FIELDS_STANDARD =
            arrayOf("refNum", "ip", "emsElectorId", "fn", "ln", "mn", "dob", "phone", "email")
        private val APPLICANT_FIELDS_POSTAL_PROXY =
            arrayOf("refNum", "ip", "emsElectorId", "postalProxy.fn", "postalProxy.ln", "postalProxy.mn", "postalProxy.dob", "phone", "email")
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

        private val PRIMARY_ELECTOR_FIELDS = arrayOf("fn", "ln", "mn")
        private val PRIMARY_ELECTOR_FIELDS_ENTITY = arrayOf("firstName", "surname", "middleNames")

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
                actual.detail.registeredAddress,
                if (primaryElectorDetails == null) applicantDetails.registeredAddress else primaryElectorDetails!!.address,
            )
            hasApplicantDetails(applicantDetails, isPostalProxy = primaryElectorDetails != null)
            hasPrimaryElectorDetails(primaryElectorDetails)
            hasRejectedReasons(this)
        }
    }

    fun hasRejectedReasons(postalVoteApplication: PostalVoteApplication) {
        with(actual.detail.rejectedReasons) {
            Assertions.assertThat(this?.englishReason?.notes).isEqualTo(postalVoteApplication.englishRejectionNotes)
            Assertions.assertThat(this?.englishReason?.reasons).isEqualTo(postalVoteApplication.englishRejectionReasons?.toList())
            Assertions.assertThat(this?.englishReason?.reasonList).isEqualTo(postalVoteApplication.englishRejectedReasonItems?.toList())
            Assertions.assertThat(this?.welshReason?.notes).isEqualTo(postalVoteApplication.welshRejectionNotes)
            Assertions.assertThat(this?.welshReason?.reasons).isEqualTo(postalVoteApplication.welshRejectionReasons?.toList())
            Assertions.assertThat(this?.welshReason?.reasonList).isEqualTo(postalVoteApplication.welshRejectedReasonItems?.toList())
        }
    }

    fun hasNoRejectedReasons() {
        with(actual.detail.rejectedReasons) {
            Assertions.assertThat(this?.englishReason?.notes).isNull()
            Assertions.assertThat(this?.englishReason?.reasons).isEmpty()
            Assertions.assertThat(this?.englishReason?.reasonList).isEmpty()
            Assertions.assertThat(this?.welshReason?.notes).isNull()
            Assertions.assertThat(this?.welshReason?.reasons).isEmpty()
            Assertions.assertThat(this?.welshReason?.reasonList).isEmpty()
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

    private fun hasApplicantDetails(applicantDetails: ApplicantDetails, isPostalProxy: Boolean) = validate {
        haveSameValues(
            actual.detail,
            if (isPostalProxy) APPLICANT_FIELDS_POSTAL_PROXY else APPLICANT_FIELDS_STANDARD,
            applicantDetails,
            APPLICANT_ENTITY_FIELDS,
        )
        Assertions.assertThat(actual.detail.lang.name).isEqualTo(applicantDetails.language.name)
    }

    private fun hasPrimaryElectorDetails(primaryElectorDetails: PostalVoteApplicationPrimaryElectorDetails?) = validate {
        if (primaryElectorDetails != null) {
            haveSameValues(
                actual.detail,
                PRIMARY_ELECTOR_FIELDS,
                primaryElectorDetails,
                PRIMARY_ELECTOR_FIELDS_ENTITY,
            )
        }
    }

    fun hasPostalVoteDetails(postalVoteDetails: PostalVoteDetails?) = validate {
        isNotNull
        haveSameValues(actual.detail, POSTAL_VOTE_FIELDS, postalVoteDetails, POSTAL_VOTE_ENTITY_FIELDS)
    }

    fun hasBallotAddress(ballotAddress: Address) = validate {
        isNotNull
        haveSameValues(
            actual.detail.ballotAddress,
            ADDRESS_ENTITY_FIELDS,
            ballotAddress
        )
    }

    fun hasBfpoAddress(bfpoAddress: BfpoAddress) = validate {
        isNotNull
        haveSameValues(
            actual.detail.ballotBfpoPostalAddress,
            BALLOT_BFPO_ADDRESS_FIELDS,
            bfpoAddress
        )
    }

    fun hasOverseasAddress(overseasAddress: OverseasAddress) = validate {
        isNotNull
        haveSameValues(
            actual.detail.ballotOverseasPostalAddress,
            BALLOT_OVERSEAS_ADDRESS_FIELDS,
            overseasAddress
        )
    }

    private fun doesNotHaveBallotAddress() = Assertions.assertThat(actual.detail.ballotAddress).isNull()

    private fun doesNotHaveBallotBfpoAddress() = Assertions.assertThat(actual.detail.ballotBfpoPostalAddress).isNull()

    private fun doesNotHaveBallotOverseasAddress() = Assertions.assertThat(actual.detail.ballotOverseasPostalAddress).isNull()

    fun doesNotHaveAddresses() {
        doesNotHaveBallotAddress()
        doesNotHaveBallotBfpoAddress()
        doesNotHaveBallotOverseasAddress()
    }

    fun doesNotHavePostalVoteDetails() {
        validate { haveNullValues(actual.detail, *POSTAL_VOTE_FIELDS) }
        doesNotHaveAddresses()
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

    fun hasNoPostalProxy() = validate { Assertions.assertThat(actual.detail.postalProxy).isNull() }

    private fun hasCorrectAddressFields(addressModel: AddressModel, addressEntity: Address?) =
        validate { haveSameValues(addressModel, ADDRESS_ENTITY_FIELDS, addressEntity) }

    private fun validate(validationFunction: () -> Unit): PostalVoteAssert {
        validationFunction()
        return this
    }
}
