package uk.gov.dluhc.emsintegrationapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import uk.gov.dluhc.email.EmailClient
import uk.gov.dluhc.emsintegrationapi.config.MonitorPendingDownloadsEmailContentConfiguration
import uk.gov.dluhc.emsintegrationapi.config.PendingRegisterChecksEmailContentConfiguration
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity.buildRegisterCheckSummaryByGssCode

@ExtendWith(MockitoExtension::class)
internal class EmailServiceRegisterCheckTest {
    private lateinit var emailService: EmailService

    @Mock
    private lateinit var emailClient: EmailClient

    @Mock
    private lateinit var dummyEmsContentConfiguration: MonitorPendingDownloadsEmailContentConfiguration

    companion object {
        private const val GSS_CODE_1 = "E00000001"
        private const val GSS_CODE_2 = "E00000002"
        private const val EXPECTED_TOTAL_STUCK_APPLICATIONS = "3"
        private const val EXPECTED_MAXIMUM_PENDING_PERIOD = "24 hours"
        private val EXPECTED_STUCK_REGISTER_CHECK_SUMMARIES = listOf(
            buildRegisterCheckSummaryByGssCode(gssCode = GSS_CODE_1, registerCheckCount = 2),
            buildRegisterCheckSummaryByGssCode(gssCode = GSS_CODE_2, registerCheckCount = 1),
        )
    }

    @Nested
    inner class SendRegisterCheckMonitoringEmail {

        @Test
        fun `should successfully send a register check monitoring email`() {
            // Given
            val expectedRecipients = setOf(
                "test1@email.com",
                "test2@email.com",
            )
            val expectedSubject = "Register Check Monitoring"

            val expectedEmailBody = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Pending register checks</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <p>A total of $EXPECTED_TOTAL_STUCK_APPLICATIONS register checks have been pending for more than $EXPECTED_MAXIMUM_PENDING_PERIOD.</p>\n" +
                "    <br>\n" +
                "    <table>\n" +
                "        <thead>\n" +
                "        <tr>\n" +
                "            <th>GSS code</th>\n" +
                "            <th>Register check count</th>\n" +
                "        </tr>\n" +
                "        </thead>\n" +
                "        <tbody>\n" +
                "            <tr>\n" +
                "                <td>$GSS_CODE_1</td>\n" +
                "                <td>2</td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td>$GSS_CODE_2</td>\n" +
                "                <td>1</td>\n" +
                "            </tr>\n" +
                "        </tbody>\n" +
                "    </table>\n" +
                "</body>\n" +
                "</html>"

            // When
            val emailContentConfiguration = buildPendingRegisterChecksEmailContentConfiguration(
                expectedSubject,
                "email-templates/pending-register-checks.html",
                "test1@email.com,test2@email.com"
            )
            emailService = EmailService(emailClient, emailContentConfiguration, dummyEmsContentConfiguration)
            emailService.sendRegisterCheckMonitoringEmail(
                stuckRegisterCheckSummaries = EXPECTED_STUCK_REGISTER_CHECK_SUMMARIES,
                totalStuck = EXPECTED_TOTAL_STUCK_APPLICATIONS,
                expectedMaximumPendingPeriod = EXPECTED_MAXIMUM_PENDING_PERIOD
            )

            // Then
            argumentCaptor<String>().apply {
                verify(emailClient).send(
                    eq(expectedRecipients),
                    eq(emptySet()),
                    eq(expectedSubject),
                    capture()
                )
                val capturedEmailBody = firstValue.filterNot { it.isWhitespace() }
                val expectedBody = expectedEmailBody.filterNot { it.isWhitespace() }

                assertThat(capturedEmailBody).matches(expectedBody)
            }

            verifyNoMoreInteractions(emailClient)
        }

        private fun buildPendingRegisterChecksEmailContentConfiguration(
            subject: String,
            emailBodyTemplate: String,
            recipients: String
        ) = PendingRegisterChecksEmailContentConfiguration(
            subject,
            emailBodyTemplate,
            recipients
        )
    }
}
