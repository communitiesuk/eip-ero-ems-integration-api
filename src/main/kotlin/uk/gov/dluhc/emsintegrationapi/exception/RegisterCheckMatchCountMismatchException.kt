package uk.gov.dluhc.emsintegrationapi.exception

/**
 * Thrown if [registerCheckMatchCount] in the POST body payload mismatches with number of records within [registerCheckMatches] list
 */
class RegisterCheckMatchCountMismatchException(errorMessage: String) : RuntimeException(errorMessage)
