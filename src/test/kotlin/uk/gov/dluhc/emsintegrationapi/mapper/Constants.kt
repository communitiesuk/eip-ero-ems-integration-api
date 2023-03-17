package uk.gov.dluhc.emsintegrationapi.mapper

class Constants {
    companion object {
        val APPLICATION_FIELDS_TO_IGNORE = arrayOf(
            "approvalDetails.authorisedAt", "approvalDetails.createdAt"
        )
    }
}
