package uk.gov.dluhc.emsintegrationapi.logging

import ch.qos.logback.classic.Level
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest
import uk.gov.dluhc.emsintegrationapi.testsupport.TestLogAppender
import uk.gov.dluhc.emsintegrationapi.testsupport.TestLogAppender.Companion.logs
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

    @Nested
    inner class PostalVoteApplicationSqs {
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
                assertThat(postalVoteApplicationRepository.findById(payload.approvalDetails.id)).isNotNull

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

// TODO placeholder to add test for interaction with RestTemplate when we integrate with IER
