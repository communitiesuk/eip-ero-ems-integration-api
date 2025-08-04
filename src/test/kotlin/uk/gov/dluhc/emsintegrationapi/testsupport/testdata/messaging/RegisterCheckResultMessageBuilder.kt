package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.messaging

import net.datafaker.Address
import org.apache.commons.lang3.RandomStringUtils
import uk.gov.dluhc.emsintegrationapi.database.entity.PersonalDetail
import uk.gov.dluhc.emsintegrationapi.dto.AddressDto
import uk.gov.dluhc.emsintegrationapi.dto.RegisterCheckMatchDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker.Companion.faker
import uk.gov.dluhc.registercheckerapi.messaging.models.RegisterCheckAddress
import uk.gov.dluhc.registercheckerapi.messaging.models.RegisterCheckMatch
import uk.gov.dluhc.registercheckerapi.messaging.models.RegisterCheckPersonalDetail
import uk.gov.dluhc.registercheckerapi.messaging.models.RegisterCheckResult
import uk.gov.dluhc.registercheckerapi.messaging.models.RegisterCheckResultMessage
import uk.gov.dluhc.registercheckerapi.messaging.models.SourceType
import uk.gov.dluhc.registercheckerapi.messaging.models.VotingArrangement
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

fun buildRegisterCheckResultMessage(
    sourceType: SourceType = SourceType.VOTER_MINUS_CARD,
    sourceReference: String = "VPIOKNHPBP",
    sourceCorrelationId: UUID = UUID.randomUUID(),
    registerCheckResult: RegisterCheckResult = RegisterCheckResult.EXACT_MINUS_MATCH,
    matches: List<RegisterCheckMatch> = listOf(buildVcaRegisterCheckMatch()),
    historicalSearchEarliestDate: OffsetDateTime? = OffsetDateTime.now()
) = RegisterCheckResultMessage(
    sourceType = sourceType,
    sourceReference = sourceReference,
    sourceCorrelationId = sourceCorrelationId,
    registerCheckResult = registerCheckResult,
    matches = matches,
    historicalSearchEarliestDate = historicalSearchEarliestDate
)

fun buildVcaRegisterCheckMatch(
    personalDetail: RegisterCheckPersonalDetail = buildVcaRegisterCheckPersonalDetailSqs(),
    emsElectoralId: String = aValidEmsElectoralId(),
    franchiseCode: String = aValidFranchiseCode(),
    registeredStartDate: LocalDate? = LocalDate.now().minusDays(2),
    registeredEndDate: LocalDate? = LocalDate.now().plusDays(2),
    postalVotingArrangement: VotingArrangement? = null,
    proxyVotingArrangement: VotingArrangement? = null,
) = RegisterCheckMatch(
    personalDetail,
    emsElectorId = emsElectoralId,
    franchiseCode = franchiseCode,
    registeredStartDate = registeredStartDate,
    registeredEndDate = registeredEndDate,
    postalVotingArrangement = postalVotingArrangement,
    proxyVotingArrangement = proxyVotingArrangement,
)

fun buildVcaRegisterCheckMatchFromMatchApi(match: uk.gov.dluhc.registercheckerapi.models.RegisterCheckMatch): RegisterCheckMatch =
    with(match) {
        buildVcaRegisterCheckMatch(
            personalDetail = buildVcaRegisterCheckPersonalDetailSqsFromApiModel(this),
            emsElectoralId = emsElectorId,
            franchiseCode = franchiseCode,
            registeredStartDate = registeredStartDate,
            registeredEndDate = registeredEndDate,
            postalVotingArrangement = postalVote?.let(::buildVcaRegisterCheckMatchVotingArrangementFromPostalVoteApi),
            proxyVotingArrangement = proxyVote?.let(::buildVcaRegisterCheckMatchVotingArrangementFromProxyVoteApi),
        )
    }

fun buildVcaRegisterCheckMatchVotingArrangementFromPostalVoteApi(vote: uk.gov.dluhc.registercheckerapi.models.PostalVote) =
    with(vote) {
        buildVcaVotingArrangementSqs(
            untilFurtherNotice = postalVoteUntilFurtherNotice!!,
            forSingleDate = postalVoteForSingleDate,
            startDate = postalVoteStartDate,
            endDate = postalVoteEndDate,
        )
    }

fun buildVcaRegisterCheckMatchVotingArrangementFromProxyVoteApi(vote: uk.gov.dluhc.registercheckerapi.models.ProxyVote) =
    with(vote) {
        buildVcaVotingArrangementSqs(
            untilFurtherNotice = proxyVoteUntilFurtherNotice!!,
            forSingleDate = proxyVoteForSingleDate,
            startDate = proxyVoteStartDate,
            endDate = proxyVoteEndDate,
        )
    }

fun buildVcaRegisterCheckMatchFromMatchDto(match: RegisterCheckMatchDto): RegisterCheckMatch =
    with(match) {
        buildVcaRegisterCheckMatch(
            personalDetail = buildVcaRegisterCheckPersonalDetailSqsFromDto(this),
            emsElectoralId = emsElectorId,
            franchiseCode = franchiseCode,
            registeredStartDate = registeredStartDate,
            registeredEndDate = registeredEndDate,
        )
    }

