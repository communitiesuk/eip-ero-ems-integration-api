package uk.gov.dluhc.emsintegrationapi.client

import uk.gov.dluhc.emsintegrationapi.exception.ResourceNotFoundException

/**
 * Exception classes used when calling `ier-api` is unsuccessful.
 * Allows for communicating the error condition/state back to consuming code within this module without exposing the
 * underlying exception and technology.
 * This abstracts the consuming code from having to deal with, for example, a RestException
 */
abstract class IerApiException(private val errorMessage: String) : ResourceNotFoundException(errorMessage)

class IerEroNotFoundException(certificateSerial: String) :
    IerApiException("EROCertificateMapping for certificateSerial=[$certificateSerial] not found")

class IerGeneralException(message: String) :
    IerApiException(message)
