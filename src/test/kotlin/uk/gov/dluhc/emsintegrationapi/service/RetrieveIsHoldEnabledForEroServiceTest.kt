package uk.gov.dluhc.emsintegrationapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import uk.gov.dluhc.emsintegrationapi.database.entity.EroAbsentVoteHold
import uk.gov.dluhc.emsintegrationapi.database.repository.EroAbsentVoteHoldRepository
import java.util.Optional

@ExtendWith(MockitoExtension::class)
internal class RetrieveIsHoldEnabledForEroServiceTest {
    @Mock
    private lateinit var retrieveEroIdService: RetrieveEroIdService

    @Mock
    private lateinit var eroAbsentVoteHoldRepository: EroAbsentVoteHoldRepository

    @InjectMocks
    private lateinit var isHoldEnabledForEroService: RetrieveIsHoldEnabledForEroService

    companion object {
        private const val CERTIFICATE_SERIAL_NUMBER = "test"
        private const val ERO_ID = "ero-id"
    }

    @Nested
    inner class GetIsHoldEnabled {

        @BeforeEach
        fun setUp() {
            given(retrieveEroIdService.getEroIdFromCertificateSerial(CERTIFICATE_SERIAL_NUMBER)).willReturn(ERO_ID)
        }

        @Test
        fun `should return true if hold is enabled for ERO`() {
            // Given
            given(eroAbsentVoteHoldRepository.findById(ERO_ID)).willReturn(Optional.of(EroAbsentVoteHold(ERO_ID, true)))

            // When
            val isHoldEnabled = isHoldEnabledForEroService.getIsHoldEnabled(CERTIFICATE_SERIAL_NUMBER)

            // Then
            assertThat(isHoldEnabled).isEqualTo(true)
        }

        @Test
        fun `should return false if hold is not enabled for ERO`() {
            // Given
            given(eroAbsentVoteHoldRepository.findById(ERO_ID)).willReturn(Optional.of(EroAbsentVoteHold(ERO_ID, true)))

            // When
            val isHoldEnabled = isHoldEnabledForEroService.getIsHoldEnabled(CERTIFICATE_SERIAL_NUMBER)

            // Then
            assertThat(isHoldEnabled).isEqualTo(true)
        }

        @Test
        fun `should return false if no row found for ERO`() {
            // Given
            given(eroAbsentVoteHoldRepository.findById(ERO_ID)).willReturn(Optional.of(EroAbsentVoteHold(ERO_ID, true)))

            // When
            val isHoldEnabled = isHoldEnabledForEroService.getIsHoldEnabled(CERTIFICATE_SERIAL_NUMBER)

            // Then
            assertThat(isHoldEnabled).isEqualTo(true)
        }
    }
}
