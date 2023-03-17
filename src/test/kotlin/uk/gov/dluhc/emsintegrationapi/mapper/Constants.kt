package uk.gov.dluhc.emsintegrationapi.mapper

class Constants {
    companion object {
        val POSTAL_VOTE_APPLICATION_FIELDS_TO_IGNORE = arrayOf(
            "applicantDetails.registeredAddress.createdBy",
            "postalVoteDetails.ballotAddress.createdBy",
            "approvalDetails.authorisedAt", "approvalDetails.createdAt", "approvalDetails.id"
        )

        val PROXY_VOTE_APPLICATION_FIELDS_TO_IGNORE = arrayOf(
            "applicantDetails.registeredAddress.createdBy",
            "proxyVoteDetails.ballotAddress.createdBy",
            "approvalDetails.authorisedAt", "approvalDetails.createdAt", "approvalDetails.id"
        )
    }
}
