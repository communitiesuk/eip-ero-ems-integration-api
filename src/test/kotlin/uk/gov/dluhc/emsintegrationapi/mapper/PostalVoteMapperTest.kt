package uk.gov.dluhc.emsintegrationapi.mapper

import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions.PostalVoteAssert
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteDetailsEntity

internal class PostalVoteMapperTest {

    private val postalVoteMapper = PostalVoteMapper(instantMapper = InstantMapper())

    @Test
    fun `should map from a postal vote application entity`() {
        val postalVoteApplication = buildPostalVoteApplication()
        val postalVote = postalVoteMapper.mapFromEntity(postalVoteApplication)
        PostalVoteAssert.assertThat(postalVote).hasCorrectFieldsFromPostalApplication(postalVoteApplication)
            .hasPostalVoteDetails(postalVoteApplication.postalVoteDetails)
            .hasBallotAddress(postalVoteApplication.postalVoteDetails?.ballotAddress!!)
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
}
