package uk.gov.dluhc.emsintegrationapi.cucumber.common

import com.amazonaws.services.sqs.AmazonSQSAsync
import io.cucumber.java8.En
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.POSTAL_APPLICATION_QUEUE
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.PROXY_APPLICATION_QUEUE
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.Companion.deleteRecords
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.Companion.deleteSqsMessage
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.TestPhase.AFTER
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.TestPhase.BEFORE
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository

open class DeletePostalRecordStep(
    private val queueConfiguration: QueueConfiguration,
    private val amazonSQSAsync: AmazonSQSAsync,
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository
) : En {

    init {
        Before("@DeletePostalEntity", ::deletePostalEntitiesBefore)
        After("@DeletePostalEntity", ::deletePostalEntitiesAfter)
        Before("@DeletePostalMessage", ::deletePostalMessageBefore)
        After("@DeletePostalMessage", ::deletePostalMessageAfter)
    }

    @Transactional
    open fun deleteAllPostalRecords(testPhase: StepHelper.TestPhase) =
        deleteRecords(postalVoteApplicationRepository, testPhase)

    private fun deletePostalEntitiesBefore() = deleteAllPostalRecords(BEFORE)

    private fun deletePostalEntitiesAfter() = deleteAllPostalRecords(AFTER)

    private fun deletePostalMessageBefore() =
        deleteSqsMessage(amazonSQSAsync, queueConfiguration.getQueueNameFrom(POSTAL_APPLICATION_QUEUE), BEFORE)

    private fun deletePostalMessageAfter() =
        deleteSqsMessage(amazonSQSAsync, queueConfiguration.getQueueNameFrom(PROXY_APPLICATION_QUEUE), AFTER)
}
