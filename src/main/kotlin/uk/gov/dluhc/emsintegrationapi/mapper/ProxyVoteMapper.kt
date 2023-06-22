package uk.gov.dluhc.emsintegrationapi.mapper

import org.apache.commons.codec.binary.Base64
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.models.ApplicationLanguage
import uk.gov.dluhc.emsintegrationapi.models.ApplicationStatus
import uk.gov.dluhc.emsintegrationapi.models.ProxyVote
import uk.gov.dluhc.emsintegrationapi.models.ProxyVoteDetail

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
                    regproperty = registeredAddress.property,
                    regstreet = registeredAddress.street,
                    regpostcode = registeredAddress.postcode,
                    regarea = registeredAddress.area,
                    regtown = registeredAddress.town,
                    reglocality = registeredAddress.locality,
                    reguprn = registeredAddress.uprn,
                    proxyfn = proxyVoteDetails.proxyFirstName,
                    proxyln = proxyVoteDetails.proxySurname,
                    proxymn = proxyVoteDetails.proxyMiddleNames,
                    proxyemail = proxyVoteDetails.proxyEmail,
                    proxyphone = proxyVoteDetails.proxyPhone,
                    proxyreason = proxyVoteDetails.proxyReason,
                    proxyproperty = proxyAddress?.property,
                    proxystreet = proxyAddress?.street,
                    proxypostcode = proxyAddress?.postcode,
                    proxyarea = proxyAddress?.area,
                    proxytown = proxyAddress?.town,
                    proxylocality = proxyAddress?.locality,
                    proxyuprn = proxyAddress?.uprn,
                    proxyfamilyrelationship = proxyVoteDetails.proxyFamilyRelationship,
                    proxyVoteUntilFurtherNotice = proxyVoteDetails.voteUntilFurtherNotice,
                    proxyVoteForSingleDate = proxyVoteDetails.voteForSingleDate,
                    proxyVoteStartDate = proxyVoteDetails.voteStartDate,
                    proxyVoteEndDate = proxyVoteDetails.voteEndDate,
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
