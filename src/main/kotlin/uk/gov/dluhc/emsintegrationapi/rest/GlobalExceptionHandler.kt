package uk.gov.dluhc.emsintegrationapi.rest

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
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
        logger.warn { "Error occurred = $errorMessage" }
        return errorMessage
    }
}
