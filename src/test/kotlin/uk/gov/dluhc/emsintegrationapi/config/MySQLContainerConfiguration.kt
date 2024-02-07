package uk.gov.dluhc.emsintegrationapi.config

import org.springframework.context.annotation.Configuration
import org.testcontainers.containers.MySQLContainer

@Configuration
class MySQLContainerConfiguration : MySQLContainer<MySQLContainerConfiguration>(MYSQL_IMAGE) {
    companion object {
        private const val MYSQL_IMAGE = "mysql:8.2"
        private const val DATABASE = "ems_integration_application"
        private const val USER = "root"
        private const val PASSWORD = "password"
        private const val DATASOURCE_URL = "spring.datasource.url"
        private var container: MySQLContainerConfiguration? = getInstance()

        fun getInstance(): MySQLContainerConfiguration {
            if (container == null) {
                container = MySQLContainerConfiguration().withDatabaseName(DATABASE)
                    .withUsername(USER)
                    .withPassword(PASSWORD)
                    .withReuse(true)
                    .withCreateContainerCmdModifier { it.withName("ems-integration-api-integration-test-mysql") }
                    .also {
                        it.start()
                        System.setProperty(DATASOURCE_URL, it.jdbcUrl)
                    }
            }
            return container!!
        }
    }
}
