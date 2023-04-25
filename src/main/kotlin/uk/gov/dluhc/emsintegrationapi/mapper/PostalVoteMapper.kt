package uk.gov.dluhc.emsintegrationapi.mapper

import org.apache.commons.codec.binary.Base64
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.models.ApplicationLanguage
import uk.gov.dluhc.emsintegrationapi.models.ApplicationStatus
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
                    lang = ApplicationLanguage.valueOf(applicantDetails.language.name),
                    emsElectorId = applicantDetails.emsElectorId,
                    fn = applicantDetails.firstName,
                    ln = applicantDetails.surname,
                    mn = applicantDetails.middleNames,
                    dob = applicantDetails.dob,
                    phone = applicantDetails.phone,
                    email = applicantDetails.email,
                    signature = Base64.decodeBase64(applicationDetails.signatureBase64),
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
                    applicationStatus = ApplicationStatus.valueOf(applicationDetails.applicationStatus.name),
                    signatureWaived = applicationDetails.signatureWaived,
                    signatureWaivedReason = applicationDetails.signatureWaivedReason,
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
}
