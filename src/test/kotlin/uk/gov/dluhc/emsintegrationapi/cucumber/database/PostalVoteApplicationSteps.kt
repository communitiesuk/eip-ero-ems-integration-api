package uk.gov.dluhc.emsintegrationapi.cucumber.database

import io.cucumber.java8.En
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicantDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplication
import java.time.Instant
import java.time.Period

open class PostalVoteApplicationSteps(
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository,
) : En {
    init {
        Given(
            "a postal vote application with gss code {string} was saved to EMS integration API {int} days ago and has record status {string}",
            ::createPostalApplicationWithEmsElectorId
        )

        Given(
            "a postal vote application with gss code {string} with an EMS Elector ID was saved to EMS integration API {int} days ago and has record status {string}",
            ::createPostalApplicationWithEmsElectorId
        )

        Given("a postal vote application with gss code {string} without an EMS Elector ID was saved to EMS integration API {int} days ago and has record status {string}") { gssCode: String, numberOfDays: Int, recordStatus: String ->
            val postalVoteApplication = buildPostalVoteApplication(
                recordStatus = RecordStatus.valueOf(recordStatus),
                applicationDetails = buildApplicationDetailsEntity(gssCode = gssCode),
                applicantDetails = buildApplicantDetailsEntity(emsElectorId = null)
            )
            setDateCreatedToDaysAgo(postalVoteApplication, numberOfDays)
        }
    }

    private fun createPostalApplicationWithEmsElectorId(gssCode: String, numberOfDays: Int, recordStatus: String) {
        val postalVoteApplication = buildPostalVoteApplication(
            recordStatus = RecordStatus.valueOf(recordStatus),
            applicationDetails = buildApplicationDetailsEntity(gssCode = gssCode)
        )
        setDateCreatedToDaysAgo(postalVoteApplication, numberOfDays)
    }

    private fun setDateCreatedToDaysAgo(application: PostalVoteApplication, daysAgo: Int) {
        // Application needs to be saved first to avoid the date created being overwritten
        postalVoteApplicationRepository.save(application)
        application.dateCreated = Instant.now().minus(Period.ofDays(daysAgo))
        postalVoteApplicationRepository.save(application)
    }
}
