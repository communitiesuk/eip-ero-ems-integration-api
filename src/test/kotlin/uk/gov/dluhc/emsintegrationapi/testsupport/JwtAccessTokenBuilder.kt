package uk.gov.dluhc.emsintegrationapi.testsupport

import io.jsonwebtoken.Jwts
import uk.gov.dluhc.emsintegrationapi.testsupport.RsaKeyPair.privateKey
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID

const val UNAUTHORIZED_BEARER_TOKEN: String =
    "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQHdpbHRzaGlyZS5nb3YudWsiLCJpYXQiOjE1MTYyMzkwMjIsImF1dGhvcml0aWVzIjpbImVyby1hZG1pbiJdfQ.-pxW8z2xb-AzNLTRP_YRnm9fQDcK6CLt6HimtS8VcDY"

fun getBearerToken(
    email: String,
    issuer: String,
) = "Bearer ${buildAccessToken(email, issuer)}"

fun buildAccessToken(
    email: String,
    issuer: String
): String = Jwts.builder()
    .subject(UUID.randomUUID().toString())
    .claims(
        mapOf(
            "email" to email
        )
    )
    .issuer(issuer)
    .issuedAt(Date.from(Instant.now()))
    .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
    .signWith(RsaKeyPair.privateKey, Jwts.SIG.RS256)
    .compact()
