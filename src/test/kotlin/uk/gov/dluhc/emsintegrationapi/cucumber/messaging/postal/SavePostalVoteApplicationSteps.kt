package uk.gov.dluhc.emsintegrationapi.cucumber.messaging.postal

import io.cucumber.java8.En
import mu.KotlinLogging
import org.awaitility.kotlin.await
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.Constants.Companion.POSTAL_VOTE_APPLICATION_FIELDS_TO_IGNORE
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicantDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplicationMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.validateObjects

private val logger = KotlinLogging.logger { }

class SavePostalVoteApplicationSteps(
    private val messageSender: MessageSender<PostalVoteApplicationMessage>,
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository
) : En {
    private var postalVoteApplicationMessage: PostalVoteApplicationMessage? = null

    init {
        Given(
            "a postal vote application with the application id {string} and electoral id {string}"
        ) { applicationId: String, emsElectorId: String ->
            logger.info("Postal application id $applicationId and Elector id = $emsElectorId")
            postalVoteApplicationMessage =
                buildPostalVoteApplicationMessageDto(
                    applicationId = applicationId,
                    applicantDetails = buildApplicantDetailsMessageDto(emsElectorId = emsElectorId)
                )
        }
        When("I send an sqs message to the postal application queue") {
            logger.info("Send to the queue")
            messageSender.send(postalVoteApplicationMessage!!, QueueConfiguration.QueueName.POSTAL_APPLICATION_QUEUE)
        }

        And("the postal vote application has been successfully saved with the application id {string} and electoral id {string}") { applicationId: String, emsElectorId: String ->
            logger.info("The saved Postal application has id $applicationId and Elector id = $emsElectorId")

            await.until { postalVoteApplicationRepository.findById(applicationId).isPresent }

            with(postalVoteApplicationRepository.findById(applicationId).get()) {
                validateObjects(
                    postalVoteApplicationMessage,
                    this,
                    *POSTAL_VOTE_APPLICATION_FIELDS_TO_IGNORE
                )
                logger.info("Successfully validated the application with id = $applicationId")
            }
        }
        Given("a postal vote application with the application id {string} and electoral id {string} exists")
        { applicationId: String, emsElectorId: String ->
            logger.info("Postal application id $applicationId and Elector id = $emsElectorId")
            postalVoteApplicationMessage = buildPostalVoteApplicationWith(applicationId, emsElectorId)
        }
    }

    private fun buildPostalVoteApplicationWith(applicationId: String, emsElectorId: String) =
        buildPostalVoteApplicationMessageDto(
            applicationId = applicationId,
            applicantDetails = buildApplicantDetailsMessageDto(emsElectorId = emsElectorId)
        )
}
