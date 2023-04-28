package uk.gov.dluhc.emsintegrationapi.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions.ProxyVoteAssert
import uk.gov.dluhc.emsintegrationapi.testsupport.haveNullValues
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.SIGNATURE_BASE64_STRING
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.SIGNATURE_WAIVER_REASON
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteDetailsEntity

internal class ProxyVoteMapperTest {

    private val proxyVoteMapper = ProxyVoteMapper(instantMapper = InstantMapper())

    @Test
    fun `should map from a proxy vote application entity with signature`() {
        val proxyVoteApplication =
            buildProxyVoteApplication(applicationDetails = buildApplicationDetailsEntity(signatureBase64 = SIGNATURE_BASE64_STRING))
        val proxyVote = proxyVoteMapper.mapFromEntity(proxyVoteApplication)
        ProxyVoteAssert.assertThat(proxyVote).hasCorrectFieldsFromProxyApplication(proxyVoteApplication)
            .hasSignature(SIGNATURE_BASE64_STRING)
            .hasNoSignatureWaiver()
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

    @Test
    fun `should map from a proxy vote application entity with signature waiver`() {
        val proxyVoteApplication =
            buildProxyVoteApplication(
                applicationDetails = buildApplicationDetailsEntity(
                    signatureWaivedReason = SIGNATURE_WAIVER_REASON,
                    signatureWaived = true
                )
            )
        val proxyVote = proxyVoteMapper.mapFromEntity(proxyVoteApplication)
        ProxyVoteAssert.assertThat(proxyVote).hasCorrectFieldsFromProxyApplication(proxyVoteApplication)
            .signatureWaived()
            .hasSignatureWaiverReason(SIGNATURE_WAIVER_REASON)
            .hasNoSignature()
    }
}
