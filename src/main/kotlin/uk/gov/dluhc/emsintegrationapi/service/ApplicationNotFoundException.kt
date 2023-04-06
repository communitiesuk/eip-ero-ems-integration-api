package uk.gov.dluhc.emsintegrationapi.service

class ApplicationNotFoundException(applicationId: String, applicationType: ApplicationType) :
    RuntimeException("The ${applicationType.displayName} application could not be found with id `$applicationId`")
