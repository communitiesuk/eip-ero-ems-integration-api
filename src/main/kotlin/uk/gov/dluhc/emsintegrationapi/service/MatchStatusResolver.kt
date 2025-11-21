package uk.gov.dluhc.emsintegrationapi.service

import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.database.entity.Address
import uk.gov.dluhc.emsintegrationapi.database.entity.PersonalDetail
import uk.gov.dluhc.emsintegrationapi.database.entity.RegisterCheck
import uk.gov.dluhc.emsintegrationapi.dto.AddressDto
import uk.gov.dluhc.emsintegrationapi.dto.PersonalDetailDto
import uk.gov.dluhc.emsintegrationapi.dto.RegisterCheckMatchDto
import uk.gov.dluhc.emsintegrationapi.dto.RegisterCheckResultDto
import uk.gov.dluhc.emsintegrationapi.dto.RegisterCheckStatus
import java.time.LocalDate

/**
 * Determines the status for a match result by checking if it is pending/not started or expired. Also checks if
 * specific personal details (e.g. surname, postcode) that are provided by the EMS match those on our system for the
 * application concerned.
 *
 * ⚠️ The logic and standardisation used here to determine if a match is partial or exact is also used for application
 * linking in the applications API, and it's important that these two implementations are kept in sync. If making changes
 * here, then changes should also be made as appropriate to `ElectorDetailsStandardisationUtils` and `StandardisedElectorDetailsRepository`
 * in the applications API. This would also require back populating the saved standardised details and recalculating links,
 * so should be very carefully considered.
 */
@Component
class MatchStatusResolver {

    fun resolveStatus(
        registerCheckResultDto: RegisterCheckResultDto,
        registerCheckEntity: RegisterCheck
    ): RegisterCheckStatus =
        when (registerCheckResultDto.matchCount) {
            0 -> RegisterCheckStatus.NO_MATCH
            1 -> evaluateRegisterCheckStatusWithOneMatch(
                registerCheckResultDto.registerCheckMatches!!.first(),
                registerCheckEntity.personalDetail,
                registerCheckEntity.historicalSearch == true,
            )

            in 2..10 -> RegisterCheckStatus.MULTIPLE_MATCH
            else -> RegisterCheckStatus.TOO_MANY_MATCHES
        }

    private fun evaluateRegisterCheckStatusWithOneMatch(
        registerCheckMatchDto: RegisterCheckMatchDto,
        personalDetailEntity: PersonalDetail,
        isHistoricalSearch: Boolean = false,
    ): RegisterCheckStatus =
        with(registerCheckMatchDto) {
            return if (franchiseCode.uppercase().trim() == "PENDING") {
                RegisterCheckStatus.PENDING_DETERMINATION
            } else {
                val now = LocalDate.now()
                if (registeredStartDate?.isAfter(now) == true) {
                    RegisterCheckStatus.NOT_STARTED
                } else if (!isHistoricalSearch && registeredEndDate?.isBefore(now) == true) {
                    RegisterCheckStatus.EXPIRED
                } else if (isPartialMatch(registerCheckMatchDto.personalDetail, personalDetailEntity)) {
                    RegisterCheckStatus.PARTIAL_MATCH
                } else {
                    RegisterCheckStatus.EXACT_MATCH
                }
            }
        }

    private fun isPartialMatch(personalDetailDto: PersonalDetailDto, personalDetailEntity: PersonalDetail): Boolean =
        !keyPersonalDetailsMatch(personalDetailDto, personalDetailEntity) ||
            !keyAddressDetailsMatch(personalDetailDto.address, personalDetailEntity.address)

    private fun keyPersonalDetailsMatch(
        personalDetailDto: PersonalDetailDto,
        personalDetailEntity: PersonalDetail
    ): Boolean =
        personalDetailDto.firstName.standardised() == personalDetailEntity.firstName.standardised() &&
            getStandardisedSurname(personalDetailDto.surname) == getStandardisedSurname(personalDetailEntity.surname) &&
            personalDetailDto.dateOfBirth == personalDetailEntity.dateOfBirth

    private fun keyAddressDetailsMatch(addressDto: AddressDto, addressEntity: Address): Boolean =
        if (addressDto.uprn != null && getStandardisedUprn(addressDto.uprn) == getStandardisedUprn(addressEntity.uprn)) {
            true
        } else {
            addressDto.property?.standardised() == addressEntity.property?.standardised() &&
                addressDto.street.standardised() == addressEntity.street.standardised() &&
                getStandardisedPostcode(addressDto.postcode) == getStandardisedPostcode(addressEntity.postcode)
        }

    private fun getStandardisedSurname(surname: String) =
        surname
            .replace(Regex("-"), " ")
            .replace(Regex("'"), "")
            .standardised()

    private fun getStandardisedPostcode(postcode: String) =
        postcode
            .replace(Regex("\\s+"), "")
            .standardised()

    private fun getStandardisedUprn(uprn: String?) = uprn?.trim()?.trimStart('0')

    private fun String.standardised() =
        replace(Regex("\\s+"), " ")
            .uppercase()
            .trim()
}
