package uk.gov.dluhc.emsintegrationapi.cucumber.rest

import io.cucumber.spring.ScenarioScope
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient

@Component
@ScenarioScope
data class ApiResponse(var responseSpec: WebTestClient.ResponseSpec?)
