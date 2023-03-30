package uk.gov.dluhc.emsintegrationapi.testsupport

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.extractor.Extractors

fun <T, R> validateObjects(input: T, output: R, vararg fieldNameToIgnore: String?) {
    assertThat(output).usingRecursiveComparison()
        .ignoringFields(*fieldNameToIgnore)
        .ignoringCollectionOrder()
        .ignoringActualNullFields()
        .ignoringExpectedNullFields()
        .isEqualTo(input)
}

fun <T, R> validateWithNull(mapperFunction: (T?) -> R) {
    assertThat(mapperFunction(null)).isNull()
}

fun <T, R> validateMappedValue(inputObject: T, mapperFunction: (T) -> R, expectedValue: R) {
    assertThat(mapperFunction(inputObject)).isEqualTo(expectedValue)
}

data class TestResult<T, R>(val input: T, val output: R)
/**
 * a function validates the object returned by the mapper function
 * @param buildInputFunction: a function creates the input object T
 * @param mapperFunction: the mapper function maps an input type T to R
 * @param fieldNamesToIgnore: the fields to be skipped while comparing the input object with the mapped object
 * @param verifications: a function consumes the input and mapped object, can be used for further assertion on a mapped object
 * @return instance of TestResult<T,R>
 */
fun <T, R> validateMappedObject(
    buildInputFunction: () -> T,
    mapperFunction: (T) -> R,
    vararg fieldNamesToIgnore: String?,
    verifications: (TestResult<T, R>) -> Unit = fun(testResult: TestResult<T, R>) { assertThat(testResult.output).isNotNull }
): TestResult<T, R> {
    val inputObject = buildInputFunction()
    val mappedObject = mapperFunction(inputObject)
    // Let us do additional checks first, if that fails then there is no point of doing full comparison
    verifications(TestResult(inputObject, mappedObject))
    // Validate the input object and mapped object
    validateObjects(inputObject, mappedObject, *fieldNamesToIgnore)
    return TestResult(inputObject, mappedObject)
}

fun <T> extractFieldValues(fromObject: T, vararg fieldNames: String): Array<Any> =
    Extractors.byName(*fieldNames).apply(fromObject)
        .toArray()

/**
 *
 * This function compares the field values extracted from the actual object and same fields (but names are different) from input object.
 * Note: This function is useful where the generated object attributes have values from multiple nested object from an input object
 */
fun <T, R> haveSameValues(
    actualObject: T,
    actualFieldsToBeCompared: Array<String>,
    inputObject: R,
    inputFieldsToBeCompared: Array<String>? = actualFieldsToBeCompared
) {
    assertThat(actualObject).isNotNull
    assertThat(inputObject).isNotNull
    assertThat(actualObject).extracting(*actualFieldsToBeCompared)
        .containsOnly(
            *extractFieldValues(inputObject, *inputFieldsToBeCompared!!)
        )
}

fun <T> haveNullValues(
    actualObject: T,
    actualFieldsToBeCompared: Array<String>,
) {
    assertThat(actualObject).isNotNull
    assertThat(actualObject).extracting(*actualFieldsToBeCompared).containsNull()
}
