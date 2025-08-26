package com.example.codex.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap

import com.example.codex.config.JwtAuthConverter

@Service
class KeycloakSessionService(
    private val restTemplateBuilder: RestTemplateBuilder,
    private val jwtAuthConverter: JwtAuthConverter,
    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private val issuerUri: String,
    @Value("\${keycloak.client-id:}")
    private val clientId: String,
    @Value("\${keycloak.client-secret:}")
    private val clientSecret: String,
) {
    fun authenticate(username: String, password: String): JwtAuthenticationToken {
        val tokenEndpoint = "$issuerUri/protocol/openid-connect/token"
        val form = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "password")
            add("client_id", clientId)
            if (clientSecret.isNotBlank()) add("client_secret", clientSecret)
            add("username", username)
            add("password", password)
        }
        val response = restTemplateBuilder.build().postForObject(tokenEndpoint, form, Map::class.java)
        val accessToken = (response?.get("access_token") as? String)
            ?: throw IllegalStateException("No access token returned from Keycloak")
        val jwt = NimbusJwtDecoder.withIssuerLocation(issuerUri).build().decode(accessToken)
        val auth = jwtAuthConverter.convert(jwt)
        return auth as JwtAuthenticationToken
    }
}
