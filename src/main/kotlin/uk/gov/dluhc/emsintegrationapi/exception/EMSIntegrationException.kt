package uk.gov.dluhc.emsintegrationapi.exception

abstract class EMSIntegrationException(private val errorMessage: String) : RuntimeException() {
    override val message: String
        get() = errorMessage
}
