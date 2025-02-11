package uk.gov.dluhc.emsintegrationapi.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus
import uk.gov.dluhc.emsintegrationapi.database.repository.RegisterCheckRepository
import uk.gov.dluhc.emsintegrationapi.exception.OptimisticLockingFailureException
import uk.gov.dluhc.emsintegrationapi.exception.PendingRegisterCheckArchiveInvalidStatusException
import uk.gov.dluhc.emsintegrationapi.exception.PendingRegisterCheckNotFoundException
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity.buildRegisterCheck
import java.util.UUID

@ExtendWith(MockitoExtension::class)
internal class PendingPendingRegisterCheckArchiveServiceTest {
    @Mock
    private lateinit var registerCheckRepository: RegisterCheckRepository

    @InjectMocks
    private lateinit var pendingRegisterCheckArchiveService: PendingRegisterCheckArchiveService

    @Nested
    inner class ArchiveRegisterCheckData {
        @Test
        fun `should handle null correlation ID`() {
            val ex =
                catchThrowableOfType(
                    { pendingRegisterCheckArchiveService.archiveIfStatusIsPending(null) },
                    IllegalArgumentException::class.java,
                )
            assertThat(ex.message).isEqualTo("Correlation ID is null")
        }

        @Test
        fun `should not archive any records for a non-existing register check`() {
            // Given
            val correlationId = UUID.randomUUID()
            val expected = PendingRegisterCheckNotFoundException(correlationId)

            // When
            val ex =
                catchThrowableOfType(
                    { pendingRegisterCheckArchiveService.archiveIfStatusIsPending(correlationId) },
                    PendingRegisterCheckNotFoundException::class.java,
                )

            // Then
            assertThat(ex).message().isEqualTo(expected.message)
        }

        @Test
        fun `should not archive any records for an already archived register check`() {
            // Given
            val correlationId = UUID.randomUUID()
            val registerCheck = buildRegisterCheck(correlationId = correlationId, status = CheckStatus.ARCHIVED)
            given(registerCheckRepository.findByCorrelationId(correlationId)).willReturn(registerCheck)
            val expected = PendingRegisterCheckArchiveInvalidStatusException(CheckStatus.ARCHIVED)

            // When
            val ex =
                catchThrowableOfType(
                    { pendingRegisterCheckArchiveService.archiveIfStatusIsPending(correlationId) },
                    PendingRegisterCheckArchiveInvalidStatusException::class.java,
                )

            // Then
            verify(registerCheckRepository).findByCorrelationId(correlationId)
            verify(registerCheckRepository, never()).save(registerCheck)
            assertThat(ex).message().isEqualTo(expected.message)
        }

        @Test
        fun `should archive record for a pending register check`() {
            // Given
            val correlationId = UUID.randomUUID()
            val registerCheck = buildRegisterCheck(correlationId = correlationId, status = CheckStatus.PENDING)
            given(registerCheckRepository.findByCorrelationId(correlationId)).willReturn(registerCheck)
            val expected = PendingRegisterCheckArchiveInvalidStatusException(CheckStatus.ARCHIVED)

            // When
            pendingRegisterCheckArchiveService.archiveIfStatusIsPending(correlationId)

            // Then
            verify(registerCheckRepository).findByCorrelationId(correlationId)
            verify(registerCheckRepository).save(registerCheck)
        }

        @Test
        fun `should handle optimistic locking exception encountered on register check repository save operation`() {
            // Given
            val correlationId = UUID.randomUUID()
            val registerCheck = buildRegisterCheck(correlationId = correlationId, status = CheckStatus.PENDING)
            given(registerCheckRepository.findByCorrelationId(correlationId)).willReturn(registerCheck)
            given(registerCheckRepository.save(any())).willThrow(OptimisticLockingFailureException(correlationId = correlationId))
            val ex =
                catchThrowableOfType(
                    { pendingRegisterCheckArchiveService.archiveIfStatusIsPending(correlationId) },
                    OptimisticLockingFailureException::class.java,
                )
            assertThat(ex.message).isEqualTo("Register check with requestid:[$correlationId] has an optimistic locking failure")
            verify(registerCheckRepository).findByCorrelationId(correlationId)
            // save call was made although it threw exception
            verify(registerCheckRepository).save(registerCheck)
        }
    }
}
