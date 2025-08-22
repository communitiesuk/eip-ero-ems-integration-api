package uk.gov.dluhc.emsintegrationapi.messaging.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus
import uk.gov.dluhc.emsintegrationapi.mapper.CheckStatusMapper
import uk.gov.dluhc.emsintegrationapi.mapper.InstantMapper
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity.buildPersonalDetailWithOptionalFieldsAsNull
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity.buildRegisterCheck
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity.buildRegisterCheckMatch
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity.buildVotingArrangement
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.messaging.buildRegisterCheckResultMessage
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.messaging.buildVcaRegisterCheckMatch
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.messaging.buildVcaRegisterCheckPersonalDetailSqsFromEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.messaging.buildVcaRegisterCheckVotingArrangementSqsFromEntity
import uk.gov.dluhc.registercheckerapi.messaging.models.RegisterCheckResult
import java.time.Instant
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
internal class RegisterCheckResultMessageMapperTest {

    @Mock
    private lateinit var instantMapper: InstantMapper

    @Mock
    private lateinit var checkStatusMapper: CheckStatusMapper

    @InjectMocks
    private val mapper = RegisterCheckResultMessageMapperImpl()

    @Nested
    inner class FromRegisterCheckEntityToRegisterCheckResultMessage {

        @ParameterizedTest
        @CsvSource(
            value = [
                "EXACT_MATCH, EXACT_MINUS_MATCH",
                "NO_MATCH, NO_MINUS_MATCH",
                "PARTIAL_MATCH, PARTIAL_MINUS_MATCH",
                "PENDING_DETERMINATION, PENDING_MINUS_DETERMINATION",
                "EXPIRED, EXPIRED",
                "NOT_STARTED, NOT_MINUS_STARTED"
            ]
        )
        fun `should map entity to message when one match found`(
            initialStatus: CheckStatus,
            expectedStatus: RegisterCheckResult
        ) {
            // Given
            val historicalSearchEarliestDateInstant = Instant.now()
            val historicalSearchEarliestDateOffset = historicalSearchEarliestDateInstant.atOffset(ZoneOffset.UTC)
            val registerCheckEntity = buildRegisterCheck(
                status = initialStatus,
                registerCheckMatches = mutableListOf(buildRegisterCheckMatch()),
                historicalSearchEarliestDate = historicalSearchEarliestDateInstant,
            )

            given(checkStatusMapper.toRegisterCheckResultEnum(any())).willReturn(expectedStatus)
            given(instantMapper.toOffsetDateTime(any())).willReturn(historicalSearchEarliestDateOffset)

            val expectedMessage = buildRegisterCheckResultMessage(
                sourceReference = registerCheckEntity.sourceReference,
                sourceCorrelationId = registerCheckEntity.sourceCorrelationId,
                registerCheckResult = expectedStatus,
                matches = registerCheckEntity.registerCheckMatches.map { registerCheckMatch ->
                    with(registerCheckMatch) {
                        buildVcaRegisterCheckMatch(
                            personalDetail = buildVcaRegisterCheckPersonalDetailSqsFromEntity(personalDetail),
                            emsElectoralId = emsElectorId,
                            franchiseCode = franchiseCode ?: "",
                            registeredStartDate = registeredStartDate,
                            registeredEndDate = registeredEndDate,
                            postalVotingArrangement = postalVotingArrangement?.let(
                                ::buildVcaRegisterCheckVotingArrangementSqsFromEntity
                            ),
                            proxyVotingArrangement = proxyVotingArrangement?.let(
                                ::buildVcaRegisterCheckVotingArrangementSqsFromEntity
                            ),
                        )
                    }
                },
                historicalSearchEarliestDate = historicalSearchEarliestDateOffset,
            )

            // When
            val actual = mapper.fromRegisterCheckEntityToRegisterCheckResultMessage(registerCheckEntity)

            // Then
            assertThat(actual).usingRecursiveComparison().isEqualTo(expectedMessage)
            verify(checkStatusMapper).toRegisterCheckResultEnum(initialStatus)
            verify(instantMapper).toOffsetDateTime(historicalSearchEarliestDateInstant)
        }

        @Test
        fun `should map entity to message when no match`() {
            // Given
            val historicalSearchEarliestDateInstant = Instant.now()
            val historicalSearchEarliestDateOffset = historicalSearchEarliestDateInstant.atOffset(ZoneOffset.UTC)
            val registerCheck = buildRegisterCheck(
                status = CheckStatus.NO_MATCH,
                registerCheckMatches = mutableListOf(),
                historicalSearchEarliestDate = historicalSearchEarliestDateInstant,
            )

            given(checkStatusMapper.toRegisterCheckResultEnum(any())).willReturn(RegisterCheckResult.NO_MINUS_MATCH)
            given(instantMapper.toOffsetDateTime(any())).willReturn(historicalSearchEarliestDateOffset)

            val expected = buildRegisterCheckResultMessage(
                sourceReference = registerCheck.sourceReference,
                sourceCorrelationId = registerCheck.sourceCorrelationId,
                registerCheckResult = RegisterCheckResult.NO_MINUS_MATCH,
                matches = emptyList(),
                historicalSearchEarliestDate = historicalSearchEarliestDateOffset,
            )

            // When
            val actual = mapper.fromRegisterCheckEntityToRegisterCheckResultMessage(registerCheck)

            // Then
            assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
            assertThat(actual.matches).isNotNull
            assertThat(actual.matches).isEmpty()
            verify(checkStatusMapper).toRegisterCheckResultEnum(CheckStatus.NO_MATCH)
            verify(instantMapper).toOffsetDateTime(historicalSearchEarliestDateInstant)
        }

        @Test
        fun `should map entity to message when optional fields are null`() {
            // Given
            val registerCheck = buildRegisterCheck(
                status = CheckStatus.MULTIPLE_MATCH,
                registerCheckMatches = mutableListOf(
                    buildRegisterCheckMatch(personalDetail = buildPersonalDetailWithOptionalFieldsAsNull(), postalVotingArrangement = buildVotingArrangement()),
                    buildRegisterCheckMatch(personalDetail = buildPersonalDetailWithOptionalFieldsAsNull(), proxyVotingArrangement = buildVotingArrangement()),
                ),
                historicalSearchEarliestDate = null,
            )

            given(checkStatusMapper.toRegisterCheckResultEnum(any())).willReturn(RegisterCheckResult.MULTIPLE_MINUS_MATCH)

            val expected = buildRegisterCheckResultMessage(
                sourceReference = registerCheck.sourceReference,
                sourceCorrelationId = registerCheck.sourceCorrelationId,
                registerCheckResult = RegisterCheckResult.MULTIPLE_MINUS_MATCH,
                matches = registerCheck.registerCheckMatches.map { registerCheckMatch ->
                    with(registerCheckMatch) {
                        buildVcaRegisterCheckMatch(
                            personalDetail = buildVcaRegisterCheckPersonalDetailSqsFromEntity(personalDetail),
                            emsElectoralId = emsElectorId,
                            franchiseCode = franchiseCode ?: "",
                            registeredStartDate = registeredStartDate,
                            registeredEndDate = registeredEndDate,
                            postalVotingArrangement = postalVotingArrangement?.let(::buildVcaRegisterCheckVotingArrangementSqsFromEntity),
                            proxyVotingArrangement = proxyVotingArrangement?.let(::buildVcaRegisterCheckVotingArrangementSqsFromEntity),
                        )
                    }
                },
                historicalSearchEarliestDate = null,
            )

            // When
            val actual = mapper.fromRegisterCheckEntityToRegisterCheckResultMessage(registerCheck)

            // Then
            assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
            assertThat(actual.matches).hasSize(2)
            verify(checkStatusMapper).toRegisterCheckResultEnum(CheckStatus.MULTIPLE_MATCH)
            verify(instantMapper).toOffsetDateTime(null)
        }
    }
}
