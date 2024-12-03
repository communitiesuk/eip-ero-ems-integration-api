package uk.gov.dluhc.emsintegrationapi.client

import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import uk.gov.dluhc.emsintegrationapi.config.ERO_CERTIFICATE_MAPPING_CACHE
import uk.gov.dluhc.emsintegrationapi.config.ERO_GSS_CODE_BY_ERO_ID_CACHE
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest
import uk.gov.dluhc.emsintegrationapi.service.RetrieveGssCodeService
import uk.gov.dluhc.emsintegrationapi.testsupport.WiremockService
import uk.gov.dluhc.external.ier.models.EROCertificateMapping
import java.time.Duration

private val logger = KotlinLogging.logger { }

class IerClientIntegrationTest : IntegrationTest() {

    @Autowired
    private lateinit var ierApiClient: IerApiClient

    @Autowired
    private lateinit var cacheManager: CacheManager

    @Autowired
    private lateinit var wireMockService: WiremockService

    @Autowired
    private lateinit var retrieveGssCodeService: RetrieveGssCodeService

    private var eroCertificateMapping: EROCertificateMapping? = null
    private val certificateSerialNumber1 = "1234567891"
    private val eroId1 = "camden-city-council"
    private val certificateSerialNumber2 = "1234567892"
    private val eroId2 = "bristol-city-council"

    @BeforeEach
    fun beforeEachTest() {
        tearDown()
    }

    @AfterEach
    fun afterEachTest() {
        tearDown()
    }

    @Nested
    inner class GetEroCertificate {

        @Test
        fun `Get ERO certificate mapping by certificate serial number `() {
            // Given
            wireMockService.stubIerApiGetEroIdentifier(certificateSerialNumber1, eroId1)

            // When
            eroCertificateMapping = ierApiClient.getEroIdentifier(certificateSerialNumber1)
            logger.info { "Certificate Mapping received for $certificateSerialNumber1" }

            // Then
            wireMockService.verifyWiremockGetInvokedFor(certificateSerialNumber1)

            // And
            assertThat(eroCertificateMapping).isNotNull.extracting("eroId").isEqualTo(eroId1)
        }

        @Test
        fun `Get ERO certificate mapping by certificate serial number - the system caches the certificate mapping response from IER`() {
            // Given
            wireMockService.stubIerApiGetEroIdentifier(certificateSerialNumber1, eroId1)

            // When
            val eroCertificateMapping = ierApiClient.getEroIdentifier(certificateSerialNumber1)
            logger.info { "Certificate Mapping received for $certificateSerialNumber1" }

            // And
            assertThat(eroCertificateMapping).isNotNull.extracting("eroId").isEqualTo(eroId1)

            // When
            logger.info { "Waiting started for $certificateSerialNumber1" }
            await.pollDelay(Duration.ofSeconds(3)).untilAsserted {
                assertThat(true).isEqualTo(true)
            }
            logger.info { "Waiting ended for $certificateSerialNumber1" }

            // Then
            wireMockService::verifyIerGetEroIdentifierCalledOnce

            // And
            assertThat(eroCertificateMapping).isNotNull.extracting("eroId").isEqualTo(eroId1)
        }

        @Test
        fun `Get ERO certificate mapping by certificate serial number - the system clear the cache once TTL (2 seconds) is passed`() {

            // Given
            wireMockService.stubIerApiGetEroIdentifier(certificateSerialNumber2, eroId2)

            // When
            eroCertificateMapping = ierApiClient.getEroIdentifier(certificateSerialNumber2)
            logger.info { "Certificate Mapping received for $certificateSerialNumber2" }

            // And
            logger.info { "Waiting started for $certificateSerialNumber2" }
            await.pollDelay(Duration.ofSeconds(3)).untilAsserted {
                assertThat(true).isEqualTo(true)
            }
            logger.info { "Waiting ended for $certificateSerialNumber2" }

            // When
            eroCertificateMapping = ierApiClient.getEroIdentifier(certificateSerialNumber2)
            logger.info { "Certificate Mapping received for $certificateSerialNumber2" }

            // And
            assertThat(eroCertificateMapping).isNotNull.extracting("eroId").isEqualTo(eroId2)

            // Then
            wireMockService.verifyIerGetEroIdentifierCalled(2)
        }
    }

