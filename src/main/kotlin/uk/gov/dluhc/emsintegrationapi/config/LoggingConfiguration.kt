package uk.gov.dluhc.emsintegrationapi.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.dluhc.logging.rest.CorrelationIdMdcInterceptor

@Configuration
class LoggingConfiguration : WebMvcConfigurer {

    private var correlationIdMdcInterceptor: CorrelationIdMdcInterceptor = CorrelationIdMdcInterceptor()

    @Override
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(correlationIdMdcInterceptor)
    }

}
