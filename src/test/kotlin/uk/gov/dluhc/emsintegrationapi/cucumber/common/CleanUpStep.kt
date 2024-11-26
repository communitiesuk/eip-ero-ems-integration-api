package uk.gov.dluhc.emsintegrationapi.cucumber.common

import io.cucumber.java8.En
import uk.gov.dluhc.emsintegrationapi.testsupport.TestLogAppender

class CleanUpStep : En {
    init {
        Before(TestLogAppender::reset)
    }
}
