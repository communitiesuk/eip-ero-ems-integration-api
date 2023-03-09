package uk.gov.dluhc.emsintegrationapi.database.repository

import org.junit.rules.ExternalResource
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.CrudRepository
import org.springframework.test.context.ActiveProfiles
import uk.gov.dluhc.emsintegrationapi.config.MySQLContainerConfiguration

@DataJpaTest
@ActiveProfiles("integration-test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = ["uk.gov.dluhc.emsintegrationapi.database.entity"])
abstract class AbstractRepositoryTest {

    companion object {
        init {
            MySQLContainerConfiguration.getInstance()
        }

        private val repositoryList = mutableListOf<CrudRepository<*, *>>()
        fun init(repository: CrudRepository<*, *>) = repositoryList.add(repository)
        fun deleteAll() {
            repositoryList.forEach { crudRepository -> crudRepository.deleteAll() }
        }
    }

    class MySQLTestServer : ExternalResource() {
        override fun after() {
            deleteAll()
        }
    }
}

