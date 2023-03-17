package uk.gov.dluhc.emsintegrationapi.mapper

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.validateMappedObject

internal class ProxyVoteDetailsMapperTest {

    private val proxyVoteDetailsMapper = ProxyVoteDetailsMapper(AddressMapper())

    @Nested
    inner class FromProxyVoteDetailsToEntity {
        @Test
        fun `should convert a proxy vote detail message dto to entity`() =
            validateMappedObject(
                ::buildProxyVoteDetailsMessageDto,
                proxyVoteDetailsMapper::mapToProxyVoteDetailsEntity
            )
    }
}
