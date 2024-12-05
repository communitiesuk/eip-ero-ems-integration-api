package uk.gov.dluhc.emsintegrationapi.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

const val IER_ELECTORAL_REGISTRATION_OFFICES_CACHE = "ier-eros"
const val ERO_CERTIFICATE_MAPPING_CACHE = "eroCertificateMappings"
const val ERO_GSS_CODE_BY_ERO_ID_CACHE = "eroGssCodesByEroId"

@Configuration
@EnableCaching
class CachingConfiguration {
    @Value("\${caching.time-to-live}")
    private lateinit var timeToLive: Duration

    @Bean
    fun cacheManager(): CacheManager {
        return CaffeineCacheManager()
            .apply {
                setCaffeine(Caffeine.newBuilder().expireAfterWrite(timeToLive))
                setCacheNames(listOf(IER_ELECTORAL_REGISTRATION_OFFICES_CACHE))
            }
    }
}
