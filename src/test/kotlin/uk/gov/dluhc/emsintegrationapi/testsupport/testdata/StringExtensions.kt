package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

fun String.replaceSpacesWith(replacement: String): String = replace(Regex("\\s+"), replacement)
