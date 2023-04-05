package uk.gov.dluhc.emsintegrationapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class ApiProperties(
    @Value("\${$REQUEST_HEADER_NAME}")
    val requestHeaderName: String,
    @Value("\${$DEFAULT_PAGE_SIZE}")
    val defaultPageSize: Int,
    @Value("\${$MAX_PAGE_SIZE}")
    val maxPageSize: Int,
) {
    companion object {
        private const val API_CONFIG_PREFIX = "dluhc"
        const val DEFAULT_PAGE_SIZE = "$API_CONFIG_PREFIX.default-page-size"
        const val MAX_PAGE_SIZE = "$API_CONFIG_PREFIX.max-page-size"
        const val REQUEST_HEADER_NAME = "$API_CONFIG_PREFIX.request.header.name"
        const val MAX_PAGE_SIZE_MIN_VALUE = 50
        const val DEFAULT_PAGE_SIZE_MIN_VALUE = 10
    }

    @PostConstruct
    fun validate() {
        when {
            maxPageSize < MAX_PAGE_SIZE_MIN_VALUE -> throw ApplicationConfigurationException("The $MAX_PAGE_SIZE value must be greater than or equal to $MAX_PAGE_SIZE_MIN_VALUE")
            defaultPageSize < DEFAULT_PAGE_SIZE_MIN_VALUE -> throw ApplicationConfigurationException("The $DEFAULT_PAGE_SIZE value must be greater than or equal to $DEFAULT_PAGE_SIZE_MIN_VALUE")
            defaultPageSize > maxPageSize -> throw ApplicationConfigurationException("The $DEFAULT_PAGE_SIZE value ($defaultPageSize) must be less than $MAX_PAGE_SIZE value ($maxPageSize)")
        }
    }
}
