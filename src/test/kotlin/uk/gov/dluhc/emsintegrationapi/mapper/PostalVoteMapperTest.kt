package uk.gov.dluhc.emsintegrationapi.mapper

import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions.PostalVoteAssert
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.SIGNATURE_BASE64_STRING
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.SIGNATURE_WAIVER_REASON
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteDetailsEntity

internal class PostalVoteMapperTest {

    private val postalVoteMapper = PostalVoteMapper(instantMapper = InstantMapper())

    @Test
    fun `should map from a postal vote application entity with signature`() {
        val postalVoteApplication =
            buildPostalVoteApplication(applicationDetails = buildApplicationDetailsEntity(signatureBase64 = SIGNATURE_BASE64_STRING))
        val postalVote = postalVoteMapper.mapFromEntity(postalVoteApplication)
        PostalVoteAssert.assertThat(postalVote).hasCorrectFieldsFromPostalApplication(postalVoteApplication)
            .hasPostalVoteDetails(postalVoteApplication.postalVoteDetails)
            .hasBallotAddress(postalVoteApplication.postalVoteDetails?.ballotAddress!!)
            .hasSignature(SIGNATURE_BASE64_STRING)
            .hasNoSignatureWaiver()
    }

    @Test
    fun `should map from an application without postal dates and ballot address`() {
        val postalVoteApplication = buildPostalVoteApplication(
            postalVoteDetails = null
        )
        val postalVote = postalVoteMapper.mapFromEntity(postalVoteApplication)
        PostalVoteAssert.assertThat(postalVote).hasCorrectFieldsFromPostalApplication(postalVoteApplication)
            .doesNotHavePostalVoteDetails()
            .doesNotHaveBallotAddress()
    }

    @Test
    fun `should map from an application contains postal vote dates but no ballot address`() {
        val postalVoteApplication = buildPostalVoteApplication(
            postalVoteDetails = buildPostalVoteDetailsEntity(ballotAddress = null)
        )
        val postalVote = postalVoteMapper.mapFromEntity(postalVoteApplication)
        PostalVoteAssert.assertThat(postalVote).hasCorrectFieldsFromPostalApplication(postalVoteApplication)
            .hasPostalVoteDetails(postalVoteApplication.postalVoteDetails)
            .doesNotHaveBallotAddress()
    }

    @Test
    fun `should map from a postal vote application entity with signature waiver`() {
        val postalVoteApplication =
            buildPostalVoteApplication(
                applicationDetails = buildApplicationDetailsEntity(
                    signatureWaivedReason = SIGNATURE_WAIVER_REASON,
                    signatureWaived = true
                )
            )
        val postalVote = postalVoteMapper.mapFromEntity(postalVoteApplication)
        PostalVoteAssert.assertThat(postalVote).hasCorrectFieldsFromPostalApplication(postalVoteApplication)
            .hasPostalVoteDetails(postalVoteApplication.postalVoteDetails)
            .hasBallotAddress(postalVoteApplication.postalVoteDetails?.ballotAddress!!)
            .signatureWaived()
            .hasSignatureWaiverReason(SIGNATURE_WAIVER_REASON)
            .hasNoSignature()
    }
}
