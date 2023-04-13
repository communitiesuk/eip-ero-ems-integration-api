package uk.gov.dluhc.emsintegrationapi.exception

abstract class ResourceNotFoundException( private val errorMessage: String) : RuntimeException() {
    override val message: String
    get() = errorMessage
}