package uk.gov.dluhc.emsintegrationapi.config

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

private val logger = KotlinLogging.logger {}
/**
 * Base class used to bring up the entire Spring ApplicationContext
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@AutoConfigureWebTestClient(timeout = "PT5M")
internal abstract class IntegrationTest {
    @Autowired
    protected lateinit var webTestClient: WebTestClient

    companion object {
        init {
            MySQLContainerConfiguration.getInstance()
            LocalStackContainerConfiguration.getInstance()
        }
    }
}
