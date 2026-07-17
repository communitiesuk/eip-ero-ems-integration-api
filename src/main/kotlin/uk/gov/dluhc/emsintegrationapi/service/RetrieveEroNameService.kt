package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.dluhc.emsintegrationapi.client.IerApiClient

private val logger = KotlinLogging.logger {}

@Service
class RetrieveEroNameService(
    private val ierApiClient: IerApiClient,
) {
    /**
     * Returns the name of the ERO managing each GSS code, as held by IER.
     * Returns an empty map if the EROs cannot be retrieved, so that callers can still
     * produce summaries without ERO names when IER is unavailable.
     */
    fun getEroNamesByGssCode(): Map<String, String?> =
        try {
            ierApiClient.getEros()
                .flatMap { ero -> ero.localAuthorities.map { it.gssCode to ero.name } }
                .toMap()
        } catch (e: Exception) {
            logger.warn(e) { "Unable to retrieve ERO names from IER, summaries will not include ERO names" }
            emptyMap()
        }
}
