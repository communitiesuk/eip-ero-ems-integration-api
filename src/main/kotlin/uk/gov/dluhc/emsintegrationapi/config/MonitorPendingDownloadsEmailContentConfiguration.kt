package uk.gov.dluhc.emsintegrationapi.config

import io.micrometer.core.instrument.util.IOUtils
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.io.ClassPathResource

/**
 * Class to bind configuration properties for the email content
 */
@ConfigurationProperties(prefix = "email.monitor-pending-downloads-content")
class MonitorPendingDownloadsEmailContentConfiguration(
    val subject: String,
    emailBodyTemplate: String,
    val recipients: String
) {
    val emailBody: String

    init {
        with(ClassPathResource(emailBodyTemplate)) {
            emailBody = inputStream.use {
                IOUtils.toString(it)
            }
        }
    }
}
