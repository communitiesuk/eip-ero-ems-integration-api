package uk.gov.dluhc.emsintegrationapi.cucumber.messaging.proxy

import io.cucumber.java8.En
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.Companion.confirmTheEntityDoesExist
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.Constants.Companion.APPLICATION_FIELDS_TO_IGNORE
import uk.gov.dluhc.emsintegrationapi.mapper.ProxyVoteApplicationMessageMapper
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.ProxyVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicantDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplicationMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.validateObjects
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger { }

open class SaveProxyVoteApplicationSteps(
    private val messageSender: MessageSender<ProxyVoteApplicationMessage>,
    private val proxyVoteApplicationRepository: ProxyVoteApplicationRepository,
    private val proxyVoteApplicationMessageMapper: ProxyVoteApplicationMessageMapper

) : En {
    private var proxyVoteApplicationMessage: ProxyVoteApplicationMessage? = null

    init {
        Given("a proxy vote application with the application id {string} and electoral id {string}") { applicationId: String, emsElectorId: String ->
            logger.info("Proxy application id $applicationId and Elector id = $emsElectorId")
            proxyVoteApplicationMessage =
                buildProxyVoteApplicationMessageDto(
                    applicationId = applicationId,
                    applicantDetails = buildApplicantDetailsMessageDto(emsElectorId = emsElectorId)
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

        Then("the proxy vote application has been successfully saved with the application id {string} and electoral id {string}") { applicationId: String, emsElectorId: String ->
            logger.info("The saved Proxy application has id $applicationId and Elector id = $emsElectorId")

            await.atMost(5, TimeUnit.SECONDS).untilAsserted {
                val savedEntity = proxyVoteApplicationRepository.findById(applicationId)
                if (savedEntity.isPresent) {
                    validateObjects(
                        proxyVoteApplicationMessage,
                        this,
                        *APPLICATION_FIELDS_TO_IGNORE
                    )
                    logger.info("Successfully validated the application with id = $applicationId")
                }
            }
        }
        Given("a proxy vote application with the application id {string} and electoral id {string} exists") { applicationId: String, emsElectorId: String ->
            logger.info("Proxy application id $applicationId and Elector id = $emsElectorId")
            proxyVoteApplicationMessage = buildProxyVoteApplicationWith(applicationId, emsElectorId)
            createProxyVoteApplication(proxyVoteApplicationMessage!!)
        }
        Then("the proxy vote application with id {string} and electoral id {string} did not save") { applicationId: String, emsElectorId: String ->
            await.during(5, TimeUnit.SECONDS).atMost(6, TimeUnit.SECONDS).untilAsserted {
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
    }

    private fun buildProxyVoteApplicationWith(applicationId: String, emsElectorId: String) =
        buildProxyVoteApplicationMessageDto(
            applicationId = applicationId,
            applicantDetails = buildApplicantDetailsMessageDto(emsElectorId = emsElectorId)
        )

    private fun sendMessage(proxyVoteApplicationMessage: ProxyVoteApplicationMessage) {
        with(proxyVoteApplicationMessage) {
            logger.info("Send proxy application with id = ${approvalDetails.id} and electoral id = ${applicantDetails.emsElectorId} the queue")
            messageSender.send(proxyVoteApplicationMessage, QueueConfiguration.QueueName.PROXY_APPLICATION_QUEUE)
        }
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
        confirmTheEntityDoesExist(proxyVoteApplicationRepository, applicationId)
}
