package uk.gov.dluhc.emsintegrationapi.cucumber.database

import io.cucumber.java8.En
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplication
import java.time.Instant
import java.time.Period

open class ProxyVoteApplicationSteps(
    private val proxyVoteApplicationRepository: ProxyVoteApplicationRepository,
) : En {
    init {
        Given("a proxy vote application with gss code {string} was saved to EMS integration API {int} days ago and has record status {string}") { gssCode: String, numberOfDays: Int, recordStatus: String ->
            val proxyVoteApplication = buildProxyVoteApplication(
                recordStatus = RecordStatus.valueOf(recordStatus),
                applicationDetails = buildApplicationDetailsEntity(gssCode = gssCode)
            )
            proxyVoteApplicationRepository.save(proxyVoteApplication)
            proxyVoteApplication.dateCreated = Instant.now().minus(Period.ofDays(numberOfDays))
            proxyVoteApplicationRepository.save(proxyVoteApplication)
        }
    }
}
