package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.CollectionUtils
import uk.gov.dluhc.emsintegrationapi.database.repository.RegisterCheckRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.RegisterCheckResultDataRepository
import uk.gov.dluhc.emsintegrationapi.messaging.dto.RegisterCheckRemovalDto
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class RegisterCheckRemovalService(
    private val registerCheckRepository: RegisterCheckRepository,
    private val registerCheckResultDataRepository: RegisterCheckResultDataRepository,
) {

    @Transactional
    fun removeRegisterCheckData(dto: RegisterCheckRemovalDto) {
        val correlationIds = removeRegisterCheck(dto)
        removeRegisterCheckResult(correlationIds)
    }

    private fun removeRegisterCheck(dto: RegisterCheckRemovalDto): Set<UUID> {
        with(dto) {
            logger.info("Finding RegisterCheck for removal by sourceReference: [$sourceReference]")
            val matchingRecords = registerCheckRepository.findBySourceReference(
                sourceReference = sourceReference,
            )
            if (CollectionUtils.isEmpty(matchingRecords)) {
                logger.info("Found no matching RegisterCheck to delete for sourceReference: [$sourceReference]")
            } else {
                logger.info("Deleting [${matchingRecords.size}] RegisterCheck record(s) for sourceReference: [$sourceReference]")
                registerCheckRepository.deleteAll(matchingRecords)
            }
            return matchingRecords.map { it.correlationId }.toSet()
        }
    }

    private fun removeRegisterCheckResult(correlationIds: Set<UUID>) {
        if (correlationIds.isNotEmpty()) {
            logger.info("Finding RegisterCheckResult records for removal for [${correlationIds.size}] correlationIds: $correlationIds")
            val matchingRecords = registerCheckResultDataRepository.findByCorrelationIdIn(correlationIds)

            if (CollectionUtils.isEmpty(matchingRecords)) {
                logger.info("Found no matching RegisterCheckResult to delete for [${correlationIds.size}] correlationIds: $correlationIds")
            } else {
                logger.info("Deleting [${matchingRecords.size}] RegisterCheckResult record(s) for [${correlationIds.size}] correlationIds: $correlationIds")
                registerCheckResultDataRepository.deleteAll(matchingRecords)
            }
        }
    }
}
