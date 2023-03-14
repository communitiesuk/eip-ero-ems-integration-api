package uk.gov.dluhc.emsintegrationapi.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildAddressMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.validateMappedObject
import uk.gov.dluhc.emsintegrationapi.testsupport.validateWithNull

internal class AddressMapperTest {

    private val addressMapper = AddressMapper()

    @Nested
    inner class FromAddressMessageDtoToEntity {
        @Test
        fun `should return address entity object from message dto`() {
            validateMappedObject(
                buildAddressMessageDto(), addressMapper::mapToAddressEntity,
                "createdBy"
            ) {
                assertThat(it!!.createdBy).isEqualTo(SourceSystem.POSTAL)
            }
        }

        @Test
        fun `should return null if the input object is null`() =
            validateWithNull(addressMapper::mapToAddressEntity)
    }
}
