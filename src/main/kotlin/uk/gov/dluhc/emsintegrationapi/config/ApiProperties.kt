package uk.gov.dluhc.emsintegrationapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ApiProperties(
    @Value("\${$DEFAULT_PAGE_SIZE}")
    val defaultPageSize: Int,
    @Value("\${$REQUEST_HEADER_NAME}")
    val requestHeaderName: String
) {
    companion object {
        private const val API_CONFIG_PREFIX = "dluhc"
        const val DEFAULT_PAGE_SIZE = "$API_CONFIG_PREFIX.default-page-size"
        const val REQUEST_HEADER_NAME = "$API_CONFIG_PREFIX.request.header.name"
    }
}
