package com.example.codex.controller

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = ["*"])
class AuthController(
    private val restTemplate: RestTemplate,
    @Value("\${keycloak.token-uri:}")
    private val tokenUri: String,
    @Value("\${keycloak.client-id:}")
    private val clientId: String,
    @Value("\${keycloak.client-secret:}")
    private val clientSecret: String,
    @Value("\${keycloak.redirect-uri:}")
    private val redirectUri: String,
) {
    data class CodeRequest(
        val code: String,
    )

    data class TokenResponse(
        @JsonProperty("access_token")
        val accessToken: String,
        @JsonProperty("refresh_token")
        val refreshToken: String?,
    )

    @PostMapping("/token")
    fun exchangeCode(
        @RequestBody request: CodeRequest,
        session: HttpSession,
    ): TokenResponse {
        val form =
            LinkedMultiValueMap<String, String>().apply {
                add("grant_type", "authorization_code")
                add("code", request.code)
                add("client_id", clientId)
                add("client_secret", clientSecret)
                add("redirect_uri", redirectUri)
            }
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }
        val entity = HttpEntity(form, headers)
        val response =
            restTemplate.postForObject(tokenUri, entity, Map::class.java)
                ?: throw IllegalStateException("No response from token endpoint")
        val accessToken =
            response["access_token"] as? String
                ?: throw IllegalStateException("Access token missing in response")
        session.setAttribute("accessToken", accessToken)
        val refreshToken = response["refresh_token"] as? String
        return TokenResponse(accessToken, refreshToken)
    }
}
