package uk.gov.dluhc.emsintegrationapi.rest.proxy

import io.awspring.cloud.messaging.core.QueueMessagingTemplate
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.dluhc.emsintegrationapi.config.ApiClient
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.config.ERO_CERTIFICATE_MAPPING_CACHE
import uk.gov.dluhc.emsintegrationapi.config.ERO_GSS_CODE_BY_ERO_ID_CACHE
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.messaging.models.EmsConfirmedReceiptMessage
import uk.gov.dluhc.emsintegrationapi.testsupport.ClearDownUtils
import uk.gov.dluhc.emsintegrationapi.testsupport.WiremockService
import uk.gov.dluhc.emsintegrationapi.testsupport.testhelpers.ProxyIntegrationTestHelpers
import java.util.concurrent.TimeUnit

class DeleteProxyVoteApplicationsIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var cacheManager: CacheManager

    @Autowired
    private lateinit var wireMockService: WiremockService

    @Autowired
    private lateinit var webClient: WebTestClient

    @Autowired
    private lateinit var apiProperties: ApiProperties

    @Autowired
    private lateinit var proxyVoteApplicationRepository: ProxyVoteApplicationRepository

    @Autowired
    private lateinit var queueMessagingTemplate: QueueMessagingTemplate

    private var apiClient: ApiClient? = null

    private var fixtures: ProxyIntegrationTestHelpers? = null

    @BeforeEach
    fun setup() {
        cacheManager.getCache(ERO_CERTIFICATE_MAPPING_CACHE)?.clear()
        cacheManager.getCache(ERO_GSS_CODE_BY_ERO_ID_CACHE)?.clear()
        ClearDownUtils.clearDownRecords(proxyRepository = proxyVoteApplicationRepository)
        apiClient = ApiClient(webClient, apiProperties)
        fixtures =
            ProxyIntegrationTestHelpers(
                wiremockService = wireMockService,
                proxyVoteApplicationRepository = proxyVoteApplicationRepository,
                queueMessagingTemplate = queueMessagingTemplate,
            )
        fixtures!!.givenEroIdAndGssCodesMapped()
    }

    @AfterEach
    fun tearDown() {
        cacheManager.getCache(ERO_CERTIFICATE_MAPPING_CACHE)?.clear()
        cacheManager.getCache(ERO_GSS_CODE_BY_ERO_ID_CACHE)?.clear()
        ClearDownUtils.clearDownRecords(proxyRepository = proxyVoteApplicationRepository)
    }

    @Test
    fun `System returns http status 403 if certificate serial number is not attached to the request`() {
        // When the EMS send a delete request to "/proxyvotes" with an application id "502cf250036469154b4f85fb" and without the certificate serial number in the request header
        val responseSpec = apiClient!!.delete("/proxyvotes/502cf250036469154b4f85fb", attachSerialNumber = false)

        // Then I received the http status 403
        responseSpec.expectStatus().isForbidden
    }

    @Test
    fun `System rejects the request with status code 400 if the application id format is invalid`() {
        // When the EMS send a delete request to "/proxyvotes" with an application id "123"
        val responseSpec = apiClient!!.delete("/proxyvotes/123", serialNumber = "1234567891")

        // Then I received the http status 400
        responseSpec.expectStatus().isBadRequest

        // And it has an error message of "The application id must match the pattern ^[a-fA-F\d]{24}$"
        val message = responseSpec.returnResult(String::class.java).responseBody.blockFirst()
        assertThat(message).isEqualTo("The application id must match the pattern ^[a-fA-F\\d]{24}$")
    }

    @Test
    fun `System returns http status 404 if a given application does not exist`() {
        // When the EMS send a delete request to "/proxyvotes" with an application id "502cf250036469154b4f85fb" and the certificate serial number "1234567891"
        val responseSpec = apiClient!!.delete("/proxyvotes/502cf250036469154b4f85fb", serialNumber = "1234567891")

        // Then I received the http status 404
        responseSpec.expectStatus().isNotFound

        // And it has an error message of "The Proxy application could not be found with id `502cf250036469154b4f85fb`"
        val message = responseSpec.returnResult(String::class.java).responseBody.blockFirst()
        assertThat(message).isEqualTo("The Proxy application could not be found with id `502cf250036469154b4f85fb`")
    }

    @Test
    fun `System returns http status 404 if the gss codes retrieved from ERO and the application gss code are different`() {
        // Given a proxy vote application with the application id "502cf250036469154b4f85fc", status "RECEIVED" and GSS Code "E12345699" exists
        fixtures!!.createProxyApplicationWithApplicationId("502cf250036469154b4f85fc", "E12345699", "RECEIVED")

        // When the EMS send a delete request to "/proxyvotes" with an application id "502cf250036469154b4f85fc" and the certificate serial number "1234567891"
        val responseSpec = apiClient!!.delete("/proxyvotes/502cf250036469154b4f85fc", serialNumber = "1234567891")

        // Then I received the http status 404
        responseSpec.expectStatus().isNotFound

        // And it has an error message of "The Proxy application could not be found with id `502cf250036469154b4f85fc`"
        val message = responseSpec.returnResult(String::class.java).responseBody.blockFirst()
        assertThat(message).isEqualTo("The Proxy application could not be found with id `502cf250036469154b4f85fc`")
    }

    @Test
    fun `System returns http status 204 on successful deletion`() {
        // Given a proxy vote application with the application id "502cf250036469154b4f85fb", status "RECEIVED" and GSS Code "E12345678" exists
        fixtures!!.createProxyApplicationWithApplicationId("502cf250036469154b4f85fb", "E12345678", "RECEIVED")

        // When the EMS send a delete request to "/proxyvotes" with an application id "502cf250036469154b4f85fb" and the certificate serial number "1234567891"
        val responseSpec = apiClient!!.delete("/proxyvotes/502cf250036469154b4f85fb", serialNumber = "1234567891")

        // Then the system updated the proxy application with the id "502cf250036469154b4f85fb" status as "DELETED"
        val proxyVoteApplication = proxyVoteApplicationRepository.findById("502cf250036469154b4f85fb").get()
        assertThat(proxyVoteApplication.status).isEqualTo(RecordStatus.valueOf("DELETED"))
        assertThat(proxyVoteApplication.updatedBy).isEqualTo(SourceSystem.EMS)

        // And the "deleted-proxy-application" queue has a SUCCESS confirmation message for the application id "502cf250036469154b4f85fb"
        await
            .pollDelay(2, TimeUnit.SECONDS)
            .untilAsserted {
                assertThat(fixtures!!.readMessage("deleted-proxy-application"))
                    .isNotNull
                    .isEqualTo(
                        EmsConfirmedReceiptMessage(
                            "502cf250036469154b4f85fb",
                            EmsConfirmedReceiptMessage.Status.SUCCESS,
                        ),
                    )
            }

        // And I received the http status 204
        responseSpec.expectStatus().isNoContent
    }

    @Test
    fun `System ignores the request if proxy vote application is already DELETED and no message will be place on queue`() {
        // Given a proxy vote application with the application id "502cf250036469154b4f85fb", status "DELETED" and GSS Code "E12345678" exists
        fixtures!!.createProxyApplicationWithApplicationId("502cf250036469154b4f85fb", "E12345678", "DELETED")

        // When the EMS send a delete request to "/proxyvotes" with an application id "502cf250036469154b4f85fb" and the certificate serial number "1234567891"
        val responseSpec = apiClient!!.delete("/proxyvotes/502cf250036469154b4f85fb", serialNumber = "1234567891")

        // Then the system ignores request and did not update the proxy application with the id "502cf250036469154b4f85fb"
        val applicationFromDB = proxyVoteApplicationRepository.findById("502cf250036469154b4f85fb").get()
        assertThat(applicationFromDB.status).isEqualTo(RecordStatus.DELETED)

        // And there will be no confirmation message on the queue "deleted-proxy-application"
        await
            .pollDelay(2, TimeUnit.SECONDS)
            .untilAsserted { assertThat(fixtures!!.readMessage("deleted-proxy-application")).isNull() }

        // And I received the http status 204
        responseSpec.expectStatus().isNoContent
    }
}
