package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import uk.gov.dluhc.emsintegrationapi.client.IerApiClient
import uk.gov.dluhc.emsintegrationapi.client.IerEroNotFoundException
import uk.gov.dluhc.emsintegrationapi.config.ERO_GSS_CODE_BY_ERO_ID_CACHE

private val logger = KotlinLogging.logger {}

@Service
class RetrieveGssCodeService(
    private val ierApiClient: IerApiClient,
) {
    @Cacheable(ERO_GSS_CODE_BY_ERO_ID_CACHE)
    fun getGssCodeFromCertificateSerial(certificateSerial: String): List<String> {
        val eros = ierApiClient.getEros()
        val ero = eros.find { it.activeClientCertificateSerials.contains(certificateSerial) }
        if (ero == null) {
            throw IerEroNotFoundException(certificateSerial)
                .also { logger.warn { "Certificate serial not found" } }
        }
        return ero.localAuthorities.map { it.gssCode }
    }
}
