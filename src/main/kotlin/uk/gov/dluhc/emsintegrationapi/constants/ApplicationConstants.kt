package uk.gov.dluhc.emsintegrationapi.constants

class ApplicationConstants {
    companion object {
        const val PAGE_SIZE_PARAM = "pageSize"
        const val APPLICATION_ID = "id"
        const val APPLICATION_ID_REGEX = "^[a-fA-F\\d]{24}$"
        const val IS_AUTHENTICATED = "isAuthenticated()"
        const val APPLICATION_ID_ERROR_MESSAGE =
            "The application id must match the pattern $APPLICATION_ID_REGEX"
        const val EMS_MESSAGE_TEXT = "Some thing went wrong"
        const val EMS_DETAILS_TEXT = "Pls look into issue"
    }
}
