package uk.gov.dluhc.emsintegrationapi.messaging

import ch.qos.logback.classic.Level
import io.awspring.cloud.sqs.operations.SqsSendOptions
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest
import uk.gov.dluhc.emsintegrationapi.config.MESSAGE_ID
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicationDetails
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.messaging.models.RemoveApplicationEmsIntegrationDataMessage
import uk.gov.dluhc.emsintegrationapi.messaging.models.RemoveApplicationEmsIntegrationDataMessage.Source.POSTAL
import uk.gov.dluhc.emsintegrationapi.messaging.models.RemoveApplicationEmsIntegrationDataMessage.Source.PROXY
import uk.gov.dluhc.emsintegrationapi.testsupport.ClearDownUtils
import uk.gov.dluhc.emsintegrationapi.testsupport.TestLogAppender
import uk.gov.dluhc.emsintegrationapi.testsupport.TestLogAppender.Companion.logs
import uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions.ILoggingEventAssert.Companion.assertThat
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.randomHexadecimalString
import java.util.concurrent.TimeUnit

private class IntegrationDataRemovalIntegrationTest : IntegrationTest() {

    @Autowired
    protected lateinit var postalVoteApplicationRepository: PostalVoteApplicationRepository

    @Autowired
    protected lateinit var proxyVoteApplicationRepository: ProxyVoteApplicationRepository

    @Value("\${sqs.remove-application-ems-integration-data-queue-name}")
    protected lateinit var removeApplicationEmsDataQueueName: String

    @AfterEach
    fun tearDown() {
        TestLogAppender.reset()
    }

    @Nested
    inner class PostalRemoveIntegrationDataSqs {

        @BeforeEach
        fun deletePostalRecordsBefore() {
            ClearDownUtils.clearDownRecords(
                postalRepository = postalVoteApplicationRepository,
                queueName = removeApplicationEmsDataQueueName
            )
        }

        @AfterEach
        fun deletePostalRecordsAfter() {
            ClearDownUtils.clearDownRecords(
                postalRepository = postalVoteApplicationRepository,
                queueName = removeApplicationEmsDataQueueName
            )
        }

        @Test
        fun `should log removal of postal ems integration data`() {
            val applicationId = randomHexadecimalString(24)
            val postalVoteApplication = buildPostalVoteApplication(applicationId = applicationId)
            postalVoteApplication.applicationDetails.emsStatus = ApplicationDetails.EmsStatus.SUCCESS

            postalVoteApplicationRepository.save(postalVoteApplication)

            val payload = RemoveApplicationEmsIntegrationDataMessage(
                applicationId,
                POSTAL,
            )

            sqsMessagingTemplate.send(
                { to: SqsSendOptions<RemoveApplicationEmsIntegrationDataMessage> ->
                    to
                        .queue(removeApplicationEmsDataQueueName)
                        .payload(payload)
                }
            )

            await.atMost(10, TimeUnit.SECONDS).untilAsserted {
                assertThat(postalVoteApplicationRepository.findById(payload.applicationId)).isNotNull

                val firstReliableMessage = TestLogAppender.getLogEventMatchingRegex(
                    "^Deleting $POSTAL application ems data with id = .{24}$",
                    Level.INFO
                )
                assertThat(firstReliableMessage).hasAnyMessageId()
                val messageId = firstReliableMessage!!.mdcPropertyMap[MESSAGE_ID]!!

                val filteredLogs = logs.filter { it.mdcPropertyMap.isNotEmpty() }
                // wait until sufficient messages are logged before verifying all share same correlation ID and message ID
                assertThat(filteredLogs).hasSizeGreaterThanOrEqualTo(3)

                filteredLogs.forEach {
                    assertThat(it).`as`("Message: ${it.message}")
                        .hasMessageId(messageId)
                }
            }
        }
    }

    @Test
    fun `should log removal of proxy ems integration data`() {
        val applicationId = randomHexadecimalString(24)
        val proxyVoteApplication = buildProxyVoteApplication(applicationId = applicationId)
        proxyVoteApplication.applicationDetails.emsStatus = ApplicationDetails.EmsStatus.SUCCESS

        proxyVoteApplicationRepository.save(proxyVoteApplication)

        val payload = RemoveApplicationEmsIntegrationDataMessage(
            applicationId,
            PROXY,
        )

        sqsMessagingTemplate.send(
            { to: SqsSendOptions<RemoveApplicationEmsIntegrationDataMessage> ->
                to
                    .queue(removeApplicationEmsDataQueueName)
                    .payload(payload)
            }
        )

        await.atMost(10, TimeUnit.SECONDS).untilAsserted {
            assertThat(proxyVoteApplicationRepository.findById(payload.applicationId)).isNotNull

            val firstReliableMessage = TestLogAppender.getLogEventMatchingRegex(
                "^Deleting $PROXY application ems data with id = .{24}$",
                Level.INFO
            )
            assertThat(firstReliableMessage).hasAnyMessageId()
            val messageId = firstReliableMessage!!.mdcPropertyMap[MESSAGE_ID]!!

            val filteredLogs = logs.filter { it.mdcPropertyMap.isNotEmpty() }
            // wait until sufficient messages are logged before verifying all share same correlation ID and message ID
            assertThat(filteredLogs).hasSizeGreaterThanOrEqualTo(3)

            filteredLogs.forEach {
                assertThat(it).`as`("Message: ${it.message}")
                    .hasMessageId(messageId)
            }
        }
    }
}
