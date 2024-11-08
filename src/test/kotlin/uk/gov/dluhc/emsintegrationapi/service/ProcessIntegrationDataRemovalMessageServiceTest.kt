package uk.gov.dluhc.emsintegrationapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicationDetails
import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicationDetails.EmsStatus
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.messaging.models.RemoveVoterApplicationEmsDataMessage
import uk.gov.dluhc.emsintegrationapi.messaging.models.RemoveVoterApplicationEmsDataMessage.Source.POSTAL
import uk.gov.dluhc.emsintegrationapi.messaging.models.RemoveVoterApplicationEmsDataMessage.Source.PROXY
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ProcessIntegrationDataRemovalMessageServiceTest {

    @Mock
    private lateinit var postalVoteApplicationRepository: PostalVoteApplicationRepository

    @Mock
    private lateinit var proxyVoteApplicationRepository: ProxyVoteApplicationRepository

    @InjectMocks
    private lateinit var service: ProcessIntegrationDataRemovalMessageService

    // -----------------------------------------------------------
    // PROXY
    // -----------------------------------------------------------

    @ParameterizedTest
    @EnumSource(names = ["SUCCESS", "FAILURE"])
    fun `should process postal integration data removal message where emsStatus is SUCCESS or FAILURE`(emsStatusType: EmsStatus) {
        val postalVoteApplication = mock(PostalVoteApplication::class.java)
        val applicationDetails = mock(ApplicationDetails::class.java)
        val message = RemoveVoterApplicationEmsDataMessage(
            "123",
            POSTAL,
        )

        // Given
        given(postalVoteApplication.applicationDetails).willReturn(applicationDetails)
        given(applicationDetails.emsStatus).willReturn(emsStatusType)
        given(postalVoteApplicationRepository.findById(anyString())).willReturn(Optional.of(postalVoteApplication))

        // When
        service.process(message)

        // Then
        verify(postalVoteApplicationRepository).delete(postalVoteApplication)
    }

    @Test
    fun `should reject postal integration data removal message where application not found`() {
        val applicationId = "123"
        val postalVoteApplication = mock(PostalVoteApplication::class.java)
        val message = RemoveVoterApplicationEmsDataMessage(
            applicationId,
            POSTAL,
        )

        // Given
        given(postalVoteApplicationRepository.findById(anyString())).willReturn(Optional.empty())

        // When
        assertThat(
            assertThrows<ApplicationNotFoundException> {
                service.process(message)
            }.message
        ).isEqualTo("The Postal application could not be found with id `$applicationId`")

        // Then
        verify(postalVoteApplicationRepository, never()).delete(postalVoteApplication)
    }

    @Test
    fun `should reject postal integration data removal message where emsStatus is null`() {
        val applicationId = "123"
        val postalVoteApplication = mock(PostalVoteApplication::class.java)
        val applicationDetails = mock(ApplicationDetails::class.java)
        val message = RemoveVoterApplicationEmsDataMessage(
            applicationId,
            POSTAL,
        )

        // Given
        given(postalVoteApplication.applicationDetails).willReturn(applicationDetails)
        given(applicationDetails.emsStatus).willReturn(null)
        given(postalVoteApplicationRepository.findById(anyString())).willReturn(Optional.of(postalVoteApplication))

        // When
        assertThat(
            assertThrows<IntegrationDataRemovalFailedException> {
                service.process(message)
            }.message
        ).isEqualTo(
            "The ems status of $POSTAL application with id `$applicationId` " +
                "was not found so it could not be processed"
        )

        // Then
        verify(postalVoteApplicationRepository, never()).delete(postalVoteApplication)
    }

    // -----------------------------------------------------------
    // PROXY
    // -----------------------------------------------------------

    @ParameterizedTest
    @EnumSource(names = ["SUCCESS", "FAILURE"])
    fun `should process proxy integration data removal message where emsStatus is SUCCESS or FAILURE`(emsStatusType: EmsStatus) {
        val proxyVoteApplication = mock(ProxyVoteApplication::class.java)
        val applicationDetails = mock(ApplicationDetails::class.java)
        val message = RemoveVoterApplicationEmsDataMessage(
            "123",
            PROXY,
        )

        // Given
        given(proxyVoteApplication.applicationDetails).willReturn(applicationDetails)
        given(applicationDetails.emsStatus).willReturn(emsStatusType)
        given(proxyVoteApplicationRepository.findById(anyString())).willReturn(Optional.of(proxyVoteApplication))

        // When
        service.process(message)

        // Then
        verify(proxyVoteApplicationRepository).delete(proxyVoteApplication)
    }

    @Test
    fun `should reject proxy integration data removal message where application not found`() {
        val applicationId = "123"
        val proxyVoteApplication = mock(ProxyVoteApplication::class.java)
        val message = RemoveVoterApplicationEmsDataMessage(
            applicationId,
            PROXY,
        )

        // Given
        given(proxyVoteApplicationRepository.findById(anyString())).willReturn(Optional.empty())

        // When
        assertThat(
            assertThrows<ApplicationNotFoundException> {
                service.process(message)
            }.message
        ).isEqualTo("The Proxy application could not be found with id `$applicationId`")

        // Then
        verify(proxyVoteApplicationRepository, never()).delete(proxyVoteApplication)
    }

    @Test
    fun `should reject proxy integration data removal message where emsStatus is null`() {
        val applicationId = "123"
        val proxyVoteApplication = mock(ProxyVoteApplication::class.java)
        val applicationDetails = mock(ApplicationDetails::class.java)
        val message = RemoveVoterApplicationEmsDataMessage(
            applicationId,
            PROXY,
        )

        // Given
        given(proxyVoteApplication.applicationDetails).willReturn(applicationDetails)
        given(applicationDetails.emsStatus).willReturn(null)
        given(proxyVoteApplicationRepository.findById(anyString())).willReturn(Optional.of(proxyVoteApplication))

        // When
        assertThat(
            assertThrows<IntegrationDataRemovalFailedException> {
                service.process(message)
            }.message
        ).isEqualTo(
            "The ems status of $PROXY application with id `$applicationId` " +
                "was not found so it could not be processed"
        )

        // Then
        verify(proxyVoteApplicationRepository, never()).delete(proxyVoteApplication)
    }
}
