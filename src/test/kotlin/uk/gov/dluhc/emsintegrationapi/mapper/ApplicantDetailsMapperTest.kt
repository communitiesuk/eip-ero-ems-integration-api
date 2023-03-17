package uk.gov.dluhc.emsintegrationapi.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicantDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.validateMappedObject

internal class ApplicantDetailsMapperTest {

    private val applicantDetailsMapper = ApplicantDetailsMapper(AddressMapper())

    @Nested
    inner class FromApplicantDetailsMessageDtoToEntity {
        @ParameterizedTest
        @EnumSource(names = ["POSTAL", "PROXY"])
        fun `should convert applicant message dto to entity`(sourceSystem: SourceSystem) {
            validateMappedObject(
                ::buildApplicantDetailsMessageDto,
                { applicantDetailsMapper.mapToApplicantEntity(it, sourceSystem) }
            ) {
                assertThat(it.output!!.registeredAddress.createdBy).isEqualTo(sourceSystem)
            }
        }

        @Test
        fun `should return null if the input object is null`() =
            assertThat(applicantDetailsMapper.mapToApplicantEntity(null, SourceSystem.PROXY)).isNull()
    }
}
