package uk.gov.dluhc.emsintegrationapi.config

import java.net.URI

data class LocalStackContainerSettings(
    val apiUrl: String,
    val queueUrlInitiateApplicantRegisterCheck: String,
    val queueUrlRemoveRegisterCheckData: String,
    val queueUrlRegisterCheckResultResponse: String,
) {
    val mappedQueueUrlInitiateApplicantRegisterCheck: String = toMappedUrl(queueUrlInitiateApplicantRegisterCheck, apiUrl)
    val mappedQueueUrlRegisterCheckResultResponse: String = toMappedUrl(queueUrlRegisterCheckResultResponse, apiUrl)
    val mappedQueueUrlRemoveRegisterCheckData: String = toMappedUrl(queueUrlRemoveRegisterCheckData, apiUrl)
    val sesMessagesUrl = "$apiUrl/_aws/ses"

    private fun toMappedUrl(rawUrlString: String, apiUrlString: String): String {
        val rawUrl = URI.create(rawUrlString)
        val apiUrl = URI.create(apiUrlString)
        return URI(
            rawUrl.scheme,
            rawUrl.userInfo,
            apiUrl.host,
            apiUrl.port,
            rawUrl.path,
            rawUrl.query,
            rawUrl.fragment
        ).toASCIIString()
    }
}
