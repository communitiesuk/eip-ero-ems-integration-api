package uk.gov.dluhc.emsintegrationapi.messaging.postal

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
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.PostalVoteApplicationMessageMapper
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.testsupport.ClearDownUtils
import uk.gov.dluhc.emsintegrationapi.testsupport.WiremockService
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.testsupport.testhelpers.PostalIntegrationTestHelpers

class SavePostalVoteApplicationIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var wireMockService: WiremockService

    @Autowired
    private lateinit var queueMessagingTemplate: QueueMessagingTemplate

    @Autowired
    protected lateinit var postalVoteApplicationRepository: PostalVoteApplicationRepository

    @Autowired
    protected lateinit var amazonSQSAsync: AmazonSQSAsync

    @Autowired
    protected lateinit var messageSender: MessageSender<PostalVoteApplicationMessage>

    @Autowired
    protected lateinit var postalVoteApplicationMessageMapper: PostalVoteApplicationMessageMapper

    @Value("\${sqs.remove-application-ems-integration-data-queue-name}")
    protected lateinit var removeApplicationEmsDataQueueName: String

    private var testHelpers: PostalIntegrationTestHelpers? = null

    @BeforeEach
    fun deletePostalRecordsBefore() {
        testHelpers =
            PostalIntegrationTestHelpers(
                wiremockService = wireMockService,
                postalVoteApplicationRepository = postalVoteApplicationRepository,
                queueMessagingTemplate = queueMessagingTemplate,
                messageSenderPostal = messageSender,
            )
        ClearDownUtils.clearDownRecords(
            postalRepository = postalVoteApplicationRepository,
            amazonSQSAsync = amazonSQSAsync,
            queueName = removeApplicationEmsDataQueueName,
        )
    }

    @AfterEach
    fun deletePostalRecordsAfter() {
        ClearDownUtils.clearDownRecords(
            postalRepository = postalVoteApplicationRepository,
            amazonSQSAsync = amazonSQSAsync,
            queueName = removeApplicationEmsDataQueueName,
        )
    }

    @ParameterizedTest
    @CsvSource(
        "502cf250036469154b4f85fa,e87cbaea-0deb-4058-95c6-8240d426f5e1,APPROVED",
        "502cf250036469154b4f85fb,e87cbaea-0deb-4058-95c6-8240d426f5e2,REJECTED",
    )
    fun `The system process and saves a postal vote application message with signature into the database`(
        postalApplicationId: String,
        emsElectorId: String,
        applicationStatus: String,
    ) {
        // Given a postal vote application with the application id "<PostalApplicationId>", electoral id "<EmsElectoralId>" and status "<ApplicationStatus>"
        val postalVoteApplicationMessage =
            testHelpers!!.buildPostalVoteApplicationsWithSignature(
                applicationStatus,
                postalApplicationId,
                emsElectorId,
            )

        // When I send an sqs message to the postal application queue
        testHelpers!!.sendMessage(postalVoteApplicationMessage)

        // Then the "<ApplicationStatus>" postal vote application has been successfully saved with the application id "<PostalApplicationId>", signature and ballot addresses
        testHelpers!!.checkPostalApplicationSuccessfullySaved(
            postalVoteApplicationMessage,
            postalApplicationId,
            applicationStatus,
        )
    }

    @ParameterizedTest
    @CsvSource(
        "502cf250036469154b4f85fa,Disabled",
        "502cf250036469154b4f85fb,Other",
        "502cf250036469154b4f85fc,I have a disability that prevents me from signing or uploading my signature: Visually impaired and wants to use the full 250 character allowance. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam vehicula hendrerit eros consequat sagittis. Curabitur eget nisi ac felis tincidunt ultrices. Duis imperdiet tempus.",
    )
    fun `The system process and saves a postal vote application message with signature waiver details`(
        postalApplicationId: String,
        waiverReason: String,
    ) {
        // Given a postal vote application with the application id "<PostalApplicationId>" and signature waiver reason "<WaiverReason>"
        val postalVoteApplicationMessage =
            buildPostalVoteApplicationMessage(
                applicationDetails = buildApplicationDetailsMessageDto(
                    applicationId = postalApplicationId,
                    signatureWaived = true,
                    signatureWaivedReason = waiverReason,
                ),
            )

        // When I send an sqs message to the postal application queue
        testHelpers!!.sendMessage(postalVoteApplicationMessage)

        // Then the postal vote application has been successfully saved with the signature waiver reason "<WaiverReason>"
        testHelpers!!.checkPostalApplicationSuccessfullySavedWithSignatureWaiver(
            postalVoteApplicationMessage,
            waiverReason,
        )
    }

    @Test
    fun `The system does not allow two different postal applications having same id and a different ems electoral id`() {
        val postalApplicationId = "502cf250036469154b4f86fa"
        val emsElectorId1 = "e87cbaea-0deb-4058-95c6-8240d426f5e1"
        val emsElectorId2 = "e87cbaea-0deb-4058-95c6-8240d426f5e2"

        // Given a postal vote application with the application id "<PostalApplicationId>" and electoral id "<EmsElectoralId1>" exists
        val postalVoteApplicationMessage1 =
            testHelpers!!.buildPostalVoteApplicationWith(postalApplicationId, emsElectorId1)
        testHelpers!!.createPostalVoteApplication(postalVoteApplicationMessage1, postalVoteApplicationMessageMapper)

        // When I send an sqs message to the postal application queue with an application id "<PostalApplicationId>" and electoral id "<EmsElectoralId2>"
        val postalVoteApplicationMessage2 =
            testHelpers!!.buildPostalVoteApplicationWith(postalApplicationId, emsElectorId2)
        testHelpers!!.sendMessage(postalVoteApplicationMessage2)

        // Then the postal vote application with id "<PostalApplicationId>" and electoral id "<EmsElectoralId2>" did not save
        testHelpers!!.confirmTheApplicationDidNotSave(postalApplicationId)
    }

    @Test
    fun `The system does allow two different postal applications having a different application id and a same ems electoral id`() {
        /*
    Examples:
      | PostalApplicationId      | EmsElectoralId                       | PostalApplicationId2     |
      | 502cf250036469154b4f87fa | e87cbaea-0deb-4058-95c6-8240d426f5e1 | 502cf250036469154b4f85fb |
         */
        val postalApplicationId1 = "502cf250036469154b4f87fa"
        val emsElectorId = "e87cbaea-0deb-4058-95c6-8240d426f5e1"
        val postalApplicationId2 = "502cf250036469154b4f85fb"

        // Given a postal vote application with the application id "<PostalApplicationId>" and electoral id "<EmsElectoralId>" exists
        val postalVoteApplicationMessage1 =
            testHelpers!!.buildPostalVoteApplicationWith(postalApplicationId1, emsElectorId)
        testHelpers!!.createPostalVoteApplication(postalVoteApplicationMessage1, postalVoteApplicationMessageMapper)

        // When I send an sqs message to the postal application queue with an application id "<PostalApplicationId2>" and electoral id "<EmsElectoralId>"
        val postalVoteApplicationMessage2 =
            testHelpers!!.buildPostalVoteApplicationWith(postalApplicationId2, emsElectorId)
        testHelpers!!.sendMessage(postalVoteApplicationMessage2)

        // Then the postal vote application with id "<PostalApplicationId2>" was saved
        testHelpers!!.confirmTheEntitySaved(postalApplicationId2)
    }

    @ParameterizedTest
    @CsvSource(
        "123456,e87cbaea-0deb-4058-95c6-8240d426f5e4,APPROVED",
        "123457,e87cbaea-0deb-4058-95c6-8240d426f5e6,REJECTED",
    )
    fun `The system will reject a postal vote application message with an invalid application id, application id must be 24 characters`(
        invalidPostalApplicationId: String,
        emsElectorId: String,
        applicationStatus: String,
    ) {
        // Given a postal vote application with the application id "<InvalidPostalApplicationId>", electoral id "<EmsElectoralId>" and status "<ApplicationStatus>"
        val postalVoteApplicationMessage =
            testHelpers!!
                .buildPostalVoteApplicationsWithSignature(
                    applicationStatus,
                    invalidPostalApplicationId,
                    emsElectorId,
                )

        // When I send an sqs message to the postal application queue
        testHelpers!!.sendMessage(postalVoteApplicationMessage)

        // Then the postal vote application with id "<InvalidPostalApplicationId>" did not save
        testHelpers!!.confirmTheApplicationDidNotSave(invalidPostalApplicationId)
    }
}
