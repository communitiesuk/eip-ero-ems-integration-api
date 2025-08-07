package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.dluhc.emsintegrationapi.database.repository.EroAbsentVoteHoldRepository

private val logger = KotlinLogging.logger {}

@Service
class RetrieveIsHoldEnabledForEroService(
    private val eroAbsentVoteHoldRepository: EroAbsentVoteHoldRepository,
    private val retrieveEroIdService: RetrieveEroIdService
) {
    fun getIsHoldEnabled(
        certificateSerialNumber: String,
    ): Boolean {
        logger.info { "Checking if hold is enabled for ERO with certificate serial number: $certificateSerialNumber" }
        val eroId = retrieveEroIdService.getEroIdFromCertificateSerial(certificateSerialNumber)
        val eroAbsentVoteHold = eroAbsentVoteHoldRepository.findById(eroId).orElse(null)
        return eroAbsentVoteHold?.holdEnabled ?: false
    }
}
