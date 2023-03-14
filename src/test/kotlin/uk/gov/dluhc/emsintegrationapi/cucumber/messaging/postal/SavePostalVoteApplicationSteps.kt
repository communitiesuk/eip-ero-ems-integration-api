package uk.gov.dluhc.emsintegrationapi.cucumber.messaging.postal

import io.cucumber.java8.En
import mu.KotlinLogging
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteApplicationMessage

private val logger = KotlinLogging.logger { }

class SavePostalVoteApplicationSteps(private val messageSender: MessageSender<PostalVoteApplicationMessage>) : En {
    init {
        Given(
            "a postal vote application with the application id {string} and electoral id {string}"
        ) { applicationId: String, emsElectorId: String ->
            logger.info("Postal application id $applicationId and Elector id = $emsElectorId")
        }
        When("I send an sqs message to the postal application queue") {
            logger.info("Send to the queue")
        }
        Then("the message is successfully processed") {
            logger.info("Successfully processed")
        }
        And("a postal vote application has been saved with application id {string} and electoral id {string}") { applicationId: String, emsElectorId: String ->
            logger.info("The saved Postal application has id $applicationId and Elector id = $emsElectorId")
        }
    }
}
