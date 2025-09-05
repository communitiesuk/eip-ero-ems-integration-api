package uk.gov.dluhc.emsintegrationapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@Configuration
@Profile("integration-test")
class TestClockConfiguration {
    companion object {

        /**
         * This will be initialised when the tests are run, and will be what the code considers to be "now" assuming that
         * it uses a clock (e.g. `Instant.now(clock)`). For all integration tests, the clock is reset in a `@BeforeEach` to
         * this value.
         */
        val now: Instant = Instant.now().truncatedTo(ChronoUnit.SECONDS)
    }

    @Bean
    fun clock(): FlexibleClock = FlexibleClock(Clock.fixed(now, ZoneOffset.UTC))

    /**
     * A proxy wrapper class around [Clock] that allows us to swap out the underlying [Clock] instance for specific tests
     * when needed and then reset to the fixed clock based on a set time.
     *
     * Trying to use a `MockBean` or `SpyBean` for the [Clock] instance resulted in either stubbing or application context
     * errors. Using this proxy wrapper allows us to change the clock implementation without modifying the application context
     * or stubbing and avoids those issues.
     */
    class FlexibleClock(private var clock: Clock) : Clock() {

        override fun instant(): Instant = clock.instant()

        override fun withZone(zone: ZoneId?): Clock = FlexibleClock(clock.withZone(zone))

        override fun getZone(): ZoneId = clock.zone

        fun reset() {
            this.clock = fixed(now, ZoneOffset.UTC)
        }

        fun setClock(clock: Clock) {
            this.clock = clock
        }
    }
}
