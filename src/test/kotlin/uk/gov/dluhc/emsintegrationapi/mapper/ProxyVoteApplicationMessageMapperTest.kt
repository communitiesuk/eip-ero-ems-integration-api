package uk.gov.dluhc.emsintegrationapi.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.mapper.Constants.Companion.APPLICATION_FIELDS_TO_IGNORE
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplicationMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.validateMappedObject

internal class ProxyVoteApplicationMessageMapperTest {
    private val addressMapper = AddressMapper()
    private val instantMapper = InstantMapper()
    private val applicantDetailsMapper = ApplicantDetailsMapper(addressMapper)
    private val proxyVoteDetailsMapper = ProxyVoteDetailsMapper(addressMapper)
    private val applicationDetailsMapper = ApplicationDetailsMapper(instantMapper)
    private val proxyVoteApplicationMessageMapper = ProxyVoteApplicationMessageMapper(
        applicantDetailsMapper = applicantDetailsMapper,
        proxyVoteDetailsMapper = proxyVoteDetailsMapper,
        applicationDetailsMapper = applicationDetailsMapper
    )

    @Nested
    inner class FromProxyVoteMessageDtoToEntity {
        @Test
        fun `should convert a proxy vote application message to entity`() {

            validateMappedObject(
                ::buildProxyVoteApplicationMessageDto,
                proxyVoteApplicationMessageMapper::mapToEntity,
                *APPLICATION_FIELDS_TO_IGNORE
            ) {
                assertThat(it.output.applicantDetails.registeredAddress.createdBy).isEqualTo(SourceSystem.PROXY)
                assertThat(it.output.proxyVoteDetails.proxyAddress?.createdBy).isEqualTo(SourceSystem.PROXY)
            }
        }
    }
}
