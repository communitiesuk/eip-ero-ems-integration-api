package uk.gov.dluhc.emsintegrationapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.dluhc.emsintegrationapi.client.IerApiClient
import uk.gov.dluhc.emsintegrationapi.client.IerGeneralException
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.models.buildIerEroDetails
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.models.buildIerLocalAuthorityDetails

@ExtendWith(MockitoExtension::class)
internal class RetrieveEroNameServiceTest {

    @Mock
    private lateinit var ierApiClient: IerApiClient

    @InjectMocks
    private lateinit var retrieveEroNameService: RetrieveEroNameService

    @Test
    fun `should return ERO names by gss code given EROs with multiple local authorities`() {
        // Given
        given(ierApiClient.getEros()).willReturn(
            listOf(
                buildIerEroDetails(
                    name = "Camden Council",
                    localAuthorities = listOf(
                        buildIerLocalAuthorityDetails(gssCode = "E00000001"),
                        buildIerLocalAuthorityDetails(gssCode = "E00000002"),
                    ),
                ),
                buildIerEroDetails(
                    name = "Westminster Council",
                    localAuthorities = listOf(
                        buildIerLocalAuthorityDetails(gssCode = "E00000003"),
                    ),
                ),
            )
        )

        // When
        val actual = retrieveEroNameService.getEroNamesByGssCode()

        // Then
        assertThat(actual).isEqualTo(
            mapOf(
                "E00000001" to "Camden Council",
                "E00000002" to "Camden Council",
                "E00000003" to "Westminster Council",
            )
        )
        verify(ierApiClient).getEros()
    }

    @Test
    fun `should return empty map given IER API client throws exception`() {
        // Given
        given(ierApiClient.getEros()).willThrow(IerGeneralException("Error getting EROs from IER API"))

        // When
        val actual = retrieveEroNameService.getEroNamesByGssCode()

        // Then
        assertThat(actual).isEmpty()
        verify(ierApiClient).getEros()
    }
}
