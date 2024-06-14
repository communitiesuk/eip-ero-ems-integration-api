package uk.gov.dluhc.emsintegrationapi.logging

import ch.qos.logback.classic.Level
import com.amazonaws.services.sqs.AmazonSQSAsync
import io.awspring.cloud.messaging.core.QueueMessagingTemplate
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties
import uk.gov.dluhc.emsintegrationapi.config.CORRELATION_ID
import uk.gov.dluhc.emsintegrationapi.config.CORRELATION_ID_HEADER
import uk.gov.dluhc.emsintegrationapi.config.ERO_CERTIFICATE_MAPPING_CACHE
import uk.gov.dluhc.emsintegrationapi.config.ERO_GSS_CODE_BY_ERO_ID_CACHE
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.Companion.deleteRecords
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.Companion.deleteSqsMessage
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.TestPhase
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.testsupport.TestLogAppender
import uk.gov.dluhc.emsintegrationapi.testsupport.TestLogAppender.Companion.logs
import uk.gov.dluhc.emsintegrationapi.testsupport.WiremockService
import uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions.ILoggingEventAssert.Companion.assertThat
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplicationMessage
import java.util.UUID.randomUUID
import java.util.concurrent.TimeUnit

/**
 * Integration tests that assert the correlation ID is correctly applied to log statements via the Interceptors and Aspects
 * in CorrelationIdMdcConfiguration
 *
 * The tests in this class assert the cross-cutting logging behaviour. They do not assert the behaviour or output of any
 * bean or code that is used to test.
 *
 * These tests do not include all controllers, listeners e.t.c. We simply test one of each to prove the mdc logging is in place.
 */
internal class CorrelationIdMdcIntegrationTest : IntegrationTest() {
    @Autowired
    protected lateinit var sqsMessagingTemplate: QueueMessagingTemplate

    @Autowired
    protected lateinit var postalVoteApplicationRepository: PostalVoteApplicationRepository

    @Value("\${sqs.postal-application-queue-name}")
    protected lateinit var postalApplicationQueueName: String

    @Autowired
    protected lateinit var amazonSQSAsync: AmazonSQSAsync

    @Autowired
    protected lateinit var wireMockService: WiremockService

    @Autowired
    protected lateinit var webClient: WebTestClient

    @Autowired
    protected lateinit var apiProperties: ApiProperties

    @Autowired
    protected lateinit var cacheManager: CacheManager

    @AfterEach
    fun tearDown() {
        TestLogAppender.reset()
        cacheManager.getCache(ERO_CERTIFICATE_MAPPING_CACHE)?.clear()
        cacheManager.getCache(ERO_GSS_CODE_BY_ERO_ID_CACHE)?.clear()
    }

