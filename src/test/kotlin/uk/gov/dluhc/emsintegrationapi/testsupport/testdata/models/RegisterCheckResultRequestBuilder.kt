package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.models

import org.apache.commons.lang3.RandomStringUtils
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker.Companion.faker
import uk.gov.dluhc.registercheckerapi.models.PostalVote
import uk.gov.dluhc.registercheckerapi.models.ProxyVote
import uk.gov.dluhc.registercheckerapi.models.RegisterCheckMatch
import uk.gov.dluhc.registercheckerapi.models.RegisterCheckResultRequest
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

fun buildRegisterCheckResultRequest(
    requestId: UUID = UUID.randomUUID(),
    gssCode: String = "E12345678",
    createdAt: OffsetDateTime = OffsetDateTime.now(),
    registerCheckMatchCount: Int = 1,
    applicationCreatedAt: OffsetDateTime? = OffsetDateTime.now(),
    registerCheckMatches: List<RegisterCheckMatch>? = listOf(buildRegisterCheckMatchRequest(applicationCreatedAt = applicationCreatedAt)),
    historicalSearchEarliestDate: OffsetDateTime? = OffsetDateTime.now()
) = RegisterCheckResultRequest(
    requestid = requestId,
    gssCode = gssCode,
    createdAt = createdAt,
    registerCheckMatchCount = registerCheckMatchCount,
    registerCheckMatches = registerCheckMatches,
    historicalSearchEarliestDate = historicalSearchEarliestDate
)

fun buildRegisterCheckMatchRequest(
    emsElectorId: String = "1234567899",
    attestationCount: Int = 1,
    fn: String = faker.name().firstName(),
    mn: String? = faker.name().firstName(),
    ln: String = faker.name().lastName(),
    dob: LocalDate? = faker.date().birthday().toLocalDateTime().toLocalDate(),
    phone: String? = faker.phoneNumber().cellPhone(),
    email: String? = faker.internet().emailAddress(),
    regstreet: String = faker.address().streetName(),
    regproperty: String? = faker.address().buildingNumber(),
    reglocality: String? = faker.address().streetName(),
    regtown: String? = faker.address().city(),
    regarea: String? = faker.address().state(),
    regpostcode: String = faker.address().postcode(),
    reguprn: String? = RandomStringUtils.randomNumeric(12),
    registeredStartDate: LocalDate? = LocalDate.now().minusDays(2),
    registeredEndDate: LocalDate? = LocalDate.now().plusDays(2),
    applicationCreatedAt: OffsetDateTime? = OffsetDateTime.now(),
    franchiseCode: String = "FRANCHISE123",
    postalVote: PostalVote? = buildPostalVote(),
    proxyVote: ProxyVote? = buildProxyVote(),
) = RegisterCheckMatch(
    emsElectorId = emsElectorId,
    fn = fn,
    ln = ln,
    regstreet = regstreet,
    regpostcode = regpostcode,
    attestationCount = attestationCount,
    mn = mn,
    dob = dob,
    regproperty = regproperty,
    reglocality = reglocality,
    regtown = regtown,
    regarea = regarea,
    reguprn = reguprn,
    phone = phone,
    email = email,
    registeredStartDate = registeredStartDate,
    registeredEndDate = registeredEndDate,
    applicationCreatedAt = applicationCreatedAt,
    franchiseCode = franchiseCode,
    postalVote = postalVote,
    proxyVote = proxyVote
)

fun buildPostalVote(
    postalVoteUntilFurtherNotice: Boolean = false,
    postalVoteForSingleDate: LocalDate? = faker.date().birthday().toLocalDateTime().toLocalDate(),
    postalVoteStartDate: LocalDate? = null,
    postalVoteEndDate: LocalDate? = null,
) = PostalVote(
    postalVoteUntilFurtherNotice = postalVoteUntilFurtherNotice,
    postalVoteForSingleDate = postalVoteForSingleDate,
    postalVoteStartDate = postalVoteStartDate,
    postalVoteEndDate = postalVoteEndDate,
)

fun buildProxyVote(
    proxyVoteUntilFurtherNotice: Boolean = true,
    proxyVoteForSingleDate: LocalDate? = null,
    proxyVoteStartDate: LocalDate? = null,
    proxyVoteEndDate: LocalDate? = null,
) = ProxyVote(
    proxyfn = faker.name().firstName(),
    proxyln = faker.name().lastName(),
    proxystreet = faker.address().streetName(),
    proxyVoteUntilFurtherNotice = proxyVoteUntilFurtherNotice,
    proxyVoteForSingleDate = proxyVoteForSingleDate,
    proxyVoteStartDate = proxyVoteStartDate,
    proxyVoteEndDate = proxyVoteEndDate,
)
