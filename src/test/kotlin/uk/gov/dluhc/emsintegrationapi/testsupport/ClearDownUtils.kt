package uk.gov.dluhc.emsintegrationapi.testsupport

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.PurgeQueueRequest
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository

object ClearDownUtils {

    fun clearDownRecords(
        proxyRepository: ProxyVoteApplicationRepository? = null,
        postalRepository: PostalVoteApplicationRepository? = null,
        amazonSQSAsync: AmazonSQSAsync? = null,
        queueName: String? = null
    ) {
        proxyRepository?.deleteAll()
        postalRepository?.deleteAll()
        queueName?.let {
            amazonSQSAsync?.purgeQueue(PurgeQueueRequest(queueName))
        }
    }
}
