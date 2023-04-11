package uk.gov.dluhc.emsintegrationapi.cucumber.common

import com.amazonaws.services.sqs.AmazonSQSAsync
import io.cucumber.java8.En
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration.QueueName.PROXY_APPLICATION_QUEUE
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.Companion.deleteRecords
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.Companion.deleteSqsMessage
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.TestPhase.AFTER
import uk.gov.dluhc.emsintegrationapi.cucumber.common.StepHelper.TestPhase.BEFORE
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository

open class DeleteProxyRecordStep(
    private val queueConfiguration: QueueConfiguration,
    private val amazonSQSAsync: AmazonSQSAsync,
    private val proxyVoteApplicationRepository: ProxyVoteApplicationRepository
) : En {

    init {
        Before("@DeleteProxyEntity", ::deleteProxyEntitiesBefore)
        After("@DeleteProxyEntity", ::deleteProxyEntitiesAfter)
        Before("@DeleteProxyMessage", ::deleteProxyMessageBefore)
        After("@DeleteProxyMessage", ::deleteProxyMessageAfter)
    }

    @Transactional
    open fun deleteAllProxyRecords(testPhase: StepHelper.TestPhase) =
        deleteRecords(proxyVoteApplicationRepository, testPhase)

    private fun deleteProxyEntitiesBefore() = deleteAllProxyRecords(BEFORE)

    private fun deleteProxyEntitiesAfter() = deleteAllProxyRecords(AFTER)

    private fun deleteProxyMessageBefore() =
        deleteSqsMessage(
            amazonSQSAsync, queueConfiguration.getQueueNameFrom(PROXY_APPLICATION_QUEUE), BEFORE
        )

    private fun deleteProxyMessageAfter() =
        deleteSqsMessage(
            amazonSQSAsync, queueConfiguration.getQueueNameFrom(PROXY_APPLICATION_QUEUE), AFTER
        )
}
