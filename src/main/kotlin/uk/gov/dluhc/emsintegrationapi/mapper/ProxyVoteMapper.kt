package uk.gov.dluhc.emsintegrationapi.mapper

import org.apache.commons.codec.binary.Base64
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.models.Address
import uk.gov.dluhc.emsintegrationapi.models.ApplicationLanguage
import uk.gov.dluhc.emsintegrationapi.models.ApplicationStatus
import uk.gov.dluhc.emsintegrationapi.models.ProxyVote
import uk.gov.dluhc.emsintegrationapi.models.ProxyVoteDetail
import uk.gov.dluhc.emsintegrationapi.models.RejectedReason
import uk.gov.dluhc.emsintegrationapi.models.RejectedReasonItem
import uk.gov.dluhc.emsintegrationapi.models.RejectedReasons
import uk.gov.dluhc.emsintegrationapi.database.entity.Address as AddressEntity
import uk.gov.dluhc.emsintegrationapi.database.entity.RejectedReasonItem as RejectedReasonItemEntity
import uk.gov.dluhc.emsintegrationapi.database.entity.Type as TypeEntity

@Component
class ProxyVoteMapper(private val instantMapper: InstantMapper) {
    fun mapFromEntities(proxyVoteApplications: List<ProxyVoteApplication>) =
        proxyVoteApplications.map { entity -> mapFromEntity(entity) }

    fun mapFromEntity(proxyVoteApplications: ProxyVoteApplication): ProxyVote {
        return with(proxyVoteApplications) {
            val registeredAddress = applicantDetails.registeredAddress
            val proxyAddress = proxyVoteDetails.proxyAddress
            ProxyVote(
                detail = ProxyVoteDetail(
                    refNum = applicantDetails.referenceNumber,
                    ip = applicantDetails.ipAddress,
                    lang = ApplicationLanguage.valueOf(applicantDetails.language.name),
                    emsElectorId = applicantDetails.emsElectorId,
                    fn = applicantDetails.firstName,
                    ln = applicantDetails.surname,
                    mn = applicantDetails.middleNames,
                    dob = applicantDetails.dob,
                    phone = applicantDetails.phone,
                    email = applicantDetails.email,
                    signature = Base64.decodeBase64(applicationDetails.signatureBase64),
                    regstreet = registeredAddress.street,
                    regpostcode = registeredAddress.postcode,
                    registeredAddress = mapFromAddressEntity(applicantDetails.registeredAddress),
                    proxyfn = proxyVoteDetails.proxyFirstName,
                    proxyln = proxyVoteDetails.proxySurname,
                    proxymn = proxyVoteDetails.proxyMiddleNames,
                    proxyemail = proxyVoteDetails.proxyEmail,
                    proxyphone = proxyVoteDetails.proxyPhone,
                    proxyreason = proxyVoteDetails.proxyReason,
                    proxyproperty = proxyAddress?.property,
                    proxystreet = proxyAddress?.street,
                    proxyAddress = proxyVoteDetails.proxyAddress?.let { mapFromAddressEntity(it) },
                    proxyfamilyrelationship = proxyVoteDetails.proxyFamilyRelationship,
                    proxyVoteUntilFurtherNotice = proxyVoteDetails.voteUntilFurtherNotice,
                    proxyVoteForSingleDate = proxyVoteDetails.voteForSingleDate,
                    proxyVoteStartDate = proxyVoteDetails.voteStartDate,
                    proxyVoteEndDate = proxyVoteDetails.voteEndDate,
                    applicationStatus = ApplicationStatus.valueOf(applicationDetails.applicationStatus.name),
                    signatureWaived = applicationDetails.signatureWaived,
                    signatureWaivedReason = applicationDetails.signatureWaivedReason,
                    rejectedReasons = mapFromRejectedReasonEntity(this),
                ),
                id = applicationId,
                createdAt = instantMapper.toOffsetDateTime(applicationDetails.createdAt),
                gssCode = applicationDetails.gssCode,
                source = applicationDetails.source,
                authorisedAt = instantMapper.toOffsetDateTime(applicationDetails.authorisedAt),
                authorisingStaffId = applicationDetails.authorisingStaffId,
            )
        }
    }

    fun mapFromAddressEntity(addressEntity: AddressEntity) = addressEntity.let {
        Address(
            property = it.property,
            street = it.street,
            postcode = it.postcode,
            area = it.area,
            town = it.town,
            locality = it.locality,
            uprn = it.uprn
        )
    }

    fun mapFromRejectedReasonEntity(proxyVoteApplication: ProxyVoteApplication): RejectedReasons {
        return with(proxyVoteApplication) {
            RejectedReasons(
                englishReason = RejectedReason(
                    notes = englishRejectionNotes,
                    reasons = englishRejectedReasonItems?.toList()?.map { it.type.value },
                    reasonList = englishRejectedReasonItems?.toList()?.map { item -> mapRejectedReasonItemFromEntity(item) }
                ),
                welshReason = RejectedReason(
                    notes = welshRejectionNotes,
                    reasons = welshRejectedReasonItems?.toList()?.map { it.type.value },
                    reasonList = welshRejectedReasonItems?.toList()?.map { item -> mapRejectedReasonItemFromEntity(item) }
                )
            )
        }
    }

    fun mapRejectedReasonItemFromEntity(rejectedReasonItem: RejectedReasonItemEntity): RejectedReasonItem {
        return with(rejectedReasonItem) {
            RejectedReasonItem(
                electorReason = electorReason,
                type = when (type) {
                    TypeEntity.IDENTITY_NOT_CONFIRMED -> RejectedReasonItem.Type.IDENTITY_MINUS_NOT_MINUS_CONFIRMED
                    TypeEntity.SIGNATURE_IS_NOT_ACCEPTABLE -> RejectedReasonItem.Type.SIGNATURE_MINUS_IS_MINUS_NOT_MINUS_ACCEPTABLE
                    TypeEntity.DOB_NOT_PROVIDED -> RejectedReasonItem.Type.DOB_MINUS_NOT_MINUS_PROVIDED
                    TypeEntity.FRAUDULENT_APPLICATION -> RejectedReasonItem.Type.FRAUDULENT_MINUS_APPLICATION
                    TypeEntity.NOT_REGISTERED_TO_VOTE -> RejectedReasonItem.Type.NOT_MINUS_REGISTERED_MINUS_TO_MINUS_VOTE
                    TypeEntity.NOT_ELIGIBLE_FOR_RESERVED_POLLS -> RejectedReasonItem.Type.NOT_MINUS_ELIGIBLE_MINUS_FOR_MINUS_RESERVED_MINUS_POLLS
                    TypeEntity.INCOMPLETE_APPLICATION -> RejectedReasonItem.Type.INCOMPLETE_MINUS_APPLICATION
                    TypeEntity.PROXY_LIMITS -> RejectedReasonItem.Type.PROXY_MINUS_LIMITS
                    TypeEntity.PROXY_NOT_REGISTERED_TO_VOTE -> RejectedReasonItem.Type.PROXY_MINUS_NOT_MINUS_REGISTERED_MINUS_TO_MINUS_VOTE
                    TypeEntity.OTHER_REJECT_REASON -> RejectedReasonItem.Type.OTHER_MINUS_REJECT_MINUS_REASON
                },
                includeInComms = includeInComms
            )
        }
    }
}
