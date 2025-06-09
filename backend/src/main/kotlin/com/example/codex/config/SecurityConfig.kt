package com.example.codex.config

import com.example.codex.service.MyUserDetailsService
import com.example.codex.service.UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val userService: UserService
) {

    companion object {
        private val AUTH_WHITELIST = arrayOf(
            "/api/users/register",
        )
    }


    @Bean
    fun daoAuthenticationProvider(
        userDetailsService: UserDetailsService,
        passwordEncoder: PasswordEncoder,
    ): DaoAuthenticationProvider {
        return DaoAuthenticationProvider().apply {
            setUserDetailsService(userDetailsService)
            setPasswordEncoder(passwordEncoder)
        }
    }

    @Bean
    fun userDetailsService(): UserDetailsService = MyUserDetailsService(userService)

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager =
        authConfig.authenticationManager

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        authProvider: DaoAuthenticationProvider
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors {
                it.configurationSource { request ->
                    CorsConfiguration().apply {
                        allowedOrigins = listOf("http://localhost:4200")
                        allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        allowedHeaders = listOf("*")
                        allowCredentials = true
                    }
                }
            }
            .authenticationProvider(authProvider)
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }.authorizeHttpRequests { authz ->
                authz
                    .requestMatchers(*AUTH_WHITELIST).permitAll()
                    .anyRequest().authenticated()
            }.httpBasic { }
        return http.build()
    }

}

