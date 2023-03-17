package uk.gov.dluhc.emsintegrationapi.mapper

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.validateMappedObject
import uk.gov.dluhc.emsintegrationapi.testsupport.validateWithNull

internal class PostalVoteDetailsMapperTest {

    private val postalVoteDetailsMapper = PostalVoteDetailsMapper(AddressMapper())

    @Nested
    inner class FromPostalVoteDetailsDtoToEntity {
        @Test
        fun `should convert postal vote details message dto to entity`() {
            validateMappedObject(
                ::buildPostalVoteDetailsMessageDto,
                postalVoteDetailsMapper::mapToPostVoteDetailsEntity
            )
        }

        @Test
        fun `should return null if the input object is null`() =
            validateWithNull(postalVoteDetailsMapper::mapToPostVoteDetailsEntity)
    }
}
