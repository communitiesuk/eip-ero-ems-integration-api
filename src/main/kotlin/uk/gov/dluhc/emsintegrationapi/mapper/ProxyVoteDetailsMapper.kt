package uk.gov.dluhc.emsintegrationapi.mapper

import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.database.entity.SourceSystem
import uk.gov.dluhc.emsintegrationapi.messaging.models.ProxyVoteDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteDetails as ProxyVoteDetailsEntity

@Component
class ProxyVoteDetailsMapper(private val addressMapper: AddressMapper) {
    fun mapToProxyVoteDetailsEntity(proxyVoteDetails: ProxyVoteDetails) = proxyVoteDetails.let {
        ProxyVoteDetailsEntity(
            proxyFirstName = it.proxyFirstName,
            proxyMiddleNames = it.proxyMiddleNames,
            proxySurname = it.proxySurname,
            proxyEmail = it.proxyEmail,
            proxyPhone = it.proxyPhone,
            proxyReason = it.proxyReason,
            proxyAddress = addressMapper.mapToAddressEntity(it.proxyAddress, SourceSystem.PROXY),
            proxyFamilyRelationship = it.proxyFamilyRelationship,
            voteForSingleDate = it.voteForSingleDate,
            voteStartDate = it.voteStartDate,
            voteEndDate = it.voteEndDate,
            voteUntilFurtherNotice = it.voteUntilFurtherNotice
        )
    }
}
