package uk.gov.dluhc.emsintegrationapi.dto

data class AddressDto(
    val street: String,
    val property: String?,
    val locality: String?,
    val town: String?,
    val area: String?,
    val postcode: String,
    val uprn: String?,
    val createdBy: String?,
)
