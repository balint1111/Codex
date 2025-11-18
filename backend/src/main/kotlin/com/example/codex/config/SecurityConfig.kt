package com.example.codex.config

import com.example.codex.service.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import reactor.core.publisher.Mono


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val userValidator: UserValidator,
    @Value("\${frontendUrl}")
    private val frontendUrl: String,
    @Value("\${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private val jwkSetUri: String?
) {
    companion object {
        private val AUTH_WHITELIST =
            arrayOf(
                "/api/users/register",
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
            )
    }


    @Bean
    fun jwtDecoder(): JwtDecoder {
        val jwtDecoder: NimbusJwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build()
        println("jwkSetUri $jwkSetUri")
        val withIssuer: OAuth2TokenValidator<Jwt> = JwtValidators.createDefault()
        val withAudience: OAuth2TokenValidator<Jwt> = DelegatingOAuth2TokenValidator(withIssuer, userValidator)

        jwtDecoder.setJwtValidator(withAudience)

        return jwtDecoder
    }

    @Component
    class UserValidator(private val userService: UserService) : OAuth2TokenValidator<Jwt> {

        private fun error() = OAuth2Error("ERR-SAVE", "Error while saving user id", null)

        override fun validate(jwt: Jwt): OAuth2TokenValidatorResult = try {
            println("validate")
            userService.findByExternalId(jwt.subject).flatMap {
                println("value: $it")
                if (it == null) {
                    userService.register(jwt.getClaim("preferred_username"), "12345678", jwt.subject)
                } else {
                    Mono.empty()
                }
            }.onErrorMap { error(it) }.block()
            OAuth2TokenValidatorResult.success()
        } catch (e: Exception) {
            OAuth2TokenValidatorResult.failure(error())
        }
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtDecoder: JwtDecoder,
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
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }.authorizeHttpRequests { authz ->
                authz
                    .requestMatchers(*AUTH_WHITELIST)
                    .permitAll()
                    .anyRequest()
                    .authenticated()
        }
        http.oauth2ResourceServer { oauth2 ->
            oauth2.jwt {
                it.decoder(jwtDecoder)
            }
        }
        println("security end")
        return http.build()
    }
}
