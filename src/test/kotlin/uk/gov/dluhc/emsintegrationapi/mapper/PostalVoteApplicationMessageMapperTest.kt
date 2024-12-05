package uk.gov.dluhc.emsintegrationapi.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.mapper.Constants.Companion.APPLICATION_FIELDS_TO_IGNORE
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalRejectedReasonsDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPrimaryElectorDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.validateMappedObject

internal class PostalVoteApplicationMessageMapperTest {
    private val addressMapper = AddressMapper()
    private val instantMapper = InstantMapper()
    private val applicantDetailsMapper = ApplicantDetailsMapper(addressMapper)
    private val postalVoteDetailsMapper = PostalVoteDetailsMapper(addressMapper)
    private val applicationDetailsMapper = ApplicationDetailsMapper(instantMapper)
    private val primaryElectorDetailsMapper = PrimaryElectorDetailsMapper(addressMapper)
    private val postalVoteApplicationMessageMapper = PostalVoteApplicationMessageMapper(
        applicantDetailsMapper = applicantDetailsMapper,
        postalVoteDetailsMapper = postalVoteDetailsMapper,
        applicationDetailsMapper = applicationDetailsMapper,
        primaryElectorDetailsMapper = primaryElectorDetailsMapper,
    )

    @Nested
    inner class FromPostalVoteMessageDtoToEntity {
        @Test
        fun `should convert postal vote application message to entity`() {

            validateMappedObject(
                { buildPostalVoteApplicationMessage(primaryElectorDetails = buildPrimaryElectorDetailsMessageDto()) },
                postalVoteApplicationMessageMapper::mapToEntity,
                *APPLICATION_FIELDS_TO_IGNORE
            ) {
                assertThat(it.output.applicantDetails.registeredAddress.createdBy).isEqualTo(SourceSystem.POSTAL)
                assertThat(it.output.postalVoteDetails!!.ballotAddress!!.createdBy).isEqualTo(SourceSystem.POSTAL)
                assertThat(it.output.postalVoteDetails!!.ballotOverseasAddress!!.createdBy).isEqualTo(SourceSystem.POSTAL)
                assertThat(it.output.postalVoteDetails!!.ballotBfpoAddress!!.createdBy).isEqualTo(SourceSystem.POSTAL)
                assertThat(it.output.primaryElectorDetails!!.address.createdBy).isEqualTo(SourceSystem.POSTAL)
                assertThat(it.output.englishRejectionNotes).isNotNull()
                assertThat(it.output.englishRejectedReasonItems?.isNotEmpty())
                assertThat(it.output.welshRejectionNotes).isNotNull()
                assertThat(it.output.welshRejectedReasonItems?.isNotEmpty())
            }
        }

        @Test
        fun `should convert postal vote application message to entity without ballot addresses`() {

            val applicationMessage: PostalVoteApplicationMessage =
                buildPostalVoteApplicationMessage(
                    postalVoteDetails = buildPostalVoteDetailsMessageDto(
                        ballotAddress = null,
                        ballotOverseasAddress = null,
                        ballotBfpoAddress = null
                    )
                )
            val postalVoteApplication: PostalVoteApplication = postalVoteApplicationMessageMapper.mapToEntity(applicationMessage)
            assertThat(postalVoteApplication.applicantDetails.registeredAddress.createdBy).isEqualTo(SourceSystem.POSTAL)
            assertThat(postalVoteApplication.postalVoteDetails?.ballotAddress).isNull()
            assertThat(postalVoteApplication.postalVoteDetails?.ballotOverseasAddress).isNull()
            assertThat(postalVoteApplication.postalVoteDetails?.ballotBfpoAddress).isNull()
        }
    }

    @Test
    fun `should convert postal vote application message to entity without rejected english notes and welsh rejected reasons`() {

        val applicationMessage: PostalVoteApplicationMessage =
            buildPostalVoteApplicationMessage(
                postalVoteDetails = buildPostalVoteDetailsMessageDto(
                    rejectedReasons = buildPostalRejectedReasonsDto(
                        englishNotes = null,
                        welshNotes = null,
                        welshReason = null
                    )
                )
            )
        val postalVoteApplication: PostalVoteApplication = postalVoteApplicationMessageMapper.mapToEntity(applicationMessage)
        assertThat(postalVoteApplication.applicantDetails.registeredAddress.createdBy).isEqualTo(SourceSystem.POSTAL)
        assertThat(postalVoteApplication.englishRejectionNotes).isNull()
        assertThat(postalVoteApplication.englishRejectedReasonItems?.isNotEmpty())
        assertThat(postalVoteApplication.welshRejectionNotes).isNull()
        assertThat(postalVoteApplication.welshRejectedReasonItems).isNull()
    }
}
