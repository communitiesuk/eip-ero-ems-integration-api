package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import org.testcontainers.utility.Base58.randomString
import uk.gov.dluhc.emsintegrationapi.database.entity.Address
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteDetails
import uk.gov.dluhc.emsintegrationapi.messaging.models.RejectedReasonItem
import java.time.LocalDate
import uk.gov.dluhc.emsintegrationapi.messaging.models.Address as AddressMessageDto
import uk.gov.dluhc.emsintegrationapi.messaging.models.ProxyVoteDetails as ProxyVoteDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.messaging.models.RejectedReason as RejectedReasonDto
import uk.gov.dluhc.emsintegrationapi.messaging.models.RejectedReasons as RejectedReasonsDto

fun buildProxyVoteDetailsEntity(
    proxyFirstName: String? = DataFaker.faker.name().firstName(),
    proxyMiddleNames: String? = DataFaker.faker.name().firstName(),
    proxySurname: String? = DataFaker.faker.name().lastName(),
    proxyEmail: String? = DataFaker.faker.internet().emailAddress(),
    proxyPhone: String? = DataFaker.faker.phoneNumber().phoneNumber(),
    proxyAddress: Address? = buildAddressEntity(),
    proxyReason: String = randomString(10),
    voteForSingleDate: LocalDate? = getRandomPastDate(),
    voteStartDate: LocalDate? = getRandomPastDate(10),
    voteEndDate: LocalDate? = getRandomPastDate(1),
    proxyFamilyRelationship: String? = randomString(20),
    voteUntilFurtherNotice: Boolean? = false
) = ProxyVoteDetails(
    proxyFirstName = proxyFirstName,
    proxyMiddleNames = proxyMiddleNames,
    proxySurname = proxySurname,
    proxyEmail = proxyEmail,
    proxyPhone = proxyPhone,
    proxyAddress = proxyAddress,
    proxyReason = proxyReason,
    proxyFamilyRelationship = proxyFamilyRelationship,
    voteForSingleDate = voteForSingleDate,
    voteStartDate = voteStartDate,
    voteEndDate = voteEndDate,
    voteUntilFurtherNotice = voteUntilFurtherNotice
)

fun buildProxyVoteDetailsMessageDto(
    proxyFirstName: String = DataFaker.faker.name().firstName(),
    proxyMiddleNames: String? = DataFaker.faker.name().firstName(),
    proxySurname: String = DataFaker.faker.name().lastName(),
    proxyEmail: String? = DataFaker.faker.internet().emailAddress(),
    proxyPhone: String? = DataFaker.faker.phoneNumber().phoneNumber(),
    proxyAddress: AddressMessageDto = buildAddressMessageDto(),
    proxyReason: String = randomString(10),
    voteForSingleDate: LocalDate? = getRandomPastDate(),
    voteStartDate: LocalDate? = getRandomPastDate(10),
    voteEndDate: LocalDate? = getRandomPastDate(1),
    proxyFamilyRelationship: String? = randomString(20),
    voteUntilFurtherNotice: Boolean? = false,
    rejectedReasons: RejectedReasonsDto? = buildProxyRejectedReasonsDto()
) = ProxyVoteDetailsMessageDto(
    proxyFirstName = proxyFirstName,
    proxyMiddleNames = proxyMiddleNames,
    proxySurname = proxySurname,
    proxyEmail = proxyEmail,
    proxyPhone = proxyPhone,
    proxyAddress = proxyAddress,
    proxyReason = proxyReason,
    proxyFamilyRelationship = proxyFamilyRelationship,
    voteForSingleDate = voteForSingleDate,
    voteStartDate = voteStartDate,
    voteEndDate = voteEndDate,
    voteUntilFurtherNotice = voteUntilFurtherNotice,
    rejectedReasons = rejectedReasons
)

fun buildProxyRejectedReasonsDto(
    englishNotes: String? = DataFaker.faker.house().room(),
    englishReason: Set<String>? = setOf(),
    welshNotes: String? = DataFaker.faker.house().furniture(),
    welshReason: Set<String>? = setOf(),
) = RejectedReasonsDto(
    englishReason = RejectedReasonDto(notes = englishNotes, reasonList = englishReason?.map { reason -> RejectedReasonItem(reason, "OTHER_REJECT_REASON", false) }),
    welshReason = RejectedReasonDto(notes = welshNotes, reasonList = welshReason?.map { reason -> RejectedReasonItem(reason, "OTHER_REJECT_REASON", false) }),
)
