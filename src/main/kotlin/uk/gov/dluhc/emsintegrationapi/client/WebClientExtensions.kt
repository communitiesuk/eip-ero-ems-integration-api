package uk.gov.dluhc.emsintegrationapi.client

import org.springframework.web.reactive.function.client.WebClient

fun WebClient.RequestHeadersSpec<*>.bearerToken(bearerToken: String): WebClient.RequestBodySpec =
    header("authorization", "Bearer $bearerToken") as WebClient.RequestBodySpec
