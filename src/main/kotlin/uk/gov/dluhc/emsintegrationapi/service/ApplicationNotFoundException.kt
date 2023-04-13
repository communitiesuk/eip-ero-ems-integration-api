package uk.gov.dluhc.emsintegrationapi.service

import uk.gov.dluhc.emsintegrationapi.exception.ResourceNotFoundException

class ApplicationNotFoundException(val applicationId: String, val applicationType: ApplicationType) :
    ResourceNotFoundException("The ${applicationType.displayName} application could not be found with id `$applicationId`")

