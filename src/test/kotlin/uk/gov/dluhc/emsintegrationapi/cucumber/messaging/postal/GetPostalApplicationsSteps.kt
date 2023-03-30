package uk.gov.dluhc.emsintegrationapi.cucumber.messaging.postal

import io.cucumber.java8.En
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class GetPostalApplicationsSteps : En {
    init {
        Given("{int} postal vote application exist with status {string} exist") { numberOfRecords: Int, recordStatus: String ->
            logger.info(
                "Creating $numberOfRecords of postal vote applications"
            )
        }
        When("I send get postal vote request with page size as {int}") { pageSize: Int ->
            logger.info { "Sending get request with page size $pageSize" }
        }
        Then("I received as response with page size {int}") { expectedPageSize: Int ->
            {
                logger.info("Expected number of post vote applications $expectedPageSize")
            }
        }
        When("I send get postal vote request without page size") {
            logger.info { "Sending get request without page size" }
        }
    }
}
