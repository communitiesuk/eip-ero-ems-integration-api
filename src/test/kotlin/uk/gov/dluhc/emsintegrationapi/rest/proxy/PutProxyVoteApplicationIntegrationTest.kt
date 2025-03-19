package uk.gov.dluhc.emsintegrationapi.rest.proxy

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
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
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationResponse
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationStatus
import uk.gov.dluhc.emsintegrationapi.testsupport.ClearDownUtils
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.RestTestUtils.APPLICATION_ID_1
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.RestTestUtils.APPLICATION_ID_2
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.RestTestUtils.APPLICATION_ID_3
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.RestTestUtils.CERTIFICATE_SERIAL_NUM_1
import uk.gov.dluhc.emsintegrationapi.testsupport.testhelpers.ProxyIntegrationTestHelpers
import uk.gov.dluhc.registercheckerapi.models.ErrorResponse
import java.util.concurrent.TimeUnit

internal class PutProxyVoteApplicationIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var webClient: WebTestClient

    @Autowired
    private lateinit var apiProperties: ApiProperties

    @Autowired
    private lateinit var proxyApplicationRepository: ProxyVoteApplicationRepository

    private var apiClient: ApiClient? = null

    private var fixtures: ProxyIntegrationTestHelpers? = null

    @BeforeEach
    fun setup() {
        cacheManager.getCache(ERO_CERTIFICATE_MAPPING_CACHE)?.clear()
        cacheManager.getCache(ERO_GSS_CODE_BY_ERO_ID_CACHE)?.clear()
        ClearDownUtils.clearDownRecords(proxyRepository = proxyApplicationRepository)
        apiClient = ApiClient(webClient, apiProperties)
        fixtures =
            ProxyIntegrationTestHelpers(
                wiremockService = wireMockService,
                proxyVoteApplicationRepository = proxyApplicationRepository,
                queueMessagingTemplate = sqsMessagingTemplate,
            )
        fixtures!!.givenEroIdAndGssCodesMapped()
    }

    @Test
    fun `System returns http status 403 if certificate serial number is not attached to the request`() {
        // When the EMS sends a put request to "/proxyvotes" with an application id APPLICATION_ID_1 and without the certificate serial number in the request header
        val responseSpec =
            apiClient!!.putEmsApplication("/proxyvotes/$APPLICATION_ID_1", attachSerialNumber = false)

        // Then I received the http status 403
        responseSpec.expectStatus().isForbidden
    }

    @Test
    fun `System rejects the request with status code 400 if the application id format is invalid`() {
        // When the EMS sends a put request to "/proxyvotes" with an application id "123"
        val responseSpec = apiClient!!.putEmsApplication("/proxyvotes/123", serialNumber = CERTIFICATE_SERIAL_NUM_1)

        // Then I received the http status 400
        responseSpec.expectStatus().isBadRequest

        // And it has an error message of "emsAccepted.applicationId: The application id must match the pattern ^[a-fA-F\d]{24}$"
        val errorResponse = responseSpec.returnResult(ErrorResponse::class.java).responseBody.blockFirst()
        assertThat(
            errorResponse!!.message,
        ).isEqualTo("emsAccepted.applicationId: The application id must match the pattern ^[a-fA-F\\d]{24}$")
    }

    @Test
    fun `System returns http status 404 if a given application does not exist`() {
        // When the EMS sends a put request to "/proxyvotes" with an application id APPLICATION_ID_3 and certificate serial number CERTIFICATE_SERIAL_NUM_1 and SUCCESS status
        val responseSpec =
            apiClient!!.putEmsApplication("/proxyvotes/$APPLICATION_ID_3", serialNumber = CERTIFICATE_SERIAL_NUM_1)

        // Then I received the http status 404
        responseSpec.expectStatus().isNotFound

        // And it has an error message of "The Proxy application could not be found with id `APPLICATION_ID_3`"
        val errorResponse = responseSpec.returnResult(ErrorResponse::class.java).responseBody.blockFirst()
        assertThat(errorResponse!!.message).isEqualTo("The Proxy application could not be found with id `$APPLICATION_ID_3`")
    }

    @Test
    fun `System returns http status 404 if the gss codes retrieved from ERO and the application gss code are different`() {
        // Given a proxy vote application with the application id APPLICATION_ID_2, status "RECEIVED" and GSS Code "E12345699" exists
        fixtures!!.createProxyApplicationWithApplicationId(APPLICATION_ID_2, "E12345699", "RECEIVED")

        // When the EMS sends a put request to "/proxyvotes" with an application id APPLICATION_ID_2 and certificate serial number CERTIFICATE_SERIAL_NUM_1 and SUCCESS status
        val responseSpec =
            apiClient!!.putEmsApplication("/proxyvotes/$APPLICATION_ID_2", serialNumber = CERTIFICATE_SERIAL_NUM_1)

        // Then I received the http status 404
        responseSpec.expectStatus().isNotFound

        // And it has an error message of "The Proxy application could not be found with id `APPLICATION_ID_2`"
        val errorResponse = responseSpec.returnResult(ErrorResponse::class.java).responseBody.blockFirst()
        assertThat(errorResponse!!.message).isEqualTo("The Proxy application could not be found with id `$APPLICATION_ID_2`")
    }

    @Test
    fun `System returns http status 204 on successful deletion of a success status`() {
        // Given a proxy vote application with the application id APPLICATION_ID_1, status "RECEIVED" and GSS Code "E12345678" exists
        fixtures!!.createProxyApplicationWithApplicationId(APPLICATION_ID_1, "E12345678", "RECEIVED")

        // When the EMS sends a put request to "/proxyvotes" with an application id APPLICATION_ID_1 and certificate serial number CERTIFICATE_SERIAL_NUM_1 and SUCCESS status
        val responseSpec =
            apiClient!!.putEmsApplication("/proxyvotes/$APPLICATION_ID_1", serialNumber = CERTIFICATE_SERIAL_NUM_1)

        // Then the system updated the proxy application with the id APPLICATION_ID_1 status as "DELETED"
        val proxyVoteApplication = proxyApplicationRepository.findById(APPLICATION_ID_1).get()
        assertThat(proxyVoteApplication.status).isEqualTo(RecordStatus.valueOf("DELETED"))
        assertThat(proxyVoteApplication.updatedBy).isEqualTo(SourceSystem.EMS)

        // And the "deleted-proxy-application" queue has a SUCCESS confirmation message for the application id APPLICATION_ID_1
        fixtures!!.checkQueueHasMessage(
            "deleted-proxy-application",
            EmsConfirmedReceiptMessage(
                APPLICATION_ID_1,
                EmsConfirmedReceiptMessage.Status.SUCCESS,
            ),
        )

        // And I received the http status 204
        responseSpec.expectStatus().isNoContent
    }

    @Test
    fun `System returns http status 204 on successful deletion of a failure status`() {
        // Given a proxy vote application with the application id APPLICATION_ID_1, status "RECEIVED" and GSS Code "E12345678" exists
        fixtures!!.createProxyApplicationWithApplicationId(APPLICATION_ID_1, "E12345678", "RECEIVED")

        // When the EMS sends a put request to "/proxyvotes" with an application id APPLICATION_ID_1 and certificate serial number CERTIFICATE_SERIAL_NUM_1 and FAILURE status
        val responseSpec =
            apiClient!!.putEmsApplication(
                "/proxyvotes/$APPLICATION_ID_1",
                serialNumber = CERTIFICATE_SERIAL_NUM_1,
                request = EMSApplicationResponse(status = EMSApplicationStatus.FAILURE),
            )

        // Then the system updated the proxy application with the id APPLICATION_ID_1 status as "DELETED"
        val proxyVoteApplication = proxyApplicationRepository.findById(APPLICATION_ID_1).get()
        assertThat(proxyVoteApplication.status).isEqualTo(RecordStatus.valueOf("DELETED"))
        assertThat(proxyVoteApplication.updatedBy).isEqualTo(SourceSystem.EMS)

        // And the "deleted-proxy-application" queue has a FAILURE confirmation message for the application id APPLICATION_ID_1
        fixtures!!.checkQueueHasMessage(
            "deleted-proxy-application",
            EmsConfirmedReceiptMessage(
                APPLICATION_ID_1,
                EmsConfirmedReceiptMessage.Status.FAILURE,
            ),
        )

        // And I received the http status 204
        responseSpec.expectStatus().isNoContent
    }

    @Test
    fun `System ignores the request if proxy vote application is already DELETED and no message will be place on queue`() {
        // Given a proxy vote application with the application id APPLICATION_ID_1, status "DELETED" and GSS Code "E12345678" exists
        fixtures!!.createProxyApplicationWithApplicationId(APPLICATION_ID_1, "E12345678", "DELETED")

        // When the EMS sends a put request to "/proxyvotes" with an application id APPLICATION_ID_1 and certificate serial number CERTIFICATE_SERIAL_NUM_1 and SUCCESS status
        val responseSpec =
            apiClient!!.putEmsApplication("/proxyvotes/$APPLICATION_ID_1", serialNumber = CERTIFICATE_SERIAL_NUM_1)

        // Then the system ignores request and did not update the proxy application with the id APPLICATION_ID_1
        val applicationFromDB = proxyApplicationRepository.findById(APPLICATION_ID_1).get()
        assertThat(applicationFromDB.status).isEqualTo(RecordStatus.DELETED)

        // And there will be no confirmation message on the queue "deleted-proxy-application"
        await
            .pollDelay(2, TimeUnit.SECONDS)
            .timeout(20L, TimeUnit.SECONDS)
            .untilAsserted { assertThat(fixtures!!.readMessage("deleted-proxy-application")).isNull() }

        // And I received the http status 204
        responseSpec.expectStatus().isNoContent
    }
}
