package uk.gov.dluhc.emsintegrationapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.OPTIONS
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration(
    private val apiProperties: ApiProperties
) {

    companion object {
        private val BYPASS_URLS_FOR_REQUEST_HEADER_AUTHENTICATION = listOf("/actuator/")
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http.also { httpSecurity ->
            httpSecurity
                .sessionManagement {
                    it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                }
                .cors { }
                .csrf().disable()
                .formLogin { it.disable() }
                .httpBasic { it.disable() }
                .authorizeHttpRequests {
                    it.requestMatchers(OPTIONS).permitAll()
                    it.requestMatchers("/actuator/**").permitAll()
                    it.anyRequest().authenticated()
                }
                .addFilter(
                    EmsIntegrationHeaderAuthenticationFilter(
                        apiProperties.requestHeaderName,
                        BYPASS_URLS_FOR_REQUEST_HEADER_AUTHENTICATION
                    )
                )
        }.build()
    }
}
