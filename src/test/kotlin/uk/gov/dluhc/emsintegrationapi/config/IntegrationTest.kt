package uk.gov.dluhc.emsintegrationapi.config

import com.zaxxer.hikari.HikariDataSource
import io.awspring.cloud.sqs.operations.SqsTemplate
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.cache.CacheManager
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.reactive.server.WebTestClient
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest
import software.amazon.awssdk.services.sqs.model.QueueAttributeName
import tools.jackson.databind.json.JsonMapper
import uk.gov.dluhc.emsintegrationapi.database.repository.RegisterCheckRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.RegisterCheckResultDataRepository
import uk.gov.dluhc.emsintegrationapi.testsupport.TestLogAppender
import uk.gov.dluhc.emsintegrationapi.testsupport.WiremockService
import uk.gov.dluhc.emsintegrationapi.testsupport.emails.LocalstackEmailMessagesSentClient
import uk.gov.dluhc.emsintegrationapi.testsupport.getBearerToken
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.sql.DataSource
import kotlin.text.toInt

/**
 * Base class used to bring up the entire Spring ApplicationContext
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@AutoConfigureWebTestClient(timeout = "PT5M")
internal abstract class IntegrationTest {

    @Autowired
    protected lateinit var localStackContainerSettings: LocalStackContainerSettings

    @Autowired
    protected lateinit var webTestClient: WebTestClient

    @Autowired
    protected lateinit var wireMockService: WiremockService

    @Autowired
    protected lateinit var emailMessagesSentClient: LocalstackEmailMessagesSentClient

    @Autowired
    protected lateinit var registerCheckRepository: RegisterCheckRepository

    @Autowired
    protected lateinit var registerCheckResultDataRepository: RegisterCheckResultDataRepository

    @Autowired
    protected lateinit var votingArrangementRepository: uk.gov.dluhc.emsintegrationapi.database.repository.VotingArrangementRepository

    @Autowired
    protected lateinit var sqsAsync: SqsAsyncClient

    @Autowired
    protected lateinit var sqsMessagingTemplate: SqsTemplate

    @Autowired
    protected lateinit var jsonMapper: JsonMapper

    @Autowired
    protected lateinit var cacheManager: CacheManager

    @MockitoSpyBean(name = "readWriteDataSource")
    protected lateinit var readWriteDataSource: HikariDataSource

    @MockitoSpyBean(name = "readOnlyDataSource")
    protected lateinit var readOnlyDataSource: HikariDataSource

    @Autowired
    protected lateinit var dataSources: List<DataSource>

    @Value("\${sqs.initiate-applicant-register-check-queue-name}")
    protected lateinit var initiateApplicantRegisterCheckQueueName: String

    @Value("\${sqs.remove-applicant-register-check-data-queue-name}")
    protected lateinit var removeApplicantRegisterCheckDataQueueName: String

    @Value("\${sqs.ems-application-processed-queue-name}")
    protected lateinit var emsApplicationProcessedQueueName: String

    @Value("\${caching.time-to-live}")
    protected lateinit var timeToLive: Duration

    @Value("\${spring.security.oauth2.resourceserver.jwt.admin-issuer}")
    protected lateinit var adminJwtIssuer: String

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            MySQLContainerConfiguration.getInstance()
        }
    }

    protected fun getBearerToken(
        email: String = "an-admin-user@gov.uk",
    ) = getBearerToken(
        email = email,
        issuer = adminJwtIssuer,
    )

    @BeforeEach
    fun resetWireMock() {
        wireMockService.resetAllStubsAndMappings()
    }

    @BeforeEach
    fun clearDatabase() {
        registerCheckResultDataRepository.deleteAll()
        registerCheckRepository.deleteAll()
        votingArrangementRepository.deleteAll()
        cacheManager.getCache(IER_ELECTORAL_REGISTRATION_OFFICES_CACHE)?.clear()
        cacheManager.getCache(ERO_GSS_CODE_BY_ERO_ID_CACHE)?.clear()
        cacheManager.getCache(IER_ELECTORAL_REGISTRATION_OFFICES_CACHE)?.clear()
    }

    @BeforeEach
    fun clearLogAppender() {
        TestLogAppender.reset()
    }

    protected fun getNumberOfMessagesOnSqsQueue(sqsAsyncClient: SqsAsyncClient, queueUrl: String): Int {
        val request = GetQueueAttributesRequest.builder()
            .queueUrl(queueUrl)
            .attributeNames(
                QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES,
                QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE,
                QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_DELAYED
            )
            .build()
        val response = sqsAsyncClient.getQueueAttributes(request).join()
        val responseAttributes = response.attributes()
        return (responseAttributes[QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES]?.toInt() ?: 0) +
            (responseAttributes[QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE]?.toInt() ?: 0) +
            (responseAttributes[QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_DELAYED]?.toInt() ?: 0)
    }

    protected fun waitForAllSqsMessagesToProcess(
        vararg queueUrl: String,
        timeoutSeconds: Long = 5,
    ) {
        await.atMost(timeoutSeconds, TimeUnit.SECONDS).untilAsserted {
            queueUrl.forEach {
                assertThat(getNumberOfMessagesOnSqsQueue(sqsAsync, it)).isZero
            }
        }
    }
}
