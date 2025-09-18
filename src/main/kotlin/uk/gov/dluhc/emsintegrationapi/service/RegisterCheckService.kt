package uk.gov.dluhc.emsintegrationapi.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus.EXACT_MATCH
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus.EXPIRED
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus.NOT_STARTED
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus.PARTIAL_MATCH
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus.PENDING_DETERMINATION
import uk.gov.dluhc.emsintegrationapi.database.entity.RegisterCheck
import uk.gov.dluhc.emsintegrationapi.database.entity.RegisterCheckResultData
import uk.gov.dluhc.emsintegrationapi.database.repository.RegisterCheckRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.RegisterCheckResultDataRepository
import uk.gov.dluhc.emsintegrationapi.dto.PendingRegisterCheckDto
import uk.gov.dluhc.emsintegrationapi.dto.RegisterCheckResultDto
import uk.gov.dluhc.emsintegrationapi.dto.RegisterCheckStatus
import uk.gov.dluhc.emsintegrationapi.exception.GssCodeMismatchException
import uk.gov.dluhc.emsintegrationapi.exception.PendingRegisterCheckNotFoundException
import uk.gov.dluhc.emsintegrationapi.exception.RegisterCheckUnexpectedStatusException
import uk.gov.dluhc.emsintegrationapi.mapper.PendingRegisterCheckMapper
import uk.gov.dluhc.emsintegrationapi.mapper.RegisterCheckResultMapper
import uk.gov.dluhc.emsintegrationapi.messaging.mapper.RegisterCheckResultMessageMapper
import uk.gov.dluhc.messagingsupport.MessageQueue
import uk.gov.dluhc.registercheckerapi.messaging.models.RegisterCheckResultMessage
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class RegisterCheckService(
    private val retrieveGssCodeService: RetrieveGssCodeService,
    private val registerCheckRepository: RegisterCheckRepository,
    private val registerCheckResultDataRepository: RegisterCheckResultDataRepository,
    private val pendingRegisterCheckMapper: PendingRegisterCheckMapper,
    private val registerCheckResultMapper: RegisterCheckResultMapper,
    private val registerCheckResultMessageMapper: RegisterCheckResultMessageMapper,
    private val matchStatusResolver: MatchStatusResolver,
    private val registerCheckResultResponseQueue: MessageQueue<RegisterCheckResultMessage>
) {
    @Transactional(readOnly = true)
    fun getPendingRegisterChecks(
        certificateSerial: String,
        pageSize: Int,
    ): List<PendingRegisterCheckDto> {
        val gssCodes = retrieveGssCodeService.getGssCodesFromCertificateSerial(certificateSerial)
        return registerCheckRepository
            .findPendingEntriesByGssCodes(gssCodes, pageSize)
            .map(pendingRegisterCheckMapper::registerCheckEntityToPendingRegisterCheckDto)
    }

    @Transactional
    fun save(pendingRegisterCheckDto: PendingRegisterCheckDto) {
        pendingRegisterCheckMapper
            .pendingRegisterCheckDtoToRegisterCheckEntity(pendingRegisterCheckDto)
            .let(registerCheckRepository::save)
    }

    @Transactional
    fun auditRequestBody(
        correlationId: UUID,
        requestBodyJson: String,
    ) {
        registerCheckResultDataRepository.save(
            RegisterCheckResultData(
                correlationId = correlationId,
                requestBody = requestBodyJson,
            ),
        )
        logger.debug { "Register check POST request payload audited/saved for requestId:[$correlationId]" }
    }

    @Transactional
    fun updatePendingRegisterCheck(
        certificateSerial: String,
        registerCheckResultDto: RegisterCheckResultDto,
    ): RegisterCheck {
        validateGssCodeMatch(certificateSerial, registerCheckResultDto.gssCode)
        return getPendingRegisterCheck(registerCheckResultDto.correlationId).apply {
            when (status) {
                CheckStatus.PENDING -> recordCheckResult(registerCheckResultDto, this)
                else -> throw RegisterCheckUnexpectedStatusException(correlationId, status)
                    .also {
                        logger.warn {
                            "Register check with correlationId:[$correlationId] is in status [$status] and cannot be set to [${registerCheckResultDto.registerCheckStatus}]"
                        }
                    }
            }
        }
    }

    fun sendConfirmRegisterCheckResultMessage(registerCheck: RegisterCheck) {
        with(registerCheckResultMessageMapper.fromRegisterCheckEntityToRegisterCheckResultMessage(registerCheck)) {
            logger.info {
                "Publishing ConfirmRegisterCheckResultMessage with sourceReferenceApplicationId:[$sourceReference], " +
                    "sourceCorrelationId:[$sourceCorrelationId] for correlationId:[${registerCheck.correlationId}]"
            }
            registerCheckResultResponseQueue.submit(this)
        }
    }

    private fun recordCheckResult(
        registerCheckResultDto: RegisterCheckResultDto,
        registerCheck: RegisterCheck,
    ) {
        with(registerCheckResultDto) {
            registerCheckStatus = matchStatusResolver.resolveStatus(this, registerCheck)
            val matches =
                registerCheckMatches?.map(registerCheckResultMapper::fromDtoToRegisterCheckMatchEntity) ?: emptyList()
            when (registerCheckStatus!!) {
                RegisterCheckStatus.NO_MATCH -> registerCheck.recordNoMatch(matchResultSentAt)
                RegisterCheckStatus.EXACT_MATCH ->
                    registerCheck.recordExactMatch(
                        EXACT_MATCH,
                        matchResultSentAt,
                        matches.first(),
                        historicalSearchEarliestDate,
                    )

                RegisterCheckStatus.PARTIAL_MATCH ->
                    registerCheck.recordExactMatch(
                        PARTIAL_MATCH,
                        matchResultSentAt,
                        matches.first(),
                        historicalSearchEarliestDate,
                    )

                RegisterCheckStatus.PENDING_DETERMINATION ->
                    registerCheck.recordExactMatch(
                        PENDING_DETERMINATION,
                        matchResultSentAt,
                        matches.first(),
                        historicalSearchEarliestDate,
                    )

                RegisterCheckStatus.EXPIRED ->
                    registerCheck.recordExactMatch(
                        EXPIRED,
                        matchResultSentAt,
                        matches.first(),
                        historicalSearchEarliestDate,
                    )

                RegisterCheckStatus.NOT_STARTED ->
                    registerCheck.recordExactMatch(
                        NOT_STARTED,
                        matchResultSentAt,
                        matches.first(),
                        historicalSearchEarliestDate,
                    )

                RegisterCheckStatus.MULTIPLE_MATCH ->
                    registerCheck.recordMultipleMatches(
                        matchResultSentAt,
                        matchCount,
                        matches,
                        historicalSearchEarliestDate,
                    )

                RegisterCheckStatus.TOO_MANY_MATCHES ->
                    registerCheck.recordTooManyMatches(
                        matchResultSentAt,
                        matchCount,
                        matches,
                        historicalSearchEarliestDate,
                    )
            }.also {
                logger.info {
                    "Updated register check status to [${registerCheck.status}], matchCount to [${registerCheck.matchCount}], " +
                        "matchResultSentAt to [${registerCheck.matchResultSentAt}] for correlationId:[${registerCheck.correlationId}]"
                }
            }
        }
    }

    fun getPendingRegisterCheck(correlationId: UUID): RegisterCheck =
        registerCheckRepository.findByCorrelationId(correlationId)
            ?: throw PendingRegisterCheckNotFoundException(correlationId)
                .also { logger.warn { it.message } }

    private fun validateGssCodeMatch(
        certificateSerial: String,
        requestGssCode: String,
    ) {
        if (requestGssCode !in retrieveGssCodeService.getGssCodesFromCertificateSerial(certificateSerial)) {
            throw GssCodeMismatchException(certificateSerial, requestGssCode)
                .also { logger.warn { it.message } }
        }
    }
}
