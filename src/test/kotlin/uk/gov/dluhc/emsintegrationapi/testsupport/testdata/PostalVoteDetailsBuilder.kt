package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import org.testcontainers.utility.Base58.randomString
import uk.gov.dluhc.emsintegrationapi.database.entity.Address
import uk.gov.dluhc.emsintegrationapi.database.entity.BfpoAddress
import uk.gov.dluhc.emsintegrationapi.database.entity.OverseasAddress
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteDetails
import java.time.LocalDate
import uk.gov.dluhc.emsintegrationapi.messaging.models.Address as AddressMessageDto
import uk.gov.dluhc.emsintegrationapi.messaging.models.BfpoAddress as BfpoAddressMessageDto
import uk.gov.dluhc.emsintegrationapi.messaging.models.OverseasAddress as OverseasAddressMessageDto
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteDetails as PostalVoteDetailsMessageDto

fun buildPostalVoteDetailsEntity(
    ballotAddress: Address? = buildAddressEntity(),
    ballotAddressReason: String? = randomString(10),
    ballotBfpoAddress: BfpoAddress? = buildBfpoAddressEntity(),
    ballotOverseasAddress: OverseasAddress? = buildOverseasAddressEntity(),
    voteForSingleDate: LocalDate? = getRandomPastDate(),
    voteStartDate: LocalDate? = getRandomPastDate(10),
    voteEndDate: LocalDate? = getRandomPastDate(1),
    voteUntilFurtherNotice: Boolean? = false
) = PostalVoteDetails(
    ballotAddress = ballotAddress,
    ballotAddressReason = ballotAddressReason,
    ballotBfpoAddress = ballotBfpoAddress,
    ballotOverseasAddress = ballotOverseasAddress,
    voteForSingleDate = voteForSingleDate,
    voteStartDate = voteStartDate,
    voteEndDate = voteEndDate,
    voteUntilFurtherNotice = voteUntilFurtherNotice
)

fun buildPostalVoteDetailsMessageDto(
    ballotAddress: AddressMessageDto? = buildAddressMessageDto(),
    ballotAddressReason: String? = randomString(10),
    ballotOverseasAddress: OverseasAddressMessageDto? = buildOverseasAddressMessageDto(),
    ballotBfpoAddress: BfpoAddressMessageDto? = buildBfpoAddressMessageDto(),
    voteForSingleDate: LocalDate? = getRandomPastDate(),
    voteStartDate: LocalDate? = getRandomPastDate(10),
    voteEndDate: LocalDate? = getRandomPastDate(1),
    voteUntilFurtherNotice: Boolean? = false
) = PostalVoteDetailsMessageDto(
    ballotAddress = ballotAddress,
    ballotOverseasPostalAddress = ballotOverseasAddress,
    ballotBfpoPostalAddress = ballotBfpoAddress,
    ballotAddressReason = ballotAddressReason,
    voteForSingleDate = voteForSingleDate,
    voteStartDate = voteStartDate,
    voteEndDate = voteEndDate,
    voteUntilFurtherNotice = voteUntilFurtherNotice
)
