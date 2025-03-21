package uk.gov.dluhc.emsintegrationapi.testsupport

import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.RegisterCheckRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.RegisterCheckResultDataRepository

object ClearDownUtils {
    fun clearDownRecords(
        proxyRepository: ProxyVoteApplicationRepository? = null,
        postalRepository: PostalVoteApplicationRepository? = null,
        registerCheckRepository: RegisterCheckRepository? = null,
        registerCheckResultDataRepository: RegisterCheckResultDataRepository? = null,
        sqsAsyncClient: SqsAsyncClient? = null,
        queueName: String? = null,
    ) {
        proxyRepository?.deleteAll()
        postalRepository?.deleteAll()
        registerCheckRepository?.deleteAll()
        registerCheckResultDataRepository?.deleteAll()
        queueName?.let {
            val request = PurgeQueueRequest.builder().queueUrl(queueName).build()
            sqsAsyncClient?.purgeQueue(request)
        }
    }
}
