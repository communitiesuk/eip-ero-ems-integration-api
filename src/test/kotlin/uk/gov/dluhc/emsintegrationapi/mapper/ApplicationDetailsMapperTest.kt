package uk.gov.dluhc.emsintegrationapi.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.validateMappedObject

internal class ApplicationDetailsMapperTest {

    private val instantMapper = InstantMapper()
    private val approvalDetailsMapper = ApplicationDetailsMapper(instantMapper)

    @Nested
    inner class FromApplicationDetailsToEntity {

        @Test
        fun `should convert applicant message dto to entity`() {
            validateMappedObject(
                ::buildApplicationDetailsMessageDto,
                approvalDetailsMapper::mapToApplicationDetails,
                "authorisedAt", "createdAt"
            ) {
                assertThat(it.output.authorisedAt).isEqualTo(instantMapper.toInstant(it.input.authorisedAt))
                assertThat(it.output.createdAt).isEqualTo(instantMapper.toInstant(it.input.createdAt))
            }
        }
    }
}
