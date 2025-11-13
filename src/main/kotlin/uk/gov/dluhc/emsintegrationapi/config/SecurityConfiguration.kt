package uk.gov.dluhc.emsintegrationapi.config

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.OPTIONS
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationManagerResolver
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver
import org.springframework.security.web.SecurityFilterChain
import org.springframework.util.AntPathMatcher

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration(
    @Value("\${dluhc.request.header.name}")
    private val requestHeaderName: String,
) {

    companion object {
        private val BYPASS_URLS_FOR_REQUEST_HEADER_AUTHENTICATION = listOf("/actuator/", "/admin/pending-checks/**")
        private val ADMIN_ENDPOINTS = listOf("/admin/pending-checks/**", "/admin/pending-downloads/**")
    }

    @Bean
    fun filterChain(
        http: HttpSecurity,
        @Value("\${spring.security.oauth2.resourceserver.jwt.admin-issuer}") adminIssuer: String,
        @Value("\${spring.security.oauth2.resourceserver.jwt.admin-issuer-uri}") adminIssuerUrl: String,
    ): SecurityFilterChain =
        http
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .cors { }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers(OPTIONS).permitAll()
                it.requestMatchers("/actuator/**").permitAll()
                it.anyRequest().authenticated()
            }
            // This is only used for authentication on the admin endpoints
            .oauth2ResourceServer { oAuth2ResourceServerConfigurer ->
                oAuth2ResourceServerConfigurer.authenticationManagerResolver(
                    adminJwtAuthenticationManagerResolver(
                        adminIssuer = adminIssuer,
                        adminIssuerUrl = adminIssuerUrl,
                    )
                )
            }
            .addFilter(RegisterCheckerHeaderAuthenticationFilter(requestHeaderName, BYPASS_URLS_FOR_REQUEST_HEADER_AUTHENTICATION))
            .build()

    private fun adminJwtAuthenticationManagerResolver(
        adminIssuer: String,
        adminIssuerUrl: String,
    ): AuthenticationManagerResolver<HttpServletRequest> {
        val pathMatcher = AntPathMatcher()
        val jwtResolver = JwtIssuerAuthenticationManagerResolver(
            mapOf(
                adminIssuer to managerFor(adminIssuerUrl)
            )::get
        )

        return AuthenticationManagerResolver { request ->
            val requestPath = request.requestURI
            if (ADMIN_ENDPOINTS.any { pattern -> pathMatcher.match(pattern, requestPath) }) {
                jwtResolver.resolve(request)
            } else {
                null
            }
        }
    }

    fun managerFor(issuer: String): AuthenticationManager {
        val jwtDecoder = NimbusJwtDecoder.withJwkSetUri(issuer).build()
        val authenticationProvider = JwtAuthenticationProvider(jwtDecoder)
        return AuthenticationManager(authenticationProvider::authenticate)
    }
}
