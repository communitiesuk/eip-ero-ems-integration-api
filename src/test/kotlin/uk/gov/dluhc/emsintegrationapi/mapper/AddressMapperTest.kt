package uk.gov.dluhc.emsintegrationapi.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildAddressMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.validateMappedObject

internal class AddressMapperTest {

    private val addressMapper = AddressMapper()

    @Nested
    inner class FromAddressMessageDtoToEntity {
        @ParameterizedTest
        @EnumSource(names = ["POSTAL", "PROXY"])
        fun `should return address entity object from message dto`(sourceSystem: SourceSystem) {
            validateMappedObject(
                ::buildAddressMessageDto, { addressMapper.mapToAddressEntity(it, sourceSystem) },
                "createdBy"
            ) {
                assertThat(it.output!!.createdBy).isEqualTo(sourceSystem)
            }
        }

        @Test
        fun `should return null if the input object is null`() =
            assertThat(addressMapper.mapToAddressEntity(null, SourceSystem.POSTAL)).isNull()
    }
}
