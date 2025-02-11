package uk.gov.dluhc.emsintegrationapi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

/**
 * Spring Boot application bootstrapping class.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaAuditing
class EmsIntegrationApiApplication

fun main(args: Array<String>) {
    runApplication<EmsIntegrationApiApplication>(*args)
}
