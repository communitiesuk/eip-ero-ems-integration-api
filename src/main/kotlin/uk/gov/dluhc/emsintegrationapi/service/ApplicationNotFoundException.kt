package uk.gov.dluhc.emsintegrationapi.service

import uk.gov.dluhc.emsintegrationapi.exception.EMSIntegrationException

class ApplicationNotFoundException(val applicationId: String, val applicationType: ApplicationType) :
    EMSIntegrationException("The ${applicationType.displayName} application could not be found with id `$applicationId`")
