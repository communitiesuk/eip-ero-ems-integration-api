package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import uk.gov.dluhc.emsintegrationapi.client.IerApiClient
import uk.gov.dluhc.emsintegrationapi.client.IerEroNotFoundException
import uk.gov.dluhc.emsintegrationapi.client.IerTooManyErosFoundException
import uk.gov.dluhc.emsintegrationapi.config.ERO_ID_FROM_CERTIFICATE_MAPPING_CACHE

private val logger = KotlinLogging.logger {}

@Service
class RetrieveEroIdService(
    private val ierApiClient: IerApiClient,
) {
    @Cacheable(ERO_ID_FROM_CERTIFICATE_MAPPING_CACHE)
    fun getEroIdFromCertificateSerial(certificateSerial: String): String {
        val eros = ierApiClient.getEros()
        val erosMatchingCertificateSerial = eros.filter { it.activeClientCertificateSerials.contains(certificateSerial) }
        if (erosMatchingCertificateSerial.isEmpty()) {
            throw IerEroNotFoundException(certificateSerial)
                .also { logger.warn { "No ERO found matching certificate serial $certificateSerial" } }
        }
        if (erosMatchingCertificateSerial.size > 1) {
            throw IerTooManyErosFoundException(certificateSerial)
                .also { logger.warn { "Multiple ERO's matching certificate serial $certificateSerial" } }
        }
        return erosMatchingCertificateSerial.map { ero -> ero.eroIdentifier }[0]
    }
}
