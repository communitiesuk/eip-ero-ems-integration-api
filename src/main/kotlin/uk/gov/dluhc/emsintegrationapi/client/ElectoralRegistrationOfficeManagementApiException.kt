package uk.gov.dluhc.emsintegrationapi.client

import uk.gov.dluhc.emsintegrationapi.exception.EMSIntegrationException

/**
 * Exception classes used when calling `ero-management-api` is not successful.
 * Allows for communicating the error condition/state back to consuming code within this module without exposing the
 * underlying exception and technology.
 * This abstracts the consuming code from having to deal with, for example, a WebClientResponseException
 */

abstract class ElectoralRegistrationOfficeManagementApiException(message: String) : EMSIntegrationException(message)

class ElectoralRegistrationOfficeNotFoundException(eroId: String) :
    ElectoralRegistrationOfficeManagementApiException("The ERO $eroId could not be found")

class ElectoralRegistrationOfficeGeneralException(message: String) :
    ElectoralRegistrationOfficeManagementApiException(message)
