package uk.gov.dluhc.emsintegrationapi.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplicationMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.validateMappedObject
import uk.gov.dluhc.emsintegrationapi.testsupport.validateWithNull

internal class PostalVoteApplicationMessageMapperTest {
    private val addressMapper = AddressMapper()
    private val instantMapper = InstantMapper()
    private val applicantDetailsMapper = ApplicantDetailsMapper(addressMapper)
    private val postalVoteDetailsMapper = PostalVoteDetailsMapper(addressMapper)
    private val approvalDetailsMapper = ApprovalDetailsMapper(instantMapper)
    private val postalVoteApplicationMessageMapper = PostalVoteApplicationMessageMapper(
        applicantDetailsMapper = applicantDetailsMapper,
        postalVoteDetailsMapper = postalVoteDetailsMapper,
        approvalDetailsMapper = approvalDetailsMapper
    )

    @Nested
    inner class FromPostalVoteMessageDtoToEntity {
        @Test
        fun `should convert postal vote application message to entity`() {

            validateMappedObject(
                buildPostalVoteApplicationMessageDto(),
                postalVoteApplicationMessageMapper::mapToEntity,
                "applicantDetails.registeredAddress.createdBy",
                "postalVoteDetails.ballotAddress.createdBy",
                "approvalDetails.authorisedAt", "approvalDetails.createdAt", "approvalDetails.id"
            ) {
                assertThat(it!!.applicantDetails.registeredAddress.createdBy).isEqualTo(SourceSystem.POSTAL)
                assertThat(it.postalVoteDetails!!.ballotAddress!!.createdBy).isEqualTo(SourceSystem.POSTAL)
            }
        }

        @Test
        fun `should return null if the input object is null`() =
            validateWithNull(postalVoteApplicationMessageMapper::mapToEntity)
    }
}
