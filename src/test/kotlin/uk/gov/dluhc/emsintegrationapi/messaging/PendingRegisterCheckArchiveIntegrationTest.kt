package uk.gov.dluhc.emsintegrationapi.messaging

import ch.qos.logback.classic.Level
import io.awspring.cloud.sqs.operations.SqsSendOptions
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus
import uk.gov.dluhc.emsintegrationapi.testsupport.ClearDownUtils
import uk.gov.dluhc.emsintegrationapi.testsupport.TestLogAppender
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity.buildRegisterCheck
import uk.gov.dluhc.registercheckerapi.messaging.models.PendingRegisterCheckArchiveMessage
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

internal class PendingRegisterCheckArchiveIntegrationTest : IntegrationTest() {
    @BeforeEach
    fun deletePostalRecordsBefore() {
        ClearDownUtils.clearDownRecords(
            registerCheckRepository = registerCheckRepository,
            queueName = pendingRegisterCheckArchiveQueueName,
        )
    }

    @AfterEach
    fun deletePostalRecordsAfter() {
        ClearDownUtils.clearDownRecords(
            registerCheckRepository = registerCheckRepository,
            queueName = pendingRegisterCheckArchiveQueueName,
        )
    }

    @Test
    fun `should not archive expired register check`() {
        val correlationId = UUID.randomUUID()
        val registerCheck =
            buildRegisterCheck(
                correlationId = correlationId,
                status = CheckStatus.EXPIRED,
            )

        registerCheckRepository.save(registerCheck)

        val payload =
            PendingRegisterCheckArchiveMessage(
                correlationId = correlationId,
            )

        sqsMessagingTemplate.send(
            { to: SqsSendOptions<PendingRegisterCheckArchiveMessage> ->
                to
                    .queue(pendingRegisterCheckArchiveQueueName)
                    .payload(payload)
            },
        )

        await.atMost(10, TimeUnit.SECONDS).untilAsserted {
            val registerCheck1 = registerCheckRepository.findByCorrelationId(correlationId)
            assertThat(registerCheck1).isNotNull
            assertThat(registerCheck1?.status).isEqualTo(CheckStatus.EXPIRED)
            assertThat(
                TestLogAppender.hasLog(
                    "Register Check with correlationId $correlationId has status ${CheckStatus.EXPIRED} so cannot be archived (must be at status PENDING)",
                    Level.WARN,
                ),
            ).isTrue()
        }
    }

    @Test
    fun `should not archive pending register check when not found`() {
        val correlationId = UUID.randomUUID()

        val payload =
            PendingRegisterCheckArchiveMessage(
                correlationId = correlationId,
            )

        sqsMessagingTemplate.send(
            { to: SqsSendOptions<PendingRegisterCheckArchiveMessage> ->
                to
                    .queue(pendingRegisterCheckArchiveQueueName)
                    .payload(payload)
            },
        )

        await.atMost(10, TimeUnit.SECONDS).untilAsserted {
            val registerCheck1 = registerCheckRepository.findByCorrelationId(correlationId)
            assertThat(registerCheck1).isNull()
            assertThat(
                TestLogAppender.hasLog(
                    "Pending register check for requestid:[$correlationId] not found",
                    Level.WARN,
                ),
            ).isTrue()
        }
    }

    @Test
    fun `should archive pending register check when found`() {
        val correlationId = UUID.randomUUID()
        val registerCheck =
            buildRegisterCheck(
                correlationId = correlationId,
                status = CheckStatus.PENDING,
            )
        val checkInstant = Instant.now()

        registerCheckRepository.save(registerCheck)

        val payload =
            PendingRegisterCheckArchiveMessage(
                correlationId = correlationId,
            )

        sqsMessagingTemplate.send(
            { to: SqsSendOptions<PendingRegisterCheckArchiveMessage> ->
                to
                    .queue(pendingRegisterCheckArchiveQueueName)
                    .payload(payload)
            },
        )

        await.atMost(10, TimeUnit.SECONDS).untilAsserted {
            val registerCheck1 = registerCheckRepository.findByCorrelationId(correlationId)
            assertThat(registerCheck1).isNotNull
            assertThat(registerCheck1?.status).isEqualTo(CheckStatus.ARCHIVED)
            assertThat(registerCheck1?.matchResultSentAt?.isAfter(checkInstant))
        }
    }
}
