package uk.gov.dluhc.emsintegrationapi.client

import org.awaitility.kotlin.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import uk.gov.dluhc.emsintegrationapi.config.IER_ELECTORAL_REGISTRATION_OFFICES_CACHE
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest
import uk.gov.dluhc.emsintegrationapi.service.RetrieveGssCodeService
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class IerApiClientIntegrationTest : IntegrationTest() {

    @Value("\${caching.time-to-live}")
    private lateinit var cacheEntryTimeToLive: Duration

    @Autowired
    private lateinit var ierApiClient: IerApiClient

    @Autowired
    private lateinit var retrieveGssCodeService: RetrieveGssCodeService

    @BeforeEach
    fun beforeEachTest() {
        cacheManager.getCache(IER_ELECTORAL_REGISTRATION_OFFICES_CACHE)?.clear()
    }

    @Test
    fun `should get EROs`() {
        // Given
        wireMockService.stubIerApiGetEros()

        // When
        ierApiClient.getEros()

        // Then
        wireMockService.verifyIerGetErosCalled(1)
    }

    @Test
    fun `Get ERO result is cached`() {
        // Given
        wireMockService.stubIerApiGetEros()

        // When
        repeat(3) { ierApiClient.getEros() }

        // Then
        wireMockService.verifyIerGetErosCalled(1)
    }

    @Test
    fun `should refresh cached IER EROs after time-to-live expires`() {
        // Given
        wireMockService.stubIerApiGetEros()

        await.atMost(cacheEntryTimeToLive.plusMillis(500)).untilAsserted {
            // When
            ierApiClient.getEros()

            // Then
            wireMockService.verifyIerGetErosCalled(2)
        }
    }

    @Test
    fun `Get ERO cache is synced`() {
        // This ensures that when the cache expires only a single request is made to IER
        // (instead of a flood until the cache is updated)

        // Given
        wireMockService.stubIerApiGetEros(responseTimeoutInSeconds = 2)
        val executor = Executors.newFixedThreadPool(2)

        // When
        executor.execute { ierApiClient.getEros() }
        executor.execute { ierApiClient.getEros() }
        executor.shutdown()
        executor.awaitTermination(1, TimeUnit.MINUTES)

        // Then
        wireMockService.verifyIerGetErosCalled(1)
    }
}
