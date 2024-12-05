package uk.gov.dluhc.emsintegrationapi.mapper

import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.messaging.models.BfpoAddress
import uk.gov.dluhc.emsintegrationapi.messaging.models.OverseasAddress
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.BfpoAddress as BfpoAddressEntity
import uk.gov.dluhc.emsintegrationapi.database.entity.OverseasAddress as OverseasAddressEntity
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteDetails as PostalVoteDetailsEntity

@Component
class PostalVoteDetailsMapper(private val addressMapper: AddressMapper) {

    fun mapToPostVoteDetailsEntity(postalVoteDetails: PostalVoteDetails?) = postalVoteDetails?.let {
        PostalVoteDetailsEntity(
            ballotAddress = addressMapper.mapToAddressEntity(it.ballotAddress, SourceSystem.POSTAL),
            ballotOverseasAddress = mapToAddressOverseasEntity(it.ballotOverseasPostalAddress),
            ballotBfpoAddress = mapToBfpoAddressEntity(it.ballotBfpoPostalAddress),
            ballotAddressReason = it.ballotAddressReason,
            voteForSingleDate = it.voteForSingleDate,
            voteStartDate = it.voteStartDate,
            voteEndDate = it.voteEndDate,
            voteUntilFurtherNotice = it.voteUntilFurtherNotice
        )
    }

    private fun mapToAddressOverseasEntity(ballotOverseasPostalAddress: OverseasAddress?) = ballotOverseasPostalAddress?.let {
        OverseasAddressEntity(
            addressLine1 = it.addressLine1,
            addressLine2 = it.addressLine2,
            addressLine3 = it.addressLine3,
            addressLine4 = it.addressLine4,
            addressLine5 = it.addressLine5,
            country = it.country,
            createdBy = SourceSystem.POSTAL
        )
    }

    private fun mapToBfpoAddressEntity(ballotBfpoPostalAddress: BfpoAddress?) = ballotBfpoPostalAddress?.let {
        BfpoAddressEntity(
            bfpoNumber = it.bfpoNumber,
            addressLine1 = it.addressLine1,
            addressLine2 = it.addressLine2,
            addressLine3 = it.addressLine3,
            addressLine4 = it.addressLine4,
            addressLine5 = it.addressLine5,
            createdBy = SourceSystem.POSTAL
        )
    }
}
