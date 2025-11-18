package com.example.codex.config

import com.example.codex.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
class JwtAuthConverter(
    private val userService: UserService,
) : Converter<Jwt, AbstractAuthenticationToken> {
    companion object {
        private val logger = LoggerFactory.getLogger(JwtAuthConverter::class.java)
    }

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val externalId = jwt.subject
        logger.debug("Authenticating JWT for subject {}", externalId)

        val user = userService.findByExternalId(externalId).block()
        if (user == null) {
            logger.warn("User not found for subject {}", externalId)
            throw UsernameNotFoundException("No user: $externalId")
        }

        val authorities = user.privileges.map { SimpleGrantedAuthority(it.name) }
        logger.debug("Authorities for subject {}: {}", externalId, authorities)

        return JwtAuthenticationToken(jwt, authorities, externalId)
    }
}
