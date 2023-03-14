package uk.gov.dluhc.emsintegrationapi.mapper

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.testsupport.validateMappedValue
import uk.gov.dluhc.emsintegrationapi.testsupport.validateWithNull
import java.time.OffsetDateTime

internal class InstantMapperTest {

    private val instantMapper = InstantMapper()

    @Nested
    inner class FromOffsetDateTimeToInstant {
        @Test
        fun `should return an instant object`() {
            OffsetDateTime.now().apply {
                validateMappedValue(this, instantMapper::toInstant, toInstant())
            }
        }

        @Test
        fun `should return null if input object is null`() =
            validateWithNull(instantMapper::toInstant)
    }
}
