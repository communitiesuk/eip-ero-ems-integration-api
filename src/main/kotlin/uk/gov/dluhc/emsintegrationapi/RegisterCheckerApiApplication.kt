package uk.gov.dluhc.emsintegrationapi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

/**
 * Spring Boot application bootstrapping class.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
class RegisterCheckerApiApplication

fun main(args: Array<String>) {
    runApplication<RegisterCheckerApiApplication>(*args)
}
