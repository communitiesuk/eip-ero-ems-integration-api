package uk.gov.dluhc.emsintegrationapi.service

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import uk.gov.dluhc.emsintegrationapi.client.IerApiClient
import uk.gov.dluhc.emsintegrationapi.config.ERO_GSS_CODE_BY_ERO_ID_CACHE

@Service
class RetrieveGssCodeService(
    private val ierApiClient: IerApiClient,
    private val eroService: EroService,
) {
    @Cacheable(ERO_GSS_CODE_BY_ERO_ID_CACHE)
    fun getGssCodeFromCertificateSerial(certificateSerial: String) =
        eroService.lookupGssCodesForEro(ierApiClient.getEroIdentifier(certificateSerial).eroId!!)
}
