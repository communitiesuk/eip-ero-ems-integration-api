package uk.gov.dluhc.emsintegrationapi.mapper

import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.models.PostalVote
import uk.gov.dluhc.emsintegrationapi.models.PostalVoteDetail

@Component
class PostalVoteMapper(private val instantMapper: InstantMapper) {
    fun mapFromEntities(postalApplications: List<PostalVoteApplication>) =
        postalApplications.map { entity -> mapFromEntity(entity) }

    fun mapFromEntity(postalVoteApplication: PostalVoteApplication): PostalVote {
        return with(postalVoteApplication) {
            val registeredAddress = applicantDetails.registeredAddress
            val ballotAddress = postalVoteDetails?.ballotAddress
            PostalVote(
                detail = PostalVoteDetail(
                    refNum = applicantDetails.referenceNumber,
                    ip = applicantDetails.ipAddress,
                    lang = applicantDetails.language,
                    emsElectorId = applicantDetails.emsElectorId,
                    fn = applicantDetails.firstName,
                    ln = applicantDetails.surname,
                    mn = applicantDetails.middleNames,
                    dob = applicantDetails.dob,
                    phone = applicantDetails.phone,
                    email = applicantDetails.email,
                    signature = signatureBase64,
                    regproperty = registeredAddress.property,
                    regstreet = registeredAddress.street,
                    regpostcode = registeredAddress.postcode,
                    regarea = registeredAddress.area,
                    regtown = registeredAddress.town,
                    reglocality = registeredAddress.locality,
                    reguprn = registeredAddress.uprn,
                    ballotproperty = ballotAddress?.property,
                    ballotstreet = ballotAddress?.street,
                    ballotpostcode = ballotAddress?.postcode,
                    ballotarea = ballotAddress?.area,
                    ballottown = ballotAddress?.town,
                    ballotlocality = ballotAddress?.locality,
                    ballotuprn = ballotAddress?.uprn,
                    postalVoteUntilFurtherNotice = postalVoteDetails?.voteUntilFurtherNotice,
                    postalVoteForSingleDate = postalVoteDetails?.voteForSingleDate,
                    postalVoteStartDate = postalVoteDetails?.voteStartDate,
                    postalVoteEndDate = postalVoteDetails?.voteEndDate,
                    ballotAddressReason = postalVoteDetails?.ballotAddressReason,
                ),
                id = applicationId,
                createdAt = instantMapper.toOffsetDateTime(approvalDetails.createdAt),
                gssCode = approvalDetails.gssCode,
                source = approvalDetails.source,
                authorisedAt = instantMapper.toOffsetDateTime(approvalDetails.authorisedAt),
                authorisingStaffId = approvalDetails.authorisingStaffId,
            )
        }
    }
}
