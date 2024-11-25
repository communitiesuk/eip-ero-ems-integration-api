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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.dluhc.emsintegrationapi.constants.ApplicationConstants
import uk.gov.dluhc.emsintegrationapi.constants.ApplicationConstants.Companion.PAGE_SIZE_PARAM
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationResponse
import uk.gov.dluhc.emsintegrationapi.models.ProxyVoteApplications
import uk.gov.dluhc.emsintegrationapi.service.ProxyVoteApplicationService
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern

private val logger = KotlinLogging.logger { }

@RestController
@CrossOrigin
@Validated
@RequestMapping("/proxyvotes")
class ProxyVoteApplicationController(private val proxyVoteApplicationService: ProxyVoteApplicationService) {

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun getProxyVoteApplications(
        authentication: Authentication,
        @RequestParam(
            name = PAGE_SIZE_PARAM,
            required = false
        )
        @ValidPageSize
        pageSize: Int?
    ): ProxyVoteApplications {
        val serialNumber = authentication.credentials.toString()
        logger.info { "Processing a get proxy vote applications request with page size =$pageSize and certificate serial no =$serialNumber" }
        return proxyVoteApplicationService.getProxyVoteApplications(serialNumber, pageSize)
    }

    @Deprecated(message = "Replaced with put")
    @DeleteMapping("/{id}")
    @PreAuthorize(ApplicationConstants.IS_AUTHENTICATED)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun emsAcceptedByDelete(
        authentication: Authentication,
        @PathVariable(name = ApplicationConstants.APPLICATION_ID)
        @Pattern(
            regexp = ApplicationConstants.APPLICATION_ID_REGEX,
            message = ApplicationConstants.APPLICATION_ID_ERROR_MESSAGE
        )
        applicationId: String
    ) {
        logger.info { "DELETE: Processing EMS confirmation of a proxy vote application" }
        val serialNumber = authentication.credentials.toString()
        logger.info { "Processing EMS confirmation of a proxy vote application with the id $applicationId and certificate serial no=$serialNumber" }
        proxyVoteApplicationService.confirmReceipt(serialNumber, applicationId, EMSApplicationResponse())
    }

    @Deprecated(message = "Replaced with put")
    @PostMapping("/{id}")
    @PreAuthorize(ApplicationConstants.IS_AUTHENTICATED)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun emsAcceptedByPost(
        authentication: Authentication,
        @PathVariable(name = ApplicationConstants.APPLICATION_ID)
        @Pattern(regexp = ApplicationConstants.APPLICATION_ID_REGEX, message = ApplicationConstants.APPLICATION_ID_ERROR_MESSAGE)
        applicationId: String,
        @Valid @RequestBody request: EMSApplicationResponse = EMSApplicationResponse()
    ) {
        val serialNumber = authentication.credentials.toString()
        logger.info { "Processing EMS confirmation of a proxy vote application with id $applicationId, certificate serial no=$serialNumber and EMS status ${request.status}" }
        proxyVoteApplicationService.confirmReceipt(serialNumber, applicationId, request)
    }

    @PutMapping("/{id}")
    @PreAuthorize(ApplicationConstants.IS_AUTHENTICATED)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun emsAccepted(
        authentication: Authentication,
        @PathVariable(name = ApplicationConstants.APPLICATION_ID)
        @Pattern(regexp = ApplicationConstants.APPLICATION_ID_REGEX, message = ApplicationConstants.APPLICATION_ID_ERROR_MESSAGE)
        applicationId: String,
        @Valid @RequestBody request: EMSApplicationResponse = EMSApplicationResponse()
    ) {
        val serialNumber = authentication.credentials.toString()
        logger.info { "Processing EMS confirmation of a proxy vote application with id $applicationId, certificate serial no=$serialNumber and EMS status ${request.status}" }
        proxyVoteApplicationService.confirmReceipt(serialNumber, applicationId, request)
    }
}
