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
import uk.gov.dluhc.emsintegrationapi.service.dto.PendingDownloadSummary
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPendingDownloadsSummaryByGssCode

@ExtendWith(MockitoExtension::class)
class EmailServiceEmsTest {
    private lateinit var emailService: EmailService

    @Mock
    private lateinit var emailClient: EmailClient

    @Mock
    private lateinit var dummyRegisterCheckContentConfiguration: PendingRegisterChecksEmailContentConfiguration

    companion object {
        private const val GSS_CODE_1 = "E00000001"
        private const val GSS_CODE_2 = "E00000002"
        private const val EXPECTED_MAXIMUM_PENDING_PERIOD = "5 days"
        private const val EXPECTED_TOTAL_POSTAL_PENDING = 3
        private const val EXPECTED_TOTAL_POSTAL_PENDING_WITH_EMS_ELECTOR_ID = 1
        private val EXPECTED_PENDING_POSTAL = listOf(
            buildPendingDownloadsSummaryByGssCode(
                gssCode = GSS_CODE_1,
                pendingDownloadCount = 2,
                pendingDownloadsWithEmsElectorId = 1
            ),
            buildPendingDownloadsSummaryByGssCode(
                gssCode = GSS_CODE_2,
                pendingDownloadCount = 1,
                pendingDownloadsWithEmsElectorId = 0
            ),
        )
        private const val EXPECTED_TOTAL_PROXY_PENDING = 4
        private const val EXPECTED_TOTAL_PROXY_PENDING_WITH_EMS_ELECTOR_ID = 2
        private val EXPECTED_PENDING_PROXY = listOf(
            buildPendingDownloadsSummaryByGssCode(
                gssCode = GSS_CODE_1,
                pendingDownloadCount = 4,
                pendingDownloadsWithEmsElectorId = 2
            ),
        )
    }

    @Nested
    inner class SendPendingDownloadMonitoringEmail {

        @Test
        fun `should successfully send a pending download monitoring email`() {
            // Given
            val expectedRecipients = setOf(
                "test1@email.com",
                "test2@email.com",
            )
            val expectedSubject = "Postal and Proxy Pending Downloads Monitoring"

            val expectedEmailBody = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Pending EMS Integration Downloads</title>
                </head>
                <body>
                    <p>A total of $EXPECTED_TOTAL_POSTAL_PENDING postal applications have been pending download for more than $EXPECTED_MAXIMUM_PENDING_PERIOD.</p>
                    <p>Of these, $EXPECTED_TOTAL_POSTAL_PENDING_WITH_EMS_ELECTOR_ID applications have an EMS Elector ID.</p>
                    <br>
                    <table>
                        <thead>
                        <tr>
                            <th>GSS code</th>
                            <th>Pending postal downloads</th>
                            <th>Pending with EMS Elector ID</th>
                        </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>$GSS_CODE_1</td>
                                <td>2</td>
                                <td>1</td>
                            </tr>
                            <tr>
                                <td>$GSS_CODE_2</td>
                               <td>1</td>
                                <td>0</td>
                            </tr>
                        </tbody>
                    </table>
                    <p>A total of $EXPECTED_TOTAL_PROXY_PENDING proxy applications have been pending download for more than $EXPECTED_MAXIMUM_PENDING_PERIOD.</p>
                    <p>Of these, $EXPECTED_TOTAL_PROXY_PENDING_WITH_EMS_ELECTOR_ID applications have an EMS Elector ID.</p>
                    <br>
                    <table>
                        <thead>
                        <tr>
                            <th>GSS code</th>
                            <th>Pending proxy downloads</th>
                            <th>Pending with EMS Elector ID</th>
                        </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>$GSS_CODE_1</td>
                                <td>4</td>
                                <td>2</td>
                            </tr>
                        </tbody>
                    </table>
                </body>
                </html>
                """

            // When
            val emailContentConfiguration = MonitorPendingDownloadsEmailContentConfiguration(
                expectedSubject,
                "email-templates/monitor-pending-downloads.html",
                "test1@email.com,test2@email.com"
            )
            emailService = EmailService(emailClient, dummyRegisterCheckContentConfiguration, emailContentConfiguration)
            emailService.sendPendingDownloadMonitoringEmail(
                postalSummary = PendingDownloadSummary(
                    totalPending = EXPECTED_TOTAL_POSTAL_PENDING,
                    totalPendingWithEmsElectorId = EXPECTED_TOTAL_POSTAL_PENDING_WITH_EMS_ELECTOR_ID,
                    pendingByGssCode = EXPECTED_PENDING_POSTAL
                ),
                proxySummary = PendingDownloadSummary(
                    totalPending = EXPECTED_TOTAL_PROXY_PENDING,
                    totalPendingWithEmsElectorId = EXPECTED_TOTAL_PROXY_PENDING_WITH_EMS_ELECTOR_ID,
                    pendingByGssCode = EXPECTED_PENDING_PROXY
                ),
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
    }
}
