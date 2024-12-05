package uk.gov.dluhc.emsintegrationapi.testsupport

import jakarta.validation.ConstraintViolation
import jakarta.validation.Path
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import java.util.stream.Collectors

val validator: Validator = Validation.buildDefaultValidatorFactory().validator

fun <T> isValid(objectToBeValidated: T) = validator.validate(objectToBeValidated).isEmpty()

fun <T> validateMaxSizeErrorMessage(
    objectToBeValidated: T,
    fieldNamesAndSizeList: List<Pair<String, Int>>
) {
    val constraintViolations = validator.validate(objectToBeValidated)
    assertThat(constraintViolations.size).isEqualTo(fieldNamesAndSizeList.size)
    // Extract the validation error field name and the error message
    val validationErrors = extractFieldNameErrorMessage(constraintViolations)

    // Now validate the expected error message
    fieldNamesAndSizeList.forEach { expectedError ->
        val expectedMessage = "size must be between 0 and ${expectedError.second}"
        assertThat(validationErrors!!.first { it.first.toString() == expectedError.first }.second).isEqualTo(
            expectedMessage
        )
    }
}

private fun extractFieldNameErrorMessage(constraintViolations: Set<ConstraintViolation<*>>): List<Pair<Path, String>>? =
    constraintViolations.stream().map { Pair(it.propertyPath, it.message) }.collect(Collectors.toList())
