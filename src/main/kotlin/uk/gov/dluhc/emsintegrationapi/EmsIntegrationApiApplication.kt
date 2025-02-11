package uk.gov.dluhc.emsintegrationapi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

/**
 * Spring Boot application bootstrapping class.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
class EmsIntegrationApiApplication

fun main(args: Array<String>) {
    runApplication<EmsIntegrationApiApplication>(*args)
}
