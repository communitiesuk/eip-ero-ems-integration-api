package uk.gov.dluhc.emsintegrationapi.cucumber.ier

import io.cucumber.java8.En
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.springframework.cache.CacheManager
import uk.gov.dluhc.emsintegrationapi.client.IerApiClient
import uk.gov.dluhc.emsintegrationapi.config.ERO_CERTIFICATE_MAPPING_CACHE
import uk.gov.dluhc.emsintegrationapi.config.ERO_GSS_CODE_BY_ERO_ID_CACHE
import uk.gov.dluhc.emsintegrationapi.service.RetrieveGssCodeService
import uk.gov.dluhc.emsintegrationapi.testsupport.WiremockService
import uk.gov.dluhc.external.ier.models.EROCertificateMapping
import java.time.Duration

private val logger = KotlinLogging.logger { }

class IerIntegrationSteps(
    private val ierApiClient: IerApiClient,
    private val cacheManager: CacheManager,
    private val wireMockService: WiremockService,
    private val retrieveGssCodeService: RetrieveGssCodeService
) : En {
    private var certificateSerialNumber: String? = null
    private var eroId: String? = null
    private var eroCertificateMapping: EROCertificateMapping? = null
    private var gssCodes: List<String>? = null

    init {

        Before("@ClearCache", ::tearDown)
        After("@ClearCache", ::tearDown)

        Given("the certificate serial number {string} mapped to the ERO Id {string}") { certificateSerialNumber: String, eroId: String ->
            this.certificateSerialNumber = certificateSerialNumber
            this.eroId = eroId
            wireMockService.stubIerApiGetEroIdentifier(certificateSerialNumber, eroId)
        }
        When("I send a request to get the mapping by serial number") {
            eroCertificateMapping = ierApiClient.getEroIdentifier(certificateSerialNumber!!)
            logger.info { "Certificate Mapping received for $certificateSerialNumber" }
        }
        Then("I received mapping response with the ERO Id {string}") { expectedEroId: String ->
            assertThat(eroCertificateMapping).isNotNull.extracting("eroId").isEqualTo(expectedEroId)
        }
        And("I waited for {long} seconds") { delayInSeconds: Long ->
            logger.info { "Waiting started for $certificateSerialNumber" }
            await.pollDelay(Duration.ofSeconds(delayInSeconds)).untilAsserted {
                assertThat(true).isEqualTo(true)
            }
            logger.info { "Waiting ended for $certificateSerialNumber" }
        }
        Then("the system sent only one get mapping request", wireMockService::verifyIerGetEroIdentifierCalledOnce)
        Then("the system sent {int} get mapping requests") { times: Int ->
            wireMockService.verifyIerGetEroIdentifierCalled(times)
        }
        Then("the system sent a request to get the mapping") {
            wireMockService.verifyWiremockGetInvokedFor(certificateSerialNumber!!)
        }
        Given("the gss codes {string} and {string} mapped to the ERO Id") { gssCode1: String, gssCode2: String ->
            wireMockService.stubEroManagementGetEro(eroId!!, gssCode1, gssCode2)
        }
        And("I send a request to get the gss codes") {
            gssCodes = retrieveGssCodeService.getGssCodeFromCertificateSerial(certificateSerialNumber!!)
        }
        Then("I received the gss codes {string} and {string}") { gssCode1: String, gssCode2: String ->
            assertThat(gssCodes).hasSize(2)
            assertThat(gssCodes).containsOnly(gssCode1, gssCode2)
        }
        Given("the certificate serial {string} does not exist in ERO") { invalidSerialNumber: String ->
            wireMockService.stubIerApiGetEroIdentifierThrowsNotFoundError(invalidSerialNumber)
        }
        Given("the ERO could not process the get mapping request for {string}") { validSerialNumber: String ->
            wireMockService.stubIerApiGetEroIdentifierThrowsInternalServerError(validSerialNumber)
        }
        Then(
            "the system sent only one get gss codes request",
            wireMockService::verifyEroManagementGetEroIdentifierCalledOnce
        )
        Then("the system sent {int} get gss codes requests") { times: Int ->
            wireMockService.verifyEroManagementGetEroIdentifierCalled(times)
        }
        Given("the ERO Id {string} does not exist in ERO") { eroId: String ->
            wireMockService.stubEroManagementGetEroThrowsNotFoundError(eroId)
        }
        Given("the ERO could not process the get gss codes request for {string}") { eroId: String ->
            wireMockService.stubEroManagementGetEroThrowsInternalServerError(eroId)
        }
    }

    private fun tearDown() {
        cacheManager.getCache(ERO_CERTIFICATE_MAPPING_CACHE)?.clear()
        cacheManager.getCache(ERO_GSS_CODE_BY_ERO_ID_CACHE)?.clear()
        wireMockService.resetAllStubsAndMappings()
    }
}
