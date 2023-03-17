package uk.gov.dluhc.emsintegrationapi.mapper

import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteDetails as PostalVoteDetailsEntity

@Component
class PostalVoteDetailsMapper(private val addressMapper: AddressMapper) {

    fun mapToPostVoteDetailsEntity(postalVoteDetails: PostalVoteDetails?) = postalVoteDetails?.let {
        PostalVoteDetailsEntity(
            ballotAddress = addressMapper.mapToAddressEntity(it.ballotAddress, SourceSystem.POSTAL),
            ballotAddressReason = it.ballotAddressReason,
            voteForSingleDate = it.voteForSingleDate,
            voteStartDate = it.voteStartDate,
            voteEndDate = it.voteEndDate,
            voteUntilFurtherNotice = it.voteUntilFurtherNotice
        )
    }
}
