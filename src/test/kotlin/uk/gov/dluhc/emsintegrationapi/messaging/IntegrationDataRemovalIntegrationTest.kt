package uk.gov.dluhc.emsintegrationapi.messaging

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
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest
import uk.gov.dluhc.emsintegrationapi.config.MESSAGE_ID
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.Companion.deleteRecords
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.Companion.deleteSqsMessage
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.TestPhase
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.messaging.models.RemoveVoterApplicationEmsDataMessage
import uk.gov.dluhc.emsintegrationapi.messaging.models.RemoveVoterApplicationEmsDataMessage.Source.POSTAL
import uk.gov.dluhc.emsintegrationapi.testsupport.TestLogAppender
import uk.gov.dluhc.emsintegrationapi.testsupport.TestLogAppender.Companion.logs
import uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions.ILoggingEventAssert.Companion.assertThat
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.getRandomString
import java.util.UUID.randomUUID
import java.util.concurrent.TimeUnit

class IntegrationDataRemovalIntegrationTest : IntegrationTest() {

    @Autowired
    protected lateinit var sqsMessagingTemplate: QueueMessagingTemplate

    @Autowired
    protected lateinit var postalVoteApplicationRepository: PostalVoteApplicationRepository

    @Autowired
    protected lateinit var proxyVoteApplicationRepository: ProxyVoteApplicationRepository

    @Autowired
    protected lateinit var amazonSQSAsync: AmazonSQSAsync

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
            deleteRecords(postalVoteApplicationRepository, TestPhase.BEFORE)
            deleteRecords(proxyVoteApplicationRepository, TestPhase.BEFORE)
            deleteSqsMessage(amazonSQSAsync, removeApplicationEmsDataQueueName, TestPhase.BEFORE)
        }

        @AfterEach
        fun deletePostalRecordsAfter() {
            deleteRecords(postalVoteApplicationRepository, TestPhase.AFTER)
            deleteRecords(proxyVoteApplicationRepository, TestPhase.AFTER)
            deleteSqsMessage(amazonSQSAsync, removeApplicationEmsDataQueueName, TestPhase.AFTER)
        }

        @Test
        fun `should log removal of postal ems integration data`() {
            val applicationId = getRandomString(24)
            val payload = RemoveVoterApplicationEmsDataMessage(
                applicationId,
                RemoveVoterApplicationEmsDataMessage.Source.POSTAL,
            )

            sqsMessagingTemplate.convertAndSend(
                removeApplicationEmsDataQueueName,
                payload
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
}
