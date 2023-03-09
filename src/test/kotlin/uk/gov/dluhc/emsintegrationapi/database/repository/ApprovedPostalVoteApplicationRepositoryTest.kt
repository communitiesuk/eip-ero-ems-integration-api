package uk.gov.dluhc.emsintegrationapi.database.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApprovedPostalApplication
import javax.annotation.PostConstruct

class ApprovedPostalVoteApplicationRepositoryTest : AbstractRepositoryTest() {

    companion object {
        @ClassRule
        @JvmField
        val mySQLTestServer = MySQLTestServer()
    }

    @Autowired
    private lateinit var approvedPostalVoteApplicationRepository: ApprovedPostalVoteApplicationRepository

    @PostConstruct
    fun init() = init(approvedPostalVoteApplicationRepository)

    @Test
    fun shouldSaveAnApprovedPostalVoteApplication() {
        // Given
        val approvedPostalVoteApplication = buildApprovedPostalApplication()

        // When
        val savedApplication = approvedPostalVoteApplicationRepository.save(approvedPostalVoteApplication)

        // Then
        assertThat(savedApplication).isNotNull
    }
}
