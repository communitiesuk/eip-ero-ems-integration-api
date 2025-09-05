package uk.gov.dluhc.emsintegrationapi.rest.postal

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
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.messaging.models.EmsConfirmedReceiptMessage
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationResponse
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationStatus
import uk.gov.dluhc.emsintegrationapi.testsupport.ClearDownUtils
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.RestTestUtils.APPLICATION_ID_1
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.RestTestUtils.APPLICATION_ID_2
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.RestTestUtils.ERO_ID_1_CERTIFICATE_SERIAL
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.RestTestUtils.ERO_ID_1_GSS_CODE_1
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.RestTestUtils.OTHER_ERO_GSS_CODE
import uk.gov.dluhc.emsintegrationapi.testsupport.testhelpers.PostalIntegrationTestHelpers
import uk.gov.dluhc.registercheckerapi.models.ErrorResponse
import java.util.concurrent.TimeUnit

internal class PostPostalVoteApplicationsIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var webClient: WebTestClient

    @Autowired
    private lateinit var apiProperties: ApiProperties

    @Autowired
    private lateinit var postalVoteApplicationRepository: PostalVoteApplicationRepository

    private var apiClient: ApiClient? = null

    private var testHelpers: PostalIntegrationTestHelpers? = null

    @BeforeEach
    fun setup() {
        cacheManager.getCache(ERO_CERTIFICATE_MAPPING_CACHE)?.clear()
        cacheManager.getCache(ERO_GSS_CODE_BY_ERO_ID_CACHE)?.clear()
        ClearDownUtils.clearDownRecords(
            postalRepository = postalVoteApplicationRepository,
            registerCheckResultDataRepository = registerCheckResultDataRepository,
            sqsAsyncClient = sqsAsyncClient,
            queueName = emsApplicationProcessedQueueName,
        )
        apiClient = ApiClient(webClient, apiProperties)
        testHelpers =
            PostalIntegrationTestHelpers(
                wiremockService = wireMockService,
                postalVoteApplicationRepository = postalVoteApplicationRepository,
                queueMessagingTemplate = sqsMessagingTemplate,
            )
        // Map ERO_ID_1_CERTIFICATE_SERIAL to ERO_ID_1 with gss codes ERO_ID_1_GSS_CODE_1 and ERO_ID_1_GSS_CODE_2
        testHelpers!!.givenEroIdAndGssCodesMapped()
    }

    @Test
    fun `System returns http status 403 if certificate serial number is not attached to the request`() {
        // When the EMS sends a post request to "/postalvotes" with an application id APPLICATION_ID_1 and without the certificate serial number in the request header
        val responseSpec =
            apiClient!!.postEmsApplication("/postalvotes/$APPLICATION_ID_1", attachSerialNumber = false)

        // Then I received the http status 403
        responseSpec.expectStatus().isForbidden
    }

    @Test
    fun `System rejects the request with status code 400 if the application id format is invalid`() {
        // When the EMS sends a post request to "/postalvotes" with an application id "123"
        val responseSpec = apiClient!!.postEmsApplication("/postalvotes/123", serialNumber = ERO_ID_1_CERTIFICATE_SERIAL)

        // Then I received the http status 400
        responseSpec.expectStatus().isBadRequest

        // And it has an error message of "emsAcceptedByPost.applicationId: The application id must match the pattern ^[a-fA-F\d]{24}$"
        val message = responseSpec.returnResult(ErrorResponse::class.java).responseBody.blockFirst()
        assertThat(
            message!!.message,
        ).isEqualTo("emsAcceptedByPost.applicationId: The application id must match the pattern ^[a-fA-F\\d]{24}$")
    }

    @Test
    fun `System returns http status 404 if a given application does not exist`() {
        // When the EMS sends a post request to "/postalvotes" with an application id APPLICATION_ID_3 and certificate serial number ERO_ID_1_CERTIFICATE_SERIAL and SUCCESS status
        val responseSpec =
            apiClient!!.postEmsApplication("/postalvotes/$APPLICATION_ID_1", serialNumber = ERO_ID_1_CERTIFICATE_SERIAL)

        // Then I received the http status 404
        responseSpec.expectStatus().isNotFound

        // And it has an error message of "The Postal application could not be found with id `502cf250036469154b4f85aa`"
        val message = responseSpec.returnResult(ErrorResponse::class.java).responseBody.blockFirst()
        assertThat(
            message!!.message,
        ).isEqualTo("The Postal application could not be found with id `$APPLICATION_ID_1`")
    }

    @Test
    fun `System returns http status 404 if the gss codes retrieved from ERO and the application gss code are different`() {
        // Given a postal vote application with the application id APPLICATION_ID_2, status "RECEIVED" and GSS Code OTHER_ERO_GSS_CODE exists
        testHelpers!!.createPostalApplicationWithApplicationId(APPLICATION_ID_2, OTHER_ERO_GSS_CODE, "RECEIVED")

        // When the EMS sends a post request to "/postalvotes" with an application id APPLICATION_ID_2 and certificate serial number ERO_ID_1_CERTIFICATE_SERIAL and SUCCESS status
        val responseSpec =
            apiClient!!.postEmsApplication("/postalvotes/$APPLICATION_ID_2", serialNumber = ERO_ID_1_CERTIFICATE_SERIAL)

        // Then I received the http status 404
        responseSpec.expectStatus().isNotFound

        // And it has an error message of "The Postal application could not be found with id `APPLICATION_ID_2`"
        val message = responseSpec.returnResult(ErrorResponse::class.java).responseBody.blockFirst()
        assertThat(
            message!!.message,
        ).isEqualTo("The Postal application could not be found with id `$APPLICATION_ID_2`")
    }

    @Test
    fun `System returns http status 204 on successful deletion of a success status`() {
        // Given a postal vote application with the application id APPLICATION_ID_1, status "RECEIVED" and GSS Code ERO_ID_1_GSS_CODE_1 exists
        testHelpers!!.createPostalApplicationWithApplicationId(APPLICATION_ID_1, ERO_ID_1_GSS_CODE_1, "RECEIVED")

        // When the EMS sends a post request to "/postalvotes" with an application id APPLICATION_ID_1 and certificate serial number ERO_ID_1_CERTIFICATE_SERIAL and SUCCESS status
        val responseSpec =
            apiClient!!.postEmsApplication("/postalvotes/$APPLICATION_ID_1", serialNumber = ERO_ID_1_CERTIFICATE_SERIAL)

        // Then the system updated the postal application with the id APPLICATION_ID_1 status as "DELETED"
        val postalVoteApplication = postalVoteApplicationRepository.findById(APPLICATION_ID_1).get()
        assertThat(postalVoteApplication.status).isEqualTo(RecordStatus.valueOf("DELETED"))
        assertThat(postalVoteApplication.updatedBy).isEqualTo(SourceSystem.EMS)

        // And the "deleted-postal-application" queue has a SUCCESS confirmation message for the application id APPLICATION_ID_1
        testHelpers!!.checkQueueHasMessage(
            emsApplicationProcessedQueueName,
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
        // Given a postal vote application with the application id APPLICATION_ID_1, status "RECEIVED" and GSS Code ERO_ID_1_GSS_CODE_1 exists
        testHelpers!!.createPostalApplicationWithApplicationId(APPLICATION_ID_1, ERO_ID_1_GSS_CODE_1, "RECEIVED")

        // When the EMS sends a post request to "/postalvotes" with an application id APPLICATION_ID_1 and certificate serial number ERO_ID_1_CERTIFICATE_SERIAL and FAILURE status
        val responseSpec =
            apiClient!!.postEmsApplication(
                "/postalvotes/$APPLICATION_ID_1",
                serialNumber = ERO_ID_1_CERTIFICATE_SERIAL,
                request = EMSApplicationResponse(status = EMSApplicationStatus.FAILURE),
            )

        // Then the system updated the postal application with the id APPLICATION_ID_1 status as "DELETED"
        val postalVoteApplication = postalVoteApplicationRepository.findById(APPLICATION_ID_1).get()
        assertThat(postalVoteApplication.status).isEqualTo(RecordStatus.valueOf("DELETED"))
        assertThat(postalVoteApplication.updatedBy).isEqualTo(SourceSystem.EMS)

        // And the "deleted-postal-application" queue has a FAILURE confirmation message for the application id APPLICATION_ID_1
        testHelpers!!.checkQueueHasMessage(
            emsApplicationProcessedQueueName,
            EmsConfirmedReceiptMessage(
                APPLICATION_ID_1,
                EmsConfirmedReceiptMessage.Status.FAILURE,
            ),
        )

        // And I received the http status 204
        responseSpec.expectStatus().isNoContent
    }

    @Test
    fun `System ignores the request if postal vote application is already DELETED and no message will be place on queue`() {
        // Given a postal vote application with the application id APPLICATION_ID_1, status "DELETED" and GSS Code ERO_ID_1_GSS_CODE_1 exists
        testHelpers!!.createPostalApplicationWithApplicationId(APPLICATION_ID_1, ERO_ID_1_GSS_CODE_1, "DELETED")

        // When the EMS sends a post request to "/postalvotes" with an application id APPLICATION_ID_1 and certificate serial number ERO_ID_1_CERTIFICATE_SERIAL and SUCCESS status
        val responseSpec =
            apiClient!!.postEmsApplication("/postalvotes/$APPLICATION_ID_1", serialNumber = ERO_ID_1_CERTIFICATE_SERIAL)

        // Then the system ignores request and did not update the postal application with the id APPLICATION_ID_1
        val applicationFromDB = postalVoteApplicationRepository.findById(APPLICATION_ID_1).get()
        assertThat(applicationFromDB.status).isEqualTo(RecordStatus.DELETED)

        // And there will be no confirmation message on the queue "deleted-postal-application"
        await
            .pollDelay(2, TimeUnit.SECONDS)
            .timeout(20L, TimeUnit.SECONDS)
            .untilAsserted { assertThat(testHelpers!!.readMessage(emsApplicationProcessedQueueName)).isNull() }

        // And I received the http status 204
        responseSpec.expectStatus().isNoContent
    }
}
