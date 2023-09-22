package uk.gov.dluhc.emsintegrationapi.mapper

import org.apache.commons.codec.binary.Base64
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.models.Address
import uk.gov.dluhc.emsintegrationapi.models.ApplicationLanguage
import uk.gov.dluhc.emsintegrationapi.models.ApplicationStatus
import uk.gov.dluhc.emsintegrationapi.models.BfpoAddress
import uk.gov.dluhc.emsintegrationapi.models.OverseasAddress
import uk.gov.dluhc.emsintegrationapi.models.PostalVote
import uk.gov.dluhc.emsintegrationapi.models.PostalVoteDetail
import uk.gov.dluhc.emsintegrationapi.models.RejectedReason
import uk.gov.dluhc.emsintegrationapi.models.RejectedReasonItem
import uk.gov.dluhc.emsintegrationapi.models.RejectedReasons
import uk.gov.dluhc.emsintegrationapi.database.entity.Address as AddressEntity
import uk.gov.dluhc.emsintegrationapi.database.entity.BfpoAddress as BfpoPostalAddressEntity
import uk.gov.dluhc.emsintegrationapi.database.entity.OverseasAddress as OverseasPostalAddressEntity

@Component
class PostalVoteMapper(private val instantMapper: InstantMapper) {
    fun mapFromEntities(postalApplications: List<PostalVoteApplication>) =
        postalApplications.map { entity -> mapFromEntity(entity) }

    fun mapFromEntity(postalVoteApplication: PostalVoteApplication): PostalVote {
        return with(postalVoteApplication) {
            val registeredAddress = applicantDetails.registeredAddress
            PostalVote(
                detail = PostalVoteDetail(
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
                    ballotAddress = postalVoteDetails?.ballotAddress?.let { mapFromAddressEntity(it) },
                    ballotBfpoPostalAddress = postalVoteDetails?.ballotBfpoAddress?.let { mapFromBfpoAddressEntity(it) },
                    ballotOverseasPostalAddress = postalVoteDetails?.ballotOverseasAddress?.let { mapFromOverseasAddressEntity(it) },
                    postalVoteUntilFurtherNotice = postalVoteDetails?.voteUntilFurtherNotice,
                    postalVoteForSingleDate = postalVoteDetails?.voteForSingleDate,
                    postalVoteStartDate = postalVoteDetails?.voteStartDate,
                    postalVoteEndDate = postalVoteDetails?.voteEndDate,
                    ballotAddressReason = postalVoteDetails?.ballotAddressReason,
                    applicationStatus = ApplicationStatus.valueOf(applicationDetails.applicationStatus.name),
                    signatureWaived = applicationDetails.signatureWaived,
                    signatureWaivedReason = applicationDetails.signatureWaivedReason,
                    rejectedReasons = mapFromRejectedReasonEntity(this)
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

    fun mapFromAddressEntity(addressEntity: AddressEntity): Address {
        return with(addressEntity) {
            Address(
                property = property,
                street = street,
                postcode = postcode,
                area = area,
                town = town,
                locality = locality,
                uprn = uprn
            )
        }
    }

    fun mapFromBfpoAddressEntity(bfpoPostalAddress: BfpoPostalAddressEntity): BfpoAddress {
        return with(bfpoPostalAddress) {
            BfpoAddress(
                bfpoNumber = bfpoNumber,
                addressLine1 = addressLine1,
                addressLine2 = addressLine2,
                addressLine3 = addressLine3,
                addressLine4 = addressLine4,
                addressLine5 = addressLine5,
            )
        }
    }

    fun mapFromOverseasAddressEntity(overseasPostalAddress: OverseasPostalAddressEntity): OverseasAddress {
        return with(overseasPostalAddress) {
            OverseasAddress(
                addressLine1 = addressLine1,
                addressLine2 = addressLine2,
                addressLine3 = addressLine3,
                addressLine4 = addressLine4,
                addressLine5 = addressLine5,
                country = country
            )
        }
    }

    fun mapFromRejectedReasonEntity(postalVoteApplication: PostalVoteApplication): RejectedReasons {
        return with(postalVoteApplication) {
            RejectedReasons(
                englishReason = RejectedReason(
                    notes = englishRejectionNotes,
                    reasons = englishRejectionReasons?.toList(),
                    reasonList = englishRejectedReasonItems?.toList()?.map { item -> RejectedReasonItem(item.electorReason, item.type, item.includeInComms) }
                ),
                welshReason = RejectedReason(
                    notes = welshRejectionNotes,
                    reasons = welshRejectionReasons?.toList(),
                    reasonList = welshRejectedReasonItems?.toList()?.map { item -> RejectedReasonItem(item.electorReason, item.type, item.includeInComms) }
                )
            )
        }
    }
}
