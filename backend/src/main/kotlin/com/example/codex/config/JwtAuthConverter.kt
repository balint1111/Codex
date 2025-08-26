package com.example.codex.config

import com.example.codex.service.UserService
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
    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val externalId = jwt.subject
        val user = userService.findByExternalId(externalId) ?: throw UsernameNotFoundException("No user: $externalId")
        val authorities = user.privileges.map { SimpleGrantedAuthority(it.name) }
        return JwtAuthenticationToken(jwt, authorities, externalId)
    }
}
