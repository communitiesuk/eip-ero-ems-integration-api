package uk.gov.dluhc.emsintegrationapi.service

class ApplicationNotFoundException(val applicationId: String, val applicationType: ApplicationType) :
    RuntimeException() {
    override val message: String
        get() = "The ${applicationType.displayName} application could not be found with id `$applicationId`"
}