    @Nested
    inner class GetGssCode {

        @Test
        fun `Get GSS Codes By ERO ID`() {
            // Given the certificate serial number "1234567891" mapped to the ERO Id "camden-city-council"
            wireMockService.stubIerApiGetEroIdentifier(certificateSerialNumber1, eroId1)

            // And the gss codes "E12345678" and "E12345679" mapped to the ERO Id
            wireMockService.stubEroManagementGetEro(eroId1, "E12345678", "E12345679")

            // When I send a request to get the gss codes
            val gssCodes = retrieveGssCodeService.getGssCodeFromCertificateSerial(certificateSerialNumber1)

            // Then I received the gss codes "E12345678" and "E12345679"
            assertThat(gssCodes).hasSize(2)
            assertThat(gssCodes).containsOnly("E12345678", "E12345679")
        }

        @Test
        fun `Get GSS Codes By ERO ID  - the system caches get gss code response`() {
            // Given the certificate serial number "1234567891" mapped to the ERO Id "camden-city-council"
            wireMockService.stubIerApiGetEroIdentifier(certificateSerialNumber1, eroId1)

            // And the gss codes "E12345678" and "E12345679" mapped to the ERO Id
            wireMockService.stubEroManagementGetEro(eroId1, "E12345678", "E12345679")

            // When I send a request to get the gss codes
            val gssCodes = retrieveGssCodeService.getGssCodeFromCertificateSerial(certificateSerialNumber1)

            // Then I received the gss codes "E12345678" and "E12345679"
            assertThat(gssCodes).hasSize(2)
            assertThat(gssCodes).containsOnly("E12345678", "E12345679")

            // Then the system sent only one get gss codes request
            wireMockService::verifyEroManagementGetEroIdentifierCalledOnce
        }

        @Test
        fun `Get GSS Codes By ERO ID - the system clear the cache once TTL (2 seconds) is passed`() {
            // Given the certificate serial number "1234567891" mapped to the ERO Id "camden-city-council"
            wireMockService.stubIerApiGetEroIdentifier(certificateSerialNumber1, eroId1)

            // And the gss codes "E12345678" and "E12345679" mapped to the ERO Id
            wireMockService.stubEroManagementGetEro(eroId1, "E12345678", "E12345679")

            // When I send a request to get the gss codes
            val gssCodesRequest1 = retrieveGssCodeService.getGssCodeFromCertificateSerial(certificateSerialNumber1)

            // Then I received the gss codes "E12345678" and "E12345679"
            assertThat(gssCodesRequest1).hasSize(2)
            assertThat(gssCodesRequest1).containsOnly("E12345678", "E12345679")

            // And
            logger.info { "Waiting started for $certificateSerialNumber1" }
            await.pollDelay(Duration.ofSeconds(3)).untilAsserted {
                assertThat(true).isEqualTo(true)
            }
            logger.info { "Waiting ended for $certificateSerialNumber1" }

            // When I send a request to get the gss codes
            val gssCodesRequest2 = retrieveGssCodeService.getGssCodeFromCertificateSerial(certificateSerialNumber1)

            // Then the system sent 2 get gss codes requests
            wireMockService.verifyEroManagementGetEroIdentifierCalled(2)

            // Then I received the gss codes "E12345678" and "E12345679"
            assertThat(gssCodesRequest2).hasSize(2)
            assertThat(gssCodesRequest2).containsOnly("E12345678", "E12345679")
        }
    }

    private fun tearDown() {
        cacheManager.getCache(ERO_CERTIFICATE_MAPPING_CACHE)?.clear()
        cacheManager.getCache(ERO_GSS_CODE_BY_ERO_ID_CACHE)?.clear()
        wireMockService.resetAllStubsAndMappings()
    }
}
