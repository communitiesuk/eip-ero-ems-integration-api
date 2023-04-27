package uk.gov.dluhc.emsintegrationapi.cucumber.messaging.proxy

import io.cucumber.java8.En
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.Companion.confirmTheEntityDoesNotExist
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.Constants.Companion.APPLICATION_FIELDS_TO_IGNORE
import uk.gov.dluhc.emsintegrationapi.mapper.ProxyVoteApplicationMessageMapper
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApplicationDetails
import uk.gov.dluhc.emsintegrationapi.messaging.models.ProxyVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.SIGNATURE_BASE64_STRING
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicantDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplicationMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.validateObjects
import java.util.concurrent.TimeUnit
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicationDetails as ApplicationDetailsEntity

private val logger = KotlinLogging.logger { }

open class SaveProxyVoteApplicationSteps(
    private val messageSender: MessageSender<ProxyVoteApplicationMessage>,
    private val proxyVoteApplicationRepository: ProxyVoteApplicationRepository,
    private val proxyVoteApplicationMessageMapper: ProxyVoteApplicationMessageMapper

) : En {
    private var proxyVoteApplicationMessage: ProxyVoteApplicationMessage? = null

    init {
        Given("a proxy vote application with the application id {string}, electoral id {string} and status {string}") { applicationId: String, emsElectorId: String, applicationStatus: String ->
            logger.info("Proxy application id $applicationId and Elector id = $emsElectorId")
            proxyVoteApplicationMessage =
                buildProxyVoteApplicationsWithSignature(applicationStatus, applicationId, emsElectorId)
        }
        Given("a proxy vote application with the application id {string} and signature waiver reason {string}") { applicationId: String, waiverReason: String ->
            proxyVoteApplicationMessage = buildProxyVoteApplicationMessageDto(
                applicationDetails = buildApplicationDetailsMessageDto(
                    applicationId = applicationId,
                    signatureWaived = true,
                    signatureWaivedReason = waiverReason
                )
            )
        }
        When("I send an sqs message to the proxy application queue") { sendMessage(proxyVoteApplicationMessage!!) }

        When("I send an sqs message to the proxy application queue with an application id {string} and electoral id {string}") { applicationId: String, emsElectorId: String ->
            sendMessage(
                buildProxyVoteApplicationWith(
                    applicationId,
                    emsElectorId
                )
            )
        }

        Then("the {string} proxy vote application has been successfully saved with the application id {string} and signature") { applicationStatus: String, applicationId: String ->
            await.atMost(5, TimeUnit.SECONDS).untilAsserted {
                val optSavedEntity = proxyVoteApplicationRepository.findById(applicationId)
                if (optSavedEntity.isPresent) {
                    validateSavedEntity(optSavedEntity.get(), applicationStatus)
                    logger.info("Successfully validated the proxy application with the id = $applicationId")
                }
            }
        }
        Given("a proxy vote application with the application id {string} and electoral id {string} exists") { applicationId: String, emsElectorId: String ->
            logger.info("Proxy application id $applicationId and Elector id = $emsElectorId")
            proxyVoteApplicationMessage = buildProxyVoteApplicationWith(applicationId, emsElectorId)
            createProxyVoteApplication(proxyVoteApplicationMessage!!)
        }
        Then("the proxy vote application with id {string} and electoral id {string} did not save") { applicationId: String, emsElectorId: String ->
            await.pollDelay(2, TimeUnit.SECONDS).atMost(5, TimeUnit.SECONDS).untilAsserted {
                val proxyVoteEntity = proxyVoteApplicationRepository.findById(applicationId)
                // Double check the ems electoral id and ensure that they are different to prove the second application did not save
                assertThat(proxyVoteEntity.get().applicantDetails.emsElectorId).isNotEqualTo(emsElectorId)
            }
        }
        Then("the proxy vote application with id {string} did not save") { applicationId: String ->
            confirmTheApplicationDidNotSave(
                applicationId
            )
        }
        Then("the proxy vote application has been successfully saved with the signature waiver reason {string}") { waiverReason: String ->
            await.pollDelay(2, TimeUnit.SECONDS).atMost(5, TimeUnit.SECONDS).untilAsserted {
                val optSavedEntity =
                    proxyVoteApplicationRepository.findById(proxyVoteApplicationMessage!!.applicationDetails.id)
                if (optSavedEntity.isPresent) {
                    val savedEntity = optSavedEntity.get()
                    assertThat(savedEntity.applicationDetails.signatureWaived).isEqualTo(true)
                    assertThat(savedEntity.applicationDetails.signatureWaivedReason).isEqualTo(waiverReason)
                    assertThat(savedEntity.applicationDetails.signatureBase64).isNull()
                }
            }
        }
    }

    private fun buildProxyVoteApplicationWith(applicationId: String, emsElectorId: String) =
        buildProxyVoteApplicationMessageDto(
            applicationDetails = buildApplicationDetailsMessageDto(
                applicationId = applicationId
            ),
            applicantDetails = buildApplicantDetailsMessageDto(emsElectorId = emsElectorId)
        )

    private fun sendMessage(proxyVoteApplicationMessage: ProxyVoteApplicationMessage) {
        with(proxyVoteApplicationMessage) {
            logger.info("Send proxy application with id = ${applicationDetails.id} and electoral id = ${applicantDetails.emsElectorId} the queue")
            messageSender.send(proxyVoteApplicationMessage, QueueConfiguration.QueueName.PROXY_APPLICATION_QUEUE)
        }
    }

    private fun buildProxyVoteApplicationsWithSignature(
        applicationStatus: String,
        applicationId: String,
        emsElectorId: String
    ) =
        buildProxyVoteApplicationMessageDto(
            applicationDetails = buildApplicationDetailsMessageDto(
                applicationStatus = ApplicationDetails.ApplicationStatus.valueOf(
                    applicationStatus,
                ),
                applicationId = applicationId,
                signatureBase64 = SIGNATURE_BASE64_STRING
            ),
            applicantDetails = buildApplicantDetailsMessageDto(emsElectorId = emsElectorId)
        )

    private fun validateSavedEntity(
        proxyVoteApplication: ProxyVoteApplication,
        applicationStatus: String
    ) {
        validateObjects(
            proxyVoteApplicationMessage,
            proxyVoteApplication,
            *APPLICATION_FIELDS_TO_IGNORE
        )
        assertThat(proxyVoteApplication.applicationDetails.applicationStatus).isEqualTo(
            ApplicationDetailsEntity.ApplicationStatus.valueOf(
                applicationStatus
            )
        )
        assertThat(proxyVoteApplication.applicationDetails.signatureBase64).isEqualTo(SIGNATURE_BASE64_STRING)
    }

    @Transactional
    open fun createProxyVoteApplication(proxyVoteApplicationMessage: ProxyVoteApplicationMessage) {
        proxyVoteApplicationRepository.saveAndFlush(
            proxyVoteApplicationMessageMapper.mapToEntity(
                proxyVoteApplicationMessage
            )
        )
    }

    @Transactional
    open fun confirmTheApplicationDidNotSave(applicationId: String) =
        confirmTheEntityDoesNotExist(proxyVoteApplicationRepository, applicationId)
}
