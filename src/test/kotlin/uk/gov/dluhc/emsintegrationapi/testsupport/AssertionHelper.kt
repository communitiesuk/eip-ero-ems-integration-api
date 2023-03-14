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

fun <T, R> validateMappedObject(
    inputObject: T,
    mapperFunction: (T) -> R,
    vararg fieldNameToIgnore: String?,
    verifications: (R) -> Unit,
): R =
    validateMappedObject(inputObject, mapperFunction, *fieldNameToIgnore).let {
        // Invoke other verifications
        verifications(it)
        it
    }
