package uk.gov.dluhc.emsintegrationapi.exception

/**
 * Thrown if gss codes returned from the IER API does not match with the gss code from the request.
 */
class GssCodeMismatchException(certificateSerial: String, requestGssCode: String) :
    RuntimeException("Request gssCode:[$requestGssCode] does not match with gssCode for certificateSerial:[$certificateSerial]")
