package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.dluhc.emsintegrationapi.client.IerApiClient
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.TEST_CERTIFICATE_SERIAL_NUMBER
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.TEST_ERO_ID
import java.time.Duration

private val logger = KotlinLogging.logger { }

internal class RetrieveGssCodeServiceIntegrationTest : IntegrationTest() {

    @Autowired
    private lateinit var ierApiClient: IerApiClient

    @Autowired
    private lateinit var retrieveGssCodeService: RetrieveGssCodeService

    @Nested
    inner class GetGssCodesFromCertificateSerial {
        @Test
        fun `Get GSS Codes By ERO ID`() {
            wireMockService.resetAllStubsAndMappings()
            // Given the certificate serial number "1234567891" mapped to the ERO Id "camden-city-council"
            // And the gss codes "E12345678" and "E12345679" mapped to the ERO Id
            wireMockService.stubIerApiGetErosWithGssCodes(TEST_CERTIFICATE_SERIAL_NUMBER, TEST_ERO_ID, listOf("E12345678", "E12345679"))
            wireMockService.givenEroIdAndGssCodesMappedToCertificate(TEST_CERTIFICATE_SERIAL_NUMBER, TEST_ERO_ID)

            // When I send a request to get the gss codes
            val gssCodes = retrieveGssCodeService.getGssCodesFromCertificateSerial(TEST_CERTIFICATE_SERIAL_NUMBER)

            // Then I received the gss codes "E12345678" and "E12345679"
            assertThat(gssCodes).hasSize(2)
            assertThat(gssCodes).containsOnly("E12345678", "E12345679")
        }

        @Test
        fun `Get GSS Codes By ERO ID  - the system caches get gss code response`() {
            wireMockService.resetAllStubsAndMappings()
            // Given the certificate serial number "1234567891" mapped to the ERO Id "camden-city-council"
            // And the gss codes "E12345678" and "E12345679" mapped to the ERO Id
            wireMockService.stubIerApiGetErosWithGssCodes(TEST_CERTIFICATE_SERIAL_NUMBER, TEST_ERO_ID, listOf("E12345678", "E12345679"))

            // When I send a request to get the gss codes
            val gssCodes = retrieveGssCodeService.getGssCodesFromCertificateSerial(TEST_CERTIFICATE_SERIAL_NUMBER)

            // Then I received the gss codes "E12345678" and "E12345679"
            assertThat(gssCodes).hasSize(2)
            assertThat(gssCodes).containsOnly("E12345678", "E12345679")

            // Then the system sent only one get gss codes request
            wireMockService.verifyIerGetErosCalled(1)
        }

        @Test
        fun `Get GSS Codes By ERO ID - the system clear the cache once TTL (2 seconds) is passed`() {
            wireMockService.resetAllStubsAndMappings()
            // Given the certificate serial number "1234567891" mapped to the ERO Id "camden-city-council"
            // And the gss codes "E12345678" and "E12345679" mapped to the ERO Id
            wireMockService.stubIerApiGetErosWithGssCodes(TEST_CERTIFICATE_SERIAL_NUMBER, TEST_ERO_ID, listOf("E12345678", "E12345679"))

            // When I send a request to get the gss codes
            val gssCodesRequest1 = retrieveGssCodeService.getGssCodesFromCertificateSerial(TEST_CERTIFICATE_SERIAL_NUMBER)

            // Then I received the gss codes "E12345678" and "E12345679"
            assertThat(gssCodesRequest1).hasSize(2)
            assertThat(gssCodesRequest1).containsOnly("E12345678", "E12345679")

            // And
            logger.info { "Waiting started for $TEST_CERTIFICATE_SERIAL_NUMBER" }
            await.pollDelay(Duration.ofSeconds(3)).untilAsserted {
                assertThat(true).isEqualTo(true)
            }
            logger.info { "Waiting ended for $TEST_CERTIFICATE_SERIAL_NUMBER" }

            // When I send a request to get the gss codes
            val gssCodesRequest2 = retrieveGssCodeService.getGssCodesFromCertificateSerial(TEST_CERTIFICATE_SERIAL_NUMBER)

            // Then the system sent 2 get gss codes requests
            wireMockService.verifyIerGetErosCalled(2)

            // Then I received the gss codes "E12345678" and "E12345679"
            assertThat(gssCodesRequest2).hasSize(2)
            assertThat(gssCodesRequest2).containsOnly("E12345678", "E12345679")
        }
    }
}
