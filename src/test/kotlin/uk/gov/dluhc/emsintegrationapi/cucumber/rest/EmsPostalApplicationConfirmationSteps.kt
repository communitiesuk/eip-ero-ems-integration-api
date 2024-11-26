package uk.gov.dluhc.emsintegrationapi.cucumber.rest

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository

class EmsPostalApplicationConfirmationSteps(
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository,
) : En {

    init {
        Then("the system updated the postal application with the id {string} status as {string}") { applicationId: String, status: String ->
            val postalVoteApplication = postalVoteApplicationRepository.findById(applicationId).get()
            assertThat(postalVoteApplication.status).isEqualTo(RecordStatus.valueOf(status))
            assertThat(postalVoteApplication.updatedBy).isEqualTo(SourceSystem.EMS)
        }
        Then("the system ignores request and did not update the postal application with the id {string}") { applicationId: String ->
            val applicationFromDB = postalVoteApplicationRepository.findById(applicationId).get()
            assertThat(applicationFromDB.status).isEqualTo(RecordStatus.DELETED)
        }
    }
}
