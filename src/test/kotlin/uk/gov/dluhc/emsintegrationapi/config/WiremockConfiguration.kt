package uk.gov.dluhc.emsintegrationapi.config

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.util.ResourceUtils
import javax.net.ssl.TrustManagerFactory

private val logger = KotlinLogging.logger {}

@Configuration
class WiremockConfiguration {

    @Bean
    fun wireMockServer(
        applicationContext: ConfigurableApplicationContext,
        @Value("\${logWiremockRequests:false}") logWiremockRequests: Boolean
    ): WireMockServer =
        WireMockServer(
            options()
                .dynamicPort()
                .dynamicHttpsPort()
                // Valtech has shared their public cert with DWP who will have added it to their trust store
                // DWP only allow clients that are trusted to use its service through mTLS
                .needClientAuth(true)
                // keytool -import -trustcacerts -file client-cert.pem -keypass changeit -storepass changeit -keystore truststore.jks
                .trustStorePath(ResourceUtils.getFile("classpath:certificates/dwp/truststore.jks").absolutePath)
                .trustStorePassword("changeit")
                .trustStoreType("JKS")
        ).apply {
            if (logWiremockRequests) {
                addMockServiceRequestListener { request: Request, _: Response ->
                    val formattedHeaders = request.headers.all().joinToString("\n") {
                        "${it.key()}: ${it.values().joinToString(", ")}"
                    }
                    val logMessage = StringBuilder()
                        .appendLine("Request sent to wiremock:")
                        .appendLine("${request.method} ${request.absoluteUrl}")
                        .appendLine(formattedHeaders)
                        .appendLine()
                        .appendLine(request.bodyAsString)
                    logger.info { logMessage }
                }
            }
            start()
            TestPropertyValues.of(
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:${this.port()}/cognito/.well-known/jwks.json",
            ).applyTo(applicationContext)
        }

    @Bean
    @Primary
    fun wireMockTrustManagerFactory(): TrustManagerFactory =
        InsecureTrustManagerFactory.INSTANCE
}
