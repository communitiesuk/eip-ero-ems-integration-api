package uk.gov.dluhc.emsintegrationapi.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import uk.gov.dluhc.emsintegrationapi.client.IerApiClient
import uk.gov.dluhc.emsintegrationapi.client.IerEroNotFoundException
import uk.gov.dluhc.emsintegrationapi.client.IerTooManyErosFoundException
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.models.buildIerEroDetails

@ExtendWith(MockitoExtension::class)
class RetrieveEroIdServiceTest {
    @Mock
    private lateinit var ierApiClient: IerApiClient

    @InjectMocks
    private lateinit var retrieveEroIdService: RetrieveEroIdService

    companion object {
        private const val CERTIFICATE_SERIAL_NUMBER = "test"
        private const val ERO_ID = "ero-id"
    }

    @Nested
    inner class GetEroIdFromCertificateSerial {
        @Test
        fun `should return ero ID of ero matching certificate serial`() {
            // Given
            given(ierApiClient.getEros()).willReturn(
                listOf(
                    buildIerEroDetails(
                        eroIdentifier = ERO_ID,
                        activeClientCertificateSerials = listOf(CERTIFICATE_SERIAL_NUMBER),
                        localAuthorities = listOf()
                    ),
                    buildIerEroDetails(
                        eroIdentifier = "$ERO_ID-2",
                        activeClientCertificateSerials = listOf("$CERTIFICATE_SERIAL_NUMBER-2"),
                        localAuthorities = listOf()
                    )
                )
            )

            // When
            val eroId = retrieveEroIdService.getEroIdFromCertificateSerial(CERTIFICATE_SERIAL_NUMBER)

            // Then
            assertThat(eroId).isEqualTo(ERO_ID)
        }

        @Test
        fun `should throw if multiple ERO's found with certificate serial`() {
            // Given
            given(ierApiClient.getEros()).willReturn(
                listOf(
                    buildIerEroDetails(
                        eroIdentifier = ERO_ID,
                        activeClientCertificateSerials = listOf(CERTIFICATE_SERIAL_NUMBER),
                        localAuthorities = listOf()
                    ),
                    buildIerEroDetails(
                        eroIdentifier = "$ERO_ID-2",
                        activeClientCertificateSerials = listOf(CERTIFICATE_SERIAL_NUMBER),
                        localAuthorities = listOf()
                    )
                )
            )

            // When
            val ex = catchThrowableOfType(IerTooManyErosFoundException::class.java) {
                retrieveEroIdService.getEroIdFromCertificateSerial(CERTIFICATE_SERIAL_NUMBER)
            }

            // Then
            assertThat(ex).isInstanceOf(IerTooManyErosFoundException::class.java)
            assertThat(ex.message).isEqualTo("Multiple EROs found for certificateSerial=[$CERTIFICATE_SERIAL_NUMBER]")
        }

        @Test
        fun `should throw if no ERO's found with certificate serial`() {
            // Given
            given(ierApiClient.getEros()).willReturn(
                emptyList()
            )

            // When
            val ex = catchThrowableOfType(IerEroNotFoundException::class.java) {
                retrieveEroIdService.getEroIdFromCertificateSerial(CERTIFICATE_SERIAL_NUMBER)
            }

            // Then
            assertThat(ex).isInstanceOf(IerEroNotFoundException::class.java)
            assertThat(ex.message).isEqualTo("EROCertificateMapping for certificateSerial=[$CERTIFICATE_SERIAL_NUMBER] not found")
        }
    }
}
