package uk.gov.dluhc.emsintegrationapi.rest

import mu.KotlinLogging
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.dluhc.emsintegrationapi.constants.ApplicationConstants.Companion.PAGE_SIZE_PARAM
import uk.gov.dluhc.emsintegrationapi.models.ProxyVoteAcceptedResponse
import uk.gov.dluhc.emsintegrationapi.service.ProxyVoteApplicationService

private val logger = KotlinLogging.logger { }

@RestController
@CrossOrigin
@Validated
@RequestMapping("/proxyVotes")
class ProxyVoteApplicationController(private val proxyVoteApplicationService: ProxyVoteApplicationService) {

    @GetMapping("/accepted")
    @PreAuthorize("isAuthenticated()")
    fun getProxyVoteApplications(
        authentication: Authentication,
        @RequestParam(
            name = PAGE_SIZE_PARAM,
            required = false
        )
        @ValidPageSize
        pageSize: Int?
    ): ProxyVoteAcceptedResponse {
        val serialNumber = authentication.credentials.toString()
        logger.info { "Processing a get proxy vote applications request with page size =$pageSize and certificate serial no =$serialNumber" }
        return proxyVoteApplicationService.getProxyVoteApplications(serialNumber, pageSize)
    }
}
