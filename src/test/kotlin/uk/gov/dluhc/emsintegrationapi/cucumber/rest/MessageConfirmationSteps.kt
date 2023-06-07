package uk.gov.dluhc.emsintegrationapi.cucumber.rest

import io.awspring.cloud.messaging.core.QueueMessagingTemplate
import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import uk.gov.dluhc.emsintegrationapi.constants.ApplicationConstants
import uk.gov.dluhc.emsintegrationapi.messaging.models.EmsConfirmedReceiptMessage
import uk.gov.dluhc.emsintegrationapi.models.EMSApplicationStatus
import java.util.concurrent.TimeUnit

class MessageConfirmationSteps(
    private val queueMessagingTemplate: QueueMessagingTemplate,
) : En {
    private var emsConfirmedReceiptMessage: EmsConfirmedReceiptMessage? = null

    init {
        Then("the {string} queue has a SUCCESS confirmation message for the application id {string}") { queueName: String, applicationId: String ->
            await.pollDelay(2, TimeUnit.SECONDS)
                .untilAsserted { assertThat(readMessage(queueName)).isNotNull }
            assertThat(emsConfirmedReceiptMessage).isNotNull.isEqualTo(EmsConfirmedReceiptMessage(applicationId, EMSApplicationStatus.SUCCESS.value))
        }
        Then("the {string} queue has a FAILURE confirmation message for the application id {string}") { queueName: String, applicationId: String ->
            await.pollDelay(2, TimeUnit.SECONDS)
                .untilAsserted { assertThat(readMessage(queueName)).isNotNull }
            assertThat(emsConfirmedReceiptMessage).isNotNull.isEqualTo(
                EmsConfirmedReceiptMessage(
                    id = applicationId,
                    status = EMSApplicationStatus.FAILURE.value,
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
        emsConfirmedReceiptMessage = queueMessagingTemplate.receiveAndConvert(
            queueName,
            EmsConfirmedReceiptMessage::class.java
        )
        return emsConfirmedReceiptMessage
    }
}
