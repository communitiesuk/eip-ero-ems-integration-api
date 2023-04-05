package uk.gov.dluhc.emsintegrationapi.config

import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.dluhc.emsintegrationapi.constants.ApplicationConstants.Companion.PAGE_SIZE_PARAM

class ApiClient(private val webClient: WebTestClient, private val apiProperties: ApiProperties) {
    companion object {
        const val DEFAULT_SERIAL_NUMBER = "543219999"

        fun buildUriStringWithQueryParam(path: String, pageSize: Int) = UriComponentsBuilder.fromUriString(path)
            .queryParam(PAGE_SIZE_PARAM, pageSize)
            .build().toUriString()

        fun <T> validateStatusAndGetResponse(
            apiResponse: WebTestClient.ResponseSpec,
            expectedHttpStatus: Int,
            type: Class<T>
        ): T? =
            apiResponse.expectStatus().isEqualTo(expectedHttpStatus).returnResult(type).responseBody.blockFirst()
    }

    fun get(
        uri: String,
        attachSerialNumber: Boolean = true,
        serialNumber: String = DEFAULT_SERIAL_NUMBER,
    ): WebTestClient.ResponseSpec {
        val requestHeadersSpec = if (attachSerialNumber) withSerialNumber(webClient.get().uri(uri), serialNumber)
        else webClient.get().uri(uri)
        return requestHeadersSpec
            .exchange()
    }

    fun <T> get(
        uri: String,
        responseType: Class<T>,
        attachSerialNumber: Boolean = true,
        serialNumber: String = DEFAULT_SERIAL_NUMBER
    ): T? {
        return get(uri, attachSerialNumber, serialNumber).expectStatus().isOk
            .returnResult(responseType).responseBody.blockFirst()
    }

    private fun withSerialNumber(
        requestHeadersSpec: WebTestClient.RequestHeadersSpec<*>,
        serialNumber: String
    ): WebTestClient.RequestHeadersSpec<*> = requestHeadersSpec.header(apiProperties.requestHeaderName, serialNumber)
}
