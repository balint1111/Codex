package com.example.codex.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.http.HttpMethod

@Configuration
open class SecurityConfig {
    @Bean
    open fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .cors{}
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                it.anyRequest().authenticated()
            }
            .httpBasic{}
        return http.build()
    }

    @Bean
    open fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
