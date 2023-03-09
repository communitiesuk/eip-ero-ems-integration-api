package uk.gov.dluhc.emsintegrationapi.testsupport

fun String.replaceSpacesWith(replacement: String): String = replace(Regex("\\s+"), replacement)
