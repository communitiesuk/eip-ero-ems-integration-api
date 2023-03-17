package uk.gov.dluhc.emsintegrationapi.mapper

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.validateMappedObject
import uk.gov.dluhc.emsintegrationapi.testsupport.validateWithNull

internal class ProxyVoteDetailsMapperTest {

    private val proxyVoteDetailsMapper = ProxyVoteDetailsMapper(AddressMapper())

    @Nested
    inner class FromProxyVoteDetailsToEntity {
        @Test
        fun `should convert a proxy vote detail message dto to entity`() =
            validateMappedObject(
                ::buildProxyVoteDetailsMessageDto,
                proxyVoteDetailsMapper::mapToProxyVoteDetailsEntity,
                "proxyAddress.createdBy"
            )

        @Test
        fun `should return null if the input object is null`() =
            validateWithNull(proxyVoteDetailsMapper::mapToProxyVoteDetailsEntity)
    }
}
