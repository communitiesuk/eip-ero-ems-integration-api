package uk.gov.dluhc.emsintegrationapi.cucumber.rest

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository

class EmsProxyApplicationConfirmationSteps(
    private val proxyVoteApplicationRepository: ProxyVoteApplicationRepository,
) : En {

    init {
        Then("the system updated the proxy application with the id {string} status as {string}") { applicationId: String, status: String ->
            val proxyVoteApplication = proxyVoteApplicationRepository.findById(applicationId).get()
            assertThat(proxyVoteApplication.status).isEqualTo(RecordStatus.valueOf(status))
            assertThat(proxyVoteApplication.updatedBy).isEqualTo(SourceSystem.EMS)
        }
        And("the system ignores request and did not update the proxy application with the id {string}") { applicationId: String ->
            val applicationFromDB = proxyVoteApplicationRepository.findById(applicationId).get()
            assertThat(applicationFromDB.status).isEqualTo(RecordStatus.DELETED)
        }
    }
}
