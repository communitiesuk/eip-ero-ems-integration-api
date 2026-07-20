package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.dluhc.emsintegrationapi.client.IerApiClient
import uk.gov.dluhc.emsintegrationapi.service.dto.EroSummary

private val logger = KotlinLogging.logger {}

@Service
class RetrieveEroDetailsService(
    private val ierApiClient: IerApiClient,
) {
    /**
     * Returns an [EroSummary] per GSS code, as held by IER.
     * Returns an empty map if the EROs cannot be retrieved, so that callers can still
     * produce summaries without ERO details when IER is unavailable.
     */
    fun getEroSummaryByGssCode(): Map<String, EroSummary> =
        try {
            ierApiClient.getEros()
                .flatMap { ero ->
                    ero.localAuthorities.map {
                        // TODO EROPSPT-741: Map ero.emsVendor once IER adds it to the /eros endpoint
                        it.gssCode to EroSummary(name = ero.name, eroId = ero.eroIdentifier, emsVendor = null)
                    }
                }
                .toMap()
        } catch (e: Exception) {
            logger.warn(e) { "Unable to retrieve ERO details from IER, summaries will not include ERO name or EMS vendor" }
            emptyMap()
        }
}
