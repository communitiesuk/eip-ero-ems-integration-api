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
    fun getGssCodesFromCertificateSerial(certificateSerial: String): List<String> {
        val eros = ierApiClient.getEros()
        val erosMatchingCertificateSerial = eros.filter { it.activeClientCertificateSerials.contains(certificateSerial) }
        if (erosMatchingCertificateSerial.isEmpty()) {
            throw IerEroNotFoundException(certificateSerial)
                .also { logger.warn { "No ERO found matching certificate serial $certificateSerial" } }
        }
        return erosMatchingCertificateSerial.flatMap { ero -> ero.localAuthorities.map { it.gssCode } }
    }
}
