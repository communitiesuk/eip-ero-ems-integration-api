package uk.gov.dluhc.emsintegrationapi.cucumber.rest

import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApprovalDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.validateObjects

class EmsProxyApplicationConfirmationSteps(
    private val proxyVoteApplicationRepository: ProxyVoteApplicationRepository,
) : En {
    private var proxyVoteApplication: ProxyVoteApplication? = null

    init {
        Then("the system updated the proxy application with the id {string} status as {string}") { applicationId: String, status: String ->
            proxyVoteApplication = proxyVoteApplicationRepository.findById(applicationId).get()
            assertThat(proxyVoteApplication!!.status).isEqualTo(RecordStatus.valueOf(status))
            assertThat(proxyVoteApplication!!.updatedBy).isEqualTo(SourceSystem.EMS)
        }
        And("the system ignores request and did not update the proxy application with the id {string}") { applicationId: String ->
            val applicationFromDB = proxyVoteApplicationRepository.findById(applicationId).get()
            validateObjects(proxyVoteApplication, applicationFromDB)
        }
        Given("a proxy vote application with the application id {string}, status {string} and GSS Code {string} exist") { applicationId: String, status: String, gssCode: String ->
            proxyVoteApplicationRepository.saveAndFlush(
                buildProxyVoteApplication(
                    applicationId = applicationId,
                    recordStatus = RecordStatus.valueOf(status),
                    approvalDetails = buildApprovalDetailsEntity(gssCode = gssCode)
                )
            )
        }
    }
}
