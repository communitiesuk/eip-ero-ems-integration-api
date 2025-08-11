package uk.gov.dluhc.emsintegrationapi.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.RegisterCheck
import uk.gov.dluhc.emsintegrationapi.dto.PendingRegisterCheckDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.dto.buildPendingRegisterCheckDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.dto.buildPersonalDetailDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity.buildPersonalDetail
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity.buildRegisterCheck
import uk.gov.dluhc.registercheckerapi.models.PendingRegisterCheck
import uk.gov.dluhc.registercheckerapi.models.SourceSystem
import java.time.Instant
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
internal class PendingRegisterCheckMapperTest {

    @Mock
    private lateinit var instantMapper: InstantMapper

    @Mock
    private lateinit var personalDetailMapper: PersonalDetailMapper

    @InjectMocks
    private val mapper = PendingRegisterCheckMapperImpl()

    @Test
    fun `should map dto to entity`() {
        // Given
        val pendingRegisterCheckDto = buildPendingRegisterCheckDto()
        val expectedPersonalDetailEntity = buildPersonalDetail()
        given(personalDetailMapper.personalDetailDtoToPersonalDetailEntity(any())).willReturn(expectedPersonalDetailEntity)

        val expected = RegisterCheck(
            correlationId = pendingRegisterCheckDto.correlationId,
            sourceReference = pendingRegisterCheckDto.sourceReference,
            sourceCorrelationId = pendingRegisterCheckDto.sourceCorrelationId,
            createdBy = pendingRegisterCheckDto.createdBy,
            gssCode = pendingRegisterCheckDto.gssCode,
            status = CheckStatus.PENDING,
            personalDetail = expectedPersonalDetailEntity
        )

        // When
        val actual = mapper.pendingRegisterCheckDtoToRegisterCheckEntity(pendingRegisterCheckDto)

        // Then
        assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("status")
            .isEqualTo(expected)
        assertThat(actual.status).isEqualTo(CheckStatus.PENDING)
        assertThat(actual.dateCreated).isNull()
        verify(personalDetailMapper).personalDetailDtoToPersonalDetailEntity(pendingRegisterCheckDto.personalDetail)
        verifyNoMoreInteractions(personalDetailMapper)
        verifyNoInteractions(instantMapper)
    }

    @Test
    fun `should map entity to dto`() {
        // Given
        val registerCheckEntity = buildRegisterCheck()
        val expectedPersonalDetailDto = buildPersonalDetailDto()
        given(personalDetailMapper.personalDetailEntityToPersonalDetailDto(any())).willReturn(expectedPersonalDetailDto)

        val expected = PendingRegisterCheckDto(
            correlationId = registerCheckEntity.correlationId,
            sourceReference = registerCheckEntity.sourceReference,
            sourceCorrelationId = registerCheckEntity.sourceCorrelationId,
            createdBy = registerCheckEntity.createdBy,
            gssCode = registerCheckEntity.gssCode,
            createdAt = registerCheckEntity.dateCreated,
            personalDetail = expectedPersonalDetailDto,
            emsElectorId = registerCheckEntity.emsElectorId,
            historicalSearch = registerCheckEntity.historicalSearch,
        )

        // When
        val actual = mapper.registerCheckEntityToPendingRegisterCheckDto(registerCheckEntity)

        // Then
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
        verify(personalDetailMapper).personalDetailEntityToPersonalDetailDto(registerCheckEntity.personalDetail)
        verifyNoMoreInteractions(personalDetailMapper)
        verifyNoInteractions(instantMapper)
    }

    @Test
    fun `should map dto to model`() {
        // Given
        val createdAt = Instant.now()
        val pendingRegisterCheckDto = buildPendingRegisterCheckDto(createdAt = createdAt)
        given(instantMapper.toOffsetDateTime(any())).willReturn(createdAt.atOffset(ZoneOffset.UTC))

        val expected = PendingRegisterCheck(
            requestid = pendingRegisterCheckDto.correlationId,
            source = SourceSystem.EROP,
            gssCode = pendingRegisterCheckDto.gssCode,
            actingStaffId = "EROP",
            createdAt = pendingRegisterCheckDto.createdAt!!.atOffset(ZoneOffset.UTC),
            emsElectorId = null,
            historicalSearch = null,
            fn = pendingRegisterCheckDto.personalDetail.firstName,
            mn = pendingRegisterCheckDto.personalDetail.middleNames,
            ln = pendingRegisterCheckDto.personalDetail.surname,
            dob = pendingRegisterCheckDto.personalDetail.dateOfBirth,
            regproperty = pendingRegisterCheckDto.personalDetail.address.property,
            regstreet = pendingRegisterCheckDto.personalDetail.address.street,
            reglocality = pendingRegisterCheckDto.personalDetail.address.locality,
            regtown = pendingRegisterCheckDto.personalDetail.address.town,
            regarea = pendingRegisterCheckDto.personalDetail.address.area,
            regpostcode = pendingRegisterCheckDto.personalDetail.address.postcode,
            reguprn = pendingRegisterCheckDto.personalDetail.address.uprn,
            phone = pendingRegisterCheckDto.personalDetail.phone,
            email = pendingRegisterCheckDto.personalDetail.email,
        )

        // When
        val actual = mapper.pendingRegisterCheckDtoToPendingRegisterCheckModel(pendingRegisterCheckDto)

        // Then
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
        verify(instantMapper).toOffsetDateTime(createdAt)
        verifyNoInteractions(personalDetailMapper)
    }

    @Test
    fun `should map manual register check dto to model`() {
        // Given
        val createdAt = Instant.now()
        val pendingRegisterCheckDto = buildPendingRegisterCheckDto(
            createdAt = createdAt,
            createdBy = "joe.bloggs@gmail.com"
        )
        given(instantMapper.toOffsetDateTime(any())).willReturn(createdAt.atOffset(ZoneOffset.UTC))

        val expected = PendingRegisterCheck(
            requestid = pendingRegisterCheckDto.correlationId,
            source = SourceSystem.EROP,
            gssCode = pendingRegisterCheckDto.gssCode,
            actingStaffId = "joe.bloggs@gmail.com",
            createdAt = pendingRegisterCheckDto.createdAt!!.atOffset(ZoneOffset.UTC),
            fn = pendingRegisterCheckDto.personalDetail.firstName,
            mn = pendingRegisterCheckDto.personalDetail.middleNames,
            ln = pendingRegisterCheckDto.personalDetail.surname,
            dob = pendingRegisterCheckDto.personalDetail.dateOfBirth,
            phone = pendingRegisterCheckDto.personalDetail.phone,
            email = pendingRegisterCheckDto.personalDetail.email,
            regstreet = pendingRegisterCheckDto.personalDetail.address.street,
            regpostcode = pendingRegisterCheckDto.personalDetail.address.postcode,
            regproperty = pendingRegisterCheckDto.personalDetail.address.property,
            reglocality = pendingRegisterCheckDto.personalDetail.address.locality,
            regtown = pendingRegisterCheckDto.personalDetail.address.town,
            regarea = pendingRegisterCheckDto.personalDetail.address.area,
            reguprn = pendingRegisterCheckDto.personalDetail.address.uprn,
            emsElectorId = null,
            historicalSearch = null,
        )

        // When
        val actual = mapper.pendingRegisterCheckDtoToPendingRegisterCheckModel(pendingRegisterCheckDto)

        // Then
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
        verify(instantMapper).toOffsetDateTime(createdAt)
        verifyNoInteractions(personalDetailMapper)
    }
}
