package com.example.codex.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    @Value("\${frontendUrl}")
    private val frontendUrl: String,
    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private val issuerUri: String,
    private val jwtAuthConverter: JwtAuthConverter,
) {
    companion object {
        private val AUTH_WHITELIST =
            arrayOf(
                "/api/users/register",
                "/api/auth/login",
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
            )
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors {
                it.configurationSource { request ->
                    CorsConfiguration().apply {
                        allowedOrigins = listOf(frontendUrl)
                        allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        allowedHeaders = listOf("*")
                        allowCredentials = true
                    }
                }
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            }.authorizeHttpRequests { authz ->
                authz
                    .requestMatchers(*AUTH_WHITELIST)
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }
        if (issuerUri.isNotBlank()) {
            http.oauth2ResourceServer { it.jwt { jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter) } }
        }
        return http.build()
    }
}
