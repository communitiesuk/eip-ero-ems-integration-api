package uk.gov.dluhc.emsintegrationapi.messaging.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.messaging.buildRemoveRegisterCheckDataMessage

@ExtendWith(MockitoExtension::class)
internal class RegisterCheckRemovalMapperTest {

    @InjectMocks
    private lateinit var mapper: RegisterCheckRemovalMapperImpl

    @Test
    fun `should map message to dto`() {
        // Given
        val message = buildRemoveRegisterCheckDataMessage()

        // When
        val actual = mapper.toRemovalDto(message)

        // Then
        assertThat(actual.sourceReference).isEqualTo(message.sourceReference)
    }
}
