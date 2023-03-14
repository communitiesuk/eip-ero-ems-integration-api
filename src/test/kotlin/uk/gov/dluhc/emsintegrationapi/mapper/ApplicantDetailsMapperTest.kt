package uk.gov.dluhc.emsintegrationapi.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicantDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.validateMappedObject
import uk.gov.dluhc.emsintegrationapi.testsupport.validateWithNull

internal class ApplicantDetailsMapperTest {

    private val applicantDetailsMapper = ApplicantDetailsMapper(AddressMapper())

    @Nested
    inner class FromApplicantDetailsMessageDtoToEntity {
        @Test
        fun `should convert applicant message dto to entity`() {
            validateMappedObject(
                buildApplicantDetailsMessageDto(),
                applicantDetailsMapper::mapToApplicantEntity,
                "registeredAddress.createdBy"
            ) {
                assertThat(it!!.registeredAddress.createdBy).isEqualTo(SourceSystem.POSTAL)
            }
        }

        @Test
        fun `should return null if the input object is null`() =
            validateWithNull(applicantDetailsMapper::mapToApplicantEntity)
    }
}
