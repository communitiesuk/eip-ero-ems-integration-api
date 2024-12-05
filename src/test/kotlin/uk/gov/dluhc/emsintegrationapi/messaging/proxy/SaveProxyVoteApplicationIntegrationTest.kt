package uk.gov.dluhc.emsintegrationapi.messaging.proxy

import com.amazonaws.services.sqs.AmazonSQSAsync
import io.awspring.cloud.messaging.core.QueueMessagingTemplate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.ProxyVoteApplicationMessageMapper
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.ProxyVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.testsupport.ClearDownUtils
import uk.gov.dluhc.emsintegrationapi.testsupport.WiremockService
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplicationMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testhelpers.ProxyIntegrationTestHelpers

class SaveProxyVoteApplicationIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var wireMockService: WiremockService

    @Autowired
    private lateinit var queueMessagingTemplate: QueueMessagingTemplate

    @Autowired
    protected lateinit var proxyVoteApplicationRepository: ProxyVoteApplicationRepository

    @Autowired
    protected lateinit var amazonSQSAsync: AmazonSQSAsync

    @Autowired
    protected lateinit var messageSender: MessageSender<ProxyVoteApplicationMessage>

    @Autowired
    protected lateinit var proxyVoteApplicationMessageMapper: ProxyVoteApplicationMessageMapper

    @Value("\${sqs.remove-application-ems-integration-data-queue-name}")
    protected lateinit var removeApplicationEmsDataQueueName: String

    private var fixtures: ProxyIntegrationTestHelpers? = null

    @BeforeEach
    fun deleteProxyRecordsBefore() {
        fixtures =
            ProxyIntegrationTestHelpers(
                wiremockService = wireMockService,
                proxyVoteApplicationRepository = proxyVoteApplicationRepository,
                queueMessagingTemplate = queueMessagingTemplate,
                messageSenderProxy = messageSender,
            )
        ClearDownUtils.clearDownRecords(
            proxyRepository = proxyVoteApplicationRepository,
            amazonSQSAsync = amazonSQSAsync,
            queueName = removeApplicationEmsDataQueueName,
        )
    }

    @AfterEach
    fun deleteProxyRecordsAfter() {
        ClearDownUtils.clearDownRecords(
            proxyRepository = proxyVoteApplicationRepository,
            amazonSQSAsync = amazonSQSAsync,
            queueName = removeApplicationEmsDataQueueName,
        )
    }

    @ParameterizedTest
    @CsvSource(
        "502cf250036469154b4f85fa,e87cbaea-0deb-4058-95c6-8240d426f5e1,APPROVED",
        "502cf250036469154b4f85fb,e87cbaea-0deb-4058-95c6-8240d426f5e2,REJECTED",
    )
    fun `The system process and saves a proxy vote application message with signature into the database`(
        proxyApplicationId: String,
        emsElectorId: String,
        applicationStatus: String,
    ) {
        // Given a proxy vote application with the application id "<ProxyApplicationId>", electoral id "<EmsElectoralId>" and status "<ApplicationStatus>"
        val proxyVoteApplicationMessage =
            fixtures!!.buildProxyVoteApplicationsWithSignature(
                applicationStatus,
                proxyApplicationId,
                emsElectorId,
            )

        // When I send an sqs message to the proxy application queue
        fixtures!! sendMessage proxyVoteApplicationMessage

        // Then the "<ApplicationStatus>" proxy vote application has been successfully saved with the application id "<ProxyApplicationId>", signature and ballot addresses
        fixtures!!.checkProxyApplicationSuccessfullySaved(
            proxyVoteApplicationMessage,
            proxyApplicationId,
            applicationStatus,
        )
    }

    @ParameterizedTest
    @CsvSource(
        "502cf250036469154b4f85fa,Disabled",
        "502cf250036469154b4f85fb,Other",
        "502cf250036469154b4f85fc,I have a disability that prevents me from signing or uploading my signature: Visually impaired and wants to use the full 250 character allowance. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam vehicula hendrerit eros consequat sagittis. Curabitur eget nisi ac felis tincidunt ultrices. Duis imperdiet tempus.",
    )
    fun `The system process and saves a proxy vote application message with signature waiver details`(
        proxyApplicationId: String,
        waiverReason: String,
    ) {
        // Given a proxy vote application with the application id "<ProxyApplicationId>" and signature waiver reason "<WaiverReason>"
        val proxyVoteApplicationMessage =
            buildProxyVoteApplicationMessageDto(
                applicationDetails =
                buildApplicationDetailsMessageDto(
                    applicationId = proxyApplicationId,
                    signatureWaived = true,
                    signatureWaivedReason = waiverReason,
                ),
            )

        // When I send an sqs message to the proxy application queue
        fixtures!! sendMessage proxyVoteApplicationMessage

        // Then the proxy vote application has been successfully saved with the signature waiver reason "<WaiverReason>"
        fixtures!!.checkProxyApplicationSuccessfullySavedWithSignatureWaiver(proxyVoteApplicationMessage, waiverReason)
    }

    @Test
    fun `The system does not allow two different proxy applications having same id and a different ems electoral id`() {
        val proxyApplicationId = "502cf250036469154b4f86fa"
        val emsElectorId1 = "e87cbaea-0deb-4058-95c6-8240d426f5e1"
        val emsElectorId2 = "e87cbaea-0deb-4058-95c6-8240d426f5e2"

        // Given a proxy vote application with the application id "<ProxyApplicationId>" and electoral id "<EmsElectoralId1>" exists
        val proxyVoteApplicationMessage1 = fixtures!!.buildProxyVoteApplicationWith(proxyApplicationId, emsElectorId1)
        fixtures!!.createProxyVoteApplication(proxyVoteApplicationMessage1, proxyVoteApplicationMessageMapper)

        // When I send an sqs message to the proxy application queue with an application id "<ProxyApplicationId>" and electoral id "<EmsElectoralId2>"
        val proxyVoteApplicationMessage2 = fixtures!!.buildProxyVoteApplicationWith(proxyApplicationId, emsElectorId2)
        fixtures!! sendMessage proxyVoteApplicationMessage2

        // Then the proxy vote application with id "<ProxyApplicationId>" and electoral id "<EmsElectoralId2>" did not save
        fixtures!!.confirmTheApplicationDidNotSave(proxyApplicationId)
    }

    @Test
    fun `The system does allow two different proxy applications having a different application id and a same ems electoral id`() {
        /*
    Examples:
      | ProxyApplicationId      | EmsElectoralId                       | ProxyApplicationId2     |
      | 502cf250036469154b4f87fa | e87cbaea-0deb-4058-95c6-8240d426f5e1 | 502cf250036469154b4f85fb |
         */
        val proxyApplicationId1 = "502cf250036469154b4f87fa"
        val emsElectorId = "e87cbaea-0deb-4058-95c6-8240d426f5e1"
        val proxyApplicationId2 = "502cf250036469154b4f85fb"

        // Given a proxy vote application with the application id "<ProxyApplicationId>" and electoral id "<EmsElectoralId>" exists
        val proxyVoteApplicationMessage1 = fixtures!!.buildProxyVoteApplicationWith(proxyApplicationId1, emsElectorId)
        fixtures!!.createProxyVoteApplication(proxyVoteApplicationMessage1, proxyVoteApplicationMessageMapper)

        // When I send an sqs message to the proxy application queue with an application id "<ProxyApplicationId2>" and electoral id "<EmsElectoralId>"
        val proxyVoteApplicationMessage2 = fixtures!!.buildProxyVoteApplicationWith(proxyApplicationId2, emsElectorId)
        fixtures!! sendMessage proxyVoteApplicationMessage2

        // Then the proxy vote application with id "<ProxyApplicationId2>" was saved
        fixtures!!.confirmTheEntitySaved(proxyApplicationId2)
    }

    @ParameterizedTest
    @CsvSource(
        "123456,e87cbaea-0deb-4058-95c6-8240d426f5e4,APPROVED",
        "123457,e87cbaea-0deb-4058-95c6-8240d426f5e6,REJECTED",
    )
    fun `The system will reject a proxy vote application message with an invalid application id, application id must be 24 characters`(
        invalidProxyApplicationId: String,
        emsElectorId: String,
        applicationStatus: String,
    ) {
        // Given a proxy vote application with the application id "<InvalidProxyApplicationId>", electoral id "<EmsElectoralId>" and status "<ApplicationStatus>"
        val proxyVoteApplicationMessage =
            fixtures!!.buildProxyVoteApplicationsWithSignature(
                applicationStatus,
                invalidProxyApplicationId,
                emsElectorId,
            )

        // When I send an sqs message to the proxy application queue
        fixtures!! sendMessage proxyVoteApplicationMessage

        // Then the proxy vote application with id "<InvalidProxyApplicationId>" did not save
        fixtures!!.confirmTheApplicationDidNotSave(invalidProxyApplicationId)
    }
}
