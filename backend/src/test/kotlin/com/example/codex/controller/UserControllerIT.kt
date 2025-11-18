package com.example.codex.controller

import com.example.codex.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import org.mockito.BDDMockito.given
import org.mockito.ArgumentMatchers.anyString
import org.mockito.junit.jupiter.MockitoExtension

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ExtendWith(value = [MockitoExtension::class])
class UserControllerIT @Autowired constructor(
    private val webTestClient: WebTestClient,
) : AbstractIntegrationTest() {

    // This replaces your real JwtDecoder bean for the test context
    @MockBean
    private lateinit var jwtDecoder: JwtDecoder

    @Test
    fun `registers user and returns it from me`() {
        val username = "user-" + UUID.randomUUID()
        val externalId = UUID.randomUUID().toString()

        // 1) create the user through your public API (unauthenticated, whitelisted)
        webTestClient.post()
            .uri("/api/users/register")
            .body(
                BodyInserters.fromFormData("username", username)
                    .with("password", "secret")
                    .with("externalId", externalId)
            )
            .exchange()
            .expectStatus().isOk

        println("externalId: $externalId")

        // 2) Prepare a Jwt that looks like what your app expects
        val tokenValue = "dummy-token" // can be any string with valid bearer chars

        val now = Instant.now()
        val jwt: Jwt = Jwt.withTokenValue(tokenValue)
            .header("alg", "none")
            .subject(externalId)                      // jwt.subject
            .claim("preferred_username", username)    // extra claim used by your app
            .issuedAt(now)
            .expiresAt(now.plus(1, ChronoUnit.HOURS))
            .build()

        // 3) Make *any* call to jwtDecoder.decode(...) return our Jwt
        given(jwtDecoder.decode(anyString())).willReturn(jwt)

        // 4) Call /me with a bearer token; the mocked JwtDecoder will be used
        webTestClient.get()
            .uri("/api/users/me")
            .headers { it.setBearerAuth(tokenValue) }
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.username").isEqualTo(username)
    }
}
