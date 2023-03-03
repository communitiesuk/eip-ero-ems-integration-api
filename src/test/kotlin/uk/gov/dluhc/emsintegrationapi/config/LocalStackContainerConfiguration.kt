package uk.gov.dluhc.emsintegrationapi.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.securitytoken.AWSSecurityTokenService
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
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
import java.net.URI

private val logger = KotlinLogging.logger {}

/**
 * Configuration class exposing beans for the LocalStack (AWS) environment.
 */
@Configuration
class LocalStackContainerConfiguration {

    companion object {
        // TODO the default region should be eu-west-2 but it currently causes the build to fail...
        const val DEFAULT_REGION = "us-east-1"
        const val DEFAULT_PORT = 4566
        const val DEFAULT_ACCESS_KEY_ID = "test"
        const val DEFAULT_SECRET_KEY = "test"

        val objectMapper = ObjectMapper()
        val localStackContainer: GenericContainer<*> = getInstance()
        private var container: GenericContainer<*>? = null

        /**
         * Creates and starts LocalStack configured with a basic (empty) SQS service.
         * Returns the container that can subsequently be used for further setup and configuration.
         */
        fun getInstance(): GenericContainer<*> {
            if (container == null) {
                container = GenericContainer(
                    DockerImageName.parse("localstack/localstack:1.1.0")
                ).withEnv(
                    mapOf(
                        "SERVICES" to "sqs, sts",
                        "AWS_DEFAULT_REGION" to DEFAULT_REGION,
                    )
                )
                    .withReuse(true)
                    .withExposedPorts(DEFAULT_PORT)
                    .withCreateContainerCmdModifier { it.withName("ems-integration-api-integration-test-localstack") }
                    .apply {
                        start()
                    }
            }

            return container!!
        }
    }

    @Bean
    fun awsBasicCredentialsProvider(): AwsCredentialsProvider =
        StaticCredentialsProvider.create(AwsBasicCredentials.create(DEFAULT_ACCESS_KEY_ID, DEFAULT_SECRET_KEY))

    /**
     * Uses the localstack container to configure the various services.
     *
     * @return a [LocalStackContainerSettings] bean encapsulating the various IDs etc of the configured container and services.
     */
    @Bean
    fun localStackContainerSqsSettings(
        applicationContext: ConfigurableApplicationContext,
        @Value("\${sqs.accepted-proxy-application-queue-name}") acceptedProxyApplicationQueueName: String,
        @Value("\${sqs.accepted-postal-application-queue-name}") acceptedPostalApplicationQueueName: String,
        @Value("\${sqs.deleted-proxy-application-queue-name}") deletedProxyApplicationQueueName: String,
        @Value("\${sqs.deleted-postal-application-queue-name}") deletedPostalApplicationQueueName: String,
    ): LocalStackContainerSettings {
        val acceptedProxyApplicationQueueUrl = localStackContainer.createSqsQueue(acceptedProxyApplicationQueueName)
        val acceptedPostalApplicationQueueUrl = localStackContainer.createSqsQueue(acceptedPostalApplicationQueueName)
        val deletedProxyApplicationQueueUrl = localStackContainer.createSqsQueue(deletedProxyApplicationQueueName)
        val deletedPostalApplicationQueueUrl = localStackContainer.createSqsQueue(deletedPostalApplicationQueueName)

        val apiUrl = "http://${localStackContainer.host}:${localStackContainer.getMappedPort(DEFAULT_PORT)}"

        TestPropertyValues.of(
            "cloud.aws.sqs.endpoint=$apiUrl",
        ).applyTo(applicationContext)

        return LocalStackContainerSettings(
            apiUrl = apiUrl,
            acceptedProxyApplicationQueueUrl = acceptedProxyApplicationQueueUrl,
            acceptedPostalApplicationQueueUrl = acceptedPostalApplicationQueueUrl,
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

    @Bean
    @Primary
    fun localStackStsClient(): AWSSecurityTokenService =
        AWSSecurityTokenServiceClient.builder()
            .withCredentials(
                AWSStaticCredentialsProvider(
                    BasicAWSCredentials(DEFAULT_ACCESS_KEY_ID, DEFAULT_SECRET_KEY)
                )
            )
            .withEndpointConfiguration(
                EndpointConfiguration(
                    "http://${localStackContainer.host}:${localStackContainer.getMappedPort(DEFAULT_PORT)}",
                    DEFAULT_REGION
                )
            )
            .build()
}
data class LocalStackContainerSettings(
    val apiUrl: String,
    val acceptedProxyApplicationQueueUrl: String,
    val acceptedPostalApplicationQueueUrl: String,
    val deletedProxyApplicationQueueUrl: String,
    val deletedPostalApplicationQueueUrl: String
) {
    val acceptedMappedProxyApplicationQueueUrl = toMappedUrl(acceptedProxyApplicationQueueUrl, apiUrl)
    val acceptedMappedPostalApplicationQueueUrl = toMappedUrl(acceptedPostalApplicationQueueUrl, apiUrl)
    val deletedMappedProxyApplicationQueueUrl = toMappedUrl(deletedProxyApplicationQueueUrl, apiUrl)
    val deletedMappedPostalApplicationQueueUrl = toMappedUrl(deletedPostalApplicationQueueUrl, apiUrl)

    private fun toMappedUrl(rawUrlString: String, apiUrlString: String): String {
        val rawUrl = URI.create(rawUrlString)
        val apiUrl = URI.create(apiUrlString)
        return URI(
            rawUrl.scheme,
            rawUrl.userInfo,
            apiUrl.host,
            apiUrl.port,
            rawUrl.path,
            rawUrl.query,
            rawUrl.fragment
        ).toASCIIString()
    }
}