fun buildVcaRegisterCheckPersonalDetailSqs(
    firstName: String = faker.name().firstName(),
    middleNames: String? = faker.name().firstName(),
    surname: String = faker.name().lastName(),
    dateOfBirth: LocalDate? = faker.date().birthday().toLocalDateTime().toLocalDate(),
    email: String? = faker.internet().emailAddress(),
    phone: String? = faker.phoneNumber().cellPhone(),
    address: RegisterCheckAddress = buildVcaRegisterCheckAddressSqs()
) = RegisterCheckPersonalDetail(
    firstName = firstName,
    middleNames = middleNames,
    surname = surname,
    dateOfBirth = dateOfBirth,
    phone = phone,
    email = email,
    address = address
)

fun buildVcaVotingArrangementSqs(
    untilFurtherNotice: Boolean,
    forSingleDate: LocalDate?,
    startDate: LocalDate?,
    endDate: LocalDate?,
) = VotingArrangement(
    untilFurtherNotice = untilFurtherNotice,
    forSingleDate = forSingleDate,
    startDate = startDate,
    endDate = endDate,
)

private fun buildVcaRegisterCheckAddressSqs(
    fakeAddress: Address = faker.address(),
    property: String? = fakeAddress.buildingNumber(),
    street: String = fakeAddress.streetName(),
    locality: String? = fakeAddress.streetName(),
    town: String? = fakeAddress.city(),
    area: String? = fakeAddress.state(),
    postcode: String = fakeAddress.postcode(),
    uprn: String? = RandomStringUtils.secure().nextNumeric(12),
) = RegisterCheckAddress(
    property = property,
    street = street,
    locality = locality,
    town = town,
    area = area,
    postcode = postcode,
    uprn = uprn,
)

fun buildVcaRegisterCheckPersonalDetailSqsFromApiModel(match: uk.gov.dluhc.registercheckerapi.models.RegisterCheckMatch): RegisterCheckPersonalDetail =
    with(match) {
        buildVcaRegisterCheckPersonalDetailSqs(
            firstName = fn,
            middleNames = mn,
            surname = ln,
            dateOfBirth = dob,
            phone = phone,
            email = email,
            address = buildVcaRegisterCheckAddressSqsFromApiModel(match)
        )
    }

fun buildVcaRegisterCheckPersonalDetailSqsFromDto(match: RegisterCheckMatchDto): RegisterCheckPersonalDetail =
    with(match.personalDetail) {
        buildVcaRegisterCheckPersonalDetailSqs(
            firstName = firstName,
            middleNames = middleNames,
            surname = surname,
            dateOfBirth = dateOfBirth,
            phone = phone,
            email = email,
            address = buildVcaRegisterCheckAddressSqsFromDto(address)
        )
    }

fun buildVcaRegisterCheckPersonalDetailSqsFromEntity(personalDetailEntity: PersonalDetail): RegisterCheckPersonalDetail =
    with(personalDetailEntity) {
        buildVcaRegisterCheckPersonalDetailSqs(
            firstName = firstName,
            middleNames = middleNames,
            surname = surname,
            dateOfBirth = dateOfBirth,
            phone = phoneNumber,
            email = email,
            address = buildVcaRegisterCheckAddressSqsFromEntity(address)
        )
    }

fun buildVcaRegisterCheckVotingArrangementSqsFromEntity(entity: uk.gov.dluhc.emsintegrationapi.database.entity.VotingArrangement): VotingArrangement =
    with(entity) {
        buildVcaVotingArrangementSqs(
            untilFurtherNotice = untilFurtherNotice,
            forSingleDate = forSingleDate,
            startDate = startDate,
            endDate = endDate,
        )
    }

private fun buildVcaRegisterCheckAddressSqsFromApiModel(match: uk.gov.dluhc.registercheckerapi.models.RegisterCheckMatch) =
    with(match) {
        buildVcaRegisterCheckAddressSqs(
            property = regproperty,
            street = regstreet,
            locality = reglocality,
            town = regtown,
            area = regarea,
            postcode = regpostcode,
            uprn = reguprn,
        )
    }

private fun buildVcaRegisterCheckAddressSqsFromDto(address: AddressDto): RegisterCheckAddress =
    with(address) {
        buildVcaRegisterCheckAddressSqs(
            property = property,
            street = street,
            locality = locality,
            town = town,
            area = area,
            postcode = postcode,
            uprn = uprn,
        )
    }

private fun buildVcaRegisterCheckAddressSqsFromEntity(address: uk.gov.dluhc.emsintegrationapi.database.entity.Address): RegisterCheckAddress =
    with(address) {
        buildVcaRegisterCheckAddressSqs(
            property = property,
            street = street,
            locality = locality,
            town = town,
            area = area,
            postcode = postcode,
            uprn = uprn,
        )
    }

private fun aValidEmsElectoralId() = faker.examplify("AAAAAAA")

private fun aValidFranchiseCode() = faker.examplify("AAA")
