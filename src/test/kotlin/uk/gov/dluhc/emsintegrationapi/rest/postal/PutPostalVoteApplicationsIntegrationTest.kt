package uk.gov.dluhc.emsintegrationapi.rest.postal

import com.amazonaws.services.sqs.AmazonSQSAsync
import io.awspring.cloud.messaging.core.QueueMessagingTemplate
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
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
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.messaging.models.EmsConfirmedReceiptMessage
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationResponse
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationStatus
import uk.gov.dluhc.emsintegrationapi.testsupport.ClearDownUtils
import uk.gov.dluhc.emsintegrationapi.testsupport.WiremockService
import uk.gov.dluhc.emsintegrationapi.testsupport.testhelpers.PostalIntegrationTestHelpers
import java.util.concurrent.TimeUnit

class PutPostalVoteApplicationsIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var cacheManager: CacheManager

    @Autowired
    private lateinit var wireMockService: WiremockService

    @Autowired
    private lateinit var webClient: WebTestClient

    @Autowired
    private lateinit var apiProperties: ApiProperties

    @Autowired
    private lateinit var postalVoteApplicationRepository: PostalVoteApplicationRepository

    @Autowired
    private lateinit var queueMessagingTemplate: QueueMessagingTemplate

    @Autowired
    private lateinit var amazonSQSAsync: AmazonSQSAsync

    private var apiClient: ApiClient? = null

    private var testHelpers: PostalIntegrationTestHelpers? = null

    @BeforeEach
    fun setup() {
        cacheManager.getCache(ERO_CERTIFICATE_MAPPING_CACHE)?.clear()
        cacheManager.getCache(ERO_GSS_CODE_BY_ERO_ID_CACHE)?.clear()
        ClearDownUtils
            .clearDownRecords(
                postalRepository = postalVoteApplicationRepository,
                amazonSQSAsync = amazonSQSAsync,
                queueName = "deleted-postal-application",
            )
        apiClient = ApiClient(webClient, apiProperties)
        testHelpers =
            PostalIntegrationTestHelpers(
                wiremockService = wireMockService,
                postalVoteApplicationRepository = postalVoteApplicationRepository,
                queueMessagingTemplate = queueMessagingTemplate,
            )
        testHelpers!!.givenEroIdAndGssCodesMapped()
    }

    @Test
    fun `System returns http status 403 if certificate serial number is not attached to the request`() {
        // When the EMS sends a put request to "/postalvotes" with an application id "502cf250036469154b4f85fb" and without the certificate serial number in the request header
        val responseSpec =
            apiClient!!.putEmsApplication("/postalvotes/502cf250036469154b4f85fb", attachSerialNumber = false)

        // Then I received the http status 403
        responseSpec.expectStatus().isForbidden
    }

    @Test
    fun `System rejects the request with status code 400 if the application id format is invalid`() {
        // When the EMS sends a put request to "/postalvotes" with an application id "123"
        val responseSpec = apiClient!!.putEmsApplication("/postalvotes/123", serialNumber = "1234567891")

        // Then I received the http status 400
        responseSpec.expectStatus().isBadRequest

        // And it has an error message of "The application id must match the pattern ^[a-fA-F\d]{24}$"
        val message = responseSpec.returnResult(String::class.java).responseBody.blockFirst()
        assertThat(message).isEqualTo("The application id must match the pattern ^[a-fA-F\\d]{24}$")
    }

    @Test
    fun `System returns http status 404 if a given application does not exist`() {
        // When the EMS sends a put request to "/postalvotes" with an application id "502cf250036469154b4f85aa" and certificate serial number "1234567891" and SUCCESS status
        val responseSpec =
            apiClient!!.putEmsApplication("/postalvotes/502cf250036469154b4f85fb", serialNumber = "1234567891")

        // Then I received the http status 404
        responseSpec.expectStatus().isNotFound

        // And it has an error message of "The Postal application could not be found with id `502cf250036469154b4f85aa`"
        val message = responseSpec.returnResult(String::class.java).responseBody.blockFirst()
        assertThat(message).isEqualTo("The Postal application could not be found with id `502cf250036469154b4f85fb`")
    }

    @Test
    fun `System returns http status 404 if the gss codes retrieved from ERO and the application gss code are different`() {
        // Given a postal vote application with the application id "502cf250036469154b4f85fc", status "RECEIVED" and GSS Code "E12345699" exists
        testHelpers!!.createPostalApplicationWithApplicationId("502cf250036469154b4f85fc", "E12345699", "RECEIVED")

        // When the EMS sends a put request to "/postalvotes" with an application id "502cf250036469154b4f85fc" and certificate serial number "1234567891" and SUCCESS status
        val responseSpec =
            apiClient!!.putEmsApplication("/postalvotes/502cf250036469154b4f85fc", serialNumber = "1234567891")

        // Then I received the http status 404
        responseSpec.expectStatus().isNotFound

        // And it has an error message of "The Postal application could not be found with id `502cf250036469154b4f85fc`"
        val message = responseSpec.returnResult(String::class.java).responseBody.blockFirst()
        assertThat(message).isEqualTo("The Postal application could not be found with id `502cf250036469154b4f85fc`")
    }

    @Test
    fun `System returns http status 204 on successful deletion of a success status`() {
        // Given a postal vote application with the application id "502cf250036469154b4f85fb", status "RECEIVED" and GSS Code "E12345678" exists
        testHelpers!!.createPostalApplicationWithApplicationId("502cf250036469154b4f85fb", "E12345678", "RECEIVED")

        // When the EMS sends a put request to "/postalvotes" with an application id "502cf250036469154b4f85fb" and certificate serial number "1234567891" and SUCCESS status
        val responseSpec =
            apiClient!!.putEmsApplication("/postalvotes/502cf250036469154b4f85fb", serialNumber = "1234567891")

        // Then the system updated the postal application with the id "502cf250036469154b4f85fb" status as "DELETED"
        val postalVoteApplication = postalVoteApplicationRepository.findById("502cf250036469154b4f85fb").get()
        assertThat(postalVoteApplication.status).isEqualTo(RecordStatus.valueOf("DELETED"))
        assertThat(postalVoteApplication.updatedBy).isEqualTo(SourceSystem.EMS)

        // And the "deleted-postal-application" queue has a SUCCESS confirmation message for the application id "502cf250036469154b4f85fb"
        testHelpers!!.checkQueueHasMessage(
            "deleted-postal-application",
            EmsConfirmedReceiptMessage(
                "502cf250036469154b4f85fb",
                EmsConfirmedReceiptMessage.Status.SUCCESS,
            ),
        )

        // And I received the http status 204
        responseSpec.expectStatus().isNoContent
    }

    @Test
    fun `System returns http status 204 on successful deletion of a failure status`() {
        // Given a postal vote application with the application id "502cf250036469154b4f85fb", status "RECEIVED" and GSS Code "E12345678" exists
        testHelpers!!.createPostalApplicationWithApplicationId("502cf250036469154b4f85fb", "E12345678", "RECEIVED")

        // When the EMS sends a put request to "/postalvotes" with an application id "502cf250036469154b4f85fb" and certificate serial number "1234567891" and FAILURE status
        val responseSpec =
            apiClient!!.putEmsApplication(
                "/postalvotes/502cf250036469154b4f85fb",
                serialNumber = "1234567891",
                request = EMSApplicationResponse(status = EMSApplicationStatus.FAILURE),
            )

        // Then the system updated the postal application with the id "502cf250036469154b4f85fb" status as "DELETED"
        val postalVoteApplication = postalVoteApplicationRepository.findById("502cf250036469154b4f85fb").get()
        assertThat(postalVoteApplication.status).isEqualTo(RecordStatus.valueOf("DELETED"))
        assertThat(postalVoteApplication.updatedBy).isEqualTo(SourceSystem.EMS)

        // And the "deleted-postal-application" queue has a FAILURE confirmation message for the application id "502cf250036469154b4f85fb"
        testHelpers!!.checkQueueHasMessage(
            "deleted-postal-application",
            EmsConfirmedReceiptMessage(
                "502cf250036469154b4f85fb",
                EmsConfirmedReceiptMessage.Status.FAILURE,
            ),
        )

        // And I received the http status 204
        responseSpec.expectStatus().isNoContent
    }

    @Test
    fun `System ignores the request if postal vote application is already DELETED and no message will be place on queue`() {
        // Given a postal vote application with the application id "502cf250036469154b4f85fb", status "DELETED" and GSS Code "E12345678" exists
        testHelpers!!.createPostalApplicationWithApplicationId("502cf250036469154b4f85fb", "E12345678", "DELETED")

        // When the EMS sends a put request to "/postalvotes" with an application id "502cf250036469154b4f85fb" and certificate serial number "1234567891" and SUCCESS status
        val responseSpec =
            apiClient!!.putEmsApplication("/postalvotes/502cf250036469154b4f85fb", serialNumber = "1234567891")

        // Then the system ignores request and did not update the postal application with the id "502cf250036469154b4f85fb"
        val applicationFromDB = postalVoteApplicationRepository.findById("502cf250036469154b4f85fb").get()
        assertThat(applicationFromDB.status).isEqualTo(RecordStatus.DELETED)

        // And there will be no confirmation message on the queue "deleted-postal-application"
        await
            .pollDelay(2, TimeUnit.SECONDS)
            .untilAsserted { assertThat(testHelpers!!.readMessage("deleted-postal-application")).isNull() }

        // And I received the http status 204
        responseSpec.expectStatus().isNoContent
    }
}
