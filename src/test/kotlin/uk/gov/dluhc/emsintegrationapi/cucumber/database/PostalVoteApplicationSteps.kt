package uk.gov.dluhc.emsintegrationapi.cucumber.database

import io.cucumber.java8.En
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplication
import java.time.Instant
import java.time.Period

open class PostalVoteApplicationSteps(
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository,
) : En {
    init {
        Given("a postal vote application with gss code {string} was saved to EMS integration API {int} days ago and has record status {string}") { gssCode: String, numberOfDays: Int, recordStatus: String ->
            val postalVoteApplication = buildPostalVoteApplication(
                recordStatus = RecordStatus.valueOf(recordStatus),
                applicationDetails = buildApplicationDetailsEntity(gssCode = gssCode)
            )
            postalVoteApplicationRepository.save(postalVoteApplication)
            postalVoteApplication.dateCreated = Instant.now().minus(Period.ofDays(numberOfDays))
            postalVoteApplicationRepository.save(postalVoteApplication)
        }
    }
}
