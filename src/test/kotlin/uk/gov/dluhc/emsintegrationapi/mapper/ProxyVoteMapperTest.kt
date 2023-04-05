package uk.gov.dluhc.emsintegrationapi.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions.ProxyVoteAssert
import uk.gov.dluhc.emsintegrationapi.testsupport.haveNullValues
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteDetailsEntity

internal class ProxyVoteMapperTest {

    private val proxyVoteMapper = ProxyVoteMapper(instantMapper = InstantMapper())

    @Test
    fun `should map from a proxy vote application entity`() {
        val proxyVoteApplication = buildProxyVoteApplication()
        val proxyVote = proxyVoteMapper.mapFromEntity(proxyVoteApplication)
        ProxyVoteAssert.assertThat(proxyVote).hasCorrectFieldsFromProxyApplication(proxyVoteApplication)
    }

    @Test
    fun `should map an entity with optional fields are null`() {
        val proxyVoteApplication = buildProxyVoteApplication(
            proxyVoteDetails = buildProxyVoteDetailsEntity(
                voteUntilFurtherNotice = null,
                voteEndDate = null,
                voteForSingleDate = null,
                proxyFamilyRelationship = null,
                proxyMiddleNames = null,
            )
        )
        val proxyVote = proxyVoteMapper.mapFromEntity(proxyVoteApplication)
        ProxyVoteAssert.assertThat(proxyVote).hasCorrectFieldsFromProxyApplication(proxyVoteApplication)

        haveNullValues(
            proxyVote.detail,
            "proxymn",
            "proxyVoteUntilFurtherNotice",
            "proxyVoteForSingleDate",
            "proxyVoteStartDate",
            "proxyVoteEndDate",
            "proxyfamilyrelationship",
        )
    }

    @Test
    fun `should map multiple proxy vote applications from entities`() {
        val proxyVoteApplications =
            listOf(buildProxyVoteApplication(), buildProxyVoteApplication(), buildProxyVoteApplication())
        val proxyVotes = proxyVoteMapper.mapFromEntities(proxyVoteApplications)
        assertThat(proxyVotes).hasSize(proxyVoteApplications.size)
        proxyVotes.forEachIndexed { index, proxyVote ->
            ProxyVoteAssert.assertThat(proxyVote)
                .hasCorrectFieldsFromProxyApplication(proxyVoteApplications[index])
        }
    }
}