    @Nested
    inner class PostalVoteApplicationRequest {

        private val PATH = "/postalvotes"
        private val CERTIFICATE_SERIAL_NUMBER = "543219999"
        private val ERO_ID = "test-ero"

        @BeforeEach
        fun setupWiremockStubs() {
            wireMockService.stubIerApiGetEroIdentifier(CERTIFICATE_SERIAL_NUMBER, ERO_ID)
            wireMockService.stubEroManagementGetEro(ERO_ID)
        }

        @Test
        fun `should service REST API call given no correlation-id header`() {
            // When
            webClient.get().uri(PATH)
                .header(apiProperties.requestHeaderName, CERTIFICATE_SERIAL_NUMBER)
                .exchange()

            // Then
            await.atMost(3, TimeUnit.SECONDS).untilAsserted {
                val firstReliableMessage = TestLogAppender.getLogEventMatchingRegex(
                    "Processing a get postal vote applications request with page size =null and certificate serial no =$CERTIFICATE_SERIAL_NUMBER",
                    Level.INFO
                )
                assertThat(firstReliableMessage).hasAnyCorrelationId()
                val correlationId = firstReliableMessage!!.mdcPropertyMap[CORRELATION_ID]!!

                val filteredLogs = logs.filter { it.mdcPropertyMap.isNotEmpty() }
                // wait until sufficient messages are logged before verifying all share same correlation ID
                assertThat(filteredLogs).hasSizeGreaterThanOrEqualTo(3)

                filteredLogs.forEach {
                    assertThat(it).`as`("Message: ${it.message}").hasCorrelationId(correlationId)
                }
                wireMockService.verifyIerApiRequestWithCorrelationId(correlationId)
                wireMockService.verifyEroManagementApiRequestWithCorrelationId(correlationId)
            }
        }

        @Test
        fun `should service REST API call given correlation-id header`() {
            // When
            val expectedCorrelationId = randomUUID().toString().replace("-", "")
            webClient.get().uri(PATH)
                .header(apiProperties.requestHeaderName, CERTIFICATE_SERIAL_NUMBER)
                .header(CORRELATION_ID_HEADER, expectedCorrelationId)
                .exchange()

            // Then
            await.atMost(3, TimeUnit.SECONDS).untilAsserted {
                assertThat(
                    TestLogAppender.getLogEventMatchingRegex(
                        "Processing a get postal vote applications request with page size =null and certificate serial no =$CERTIFICATE_SERIAL_NUMBER",
                        Level.INFO
                    )
                ).hasCorrelationId(expectedCorrelationId)

                val filteredLogs = logs.filter { it.mdcPropertyMap.isNotEmpty() }
                // wait until sufficient messages are logged before verifying all share same correlation ID
                assertThat(filteredLogs).hasSizeGreaterThanOrEqualTo(3)

                filteredLogs.forEach {
                    assertThat(it).`as`("Message: ${it.message}").hasCorrelationId(expectedCorrelationId)
                }
                wireMockService.verifyIerApiRequestWithCorrelationId(expectedCorrelationId)
                wireMockService.verifyEroManagementApiRequestWithCorrelationId(expectedCorrelationId)
            }
        }
    }

    @Nested
    inner class PostalVoteApplicationSqs {

        @BeforeEach
        fun deletePostalRecordsBefore() {
            deleteRecords(postalVoteApplicationRepository, TestPhase.BEFORE)
            deleteSqsMessage(amazonSQSAsync, postalApplicationQueueName, TestPhase.BEFORE)
        }

        @AfterEach
        fun deletePostalRecordsAfter() {
            deleteRecords(postalVoteApplicationRepository, TestPhase.AFTER)
            deleteSqsMessage(amazonSQSAsync, postalApplicationQueueName, TestPhase.AFTER)
        }

        @Test
        fun `should log consistent correlation id upon application creation`() {
            val payload = buildPostalVoteApplicationMessage()

            val expectedCorrelationId = randomUUID().toString().replace("-", "")

            sqsMessagingTemplate.convertAndSend(
                postalApplicationQueueName,
                payload,
                mapOf("x-correlation-id" to expectedCorrelationId)
            )

            await.atMost(10, TimeUnit.SECONDS).untilAsserted {
                assertThat(postalVoteApplicationRepository.findById(payload.applicationDetails.id)).isNotNull

                assertThat(
                    // first message that is reliably logged
                    TestLogAppender.getLogEventMatchingRegex(
                        "^Postal Vote Application Message received with an application id = .{24}$",
                        Level.INFO
                    )
                ).isNotNull

                val filteredLogs = logs.filter { it.mdcPropertyMap.isNotEmpty() }
                // wait until sufficient messages are logged before verifying all share same correlation ID
                assertThat(filteredLogs).hasSizeGreaterThanOrEqualTo(3)

                filteredLogs.forEach {
                    assertThat(it).`as`("Message: ${it.message}").hasCorrelationId(expectedCorrelationId)
                }
            }
        }
    }
}

// TODO placeholder to add test for scheduled jobs when we have them!
