package uk.gov.dluhc.emsintegrationapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sts.StsClient
import java.net.URI

private val logger = KotlinLogging.logger {}

/**
 * Configuration class exposing beans for the LocalStack (AWS) environment.
 */
@Configuration
class LocalStackContainerConfiguration {

    companion object {
        const val DEFAULT_PORT = 4566
        const val DEFAULT_ACCESS_KEY_ID = "test"
        const val DEFAULT_SECRET_KEY = "test"

        val objectMapper = ObjectMapper()

        /**
         * Creates and starts LocalStack configured with a basic (empty) SQS service.
         * Returns the container that can subsequently be used for further setup and configuration.
         */
        @Bean
        fun localstackContainer(@Value("\${cloud.aws.region.static}") region: String): GenericContainer<*> {
            return GenericContainer(
                DockerImageName.parse("localstack/localstack:1.1.0")
            ).withEnv(
                mapOf(
                    "SERVICES" to "sqs, sts",
                    "AWS_DEFAULT_REGION" to region,
                )
            )
                .withReuse(true)
                .withExposedPorts(DEFAULT_PORT)
                .withCreateContainerCmdModifier { it.withName("ems-integration-api-integration-test-localstack") }
                .apply {
                    start()
                }
        }
    }

    @Bean
    fun awsBasicCredentialsProvider(): AwsCredentialsProvider =
        StaticCredentialsProvider.create(AwsBasicCredentials.create(DEFAULT_ACCESS_KEY_ID, DEFAULT_SECRET_KEY))

    @Bean
    @Primary
    fun localStackStsClient(
        @Value("\${cloud.aws.region.static}") region: String,
        @Qualifier("localstackContainer") localStackContainer: GenericContainer<*>,
        awsCredentialsProvider: AwsCredentialsProvider
    ): StsClient {

        val uri = URI.create("http://${localStackContainer.host}:${localStackContainer.getMappedPort(DEFAULT_PORT)}")
        return StsClient.builder()
            .credentialsProvider(awsCredentialsProvider)
            .region(Region.of(region))
            .endpointOverride(uri)
            .build()
    }

    /**
     * Uses the localstack container to configure the various services.
     *
     * @return a [LocalStackContainerSettings] bean encapsulating the various IDs etc of the configured container and services.
     */
    @Bean
    fun localStackContainerSqsSettings(
        @Qualifier("localstackContainer") localStackContainer: GenericContainer<*>,
        applicationContext: ConfigurableApplicationContext,
        @Value("\${sqs.proxy-application-queue-name}") proxyApplicationQueueName: String,
        @Value("\${sqs.postal-application-queue-name}") postalApplicationQueueName: String,
        @Value("\${sqs.deleted-proxy-application-queue-name}") deletedProxyApplicationQueueName: String,
        @Value("\${sqs.deleted-postal-application-queue-name}") deletedPostalApplicationQueueName: String,
    ): LocalStackContainerSettings {
        val proxyApplicationQueueUrl = localStackContainer.createSqsQueue(proxyApplicationQueueName)
        val postalApplicationQueueUrl = localStackContainer.createSqsQueue(postalApplicationQueueName)
        val deletedProxyApplicationQueueUrl = localStackContainer.createSqsQueue(deletedProxyApplicationQueueName)
        val deletedPostalApplicationQueueUrl = localStackContainer.createSqsQueue(deletedPostalApplicationQueueName)

        val apiUrl = "http://${localStackContainer.host}:${localStackContainer.getMappedPort(DEFAULT_PORT)}"

        TestPropertyValues.of(
            "cloud.aws.sqs.endpoint=$apiUrl",
        ).applyTo(applicationContext)

        return LocalStackContainerSettings(
            apiUrl = apiUrl,
            proxyApplicationQueueUrl = proxyApplicationQueueUrl,
            postalApplicationQueueUrl = postalApplicationQueueUrl,
            deletedProxyApplicationQueueUrl = deletedProxyApplicationQueueUrl,
            deletedPostalApplicationQueueUrl = deletedPostalApplicationQueueUrl
        )
    }

    private fun GenericContainer<*>.createSqsQueue(queueName: String): String {
        val execInContainer = execInContainer(
            "awslocal",
            "sqs",
            "create-queue",
            "--queue-name",
            queueName,
            "--attributes",
            "VisibilityTimeout=1,MessageRetentionPeriod=5"
        )
        return execInContainer.stdout.let {
            objectMapper.readValue(it, Map::class.java)
        }.let {
            it["QueueUrl"] as String
        }
    }
}
data class LocalStackContainerSettings(
    val apiUrl: String,
    val proxyApplicationQueueUrl: String,
    val postalApplicationQueueUrl: String,
    val deletedProxyApplicationQueueUrl: String,
    val deletedPostalApplicationQueueUrl: String
)

