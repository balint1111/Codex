package com.example.codex.config

import com.example.codex.domain.User
import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtAuthConverter(
    private val userValidator: UserValidator,
) : Converter<Jwt, Mono<AbstractAuthenticationToken>> {
    companion object {
        private val logger = LoggerFactory.getLogger(JwtAuthConverter::class.java)
    }

    override fun convert(jwt: Jwt): Mono<AbstractAuthenticationToken> =
        userValidator.ensureUser(jwt).map { user ->
            val authorities = buildAuthorities(user)
            logger.debug("Authorities for subject {}: {}", jwt.subject, authorities)
            JwtAuthenticationToken(jwt, authorities, jwt.subject)
        }

    private fun buildAuthorities(user: User) = user.privileges.map { SimpleGrantedAuthority(it.name) }
}
