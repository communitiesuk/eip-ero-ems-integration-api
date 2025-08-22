package uk.gov.dluhc.emsintegrationapi.messaging.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.dluhc.emsintegrationapi.dto.AddressDto
import uk.gov.dluhc.emsintegrationapi.dto.PendingRegisterCheckDto
import uk.gov.dluhc.emsintegrationapi.dto.PersonalDetailDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.messaging.buildInitiateRegisterCheckMessage
import java.util.UUID

@ExtendWith(MockitoExtension::class)
internal class InitiateRegisterCheckMapperTest {

    @InjectMocks
    private val mapper = InitiateRegisterCheckMapperImpl()

    @Test
    fun `should map model to dto`() {
        // Given
        val message = buildInitiateRegisterCheckMessage()

        val expected = PendingRegisterCheckDto(
            correlationId = UUID.randomUUID(),
            sourceReference = message.sourceReference,
            sourceCorrelationId = message.sourceCorrelationId,
            createdBy = message.requestedBy,
            gssCode = message.gssCode,
            personalDetail = with(message.personalDetail) {
                PersonalDetailDto(
                    firstName = firstName,
                    middleNames = middleNames,
                    surname = surname,
                    dateOfBirth = dateOfBirth,
                    phone = phone,
                    email = email,
                    address = AddressDto(
                        property = address.property,
                        street = address.street,
                        locality = address.locality,
                        town = address.town,
                        area = address.area,
                        postcode = address.postcode,
                        uprn = address.uprn,
                        createdBy = null,
                    )
                )
            },
            emsElectorId = message.emsElectorId,
            historicalSearch = message.historicalSearch,
        )

        // When
        val actual = mapper.initiateCheckMessageToPendingRegisterCheckDto(message)

        // Then
        assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("correlationId")
            .isEqualTo(expected)
        assertThat(actual.correlationId).isNotNull
        assertThat(actual.createdAt).isNull()
    }
}
