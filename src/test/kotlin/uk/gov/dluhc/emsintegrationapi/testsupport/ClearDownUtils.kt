package uk.gov.dluhc.emsintegrationapi.testsupport

import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.RegisterCheckRepository

object ClearDownUtils {
    fun clearDownRecords(
        proxyRepository: ProxyVoteApplicationRepository? = null,
        postalRepository: PostalVoteApplicationRepository? = null,
        registerCheckRepository: RegisterCheckRepository? = null,
        sqsAsyncClient: SqsAsyncClient? = null,
        queueName: String? = null,
    ) {
        proxyRepository?.deleteAll()
        postalRepository?.deleteAll()
        registerCheckRepository?.deleteAll()
        queueName?.let {
            val request = PurgeQueueRequest.builder().queueUrl(queueName).build()
            sqsAsyncClient?.purgeQueue(request)
        }
    }
}
