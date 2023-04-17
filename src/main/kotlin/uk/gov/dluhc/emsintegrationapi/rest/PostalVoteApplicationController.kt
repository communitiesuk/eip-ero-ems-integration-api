package uk.gov.dluhc.emsintegrationapi.rest

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.dluhc.emsintegrationapi.constants.ApplicationConstants.Companion.APPLICATION_ID
import uk.gov.dluhc.emsintegrationapi.constants.ApplicationConstants.Companion.APPLICATION_ID_ERROR_MESSAGE
import uk.gov.dluhc.emsintegrationapi.constants.ApplicationConstants.Companion.APPLICATION_ID_REGEX
import uk.gov.dluhc.emsintegrationapi.constants.ApplicationConstants.Companion.IS_AUTHENTICATED
import uk.gov.dluhc.emsintegrationapi.constants.ApplicationConstants.Companion.PAGE_SIZE_PARAM
import uk.gov.dluhc.emsintegrationapi.models.PostalVoteAcceptedResponse
import uk.gov.dluhc.emsintegrationapi.service.PostalVoteApplicationService
import javax.validation.constraints.Pattern

private val logger = KotlinLogging.logger { }

@RestController
@CrossOrigin
@Validated
@RequestMapping("/postalVotes")
class PostalVoteApplicationController(private val postalVoteApplicationService: PostalVoteApplicationService) {

    @GetMapping("/accepted")
    @PreAuthorize(IS_AUTHENTICATED)
    fun getPostalVoteApplications(
        authentication: Authentication,
        @RequestParam(
            name = PAGE_SIZE_PARAM,
            required = false
        )
        @ValidPageSize
        pageSize: Int?
    ): PostalVoteAcceptedResponse {
        val serialNumber = authentication.credentials.toString()
        logger.info { "Processing a get postal vote applications request with page size =$pageSize and certificate serial no =$serialNumber" }
        return postalVoteApplicationService.getPostalVoteApplications(serialNumber, pageSize)
    }

    @DeleteMapping("/accepted/{id}")
    @PreAuthorize(IS_AUTHENTICATED)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun emsAccepted(
        authentication: Authentication,
        @PathVariable(name = APPLICATION_ID)
        @Pattern(regexp = APPLICATION_ID_REGEX, message = APPLICATION_ID_ERROR_MESSAGE)
        applicationId: String
    ) {
        val serialNumber = authentication.credentials.toString()
        logger.info { "Processing EMS confirmation of a postal vote application with id $applicationId and certificate serial no=$serialNumber" }
        postalVoteApplicationService.confirmReceipt(serialNumber, applicationId)
    }
}
