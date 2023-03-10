package uk.gov.dluhc.emsintegrationapi.testsupport

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import java.util.stream.Collectors
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.Path

/**
 * Validate the entity's max constraint violations
 * @param functionToBeTested : The function which will trigger the validation
 * @param fieldNamesAndSizeList: a list of field name and it's maximum size configured in entity
 */
fun validateEntityMaxSizeConstraintViolation(
    functionToBeTested: () -> Unit,
    fieldNamesAndSizeList: List<Pair<String, Int>>
) {
    validateMaxSizeErrorMessage(
        catchThrowableOfType(functionToBeTested, ConstraintViolationException::class.java),
        fieldNamesAndSizeList
    )
}

private fun validateMaxSizeErrorMessage(
    constraintViolationException: ConstraintViolationException,
    fieldNamesAndSizeList: List<Pair<String, Int>>
) {

    assertThat(constraintViolationException.constraintViolations.size).isEqualTo(fieldNamesAndSizeList.size)
    // Extract the validation error field name and the error message
    val validationErrors = extractFieldNameErrorMessage(constraintViolationException.constraintViolations)

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
