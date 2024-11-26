package uk.gov.dluhc.emsintegrationapi.cucumber.rest

import io.awspring.cloud.sqs.operations.SqsReceiveOptions
import io.awspring.cloud.sqs.operations.SqsSendOptions
import io.awspring.cloud.sqs.operations.SqsTemplate
import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import uk.gov.dluhc.emsintegrationapi.constants.ApplicationConstants
import uk.gov.dluhc.emsintegrationapi.messaging.models.EmsConfirmedReceiptMessage
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteApplicationMessage
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

class MessageConfirmationSteps(
    private val sqsTemplate: SqsTemplate,
) : En {
    private var emsConfirmedReceiptMessage: EmsConfirmedReceiptMessage? = null

    init {
        Then("the {string} queue has a SUCCESS confirmation message for the application id {string}") { queueName: String, applicationId: String ->
            await.pollDelay(2, TimeUnit.SECONDS)
                .untilAsserted { assertThat(readMessage(queueName)).isNotNull }
            assertThat(emsConfirmedReceiptMessage).isNotNull.isEqualTo(EmsConfirmedReceiptMessage(applicationId, EmsConfirmedReceiptMessage.Status.SUCCESS))
        }
        Then("the {string} queue has a FAILURE confirmation message for the application id {string}") { queueName: String, applicationId: String ->
            await.pollDelay(2, TimeUnit.SECONDS)
                .untilAsserted { assertThat(readMessage(queueName)).isNotNull }
            assertThat(emsConfirmedReceiptMessage).isNotNull.isEqualTo(
                EmsConfirmedReceiptMessage(
                    id = applicationId,
                    status = EmsConfirmedReceiptMessage.Status.FAILURE,
                    message = ApplicationConstants.EMS_MESSAGE_TEXT,
                    details = ApplicationConstants.EMS_DETAILS_TEXT
                )
            )
        }
        And("there will be no confirmation message on the queue {string}") { queueName: String ->
            await.pollDelay(2, TimeUnit.SECONDS)
                .untilAsserted { assertThat(readMessage(queueName)).isNull() }
            assertThat(emsConfirmedReceiptMessage).isNull()
        }
    }

    private fun readMessage(queueName: String): EmsConfirmedReceiptMessage? {
        val emsConfirmedReceiptMessage = sqsTemplate.receive(
            { from -> from.queue(queueName) },
            EmsConfirmedReceiptMessage::class.java
        )
        return emsConfirmedReceiptMessage.map { it.payload }.getOrNull()
    }
}
