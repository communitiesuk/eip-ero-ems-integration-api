package uk.gov.dluhc.emsintegrationapi.client

import uk.gov.dluhc.emsintegrationapi.exception.EMSIntegrationException

/**
 * Exception classes used when calling `ier-api` is unsuccessful.
 * Allows for communicating the error condition/state back to consuming code within this module without exposing the
 * underlying exception and technology.
 * This abstracts the consuming code from having to deal with, for example, a RestException
 */
abstract class IerApiException(private val errorMessage: String) : EMSIntegrationException(errorMessage)

class IerEroNotFoundException(certificateSerial: String) :
    IerApiException("The EROCertificateMapping for certificateSerial=[$certificateSerial] could not be found")

class IerGeneralException(message: String) :
    IerApiException(message)
