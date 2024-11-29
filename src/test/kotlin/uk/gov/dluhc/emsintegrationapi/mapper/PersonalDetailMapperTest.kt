package uk.gov.dluhc.emsintegrationapi.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.dto.buildAddressDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.dto.buildAddressDtoWithOptionalFieldsNull
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.dto.buildPersonalDetailDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.dto.buildPersonalDetailDtoWithOptionalFieldsNull
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity.buildAddress
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity.buildAddressWithOptionalFieldsAsNull
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity.buildPersonalDetail
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity.buildPersonalDetailWithOptionalFieldsAsNull

internal class PersonalDetailMapperTest {

    private val mapper = PersonalDetailMapperImpl()

    companion object {
        val JPA_MANAGED_FIELDS = arrayOf(
            "id",
            "dateCreated",
            "address.id",
            "address.dateCreated",
            "address.version",
        )
    }

    @Nested
    inner class FromDtoToEntity {

        @Test
        fun `should map dto to entity`() {
            // Given
            val personalDetailDto = buildPersonalDetailDto()
            val expected = buildPersonalDetail(
                firstName = personalDetailDto.firstName,
                middleNames = personalDetailDto.middleNames,
                surname = personalDetailDto.surname,
                dateOfBirth = personalDetailDto.dateOfBirth,
                email = personalDetailDto.email,
                phoneNumber = personalDetailDto.phone,
                address = buildAddress(
                    street = personalDetailDto.address.street,
                    property = personalDetailDto.address.property,
                    locality = personalDetailDto.address.locality,
                    town = personalDetailDto.address.town,
                    area = personalDetailDto.address.area,
                    postcode = personalDetailDto.address.postcode,
                    uprn = personalDetailDto.address.uprn
                )
            )

            // When
            val actual = mapper.personalDetailDtoToPersonalDetailEntity(personalDetailDto)

            // Then
            assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields(*JPA_MANAGED_FIELDS)
                .isEqualTo(expected)
        }

        @Test
        fun `should map dto to entity when optional fields are null`() {
            // Given
            val personalDetailDto = buildPersonalDetailDtoWithOptionalFieldsNull()

            val expected = buildPersonalDetailWithOptionalFieldsAsNull(
                firstName = personalDetailDto.firstName,
                surname = personalDetailDto.surname,
                address = buildAddressWithOptionalFieldsAsNull(
                    street = personalDetailDto.address.street,
                    postcode = personalDetailDto.address.postcode,
                )
            )

            // When
            val actual = mapper.personalDetailDtoToPersonalDetailEntity(personalDetailDto)

            // Then
            assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields(*JPA_MANAGED_FIELDS)
                .isEqualTo(expected)
        }
    }

    @Nested
    inner class FromEntityToDto {

        @Test
        fun `should map entity to dto`() {
            // Given
            val personalDetailEntity = buildPersonalDetail()
            val expected = buildPersonalDetailDto(
                firstName = personalDetailEntity.firstName,
                middleNames = personalDetailEntity.middleNames,
                surname = personalDetailEntity.surname,
                dateOfBirth = personalDetailEntity.dateOfBirth,
                email = personalDetailEntity.email,
                phone = personalDetailEntity.phoneNumber,
                address = buildAddressDto(
                    street = personalDetailEntity.address.street,
                    property = personalDetailEntity.address.property,
                    locality = personalDetailEntity.address.locality,
                    town = personalDetailEntity.address.town,
                    area = personalDetailEntity.address.area,
                    postcode = personalDetailEntity.address.postcode,
                    uprn = personalDetailEntity.address.uprn
                )
            )

            // When
            val actual = mapper.personalDetailEntityToPersonalDetailDto(personalDetailEntity)

            // Then
            assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields(*JPA_MANAGED_FIELDS)
                .isEqualTo(expected)
        }

        @Test
        fun `should map entity to dto when optional fields are null`() {
            // Given
            val personalDetailEntity = buildPersonalDetailWithOptionalFieldsAsNull()
            val expected = buildPersonalDetailDtoWithOptionalFieldsNull(
                firstName = personalDetailEntity.firstName,
                surname = personalDetailEntity.surname,
                address = buildAddressDtoWithOptionalFieldsNull(
                    street = personalDetailEntity.address.street,
                    postcode = personalDetailEntity.address.postcode,
                )
            )

            // When
            val actual = mapper.personalDetailEntityToPersonalDetailDto(personalDetailEntity)

            // Then
            assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected)
        }
    }
}
