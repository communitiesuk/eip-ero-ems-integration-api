package uk.gov.dluhc.emsintegrationapi.mapper

import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
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
                    proxyfn = proxyVoteDetails.proxyFirstName,
                    proxyln = proxyVoteDetails.proxySurname,
                    proxymn = proxyVoteDetails.proxyMiddleNames,
                    proxyemail = proxyVoteDetails.proxyEmail,
                    proxyphone = proxyVoteDetails.proxyPhone,
                    proxyreason = proxyVoteDetails.proxyReason,
                    proxyproperty = proxyAddress.property,
                    proxystreet = proxyAddress.street,
                    proxypostcode = proxyAddress.postcode,
                    proxyarea = proxyAddress.area,
                    proxytown = proxyAddress.town,
                    proxylocality = proxyAddress.locality,
                    proxyuprn = proxyAddress.uprn,
                    proxyfamilyrelationship = proxyVoteDetails.proxyFamilyRelationship,
                    proxyVoteUntilFurtherNotice = proxyVoteDetails.voteUntilFurtherNotice,
                    proxyVoteForSingleDate = proxyVoteDetails.voteForSingleDate,
                    proxyVoteStartDate = proxyVoteDetails.voteStartDate,
                    proxyVoteEndDate = proxyVoteDetails.voteEndDate,
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
