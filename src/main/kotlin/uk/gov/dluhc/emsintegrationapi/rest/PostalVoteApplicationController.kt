package uk.gov.dluhc.emsintegrationapi.rest

import mu.KotlinLogging
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.dluhc.emsintegrationapi.constants.ApplicationConstants.Companion.PAGE_SIZE_PARAM
import uk.gov.dluhc.emsintegrationapi.models.PostalVoteAcceptedResponse
import uk.gov.dluhc.emsintegrationapi.rest.PostalVoteApplicationController.Companion.ROOT_PATH
import uk.gov.dluhc.emsintegrationapi.service.PostalVoteApplicationService

private val logger = KotlinLogging.logger { }

@RestController
@CrossOrigin
@RequestMapping(ROOT_PATH)
class PostalVoteApplicationController(private val postalVoteApplicationService: PostalVoteApplicationService) {
    companion object {
        const val ROOT_PATH = "/postalVotes"
        const val ACCEPTED = "/accepted"
    }

    @GetMapping(ACCEPTED)
    @PreAuthorize("isAuthenticated()")
    fun getGetPostalVoteApplications(
        authentication: Authentication,
        @RequestParam(
            name = PAGE_SIZE_PARAM,
            required = false
        )
        pageSize: Int?
    ): PostalVoteAcceptedResponse {
        val serialNumber = authentication.credentials.toString()
        logger.info { "Processing a get request with page size =$pageSize and certificate serial no =$serialNumber" }
        return postalVoteApplicationService.getPostalVoteApplications(serialNumber, pageSize)
    }
}
