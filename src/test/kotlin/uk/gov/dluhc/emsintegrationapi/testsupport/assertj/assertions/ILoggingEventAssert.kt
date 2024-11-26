package uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions

import ch.qos.logback.classic.spi.ILoggingEvent
import org.apache.commons.lang3.StringUtils.isBlank
import org.assertj.core.api.AbstractAssert
import uk.gov.dluhc.emsintegrationapi.config.CORRELATION_ID
import uk.gov.dluhc.emsintegrationapi.config.MESSAGE_ID
import uk.gov.dluhc.emsintegrationapi.config.REQUEST_ID

class ILoggingEventAssert(actual: ILoggingEvent?) :
    AbstractAssert<ILoggingEventAssert, ILoggingEvent?>(actual, ILoggingEventAssert::class.java) {

    companion object {
        fun assertThat(actual: ILoggingEvent?) = ILoggingEventAssert(actual)
    }

    fun hasCorrelationId(expected: String?): ILoggingEventAssert {
        isNotNull
        with(actual!!) {
            if (mdcPropertyMap[CORRELATION_ID] != expected) {
                failWithMessage("Expected correlation ID to be $expected, but was $mdcPropertyMap[CORRELATION_ID]")
            }
        }
        return this
    }

    fun hasAnyCorrelationId(): ILoggingEventAssert {
        isNotNull
        with(actual!!) {
            if (isBlank(mdcPropertyMap[CORRELATION_ID])) {
                failWithMessage("Expected log message to have a correlation ID, but it did not")
            }
        }
        return this
    }

    fun hasRequestId(expected: String?): ILoggingEventAssert {
        isNotNull
        with(actual!!) {
            if (mdcPropertyMap[REQUEST_ID] != expected) {
                failWithMessage("Expected request ID to be $expected, but was ${mdcPropertyMap[REQUEST_ID]}")
            }
        }
        return this
    }

    fun hasNoRequestId(): ILoggingEventAssert {
        isNotNull
        with(actual!!) {
            if (!isBlank(mdcPropertyMap[REQUEST_ID])) {
                failWithMessage("Expected log message to have no request ID, but had request id: ${mdcPropertyMap[REQUEST_ID]}")
            }
        }
        return this
    }

    fun hasMessageId(expected: String?): ILoggingEventAssert {
        isNotNull
        with(actual!!) {
            if (mdcPropertyMap[MESSAGE_ID] != expected) {
                failWithMessage("Expected message ID to be $expected, but was ${mdcPropertyMap[MESSAGE_ID]}")
            }
        }
        return this
    }

    fun hasAnyMessageId(): ILoggingEventAssert {
        isNotNull
        with(actual!!) {
            if (isBlank(mdcPropertyMap[MESSAGE_ID])) {
                failWithMessage("Expected log message to have a message ID, but it did not")
            }
        }
        return this
    }
}
