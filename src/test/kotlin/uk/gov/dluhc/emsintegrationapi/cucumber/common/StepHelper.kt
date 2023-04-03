package uk.gov.dluhc.emsintegrationapi.cucumber.common

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.PurgeQueueRequest
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.springframework.aop.framework.AopProxyUtils
import org.springframework.data.repository.CrudRepository
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream

private val logger = KotlinLogging.logger { }

class StepHelper {

    companion object {
        fun deleteRecords(repository: CrudRepository<*, *>, testPhase: TestPhase) {
            repository.deleteAll()
            logger.info("$testPhase - ${AopProxyUtils.proxiedUserInterfaces(repository)[0]} - records have been deleted")
        }

        fun deleteSqsMessage(amazonSQSAsync: AmazonSQSAsync, queueUrl: String, testPhase: TestPhase) {
            amazonSQSAsync.purgeQueue(PurgeQueueRequest(queueUrl))
            logger.info("$testPhase - All the messages from queue `$queueUrl` have been deleted")
        }

        fun confirmTheEntityDoesNotExist(repository: CrudRepository<*, String>, id: String) {
            assertThat(id.trim()).hasSizeGreaterThan(1)
            await.during(5, TimeUnit.SECONDS).atMost(6, TimeUnit.SECONDS).untilAsserted {
                assertThat(repository.findById(id)).isEmpty
            }
        }

        fun <T> saveRecords(
            repository: CrudRepository<T, *>,
            numberOfRecords: Int,
            buildFunction: () -> T
        ): MutableIterable<T> =
            repository.saveAll(IntStream.rangeClosed(1, numberOfRecords).mapToObj { buildFunction() }.toList())
    }

    enum class TestPhase {
        BEFORE,
        AFTER
    }
}
