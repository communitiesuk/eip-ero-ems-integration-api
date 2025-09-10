package uk.gov.dluhc.emsintegrationapi.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import uk.gov.dluhc.emsintegrationapi.client.EroIdNotFoundException
import uk.gov.dluhc.emsintegrationapi.client.IerGeneralException
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus.PENDING
import uk.gov.dluhc.emsintegrationapi.database.repository.RegisterCheckRepository
import uk.gov.dluhc.emsintegrationapi.mapper.AdminPendingRegisterCheckMapper
import uk.gov.dluhc.emsintegrationapi.testsupport.getRandomGssCode
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.dto.buildAdminPendingRegisterCheckDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity.buildRegisterCheck

@ExtendWith(MockitoExtension::class)
internal class AdminServiceTest {

    @Mock
    private lateinit var retrieveGssCodeService: RetrieveGssCodeService

    @Mock
    private lateinit var registerCheckRepository: RegisterCheckRepository

    @Mock
    private lateinit var adminPendingRegisterCheckMapper: AdminPendingRegisterCheckMapper

    @InjectMocks
    private lateinit var adminService: AdminService

    @Nested
    inner class GetPendingRegisterChecks {

        @Test
        fun `should get empty pending register check records for a given ERO ID given IER returns valid values`() {
            // Given
            val eroId = "south-testington"
            val gssCodeFromEroApi = getRandomGssCode()

            given(retrieveGssCodeService.getGssCodesFromEroId(eq(eroId))).willReturn(listOf(gssCodeFromEroApi))
            given(
                registerCheckRepository.adminFindPendingEntriesByGssCodes(
                    eq(listOf(gssCodeFromEroApi)),
                    any()
                )
            ).willReturn(emptyList())

            // When
            val actualPendingRegisterChecks = adminService.adminGetPendingRegisterChecks(eroId)

            // Then
            assertThat(actualPendingRegisterChecks).isNotNull
            assertThat(actualPendingRegisterChecks).isEmpty()
            verify(retrieveGssCodeService).getGssCodesFromEroId(eroId)
            verify(registerCheckRepository).adminFindPendingEntriesByGssCodes(listOf(gssCodeFromEroApi))
            verifyNoInteractions(adminPendingRegisterCheckMapper)
        }

        @Test
        fun `should get one pending register check record for a given ERO ID given IER returns valid values`() {
            // Given
            val eroId = "south-testington"
            val gssCodeFromEroApi = getRandomGssCode()
            val expectedRecordCount = 1

            val matchedRegisterCheckEntity = buildRegisterCheck(
                gssCode = gssCodeFromEroApi,
                status = PENDING
            )
            val expectedRegisterCheckDto = buildAdminPendingRegisterCheckDto(
                sourceReference = matchedRegisterCheckEntity.sourceReference,
                gssCode = gssCodeFromEroApi
            )

            given(retrieveGssCodeService.getGssCodesFromEroId(eq(eroId))).willReturn(listOf(gssCodeFromEroApi))
            given(
                registerCheckRepository.adminFindPendingEntriesByGssCodes(
                    eq(listOf(gssCodeFromEroApi)),
                    any()
                )
            ).willReturn(listOf(matchedRegisterCheckEntity))
            given(
                adminPendingRegisterCheckMapper.registerCheckEntityToAdminPendingRegisterCheckDto(
                    matchedRegisterCheckEntity
                )
            ).willReturn(expectedRegisterCheckDto)

            val expectedPendingRegisterChecks = listOf(expectedRegisterCheckDto)

            // When
            val actualPendingRegisterChecks = adminService.adminGetPendingRegisterChecks(eroId)

            // Then
            assertThat(actualPendingRegisterChecks).hasSize(expectedRecordCount)
            assertThat(actualPendingRegisterChecks)
                .isEqualTo(expectedPendingRegisterChecks)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
            verify(retrieveGssCodeService).getGssCodesFromEroId(eroId)
            verify(registerCheckRepository).adminFindPendingEntriesByGssCodes(listOf(gssCodeFromEroApi))
            verify(adminPendingRegisterCheckMapper).registerCheckEntityToAdminPendingRegisterCheckDto(
                matchedRegisterCheckEntity
            )
            verifyNoMoreInteractions(registerCheckRepository, adminPendingRegisterCheckMapper)
        }

        @Test
        fun `should get multiple pending register check records for a given ERO ID`() {
            // Given
            val eroId = "south-testington"
            val firstGssCodeFromEroApi = "E1234561"
            val secondGssCodeFromEroApi = "E567892"
            val anotherGssCodeFromEroApi = "E9876543" // No records will be matched for this gssCode
            val expectedRecordCount = 3

            val firstRegisterCheckEntity = buildRegisterCheck(
                gssCode = firstGssCodeFromEroApi,
                status = PENDING
            )
            val secondRegisterCheckEntity = buildRegisterCheck(
                gssCode = firstGssCodeFromEroApi,
                status = PENDING
            )
            val thirdRegisterCheckEntity = buildRegisterCheck(
                gssCode = secondGssCodeFromEroApi,
                status = PENDING
            )

            val firstRegisterCheckDto = buildAdminPendingRegisterCheckDto(
                gssCode = firstRegisterCheckEntity.gssCode,
                sourceReference = firstRegisterCheckEntity.sourceReference
            )
            val secondRegisterCheckDto = buildAdminPendingRegisterCheckDto(
                gssCode = firstGssCodeFromEroApi,
                sourceReference = secondRegisterCheckEntity.sourceReference
            )
            val thirdRegisterCheckDto = buildAdminPendingRegisterCheckDto(
                gssCode = secondGssCodeFromEroApi,
                sourceReference = thirdRegisterCheckEntity.sourceReference
            )

            val expectedGssCodes = listOf(firstGssCodeFromEroApi, secondGssCodeFromEroApi, anotherGssCodeFromEroApi)
            given(retrieveGssCodeService.getGssCodesFromEroId(eq(eroId))).willReturn(expectedGssCodes)
            given(registerCheckRepository.adminFindPendingEntriesByGssCodes(eq(expectedGssCodes), any())).willReturn(
                listOf(firstRegisterCheckEntity, secondRegisterCheckEntity, thirdRegisterCheckEntity)
            )
            given(
                adminPendingRegisterCheckMapper.registerCheckEntityToAdminPendingRegisterCheckDto(
                    eq(
                        firstRegisterCheckEntity
                    )
                )
            ).willReturn(firstRegisterCheckDto)
            given(
                adminPendingRegisterCheckMapper.registerCheckEntityToAdminPendingRegisterCheckDto(
                    eq(
                        secondRegisterCheckEntity
                    )
                )
            ).willReturn(secondRegisterCheckDto)
            given(
                adminPendingRegisterCheckMapper.registerCheckEntityToAdminPendingRegisterCheckDto(
                    eq(
                        thirdRegisterCheckEntity
                    )
                )
            ).willReturn(thirdRegisterCheckDto)

            val expectedPendingRegisterChecks =
                listOf(firstRegisterCheckDto, secondRegisterCheckDto, thirdRegisterCheckDto)

            // When
            val actualPendingRegisterChecks = adminService.adminGetPendingRegisterChecks(eroId)

            // Then
            assertThat(actualPendingRegisterChecks).hasSize(expectedRecordCount)
            assertThat(actualPendingRegisterChecks)
                .isEqualTo(expectedPendingRegisterChecks)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
            verify(retrieveGssCodeService).getGssCodesFromEroId(eroId)
            verify(registerCheckRepository).adminFindPendingEntriesByGssCodes(
                listOf(
                    firstGssCodeFromEroApi,
                    secondGssCodeFromEroApi,
                    anotherGssCodeFromEroApi
                )
            )
            verify(adminPendingRegisterCheckMapper).registerCheckEntityToAdminPendingRegisterCheckDto(
                firstRegisterCheckEntity
            )
            verify(adminPendingRegisterCheckMapper).registerCheckEntityToAdminPendingRegisterCheckDto(
                secondRegisterCheckEntity
            )
            verify(adminPendingRegisterCheckMapper).registerCheckEntityToAdminPendingRegisterCheckDto(
                thirdRegisterCheckEntity
            )
            verifyNoMoreInteractions(registerCheckRepository, adminPendingRegisterCheckMapper)
        }

        @Test
        fun `should throw IER not found exception given IER API client throws IER not found exception`() {
            // Given
            val eroId = "south-testington"

            val expected = EroIdNotFoundException(eroId)
            given(retrieveGssCodeService.getGssCodesFromEroId(eq(eroId))).willThrow(expected)

            // When
            val ex = catchThrowableOfType(EroIdNotFoundException::class.java) {
                adminService.adminGetPendingRegisterChecks(eroId)
            }

            // Then
            assertThat(ex).isEqualTo(expected)
            verify(retrieveGssCodeService).getGssCodesFromEroId(eroId)
            verifyNoInteractions(registerCheckRepository, adminPendingRegisterCheckMapper)
        }

        @Test
        fun `should throw general IER exception given IER API client throws general exception`() {
            // Given
            val eroId = "south-testington"

            val expected = IerGeneralException("Error retrieving EROs from IER API")
            given(retrieveGssCodeService.getGssCodesFromEroId(eq(eroId))).willThrow(expected)

            // When
            val ex = catchThrowableOfType(IerGeneralException::class.java) {
                adminService.adminGetPendingRegisterChecks(eroId)
            }

            // Then
            assertThat(ex).isEqualTo(expected)
            verify(retrieveGssCodeService).getGssCodesFromEroId(eroId)
            verifyNoInteractions(registerCheckRepository, adminPendingRegisterCheckMapper)
        }
    }
}
