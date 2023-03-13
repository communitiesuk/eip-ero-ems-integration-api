package uk.gov.dluhc.emsintegrationapi.database.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.testsupport.isValid
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildAddressEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicantDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApprovalDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.getRandomString
import uk.gov.dluhc.emsintegrationapi.testsupport.validateMaxSizeErrorMessage

internal class EntityValidationTest {
    @Nested
    inner class ValidatePostalVoteApplication {
        @Test
        fun `should not throw constraint violation if a PostalVoteApplication object is valid`() {
            assertThat(isValid(buildPostalApplication())).isTrue
        }

        @Test
        fun `should throw constraint violation if a PostalVoteApplication object is not valid`() {
            assertThat(
                isValid(
                    buildPostalApplication(
                        postalVoteDetails = buildPostalVoteDetailsEntity(
                            ballotAddress = buildAddressEntity(
                                street = getRandomString(256)

                            )
                        )
                    )
                )
            ).isFalse
        }

        @Test
        fun `should throw constraint violations if an address object is invalid`() {
            val address = buildAddressEntity(
                street = getRandomString(256),
                locality = getRandomString(256),
                town = getRandomString(256),
                area = getRandomString(256),
                postcode = getRandomString(11),
                uprn = getRandomString(13),
            )

            validateMaxSizeErrorMessage(
                address,
                listOf(
                    Pair("street", 255),
                    Pair("locality", 255),
                    Pair("town", 255),
                    Pair("area", 255),
                    Pair("postcode", 10),
                    Pair("uprn", 12),
                )
            )
        }

        @Test
        fun `should throw constraint violation if an applicant object is invalid`() {

            val applicantDetails = buildApplicantDetailsEntity(
                firstName = getRandomString(36),
                surname = getRandomString(36),
                middleNames = getRandomString(101),
                email = getRandomString(256),
                phone = getRandomString(51),
                referenceNumber = getRandomString(11),
                ipAddress = getRandomString(46),
                language = getRandomString(3),
                emsElectorId = getRandomString(256),
            )

            validateMaxSizeErrorMessage(
                applicantDetails,
                listOf(
                    Pair("firstName", 35),
                    Pair("surname", 35),
                    Pair("middleNames", 100),
                    Pair("email", 255),
                    Pair("phone", 50),
                    Pair("referenceNumber", 10),
                    Pair("ipAddress", 45),
                    Pair("language", 2),
                    Pair("emsElectorId", 255),
                )
            )
        }

        @Test
        fun `should throw constraint violations if an approval details object is invalid`() {
            val approvalDetails = buildApprovalDetailsEntity(
                gssCode = getRandomString(10),
                authorisingStaffId = getRandomString(256),
                source = getRandomString(51)
            )
            validateMaxSizeErrorMessage(
                approvalDetails,
                listOf(
                    Pair("gssCode", 9),
                    Pair("authorisingStaffId", 255),
                    Pair("source", 50),
                )
            )
        }
    }
}
