package uk.gov.dluhc.emsintegrationapi.database.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.dluhc.emsintegrationapi.testsupport.isValid
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildAddressEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicantDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.getRandomString
import uk.gov.dluhc.emsintegrationapi.testsupport.validateMaxSizeErrorMessage

internal class EntityValidationTest {

    @Nested
    inner class ValidatePostalVoteApplication {
        @Test
        fun `should not throw constraint violation if a PostalVoteApplication object is valid`() {
            assertThat(isValid(buildPostalVoteApplication())).isTrue
        }

        @Test
        fun `should throw constraint violation if a PostalVoteApplication object is not valid`() {
            val invalidPostalVoteApplication = buildPostalVoteApplication(
                postalVoteDetails = buildPostalVoteDetailsEntity(
                    ballotAddress = buildAddressEntity(
                        street = getRandomString(256)
                    )
                ),
                applicantDetails = buildApplicantDetailsEntity(
                    firstName = getRandomString(36),
                ),
                applicationDetails = buildApplicationDetailsEntity(
                    gssCode = getRandomString(10),
                )
            )
            validateMaxSizeErrorMessage(
                invalidPostalVoteApplication,
                listOf(
                    Pair("postalVoteDetails.ballotAddress.street", 255),
                    Pair("applicantDetails.firstName", 35),
                    Pair("applicationDetails.gssCode", 9),
                )
            )
        }
    }

    @Nested
    inner class ValidateProxyVoteApplication {
        @Test
        fun `should not throw constraint violation if a ProxyVoteApplication object is valid`() {
            assertThat(isValid(buildProxyVoteDetailsEntity())).isTrue
        }

        @Test
        fun `should throw constraint violation if a ProxyVoteApplication object is not valid`() {
            val invalidProxyVoteApplication = buildProxyVoteApplication(
                proxyVoteDetails = buildProxyVoteDetailsEntity(
                    proxyAddress = buildAddressEntity(
                        street = getRandomString(256)
                    )
                ),
                applicantDetails = buildApplicantDetailsEntity(
                    firstName = getRandomString(36),
                ),
                applicationDetails = buildApplicationDetailsEntity(
                    gssCode = getRandomString(10),
                )
            )
            validateMaxSizeErrorMessage(
                invalidProxyVoteApplication,
                listOf(
                    Pair("proxyVoteDetails.proxyAddress.street", 255),
                    Pair("applicantDetails.firstName", 35),
                    Pair("applicationDetails.gssCode", 9),
                )
            )
        }
    }

    @Nested
    inner class ValidateEmbeddedEntities {
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
                language = ApplicantDetails.Language.CY,
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
                    Pair("emsElectorId", 255),
                )
            )
        }

        @Test
        fun `should throw constraint violations if an approval details object is invalid`() {
            val applicationDetails = buildApplicationDetailsEntity(
                gssCode = getRandomString(10),
                authorisingStaffId = getRandomString(256),
                source = getRandomString(51)
            )
            validateMaxSizeErrorMessage(
                applicationDetails,
                listOf(
                    Pair("gssCode", 9),
                    Pair("authorisingStaffId", 255),
                    Pair("source", 50),
                )
            )
        }
    }
}
