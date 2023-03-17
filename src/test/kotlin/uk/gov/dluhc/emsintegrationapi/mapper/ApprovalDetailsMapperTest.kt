package uk.gov.dluhc.emsintegrationapi.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApprovalDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.validateMappedObject

internal class ApprovalDetailsMapperTest {

    private val instantMapper = InstantMapper()
    private val approvalDetailsMapper = ApprovalDetailsMapper(instantMapper)

    @Nested
    inner class FromApprovalDetailsToEntity {

        @Test
        fun `should convert applicant message dto to entity`() {
            validateMappedObject(
                ::buildApprovalDetailsMessageDto,
                approvalDetailsMapper::mapToApprovalDetails,
                "authorisedAt", "createdAt"
            ) {
                assertThat(it.output.authorisedAt).isEqualTo(instantMapper.toInstant(it.input.authorisedAt))
                assertThat(it.output.createdAt).isEqualTo(instantMapper.toInstant(it.input.createdAt))
            }
        }
    }
}
