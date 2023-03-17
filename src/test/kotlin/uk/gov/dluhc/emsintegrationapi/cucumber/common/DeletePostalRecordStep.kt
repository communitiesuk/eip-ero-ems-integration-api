package uk.gov.dluhc.emsintegrationapi.cucumber.common

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.PurgeQueueRequest
import io.cucumber.java8.En
import mu.KotlinLogging
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.config.LocalStackContainerSettings
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository

private val logger = KotlinLogging.logger { }

open class DeletePostalRecordStep(
    private val localStackContainerSettings: LocalStackContainerSettings,
    private val amazonSQSAsync: AmazonSQSAsync,
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository
) : En {
    companion object {
        const val BEFORE = "Before"
        const val AFTER = "After"
    }

    init {
        Before("@DeletePostalEntity", this::deletePostalEntitiesBefore)
        After("@DeletePostalEntity", this::deletePostalEntitiesAfter)
        Before("@DeletePostalMessage", this::deletePostalMessageBefore)
        After("@DeletePostalMessage", this::deletePostalMessageAfter)
        Before("@DeleteProxyEntity", this::deletePostalEntitiesBefore)
        After("@DeleteProxyEntity", this::deletePostalEntitiesAfter)
        Before("@DeleteProxyMessage", this::deletePostalMessageBefore)
        After("@DeleteProxyMessage", this::deletePostalMessageAfter)
    }

    @Transactional
    open fun deleteAllPostalRecords(beforeOrAfter: String) {
        postalVoteApplicationRepository.deleteAll()
        logger.info("$beforeOrAfter - All the records from PostalVoteApplication table have been deleted")
    }

    private fun deleteSqsMessage(queueUrl: String, beforeOrAfter: String) {
        amazonSQSAsync.purgeQueue(PurgeQueueRequest(queueUrl))
        logger.info("$beforeOrAfter - All the messages from PostalVoteApplication queue `$queueUrl` have been deleted")
    }

    private fun deletePostalSqsMessage(beforeOrAfter: String) =
        deleteSqsMessage(localStackContainerSettings.mappedPostalApplicationQueueUrl, beforeOrAfter)

    private fun deletePostalEntitiesBefore() = deleteAllPostalRecords(BEFORE)

    private fun deletePostalEntitiesAfter() = deleteAllPostalRecords(AFTER)

    private fun deletePostalMessageBefore() = deletePostalSqsMessage(BEFORE)

    private fun deletePostalMessageAfter() = deletePostalSqsMessage(AFTER)
}


