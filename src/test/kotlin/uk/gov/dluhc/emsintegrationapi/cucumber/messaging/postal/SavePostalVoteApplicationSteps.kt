package uk.gov.dluhc.emsintegrationapi.cucumber.messaging.postal

import io.cucumber.java8.En
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.Companion.confirmTheEntityDoesNotExist
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.Constants.Companion.APPLICATION_FIELDS_TO_IGNORE
import uk.gov.dluhc.emsintegrationapi.mapper.PostalVoteApplicationMessageMapper
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicantDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplicationMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.validateObjects
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger { }

open class SavePostalVoteApplicationSteps(
    private val messageSender: MessageSender<PostalVoteApplicationMessage>,
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository,
    private val postalVoteApplicationMessageMapper: PostalVoteApplicationMessageMapper

) : En {
    private var postalVoteApplicationMessage: PostalVoteApplicationMessage? = null

    init {
        Given("a postal vote application with the application id {string} and electoral id {string}") { applicationId: String, emsElectorId: String ->
            logger.info("Postal application id $applicationId and Elector id = $emsElectorId")
            postalVoteApplicationMessage =
                buildPostalVoteApplicationMessageDto(
                    applicationId = applicationId,
                    applicantDetails = buildApplicantDetailsMessageDto(emsElectorId = emsElectorId)
                )
        }
        When("I send an sqs message to the postal application queue") { sendMessage(postalVoteApplicationMessage!!) }

        When("I send an sqs message to the postal application queue with an application id {string} and electoral id {string}") { applicationId: String, emsElectorId: String ->
            sendMessage(
                buildPostalVoteApplicationWith(
                    applicationId,
                    emsElectorId
                )
            )
        }

        Then("the postal vote application has been successfully saved with the application id {string} and electoral id {string}") { applicationId: String, emsElectorId: String ->
            logger.info("The saved Postal application has id $applicationId and Elector id = $emsElectorId")

            await.atMost(5, TimeUnit.SECONDS).untilAsserted {
                val savedEntity = postalVoteApplicationRepository.findById(applicationId)
                if (savedEntity.isPresent) {
                    validateObjects(
                        postalVoteApplicationMessage,
                        this,
                        *APPLICATION_FIELDS_TO_IGNORE
                    )
                    logger.info("Successfully validated the application with id = $applicationId")
                }
            }
        }
        Given("a postal vote application with the application id {string} and electoral id {string} exists") { applicationId: String, emsElectorId: String ->
            logger.info("Postal application id $applicationId and Elector id = $emsElectorId")
            postalVoteApplicationMessage = buildPostalVoteApplicationWith(applicationId, emsElectorId)
            createPostalVoteApplication(postalVoteApplicationMessage!!)
        }
        Then("the postal vote application with id {string} and electoral id {string} did not save") { applicationId: String, emsElectorId: String ->
            await.during(5, TimeUnit.SECONDS).atMost(6, TimeUnit.SECONDS).untilAsserted {
                val postalVoteEntity = postalVoteApplicationRepository.findById(applicationId)
                // Double check the ems electoral id and ensure that they are different to prove the second application did not save
                assertThat(postalVoteEntity.get().applicantDetails.emsElectorId).isNotEqualTo(emsElectorId)
            }
        }
        Then("the postal vote application with id {string} did not save") { applicationId: String ->
            confirmTheApplicationDidNotSave(
                applicationId
            )
        }
    }

    private fun buildPostalVoteApplicationWith(applicationId: String, emsElectorId: String) =
        buildPostalVoteApplicationMessageDto(
            applicationId = applicationId,
            applicantDetails = buildApplicantDetailsMessageDto(emsElectorId = emsElectorId)
        )

    private fun sendMessage(postalVoteApplicationMessage: PostalVoteApplicationMessage) {
        with(postalVoteApplicationMessage) {
            logger.info("Send postal application with id = ${approvalDetails.id} and electoral id = ${applicantDetails.emsElectorId} the queue")
            messageSender.send(postalVoteApplicationMessage, QueueConfiguration.QueueName.POSTAL_APPLICATION_QUEUE)
        }
    }

    @Transactional
    open fun createPostalVoteApplication(postalVoteApplicationMessage: PostalVoteApplicationMessage) {
        postalVoteApplicationRepository.saveAndFlush(
            postalVoteApplicationMessageMapper.mapToEntity(
                postalVoteApplicationMessage
            )
        )
    }

    @Transactional
    open fun confirmTheApplicationDidNotSave(applicationId: String) =
        confirmTheEntityDoesNotExist(postalVoteApplicationRepository, applicationId)
}
