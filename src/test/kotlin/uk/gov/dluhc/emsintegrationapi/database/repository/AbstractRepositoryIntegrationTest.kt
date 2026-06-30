package uk.gov.dluhc.emsintegrationapi.database.repository

import org.junit.jupiter.api.TestInstance
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.test.context.ActiveProfiles
import uk.gov.dluhc.emsintegrationapi.config.MySQLContainerConfiguration

@DataJpaTest
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = ["uk.gov.dluhc.emsintegrationapi.database.entity"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractRepositoryIntegrationTest {

    companion object {
        init {
            MySQLContainerConfiguration.getInstance()
        }
    }
}
