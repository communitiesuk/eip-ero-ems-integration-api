package uk.gov.dluhc.emsintegrationapi.rest

import mu.KotlinLogging
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.dluhc.emsintegrationapi.models.PostalVoteAcceptedResponse
import uk.gov.dluhc.emsintegrationapi.rest.PostalVoteApplicationController.Companion.ROOT_PATH
import uk.gov.dluhc.emsintegrationapi.service.PostalVoteApplicationService
import javax.validation.constraints.Max
import javax.validation.constraints.Min

private val logger = KotlinLogging.logger { }

@RestController
@CrossOrigin
@RequestMapping(ROOT_PATH)
class PostalVoteApplicationController(private val postalVoteApplicationService: PostalVoteApplicationService) {
    companion object {
        const val ROOT_PATH = "/postalVotes"
        const val ACCEPTED = "/accepted"
        const val PAGE_SIZE_PARAM = "pageSize"
        const val API_CONFIG_PREFIX = "dluhc"
        const val DEFAULT_PAGE_SIZE = "$API_CONFIG_PREFIX.default-page-size"
    }

    @GetMapping(ACCEPTED)
    fun getGetPostalVoteApplications(
        @Min(1)
        @Max(500)
        @RequestParam(
            name = PAGE_SIZE_PARAM,
            required = false
        ) pageSize: Int?
    ): PostalVoteAcceptedResponse {
        return postalVoteApplicationService.getPostalVoteApplications(pageSize)
    }
}
