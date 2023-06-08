package uk.gov.dluhc.emsintegrationapi.config

import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.dluhc.emsintegrationapi.constants.ApplicationConstants.Companion.PAGE_SIZE_PARAM
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationResponse

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

    fun delete(
        uri: String,
        attachSerialNumber: Boolean = true,
        serialNumber: String = DEFAULT_SERIAL_NUMBER,
        request: EMSApplicationResponse = EMSApplicationResponse()
    ): WebTestClient.ResponseSpec {
        val emsURI = webClient.delete().uri(uri)
        val requestHeadersSpec = if (attachSerialNumber) withSerialNumber(emsURI, serialNumber)
        else emsURI
        return requestHeadersSpec
            .exchange()
    }

    fun postEmsApplication(
        uri: String,
        attachSerialNumber: Boolean = true,
        serialNumber: String = DEFAULT_SERIAL_NUMBER,
        request: EMSApplicationResponse = EMSApplicationResponse()
    ): WebTestClient.ResponseSpec {
        val emsURI = webClient.post().uri(uri).contentType(MediaType.APPLICATION_JSON).bodyValue(request)
        val requestHeadersSpec = if (attachSerialNumber) withSerialNumber(emsURI, serialNumber)
        else emsURI
        return requestHeadersSpec.exchange()
    }

    fun putEmsApplication(
        uri: String,
        attachSerialNumber: Boolean = true,
        serialNumber: String = DEFAULT_SERIAL_NUMBER,
        request: EMSApplicationResponse = EMSApplicationResponse()
    ): WebTestClient.ResponseSpec {
        val emsURI = webClient.put().uri(uri).contentType(MediaType.APPLICATION_JSON).bodyValue(request)
        val requestHeadersSpec = if (attachSerialNumber) withSerialNumber(emsURI, serialNumber)
        else emsURI
        return requestHeadersSpec.exchange()
    }

    private fun withSerialNumber(
        requestHeadersSpec: WebTestClient.RequestHeadersSpec<*>,
        serialNumber: String
    ): WebTestClient.RequestHeadersSpec<*> = requestHeadersSpec.header(apiProperties.requestHeaderName, serialNumber)
}
