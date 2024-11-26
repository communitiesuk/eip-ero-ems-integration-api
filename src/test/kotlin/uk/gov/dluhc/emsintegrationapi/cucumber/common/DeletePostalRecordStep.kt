package uk.gov.dluhc.emsintegrationapi.cucumber.common

import io.cucumber.java8.En
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.DELETED_POSTAL_APPLICATION_QUEUE
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.POSTAL_APPLICATION_QUEUE
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.Companion.deleteRecords
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.Companion.deleteSqsMessage
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.TestPhase.AFTER
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.TestPhase.BEFORE
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository

open class DeletePostalRecordStep(
    private val queueConfiguration: QueueConfiguration,
    private val sqsAsyncClient: SqsAsyncClient,
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository
) : En {

    init {
        Before("@DeletePostalEntity", ::deletePostalEntitiesBefore)
        After("@DeletePostalEntity", ::deletePostalEntitiesAfter)
        Before("@DeletePostalMessage", ::deletePostalMessageBefore)
        After("@DeletePostalMessage", ::deletePostalMessageAfter)
        Before("@DeletePostalConfirmationMessage", ::deletePostalConfirmationMessageBefore)
        After("@DeletePostalConfirmationMessage", ::deletePostalConfirmationMessageAfter)
    }

    @Transactional
    open fun deleteAllPostalRecords(testPhase: StepHelper.TestPhase) =
        deleteRecords(postalVoteApplicationRepository, testPhase)

    private fun deletePostalEntitiesBefore() = deleteAllPostalRecords(BEFORE)

    private fun deletePostalEntitiesAfter() = deleteAllPostalRecords(AFTER)

    private fun deletePostalMessageBefore() =
        deleteSqsMessage(sqsAsyncClient, queueConfiguration.getQueueNameFrom(POSTAL_APPLICATION_QUEUE), BEFORE)

    private fun deletePostalMessageAfter() =
        deleteSqsMessage(sqsAsyncClient, queueConfiguration.getQueueNameFrom(POSTAL_APPLICATION_QUEUE), AFTER)

    private fun deletePostalConfirmationMessageBefore() =
        deleteSqsMessage(sqsAsyncClient, queueConfiguration.getQueueNameFrom(DELETED_POSTAL_APPLICATION_QUEUE), BEFORE)

    private fun deletePostalConfirmationMessageAfter() =
        deleteSqsMessage(sqsAsyncClient, queueConfiguration.getQueueNameFrom(DELETED_POSTAL_APPLICATION_QUEUE), AFTER)
}
