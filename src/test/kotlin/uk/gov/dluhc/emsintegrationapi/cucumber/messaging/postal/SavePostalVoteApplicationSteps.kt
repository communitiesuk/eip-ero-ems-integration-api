package uk.gov.dluhc.emsintegrationapi.cucumber.messaging.postal

import io.cucumber.java8.En
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.Companion.confirmTheEntityDoesNotExist
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.Constants.Companion.APPLICATION_FIELDS_TO_IGNORE
import uk.gov.dluhc.emsintegrationapi.mapper.PostalVoteApplicationMessageMapper
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApplicationDetails
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.SIGNATURE_BASE64_STRING
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicantDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.validateObjects
import java.util.concurrent.TimeUnit
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicationDetails as ApplicationDetailsEntity

private val logger = KotlinLogging.logger { }

open class SavePostalVoteApplicationSteps(
    private val messageSender: MessageSender<PostalVoteApplicationMessage>,
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository,
    private val postalVoteApplicationMessageMapper: PostalVoteApplicationMessageMapper

) : En {
    private var postalVoteApplicationMessage: PostalVoteApplicationMessage? = null

    init {
        Given("a postal vote application with the application id {string}, electoral id {string} and status {string}") { applicationId: String, emsElectorId: String, applicationStatus: String ->
            postalVoteApplicationMessage =
                buildPostalVoteApplicationsWithSignature(applicationStatus, applicationId, emsElectorId)
        }
        Given("a postal vote application with the application id {string} and signature waiver reason {string}") { applicationId: String, waiverReason: String ->
            postalVoteApplicationMessage = buildPostalVoteApplicationMessage(
                applicationDetails = buildApplicationDetailsMessageDto(
                    applicationId = applicationId,
                    signatureWaived = true,
                    signatureWaivedReason = waiverReason
                )
            )
        }
        Given("a postal vote application with the application id {string} and no ballot addresses") { applicationId: String ->
            postalVoteApplicationMessage = buildPostalVoteApplicationMessage(
                applicationDetails = buildApplicationDetailsMessageDto(applicationId = applicationId),
                postalVoteDetails = buildPostalVoteDetailsMessageDto(ballotAddress = null, ballotBfpoAddress = null, ballotOverseasAddress = null)
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

        Then("the {string} postal vote application has been successfully saved with the application id {string}, signature and ballot addresses") { applicationStatus: String, applicationId: String ->

            await.pollDelay(2, TimeUnit.SECONDS).atMost(5, TimeUnit.SECONDS).untilAsserted {
                val optSavedEntity = postalVoteApplicationRepository.findById(applicationId)
                if (optSavedEntity.isPresent) {
                    validateSavedEntity(optSavedEntity.get(), applicationStatus)
                    logger.info("Successfully validated the postal application with the id = $applicationId")
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
        Then("the postal vote application with id {string} was saved") { applicationId: String ->
            await.pollDelay(2, TimeUnit.SECONDS).atMost(5, TimeUnit.SECONDS).untilAsserted {
                assertThat(postalVoteApplicationRepository.findById(applicationId)).isPresent
            }
        }
        Then("the postal vote application has been successfully saved with the signature waiver reason {string}") { waiverReason: String ->
            await.pollDelay(2, TimeUnit.SECONDS).atMost(5, TimeUnit.SECONDS).untilAsserted {
                val optSavedEntity =
                    postalVoteApplicationRepository.findById(postalVoteApplicationMessage!!.applicationDetails.id)
                if (optSavedEntity.isPresent) {
                    val savedEntity = optSavedEntity.get()
                    assertThat(savedEntity.applicationDetails.signatureWaived).isEqualTo(true)
                    assertThat(savedEntity.applicationDetails.signatureWaivedReason).isEqualTo(waiverReason)
                    assertThat(savedEntity.applicationDetails.signatureBase64).isNull()
                }
            }
        }
        Then("the postal vote application has been successfully saved without ballot addresses") {
            await.pollDelay(2, TimeUnit.SECONDS).atMost(5, TimeUnit.SECONDS).untilAsserted {
                val optSavedEntity =
                    postalVoteApplicationRepository.findById(postalVoteApplicationMessage!!.applicationDetails.id)
                if (optSavedEntity.isPresent) {
                    val savedEntity = optSavedEntity.get()
                    assertThat(savedEntity.postalVoteDetails?.ballotAddress).isNull()
                    assertThat(savedEntity.postalVoteDetails?.ballotBfpoAddress).isNull()
                    assertThat(savedEntity.postalVoteDetails?.ballotOverseasAddress).isNull()
                }
            }
        }
    }

    private fun buildPostalVoteApplicationWith(applicationId: String, emsElectorId: String) =
        buildPostalVoteApplicationMessage(
            applicationDetails = buildApplicationDetailsMessageDto(
                applicationId = applicationId
            ),
            applicantDetails = buildApplicantDetailsMessageDto(emsElectorId = emsElectorId)
        )

    private fun sendMessage(postalVoteApplicationMessage: PostalVoteApplicationMessage) {
        with(postalVoteApplicationMessage) {
            logger.info("Send postal application with id = ${applicationDetails.id} and electoral id = ${applicantDetails.emsElectorId} the queue")
            messageSender.send(postalVoteApplicationMessage, QueueConfiguration.QueueName.POSTAL_APPLICATION_QUEUE)
        }
    }

    private fun buildPostalVoteApplicationsWithSignature(
        applicationStatus: String,
        applicationId: String,
        emsElectorId: String
    ) = buildPostalVoteApplicationMessage(
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
        postalVoteApplication: PostalVoteApplication,
        applicationStatus: String
    ) {
        validateObjects(
            postalVoteApplicationMessage,
            postalVoteApplication,
            *APPLICATION_FIELDS_TO_IGNORE
        )
        assertThat(postalVoteApplication.applicationDetails.applicationStatus).isEqualTo(
            ApplicationDetailsEntity.ApplicationStatus.valueOf(
                applicationStatus
            )
        )
        assertThat(postalVoteApplication.applicationDetails.signatureBase64).isEqualTo(SIGNATURE_BASE64_STRING)
        assertThat(postalVoteApplication.postalVoteDetails?.ballotOverseasAddress).isNotNull
        assertThat(postalVoteApplication.postalVoteDetails?.ballotBfpoAddress).isNotNull
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
