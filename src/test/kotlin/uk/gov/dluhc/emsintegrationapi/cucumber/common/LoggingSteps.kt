package uk.gov.dluhc.emsintegrationapi.cucumber.common

import ch.qos.logback.classic.Level
import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import uk.gov.dluhc.emsintegrationapi.testsupport.TestLogAppender

open class LoggingSteps : En {
    init {
        Then("the message {string} is logged") { message: String ->
            assertThat(TestLogAppender.hasLog(message, Level.INFO)).isTrue
        }
    }
}
