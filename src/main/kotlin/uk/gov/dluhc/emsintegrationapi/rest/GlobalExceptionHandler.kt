package uk.gov.dluhc.emsintegrationapi.rest

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import uk.gov.dluhc.emsintegrationapi.client.IerEroNotFoundException
import uk.gov.dluhc.emsintegrationapi.client.IerGeneralException
import uk.gov.dluhc.emsintegrationapi.exception.ResourceNotFoundException
import uk.gov.dluhc.emsintegrationapi.service.ApplicationNotFoundException
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException

private val logger = KotlinLogging.logger { }

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleConstraintViolationException(e: ConstraintViolationException): String {
        val violations: Set<ConstraintViolation<*>> = e.constraintViolations
        val errorMessage = if (violations.isNotEmpty()) {
            violations.joinToString(" ") { it.message }
        } else {
            "Validation error"
        }
        logger.warn { "Validation error occurred: $errorMessage" }
        return errorMessage
    }

    @ExceptionHandler(value = [ApplicationNotFoundException::class, IerEroNotFoundException::class])
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleApplicationNotFound(e: ResourceNotFoundException): String {
        logger.error { e.message }
        return e.message
    }

    @ExceptionHandler(IerGeneralException::class)
    fun handleIerApiException(ierApiException: IerGeneralException): String {
        logger.error(ierApiException.message)
        return ierApiException.message!!
    }
}
