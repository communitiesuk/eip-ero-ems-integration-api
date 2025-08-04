package uk.gov.dluhc.emsintegrationapi.service

import org.apache.commons.lang3.Strings
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
            return if (Strings.CI.equals(franchiseCode.trim(), "PENDING")) {
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
        trimAndEqualsIgnoreCase(personalDetailDto.firstName, personalDetailEntity.firstName) &&
            Strings.CS.equals(
                sanitizeSurname(personalDetailDto.surname),
                sanitizeSurname(personalDetailEntity.surname)
            ) &&
            personalDetailDto.dateOfBirth == personalDetailEntity.dateOfBirth

    private fun keyAddressDetailsMatch(addressDto: AddressDto, addressEntity: Address): Boolean =
        if (addressDto.uprn != null && uprnsAreEqual(addressDto.uprn, addressEntity.uprn)) {
            true
        } else {
            trimAndEqualsIgnoreCase(addressDto.property, addressEntity.property) &&
                trimAndEqualsIgnoreCase(addressDto.street, addressEntity.street) &&
                Strings.CS.equals(sanitizePostcode(addressDto.postcode), sanitizePostcode(addressEntity.postcode))
        }

    private fun trimAndEqualsIgnoreCase(str1: String?, str2: String?) =
        Strings.CI.equals(str1?.trim(), str2?.trim())

    private fun uprnsAreEqual(uprn1: String?, uprn2: String?) =
        Strings.CI.equals(uprn1?.trim()?.trimStart('0'), uprn2?.trim()?.trimStart('0'))

    private fun sanitizeSurname(surname: String): String =
        surname.uppercase()
            .replace(Regex("-"), " ")
            .replace(Regex("'"), "")
            .replace(Regex(" {2,}"), " ")
            .trim()

    private fun sanitizePostcode(postcode: String): String =
        postcode.uppercase().replace(Regex(" +"), "").trim()
}
