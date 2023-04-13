package uk.gov.dluhc.emsintegrationapi.database.repository

import org.junit.jupiter.api.TestInstance
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.dluhc.emsintegrationapi.config.MySQLContainerConfiguration

@DataJpaTest
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = ["uk.gov.dluhc.emsintegrationapi.database.entity"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractRepositoryIntegrationTest {

    companion object {
        const val GSS_CODE_1 = "E12345678"
        init {
            MySQLContainerConfiguration.getInstance()
        }
    }
}
