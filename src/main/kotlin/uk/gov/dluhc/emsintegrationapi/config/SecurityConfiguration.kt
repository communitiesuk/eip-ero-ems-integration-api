package uk.gov.dluhc.emsintegrationapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod.OPTIONS
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration(
    @Value("\${dluhc.request.header.name}")
    private val requestHeaderName: String,
) {

    companion object {
        private val BYPASS_URLS_FOR_REQUEST_HEADER_AUTHENTICATION = listOf("/actuator/", "/admin/pending-checks/**")
    }

    @Bean
    @Order(1)
    fun adminPendingDownloadsFilterChain(
        http: HttpSecurity,
        @Value("\${spring.security.oauth2.resourceserver.jwt.admin-issuer}") adminIssuer: String,
        @Value("\${spring.security.oauth2.resourceserver.jwt.admin-issuer-uri}") adminIssuerUrl: String,
    ): SecurityFilterChain =
        baseSecurityFilterChain(http, "/admin/pending-downloads/**")
            .authorizeHttpRequests {
                it.anyRequest().authenticated()
            }
            .addFilter(RegisterCheckerHeaderAuthenticationFilter(requestHeaderName, emptyList()))
            .also { addOAuth2ResourceServer(it, adminIssuer, adminIssuerUrl) }
            .build()

    @Bean
    @Order(2)
    fun adminPendingChecksFilterChain(
        http: HttpSecurity,
        @Value("\${spring.security.oauth2.resourceserver.jwt.admin-issuer}") adminIssuer: String,
        @Value("\${spring.security.oauth2.resourceserver.jwt.admin-issuer-uri}") adminIssuerUrl: String,
    ): SecurityFilterChain =
        baseSecurityFilterChain(http, "/admin/pending-checks/**")
            .authorizeHttpRequests {
                it.anyRequest().authenticated()
            }
            .also { addOAuth2ResourceServer(it, adminIssuer, adminIssuerUrl) }
            .build()

    @Bean
    @Order(3)
    fun defaultFilterChain(http: HttpSecurity): SecurityFilterChain =
        baseSecurityFilterChain(http, "/**")
            .authorizeHttpRequests {
                it.requestMatchers(OPTIONS).permitAll()
                it.requestMatchers("/actuator/**").permitAll()
                it.anyRequest().authenticated()
            }
            .addFilter(RegisterCheckerHeaderAuthenticationFilter(requestHeaderName, BYPASS_URLS_FOR_REQUEST_HEADER_AUTHENTICATION))
            .build()

    private fun baseSecurityFilterChain(http: HttpSecurity, pattern: String) =
        http
            .securityMatcher(pattern)
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .cors { }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers(OPTIONS).permitAll()
            }

    private fun addOAuth2ResourceServer(
        http: HttpSecurity,
        adminIssuer: String,
        adminIssuerUrl: String,
    ) {
        http.oauth2ResourceServer { oauth2 ->
            oauth2.authenticationManagerResolver(
                JwtIssuerAuthenticationManagerResolver(
                    mapOf(adminIssuer to managerFor(adminIssuerUrl))::get
                )
            )
        }
    }

    private fun managerFor(issuer: String): AuthenticationManager {
        val jwtDecoder = NimbusJwtDecoder.withJwkSetUri(issuer).build()
        val authenticationProvider = JwtAuthenticationProvider(jwtDecoder)
        return AuthenticationManager(authenticationProvider::authenticate)
    }
}
