package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.dluhc.emsintegrationapi.client.ElectoralRegistrationOfficeManagementApiClient
import uk.gov.dluhc.emsintegrationapi.client.IerApiClient
import uk.gov.dluhc.emsintegrationapi.client.IerEroNotFoundException

private val logger = KotlinLogging.logger {}

@Service
class RetrieveGssCodeService(
    private val ierApiClient: IerApiClient,
) {
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
