package uk.gov.dluhc.emsintegrationapi.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties.Companion.DEFAULT_PAGE_SIZE
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties.Companion.DEFAULT_PAGE_SIZE_MIN_VALUE
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties.Companion.MAX_PAGE_SIZE
import uk.gov.dluhc.emsintegrationapi.config.ApiProperties.Companion.MAX_PAGE_SIZE_MIN_VALUE

internal class ApiPropertiesTest {
    @Test
    fun `should not throw ApplicationConfigurationException if page size properties are valid`() {
        assertDoesNotThrow {
            ApiProperties(
                requestHeaderName = "something",
                defaultPageSize = 10,
                maxPageSize = 50,
                forceMaxPageSize = 50
            ).validate()
        }
    }

    @Test
    fun `should throw ApplicationConfigurationException if default page size is less than 10`() {
        val configurationException = assertThrows<ApplicationConfigurationException> {
            ApiProperties(
                requestHeaderName = "something",
                defaultPageSize = 1,
                maxPageSize = 50,
                forceMaxPageSize = 50
            ).validate()
        }
        assertThat(configurationException.message).isEqualTo("The $DEFAULT_PAGE_SIZE value must be greater than or equal to $DEFAULT_PAGE_SIZE_MIN_VALUE")
    }

    @Test
    fun `should throw ApplicationConfigurationException if max page size is less than 50`() {
        val applicationConfigurationException = assertThrows<ApplicationConfigurationException> {
            ApiProperties(
                requestHeaderName = "something",
                defaultPageSize = 10,
                maxPageSize = 1,
                forceMaxPageSize = 1
            ).validate()
        }
        assertThat(applicationConfigurationException.message).isEqualTo("The $MAX_PAGE_SIZE value must be greater than or equal to $MAX_PAGE_SIZE_MIN_VALUE")
    }

    @Test
    fun `should throw ApplicationConfigurationException if default page size is greater than max page size`() {
        val applicationConfigurationException = assertThrows<ApplicationConfigurationException> {
            ApiProperties(
                requestHeaderName = "something",
                defaultPageSize = 51,
                maxPageSize = 50,
                forceMaxPageSize = 50
            ).validate()
        }
        assertThat(applicationConfigurationException.message).isEqualTo("The $DEFAULT_PAGE_SIZE value (51) must be less than $MAX_PAGE_SIZE value (50)")
    }
}
