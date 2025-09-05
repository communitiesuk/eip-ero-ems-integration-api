package uk.gov.dluhc.emsintegrationapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.Clock

/**
 * The clock bean is provided by `TestClockConfiguration` in integration tests
 */
@Configuration
@Profile("!integration-test")
class ClockConfiguration {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
