package com.example.codex.config

import com.example.codex.domain.User
import com.example.codex.service.UserService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig(
    private val userValidator: UserValidator,
    @Value("\${frontendUrl}")
    private val frontendUrl: String,
    @Value("\${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private val jwkSetUri: String,
) {
    companion object {
        private val AUTH_WHITELIST =
            arrayOf(
                "/api/users/register/**",
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
            )
    }

    @Bean
    fun jwtDecoder(): ReactiveJwtDecoder {
        val jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build()
        jwtDecoder.setJwtValidator(JwtValidators.createDefault())
        return jwtDecoder
    }

    @Bean
    fun jwtAuthenticationConverter(): Converter<Jwt, Mono<out AbstractAuthenticationToken>> {
        val delegate = ReactiveJwtAuthenticationConverterAdapter(JwtAuthenticationConverter())

        return object : Converter<Jwt, Mono<out AbstractAuthenticationToken>> {
            override fun convert(jwt: Jwt): Mono<out AbstractAuthenticationToken> =
                userValidator
                    .ensureUser(jwt)
                    .then(delegate.convert(jwt) ?: Mono.empty())
        }
    }

    @Bean
    fun securityFilterChain(
        http: ServerHttpSecurity,
        jwtDecoder: ReactiveJwtDecoder,
        jwtAuthenticationConverter: Converter<Jwt, Mono<out AbstractAuthenticationToken>>,
    ): SecurityWebFilterChain =
        http
            .csrf { it.disable() }
            .cors {
                it.configurationSource { _ ->
                    CorsConfiguration().apply {
                        allowedOrigins = listOf(frontendUrl)
                        allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        allowedHeaders = listOf("*")
                        allowCredentials = true
                    }
                }
            }.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .authorizeExchange { authz ->
                authz
                    .pathMatchers(*AUTH_WHITELIST)
                    .permitAll()
                    .anyExchange()
                    .authenticated()
            }.oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtDecoder(jwtDecoder)
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                }
            }.build()

    @Component
    class UserValidator(
        private val userService: UserService,
    ) {
        fun ensureUser(jwt: Jwt): Mono<User> {
            val externalId = jwt.subject
            val username = jwt.getClaimAsString("preferred_username")

            return userService
                .findByExternalId(externalId)
                .switchIfEmpty(
                    userService
                        .register(username, "12345678", externalId)
                        .flatMap { registered ->
                            if (registered) {
                                userService.findByExternalId(externalId)
                            } else {
                                Mono.empty()
                            }
                        },
                ).switchIfEmpty(Mono.error(UsernameNotFoundException("No user: $externalId")))
        }
    }
}
