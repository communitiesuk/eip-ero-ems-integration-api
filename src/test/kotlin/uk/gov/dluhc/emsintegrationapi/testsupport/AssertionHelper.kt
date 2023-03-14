package uk.gov.dluhc.emsintegrationapi.testsupport

import org.assertj.core.api.Assertions.assertThat

fun <T, R> validateObjects(input: T, output: R, vararg fieldNameToIgnore: String?) {
    assertThat(output).usingRecursiveComparison()
        .ignoringFields(*fieldNameToIgnore)
        .ignoringCollectionOrder()
        .ignoringActualNullFields()
        .isEqualTo(input)
}

fun <T, R> validateWithNull(mapperFunction: (T?) -> R) {
    assertThat(mapperFunction.invoke(null)).isNull()
}

fun <T, R> validateMappedValue(inputValue: T, mapperFunction: (T) -> R, expectedValue: R) {
    assertThat(mapperFunction.invoke(inputValue)).isEqualTo(expectedValue)
}

fun <T, R> validateMappedObject(inputObject: T, mapperFunction: (T) -> R, vararg fieldNameToIgnore: String?): R {
    // Invoke the mapper function
    val mappedObject = mapperFunction.invoke(inputObject)
    // Validate the input object and mapped object
    validateObjects(mappedObject, inputObject, *fieldNameToIgnore)
    return mappedObject
}

/**
 * a function validates the object returned by the mapper function
 * @param inputObject: input object to be mapped
 * @param mapperFunction: the mapper function
 * @param fieldNamesToIgnore: the fields to be skipped while comparing the mapped object
 * @param verifications: a function consumes the mapped object, can be used for further assertion on a mapped object
 */
fun <T, R> validateMappedObject(
    inputObject: T,
    mapperFunction: (T) -> R,
    vararg fieldNamesToIgnore: String?,
    verifications: (R) -> Unit,
): R =
    validateMappedObject(inputObject, mapperFunction, *fieldNamesToIgnore).let {
        // Invoke other verifications
        verifications(it)
        it
    }
