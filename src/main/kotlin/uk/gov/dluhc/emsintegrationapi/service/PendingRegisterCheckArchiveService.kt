package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus
import uk.gov.dluhc.emsintegrationapi.database.repository.RegisterCheckRepository
import uk.gov.dluhc.emsintegrationapi.exception.PendingRegisterCheckArchiveInvalidStatusException
import uk.gov.dluhc.emsintegrationapi.exception.PendingRegisterCheckNotFoundException
import java.time.Instant
import java.util.UUID

private val logger = KotlinLogging.logger { }

@Service
class PendingRegisterCheckArchiveService(
    private val registerCheckRepository: RegisterCheckRepository,
) {
    fun archiveIfStatusIsPending(correlationId: UUID?) {
        val corrid = correlationId ?: throw IllegalArgumentException("Correlation ID is null")
        val registerCheck = registerCheckRepository.findByCorrelationId(corrid)
        if (registerCheck == null) {
            logger.warn {
                "Pending register check for requestid:[$correlationId] not found"
            }
            throw PendingRegisterCheckNotFoundException(corrid)
        }
        if (registerCheck.status == CheckStatus.PENDING) {
            registerCheck.status = CheckStatus.ARCHIVED
            registerCheck.matchResultSentAt = Instant.now()
            registerCheckRepository.save(registerCheck)
        } else {
            logger.warn {
                "Register Check with correlationId $correlationId has status ${registerCheck.status} so cannot be archived (must be at status PENDING)"
            }
            throw PendingRegisterCheckArchiveInvalidStatusException(registerCheck.status)
        }
    }
}
