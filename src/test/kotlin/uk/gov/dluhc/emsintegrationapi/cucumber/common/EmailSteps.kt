package uk.gov.dluhc.emsintegrationapi.cucumber.common

import io.cucumber.java8.En
import org.apache.commons.lang3.StringUtils.deleteWhitespace
import org.assertj.core.api.Assertions.assertThat
import uk.gov.dluhc.emsintegrationapi.config.LocalStackContainerSettings
import uk.gov.dluhc.emsintegrationapi.testsupport.emails.EmailMessagesSentClient
import uk.gov.dluhc.emsintegrationapi.testsupport.emails.LocalstackEmailMessage
import uk.gov.dluhc.emsintegrationapi.testsupport.emails.buildLocalstackEmailMessage
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit

open class EmailSteps(
    private val emailMessagesSentClient: EmailMessagesSentClient,
    private val localStackContainerSettings: LocalStackContainerSettings,
) : En {
    init {
        Then("an email is sent from {string} to {string} with subject line {string} and email body") { sender: String, recipient: String, subjectLine: String, emailBody: String ->
            val expectedEmailRequest = buildLocalstackEmailMessage(
                emailSender = sender,
                toAddresses = setOf(recipient),
                subject = subjectLine,
                htmlBody = emailBody.trimIndent(),
                timestamp = OffsetDateTime.now(UTC).toLocalDateTime().truncatedTo(ChronoUnit.SECONDS)
            )
            assertEmailSent(expectedEmailRequest)
        }
    }

    private fun assertEmailSent(expected: LocalstackEmailMessage) {
        with(emailMessagesSentClient.getEmailMessagesSent(localStackContainerSettings.sesMessagesUrl)) {
            val foundMessage = messages.any {
                !it.timestamp.isBefore(expected.timestamp) &&
                    it.destination.toAddresses.toSet() == expected.destination.toAddresses.toSet() &&
                    it.subject == expected.subject &&
                    deleteWhitespace(it.body.htmlPart) == deleteWhitespace(expected.body.htmlPart) &&
                    it.body.textPart == expected.body.textPart &&
                    it.source == expected.source
            }
            assertThat(foundMessage)
                .`as` { "failed to find expectedEmailMessage[$expected], in list of messages[$messages]" }
                .isTrue
        }
    }
}
