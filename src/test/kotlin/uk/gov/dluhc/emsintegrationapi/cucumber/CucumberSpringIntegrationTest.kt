package uk.gov.dluhc.emsintegrationapi.cucumber

import io.cucumber.spring.CucumberContextConfiguration
import uk.gov.dluhc.emsintegrationapi.config.IntegrationTest

@CucumberContextConfiguration
private class CucumberSpringIntegrationTest : IntegrationTest()
